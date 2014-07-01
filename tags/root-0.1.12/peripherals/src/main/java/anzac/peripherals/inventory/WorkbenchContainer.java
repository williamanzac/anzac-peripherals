package anzac.peripherals.inventory;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import anzac.peripherals.tiles.WorkbenchTileEntity;

public class WorkbenchContainer extends BaseItemContainer<WorkbenchTileEntity> {

	public WorkbenchContainer(final InventoryPlayer inventoryPlayer, final WorkbenchTileEntity te) {
		super(te);

		addSlotToContainer(new SlotPhantom(te.craftResult, 0, 124, 35) {
			@Override
			public boolean isItemValid(final ItemStack par1ItemStack) {
				return false;
			}

			@Override
			public boolean isAdjustable() {
				return false;
			}
		});
		int row;
		int col;
		for (row = 0; row < 3; ++row) {
			for (col = 0; col < 3; ++col) {
				addSlotToContainer(new SlotPhantom(te.craftMatrix, col + row * 3, 30 + col * 18, 17 + row * 18) {
					@Override
					public boolean isAdjustable() {
						return false;
					}
				});
			}
		}

		for (row = 0; row < 3; ++row) {
			for (col = 0; col < 5; ++col) {
				addSlotToContainer(new Slot(te, col + row * 5, 12 + col * 18, 87 + row * 18));
			}
		}
		for (row = 0; row < 3; ++row) {
			for (col = 0; col < 3; ++col) {
				addSlotToContainer(new SlotOutput(te, 15 + col + row * 3, 106 + col * 18, 87 + row * 18));
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

		te.updateCraftingRecipe();
	}

	@Override
	public boolean canInteractWith(final EntityPlayer entityplayer) {
		return te.isUseableByPlayer(entityplayer);
	}
}
