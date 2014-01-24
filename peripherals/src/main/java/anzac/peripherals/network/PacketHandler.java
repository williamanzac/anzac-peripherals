package anzac.peripherals.network;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.INetworkManager;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.Packet250CustomPayload;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import anzac.peripherals.AnzacPeripheralsCore;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.network.IPacketHandler;
import cpw.mods.fml.common.network.Player;

public class PacketHandler implements IPacketHandler {

	public static final int ID_TILE_ENTITY = 2;

	@Override
	public void onPacketData(final INetworkManager manager, final Packet250CustomPayload packet, final Player player) {
		if (packet.data != null && packet.data.length <= 0) {
			return;
		}

		DataInputStream data = new DataInputStream(new ByteArrayInputStream(packet.data));
		try {
			int id = data.readInt();
			if (id == ID_TILE_ENTITY && player instanceof EntityPlayer) {
				handleTileEntityPacket(((EntityPlayer) player).worldObj, false, data);
			} else {
				AnzacPeripheralsCore.logger.warning("PacketHandler.onPacketData: Recieved packet of unknown type: "
						+ id);
			}
		} catch (IOException ex) {
			FMLCommonHandler.instance().raiseException(ex, "PacketHandler.onPacketData", false);
		} finally {
			try {
				data.close();
			} catch (IOException e) {
				AnzacPeripheralsCore.logger.warning("Error closing data input stream: " + e.getMessage());
			}
		}
	}

	public static Packet createTileEntityPacket(final String channel, final int id, final TileEntity te) {
		final ByteArrayOutputStream bos = new ByteArrayOutputStream(140);
		final DataOutputStream dos = new DataOutputStream(bos);
		try {
			dos.writeInt(id);
			dos.writeInt(te.xCoord);
			dos.writeInt(te.yCoord);
			dos.writeInt(te.zCoord);
			final NBTTagCompound root = new NBTTagCompound();
			te.writeToNBT(root);
			writeNBTTagCompound(root, dos);

		} catch (final IOException e) {
			// never thrown
		}

		final Packet250CustomPayload pkt = new Packet250CustomPayload();
		pkt.channel = channel;
		pkt.data = bos.toByteArray();
		pkt.length = bos.size();
		pkt.isChunkDataPacket = true;
		return pkt;
	}

	public static TileEntity handleTileEntityPacket(final World world, final boolean readId, final DataInputStream dis) {
		int x;
		int y;
		int z;
		try {
			if (readId) {
				dis.readInt();
			}
			x = dis.readInt();
			y = dis.readInt();
			z = dis.readInt();
		} catch (final IOException e) {
			FMLCommonHandler.instance().raiseException(e, "PacketUtil.readTileEntityPacket", false);
			return null;
		}
		final NBTTagCompound tags = readNBTTagCompound(dis);

		if (world == null) {
			AnzacPeripheralsCore.logger
					.warning("PacketUtil.handleTileEntityPacket: Null world recieved when processing tile entity packet.");
			return null;
		}
		final TileEntity te = world.getBlockTileEntity(x, y, z);
		if (te == null) {
			AnzacPeripheralsCore.logger
					.warning("PacketUtil.handleTileEntityPacket: TileEntity null when processing tile entity packet.");
			return null;
		}
		te.readFromNBT(tags);
		return te;
	}

	public static byte[] readByteArray(final int length, final DataInputStream dataIn) throws IOException {
		final byte[] barray = new byte[length];
		dataIn.readFully(barray, 0, length);
		return barray;
	}

	public static NBTTagCompound readNBTTagCompound(final DataInputStream dataIn) {
		try {
			final short var2 = dataIn.readShort();
			if (var2 < 0) {
				return null;
			} else {
				final byte[] var3 = readByteArray(var2, dataIn);
				return CompressedStreamTools.decompress(var3);
			}
		} catch (final IOException e) {
			FMLCommonHandler.instance().raiseException(e, "Custom Packet", true);
			return null;
		}
	}

	public static void writeNBTTagCompound(final NBTTagCompound compound, final DataOutputStream dataout) {
		try {
			if (compound == null) {
				dataout.writeShort(-1);
			} else {
				final byte[] var3 = CompressedStreamTools.compress(compound);
				dataout.writeShort((short) var3.length);
				dataout.write(var3);
			}
		} catch (final IOException e) {
			FMLCommonHandler.instance().raiseException(e, "PacketUtil.readTileEntityPacket.writeNBTTagCompound", true);
		}
	}
}
