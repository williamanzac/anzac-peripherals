package anzac.peripherals.tiles;

import java.util.HashSet;
import java.util.Set;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import anzac.peripherals.AnzacPeripheralsCore;
import anzac.peripherals.utils.Utils;

public class ItemStorageTileEntity extends ItemRouterTileEntity {

	private final Set<ItemStack> inventory = new HashSet<ItemStack>();
	private int totalCount = 0;
	private static final int maxCount = AnzacPeripheralsCore.storageSize / 64;

	// private List<Position> multiblock = null;
	// private boolean multiblockDirty = false;

	public ItemStorageTileEntity() {
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

		totalCount = 0;
		final NBTTagList itemList = nbtRoot.getTagList("Items");
		for (int i = 0; i < itemList.tagCount(); i++) {
			final NBTTagCompound itemStack = (NBTTagCompound) itemList.tagAt(i);
			final ItemStack stack = ItemStack.loadItemStackFromNBT(itemStack);
			totalCount += stack.stackSize;
			inventory.add(stack);
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

		// write inventory list
		final NBTTagList itemList = new NBTTagList();
		for (final ItemStack entry : inventory) {
			if (entry != null && entry.stackSize > 0) {
				final NBTTagCompound itemStackNBT = new NBTTagCompound();
				entry.writeToNBT(itemStackNBT);
				itemList.appendTag(itemStackNBT);
			}
		}
		nbtRoot.setTag("Items", itemList);
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
	protected void defaultRoute(final ItemStack copy) {
		boolean found = false;
		for (final ItemStack entry : inventory) {
			if (Utils.stacksMatch(entry, copy)) {
				found = true;
				if (totalCount + copy.stackSize >= maxCount) {
					final int max = maxCount - totalCount;
					copy.splitStack(max);
					entry.stackSize += copy.stackSize;
					totalCount += copy.stackSize;
				} else {
					entry.stackSize += copy.stackSize;
					totalCount += copy.stackSize;
					copy.stackSize = 0;
				}
				break;
			}
		}
		if (!found) {
			inventory.add(copy);
			if (totalCount + copy.stackSize >= maxCount) {
				final int max = maxCount - totalCount;
				copy.splitStack(max);
				totalCount += copy.stackSize;
			} else {
				totalCount += copy.stackSize;
				copy.stackSize = 0;
			}
		}
		if (copy.stackSize > 0) {
			super.defaultRoute(copy);
		}
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

}
