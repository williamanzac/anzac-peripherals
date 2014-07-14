package anzac.peripherals.inventory;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import anzac.peripherals.tiles.CraftingRouterTileEntity;

public class CraftingRouterContainer extends BaseItemContainer<CraftingRouterTileEntity> {

	public CraftingRouterContainer(final InventoryPlayer inventoryPlayer, final CraftingRouterTileEntity te) {
		super(te);
		int row;
		int col;

		addSlotToContainer(new Slot(te.discInv, 0, 120, 53) {
			@Override
			public boolean isItemValid(final ItemStack itemStack) {
				return inventory.isItemValidForSlot(getSlotIndex(), itemStack);
			}
		});

		for (row = 0; row < 3; ++row) {
			for (col = 0; col < 3; ++col) {
				addSlotToContainer(new SlotPhantom(te.craftMatrix, col + row * 3, 30 + col * 18, 17 + row * 18));
			}
		}

		addSlotToContainer(new SlotPhantom(te.craftResult, 0, 120, 17));

		for (row = 0; row < 3; ++row) {
			for (col = 0; col < 9; ++col) {
				addSlotToContainer(new Slot(inventoryPlayer, col + row * 9 + 9, 8 + col * 18, 88 + row * 18));
			}
		}

		for (row = 0; row < 9; ++row) {
			addSlotToContainer(new Slot(inventoryPlayer, row, 8 + row * 18, 146));
		}
	}

	@Override
	public boolean canInteractWith(final EntityPlayer entityplayer) {
		return te.isUseableByPlayer(entityplayer);
	}
}
