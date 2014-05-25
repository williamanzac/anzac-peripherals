package anzac.peripherals.items;

import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import anzac.peripherals.annotations.Items;

@Items(key = "item.anzac.teleportitem", value = { ItemType.TELEPORTER_IRON, ItemType.TELEPORTER_GOLD,
		ItemType.TELEPORTER_DIAMOND, })
public class TeleporterItem extends ItemBlock {

	public TeleporterItem(final int par1) {
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
