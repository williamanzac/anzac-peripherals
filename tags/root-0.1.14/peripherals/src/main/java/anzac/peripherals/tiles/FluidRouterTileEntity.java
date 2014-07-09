package anzac.peripherals.tiles;

import java.util.HashMap;
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
import anzac.peripherals.peripheral.FluidRouterPeripheral;
import anzac.peripherals.utils.Position;
import anzac.peripherals.utils.Utils;

public class FluidRouterTileEntity extends BaseRouterTileEntity implements IFluidHandler {

	public static class TankInfo {
		public int fluidId;
		public int capacity;
		public int amount;
	}

	public FluidRouterTileEntity() throws Exception {
		super(FluidRouterPeripheral.class);
	}

	public FluidTank fluidTank = new FluidTank(FluidContainerRegistry.BUCKET_VOLUME);

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
			queueEvent(PeripheralEvent.fluid_route, Utils.getUUID(resource), fill);
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

	public TankInfo[] contents(final ForgeDirection direction, final ForgeDirection dir) throws Exception {
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
		final Map<Integer, TankInfo> table = new HashMap<Integer, TankInfo>();
		for (final FluidTankInfo info : tankInfo) {
			if (info != null) {
				final FluidStack fluid = info.fluid;
				final int uuid = Utils.getUUID(fluid);
				final int amount = fluid == null ? 0 : fluid.amount;
				final int capacity = info.capacity;
				if (table.containsKey(uuid)) {
					final TankInfo map = table.get(uuid);
					map.amount += amount;
					map.capacity += capacity;
				} else {
					final TankInfo map = new TankInfo();
					table.put(uuid, map);
					map.fluidId = uuid;
					map.amount = amount;
					map.capacity = capacity;
				}
			}
		}
		AnzacPeripheralsCore.logger.info("table:" + table);
		return table.values().toArray(new TankInfo[table.size()]);
	}

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

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((fluidTank == null) ? 0 : fluidTank.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		FluidRouterTileEntity other = (FluidRouterTileEntity) obj;
		if (fluidTank == null) {
			if (other.fluidTank != null)
				return false;
		} else if (!fluidTank.equals(other.fluidTank))
			return false;
		return true;
	}
}
