package anzac.peripherals.inventory;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

public abstract class BaseItemContainer extends Container {

	protected final IInventory te;

	public BaseItemContainer(final IInventory te) {
		this.te = te;
	}

	@Override
	public boolean canInteractWith(final EntityPlayer par1EntityPlayer) {
		return te.isUseableByPlayer(par1EntityPlayer);
	}

	private boolean tryMergeItemStack(final ItemStack stackToShift, final int numSlots) {
		for (int machineIndex = 0; machineIndex < numSlots - 9 * 4; machineIndex++) {
			final Slot slot = (Slot) inventorySlots.get(machineIndex);
			// if (slot instanceof SlotBase && !((SlotBase) slot).canShift()) {
			// continue;
			// }
			// if (slot instanceof IPhantomSlot) {
			// continue;
			// }
			if (!slot.isItemValid(stackToShift)) {
				continue;
			}
			if (mergeItemStack(stackToShift, machineIndex, machineIndex + 1, false)) {
				return true;
			}
		}
		return false;
	}

	@Override
	public ItemStack transferStackInSlot(final EntityPlayer player, final int slot) {
		ItemStack stack = null;
		final Slot slotObject = (Slot) inventorySlots.get(slot);
		final int numSlots = inventorySlots.size();

		// null checks and checks if the item can be stacked (maxStackSize > 1)
		if (slotObject != null && slotObject.getHasStack()) {
			final ItemStack stackInSlot = slotObject.getStack();
			stack = stackInSlot.copy();

			final int palyerStart = numSlots - 9 * 4;
			final int hotbarStart = numSlots - 9;
			if (slot >= palyerStart && tryMergeItemStack(stackInSlot, numSlots)) {
				// NOOP
			} else if (slot >= palyerStart && slot < hotbarStart) {
				if (!mergeItemStack(stackInSlot, hotbarStart, numSlots, false)) {
					return null;
				}
			} else if (slot >= hotbarStart && slot < numSlots) {
				if (!mergeItemStack(stackInSlot, palyerStart, hotbarStart, false)) {
					return null;
				}
			} else if (!mergeItemStack(stackInSlot, palyerStart, numSlots, false)) {
				return null;
			}

			if (stackInSlot.stackSize == 0) {
				slotObject.putStack(null);
			} else {
				slotObject.onSlotChanged();
			}

			if (stackInSlot.stackSize == stack.stackSize) {
				return null;
			}
			slotObject.onPickupFromSlot(player, stackInSlot);
		}
		return stack;
	}
}