package anzac.peripherals.items;

import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import anzac.peripherals.annotations.Item;

public class ChargeItem extends ItemBlock {

	@Item
	private static final int CHARGE_IRON = 1;
	@Item
	private static final int CHARGE_GOLD = 2;
	@Item
	private static final int CHARGE_DIAMOND = 3;

	public ChargeItem(final int par1) {
		super(par1);
		setMaxStackSize(64);
		setHasSubtypes(true);
		setUnlocalizedName("anzacchargestation");
	}

	@Override
	public String getUnlocalizedName(final ItemStack par1ItemStack) {
		final int meta = par1ItemStack.getItemDamage();
		switch (meta) {
		case CHARGE_IRON:
			return "item.chargeIron";
		case CHARGE_GOLD:
			return "item.chargeGold";
		case CHARGE_DIAMOND:
			return "item.chargeDiamond";
		}
		return super.getUnlocalizedName(par1ItemStack);
	}

	@Override
	public int getMetadata(final int par1) {
		return par1;
	}
}
