package anzac.peripherals.items;

import java.util.List;

import net.minecraft.client.renderer.texture.IconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.Icon;
import net.minecraft.world.World;
import anzac.peripherals.AnzacPeripheralsCore;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import dan200.computer.api.ComputerCraftAPI;
import dan200.computer.api.IMedia;
import dan200.computer.api.IMount;

public class HDDItem extends Item implements IMedia {

	@SideOnly(Side.CLIENT)
	private Icon icon;

	public HDDItem(final int id) {
		super(id);
		setCreativeTab(CreativeTabs.tabRedstone);
		setMaxStackSize(1);
		setUnlocalizedName("hdd");
	}

	public int getDiskID(final ItemStack stack) {
		final NBTTagCompound tag = stack.getTagCompound();
		if (tag == null || !tag.hasKey("diskid")) {
			return -1;
		}
		return tag.getInteger("diskid");
	}

	protected void setDiskID(final ItemStack stack, final int id) {
		if (!stack.hasTagCompound()) {
			stack.setTagCompound(new NBTTagCompound());
		}
		final NBTTagCompound tag = stack.getTagCompound();
		tag.setInteger("diskid", id);
	}

	@Override
	public IMount createDataMount(final ItemStack stack, final World world) {
		int diskID = getDiskID(stack);
		if (diskID < 0) {
			diskID = ComputerCraftAPI.createUniqueNumberedSaveDir(world, "anzac/hdd");
			setDiskID(stack, diskID);
		}
		return ComputerCraftAPI.createSaveDirMount(world, "anzac/hdd/" + diskID, AnzacPeripheralsCore.storageSize);
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

	@Override
	public boolean setLabel(final ItemStack stack, final String label) {
		if (!stack.hasTagCompound()) {
			stack.setTagCompound(new NBTTagCompound());
		}
		final NBTTagCompound tag = stack.getTagCompound();
		tag.setString("label", label);
		return true;
	}

	@Override
	public String getLabel(final ItemStack stack) {
		final NBTTagCompound tag = stack.getTagCompound();
		if (tag == null || !tag.hasKey("label")) {
			return null;
		}
		return tag.getString("label");
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public void addInformation(final ItemStack stack, final EntityPlayer player, final List list, final boolean par4) {
		super.addInformation(stack, player, list, par4);
		if (stack.hasTagCompound()) {
			final NBTTagCompound tag = stack.getTagCompound();
			if (tag.hasKey("diskid")) {
				final int diskid = tag.getInteger("diskid");
				list.add(EnumChatFormatting.GRAY + "ID: " + diskid);
			}
			if (tag.hasKey("label")) {
				final String label = tag.getString("label");
				list.add(EnumChatFormatting.GRAY + "Label: " + label);
			}
		}
	}

	@Override
	public String getAudioTitle(final ItemStack stack) {
		return null;
	}

	@Override
	public String getAudioRecordName(final ItemStack stack) {
		return null;
	}
}
