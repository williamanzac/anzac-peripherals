package anzac.peripherals.inventory;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.ICrafting;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;
import anzac.peripherals.tiles.FluidSupplierTileEntity;

public class FluidSupplierContainer extends BaseItemContainer<FluidSupplierTileEntity> {
	private int lastAmount;
	private float lastStored;

	public FluidSupplierContainer(final InventoryPlayer inventoryPlayer, final FluidSupplierTileEntity te) {
		super(te);
		int row;
		int col;

		addSlotToContainer(new Slot(te, 0, 134, 22) {
			@Override
			public boolean isItemValid(final ItemStack itemStack) {
				return inventory.isItemValidForSlot(getSlotIndex(), itemStack);
			}
		});

		addSlotToContainer(new Slot(te.discInv, 0, 134, 53) {
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

	@Override
	public void addCraftingToCrafters(final ICrafting par1iCrafting) {
		super.addCraftingToCrafters(par1iCrafting);
		sendUpdate(par1iCrafting);
	}

	private void sendUpdate(final ICrafting par1iCrafting) {
		par1iCrafting.sendProgressBarUpdate(this, 0, (int) te.getStoredEnergy());
		final FluidStack fluid = te.getInfo().fluid;
		if (fluid != null) {
			par1iCrafting.sendProgressBarUpdate(this, 1, fluid.amount);
		} else {
			par1iCrafting.sendProgressBarUpdate(this, 1, 0);
		}
	}

	@Override
	public void detectAndSendChanges() {
		super.detectAndSendChanges();
		final float storedEnergy = te.getStoredEnergy();
		final FluidStack fluid = te.getInfo().fluid;
		final int amount = fluid != null ? fluid.amount : 0;
		if (lastStored != storedEnergy || lastAmount != amount) {
			for (final Object crafting : crafters) {
				sendUpdate((ICrafting) crafting);
			}
		}
		lastStored = storedEnergy;
		lastAmount = amount;
	}

	@Override
	public void updateProgressBar(final int index, final int value) {
		switch (index) {
		case 0:
			te.setStoredEnergy(value);
			break;
		case 1:
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
