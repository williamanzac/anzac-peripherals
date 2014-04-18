package anzac.peripherals.items;

import java.util.List;

import net.minecraft.client.renderer.texture.IconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ChatMessageComponent;
import net.minecraft.util.Icon;
import net.minecraft.world.World;
import anzac.peripherals.tiles.TeleporterTileEntity;
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
	@SideOnly(Side.CLIENT)
	private Icon cardIcon;

	public ComponentItem(final int id) {
		super(id);
		setHasSubtypes(true);
		setCreativeTab(CreativeTabs.tabRedstone);
		setUnlocalizedName("anzaccpu");
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public void getSubItems(final int par1, final CreativeTabs par2CreativeTabs, final List par3List) {
		par3List.add(new ItemStack(par1, 1, 0));
		par3List.add(new ItemStack(par1, 1, 1));
		par3List.add(new ItemStack(par1, 1, 2));
		par3List.add(new ItemStack(par1, 1, 3));
		par3List.add(new ItemStack(par1, 1, 4));
	}

	@Override
	public void registerIcons(final IconRegister par1IconRegister) {
		basicIcon = par1IconRegister.registerIcon("anzac:basiccpu");
		advancedIcon = par1IconRegister.registerIcon("anzac:advancedcpu");
		discIcon = par1IconRegister.registerIcon("anzac:disc");
		discsIcon = par1IconRegister.registerIcon("anzac:discs");
		cardIcon = par1IconRegister.registerIcon("anzac:teleport_card");
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
		case 4:
			return cardIcon;
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
			return "item.platter";
		case 3:
			return "item.spindle";
		case 4:
			return "item.card";
		}
		return super.getUnlocalizedName(par1ItemStack);
	}

	@Override
	public boolean shouldPassSneakingClickToBlock(World par2World, int par4, int par5, int par6) {
		return true;
	}

	@Override
	public boolean onItemUseFirst(ItemStack stack, EntityPlayer player, World world, int x, int y, int z, int side,
			float hitX, float hitY, float hitZ) {
		if (stack.getItemDamage() != 4) {
			return super.onItemUseFirst(stack, player, world, x, y, z, side, hitX, hitY, hitZ);
		}
		TileEntity entity = world.getBlockTileEntity(x, y, z);
		return handleCard(stack, entity, player);
	}

	private boolean handleCard(final ItemStack current, final TileEntity tileEntity, final EntityPlayer player) {
		if (tileEntity == null || !(tileEntity instanceof TeleporterTileEntity)) {
			if (current.hasTagCompound() && player.isSneaking()) {
				current.setTagCompound(null);
				player.sendChatToPlayer(ChatMessageComponent.createFromText("clearing stored coordinates"));
			}
			return false;
		}
		final TeleporterTileEntity entity = (TeleporterTileEntity) tileEntity;
		// is card
		NBTTagCompound tagCompound;
		if (player.isSneaking()) {
			if (!current.hasTagCompound()) {
				current.setTagCompound(new NBTTagCompound());
			}
			tagCompound = current.getTagCompound();
			player.sendChatToPlayer(ChatMessageComponent.createFromText("storing; x:" + entity.xCoord + ",y:"
					+ entity.yCoord + ",z:" + entity.zCoord));
			tagCompound.setInteger("linkx", entity.xCoord);
			tagCompound.setInteger("linky", entity.yCoord);
			tagCompound.setInteger("linkz", entity.zCoord);
			tagCompound.setInteger("linkd", entity.worldObj.provider.dimensionId);
			return true;
		} else {
			if (current.hasTagCompound()) {
				tagCompound = current.getTagCompound();
				final int x = tagCompound.getInteger("linkx");
				final int y = tagCompound.getInteger("linky");
				final int z = tagCompound.getInteger("linkz");
				final int d = tagCompound.getInteger("linkd");
				entity.addRemoveTarget(x, y, z, d, player);
				return true;
			}
		}
		return false;
	}

}
