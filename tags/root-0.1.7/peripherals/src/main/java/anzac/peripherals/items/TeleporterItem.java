package anzac.peripherals.items;

import java.util.List;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;

public class TeleporterItem extends ItemBlock {

	public TeleporterItem(final int par1) {
		super(par1);
		setMaxStackSize(64);
		setHasSubtypes(true);
		setUnlocalizedName("anzacteleporter");
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public void getSubItems(final int par1, final CreativeTabs par2CreativeTabs, final List par3List) {
		par3List.add(new ItemStack(par1, 1, 1));
		par3List.add(new ItemStack(par1, 1, 2));
		par3List.add(new ItemStack(par1, 1, 3));
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
