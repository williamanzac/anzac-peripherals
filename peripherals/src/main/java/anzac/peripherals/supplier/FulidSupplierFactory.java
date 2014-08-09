package anzac.peripherals.supplier;

import java.util.UUID;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeDirection;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidContainerRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fluids.FluidTankInfo;
import net.minecraftforge.fluids.IFluidHandler;
import anzac.peripherals.AnzacPeripheralsCore;
import buildcraft.api.power.IPowerReceptor;
import buildcraft.api.power.PowerHandler;
import buildcraft.api.power.PowerHandler.PowerReceiver;
import buildcraft.api.power.PowerHandler.Type;
import cofh.api.energy.IEnergyHandler;

public class FulidSupplierFactory implements SupplierStorageFactory {
	private static final int MJ = AnzacPeripheralsCore.mjMultiplier;
	private static final int RF_TO_MJ = 10;

	public static class FluidSupplierStorage implements SupplierStorage, IFluidHandler, IPowerReceptor, IEnergyHandler {
		public FluidTank fluidTank = new FluidTank(FluidContainerRegistry.BUCKET_VOLUME);

		private final PowerHandler handler = new PowerHandler(this, Type.MACHINE);
		private UUID owner;

		public FluidSupplierStorage(final UUID owner) {
			this.owner = owner;
			configure();
		}

		@Override
		public void readFromNBT(final NBTTagCompound tagCompound) {
			// read tank
			if (tagCompound.hasKey("fluid")) {
				final NBTTagCompound tagFluid = (NBTTagCompound) tagCompound.getTag("fluid");
				fluidTank.readFromNBT(tagFluid);
			}

			handler.readFromNBT(tagCompound);
			if (tagCompound.hasKey("owner")) {
				owner = UUID.fromString(tagCompound.getString("owner"));
			}
			configure();
		}

		@Override
		public void writeToNBT(final NBTTagCompound tagCompound) {
			// write tank
			if (fluidTank != null) {
				final NBTTagCompound tagFluid = new NBTTagCompound();
				fluidTank.writeToNBT(tagFluid);
				tagCompound.setTag("fluid", tagFluid);
			}

			handler.writeToNBT(tagCompound);
			tagCompound.setString("owner", owner.toString());
		}

		@Override
		public int fill(final ForgeDirection from, final FluidStack resource, final boolean doFill) {
			if (resource == null) {
				return 0;
			}
			final int fill = fluidTank.fill(resource, doFill);
			// if (fill > 0 && doFill) {
			// entity.queueEvent(PeripheralEvent.fluid_route, Utils.getUUID(resource), fill);
			// }
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

		private void configure() {
			final double maxStorage = 5000;
			final double maxIn = 500;
			handler.configure(1f, (float) maxIn, MJ, (float) maxStorage);
			handler.configurePowerPerdition(0, 0);
		}

		@Override
		public PowerReceiver getPowerReceiver(final ForgeDirection side) {
			return handler.getPowerReceiver();
		}

		@Override
		public void doWork(final PowerHandler workProvider) {
		}

		@Override
		public World getWorld() {
			return null;
		}

		@Override
		public boolean canInterface(final ForgeDirection arg0) {
			return true;
		}

		@Override
		public int extractEnergy(final ForgeDirection arg0, final int arg1, final boolean arg2) {
			return 0;
		}

		@Override
		public int getEnergyStored(final ForgeDirection arg0) {
			return (int) (handler.getEnergyStored() * RF_TO_MJ);
		}

		@Override
		public int getMaxEnergyStored(final ForgeDirection arg0) {
			return (int) (handler.getMaxEnergyStored() * RF_TO_MJ);
		}

		@Override
		public int receiveEnergy(final ForgeDirection arg0, final int arg1, final boolean arg2) {
			final int quantity = arg1 / RF_TO_MJ;
			if (arg2) {
				if (handler.getEnergyStored() + quantity <= handler.getMaxEnergyStored()) {
					return quantity;
				} else {
					return (int) ((handler.getMaxEnergyStored() - handler.getEnergyStored()) * RF_TO_MJ);
				}
			}
			return (int) (handler.addEnergy(quantity) * RF_TO_MJ);
		}

		@Override
		public UUID getOwner() {
			return owner;
		}

		public PowerHandler getHandler() {
			return handler;
		}
	}

	@Override
	public SupplierStorageType getType() {
		return SupplierStorageType.FLUID;
	}

	@Override
	public SupplierStorage create(SupplierManager manager, UUID id1, UUID id2) {
		return new FluidSupplierStorage(id1);
	}

}
