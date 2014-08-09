package anzac.peripherals.supplier;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.event.ForgeSubscribe;
import net.minecraftforge.event.world.ChunkDataEvent;
import net.minecraftforge.event.world.WorldEvent.Load;
import net.minecraftforge.event.world.WorldEvent.Save;
import net.minecraftforge.event.world.WorldEvent.Unload;

import org.apache.commons.lang3.ObjectUtils;

import anzac.peripherals.tiles.TeleporterTileEntity.Target;
import anzac.peripherals.utils.Position;

public class SupplierManager {
	public static class SupplierManagerSaveHandler {
		@ForgeSubscribe
		public void onWorldLoad(final Load event) {
			final SupplierManager manager = instance(event.world.isRemote);
			if (event.world.isRemote) {
				reloadManager(true, null);
			} else if (manager == null) {
				reloadManager(false, event.world);
			}
		}

		@ForgeSubscribe
		public void onWorldSave(final Save event) {
			if (!event.world.isRemote && instance(false) != null) {
				instance(false).save(true);
			}
		}

		@ForgeSubscribe
		public void onChunkDataLoad(final ChunkDataEvent.Load event) {
			if (serverManager == null) {
				reloadManager(false, event.world);
			}
		}

		@ForgeSubscribe
		public void onWorldUnload(final Unload event) {
			if (!event.world.isRemote && !MinecraftServer.getServer().isServerRunning()) {
				serverManager = null;
			}
		}
		//
		// @ForgeSubscribe
		// public void onPlayerLogin(PlayerLoggedInEvent event) {
		// instance(false).sendClientInfo(event.player);
		// }
		//
		// @ForgeSubscribe
		// public void onPlayerChangedDimension(PlayerLoggedOutEvent event) {
		// instance(false).sendClientInfo(event.player);
		// }
	}

	private static SupplierManager serverManager;
	private static SupplierManager clientManager;

	private final Map<UUID, UUID> targets;
	private final Map<UUID, SupplierStorage> targetStorage;

	public final boolean client;
	private File saveDir;
	private File saveFile;
	private final List<SupplierStorage> dirtyStorage;
	private NBTTagCompound saveTag;
	private static HashMap<SupplierStorageType, SupplierStorageFactory> factories = new HashMap<SupplierStorageType, SupplierStorageFactory>();

	public SupplierManager(final boolean client, final World world) {
		this.client = client;
		dirtyStorage = Collections.synchronizedList(new LinkedList<SupplierStorage>());
		targets = Collections.synchronizedMap(new HashMap<UUID, UUID>());
		targetStorage = Collections.synchronizedMap(new HashMap<UUID, SupplierStorage>());

		if (!client) {
			load(world);
		}
	}

	private void load(final World world) {
		saveDir = new File(DimensionManager.getCurrentSaveRootDirectory(), "SupplierManager");
		try {
			if (!saveDir.exists()) {
				saveDir.mkdirs();
			}
			saveFile = new File(saveDir, "data1.dat");
			if (saveFile.exists()) {
				final DataInputStream din = new DataInputStream(new FileInputStream(saveFile));
				saveTag = CompressedStreamTools.readCompressed(din);
				din.close();
			} else {
				saveTag = new NBTTagCompound();
			}
		} catch (final Exception e) {
			throw new RuntimeException(e);
		}
	}

	private void save(final boolean force) {
		if (!dirtyStorage.isEmpty() || force) {
			for (final SupplierStorage inv : dirtyStorage) {
				final NBTTagCompound tag = new NBTTagCompound();
				inv.writeToNBT(tag);
				final String key = inv.getOwner().toString();
				saveTag.setTag(key, tag);
				// inv.setClean();
			}
			dirtyStorage.clear();
			try {
				if (!saveFile.exists()) {
					saveFile.createNewFile();
				}
				final DataOutputStream dout = new DataOutputStream(new FileOutputStream(saveFile));
				CompressedStreamTools.writeCompressed(saveTag, dout);
				dout.close();
			} catch (final Exception e) {
				throw new RuntimeException(e);
			}
		}
	}

	public static void reloadManager(final boolean client, final World world) {
		final SupplierManager newManager = new SupplierManager(client, world);
		if (client) {
			clientManager = newManager;
		} else {
			serverManager = newManager;
		}
	}

	public File getSaveDir() {
		return saveDir;
	}

	public static SupplierManager instance(final boolean client) {
		return client ? clientManager : serverManager;
	}

	public static void registerStorageFactory(final SupplierStorageFactory plugin) {
		factories.put(plugin.getType(), plugin);
	}

	public void requestSave(final SupplierStorage storage) {
		dirtyStorage.add(storage);
	}

	public SupplierStorage getStorage(final SupplierTarget target, SupplierStorageType type) {
		final UUID srcId = target.getId();
		final UUID destId = targets.get(srcId);
		if (destId == null) {
			return null;
		}
		final UUID destSrcId = targets.get(destId);
		if (destSrcId == null || !destSrcId.equals(srcId)) {
			return null;
		}
		SupplierStorage storage;
		if (!targetStorage.containsKey(srcId) && !targetStorage.containsKey(destId)) {
			storage = factories.get(type).create(this, srcId, destId);
			final String key = srcId.toString();
			if (!client && saveTag.hasKey(key)) {
				storage.readFromNBT(saveTag.getCompoundTag(key));
			} else {
				dirtyStorage.add(storage);
			}
			targetStorage.put(srcId, storage);
			targetStorage.put(destId, storage);
		} else if (targetStorage.get(srcId) != null
				&& (!targetStorage.containsKey(destId) || targetStorage.get(destId) == null)) {
			storage = targetStorage.get(srcId);
			targetStorage.put(destId, storage);
		} else if (targetStorage.get(destId) != null
				&& (!targetStorage.containsKey(srcId) || targetStorage.get(srcId) == null)) {
			storage = targetStorage.get(destId);
			targetStorage.put(srcId, storage);
		}
		return targetStorage.get(srcId);
	}

	public void registerTarget(final SupplierTarget target) {
		final UUID id = target.getId();
		if (!targets.containsKey(id)) {
			targets.put(id, null);
		}
	}

	public void linkTarget(final SupplierTarget src, final SupplierTarget dest) {
		final UUID srcId = src.getId();
		final UUID destId = dest.getId();
		if (!targets.containsKey(srcId)) {
			registerTarget(src);
		}
		if (!targets.containsKey(destId)) {
			registerTarget(dest);
		}
		if (!ObjectUtils.equals(destId, targets.get(srcId))) {
			targets.put(srcId, destId);
		}
	}

	public void removeLink(final SupplierTarget src) {
		final UUID srcId = src.getId();
		if (!targets.containsKey(srcId)) {
			registerTarget(src);
		}
		final UUID destId = targets.get(srcId);
		targetStorage.remove(srcId);
		if (destId != null) {
			targets.put(srcId, null);
			targetStorage.remove(destId);
		}
	}

	public void linkTarget(final SupplierTarget src, final Target dest) {
		linkTarget(src, getTargetSupplier(dest));
	}

	private SupplierTarget getTargetSupplier(final Target target) {
		final Position pos = target.position;
		final World destWorld = MinecraftServer.getServer().worldServerForDimension(target.dimension);
		final TileEntity te = destWorld.getBlockTileEntity(pos.x, pos.y, pos.z);
		if (te instanceof SupplierTarget) {
			return (SupplierTarget) te;
		}
		return null;
	}
}
