package anzac.peripherals.tiles;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import anzac.peripherals.AnzacPeripheralsCore;
import anzac.peripherals.annotations.PeripheralMethod;
import anzac.peripherals.utils.Utils;
import dan200.computer.api.IWritableMount;

public class ItemStorageTileEntity extends ItemRouterTileEntity {

	private final int[] SLOT_ARRAY = Utils.createSlotArray(0, getSizeInventory());

	private static final List<String> METHOD_NAMES = getMethodNames(ItemStorageTileEntity.class);

	private final Map<Integer, ItemStack> inventory = new HashMap<Integer, ItemStack>();
	private static final int maxCount = AnzacPeripheralsCore.storageSize / 64;
	private final Set<Integer> filter = new HashSet<Integer>();

	// private List<Position> multiblock = null;
	// private boolean multiblockDirty = false;

	public ItemStorageTileEntity() {
	}

	@Override
	protected List<String> methodNames() {
		final List<String> methodNames = super.methodNames();
		methodNames.addAll(METHOD_NAMES);
		return methodNames;
	}

	@Override
	public String getType() {
		return "ItemStorage";
	}

	@Override
	public void readFromNBT(final NBTTagCompound nbtRoot) {
		super.readFromNBT(nbtRoot);

		// final boolean wasMulti = isMultiblock();
		// if (nbtRoot.getBoolean("isMultiblock")) {
		// final int[] coords = nbtRoot.getIntArray("multiblock");
		// multiblock = new ArrayList<Position>(coords.length / 3);
		// for (int c = 0; c < coords.length; c += 3) {
		// multiblock.add(new Position(coords[c], coords[c + 1], coords[c + 2]));
		// }
		// } else {
		// multiblock = null;
		// }

		if (mount != null) {
			// read tanks
			final List<String> slots = new ArrayList<String>();
			try {
				mount.list("inventory", slots);
				inventory.clear();
				for (String slot : slots) {
					final InputStream in = mount.openForRead("inventory/" + slot);
					final DataInputStream dis = new DataInputStream(in);
					try {
						final int uuid = dis.readInt();
						if (uuid != -1) {
							int amount = dis.readInt();
							inventory.put(Integer.parseInt(slot), Utils.getUUID(uuid, amount));
						}
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

		// nbtRoot.setBoolean("isMultiblock", isMultiblock());
		// if (isMultiblock()) {
		// final int[] vals = new int[multiblock.size() * 3];
		// int i = 0;
		// for (final Position bc : multiblock) {
		// vals[i++] = (int) bc.x;
		// vals[i++] = (int) bc.y;
		// vals[i++] = (int) bc.z;
		// }
		// nbtRoot.setIntArray("multiblock", vals);
		// }

		if (mount != null) {
			// write tanks
			try {
				((IWritableMount) mount).delete("inventory");
				for (final Entry<Integer, ItemStack> entry : inventory.entrySet()) {
					final OutputStream out = ((IWritableMount) mount).openForWrite("inventory/" + entry.getKey());
					final DataOutputStream dos = new DataOutputStream(out);
					try {
						final ItemStack stack = entry.getValue();
						final int id = Utils.getUUID(stack);
						dos.writeInt(id);
						if (id != -1) {
							final int amount = stack.stackSize;
							dos.writeInt(amount);
						}
					} finally {
						dos.close();
					}
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
	public void updateEntity() {
		super.updateEntity();

		// if (multiblockDirty) {
		// multiblockDirty = false;
		// formMultiblock();
		// }
		//
		// if (!isContoller()) {
		// return;
		// }

		// if (worldObj.getTotalWorldTime() % 10 == 0) {
		// worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
		// // onInventoryChanged();
		// }
	}

	@Override
	public int getSizeInventory() {
		return maxCount;
	}

	@Override
	public ItemStack getStackInSlot(final int i) {
		return inventory.get(i);
	}

	@Override
	public void setInventorySlotContents(final int slot, final ItemStack stack) {
		inventory.put(slot, stack);
		if (stack == null) {
			return;
		}
		if (stack.stackSize > getInventoryStackLimit()) {
			stack.stackSize = getInventoryStackLimit();
		}
		onInventoryChanged();
	}

	@Override
	public ItemStack decrStackSize(final int slot, final int amt) {
		ItemStack stack = getStackInSlot(slot);
		if (stack != null) {
			if (stack.stackSize <= amt) {
				setInventorySlotContents(slot, null);
			} else {
				stack = stack.splitStack(amt);
				if (stack.stackSize == 0) {
					setInventorySlotContents(slot, null);
				}
			}
		}
		return stack;
	}

	@Override
	public ItemStack getStackInSlotOnClosing(final int slot) {
		final ItemStack stack = getStackInSlot(slot);
		if (stack != null) {
			setInventorySlotContents(slot, null);
		}
		return stack;
	}

	@Override
	public String getInvName() {
		return getLabel();
	}

	@Override
	public boolean isInvNameLocalized() {
		return hasLabel();
	}

	@Override
	public int[] getAccessibleSlotsFromSide(final int var1) {
		return isConnected() ? SLOT_ARRAY : new int[0];
	}

	// public void onBlockAdded() {
	// multiblockDirty = true;
	// }
	//
	// public void onBreakBlock() {
	// clearCurrentMultiblock();
	// }
	//
	// private void clearCurrentMultiblock() {
	// if (multiblock == null) {
	// return;
	// }
	// for (final Position bc : multiblock) {
	// final ItemStorageTileEntity storage = getStorage(bc);
	// if (storage != null) {
	// storage.setMultiblock(null);
	// }
	// }
	// multiblock = null;
	// }
	//
	// private void formMultiblock() {
	// final List<ItemStorageTileEntity> blocks = new ArrayList<ItemStorageTileEntity>();
	// blocks.add(this);
	// findNighbouringBanks(this, blocks);
	//
	// if (blocks.size() < 2) {
	// return;
	// }
	// for (final ItemStorageTileEntity cb : blocks) {
	// cb.clearCurrentMultiblock();
	// }
	// final List<Position> mb = new ArrayList<Position>(blocks.size());
	// for (int i = 0; i < blocks.size(); i++) {
	// mb.add(new Position(blocks.get(i)));
	// }
	//
	// for (final ItemStorageTileEntity cb : blocks) {
	// cb.setMultiblock(mb);
	// }
	// }
	//
	// private void findNighbouringBanks(final ItemStorageTileEntity tileCapacitorBank,
	// final List<ItemStorageTileEntity> blocks) {
	// for (final ForgeDirection dir : ForgeDirection.VALID_DIRECTIONS) {
	// final Position bc = new Position(tileCapacitorBank);
	// bc.orientation = dir;
	// bc.moveForwards(1);
	// final ItemStorageTileEntity cb = getStorage(bc);
	// if (cb != null && !blocks.contains(cb)) {
	// blocks.add(cb);
	// findNighbouringBanks(cb, blocks);
	// }
	// }
	// }
	//
	// private void setMultiblock(final List<Position> mb) {
	// multiblock = mb;
	// if (multiblock != null && isMaster()) {
	// final int newSize = multiblock.size() * chestSize;
	// controllerInventory = new ItemStack[newSize];
	// final int row = 0;
	// for (final Position bc : multiblock) {
	// final ItemStorageTileEntity storage = getStorage(bc);
	// if (storage != null) {
	// storage.multiblockDirty = true;
	// for (int col = 0; col < storage.inventory.length; col++) {
	// controllerInventory[col + row * chestSize] = storage.inventory[col];
	// }
	// }
	// }
	// }
	//
	// // Forces an update
	// worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
	// // worldObj.setBlockMetadataWithNotify(xCoord, yCoord, zCoord, isMultiblock() ? 1 : 0, 2);
	// }
	//
	// public ItemStorageTileEntity getController() {
	// if (isMaster() || !isMultiblock()) {
	// return this;
	// }
	// final Position position = multiblock.get(0);
	// final ItemStorageTileEntity res = getStorage(position);
	// return res != null ? res : this;
	// }
	//
	// boolean isContoller() {
	// return multiblock == null ? true : isMaster();
	// }
	//
	// boolean isMaster() {
	// if (multiblock != null) {
	// return multiblock.get(0).equals(new Position(this));
	// }
	// return false;
	// }
	//
	// public boolean isMultiblock() {
	// return multiblock != null;
	// }
	//
	// private boolean isCurrentMultiblockValid() {
	// if (multiblock == null) {
	// return false;
	// }
	// for (final Position bc : multiblock) {
	// final ItemStorageTileEntity res = getStorage(bc);
	// if (res == null || !res.isMultiblock()) {
	// return false;
	// }
	// }
	// return true;
	// }
	//
	// public ItemStorageTileEntity getStorage(final Position bc) {
	// return getStorage((int) bc.x, (int) bc.y, (int) bc.z);
	// }
	//
	// private ItemStorageTileEntity getStorage(final int x, final int y, final int z) {
	// final TileEntity te = worldObj.getBlockTileEntity(x, y, z);
	// if (te instanceof ItemStorageTileEntity) {
	// return (ItemStorageTileEntity) te;
	// }
	// return null;
	// }
	//
	// public List<Position> getMultiblock() {
	// return multiblock;
	// }

	@Override
	protected boolean isAllowed(final ItemStack stack) {
		if (mount == null) {
			return false;
		}
		final int id = Utils.getUUID(stack);
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
