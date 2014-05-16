package anzac.peripherals.tiles;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.minecraft.entity.player.EntityPlayer;
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
import anzac.peripherals.annotations.Peripheral;
import anzac.peripherals.annotations.PeripheralMethod;
import anzac.peripherals.utils.Position;
import anzac.peripherals.utils.Utils;

/**
 * This block allows you to control the flow of fluid via a connected computer.
 * 
 * @author Tony
 */
@Peripheral(type = "FluidRouter", events = { PeripheralEvent.fluid_route })
public class FluidRouterTileEntity extends BaseRouterTileEntity implements IFluidHandler {

	public FluidTank fluidTank = new FluidTank(FluidContainerRegistry.BUCKET_VOLUME);

	@Override
	protected List<String> methodNames() {
		return getMethodNames(FluidRouterTileEntity.class);
	}

	/**
	 * Transfer {@code amount} amount of fluid from the internal tank to the tanks connected on {@code toDir} side.
	 * 
	 * @param toDir
	 *            the side the tanks are connected to.
	 * @param amount
	 *            the amount of fluid to transfer.
	 * @return the actual amount transferred.
	 * @throws Exception
	 */
	@PeripheralMethod
	public int routeTo(final ForgeDirection toDir, final int amount) throws Exception {
		return routeTo(toDir, toDir.getOpposite(), amount);
	}

