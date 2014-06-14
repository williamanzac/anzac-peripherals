package anzac.peripherals.tiles;

import java.util.HashMap;
import java.util.Map;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.ForgeDirection;
import net.minecraftforge.oredict.OreDictionary;
import anzac.peripherals.AnzacPeripheralsCore;
import anzac.peripherals.peripheral.ItemStoragePeripheral;
import anzac.peripherals.utils.Utils;
import buildcraft.api.inventory.ISpecialInventory;

public class ItemStorageTileEntity extends BaseStorageTileEntity implements IInventory, ISidedInventory,
		ISpecialInventory {

	private final class DiscListener implements InventoryListener {
		@Override
		public void inventoryChanged() {
			readFromDisk();
		}
	}

	private static final String INVENTORY = "inventory";
	private static final String SLOT = "Slot";
	private static final int maxStackSize = 128;
	private static final int maxCount = (AnzacPeripheralsCore.storageSize / 128) / maxStackSize;
	private static final int[] SLOT_ARRAY = Utils.createSlotArray(0, maxCount);

	private final Map<Integer, ItemStack> inventory = new HashMap<Integer, ItemStack>(maxCount);
	private boolean useOreDict = true;
	private boolean ignoreMeta = true;

	public ItemStorageTileEntity() throws Exception {
		super(ItemStoragePeripheral.class);
		discInv.addListner(new DiscListener());
	}

	public Map<Integer, Integer> contents() throws Exception {
		final Map<Integer, Integer> table = new HashMap<Integer, Integer>();
		for (int slot = 0; slot < getSizeInventory(); slot++) {
			final ItemStack stack = getStackInSlot(slot);
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

	@Override
	protected int getId(final int uuid) {
		int id = -1;
		final ItemStack stack = Utils.getItemStack(uuid);
		if (isUseOreDict()) {
			id = OreDictionary.getOreID(stack);
		}
		if (id == -1) {
			if (isIgnoreMeta()) {
				id = stack.itemID;
			} else {
				id = Utils.getUUID(stack);
			}
		}
		return id;
	}

	protected boolean isAllowed(final ItemStack stack) {
		return isAllowed(Utils.getUUID(stack));
	}

	@Override
	public void readFromNBT(final NBTTagCompound nbtRoot) {
		super.readFromNBT(nbtRoot);

		if (nbtRoot.hasKey("useore")) {
			useOreDict = nbtRoot.getBoolean("useore");
		}
		if (nbtRoot.hasKey("ignoreMeta")) {
			ignoreMeta = nbtRoot.getBoolean("ignoreMeta");
		}

		readFromDisk();
	}

	private void readFromDisk() {
		inventory.clear();
		final ItemStack hddItem = discInv.getHDDItem();
		if (hddItem != null && hddItem.hasTagCompound()) {
			final NBTTagCompound tagCompound = hddItem.getTagCompound();
			final NBTTagList list = tagCompound.getTagList(INVENTORY);
			for (byte entry = 0; entry < list.tagCount(); entry++) {
				final NBTTagCompound itemTag = (NBTTagCompound) list.tagAt(entry);
				final int slot = itemTag.getInteger(SLOT);
				if (slot >= 0 && slot < getSizeInventory()) {
					final ItemStack stack = createStack(itemTag);
					setInventorySlotContents(slot, stack);
				}
			}
		}
	}

	private ItemStack createStack(final NBTTagCompound itemTag) {
		final int uuid = itemTag.getInteger("uuid");
		final int size = itemTag.getInteger("count");
		final ItemStack stack = Utils.getItemStack(uuid, size);
		if (itemTag.hasKey("tag")) {
			stack.stackTagCompound = itemTag.getCompoundTag("tag");
		}
		return stack;
	}

	@Override
	public void writeToNBT(final NBTTagCompound nbtRoot) {
		super.writeToNBT(nbtRoot);

		nbtRoot.setBoolean("useore", useOreDict);
		nbtRoot.setBoolean("ignoreMeta", ignoreMeta);

		writeToDisk();
	}

	private void writeToDisk() {
		final ItemStack hddItem = discInv.getHDDItem();
		if (hddItem != null) {
			if (!hddItem.hasTagCompound()) {
				hddItem.setTagCompound(new NBTTagCompound());
			}
			final NBTTagCompound tagCompound = hddItem.getTagCompound();
			final NBTTagList list = new NBTTagList();
			for (int slot = 0; slot < getSizeInventory(); slot++) {
				final ItemStack stack = getStackInSlot(slot);
				if (stack != null) {
					final NBTTagCompound itemTag = new NBTTagCompound();
					itemTag.setInteger(SLOT, slot);
					writeStack(stack, itemTag);
					list.appendTag(itemTag);
				}
			}
			tagCompound.setTag(INVENTORY, list);
		}
	}

	private void writeStack(final ItemStack stack, final NBTTagCompound itemTag) {
		itemTag.setInteger("uuid", Utils.getUUID(stack));
		itemTag.setInteger("count", stack.stackSize);
		if (stack.hasTagCompound()) {
			itemTag.setTag("tag", stack.stackTagCompound);
		}
	}

	@Override
	public int getSizeInventory() {
		return maxCount;
	}

	@Override
	public ItemStack getStackInSlot(final int i) {
		return discInv.getHDDItem() != null ? inventory.get(i) : null;
	}

	@Override
	public void setInventorySlotContents(final int slot, final ItemStack stack) {
		inventory.put(slot, stack);
		if (stack != null) {
			final int inventoryStackLimit = getInventoryStackLimit();
			if (stack.stackSize > inventoryStackLimit) {
				stack.stackSize = inventoryStackLimit;
			}
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
				} else {
					setInventorySlotContents(slot, stack);
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

	@Override
	public boolean canInsertItem(final int i, final ItemStack itemstack, final int j) {
		return discInv.getHDDItem() != null && isConnected() && isAllowed(itemstack);
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
		final TileEntity entity = worldObj.getBlockTileEntity(xCoord, yCoord, zCoord);
		final double distanceSq = entityplayer.getDistanceSq(xCoord + 0.5D, yCoord + 0.5D, zCoord + 0.5D);
		return isConnected() && entity == this && distanceSq <= 64.0D;
	}

	@Override
	public void openChest() {
	}

	@Override
	public void closeChest() {
	}

	@Override
	public boolean isItemValidForSlot(final int i, final ItemStack itemstack) {
		return discInv.getHDDItem() != null && isConnected() && isAllowed(itemstack);
	}

	public boolean isUseOreDict() {
		return useOreDict;
	}

	public void setUseOreDict(final boolean useOreDict) {
		this.useOreDict = useOreDict;
	}

	public boolean isIgnoreMeta() {
		return ignoreMeta;
	}

	public void setIgnoreMeta(final boolean ignoreMeta) {
		this.ignoreMeta = ignoreMeta;
	}

	@Override
	public int addItem(final ItemStack stack, final boolean doAdd, final ForgeDirection from) {
		return Utils.addItem(this, stack, doAdd, from);
	}

	@Override
	public ItemStack[] extractItem(final boolean doRemove, final ForgeDirection from, final int maxItemCount) {
		// cannot extract return empty array
		return new ItemStack[0];
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + (ignoreMeta ? 1231 : 1237);
		result = prime * result + ((inventory == null) ? 0 : inventory.hashCode());
		result = prime * result + (useOreDict ? 1231 : 1237);
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		ItemStorageTileEntity other = (ItemStorageTileEntity) obj;
		if (ignoreMeta != other.ignoreMeta)
			return false;
		if (inventory == null) {
			if (other.inventory != null)
				return false;
		} else if (!inventory.equals(other.inventory))
			return false;
		if (useOreDict != other.useOreDict)
			return false;
		return true;
	}
}
