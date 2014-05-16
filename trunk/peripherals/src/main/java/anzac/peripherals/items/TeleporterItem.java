package anzac.peripherals.items;

import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;

public class TeleporterItem extends ItemBlock {

	public TeleporterItem(final int par1) {
		super(par1);
		setMaxStackSize(64);
		setHasSubtypes(true);
		setUnlocalizedName("anzacteleporter");
	}

	@Override
	public String getUnlocalizedName(final ItemStack par1ItemStack) {
		final int meta = par1ItemStack.getItemDamage();
		switch (meta) {
		case 1:
			return "item.teleportIron";
		case 2:
			return "item.teleportGold";
		case 3:
			return "item.teleportDiamond";
		}
		return super.getUnlocalizedName(par1ItemStack);
	}

	@Override
	public int getMetadata(final int par1) {
		return par1;
	}
}