	/**
	 * Transfer {@code amount} amount of fluid from the internal tank to the {@code side} side of the tanks connected on
	 * {@code toDir} side.
	 * 
	 * @param toDir
	 *            the side the tanks are connected to.
	 * @param insertDir
	 *            the side the tank to insert the fluid from.
	 * @param amount
	 *            the amount of fluid to transfer.
	 * @return the actual amount transferred.
	 * @throws Exception
	 */
	@PeripheralMethod
	public int routeTo(final ForgeDirection toDir, final ForgeDirection insetDir, final int amount) throws Exception {
		final FluidStack copy = fluidTank.getFluid().copy();
		copy.amount = amount;
		final int amount1 = copy.amount;
		copy.amount -= Utils.addToFluidHandler(worldObj, xCoord, yCoord, zCoord, toDir, insetDir, copy);
		final int toDrain = amount1 - copy.amount;
		if (toDrain > 0) {
			fluidTank.drain(toDrain, true);
		}
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

	public boolean isUseableByPlayer(final EntityPlayer entityplayer) {
		return isConnected() && worldObj.getBlockTileEntity(xCoord, yCoord, zCoord) == this
				&& entityplayer.getDistanceSq(xCoord + 0.5D, yCoord + 0.5D, zCoord + 0.5D) <= 64.0D;
	}

	@Override
	public int fill(final ForgeDirection from, final FluidStack resource, final boolean doFill) {
		if (resource == null) {
			return 0;
		}
		final int fill = fluidTank.fill(resource, doFill);
		if (fill > 0 && doFill) {
			PeripheralEvent.fluid_route.fire(computers, Utils.getUUID(resource), fill);
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

	public FluidTankInfo getInfo() {
		return fluidTank.getInfo();
	}

	@Override
	public FluidTankInfo[] getTankInfo(final ForgeDirection from) {
		return new FluidTankInfo[] { fluidTank.getInfo() };
	}

	/**
	 * Will return a table containing the uuid and count of the fluid in the internal tank.
	 * 
	 * @return A table of the internal contents.
	 * @throws Exception
	 */
	@PeripheralMethod
	public Map<?, ?> contents() throws Exception {
		return contents(ForgeDirection.UNKNOWN);
	}

	/**
	 * Will return a table containing the uuid and count of each fluid in the tanks connected to {@code direction} side
	 * of this block.
	 * 
	 * @param direction
	 *            which side of this block to examine the tanks of.
	 * @return A table of the contents of the connected tanks.
	 * @throws Exception
	 */
	@PeripheralMethod
	public Map<?, ?> contents(final ForgeDirection direction) throws Exception {
		return contents(direction, direction.getOpposite());
	}

	/**
	 * Will return a table containing the uuid and count of each fluid in the tanks connected to <code>direction</code>
	 * side of this block and limited the to those tanks accessible from <code>side</code> side.
	 * 
	 * @param direction
	 *            which side of this block to examine the tanks of.
	 * @param dir
	 *            which side of the tanks to examine.
	 * @return A table of the contents of the connected tanks.
	 * @throws Exception
	 */
	@PeripheralMethod
	public Map<Integer, Map<String, Integer>> contents(final ForgeDirection direction, final ForgeDirection dir)
			throws Exception {
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
		final Map<Integer, Map<String, Integer>> table = new HashMap<Integer, Map<String, Integer>>();
		for (final FluidTankInfo info : tankInfo) {
			if (info != null) {
				final FluidStack fluid = info.fluid;
				final int uuid = Utils.getUUID(fluid);
				final int amount = fluid == null ? 0 : fluid.amount;
				final int capacity = info.capacity;
				if (table.containsKey(uuid)) {
					final Map<String, Integer> map = table.get(uuid);
					map.put("amount", map.get("amount") + amount);
					map.put("capacity", map.get("capacity") + capacity);
				} else {
					final Map<String, Integer> map = new HashMap<String, Integer>();
					table.put(uuid, map);
					map.put("amount", amount);
					map.put("capacity", capacity);
				}
			}
		}
		AnzacPeripheralsCore.logger.info("table:" + table);
		return table;
	}

	/**
	 * Extract {@code amount} amount of fluid with {@code uuid} from the tanks connected to {@code fromDir} side.
	 * 
	 * @param fromDir
	 *            which side of this block to extract from.
	 * @param uuid
	 *            the uuid of the fluid to extract.
	 * @param amount
	 *            the amount of the fluid to extract.
	 * @return The actual amount extracted.
	 * @throws Exception
	 */
	@PeripheralMethod
	public int extractFrom(final ForgeDirection fromDir, final int uuid, final int amount) throws Exception {
		return extractFrom(fromDir, uuid, amount, fromDir.getOpposite());
	}

	/**
	 * Extract {@code amount} amount of fluid with {@code uuid} from the {@code side} side of the tanks connected to
	 * {@code fromDir} side.
	 * 
	 * @param fromDir
	 *            which side of this block to extract from.
	 * @param uuid
	 *            the uuid of the fluid to extract.
	 * @param amount
	 *            the amount of fluid to extract.
	 * @param extractSide
	 *            which side of the tanks to extract from.
	 * @return The actual amount extracted.
	 * @throws Exception
	 */
	@PeripheralMethod
	public int extractFrom(final ForgeDirection fromDir, final int uuid, final int amount,
			final ForgeDirection extractSide) throws Exception {
		final Position pos = new Position(xCoord, yCoord, zCoord, fromDir);
		pos.moveForwards(1);
		final TileEntity te = worldObj.getBlockTileEntity(pos.x, pos.y, pos.z);
		if (te == null || !(te instanceof IFluidHandler)) {
			throw new Exception("Fluid Handler not found");
		}
		final IFluidHandler handler = (IFluidHandler) te;
		final int canDrain = handler.drain(extractSide, amount, false).amount;
		if (canDrain > 0) {
			final FluidStack fluidStack = handler.drain(extractSide, amount, true);
			fill(fromDir, fluidStack, true);
			return fluidStack.amount;
		}
		return 0;
	}

	/**
	 * Transfer {@code amount} amount of fluid from the internal tank to another connected peripheral with {@code label}
	 * label. The peripheral must be connected to the same computer.
	 * 
	 * @param label
	 *            the label of the peripheral.
	 * @param amount
	 *            the amount of fluid to transfer.
	 * @return the actual amount transferred.
	 * @throws Exception
	 */
	@PeripheralMethod
	public int sendTo(final String label, final int amount) throws Exception {
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
		final int amount1 = copy.amount;
		copy.amount -= tank.fill(ForgeDirection.UNKNOWN, copy, true);
		final int toDrain = amount1 - copy.amount;
		if (toDrain > 0) {
			fluidTank.drain(toDrain, true);
		}
		return amount - copy.amount;
	}

	/**
	 * Transfer {@code amount} amount of fluid from another connected peripheral with {@code label} label to the
	 * internal tank. The peripheral must be connected to the same computer.
	 * 
	 * @param label
	 *            the label of the peripheral.
	 * @param uuid
	 *            the uuid of the fluid to transfer.
	 * @param amount
	 *            the amount of fluid to transfer.
	 * @return the actual amount transferred.
	 * @throws Exception
	 */
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
		final int canDrain = handler.drain(ForgeDirection.UNKNOWN, amount, false).amount;
		if (canDrain > 0) {
			final FluidStack fluidStack = handler.drain(ForgeDirection.UNKNOWN, amount, true);
			fill(ForgeDirection.UNKNOWN, fluidStack, true);
			return fluidStack.amount;
		}
		return 0;
	}
}
