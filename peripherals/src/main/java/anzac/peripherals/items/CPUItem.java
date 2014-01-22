package anzac.peripherals.items;

import java.util.List;

import net.minecraft.client.renderer.texture.IconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Icon;
import anzac.peripherals.AnzacPeripheralsCore;

public class CPUItem extends Item {

	private Icon basicIcon;
	private Icon advancedIcon;

	public CPUItem(final int id) {
		super(id);
		setHasSubtypes(true);
		setCreativeTab(CreativeTabs.tabRedstone);
		setUnlocalizedName("anzaccpu");
	}

	@Override
	public void getSubItems(final int par1, final CreativeTabs par2CreativeTabs, final List par3List) {
		par3List.add(new ItemStack(AnzacPeripheralsCore.cpu, 1, 0));
		par3List.add(new ItemStack(AnzacPeripheralsCore.cpu, 1, 1));
	}

	@Override
	public void registerIcons(final IconRegister par1IconRegister) {
		basicIcon = par1IconRegister.registerIcon("anzac:basiccpu");
		advancedIcon = par1IconRegister.registerIcon("anzac:advancedcpu");
	}

	@Override
	public Icon getIconFromDamage(final int par1) {
		return par1 == 0 ? basicIcon : advancedIcon;
	}

	@Override
	public String getUnlocalizedName(final ItemStack par1ItemStack) {
		return par1ItemStack.getItemDamage() == 0 ? "item.basiccpu" : "item.advancedcpu";
	}
}
