package anzac.peripherals.items;

import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import anzac.peripherals.annotations.Items;

/**
 * @author Tony
 * 
 */
@Items(key = "item.anzac.chargeitem", value = { ItemType.CHARGE_IRON, ItemType.CHARGE_GOLD, ItemType.CHARGE_DIAMOND, })
public class ChargeItem extends ItemBlock {

	public ChargeItem(final int par1) {
		super(par1);
		setMaxStackSize(64);
		setHasSubtypes(true);
	}

	@Override
	public String getUnlocalizedName(final ItemStack par1ItemStack) {
		return ItemFactory.getUnlocalizedName(par1ItemStack);
	}

	@Override
	public int getMetadata(final int par1) {
		return par1;
	}
}
