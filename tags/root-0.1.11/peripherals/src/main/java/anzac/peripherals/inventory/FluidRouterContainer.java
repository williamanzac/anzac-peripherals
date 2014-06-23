package anzac.peripherals.inventory;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.ICrafting;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;
import anzac.peripherals.tiles.FluidRouterTileEntity;

public class FluidRouterContainer extends BaseItemContainer<FluidRouterTileEntity> {
	private int lastAmount;

	public FluidRouterContainer(final InventoryPlayer inventoryPlayer, final FluidRouterTileEntity te) {
		super(te);
		int row;
		int col;

		addSlotToContainer(new Slot(te.discInv, 0, 120, 49) {
			@Override
			public boolean isItemValid(final ItemStack itemStack) {
				return inventory.isItemValidForSlot(getSlotIndex(), itemStack);
			}
		});

		// addSlotToContainer(new Slot(te, 0, 48, 35) {
		// @Override
		// public boolean isItemValid(final ItemStack itemStack) {
		// return inventory.isItemValidForSlot(getSlotIndex(), itemStack);
		// }
		// });

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
		final FluidStack fluid = te.getInfo().fluid;
		if (fluid != null) {
			par1iCrafting.sendProgressBarUpdate(this, 0, fluid.amount);
		} else {
			par1iCrafting.sendProgressBarUpdate(this, 0, 0);
		}
	}

	@Override
	public void detectAndSendChanges() {
		super.detectAndSendChanges();
		final FluidStack fluid = te.getInfo().fluid;
		final int amount = fluid != null ? fluid.amount : 0;
		if (lastAmount != amount) {
			for (final Object crafting : crafters) {
				sendUpdate((ICrafting) crafting);
			}
		}
		lastAmount = amount;
	}

	@Override
	public void updateProgressBar(final int index, final int value) {
		switch (index) {
		case 0:
			final FluidStack fluid = te.getInfo().fluid;
			if (fluid != null) {
				fluid.amount = value;
			}
		}
	}

	@Override
	public boolean canInteractWith(final EntityPlayer entityplayer) {
		return te.isUseableByPlayer(entityplayer);
	}
}
