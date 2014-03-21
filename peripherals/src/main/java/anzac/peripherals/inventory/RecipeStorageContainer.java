package anzac.peripherals.inventory;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import anzac.peripherals.tiles.RecipeStorageTileEntity;

public class RecipeStorageContainer extends BaseItemContainer<RecipeStorageTileEntity> {

	private ItemStack prevOutput;

	public RecipeStorageContainer(final InventoryPlayer inventoryPlayer, final RecipeStorageTileEntity te) {
		super(te);

		addSlotToContainer(new SlotPhantom(te.craftResult, 0, 120, 17) {
			@Override
			public boolean isItemValid(final ItemStack par1ItemStack) {
				return false;
			}

			@Override
			public boolean isAdjustable() {
				return false;
			}
		});

		addSlotToContainer(new Slot(te.discInv, 0, 120, 53) {
			@Override
			public boolean isItemValid(final ItemStack itemStack) {
				return inventory.isItemValidForSlot(getSlotIndex(), itemStack);
			}
		});

		int row;
		int col;
		for (row = 0; row < 3; ++row) {
			for (col = 0; col < 3; ++col) {
				addSlotToContainer(new SlotPhantom(te.craftMatrix, col + row * 3, 30 + col * 18, 17 + row * 18));
			}
		}

		for (row = 0; row < 3; ++row) {
			for (col = 0; col < 9; ++col) {
				addSlotToContainer(new Slot(inventoryPlayer, col + row * 9 + 9, 8 + col * 18, 88 + row * 18));
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
		return te.isUseableByPlayer(par1EntityPlayer);
	}
}
