package anzac.peripherals.tiles;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.common.ForgeDirection;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fluids.FluidTankInfo;
import net.minecraftforge.fluids.IFluidHandler;
import anzac.peripherals.AnzacPeripheralsCore;
import anzac.peripherals.annotations.PeripheralMethod;
import anzac.peripherals.utils.Utils;

public class FluidStorageTileEntity extends BaseStorageTileEntity implements IFluidHandler {

	private static final int maxTanks = 64;
	private static final int CAPACITY = AnzacPeripheralsCore.storageSize / maxTanks;
	private final FluidTank[] fluidTanks = new FluidTank[maxTanks];

	public FluidStorageTileEntity() {
		for (int i = 0; i < maxTanks; i++) {
			final FluidTank fluidTank = new FluidTank(CAPACITY);
			fluidTanks[i] = fluidTank;
		}
	}

	@Override
	public String getType() {
		return "FluidStorage";
	}

	@Override
	protected List<String> methodNames() {
		return getMethodNames(FluidStorageTileEntity.class);
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
				fluidTanks[i] = fluidTank;
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

	@Override
	public int fill(final ForgeDirection from, final FluidStack resource, final boolean doFill) {
		if (resource == null || !isAllowed(resource.getFluid())) {
			return 0;
		}
		final int fill = internalFill(resource, doFill);
		if (fill > 0 && doFill) {
			// TODO add event
			// for (final IComputerAccess computer : computers.keySet()) {
			// computer.queueEvent("fluid_route", new Object[] { computer.getAttachmentName(),
			// Utils.getUUID(resource), resource.amount });
			// }
		}
		return fill;
	}

	private int internalFill(FluidStack resource, final boolean doFill) {
		resource = resource.copy();
		int totalUsed = 0;

		for (final FluidTank tank : fluidTanks) {
			final int used = tank.fill(resource, doFill);
			resource.amount -= used;

			totalUsed += used;
			if (resource.amount <= 0) {
				break;
			}
		}
		return totalUsed;
	}

	@Override
	public FluidStack drain(final ForgeDirection from, final FluidStack resource, final boolean doDrain) {
		// cannot drain
		return null;
	}

	// private FluidStack internalDrain(final ForgeDirection from, final int maxDrain, final boolean doDrain) {
	// return internalDrain(fluidTanks, maxDrain, doDrain);
	// }

	@Override
	public FluidStack drain(final ForgeDirection from, final int maxDrain, final boolean doDrain) {
		// cannot drain
		return null;
	}

	// private FluidStack internalDrain(final ForgeDirection from, final FluidStack resource, final boolean doDrain) {
	// if (resource == null) {
	// return null;
	// }
	// final Collection<FluidTank> filter = Collections2.filter(fluidTanks, new Predicate<FluidTank>() {
	// @Override
	// public boolean apply(final FluidTank input) {
	// if (input != null) {
	// return resource.isFluidEqual(input.getFluid());
	// }
	// return false;
	// }
	// });
	// return internalDrain(filter, resource.amount, doDrain);
	// }

	// private FluidStack internalDrain(final Collection<FluidTank> tanksToDrain, final int maxEmpty, final boolean
	// doDrain) {
	// int totalDrained = 0;
	// int maxDrain = maxEmpty;
	// FluidStack stack = null;
	//
	// for (final FluidTank tank : tanksToDrain) {
	// stack = tank.drain(maxDrain, doDrain);
	// maxDrain -= stack.amount;
	//
	// totalDrained += stack.amount;
	// if (maxDrain <= 0) {
	// break;
	// }
	// }
	// if (stack != null) {
	// stack.amount = totalDrained;
	// }
	// return stack;
	// }

	@Override
	public boolean canFill(final ForgeDirection from, final Fluid fluid) {
		return isConnected() && isAllowed(fluid);
	}

	@Override
	public boolean canDrain(final ForgeDirection from, final Fluid fluid) {
		return false;
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

	private boolean isAllowed(final Fluid fluid) {
		final int id = Utils.getUUID(fluid);
		return isAllowed(id);
	}

	@Override
	@PeripheralMethod
	public Object contents() throws Exception {
		// AnzacPeripheralsCore.logger.info("fluid storage contents");
		final Map<Integer, Map<Integer, Integer>> table = new HashMap<Integer, Map<Integer, Integer>>();
		for (final FluidTank tank : fluidTanks) {
			// AnzacPeripheralsCore.logger.info("tank: " + tank);
			if (tank == null) {
				continue;
			}
			final FluidStack fluid = tank.getFluid();
			// AnzacPeripheralsCore.logger.info("fluid: " + fluid);
			final int uuid = Utils.getUUID(fluid);
			// AnzacPeripheralsCore.logger.info("uuid: " + uuid);
			final int amount = fluid == null ? 0 : fluid.amount;
			// AnzacPeripheralsCore.logger.info("amount: " + amount);
			final int capacity = tank.getCapacity();
			// AnzacPeripheralsCore.logger.info("capacity: " + capacity);
			if (table.containsKey(uuid)) {
				final Map<Integer, Integer> map = table.get(uuid);
				map.put(0, map.get(0) + amount);
				map.put(1, map.get(1) + capacity);
			} else {
				final Map<Integer, Integer> list = new HashMap<Integer, Integer>();
				table.put(uuid, list);
				list.put(0, amount);
				list.put(1, capacity);
			}
		}
		AnzacPeripheralsCore.logger.info("table:" + table);
		return table;
	}
}
