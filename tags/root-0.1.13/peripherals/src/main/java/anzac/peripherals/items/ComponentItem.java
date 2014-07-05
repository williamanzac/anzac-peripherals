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
import anzac.peripherals.annotations.Items;
import anzac.peripherals.network.PacketHandler;
import anzac.peripherals.tiles.TeleporterTileEntity;
import cpw.mods.fml.common.network.PacketDispatcher;

@Items(key = "item.anzac.component", value = { ItemType.BASIC_PROCESSOR, ItemType.ADVANCED_PROCESSOR, ItemType.PLATTER,
		ItemType.SPINDLE, ItemType.TELEPORTER_CARD, ItemType.BASIC_PERIPHERAL_FRAME,
		ItemType.ADVANCED_PERIPHERAL_FRAME, ItemType.TELEPORTER_FRAME })
public class ComponentItem extends Item {

	public ComponentItem(final int id) {
		super(id);
		setHasSubtypes(true);
		setCreativeTab(CreativeTabs.tabRedstone);
	}

	@SuppressWarnings({ "rawtypes" })
	@Override
	public void getSubItems(final int par1, final CreativeTabs par2CreativeTabs, final List par3List) {
		ItemFactory.getSubItems(getClass(), itemID, par3List);
	}

	@Override
	public void registerIcons(final IconRegister par1IconRegister) {
		ItemFactory.registerIcons(getClass(), par1IconRegister);
	}

	@Override
	public Icon getIconFromDamage(final int par1) {
		final Icon icon = ItemFactory.getIcon(getClass(), par1);
		return icon != null ? icon : super.getIconFromDamage(par1);
	}

	@Override
	public String getUnlocalizedName(final ItemStack par1ItemStack) {
		return ItemFactory.getUnlocalizedName(par1ItemStack);
	}

	@Override
	public boolean shouldPassSneakingClickToBlock(final World par2World, final int par4, final int par5, final int par6) {
		return true;
	}

	@Override
	public boolean onItemUseFirst(final ItemStack stack, final EntityPlayer player, final World world, final int x,
			final int y, final int z, final int side, final float hitX, final float hitY, final float hitZ) {
		if (stack.getItemDamage() != ItemType.TELEPORTER_CARD.getMeta()) {
			return super.onItemUseFirst(stack, player, world, x, y, z, side, hitX, hitY, hitZ);
		}
		final TileEntity entity = world.getBlockTileEntity(x, y, z);
		return handleCard(stack, entity, player);
	}

	private boolean handleCard(final ItemStack current, final TileEntity tileEntity, final EntityPlayer player) {
		final ItemStack stack = current.copy();
		// AnzacPeripheralsCore.logger.info("isRemote: " + tileEntity.worldObj.isRemote);
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
