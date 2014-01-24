package anzac.peripherals.tiles;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;

public class InternalInventoryCrafting extends InventoryCrafting {

	private static final class InternalCraftingContainer extends Container {
		@Override
		public boolean canInteractWith(final EntityPlayer entityplayer) {
			return false;
		}
	}

	private int[] bindings;
	private int[] counts;
	private boolean crafting;
	private IInventory inventory;

	public InternalInventoryCrafting(final int size) {
		super(new InternalCraftingContainer(), size, size);
	}

	public InternalInventoryCrafting(final int size, final IInventory inventory) {
		this(size);
		this.inventory = inventory;
	}

	@Override
	public int getInventoryStackLimit() {
		return 1;
	}

	@Override
	public boolean isUseableByPlayer(final EntityPlayer par1EntityPlayer) {
		return false;
	}

	@Override
	public ItemStack getStackInSlot(final int slot) {
		if (crafting && inventory != null && bindings != null && bindings.length > 0) {
			if (bindings[slot] >= 0) {
				return inventory.getStackInSlot(bindings[slot]);
			}
			return null;
		}
		return super.getStackInSlot(slot);
	}

	@Override
	public ItemStack decrStackSize(final int slot, final int amount) {
		if (crafting && inventory != null && bindings != null && bindings.length > 0) {
			if (bindings[slot] >= 0) {
				final ItemStack stackInSlot = inventory.getStackInSlot(bindings[slot]);
				if (stackInSlot.stackSize <= amount) {
					final ItemStack result = stackInSlot;
					inventory.setInventorySlotContents(bindings[slot], null);
					return result;
				} else {
					final ItemStack result = stackInSlot.splitStack(amount);
					if (stackInSlot.stackSize <= 0) {
						inventory.setInventorySlotContents(bindings[slot], null);
						bindings[slot] = -1;
					}
					return result;
				}
			}
			return null;
		}
		return super.decrStackSize(slot, amount);
	}

	public void clear() {
		for (int i = 0; i < getSizeInventory(); i++) {
			setInventorySlotContents(i, null);
		}
	}

	@Override
	public void setInventorySlotContents(final int slot, final ItemStack itemStack) {
		super.setInventorySlotContents(slot, itemStack);
	}

	public boolean hasIngredients() {
		counts = new int[inventory.getSizeInventory()];
		bindings = new int[getSizeInventory()];
		int i, j;
		boolean foundMatch = false;
		for (i = 0; i < getSizeInventory(); i++) {
			ItemStack stackInSlot = getStackInSlot(i);
			if (stackInSlot == null) {
				bindings[i] = -1;
				continue;
			}
			foundMatch = false;
			for (j = 0; j < inventory.getSizeInventory(); j++) {
				ItemStack invStack = inventory.getStackInSlot(j);
				if (invStack == null) {
					continue;
				}
				boolean itemEqual = invStack.isItemEqual(stackInSlot);
				boolean b = counts[j] < invStack.stackSize;
				if (itemEqual && b) {
					bindings[i] = j;
					counts[j]++;
					foundMatch = true;
					break;
				}
			}
		}
		return foundMatch;
	}

	public void setCrafting(boolean crafting) {
		this.crafting = crafting;
		if (!crafting) {
			counts = null;
			bindings = null;
		}
	}
}