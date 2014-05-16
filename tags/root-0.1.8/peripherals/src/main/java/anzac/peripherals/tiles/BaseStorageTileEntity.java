package anzac.peripherals.tiles;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashSet;
import java.util.Set;

import net.minecraft.nbt.NBTTagCompound;
import anzac.peripherals.annotations.PeripheralMethod;
import dan200.computer.api.IWritableMount;

public abstract class BaseStorageTileEntity extends BasePeripheralTileEntity {

	protected static enum FilterMode {
		WHITELIST, BLACKLIST, NONE;
	}

	protected Set<Integer> filter = new HashSet<Integer>();
	protected FilterMode filterMode = FilterMode.NONE;

	protected boolean isAllowed(final int id) {
		if (getMount() == null) {
			return false;
		}
		switch (getFilterMode()) {
		case NONE:
			return true;
		case BLACKLIST:
			return !filterContains(id);
		case WHITELIST:
			return filterContains(id);
		}
		return false;
	}

	private boolean filterContains(final int uuid) {
		final int id = getId(uuid);
		for (final int fId : getFilter()) {
			final int cId = getId(fId);
			if (cId == id) {
				return true;
			}
		}
		return false;
	}

	protected int getId(final int uuid) {
		return uuid;
	}

	private Set<Integer> getFilter() {
		if (getMount() != null && worldObj != null && !worldObj.isRemote) {
			// read from disk
			InputStream inputStream = null;
			DataInputStream in = null;
			try {
				if (!getMount().exists("filter")) {
					return filter;
				}
				inputStream = getMount().openForRead("filter");
				in = new DataInputStream(inputStream);
				final Set<Integer> f = new HashSet<Integer>();
				final int count = in.readInt();
				for (int i = 0; i < count; i++) {
					f.add(in.readInt());
				}
				return f;
			} catch (final IOException e) {
				e.printStackTrace();
			} finally {
				try {
					if (in != null) {
						in.close();
					}
					if (inputStream != null) {
						inputStream.close();
					}
				} catch (final IOException e) {
					e.printStackTrace();
				}
			}
		}
		return filter;
	}

	private void setFilter(final Set<Integer> f) {
		filter = f;
		if (getMount() != null && (getMount() instanceof IWritableMount) && worldObj != null && !worldObj.isRemote) {
			OutputStream outputStream = null;
			DataOutputStream out = null;
			try {
				outputStream = ((IWritableMount) getMount()).openForWrite("filter");
				out = new DataOutputStream(outputStream);
				out.writeInt(f.size());
				for (final Integer integer : f) {
					out.writeInt(integer);
				}
			} catch (final IOException e) {
				e.printStackTrace();
			} finally {
				try {
					if (out != null) {
						out.close();
					}
					if (outputStream != null) {
						outputStream.close();
					}
				} catch (final IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	/**
	 * @return
	 * @throws Exception
	 */
	@PeripheralMethod
	public Integer[] listFilter() throws Exception {
		final Set<Integer> f = getFilter();
		return f.toArray(new Integer[f.size()]);
	}

	/**
	 * @param id
	 * @throws Exception
	 */
	@PeripheralMethod
	public void removeFilter(final int id) throws Exception {
		final Set<Integer> f = getFilter();
		f.remove(id);
		setFilter(f);
	}

	/**
	 * @param id
	 * @throws Exception
	 */
	@PeripheralMethod
	public void addFilter(final int id) throws Exception {
		final Set<Integer> f = getFilter();
		f.add(id);
		setFilter(f);
	}

	/**
	 * @return
	 */
	@PeripheralMethod
	public FilterMode getFilterMode() {
		if (getMount() != null && worldObj != null && !worldObj.isRemote) {
			// read from disk
			InputStream inputStream = null;
			DataInputStream in = null;
			try {
				if (!getMount().exists("filterMode")) {
					return FilterMode.NONE;
				}
				inputStream = getMount().openForRead("filterMode");
				in = new DataInputStream(inputStream);
				final String name = in.readUTF();
				return FilterMode.valueOf(name);
			} catch (final IOException e) {
				e.printStackTrace();
			} finally {
				try {
					if (in != null) {
						in.close();
					}
					if (inputStream != null) {
						inputStream.close();
					}
				} catch (final IOException e) {
					e.printStackTrace();
				}
			}
		}
		return filterMode;
	}

	/**
	 * @param mode
	 */
	@PeripheralMethod
	public void setFilterMode(final FilterMode mode) {
		filterMode = mode;
		if (getMount() != null && (getMount() instanceof IWritableMount) && worldObj != null && !worldObj.isRemote) {
			OutputStream outputStream = null;
			DataOutputStream out = null;
			try {
				outputStream = ((IWritableMount) getMount()).openForWrite("filterMode");
				out = new DataOutputStream(outputStream);
				out.writeUTF(mode.name());
			} catch (final IOException e) {
				e.printStackTrace();
			} finally {
				try {
					if (out != null) {
						out.close();
					}
					if (outputStream != null) {
						outputStream.close();
					}
				} catch (final IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	@Override
	public void readFromNBT(final NBTTagCompound nbtTagCompound) {
		super.readFromNBT(nbtTagCompound);

		if (nbtTagCompound.hasKey("filter")) {
			final int[] intArray = nbtTagCompound.getIntArray("filter");
			final Set<Integer> f = new HashSet<Integer>();
			for (final int i : intArray) {
				f.add(i);
			}
			setFilter(f);
		}
		if (nbtTagCompound.hasKey("filter_mode")) {
			setFilterMode(FilterMode.valueOf(nbtTagCompound.getString("filter_mode")));
		}
	}

	@Override
	public void writeToNBT(final NBTTagCompound nbtTagCompound) {
		super.writeToNBT(nbtTagCompound);

		final Set<Integer> f = getFilter();
		final int[] intArray = new int[f.size()];
		int count = 0;
		for (final Integer i : f) {
			intArray[count++] = i;
		}
		nbtTagCompound.setIntArray("filter", intArray);
		final FilterMode fm = getFilterMode();
		if (fm != null) {
			nbtTagCompound.setString("filter_mode", fm.name());
		}
	}
}
