package anzac.peripherals.tiles;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ChatMessageComponent;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeDirection;
import anzac.peripherals.AnzacPeripheralsCore;
import anzac.peripherals.annotations.Peripheral;
import anzac.peripherals.annotations.PeripheralMethod;
import anzac.peripherals.utils.ClassUtils;
import anzac.peripherals.utils.Position;
import buildcraft.api.power.IPowerReceptor;
import buildcraft.api.power.PowerHandler;
import buildcraft.api.power.PowerHandler.PowerReceiver;
import buildcraft.api.power.PowerHandler.Type;
import cofh.api.energy.IEnergyHandler;
import dan200.computercraft.api.peripheral.IPeripheral;
import dan200.computercraft.api.turtle.ITurtleAccess;

/**
 * @author Tony
 * 
 */
@Peripheral(type = "Teleporter")
public class TeleporterTileEntity extends BasePeripheralTileEntity implements IPowerReceptor, IEnergyHandler {

	public static class Target {
		private int dimension;
		private Position position;

		public void readFromNBT(final NBTTagCompound tag) {
			final int x = tag.getInteger("x");
			final int y = tag.getInteger("y");
			final int z = tag.getInteger("z");
			position = new Position(x, y, z);
			dimension = tag.getInteger("d");
		}

		public void writeToNBT(final NBTTagCompound tag) {
			tag.setInteger("x", position.x);
			tag.setInteger("y", position.y);
			tag.setInteger("z", position.z);
			tag.setInteger("d", dimension);
		}

		public int getDimension() {
			return dimension;
		}

		public Position getPosition() {
			return position;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + dimension;
			result = prime * result + ((position == null) ? 0 : position.hashCode());
			return result;
		}

		@Override
		public boolean equals(final Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			final Target other = (Target) obj;
			if (dimension != other.dimension)
				return false;
			if (position == null) {
				if (other.position != null)
					return false;
			} else if (!position.equals(other.position))
				return false;
			return true;
		}
	}

	private static final int MJ = AnzacPeripheralsCore.mjMultiplier;
	private static final int RF_TO_MJ = 10;

	private final PowerHandler handler = new PowerHandler(this, Type.MACHINE);
	private int type;
	private final List<Target> targets = new ArrayList<Target>();

	public TeleporterTileEntity() {
	}

	public TeleporterTileEntity(final int metadata) {
		this();
		type = metadata;
		configure();
	}

	@Override
	protected List<String> methodNames() {
		return ClassUtils.getMethodNames(TeleporterTileEntity.class);
	}

	private int maxTargets() {
		switch (type) {
		case 1:
			return 1;
		case 2:
			return 2;
		case 3:
			return 4;
		}
		return 0;
	}

	private void configure() {
		final double maxStorage = 500 * Math.pow(10, type);
		final double maxIn = Math.max(Math.pow(2, type + 6), maxStorage * 0.01);
		handler.configure(1f, (float) maxIn, MJ, (float) maxStorage);
		handler.configurePowerPerdition(0, 0);
	}

	/**
	 * @return
	 */
	@PeripheralMethod
	public float getStoredEnergy() {
		return handler.getEnergyStored() / MJ;
	}

	public void setStoredEnergy(final float stored) {
		handler.setEnergy(stored * MJ);
	}

	/**
	 * @return
	 */
	@PeripheralMethod
	public float getMaxEnergy() {
		return handler.getMaxEnergyStored() / MJ;
	}

	/**
	 * @return
	 */
	@PeripheralMethod
	public Map<Integer, Map<String, Integer>> getTargets() {
		// AnzacPeripheralsCore.logger.info("targets: " + targets + "isRemote: " + worldObj.isRemote);
		final Map<Integer, Map<String, Integer>> table = new HashMap<Integer, Map<String, Integer>>();
		final int index = 0;
		for (final Target target : targets) {
			final Map<String, Integer> map = new HashMap<String, Integer>();
			map.put("x", target.position.x);
			map.put("y", target.position.y);
			map.put("z", target.position.z);
			map.put("dimension", target.dimension);
			table.put(index, map);
		}
		return table;
	}

