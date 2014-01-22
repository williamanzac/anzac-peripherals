package anzac.peripherals.inventory;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.inventory.SlotCrafting;
import net.minecraft.item.ItemStack;
import anzac.peripherals.tiles.WorkbenchTileEntity;

public class WorkbenchContainer extends BaseItemContainer {

	private ItemStack prevOutput;

	public WorkbenchContainer(final InventoryPlayer inventoryPlayer, final WorkbenchTileEntity te) {
		super(te);

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
				addSlotToContainer(new Slot(te.craftMatrix, col + row * 3, 30 + col * 18, 17 + row * 18) {
					@Override
					public boolean isItemValid(ItemStack par1ItemStack) {
						return false;
					}

					@Override
					public boolean canTakeStack(final EntityPlayer par1EntityPlayer) {
						return false;
					}
				});
			}
		}

		for (row = 0; row < 3; ++row) {
			for (col = 0; col < 5; ++col) {
				addSlotToContainer(new Slot(te, col + row * 5, 12 + col * 18, 87 + row * 18) {
					@Override
					public boolean isItemValid(final ItemStack itemStack) {
						return inventory.isItemValidForSlot(getSlotIndex(), itemStack);
					}
				});
			}
		}
		for (row = 0; row < 3; ++row) {
			for (col = 0; col < 3; ++col) {
				addSlotToContainer(new Slot(te, 15 + col + row * 3, 106 + col * 18, 87 + row * 18) {
					@Override
					public boolean isItemValid(final ItemStack itemStack) {
						return inventory.isItemValidForSlot(getSlotIndex(), itemStack);
					}
				});
			}
		}

		for (row = 0; row < 3; ++row) {
			for (col = 0; col < 9; ++col) {
				addSlotToContainer(new Slot(inventoryPlayer, 9 + col + row * 9, 8 + col * 18, 155 + row * 18));
			}
		}
		for (row = 0; row < 9; ++row) {
			addSlotToContainer(new Slot(inventoryPlayer, row, 8 + row * 18, 213));
		}

		onCraftMatrixChanged(te.craftMatrix);
	}

	@Override
	public void onCraftMatrixChanged(final IInventory par1IInventory) {
		super.onCraftMatrixChanged(par1IInventory);
		getTileEntity().updateCraftingRecipe();
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
		final WorkbenchTileEntity tileEntity = getTileEntity();
		final ItemStack output = tileEntity.craftResult.getStackInSlot(0);
		if (output != prevOutput) {
			prevOutput = output;
			onCraftMatrixChanged(tileEntity.craftMatrix);
		}
	}

	@Override
	public boolean canInteractWith(final EntityPlayer entityplayer) {
		return te.isUseableByPlayer(entityplayer);
	}

	private WorkbenchTileEntity getTileEntity() {
		return (WorkbenchTileEntity) te;
	}

}
