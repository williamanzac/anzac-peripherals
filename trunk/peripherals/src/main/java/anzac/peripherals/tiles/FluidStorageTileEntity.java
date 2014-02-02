package anzac.peripherals.tiles;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.ForgeDirection;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fluids.FluidTankInfo;
import anzac.peripherals.AnzacPeripheralsCore;
import anzac.peripherals.annotations.PeripheralMethod;
import anzac.peripherals.utils.Utils;

import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;

import dan200.computer.api.IWritableMount;

public class FluidStorageTileEntity extends FluidRouterTileEntity {

	private static final List<String> METHOD_NAMES = getMethodNames(FluidStorageTileEntity.class);

	private static final int maxTanks = 16;
	private static final int CAPACITY = AnzacPeripheralsCore.storageSize / maxTanks;
	private final List<FluidTank> fluidTanks = new ArrayList<FluidTank>(maxTanks);
	private final Set<Integer> filter = new HashSet<Integer>();

	public FluidStorageTileEntity() {
		for (int i = 0; i < maxTanks; i++) {
			final FluidTank fluidTank = new FluidTank(CAPACITY);
			fluidTanks.add(fluidTank);
		}
	}

	@Override
	protected List<String> methodNames() {
		final List<String> methodNames = super.methodNames();
		methodNames.addAll(METHOD_NAMES);
		return methodNames;
	}

	@Override
	public String getType() {
		return "FluidStorage";
	}

	@Override
	public void readFromNBT(final NBTTagCompound nbtRoot) {
		super.readFromNBT(nbtRoot);

		if (mount != null) {
			// read tanks
			final List<String> tankIds = new ArrayList<String>();
			try {
				mount.list("tanks", tankIds);
				fluidTanks.clear();
				for (String id : tankIds) {
					final FluidTank tank = new FluidTank(CAPACITY);
					final InputStream in = mount.openForRead("tanks/" + id);
					final DataInputStream dis = new DataInputStream(in);
					try {
						final int capacity = dis.readInt();
						tank.setCapacity(capacity);
						final int uuid = dis.readInt();
						if (uuid != -1) {
							int amount = dis.readInt();
							tank.setFluid(new FluidStack(uuid, amount));
						}
						fluidTanks.add(tank);
					} finally {
						dis.close();
					}
				}
			} catch (IOException e) {
				// ignore this
			}

			// read filter
			try {
				final InputStream in = mount.openForRead("filter");
				final DataInputStream din = new DataInputStream(in);
				filter.clear();
				try {
					filter.add(din.readInt());
				} catch (final EOFException e1) {
					// ignore this will happen
				} finally {
					din.close();
				}
			} catch (final IOException e) {
				// ignore this
			}
		}
	}

	@Override
	public void writeToNBT(final NBTTagCompound nbtRoot) {
		super.writeToNBT(nbtRoot);

		if (mount != null) {
			// write tanks
			try {
				int count = 0;
				for (final FluidTank tank : fluidTanks) {
					final OutputStream out = ((IWritableMount) mount).openForWrite("tanks/" + count);
					final DataOutputStream dos = new DataOutputStream(out);
					try {
						final int capacity = tank.getCapacity();
						dos.writeInt(capacity);
						final FluidStack fluid = tank.getFluid();
						final int id = Utils.getUUID(fluid);
						dos.writeInt(id);
						if (id != -1) {
							final int amount = fluid.amount;
							dos.writeInt(amount);
						}
					} finally {
						dos.close();
					}
					count++;
				}
			} catch (final IOException e) {
				// ignore this
			}

			// write filter
			try {
				final OutputStream out = ((IWritableMount) mount).openForWrite("filter");
				final DataOutputStream dos = new DataOutputStream(out);
				try {
					for (final Integer id : filter) {
						dos.writeInt(id);
					}
				} finally {
					dos.close();
				}
			} catch (final IOException e) {
				// ignore this
			}
		}
	}

	@Override
	protected int internalFill(FluidStack resource, final boolean doFill) {
		if (resource == null) {
			return 0;
		}

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
	protected FluidStack internalDrain(final ForgeDirection from, final int maxDrain, final boolean doDrain) {
		return internalDrain(fluidTanks, maxDrain, doDrain);
	}

	@Override
	protected FluidStack internalDrain(final ForgeDirection from, final FluidStack resource, final boolean doDrain) {
		if (resource == null) {
			return null;
		}
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
	protected boolean isAllowed(final Fluid fluid) {
		if (mount == null) {
			return false;
		}
		final int id = Utils.getUUID(fluid);
		return filter.contains(id);
	}

	@PeripheralMethod
	private Set<Integer> listFilter() throws Exception {
		if (mount == null) {
			throw new Exception("No disk loaded");
		}
		return filter;
	}

	@PeripheralMethod
	private void removeFilter(final int id) throws Exception {
		if (mount == null) {
			throw new Exception("No disk loaded");
		}
		filter.remove(id);
	}

	@PeripheralMethod
	private void addFilter(final int id) throws Exception {
		if (mount == null) {
			throw new Exception("No disk loaded");
		}
		filter.add(id);
	}

	@Override
	protected boolean requiresMount() {
		return true;
	}
}
