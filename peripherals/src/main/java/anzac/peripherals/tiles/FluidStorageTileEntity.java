package anzac.peripherals.tiles;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.common.ForgeDirection;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fluids.FluidTankInfo;
import net.minecraftforge.fluids.IFluidHandler;
import anzac.peripherals.AnzacPeripheralsCore;
import anzac.peripherals.annotations.Peripheral;
import anzac.peripherals.annotations.PeripheralMethod;
import anzac.peripherals.utils.ClassUtils;
import anzac.peripherals.utils.Utils;
import dan200.computercraft.api.peripheral.IPeripheral;

@Peripheral(type = "FluidStorage")
public class FluidStorageTileEntity extends BaseStorageTileEntity implements IFluidHandler {

	private final class DiscListener implements InventoryListener {
		@Override
		public void inventoryChanged() {
			readFromDisk();
		}
	}

	private static final int maxTanks = 64;
	private static final int CAPACITY = AnzacPeripheralsCore.storageSize / maxTanks;
	private final Map<Integer, FluidTank> fluidTanks = new HashMap<Integer, FluidTank>(maxTanks);

	public FluidStorageTileEntity() {
		super();
		discInv.addListner(new DiscListener());
		clear();
	}

	private void clear() {
		for (int i = 0; i < maxTanks; i++) {
			final FluidTank fluidTank = new FluidTank(CAPACITY);
			fluidTanks.put(i, fluidTank);
		}
	}

	@Override
	protected List<String> methodNames() {
		return ClassUtils.getMethodNames(FluidStorageTileEntity.class);
	}

	@Override
	public void readFromNBT(final NBTTagCompound nbtRoot) {
		super.readFromNBT(nbtRoot);

		readFromDisk();
	}

	private void readFromDisk() {
		// read tanks
		clear();
		final ItemStack hddItem = discInv.getHDDItem();
		if (hddItem != null && hddItem.hasTagCompound()) {
			final NBTTagCompound tagCompound = hddItem.getTagCompound();
			final NBTTagList tankList = tagCompound.getTagList("tanks");
			for (int i = 0; i < tankList.tagCount(); i++) {
				final FluidTank fluidTank = new FluidTank(CAPACITY);
				final NBTTagCompound fluidTag = (NBTTagCompound) tankList.tagAt(i);
				fluidTank.readFromNBT(fluidTag);
				fluidTanks.put(i, fluidTank);
			}
		}
	}

	@Override
	public void writeToNBT(final NBTTagCompound nbtRoot) {
		super.writeToNBT(nbtRoot);

		writeToDisk();
	}

	private void writeToDisk() {
		// write tanks
		final ItemStack hddItem = discInv.getHDDItem();
		if (hddItem != null) {
			if (!hddItem.hasTagCompound()) {
				hddItem.setTagCompound(new NBTTagCompound());
			}
			final NBTTagCompound tagCompound = hddItem.getTagCompound();
			final NBTTagList list = new NBTTagList();
			for (int slot = 0; slot < maxTanks; slot++) {
				final FluidTank tank = fluidTanks.get(slot);
				if (tank != null && tank.getFluid() != null) {
					final NBTTagCompound tankTag = new NBTTagCompound();
					tank.writeToNBT(tankTag);
					list.appendTag(tankTag);
				}
			}
			tagCompound.setTag("tanks", list);
		}
	}

	@Override
	public int fill(final ForgeDirection from, final FluidStack resource, final boolean doFill) {
		if (getMount() == null || resource == null || !isAllowed(resource.getFluid())) {
			return 0;
		}
		final int fill = internalFill(resource, doFill);
		return fill;
	}

	private int internalFill(FluidStack resource, final boolean doFill) {
		resource = resource.copy();
		int totalUsed = 0;

		for (final FluidTank tank : fluidTanks.values()) {
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

	@Override
	public FluidStack drain(final ForgeDirection from, final int maxDrain, final boolean doDrain) {
		// cannot drain
		return null;
	}

	public boolean isUseableByPlayer(final EntityPlayer entityplayer) {
		return isConnected() && worldObj.getBlockTileEntity(xCoord, yCoord, zCoord) == this
				&& entityplayer.getDistanceSq(xCoord + 0.5D, yCoord + 0.5D, zCoord + 0.5D) <= 64.0D;
	}

	@Override
	public boolean canFill(final ForgeDirection from, final Fluid fluid) {
		return getMount() != null && isConnected() && isAllowed(fluid);
	}

	@Override
	public boolean canDrain(final ForgeDirection from, final Fluid fluid) {
		return false;
	}

	@Override
	public FluidTankInfo[] getTankInfo(final ForgeDirection from) {
		final List<FluidTankInfo> info = new ArrayList<FluidTankInfo>();
		for (final FluidTank tank : fluidTanks.values()) {
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

	/**
	 * @return
	 * @throws Exception
	 */
	@PeripheralMethod
	public Map<Integer, Map<String, Integer>> contents() throws Exception {
		final Map<Integer, Map<String, Integer>> table = new HashMap<Integer, Map<String, Integer>>();
		for (final FluidTank tank : fluidTanks.values()) {
			if (tank == null) {
				continue;
			}
			final FluidStack fluid = tank.getFluid();
			final int uuid = Utils.getUUID(fluid);
			final int amount = fluid == null ? 0 : fluid.amount;
			final int capacity = tank.getCapacity();
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
		AnzacPeripheralsCore.logger.info("table:" + table);
		return table;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((fluidTanks == null) ? 0 : fluidTanks.hashCode());
		return result;
	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		final FluidStorageTileEntity other = (FluidStorageTileEntity) obj;
		if (fluidTanks == null) {
			if (other.fluidTanks != null)
				return false;
		} else if (!fluidTanks.equals(other.fluidTanks))
			return false;
		return true;
	}

	@Override
	public boolean equals(final IPeripheral other) {
		return equals((Object) other);
	}
}
