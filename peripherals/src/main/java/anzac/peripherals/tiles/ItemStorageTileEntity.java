package anzac.peripherals.tiles;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.common.ForgeDirection;
import net.minecraftforge.oredict.OreDictionary;
import anzac.peripherals.AnzacPeripheralsCore;
import anzac.peripherals.annotations.PeripheralMethod;
import anzac.peripherals.utils.Utils;
import buildcraft.api.inventory.ISpecialInventory;

public class ItemStorageTileEntity extends BaseStorageTileEntity implements IInventory, ISidedInventory,
		ISpecialInventory {

	private static final String INVENTORY = "inventory";
	private static final String SLOT = "Slot";
	private static final int maxStackSize = 640;
	private static final int maxCount = AnzacPeripheralsCore.storageSize / maxStackSize;
	private static final int[] SLOT_ARRAY = Utils.createSlotArray(0, maxCount);

	private final ItemStack[] inventory = new ItemStack[maxCount];
	private boolean useOreDict;
	private boolean ignoreMeta;

	// private List<Position> multiblock = null;
	// private boolean multiblockDirty = false;

	public ItemStorageTileEntity() {
	}

	@Override
	public String getType() {
		return "ItemStorage";
	}

	@Override
	protected List<String> methodNames() {
		return getMethodNames(ItemStorageTileEntity.class);
	}

	@Override
	@PeripheralMethod
	public Object contents() throws Exception {
		final Map<Integer, Integer> table = new HashMap<Integer, Integer>();
		for (final ItemStack stack : inventory) {
			if (stack != null) {
				final int uuid = Utils.getUUID(stack);
				final int amount = stack.stackSize;
				if (table.containsKey(uuid)) {
					final int a = table.get(uuid);
					table.put(uuid, a + amount);
				} else {
					table.put(uuid, amount);
				}
			}
		}
		AnzacPeripheralsCore.logger.info("table:" + table);
		return table;
	}

	protected boolean isAllowed(final ItemStack stack) {
		final int id = getId(stack);
		return isAllowed(id);
	}

	private int getId(final ItemStack stack) {
		int id = -1;
		if (useOreDict) {
			id = OreDictionary.getOreID(stack);
		}
		if (id == -1) {
			if (ignoreMeta) {
				id = stack.itemID;
			} else {
				id = Utils.getUUID(stack);
			}
		}
		return id;
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

		// read inventory slots
		final NBTTagList list = nbtRoot.getTagList(INVENTORY);
		for (byte entry = 0; entry < list.tagCount(); entry++) {
			final NBTTagCompound itemTag = (NBTTagCompound) list.tagAt(entry);
			final int slot = itemTag.getInteger(SLOT);
			if (slot >= 0 && slot < getSizeInventory()) {
				final ItemStack stack = ItemStack.loadItemStackFromNBT(itemTag);
				inventory[slot] = stack;
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

		// write craft slots
		final NBTTagList list = new NBTTagList();
		for (int slot = 0; slot < inventory.length; slot++) {
			final ItemStack stack = inventory[slot];
			if (stack != null) {
				final NBTTagCompound itemTag = new NBTTagCompound();
				itemTag.setInteger(SLOT, slot);
				stack.writeToNBT(itemTag);
				list.appendTag(itemTag);
			}
		}
		nbtRoot.setTag(INVENTORY, list);
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
		return inventory[i];
	}

	@Override
	public void setInventorySlotContents(final int slot, final ItemStack stack) {
		inventory[slot] = stack;
		if (stack == null) {
			return;
		}
		final int inventoryStackLimit = getInventoryStackLimit();
		if (stack.stackSize > inventoryStackLimit) {
			stack.stackSize = inventoryStackLimit;
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
		return SLOT_ARRAY;
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
	public boolean canInsertItem(final int i, final ItemStack itemstack, final int j) {
		return isConnected() && isAllowed(itemstack);
	}

	@Override
	public boolean canExtractItem(final int i, final ItemStack itemstack, final int j) {
		// cannot extract
		return false;
	}

	@Override
	public int getInventoryStackLimit() {
		return maxStackSize;
	}

	@Override
	public boolean isUseableByPlayer(final EntityPlayer entityplayer) {
		return isConnected() && worldObj.getBlockTileEntity(xCoord, yCoord, zCoord) == this
				&& entityplayer.getDistanceSq(xCoord + 0.5D, yCoord + 0.5D, zCoord + 0.5D) <= 64.0D;
	}

	@Override
	public void openChest() {
	}

	@Override
	public void closeChest() {
	}

	@Override
	public boolean isItemValidForSlot(final int i, final ItemStack itemstack) {
		return isConnected() && isAllowed(itemstack);
	}

	@Override
	@PeripheralMethod
	public void addFilter(final int id) throws Exception {
		int uuid = id;
		if (useOreDict) {
			final ItemStack itemStack = Utils.getItemStack(uuid);
			final int oreid = OreDictionary.getOreID(itemStack);
			if (oreid != -1) {
				uuid = oreid;
			}
		}
		if (ignoreMeta) {
			uuid = Utils.getId(uuid);
		}
		super.addFilter(uuid);
	}

	@PeripheralMethod
	public boolean isUseOreDict() {
		return useOreDict;
	}

	@PeripheralMethod
	public void setUseOreDict(final boolean useOreDict) {
		this.useOreDict = useOreDict;
	}

	@PeripheralMethod
	public boolean isIgnoreMeta() {
		return ignoreMeta;
	}

	@PeripheralMethod
	public void setIgnoreMeta(final boolean ignoreMeta) {
		this.ignoreMeta = ignoreMeta;
	}

	@Override
	public int addItem(final ItemStack stack, final boolean doAdd, final ForgeDirection from) {
		final ItemStack copy;
		if (doAdd) {
			copy = stack;
		} else {
			copy = stack.copy();
		}
		if (isAllowed(copy)) {
			for (final int slot : getAccessibleSlotsFromSide(from.ordinal())) {
				final ItemStack stackInSlot = getStackInSlot(slot);
				if (stackInSlot != null && Utils.stacksMatch(stackInSlot, copy)) {
					final int l = stackInSlot.stackSize + copy.stackSize;
					final int inventoryStackLimit = getInventoryStackLimit();
					if (l <= inventoryStackLimit) {
						copy.stackSize = 0;
						stackInSlot.stackSize = l;
					} else if (stackInSlot.stackSize < inventoryStackLimit) {
						copy.stackSize -= inventoryStackLimit - stackInSlot.stackSize;
						stackInSlot.stackSize = inventoryStackLimit;
					}
				}
				if (copy.stackSize == 0) {
					break;
				}
			}
			if (copy.stackSize > 0) {
				for (final int slot : getAccessibleSlotsFromSide(from.ordinal())) {
					final ItemStack stackInSlot = getStackInSlot(slot);
					if (stackInSlot == null) {
						final ItemStack target = copy.copy();
						inventory[slot] = target;
						final int inventoryStackLimit = getInventoryStackLimit();
						if (target.stackSize > inventoryStackLimit) {
							target.stackSize = inventoryStackLimit;
						}
						copy.stackSize -= target.stackSize;
					}
					if (copy.stackSize == 0) {
						break;
					}
				}
			}
			onInventoryChanged();
			return stack.stackSize - copy.stackSize;
		}
		return 0;
	}

	@Override
	public ItemStack[] extractItem(final boolean doRemove, final ForgeDirection from, final int maxItemCount) {
		// cannot extract return empty array
		return new ItemStack[0];
	}
}
