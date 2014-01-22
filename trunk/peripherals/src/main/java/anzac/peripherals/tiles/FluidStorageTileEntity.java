package anzac.peripherals.tiles;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.common.ForgeDirection;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fluids.FluidTankInfo;
import anzac.peripherals.AnzacPeripheralsCore;

import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;

public class FluidStorageTileEntity extends FluidRouterTileEntity {

	private static final int maxTanks = 16;
	private static final int CAPACITY = AnzacPeripheralsCore.storageSize / maxTanks;
	public List<FluidTank> fluidTanks = new ArrayList<FluidTank>(maxTanks);

	public FluidStorageTileEntity() {
		for (int i = 0; i < maxTanks; i++) {
			final FluidTank fluidTank = new FluidTank(CAPACITY);
			fluidTanks.add(fluidTank);
		}
	}

	@Override
	public String getType() {
		return "FluidStorage";
	}

	@Override
	public void readFromNBT(final NBTTagCompound nbtRoot) {
		super.readFromNBT(nbtRoot);

		// read tanks
		if (nbtRoot.hasKey("tanks")) {
			final NBTTagList tankList = nbtRoot.getTagList("tanks");
			for (int i = 0; i < tankList.tagCount(); i++) {
				final FluidTank fluidTank = new FluidTank(CAPACITY);
				final NBTTagCompound fluidTag = (NBTTagCompound) tankList.tagAt(i);
				fluidTank.readFromNBT(fluidTag);
				fluidTanks.add(fluidTank);
			}
		}
	}

	@Override
	public void writeToNBT(final NBTTagCompound nbtRoot) {
		super.writeToNBT(nbtRoot);

		// write tanks
		final NBTTagList tankList = new NBTTagList();
		for (final FluidTank tank : fluidTanks) {
			if (tank != null && tank.getFluid() != null) {
				final NBTTagCompound tankTag = new NBTTagCompound();
				tank.writeToNBT(tankTag);
				tankList.appendTag(tankTag);
			}
		}
		nbtRoot.setTag("tanks", tankList);
	}

	private int internalFill(FluidStack resource) {
		if (resource == null) {
			return 0;
		}

		resource = resource.copy();
		int totalUsed = 0;

		for (final FluidTank tank : fluidTanks) {
			final int used = tank.fill(resource, true);
			resource.amount -= used;

			totalUsed += used;
			if (resource.amount <= 0) {
				break;
			}
		}
		return totalUsed;
	}

	private FluidStack internalDrain(final Collection<FluidTank> tanksToDrain, final int maxEmpty, final boolean doDrain) {
		int totalDrained = 0;
		int maxDrain = maxEmpty;
		FluidStack stack = null;

		for (final FluidTank tank : tanksToDrain) {
			stack = tank.drain(maxDrain, doDrain);
			maxDrain -= stack.amount;

			totalDrained += stack.amount;
			if (maxDrain <= 0) {
				break;
			}
		}
		if (stack != null) {
			stack.amount = totalDrained;
		}
		return stack;
	}

	private FluidStack internalDrain(final int maxEmpty, final boolean doDrain) {
		return internalDrain(fluidTanks, maxEmpty, doDrain);
	}

	private FluidStack internalDrain(final FluidStack resource, final boolean doDrain) {
		if (resource == null)
			return null;
		final Collection<FluidTank> filter = Collections2.filter(fluidTanks, new Predicate<FluidTank>() {
			@Override
			public boolean apply(final FluidTank input) {
				if (input != null) {
					return resource.isFluidEqual(input.getFluid());
				}
				return false;
			}
		});
		return internalDrain(filter, resource.amount, doDrain);
	}

	@Override
	public FluidTankInfo[] getTankInfo(final ForgeDirection from) {
		final List<FluidTankInfo> info = new ArrayList<FluidTankInfo>();
		for (final FluidTank tank : fluidTanks) {
			if (tank != null) {
				info.add(tank.getInfo());
			}
		}

		return info.toArray(new FluidTankInfo[info.size()]);
	}

	@Override
	protected void defaultRoute(final FluidStack copy) {
		copy.amount -= internalFill(copy);
		if (copy.amount > 0) {
			super.defaultRoute(copy);
		}
	}
}