	@Override
	public PowerReceiver getPowerReceiver(final ForgeDirection side) {
		return handler.getPowerReceiver();
	}

	@Override
	public void doWork(final PowerHandler workProvider) {
	}

	@Override
	public World getWorld() {
		return this.worldObj;
	}

	private double requiredPower(final int x, final int y, final int z, final int d) {
		final double samed = Math.abs(worldObj.provider.dimensionId - d) + 1;
		final double dist = Math.sqrt(getDistanceFrom(x, y, z));
		return dist * samed * MJ;
	}

	/**
	 * @param index
	 * @throws Exception
	 */
	@PeripheralMethod
	public void teleport(final int index) throws Exception {
		// AnzacPeripheralsCore.logger.info("targets: " + targets + "isRemote: " + worldObj.isRemote);
		final Target target = targets.get(index);
		ITurtleAccess turtle = null;
		final Position position = new Position(target.position);
		for (final ForgeDirection direction : ForgeDirection.values()) {
			final Position pos = new Position(xCoord, yCoord, zCoord, direction);
			pos.moveForwards(1);
			final TileEntity entity = worldObj.getBlockTileEntity(pos.x, pos.y, pos.z);
			if (ClassUtils.instanceOf(entity, "dan200.computercraft.shared.turtle.blocks.ITurtleTile")) {
				// AnzacPeripheralsCore.logger.info("found turtle");
				turtle = ClassUtils.callMethod(entity, "getAccess", null);
				AnzacPeripheralsCore.logger.info("found turtle: " + turtle);
				position.orientation = direction.getOpposite();
				break;
			}
		}
		if (turtle == null) {
			throw new Exception("No turtles found");
		}
		// check destination
		final World destWorld = MinecraftServer.getServer().worldServerForDimension(target.dimension);
		if (destWorld == null) {
			throw new Exception("Destination world does not exist");
		}
		final Position pos = target.position;
		final TileEntity entity = destWorld.getBlockTileEntity(pos.x, pos.y, pos.z);
		if (!(entity instanceof TeleporterTileEntity)) {
			throw new Exception("Destination is not a Teleporter");
		}
		position.moveForwards(1);
		if (!canPlaceBlockAt(destWorld, position)) {
			throw new Exception("Destination is blocked");
		}
		final float required = (float) requiredPower(pos.x, pos.y, pos.z, target.dimension);
		final float useEnergy = handler.useEnergy(required, required, false);
		if (useEnergy != required) {
			throw new Exception("Not enough power");
		}
		// AnzacPeripheralsCore.logger.info("teleporting");
		if (turtle.teleportTo(destWorld, position.x, position.y, position.z)) {
			handler.useEnergy(required, required, true);
			onTeleport();
			((TeleporterTileEntity) entity).onTeleport();
		}
	}

	public void onTeleport() {
		worldObj.playSoundEffect(xCoord + 0.5d, yCoord + 0.5d, zCoord + 0.5d, "mob.endermen.portal", 1f, 1f);
	}

	private boolean canPlaceBlockAt(final World par1World, final Position position) {
		final int par2 = position.x;
		final int par3 = position.y;
		final int par4 = position.z;
		final int l = par1World.getBlockId(par2, par3, par4);
		// AnzacPeripheralsCore.logger.info("block at; x:" + par2 + ", y:" + par3 + ", z:" + par4 + ", l:" + l);
		final Block block = Block.blocksList[l];
		return block == null || block.isBlockReplaceable(par1World, par2, par3, par4);
	}

	@Override
	public void readFromNBT(final NBTTagCompound par1nbtTagCompound) {
		super.readFromNBT(par1nbtTagCompound);

		if (par1nbtTagCompound.hasKey("type")) {
			type = par1nbtTagCompound.getInteger("type");
		}
		handler.readFromNBT(par1nbtTagCompound);
		configure();

		if (par1nbtTagCompound.hasKey("targets")) {
			targets.clear();
			final NBTTagList tagList = par1nbtTagCompound.getTagList("targets");
			for (int i = 0; i < tagList.tagCount(); i++) {
				final Target target = new Target();
				target.readFromNBT((NBTTagCompound) tagList.tagAt(i));
				targets.add(target);
			}
		}
	}

