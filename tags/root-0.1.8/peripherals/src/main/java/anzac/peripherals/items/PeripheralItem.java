package anzac.peripherals.items;

import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;

public class PeripheralItem extends ItemBlock {

	public PeripheralItem(final int par1) {
		super(par1);
		setMaxStackSize(64);
		setHasSubtypes(true);
		setUnlocalizedName("anzacperipheral");
	}

	@Override
	public String getUnlocalizedName(final ItemStack par1ItemStack) {
		final int meta = par1ItemStack.getItemDamage();
		switch (meta) {
		case 0:
			return "item.computerBench";
		case 1:
			return "item.recipeStorage";
		case 2:
			return "item.itemRouter";
		case 3:
			return "item.fluidRouter";
		case 4:
			return "item.itemStorage";
		case 5:
			return "item.fluidStorage";
		case 6:
			return "item.redstonecontrol";
		case 7:
			return "item.craftingRouter";
		}
		return super.getUnlocalizedName(par1ItemStack);
	}

	@Override
	public int getMetadata(final int par1) {
		return par1;
	}
}
