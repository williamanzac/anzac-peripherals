package anzac.peripherals.tiles;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.ForgeDirection;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidContainerRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fluids.FluidTankInfo;
import net.minecraftforge.fluids.IFluidHandler;
import anzac.peripherals.AnzacPeripheralsCore;
import anzac.peripherals.annotations.PeripheralMethod;
import anzac.peripherals.utils.Position;
import anzac.peripherals.utils.Utils;
import dan200.computer.api.IComputerAccess;

public class FluidRouterTileEntity extends BasePeripheralTileEntity implements IFluidHandler {

	private static final List<String> METHOD_NAMES = getMethodNames(FluidRouterTileEntity.class);
	public FluidTank fluidTank = new FluidTank(FluidContainerRegistry.BUCKET_VOLUME);

	@Override
	public String getType() {
		return "FluidRouter";
	}

	@Override
	protected List<String> methodNames() {
		final List<String> methodNames = super.methodNames();
		methodNames.addAll(METHOD_NAMES);
		return methodNames;
	}

	@PeripheralMethod
	private Object tankInfo() throws Exception {
		return tankInfo(ForgeDirection.UNKNOWN);
	}

	@PeripheralMethod
	private Object tankInfo(final ForgeDirection direction) throws Exception {
		final TileEntity te;
		if (direction == ForgeDirection.UNKNOWN) {
			te = this;
		} else {
			final Position pos = new Position(xCoord, yCoord, zCoord, direction);
			pos.moveForwards(1);
			te = worldObj.getBlockTileEntity(pos.x, pos.y, pos.z);
			if (te == null || !(te instanceof IFluidHandler)) {
				throw new Exception("Fluid Handler not found");
			}
		}
		final IFluidHandler handler = (IFluidHandler) te;
		final ForgeDirection opposite = direction.getOpposite();
		final FluidTankInfo[] tankInfo = handler.getTankInfo(opposite);
		final Map<Integer, Map<Integer, Integer>> table = new HashMap<Integer, Map<Integer, Integer>>();
		for (final FluidTankInfo info : tankInfo) {
			if (info != null) {
				final FluidStack fluid = info.fluid;
				final int uuid = Utils.getUUID(fluid);
				final int amountI = fluid == null ? 0 : fluid.amount;
				final int capacity = info.capacity;
				if (table.containsKey(uuid)) {
					final Map<Integer, Integer> map = table.get(uuid);
					map.put(0, map.get(0) + amountI);
					map.put(1, map.get(1) + capacity);
				} else {
					final Map<Integer, Integer> list = new HashMap<Integer, Integer>();
					table.put(uuid, list);
					list.put(0, amountI);
					list.put(1, capacity);
				}
			}
		}
		AnzacPeripheralsCore.logger.info("table:" + table);
		return table;
	}

	@PeripheralMethod
	private Object routeFrom(final ForgeDirection fromDir, final int amount) throws Exception {
		final Position pos = new Position(xCoord, yCoord, zCoord, fromDir);
		pos.moveForwards(1);
		final TileEntity te = worldObj.getBlockTileEntity(pos.x, pos.y, pos.z);
		if (te == null || !(te instanceof IFluidHandler)) {
			throw new Exception("Fluid Handler not found");
		}
		final IFluidHandler handler = (IFluidHandler) te;
		final ForgeDirection opposite = fromDir.getOpposite();
		AnzacPeripheralsCore.logger.info("opposite:" + opposite);
		final int canDrain = handler.drain(opposite, amount, false).amount;
		AnzacPeripheralsCore.logger.info("canDrain:" + canDrain);
		if (canDrain > 0) {
			final FluidStack fluidStack = handler.drain(opposite, amount, true);
			AnzacPeripheralsCore.logger.info("fluidStack:" + fluidStack);
			fill(fromDir, fluidStack, true);
			AnzacPeripheralsCore.logger.info("amount:" + fluidStack.amount);
			return fluidStack.amount;
		}
		return null;
	}