	@Override
	public void writeToNBT(final NBTTagCompound par1nbtTagCompound) {
		super.writeToNBT(par1nbtTagCompound);

		handler.writeToNBT(par1nbtTagCompound);
		par1nbtTagCompound.setInteger("type", type);
		if (!targets.isEmpty()) {
			// AnzacPeripheralsCore.logger.info("targets: " + targets + "isRemote: " + worldObj.isRemote);
			final NBTTagList list = new NBTTagList();
			for (final Target target : targets) {
				final NBTTagCompound targetTag = new NBTTagCompound();
				target.writeToNBT(targetTag);
				list.appendTag(targetTag);
			}
			par1nbtTagCompound.setTag("targets", list);
		}
	}

	public void addRemoveTarget(final int x, final int y, final int z, final int d, final EntityPlayer player) {
		final World destWorld = MinecraftServer.getServer().worldServerForDimension(d);
		if (destWorld == null) {
			player.sendChatToPlayer(ChatMessageComponent.createFromText("Destination world does not exist"));
			return;
		}
		final TileEntity entity = destWorld.getBlockTileEntity(x, y, z);
		if (!(entity instanceof TeleporterTileEntity)) {
			player.sendChatToPlayer(ChatMessageComponent.createFromText("Destination is not a Teleporter"));
			return;
		}
		final Target target = new Target();
		target.dimension = d;
		target.position = new Position(x, y, z);
		if (targets.contains(target)) {
			targets.remove(target);
			player.sendChatToPlayer(ChatMessageComponent.createFromText("Removed target; x:" + x + ",y:" + y + ",z:"
					+ z + ", d:" + d));
			return;
		}
		if (targets.size() >= maxTargets()) {
			player.sendChatToPlayer(ChatMessageComponent.createFromText("Maximum targets reached"));
			return;
		}
		final double requiredPower = requiredPower(x, y, z, d);
		// AnzacPeripheralsCore.logger.info("requiredPower:" + requiredPower);
		if (requiredPower <= handler.getMaxEnergyStored()) {
			targets.add(target);
			player.sendChatToPlayer(ChatMessageComponent.createFromText("Added target; x:" + x + ",y:" + y + ",z:" + z
					+ ", d:" + d));
			// AnzacPeripheralsCore.logger.info("targets: " + targets + "isRemote: " + worldObj.isRemote);
			return;
		}
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((targets == null) ? 0 : targets.hashCode());
		result = prime * result + type;
		return result;
	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		final TeleporterTileEntity other = (TeleporterTileEntity) obj;
		if (targets == null) {
			if (other.targets != null)
				return false;
		} else if (!targets.equals(other.targets))
			return false;
		if (type != other.type)
			return false;
		return true;
	}

	@Override
	public boolean equals(final IPeripheral other) {
		return equals((Object) other);
	}

	@Override
	public boolean canInterface(final ForgeDirection arg0) {
		return true;
	}

	@Override
	public int extractEnergy(final ForgeDirection arg0, final int arg1, final boolean arg2) {
		return 0;
	}

	@Override
	public int getEnergyStored(final ForgeDirection arg0) {
		return (int) (handler.getEnergyStored() * RF_TO_MJ);
	}

	@Override
	public int getMaxEnergyStored(final ForgeDirection arg0) {
		return (int) (handler.getMaxEnergyStored() * RF_TO_MJ);
	}

	@Override
	public int receiveEnergy(final ForgeDirection arg0, final int arg1, final boolean arg2) {
		final int quantity = arg1 / RF_TO_MJ;
		if (arg2) {
			if (handler.getEnergyStored() + quantity <= handler.getMaxEnergyStored()) {
				return quantity;
			} else {
				return (int) ((handler.getMaxEnergyStored() - handler.getEnergyStored()) * RF_TO_MJ);
			}
		}
		return (int) (handler.addEnergy(quantity) * RF_TO_MJ);
	}
}
