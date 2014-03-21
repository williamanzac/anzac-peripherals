package anzac.peripherals.items;

import java.util.List;

import net.minecraft.client.renderer.texture.IconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Icon;
import anzac.peripherals.AnzacPeripheralsCore;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class ComponentItem extends Item {

	@SideOnly(Side.CLIENT)
	private Icon basicIcon;
	@SideOnly(Side.CLIENT)
	private Icon advancedIcon;
	@SideOnly(Side.CLIENT)
	private Icon discIcon;
	@SideOnly(Side.CLIENT)
	private Icon discsIcon;

	public ComponentItem(final int id) {
		super(id);
		setHasSubtypes(true);
		setCreativeTab(CreativeTabs.tabRedstone);
		setUnlocalizedName("anzaccpu");
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public void getSubItems(final int par1, final CreativeTabs par2CreativeTabs, final List par3List) {
		par3List.add(new ItemStack(AnzacPeripheralsCore.component, 1, 0));
		par3List.add(new ItemStack(AnzacPeripheralsCore.component, 1, 1));
		par3List.add(new ItemStack(AnzacPeripheralsCore.component, 1, 2));
		par3List.add(new ItemStack(AnzacPeripheralsCore.component, 1, 3));
	}

	@Override
	public void registerIcons(final IconRegister par1IconRegister) {
		basicIcon = par1IconRegister.registerIcon("anzac:basiccpu");
		advancedIcon = par1IconRegister.registerIcon("anzac:advancedcpu");
		discIcon = par1IconRegister.registerIcon("anzac:disc");
		discsIcon = par1IconRegister.registerIcon("anzac:discs");
	}

	@Override
	public Icon getIconFromDamage(final int par1) {
		switch (par1) {
		case 0:
			return basicIcon;
		case 1:
			return advancedIcon;
		case 2:
			return discIcon;
		case 3:
			return discsIcon;
		}
		return super.getIconFromDamage(par1);
	}

	@Override
	public String getUnlocalizedName(final ItemStack par1ItemStack) {
		switch (par1ItemStack.getItemDamage()) {
		case 0:
			return "item.basiccpu";
		case 1:
			return "item.advancedcpu";
		case 2:
			return "item.disc";
		case 3:
			return "item.discs";
		}
		return super.getUnlocalizedName(par1ItemStack);
	}
}
