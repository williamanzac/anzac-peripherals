package anzac.peripherals.tiles;

import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeDirection;
import anzac.peripherals.AnzacPeripheralsCore;
import anzac.peripherals.peripheral.TeleporterPeripheral;
import anzac.peripherals.utils.ClassUtils;
import anzac.peripherals.utils.Position;
import anzac.peripherals.utils.Utils;
import buildcraft.api.inventory.ISpecialInventory;
import buildcraft.api.power.IPowerReceptor;
import buildcraft.api.power.PowerHandler;
import buildcraft.api.power.PowerHandler.PowerReceiver;
import buildcraft.api.power.PowerHandler.Type;
import cofh.api.energy.IEnergyHandler;
import dan200.computercraft.api.turtle.ITurtleAccess;

/**
 * @author Tony
 * 
 */
public class TeleporterTileEntity extends BasePeripheralTileEntity implements IPowerReceptor, IEnergyHandler,
		IInventory, ISpecialInventory, ISidedInventory {

	public static class Target {
		public int dimension;
		public Position position;

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
	private SimpleTargetInventory targetInv;

	public TeleporterTileEntity() throws Exception {
		super(TeleporterPeripheral.class);
	}

	public TeleporterTileEntity(final int metadata) throws Exception {
		this();
		type = metadata;
		configure();
	}

	private void configure() {
		final double maxStorage = 500 * Math.pow(10, type);
		final double maxIn = Math.max(Math.pow(2, type + 6), maxStorage * 0.01);
		handler.configure(1f, (float) maxIn, MJ, (float) maxStorage);
		handler.configurePowerPerdition(0, 0);
		targetInv = new SimpleTargetInventory(type, this);
	}

	public float getStoredEnergy() {
		return handler.getEnergyStored() / MJ;
	}

	public void setStoredEnergy(final float stored) {
		handler.setEnergy(stored * MJ);
	}

	public float getMaxEnergy() {
		return handler.getMaxEnergyStored() / MJ;
	}

	public Target[] getTargets() {
		// AnzacPeripheralsCore.logger.info("targets: " + targets + "isRemote: " + worldObj.isRemote);
		return targetInv.getTargets();
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

	public void teleport(final int index) throws Exception {
		// AnzacPeripheralsCore.logger.info("targets: " + targets + "isRemote: " + worldObj.isRemote);
		final Target target = getTargets()[index];
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
			targetInv.readFromNBT(par1nbtTagCompound.getCompoundTag("targets"));
		}
	}

	@Override
	public void writeToNBT(final NBTTagCompound par1nbtTagCompound) {
		super.writeToNBT(par1nbtTagCompound);

		handler.writeToNBT(par1nbtTagCompound);
		par1nbtTagCompound.setInteger("type", type);
		// AnzacPeripheralsCore.logger.info("targets: " + targets + "isRemote: " + worldObj.isRemote);
		final NBTTagCompound targetsTag = new NBTTagCompound();
		targetInv.writeToNBT(targetsTag);
		par1nbtTagCompound.setTag("targets", targetsTag);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((targetInv == null) ? 0 : targetInv.hashCode());
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
		if (targetInv == null) {
			if (other.targetInv != null)
				return false;
		} else if (!targetInv.equals(other.targetInv))
			return false;
		if (type != other.type)
			return false;
		return true;
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

	@Override
	public int addItem(ItemStack stack, boolean doAdd, ForgeDirection from) {
		return Utils.addItem(this, stack, doAdd, from);
	}

	@Override
	public ItemStack[] extractItem(boolean doRemove, ForgeDirection from, int maxItemCount) {
		return null;
	}

	@Override
	public int getSizeInventory() {
		return targetInv.getSizeInventory();
	}

	@Override
	public ItemStack getStackInSlot(int i) {
		return targetInv.getStackInSlot(i);
	}

	@Override
	public ItemStack decrStackSize(int i, int j) {
		return targetInv.decrStackSize(i, j);
	}

	@Override
	public ItemStack getStackInSlotOnClosing(int i) {
		return targetInv.getStackInSlotOnClosing(i);
	}

	@Override
	public void setInventorySlotContents(int i, ItemStack itemstack) {
		targetInv.setInventorySlotContents(i, itemstack);
	}

	@Override
	public String getInvName() {
		return getLabel();
	}

	@Override
	public boolean isInvNameLocalized() {
		return hasLabel();
	}

	@Override
	public int getInventoryStackLimit() {
		return targetInv.getInventoryStackLimit();
	}

	@Override
	public boolean isUseableByPlayer(EntityPlayer entityplayer) {
		return isConnected() && worldObj.getBlockTileEntity(xCoord, yCoord, zCoord) == this
				&& entityplayer.getDistanceSq(xCoord + 0.5D, yCoord + 0.5D, zCoord + 0.5D) <= 64.0D;
	}

	@Override
	public void openChest() {
		targetInv.openChest();
	}

	@Override
	public void closeChest() {
		targetInv.closeChest();
	}

	@Override
	public boolean isItemValidForSlot(int i, ItemStack itemstack) {
		return targetInv.isItemValidForSlot(i, itemstack);
	}

	@Override
	public int[] getAccessibleSlotsFromSide(int var1) {
		return Utils.createSlotArray(0, targetInv.getSizeInventory());
	}

	@Override
	public boolean canInsertItem(int i, ItemStack itemstack, int j) {
		return targetInv.isItemValidForSlot(i, itemstack);
	}

	@Override
	public boolean canExtractItem(int i, ItemStack itemstack, int j) {
		return false;
	}
}
