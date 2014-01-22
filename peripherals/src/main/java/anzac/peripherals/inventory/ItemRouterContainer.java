package anzac.peripherals.inventory;

import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import anzac.peripherals.tiles.ItemRouterTileEntity;

public class ItemRouterContainer extends BaseItemContainer {

	public ItemRouterContainer(final InventoryPlayer inventoryPlayer, final ItemRouterTileEntity te) {
		super(te);
		int row;
		int col;

		addSlotToContainer(new Slot(te, 0, 80, 35) {
			@Override
			public boolean isItemValid(final ItemStack itemStack) {
				return inventory.isItemValidForSlot(getSlotIndex(), itemStack);
			}
		});

		for (row = 0; row < 3; ++row) {
			for (col = 0; col < 9; ++col) {
				addSlotToContainer(new Slot(inventoryPlayer, col + row * 9 + 9, 8 + col * 18, 84 + row * 18));
			}
		}

		for (row = 0; row < 9; ++row) {
			addSlotToContainer(new Slot(inventoryPlayer, row, 8 + row * 18, 142));
		}
	}
}
