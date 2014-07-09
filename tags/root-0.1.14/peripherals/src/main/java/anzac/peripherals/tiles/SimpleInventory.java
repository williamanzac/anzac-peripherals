package anzac.peripherals.tiles;

import java.util.HashSet;
import java.util.Set;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;

public class SimpleInventory implements IInventory {

	private final ItemStack[] inv;
	private final Set<InventoryListener> listeners = new HashSet<InventoryListener>();

	public SimpleInventory(final int size) {
		inv = new ItemStack[size];
	}

	@Override
	public int getSizeInventory() {
		return inv.length;
	}

	@Override
	public ItemStack getStackInSlot(final int slot) {
		return inv[slot];
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
	public void setInventorySlotContents(final int slot, final ItemStack stack) {
		inv[slot] = stack;
		if (stack != null) {
			final int inventoryStackLimit = getInventoryStackLimit();
			if (stack.stackSize > inventoryStackLimit) {
				stack.stackSize = inventoryStackLimit;
			}
		}
		onInventoryChanged();
	}

	@Override
	public String getInvName() {
		return null;
	}

	@Override
	public boolean isInvNameLocalized() {
		return false;
	}

	@Override
	public int getInventoryStackLimit() {
		return 64;
	}

	@Override
	public void onInventoryChanged() {
		for (final InventoryListener listener : listeners) {
			listener.inventoryChanged();
		}
	}

	@Override
	public boolean isUseableByPlayer(final EntityPlayer entityplayer) {
		return true;
	}

	@Override
	public void openChest() {
	}

	@Override
	public void closeChest() {
	}

	@Override
	public boolean isItemValidForSlot(final int i, final ItemStack itemstack) {
		return false;
	}

	public void readFromNBT(final NBTTagCompound tagCompound) {
		// read slots
		final NBTTagList list = tagCompound.getTagList("inventory");
		for (byte entry = 0; entry < list.tagCount(); entry++) {
			final NBTTagCompound itemTag = (NBTTagCompound) list.tagAt(entry);
			final int slot = itemTag.getByte("slot");
			if (slot >= 0 && slot < getSizeInventory()) {
				final ItemStack stack = ItemStack.loadItemStackFromNBT(itemTag);
				setInventorySlotContents(slot, stack);
			}
		}
	}

	public void writeToNBT(final NBTTagCompound tagCompound) {
		// write slots
		final NBTTagList list = new NBTTagList();
		for (byte slot = 0; slot < getSizeInventory(); slot++) {
			final ItemStack stack = getStackInSlot(slot);
			if (stack != null) {
				final NBTTagCompound itemTag = new NBTTagCompound();
				itemTag.setByte("slot", slot);
				stack.writeToNBT(itemTag);
				list.appendTag(itemTag);
			}
		}
		tagCompound.setTag("inventory", list);
	}

	public void addListner(final InventoryListener listener) {
		listeners.add(listener);
	}

	public void removeListner(final InventoryListener listener) {
		listeners.remove(listener);
	}
}
