package anzac.peripherals.inventory;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.ICrafting;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import anzac.peripherals.tiles.TeleporterTileEntity;
import anzac.peripherals.utils.Utils;

public class TeleporterContainer extends Container {
	private final TeleporterTileEntity te;
	private float lastStored;
	private final int numRows = 2;
	private final int numCols = 2;

	public TeleporterContainer(final InventoryPlayer inventoryPlayer, final TeleporterTileEntity te) {
		this.te = te;
		int row;
		int col;

		final int sizeInventory = te.getSizeInventory();
		for (row = 0; row < numRows; row++) {
			for (col = 0; col < numCols; col++) {
				final int slot = row * numCols + col;
				final int x = 116 + 18 * col;
				final int y = 19 + 18 * row;
				if (slot < sizeInventory) {
					addSlotToContainer(new Slot(te, slot, x, y) {
						@Override
						public boolean isItemValid(final ItemStack itemStack) {
							return inventory.isItemValidForSlot(getSlotIndex(), itemStack);
						}
					});
				}
			}
		}

		for (row = 0; row < 3; ++row) {
			for (col = 0; col < 9; ++col) {
				addSlotToContainer(new Slot(inventoryPlayer, col + row * 9 + 9, 8 + col * 18, 84 + row * 18));
			}
		}

		for (row = 0; row < 9; ++row) {
			addSlotToContainer(new Slot(inventoryPlayer, row, 8 + row * 18, 142));
		}
	}

	@Override
	public void addCraftingToCrafters(final ICrafting par1iCrafting) {
		super.addCraftingToCrafters(par1iCrafting);
		sendUpdate(par1iCrafting);
	}

	private void sendUpdate(final ICrafting par1iCrafting) {
		par1iCrafting.sendProgressBarUpdate(this, 0, (int) te.getStoredEnergy());
	}

	@Override
	public void detectAndSendChanges() {
		super.detectAndSendChanges();
		final float storedEnergy = te.getStoredEnergy();
		if (lastStored != storedEnergy) {
			for (final Object crafting : crafters) {
				sendUpdate((ICrafting) crafting);
			}
		}
		lastStored = storedEnergy;
	}

	@Override
	public void updateProgressBar(final int index, final int value) {
		switch (index) {
		case 0:
			te.setStoredEnergy(value);
		}
	}

	@Override
	public boolean canInteractWith(final EntityPlayer entityplayer) {
		return true;
	}

	@Override
	public ItemStack slotClick(final int slotNum, final int mouseButton, final int modifier, final EntityPlayer player) {
		final Slot slot = slotNum < 0 ? null : (Slot) this.inventorySlots.get(slotNum);
		if (slot instanceof ISpecialSlot) {
			return slotClickSpecial(slot, mouseButton, modifier, player);
		}
		return super.slotClick(slotNum, mouseButton, modifier, player);
	}

	private ItemStack slotClickSpecial(final Slot slot, final int mouseButton, final int modifier,
			final EntityPlayer player) {
		ItemStack stack = null;

		if (mouseButton == 2) {
			if (((ISpecialSlot) slot).isAdjustable()) {
				slot.putStack(null);
			}
		} else if (mouseButton == 0 || mouseButton == 1) {
			final InventoryPlayer playerInv = player.inventory;
			slot.onSlotChanged();
			final ItemStack stackSlot = slot.getStack();
			final ItemStack stackHeld = playerInv.getItemStack();

			if (stackSlot != null) {
				stack = stackSlot.copy();
			}

			if (stackSlot == null) {
				if (stackHeld != null && slot.isItemValid(stackHeld)) {
					fillSpecialSlot(slot, stackHeld, mouseButton, modifier);
				}
			} else if (stackHeld == null) {
				adjustSpecialSlot(slot, mouseButton, modifier);
				slot.onPickupFromSlot(player, playerInv.getItemStack());
			} else if (slot.isItemValid(stackHeld)) {
				if (Utils.canMergeItemStack(stackSlot, stackHeld)) {
					adjustSpecialSlot(slot, mouseButton, modifier);
				} else {
					fillSpecialSlot(slot, stackHeld, mouseButton, modifier);
				}
			}
		}
		return stack;
	}

	protected void adjustSpecialSlot(final Slot slot, final int mouseButton, final int modifier) {
		if (!((ISpecialSlot) slot).isAdjustable()) {
			return;
		}
		final ItemStack stackSlot = slot.getStack();
		int stackSize;
		if (modifier == 1) {
			stackSize = mouseButton == 0 ? (stackSlot.stackSize + 1) / 2 : stackSlot.stackSize * 2;
		} else {
			stackSize = mouseButton == 0 ? stackSlot.stackSize - 1 : stackSlot.stackSize + 1;
		}

		if (stackSize > slot.getSlotStackLimit()) {
			stackSize = slot.getSlotStackLimit();
		}

		stackSlot.stackSize = stackSize;

		if (stackSlot.stackSize <= 0) {
			slot.putStack((ItemStack) null);
		}
	}

	protected void fillSpecialSlot(final Slot slot, final ItemStack stackHeld, final int mouseButton, final int modifier) {
		if (!((ISpecialSlot) slot).isAdjustable()) {
			return;
		}
		int stackSize = mouseButton == 0 ? stackHeld.stackSize : 1;
		if (stackSize > slot.getSlotStackLimit()) {
			stackSize = slot.getSlotStackLimit();
		}
		final ItemStack specialStack = stackHeld.copy();
		specialStack.stackSize = stackSize;

		slot.putStack(specialStack);
	}

	protected boolean tryMergeItemStack(final ItemStack stackToShift, final int numSlots) {
		for (int machineIndex = 0; machineIndex < numSlots - 9 * 4; machineIndex++) {
			final Slot slot = (Slot) inventorySlots.get(machineIndex);
			// if (slot instanceof SlotBase && !((SlotBase) slot).canShift()) {
			// continue;
			// }
			if (slot instanceof SlotPhantom) {
				continue;
			}
			if (!slot.isItemValid(stackToShift)) {
				continue;
			}
			if (Utils.mergeItemStack(stackToShift, slot.getStack())) {
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
