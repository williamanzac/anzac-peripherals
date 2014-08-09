package anzac.peripherals.tiles;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeDirection;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidContainerRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTankInfo;
import net.minecraftforge.fluids.IFluidHandler;

import org.apache.commons.lang3.StringUtils;

import anzac.peripherals.AnzacPeripheralsCore;
import anzac.peripherals.peripheral.FluidSupplierPeripheral;
import anzac.peripherals.supplier.FulidSupplierFactory.FluidSupplierStorage;
import anzac.peripherals.supplier.SupplierManager;
import anzac.peripherals.supplier.SupplierStorageType;
import anzac.peripherals.supplier.SupplierTarget;
import anzac.peripherals.tiles.FluidRouterTileEntity.TankInfo;
import anzac.peripherals.tiles.TeleporterTileEntity.Target;
import anzac.peripherals.utils.Position;
import anzac.peripherals.utils.Utils;
import buildcraft.api.inventory.ISpecialInventory;
import buildcraft.api.power.IPowerReceptor;
import buildcraft.api.power.PowerHandler;
import buildcraft.api.power.PowerHandler.PowerReceiver;
import cofh.api.energy.IEnergyHandler;

public class FluidSupplierTileEntity extends BaseRouterTileEntity implements IFluidHandler, IPowerReceptor,
		IEnergyHandler, IInventory, ISpecialInventory, ISidedInventory, SupplierTarget {
	private static final int MJ = AnzacPeripheralsCore.mjMultiplier;

	private final SimpleTargetInventory targetInv = new SimpleTargetInventory(1, this);
	private UUID id = UUID.randomUUID();

	public FluidSupplierTileEntity() throws Exception {
		super(FluidSupplierPeripheral.class);
		targetInv.addListner(new InventoryListener() {
			@Override
			public void inventoryChanged() {
				final Target target = getTarget();
				if (worldObj != null) {
					final SupplierManager instance = SupplierManager.instance(worldObj.isRemote);
					if (target == null) {
						instance.removeLink(FluidSupplierTileEntity.this);
					} else {
						instance.linkTarget(FluidSupplierTileEntity.this, target);
					}
				}
			}
		});
		// if (worldObj != null) {
		// final SupplierManager instance = SupplierManager.instance(worldObj.isRemote);
		// instance.registerTarget(this);
		// final Target target = getTarget();
		// if (target != null) {
		// instance.linkTarget(this, target);
		// }
		// }
	}

	public int routeTo(final ForgeDirection toDir, final ForgeDirection insetDir, final int amount) throws Exception {
		final FluidStack copy = getFluidStorage().fluidTank.getFluid().copy();
		copy.amount = amount;
		final int amount1 = copy.amount;
		copy.amount -= Utils.addToFluidHandler(worldObj, xCoord, yCoord, zCoord, toDir, insetDir, copy);
		final int toDrain = amount1 - copy.amount;
		if (toDrain > 0) {
			getFluidStorage().fluidTank.drain(toDrain, true);
		}
		return amount - copy.amount;
	}

	@Override
	public void readFromNBT(final NBTTagCompound tagCompound) {
		super.readFromNBT(tagCompound);

		// if (tagCompound.hasKey("storage")) {
		// final NBTTagCompound tag = (NBTTagCompound) tagCompound.getTag("storage");
		// final FluidSupplierStorage fluidStorage = getFluidStorage();
		// if (fluidStorage != null) {
		// fluidStorage.readFromNBT(tag);
		// }
		// }

		if (tagCompound.hasKey("targets")) {
			targetInv.readFromNBT(tagCompound.getCompoundTag("targets"));
		}

		if (StringUtils.isNotBlank(tagCompound.getString("uuid"))) {
			id = UUID.fromString(tagCompound.getString("uuid"));
		}
		if (worldObj != null) {
			final SupplierManager instance = SupplierManager.instance(worldObj.isRemote);
			instance.registerTarget(this);
			final Target target = getTarget();
			if (target != null) {
				instance.linkTarget(this, target);
			}
		}
	}

	@Override
	public void writeToNBT(final NBTTagCompound tagCompound) {
		super.writeToNBT(tagCompound);

		// final FluidSupplierStorage fluidStorage = getFluidStorage();
		// if (fluidStorage != null && fluidStorage.owner.equals(id)) {
		// final NBTTagCompound tag = new NBTTagCompound();
		// fluidStorage.writeToNBT(tag);
		// tagCompound.setTag("storage", tag);
		// }

		final NBTTagCompound targetsTag = new NBTTagCompound();
		targetInv.writeToNBT(targetsTag);
		tagCompound.setTag("targets", targetsTag);

		tagCompound.setString("uuid", id.toString());
	}

	@Override
	public boolean isUseableByPlayer(final EntityPlayer entityplayer) {
		return isConnected() && worldObj.getBlockTileEntity(xCoord, yCoord, zCoord) == this
				&& entityplayer.getDistanceSq(xCoord + 0.5D, yCoord + 0.5D, zCoord + 0.5D) <= 64.0D;
	}

	@Override
	public int fill(final ForgeDirection from, final FluidStack resource, final boolean doFill) {
		final FluidSupplierStorage storage = getFluidStorage();
		if (storage != null) {
			return storage.fill(from, resource, doFill);
		}
		return 0;
	}

	@Override
	public FluidStack drain(final ForgeDirection from, final FluidStack resource, final boolean doDrain) {
		final FluidSupplierStorage storage = getFluidStorage();
		if (storage != null) {
			return storage.drain(from, resource, doDrain);
		}
		return null;
	}

	@Override
	public FluidStack drain(final ForgeDirection from, final int maxDrain, final boolean doDrain) {
		final FluidSupplierStorage storage = getFluidStorage();
		if (storage != null) {
			return storage.drain(from, maxDrain, doDrain);
		}
		return null;
	}

	@Override
	public boolean canFill(final ForgeDirection from, final Fluid fluid) {
		final FluidSupplierStorage storage = getFluidStorage();
		if (storage != null) {
			return isConnected() && storage.canFill(from, fluid);
		}
		return false;
	}

	@Override
	public boolean canDrain(final ForgeDirection from, final Fluid fluid) {
		final FluidSupplierStorage storage = getFluidStorage();
		if (storage != null) {
			return storage.canDrain(from, fluid);
		}
		return false;
	}

	@Override
	public FluidTankInfo[] getTankInfo(final ForgeDirection from) {
		final FluidSupplierStorage storage = getFluidStorage();
		if (storage != null) {
			return storage.getTankInfo(from);
		}
		return new FluidTankInfo[] { new FluidTankInfo(null, FluidContainerRegistry.BUCKET_VOLUME) };
	}

	public TankInfo[] contents(final ForgeDirection direction, final ForgeDirection dir) throws Exception {
		final TileEntity te;
		if (direction == ForgeDirection.UNKNOWN) {
			te = this;
		} else {
			final Position pos = new Position(xCoord, yCoord, zCoord, direction);
			pos.moveForwards(1);
			te = worldObj.getBlockTileEntity(pos.x, pos.y, pos.z);
			if (te == null || !(te instanceof IFluidHandler)) {
				throw new Exception("Fluid Handler not found");
			}
		}
		final IFluidHandler handler = (IFluidHandler) te;
		final FluidTankInfo[] tankInfo = handler.getTankInfo(dir);
		final Map<Integer, TankInfo> table = new HashMap<Integer, TankInfo>();
		for (final FluidTankInfo info : tankInfo) {
			if (info != null) {
				final FluidStack fluid = info.fluid;
				final int uuid = Utils.getUUID(fluid);
				final int amount = fluid == null ? 0 : fluid.amount;
				final int capacity = info.capacity;
				if (table.containsKey(uuid)) {
					final TankInfo map = table.get(uuid);
					map.amount += amount;
					map.capacity += capacity;
				} else {
					final TankInfo map = new TankInfo();
					table.put(uuid, map);
					map.fluidId = uuid;
					map.amount = amount;
					map.capacity = capacity;
				}
			}
		}
		AnzacPeripheralsCore.logger.info("table:" + table);
		return table.values().toArray(new TankInfo[table.size()]);
	}

	public int extractFrom(final ForgeDirection fromDir, final int uuid, final int amount,
			final ForgeDirection extractSide) throws Exception {
		final Position pos = new Position(xCoord, yCoord, zCoord, fromDir);
		pos.moveForwards(1);
		final TileEntity te = worldObj.getBlockTileEntity(pos.x, pos.y, pos.z);
		if (te == null || !(te instanceof IFluidHandler)) {
			throw new Exception("Fluid Handler not found");
		}
		final IFluidHandler handler = (IFluidHandler) te;
		final int canDrain = handler.drain(extractSide, amount, false).amount;
		if (canDrain > 0) {
			final FluidStack fluidStack = handler.drain(extractSide, amount, true);
			fill(fromDir, fluidStack, true);
			return fluidStack.amount;
		}
		return 0;
	}

	public int sendTo(final String label, final int amount) throws Exception {
		if (getFluidStorage().fluidTank.getFluid() == null) {
			throw new Exception("No fluid to transfer");
		}
		if (amount <= 0) {
			throw new Exception("Amount must be greater than 0");
		}
		final BasePeripheralTileEntity entity = AnzacPeripheralsCore.peripheralLabels.get(label);
		if (entity == null) {
			throw new Exception("No entity found with label " + label);
		}
		if (!(entity instanceof IFluidHandler)) {
			throw new Exception("Invalid target for label " + label);
		}
		final IFluidHandler tank = (IFluidHandler) entity;
		final FluidStack copy = getFluidStorage().fluidTank.getFluid().copy();
		if (amount < getFluidStorage().fluidTank.getFluidAmount()) {
			copy.amount = amount;
		}
		final int amount1 = copy.amount;
		copy.amount -= tank.fill(ForgeDirection.UNKNOWN, copy, true);
		final int toDrain = amount1 - copy.amount;
		if (toDrain > 0) {
			getFluidStorage().fluidTank.drain(toDrain, true);
		}
		return amount - copy.amount;
	}

	public int requestFrom(final String label, final int uuid, final int amount) throws Exception {
		if (amount <= 0) {
			throw new Exception("Amount must be greater than 0");
		}
		final BasePeripheralTileEntity entity = AnzacPeripheralsCore.peripheralLabels.get(label);
		if (entity == null) {
			throw new Exception("No entity found with label " + label);
		}
		if (!(entity instanceof IFluidHandler)) {
			throw new Exception("Invalid target for label " + label);
		}
		final IFluidHandler handler = (IFluidHandler) entity;
		final int canDrain = handler.drain(ForgeDirection.UNKNOWN, amount, false).amount;
		if (canDrain > 0) {
			final FluidStack fluidStack = handler.drain(ForgeDirection.UNKNOWN, amount, true);
			fill(ForgeDirection.UNKNOWN, fluidStack, true);
			return fluidStack.amount;
		}
		return 0;
	}

	private FluidSupplierStorage getFluidStorage() {
		if (worldObj != null) {
			return (FluidSupplierStorage) SupplierManager.instance(worldObj.isRemote).getStorage(this,
					SupplierStorageType.FLUID);
		}
		return null;
	}

	public float getStoredEnergy() {
		final FluidSupplierStorage storage = getFluidStorage();
		if (storage != null) {
			return storage.getHandler().getEnergyStored() / MJ;
		}
		return 0;
	}

	public void setStoredEnergy(final float stored) {
		final FluidSupplierStorage storage = getFluidStorage();
		if (storage != null) {
			storage.getHandler().setEnergy(stored * MJ);
		}
	}

	public float getMaxEnergy() {
		final FluidSupplierStorage storage = getFluidStorage();
		if (storage != null) {
			return storage.getHandler().getMaxEnergyStored() / MJ;
		}
		return 0;
	}

	public Target getTarget() {
		// AnzacPeripheralsCore.logger.info("targets: " + targets + "isRemote: " + worldObj.isRemote);
		return targetInv.getTargets()[0];
	}

	@Override
	public PowerReceiver getPowerReceiver(final ForgeDirection side) {
		final FluidSupplierStorage fluidStorage = getFluidStorage();
		if (fluidStorage != null) {
			return fluidStorage.getPowerReceiver(side);
		}
		return null;
	}

	@Override
	public void doWork(final PowerHandler workProvider) {
	}

	@Override
	public World getWorld() {
		return this.worldObj;
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
		final FluidSupplierStorage fluidStorage = getFluidStorage();
		if (fluidStorage != null) {
			return fluidStorage.getEnergyStored(arg0);
		}
		return 0;
	}

	@Override
	public int getMaxEnergyStored(final ForgeDirection arg0) {
		final FluidSupplierStorage fluidStorage = getFluidStorage();
		if (fluidStorage != null) {
			return fluidStorage.getMaxEnergyStored(arg0);
		}
		return 0;
	}

	@Override
	public int receiveEnergy(final ForgeDirection arg0, final int arg1, final boolean arg2) {
		final FluidSupplierStorage fluidStorage = getFluidStorage();
		if (fluidStorage != null) {
			return fluidStorage.receiveEnergy(arg0, arg1, arg2);
		}
		return 0;
	}

	@Override
	public int addItem(final ItemStack stack, final boolean doAdd, final ForgeDirection from) {
		return Utils.addItem(this, stack, doAdd, from);
	}

	@Override
	public ItemStack[] extractItem(final boolean doRemove, final ForgeDirection from, final int maxItemCount) {
		return null;
	}

	@Override
	public int getSizeInventory() {
		return targetInv.getSizeInventory();
	}

	@Override
	public ItemStack getStackInSlot(final int i) {
		return targetInv.getStackInSlot(i);
	}

	@Override
	public ItemStack decrStackSize(final int i, final int j) {
		return targetInv.decrStackSize(i, j);
	}

	@Override
	public ItemStack getStackInSlotOnClosing(final int i) {
		return targetInv.getStackInSlotOnClosing(i);
	}

	@Override
	public void setInventorySlotContents(final int i, final ItemStack itemstack) {
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
	public void openChest() {
		targetInv.openChest();
	}

	@Override
	public void closeChest() {
		targetInv.closeChest();
	}

	@Override
	public boolean isItemValidForSlot(final int i, final ItemStack itemstack) {
		return targetInv.isItemValidForSlot(i, itemstack);
	}

	@Override
	public int[] getAccessibleSlotsFromSide(final int var1) {
		return Utils.createSlotArray(0, targetInv.getSizeInventory());
	}

	@Override
	public boolean canInsertItem(final int i, final ItemStack itemstack, final int j) {
		return targetInv.isItemValidForSlot(i, itemstack);
	}

	@Override
	public boolean canExtractItem(final int i, final ItemStack itemstack, final int j) {
		return false;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((id == null) ? 0 : id.hashCode());
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
		final FluidSupplierTileEntity other = (FluidSupplierTileEntity) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		return true;
	}

	@Override
	public void updateEntity() {
		super.updateEntity();

		if (worldObj.isRemote) {
			return;
		}

		if (worldObj.getTotalWorldTime() % 10 == 0) {
			// final SupplierManager instance = SupplierManager.instance(worldObj);
			// instance.registerTarget(this);
			// final Target target = getTarget();
			// if (target != null) {
			// instance.linkTarget(this, target);
			// }
			// worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
			// onInventoryChanged();
			final FluidSupplierStorage fluidStorage = getFluidStorage();
			if (fluidStorage != null && fluidStorage.getOwner().equals(id)) {
				final Target target = getTarget();
				if (target != null) {
					final Position pos = target.position;
					final float requiredPower = (float) requiredPower(pos.x, pos.y, pos.z, target.dimension);
					fluidStorage.getHandler().useEnergy(requiredPower, requiredPower, true);
				}
			}
		}
	}

	private double requiredPower(final int x, final int y, final int z, final int d) {
		final double samed = Math.abs(worldObj.provider.dimensionId - d) + 1;
		final double dist = Math.sqrt(getDistanceFrom(x, y, z));
		return dist * samed;
	}

	@Override
	public UUID getId() {
		return id;
	}

	public FluidTankInfo getInfo() {
		final FluidSupplierStorage storage = getFluidStorage();
		if (storage != null) {
			return storage.fluidTank.getInfo();
		}
		return null;
	}
}
