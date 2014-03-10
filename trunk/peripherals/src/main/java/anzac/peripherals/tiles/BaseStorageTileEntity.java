package anzac.peripherals.tiles;

import java.util.HashSet;
import java.util.Set;

import net.minecraft.nbt.NBTTagCompound;
import anzac.peripherals.annotations.PeripheralMethod;

public abstract class BaseStorageTileEntity extends BasePeripheralTileEntity {

	protected static enum FilterMode {
		WHITELIST, BLACKLIST, NONE;
	}

	protected final Set<Integer> filter = new HashSet<Integer>();
	protected FilterMode filterMode = FilterMode.NONE;

	@Override
	protected boolean requiresMount() {
		return false;
	}

	@PeripheralMethod
	public abstract Object contents() throws Exception;

	protected boolean isAllowed(final int id) {
		switch (filterMode) {
		case NONE:
			return true;
		case BLACKLIST:
			return !filter.contains(id);
		case WHITELIST:
			return filter.contains(id);
		}
		return false;
	}

	@PeripheralMethod
	public Integer[] listFilter() throws Exception {
		return filter.toArray(new Integer[filter.size()]);
	}

	@PeripheralMethod
	public void removeFilter(final int id) throws Exception {
		filter.remove(id);
	}

	@PeripheralMethod
	public void addFilter(final int id) throws Exception {
		filter.add(id);
	}

	@PeripheralMethod
	public FilterMode getFilterMode() {
		return filterMode;
	}

	@PeripheralMethod
	public void setFilterMode(final FilterMode mode) {
		filterMode = mode;
	}

	@Override
	public void readFromNBT(final NBTTagCompound nbtTagCompound) {
		super.readFromNBT(nbtTagCompound);

		final int[] intArray = nbtTagCompound.getIntArray("filter");
		filter.clear();
		for (final int i : intArray) {
			filter.add(i);
		}
		if (nbtTagCompound.hasKey("filter_mode")) {
			filterMode = FilterMode.valueOf(nbtTagCompound.getString("filter_mode"));
		}
	}

	@Override
	public void writeToNBT(final NBTTagCompound nbtTagCompound) {
		super.writeToNBT(nbtTagCompound);

		final int[] intArray = new int[filter.size()];
		int count = 0;
		for (final Integer i : filter) {
			intArray[count++] = i;
		}
		nbtTagCompound.setIntArray("filter", intArray);
		if (filterMode != null) {
			nbtTagCompound.setString("filter_mode", filterMode.name());
		}
	}
}
