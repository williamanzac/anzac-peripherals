package anzac.peripherals.tiles;

import java.util.HashMap;
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

public class FluidRouterTileEntity extends BaseRouterTileEntity implements IFluidHandler {

	public FluidTank fluidTank = new FluidTank(FluidContainerRegistry.BUCKET_VOLUME);

	@Override
	public String getType() {
		return "FluidRouter";
	}

	@Override
	@PeripheralMethod
	public int routeTo(final ForgeDirection toDir, final ForgeDirection insetDir, final int amount) throws Exception {
		final FluidStack copy = fluidTank.getFluid().copy();
		copy.amount = amount;
		// AnzacPeripheralsCore.logger.info("copy:" + copy);
		final int amount1 = copy.amount;
		copy.amount -= Utils.addToFluidHandler(worldObj, xCoord, yCoord, zCoord, toDir, insetDir, copy);
		final int toDrain = amount1 - copy.amount;
		if (toDrain > 0) {
			fluidTank.drain(toDrain, true);
		}
		// AnzacPeripheralsCore.logger.info("amount:" + copy.amount);
		return amount - copy.amount;
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
		if (resource == null) {
			return 0;
		}
		final int fill = fluidTank.fill(resource, doFill);
		if (fill > 0 && doFill) {
			for (final IComputerAccess computer : computers) {
				computer.queueEvent("fluid_route", new Object[] { computer.getAttachmentName(),
						Utils.getUUID(resource), resource.amount });
			}
		}
		return fill;
	}

	@Override
	public FluidStack drain(final ForgeDirection from, final FluidStack resource, final boolean doDrain) {
		// cannot drain
		return null;
	}

	@Override
	public FluidStack drain(final ForgeDirection from, final int maxDrain, final boolean doDrain) {
		// cannot drain
		return null;
	}

	@Override
	public boolean canFill(final ForgeDirection from, final Fluid fluid) {
		return isConnected();
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
	@PeripheralMethod
	public Object contents(final ForgeDirection direction, final ForgeDirection dir) throws Exception {
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
		final FluidTankInfo[] tankInfo = handler.getTankInfo(dir);
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

	@Override
	@PeripheralMethod
	public Object extractFrom(final ForgeDirection fromDir, final int uuid, final int amount,
			final ForgeDirection extractSide) throws Exception {
		final Position pos = new Position(xCoord, yCoord, zCoord, fromDir);
		pos.moveForwards(1);
		final TileEntity te = worldObj.getBlockTileEntity(pos.x, pos.y, pos.z);
		if (te == null || !(te instanceof IFluidHandler)) {
			throw new Exception("Fluid Handler not found");
		}
		final IFluidHandler handler = (IFluidHandler) te;
		// AnzacPeripheralsCore.logger.info("opposite:" + extractSide);
		final int canDrain = handler.drain(extractSide, amount, false).amount;
		// AnzacPeripheralsCore.logger.info("canDrain:" + canDrain);
		if (canDrain > 0) {
			final FluidStack fluidStack = handler.drain(extractSide, amount, true);
			// AnzacPeripheralsCore.logger.info("fluidStack:" + fluidStack);
			fill(fromDir, fluidStack, true);
			// AnzacPeripheralsCore.logger.info("amount:" + fluidStack.amount);
			return fluidStack.amount;
		}
		return null;
	}

	@Override
	@PeripheralMethod
	public int sendTo(String label, int amount) throws Exception {
		if (fluidTank.getFluid() == null) {
			throw new Exception("No fluid to transfer");
		}
		if (amount <= 0) {
			throw new Exception("Amount must be greater than 0");
		}
		final BasePeripheralTileEntity entity = AnzacPeripheralsCore.peripheralLabels.get(label);
		if (entity == null) {
			throw new Exception("No entity found with label " + label);
		}
		if (!(entity instanceof IFluidHandler)) {
			throw new Exception("Invalid target for label " + label);
		}
		final IFluidHandler tank = (IFluidHandler) entity;
		final FluidStack copy = fluidTank.getFluid().copy();
		if (amount < fluidTank.getFluidAmount()) {
			copy.amount = amount;
		}
		// AnzacPeripheralsCore.logger.info("copy:" + copy);
		final int amount1 = copy.amount;
		copy.amount -= tank.fill(ForgeDirection.UNKNOWN, copy, true);
		final int toDrain = amount1 - copy.amount;
		if (toDrain > 0) {
			fluidTank.drain(toDrain, true);
		}
		// AnzacPeripheralsCore.logger.info("amount:" + copy.amount);
		return amount - copy.amount;
	}

	@Override
	@PeripheralMethod
	public int requestFrom(String label, int uuid, int amount) throws Exception {
		if (amount <= 0) {
			throw new Exception("Amount must be greater than 0");
		}
		final BasePeripheralTileEntity entity = AnzacPeripheralsCore.peripheralLabels.get(label);
		if (entity == null) {
			throw new Exception("No entity found with label " + label);
		}
		if (!(entity instanceof IFluidHandler)) {
			throw new Exception("Invalid target for label " + label);
		}
		final IFluidHandler handler = (IFluidHandler) entity;
		// AnzacPeripheralsCore.logger.info("opposite:" + extractSide);
		final int canDrain = handler.drain(ForgeDirection.UNKNOWN, amount, false).amount;
		// AnzacPeripheralsCore.logger.info("canDrain:" + canDrain);
		if (canDrain > 0) {
			final FluidStack fluidStack = handler.drain(ForgeDirection.UNKNOWN, amount, true);
			// AnzacPeripheralsCore.logger.info("fluidStack:" + fluidStack);
			fill(ForgeDirection.UNKNOWN, fluidStack, true);
			// AnzacPeripheralsCore.logger.info("amount:" + fluidStack.amount);
			return fluidStack.amount;
		}
		return 0;
	}
}
