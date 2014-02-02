package anzac.peripherals.items;

import net.minecraft.client.renderer.texture.IconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Icon;
import net.minecraft.world.World;
import anzac.peripherals.AnzacPeripheralsCore;
import dan200.computer.api.ComputerCraftAPI;
import dan200.computer.api.IMount;

public class HDDItem extends Item {
	private Icon icon;

	public HDDItem(final int id) {
		super(id);
		setCreativeTab(CreativeTabs.tabRedstone);
		setMaxStackSize(1);
		setUnlocalizedName("hdd");
	}

	public int getDiskID(final ItemStack stack) {
		final int damage = stack.getItemDamage();
		if (damage > 0) {
			return damage;
		}
		return -1;
	}

	protected void setDiskID(final ItemStack stack, final int id) {
		if (id > 0) {
			stack.setItemDamage(id);
		} else {
			stack.setItemDamage(0);
		}
	}

	public IMount createDataMount(final ItemStack stack, final World world) {
		int diskID = getDiskID(stack);
		if (diskID < 0) {
			diskID = ComputerCraftAPI.createUniqueNumberedSaveDir(world,
					"anzac/hdd");
			setDiskID(stack, diskID);
		}
		return ComputerCraftAPI.createSaveDirMount(world,
				"anzac/hdd/" + diskID, AnzacPeripheralsCore.storageSize);
	}

	@Override
	public void registerIcons(final IconRegister par1IconRegister) {
		icon = par1IconRegister.registerIcon("anzac:hdd");
	}

	@Override
	public Icon getIconFromDamage(final int par1) {
		return icon;
	}

	@Override
	public String getUnlocalizedName(final ItemStack par1ItemStack) {
		return "item.hdd";
	}
}
