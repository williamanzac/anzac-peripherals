package anzac.peripherals.items;

import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import anzac.peripherals.annotations.Items;

@Items(key = "item.anzac.peripheral", value = { ItemType.WORKBENCH, ItemType.RECIPE_STORAGE, ItemType.ITEM_ROUTER,
		ItemType.FLUID_ROUTER, ItemType.ITEM_STORAGE, ItemType.FLUID_STORAGE, ItemType.REDSTONE_CONTROL,
		ItemType.CRAFTING_ROUTER, })
public class PeripheralItem extends ItemBlock {

	public PeripheralItem(final int par1) {
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