	@PeripheralMethod
	private int routeTo(final ForgeDirection toDir, final int amount) {
		final FluidStack copy = fluidTank.getFluid().copy();
		copy.amount = amount;
		AnzacPeripheralsCore.logger.info("copy:" + copy);
		final int amount1 = copy.amount;
		copy.amount -= Utils.addToFluidHandler(worldObj, xCoord, yCoord, zCoord, toDir, copy);
		final int toDrain = amount1 - copy.amount;
		if (toDrain > 0) {
			internalDrain(toDir, toDrain, true);
		}
		AnzacPeripheralsCore.logger.info("amount:" + copy.amount);
		return amount - copy.amount;
	}

	@Override
	public void attach(final IComputerAccess computer) {
		super.attach(computer);
		AnzacPeripheralsCore.fluidRouterMap.put(computer.getID(), this);
	}

	@Override
	public void detach(final IComputerAccess computer) {
		AnzacPeripheralsCore.fluidRouterMap.remove(computer.getID());
		super.detach(computer);
	}

	@Override
	public void readFromNBT(final NBTTagCompound tagCompound) {
		super.readFromNBT(tagCompound);

		// read tank
		if (tagCompound.hasKey("fluid")) {
			final NBTTagCompound tagFluid = (NBTTagCompound) tagCompound.getTag("fluid");
			fluidTank.readFromNBT(tagFluid);
		}
	}

	@Override
	public void writeToNBT(final NBTTagCompound tagCompound) {
		super.writeToNBT(tagCompound);

		// write tank
		if (fluidTank != null) {
			final NBTTagCompound tagFluid = new NBTTagCompound();
			fluidTank.writeToNBT(tagFluid);
			tagCompound.setTag("fluid", tagFluid);
		}
	}

	@Override
	public int fill(final ForgeDirection from, final FluidStack resource, final boolean doFill) {
		if (resource == null || !isAllowed(resource.getFluid())) {
			return 0;
		}
		final int fill = internalFill(resource, doFill);
		if (fill > 0 && doFill) {
			for (final IComputerAccess computer : computers.keySet()) {
				computer.queueEvent("fluid_sort", new Object[] { Utils.getUUID(resource), resource.amount });
			}
		}
		return fill;
	}

	protected int internalFill(final FluidStack resource, final boolean doFill) {
		return fluidTank.fill(resource, doFill);
	}

	@Override
	public FluidStack drain(final ForgeDirection from, final FluidStack resource, final boolean doDrain) {
		// cannot drain
		return null;
	}

	protected FluidStack internalDrain(final ForgeDirection from, final FluidStack resource, final boolean doDrain) {
		if (resource == null) {
			return null;
		}
		if (!resource.isFluidEqual(fluidTank.getFluid())) {
			return null;
		}
		return internalDrain(from, resource.amount, doDrain);
	}

	@Override
	public FluidStack drain(final ForgeDirection from, final int maxDrain, final boolean doDrain) {
		// cannot drain
		return null;
	}

	protected FluidStack internalDrain(final ForgeDirection from, final int maxDrain, final boolean doDrain) {
		return fluidTank.drain(maxDrain, doDrain);
	}

	@Override
	public boolean canFill(final ForgeDirection from, final Fluid fluid) {
		return isConnected() && isAllowed(fluid);
	}

	protected boolean isAllowed(final Fluid fluid) {
		return true;
	}

	@Override
	public boolean canDrain(final ForgeDirection from, final Fluid fluid) {
		return false;
	}

	@Override
	public FluidTankInfo[] getTankInfo(final ForgeDirection from) {
		return new FluidTankInfo[] { fluidTank.getInfo() };
	}

	@Override
	public void updateEntity() {
		if (worldObj == null) { // sanity check
			return;
		}

		super.updateEntity();
		if (!worldObj.isRemote) {
			if (fluidTank != null && fluidTank.getFluid() != null && fluidTank.getFluid().amount > 0
					&& worldObj.getTotalWorldTime() % 10 == 0 && isConnected()) {
				// routeFluid();
			}
		}
	}

	@Override
	protected boolean requiresMount() {
		return false;
	}
}
