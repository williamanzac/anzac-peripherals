package anzac.peripherals.inventory;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.inventory.SlotCrafting;
import net.minecraft.item.ItemStack;
import anzac.peripherals.tiles.RecipeStorageTileEntity;

public class RecipeStorageContainer extends Container {

	private ItemStack prevOutput;
	private final RecipeStorageTileEntity te;

	public RecipeStorageContainer(final InventoryPlayer inventoryPlayer, final RecipeStorageTileEntity te) {
		this.te = te;

		addSlotToContainer(new SlotCrafting(inventoryPlayer.player, te.craftMatrix, te.craftResult, 0, 124, 35) {
			@Override
			public boolean canTakeStack(final EntityPlayer par1EntityPlayer) {
				return false;
			}
		});
		int row;
		int col;
		for (row = 0; row < 3; ++row) {
			for (col = 0; col < 3; ++col) {
				addSlotToContainer(new Slot(te.craftMatrix, col + row * 3, 30 + col * 18, 17 + row * 18));
			}
		}

		for (row = 0; row < 3; ++row) {
			for (col = 0; col < 9; ++col) {
				addSlotToContainer(new Slot(inventoryPlayer, col + row * 9 + 9,
						8 + col * 18, 88 + row * 18));
			}
		}

		for (row = 0; row < 9; ++row) {
			addSlotToContainer(new Slot(inventoryPlayer, row, 8 + row * 18, 146));
		}

		onCraftMatrixChanged(te.craftMatrix);
	}

	@Override
	public void onCraftMatrixChanged(final IInventory par1IInventory) {
		super.onCraftMatrixChanged(par1IInventory);
		final RecipeStorageTileEntity tileEntity = getTileEntity();
		tileEntity.onCraftMatrixChanged();
	}

	@Override
	public ItemStack slotClick(final int i, final int j, final int modifier, final EntityPlayer entityplayer) {
		final ItemStack stack = super.slotClick(i, j, modifier, entityplayer);
		onCraftMatrixChanged(getTileEntity().craftMatrix);
		return stack;
	}

	@Override
	public void detectAndSendChanges() {
		super.detectAndSendChanges();
		final RecipeStorageTileEntity tileEntity = getTileEntity();
		final ItemStack output = tileEntity.craftResult.getStackInSlot(0);
		if (output != prevOutput) {
			prevOutput = output;
			onCraftMatrixChanged(tileEntity.craftMatrix);
		}
	}

	private RecipeStorageTileEntity getTileEntity() {
		return te;
	}

	@Override
	public boolean canInteractWith(final EntityPlayer par1EntityPlayer) {
		return true;
	}

	private boolean tryMergeItemStack(final ItemStack stackToShift,
			final int numSlots) {
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
			if (mergeItemStack(stackToShift, machineIndex, machineIndex + 1,
					false)) {
				return true;
			}
		}
		return false;
	}

	@Override
	public ItemStack transferStackInSlot(final EntityPlayer player,
			final int slot) {
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
				if (!mergeItemStack(stackInSlot, palyerStart, hotbarStart,
						false)) {
					return null;
				}
			} else if (!mergeItemStack(stackInSlot, palyerStart, numSlots,
					false)) {
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
