package anzac.peripherals.items;

import java.util.List;

import net.minecraft.client.renderer.texture.IconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.packet.Packet;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ChatMessageComponent;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.Icon;
import net.minecraft.world.World;
import anzac.peripherals.AnzacPeripheralsCore;
import anzac.peripherals.network.PacketHandler;
import anzac.peripherals.tiles.TeleporterTileEntity;
import cpw.mods.fml.common.network.PacketDispatcher;
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
	public boolean shouldPassSneakingClickToBlock(final World par2World, final int par4, final int par5, final int par6) {
		return true;
	}

	@Override
	public boolean onItemUseFirst(final ItemStack stack, final EntityPlayer player, final World world, final int x,
			final int y, final int z, final int side, final float hitX, final float hitY, final float hitZ) {
		if (stack.getItemDamage() != 4) {
			return super.onItemUseFirst(stack, player, world, x, y, z, side, hitX, hitY, hitZ);
		}
		final TileEntity entity = world.getBlockTileEntity(x, y, z);
		return handleCard(stack, entity, player);
	}

	private boolean handleCard(final ItemStack current, final TileEntity tileEntity, final EntityPlayer player) {
		final ItemStack stack = current.copy();
		AnzacPeripheralsCore.logger.info("isRemote: " + tileEntity.worldObj.isRemote);
		if (tileEntity == null || !(tileEntity instanceof TeleporterTileEntity)) {
			if (stack.hasTagCompound() && player.isSneaking()) {
				stack.setTagCompound(null);
				player.sendChatToPlayer(ChatMessageComponent.createFromText("clearing stored coordinates"));
				player.inventory.setInventorySlotContents(player.inventory.currentItem, stack);
			}
			return false;
		}
		final TeleporterTileEntity entity = (TeleporterTileEntity) tileEntity;
		// is card
		NBTTagCompound tagCompound;
		if (player.isSneaking()) {
			if (!stack.hasTagCompound()) {
				stack.setTagCompound(new NBTTagCompound());
			}
			tagCompound = stack.getTagCompound();
			player.sendChatToPlayer(ChatMessageComponent.createFromText("storing; x:" + entity.xCoord + ",y:"
					+ entity.yCoord + ",z:" + entity.zCoord));
			tagCompound.setInteger("linkx", entity.xCoord);
			tagCompound.setInteger("linky", entity.yCoord);
			tagCompound.setInteger("linkz", entity.zCoord);
			tagCompound.setInteger("linkd", entity.worldObj.provider.dimensionId);
			final Packet packet = PacketHandler.createTileEntityPacket("anzac", PacketHandler.ID_TILE_ENTITY,
					tileEntity);
			PacketDispatcher.sendPacketToServer(packet);
			player.inventory.setInventorySlotContents(player.inventory.currentItem, stack);
			return true;
		} else {
			if (stack.hasTagCompound()) {
				tagCompound = stack.getTagCompound();
				final int x = tagCompound.getInteger("linkx");
				final int y = tagCompound.getInteger("linky");
				final int z = tagCompound.getInteger("linkz");
				final int d = tagCompound.getInteger("linkd");
				entity.addRemoveTarget(x, y, z, d, player);
				final Packet packet = PacketHandler.createTileEntityPacket("anzac", PacketHandler.ID_TILE_ENTITY,
						tileEntity);
				PacketDispatcher.sendPacketToServer(packet);
				return true;
			}
		}
		return false;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public void addInformation(final ItemStack stack, final EntityPlayer player, final List list, final boolean par4) {
		super.addInformation(stack, player, list, par4);
		if (stack.hasTagCompound()) {
			final NBTTagCompound tag = stack.getTagCompound();
			final int x = tag.getInteger("linkx");
			final int y = tag.getInteger("linky");
			final int z = tag.getInteger("linkz");
			final int d = tag.getInteger("linkd");
			list.add("Destination:");
			list.add(EnumChatFormatting.GRAY + "X: " + x);
			list.add(EnumChatFormatting.GRAY + "Y: " + y);
			list.add(EnumChatFormatting.GRAY + "Z: " + z);
			list.add(EnumChatFormatting.GRAY + "D: " + d);
		}
	}
}
