package anzac.peripherals.tiles;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeDirection;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidContainerRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fluids.FluidTankInfo;
import net.minecraftforge.fluids.IFluidHandler;
import anzac.peripherals.AnzacPeripheralsCore;
import anzac.peripherals.peripheral.FluidSupplierPeripheral;
import anzac.peripherals.tiles.FluidRouterTileEntity.TankInfo;
import anzac.peripherals.tiles.TeleporterTileEntity.Target;
import anzac.peripherals.utils.Position;
import anzac.peripherals.utils.Utils;
import buildcraft.api.inventory.ISpecialInventory;
import buildcraft.api.power.IPowerReceptor;
import buildcraft.api.power.PowerHandler;
import buildcraft.api.power.PowerHandler.PowerReceiver;
import buildcraft.api.power.PowerHandler.Type;
import cofh.api.energy.IEnergyHandler;

public class FluidSupplierTileEntity extends BaseRouterTileEntity implements IFluidHandler, IPowerReceptor,
		IEnergyHandler, IInventory, ISpecialInventory, ISidedInventory, TeleporterTarget {

	public FluidTank fluidTank = new FluidTank(FluidContainerRegistry.BUCKET_VOLUME);
	private static final int MJ = AnzacPeripheralsCore.mjMultiplier;
	private static final int RF_TO_MJ = 10;

	private final PowerHandler handler = new PowerHandler(this, Type.MACHINE);
	private SimpleTargetInventory targetInv;

	private List<Target> multiblock;
	private boolean multiblockDirty = false;

	public FluidSupplierTileEntity() throws Exception {
		super(FluidSupplierPeripheral.class);
		configure();
	}

	public int routeTo(final ForgeDirection toDir, final ForgeDirection insetDir, final int amount) throws Exception {
		final FluidStack copy = getTank().getFluid().copy();
		copy.amount = amount;
		final int amount1 = copy.amount;
		copy.amount -= Utils.addToFluidHandler(worldObj, xCoord, yCoord, zCoord, toDir, insetDir, copy);
		final int toDrain = amount1 - copy.amount;
		if (toDrain > 0) {
			getTank().drain(toDrain, true);
		}
		return amount - copy.amount;
	}

	@Override
	public void readFromNBT(final NBTTagCompound tagCompound) {
		super.readFromNBT(tagCompound);

		// read tank
		if (tagCompound.hasKey("fluid")) {
			final NBTTagCompound tagFluid = (NBTTagCompound) tagCompound.getTag("fluid");
			fluidTank.readFromNBT(tagFluid);
		}

		// if (tagCompound.hasKey("type")) {
		// type = tagCompound.getInteger("type");
		// }
		handler.readFromNBT(tagCompound);
		configure();

		if (tagCompound.hasKey("targets")) {
			targetInv.readFromNBT(tagCompound.getCompoundTag("targets"));
		}

		if (tagCompound.getBoolean("isMultiblock")) {
			multiblock = new ArrayList<Target>();
			final NBTTagList tagList = tagCompound.getTagList("multiblock");
			for (int i = 0; i < tagList.tagCount(); i++) {
				final NBTTagCompound tagAt = (NBTTagCompound) tagList.tagAt(i);
				final Target target = new Target();
				target.readFromNBT(tagAt);
				multiblock.add(target);
			}
		} else {
			multiblock = null;
		}
	}

	@Override
	public void writeToNBT(final NBTTagCompound tagCompound) {
		super.writeToNBT(tagCompound);

		// write tank
		if (fluidTank != null) {
			final NBTTagCompound tagFluid = new NBTTagCompound();
			fluidTank.writeToNBT(tagFluid);
			tagCompound.setTag("fluid", tagFluid);
		}

		handler.writeToNBT(tagCompound);
		// tagCompound.setInteger("type", type);
		// AnzacPeripheralsCore.logger.info("targets: " + targets + "isRemote: " + worldObj.isRemote);
		final NBTTagCompound targetsTag = new NBTTagCompound();
		targetInv.writeToNBT(targetsTag);
		tagCompound.setTag("targets", targetsTag);

		tagCompound.setBoolean("isMultiblock", isMultiblock());
		if (isMultiblock()) {
			final NBTTagList list = new NBTTagList();
			for (final Target target : multiblock) {
				if (target != null) {
					final NBTTagCompound targetTag = new NBTTagCompound();
					target.writeToNBT(targetTag);
				}
			}
			tagCompound.setTag("multiblock", list);
		}
	}

	@Override
	public boolean isUseableByPlayer(final EntityPlayer entityplayer) {
		return isConnected() && worldObj.getBlockTileEntity(xCoord, yCoord, zCoord) == this
				&& entityplayer.getDistanceSq(xCoord + 0.5D, yCoord + 0.5D, zCoord + 0.5D) <= 64.0D;
	}

	@Override
	public int fill(final ForgeDirection from, final FluidStack resource, final boolean doFill) {
		if (resource == null) {
			return 0;
		}
		final int fill = getTank().fill(resource, doFill);
		if (fill > 0 && doFill) {
			queueEvent(PeripheralEvent.fluid_route, Utils.getUUID(resource), fill);
		}
		return fill;
	}

	@Override
	public FluidStack drain(final ForgeDirection from, final FluidStack resource, final boolean doDrain) {
		// cannot drain
		return null;
	}

	@Override
	public FluidStack drain(final ForgeDirection from, final int maxDrain, final boolean doDrain) {
		// cannot drain
		return null;
	}

	@Override
	public boolean canFill(final ForgeDirection from, final Fluid fluid) {
		return isConnected();
	}

	@Override
	public boolean canDrain(final ForgeDirection from, final Fluid fluid) {
		return false;
	}

	public FluidTankInfo getInfo() {
		return getTank().getInfo();
	}

	@Override
	public FluidTankInfo[] getTankInfo(final ForgeDirection from) {
		return new FluidTankInfo[] { getTank().getInfo() };
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
		if (getTank().getFluid() == null) {
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
		final FluidStack copy = getTank().getFluid().copy();
		if (amount < getTank().getFluidAmount()) {
			copy.amount = amount;
		}
		final int amount1 = copy.amount;
		copy.amount -= tank.fill(ForgeDirection.UNKNOWN, copy, true);
		final int toDrain = amount1 - copy.amount;
		if (toDrain > 0) {
			getTank().drain(toDrain, true);
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

	private void configure() {
		final double maxStorage = 5000;
		final double maxIn = 500;
		handler.configure(1f, (float) maxIn, MJ, (float) maxStorage);
		handler.configurePowerPerdition(0, 0);
		targetInv = new SimpleTargetInventory(1, this);
		targetInv.addListner(new InventoryListener() {
			@Override
			public void inventoryChanged() {
				multiblockDirty = true;
			}
		});
	}

	private PowerHandler getPowerHandler() {
		return getController().handler;
	}

	private FluidTank getTank() {
		return getController().fluidTank;
	}

	public float getStoredEnergy() {
		return getPowerHandler().getEnergyStored() / MJ;
	}

	public void setStoredEnergy(final float stored) {
		getPowerHandler().setEnergy(stored * MJ);
	}

	public float getMaxEnergy() {
		return getPowerHandler().getMaxEnergyStored() / MJ;
	}

	public Target getTarget() {
		// AnzacPeripheralsCore.logger.info("targets: " + targets + "isRemote: " + worldObj.isRemote);
		return targetInv.getTargets()[0];
	}

	@Override
	public PowerReceiver getPowerReceiver(final ForgeDirection side) {
		return getPowerHandler().getPowerReceiver();
	}

	@Override
	public void doWork(final PowerHandler workProvider) {
		if (isMaster()) {
			final Target target = getTarget();
			if (target != null) {
				final Position pos = target.position;
				final float requiredPower = (float) requiredPower(pos.x, pos.y, pos.z, target.dimension);
				workProvider.useEnergy(requiredPower, requiredPower, true);
			}
		}
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
		return (int) (getPowerHandler().getEnergyStored() * RF_TO_MJ);
	}

	@Override
	public int getMaxEnergyStored(final ForgeDirection arg0) {
		return (int) (getPowerHandler().getMaxEnergyStored() * RF_TO_MJ);
	}

	@Override
	public int receiveEnergy(final ForgeDirection arg0, final int arg1, final boolean arg2) {
		final int quantity = arg1 / RF_TO_MJ;
		if (arg2) {
			if (getPowerHandler().getEnergyStored() + quantity <= getPowerHandler().getMaxEnergyStored()) {
				return quantity;
			} else {
				return (int) ((getPowerHandler().getMaxEnergyStored() - getPowerHandler().getEnergyStored()) * RF_TO_MJ);
			}
		}
		return (int) (getPowerHandler().addEnergy(quantity) * RF_TO_MJ);
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
		result = prime * result + ((fluidTank == null) ? 0 : fluidTank.hashCode());
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
		if (fluidTank == null) {
			if (other.fluidTank != null)
				return false;
		} else if (!fluidTank.equals(other.fluidTank))
			return false;
		return true;
	}

	@Override
	public void updateEntity() {
		super.updateEntity();

		if (multiblockDirty) {
			multiblockDirty = false;
			formMultiblock();
		}

		if (!isMaster()) {
			return;
		}

		if (worldObj.getTotalWorldTime() % 10 == 0) {
			// worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
			// onInventoryChanged();
			doWork(getPowerHandler());
		}
	}

	private void clearCurrentMultiblock() {
		if (multiblock == null || multiblock.isEmpty()) {
			return;
		}
		for (final Target bc : multiblock) {
			final FluidSupplierTileEntity storage = getStorage(bc);
			if (storage != null) {
				storage.setMultiblock(null);
			}
		}
		multiblock = null;
	}

	private void formMultiblock() {
		final List<FluidSupplierTileEntity> blocks = new ArrayList<FluidSupplierTileEntity>();
		blocks.add(this);
		Target target = getTarget();
		if (target != null) {
			final FluidSupplierTileEntity cb1 = getStorage(target);
			if (cb1 != null && !blocks.contains(cb1)) {
				blocks.add(cb1);
			}
		}

		if (blocks.size() < 2) {
			return;
		}
		for (final FluidSupplierTileEntity cb : blocks) {
			cb.clearCurrentMultiblock();
		}
		final List<Target> mb = new ArrayList<Target>(blocks.size());
		for (final FluidSupplierTileEntity cb : blocks) {
			target = new Target();
			target.position = new Position(cb);
			target.dimension = cb.worldObj.provider.dimensionId;
			mb.add(target);
		}

		for (final FluidSupplierTileEntity cb : blocks) {
			cb.setMultiblock(mb);
		}
	}

	private void setMultiblock(final List<Target> mb) {
		multiblock = mb;
		if (multiblock != null && isMaster()) {
			for (final Target bc : multiblock) {
				final FluidSupplierTileEntity storage = getStorage(bc);
				if (storage != null) {
					storage.multiblockDirty = true;
				}
			}
		}

		// Forces an update
		worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
		// worldObj.setBlockMetadataWithNotify(xCoord, yCoord, zCoord, isMultiblock() ? 1 : 0, 2);
	}

	public FluidSupplierTileEntity getController() {
		if (multiblock == null || multiblock.isEmpty()) {
			return this;
		}
		final Target position = multiblock.get(0);
		final FluidSupplierTileEntity res = getStorage(position);
		return res != null ? res : this;
	}

	boolean isContoller() {
		return multiblock == null || multiblock.isEmpty() ? true : isMaster();
	}

	boolean isMaster() {
		if (multiblock != null && !multiblock.isEmpty()) {
			return getStorage(multiblock.get(0)).equals(this);
		}
		return false;
	}

	public boolean isMultiblock() {
		return isCurrentMultiblockValid();
	}

	private boolean isCurrentMultiblockValid() {
		if (multiblock == null || multiblock.isEmpty()) {
			return false;
		}
		final Target target = getTarget();
		if (target == null) {
			return false;
		}
		final FluidSupplierTileEntity res = getStorage(target);
		if (res == null || res.multiblock == null || res.multiblock.isEmpty() || res.getTarget() == null) {
			return false;
		}
		final FluidSupplierTileEntity storage = getStorage(res.getTarget());
		if (storage == null || !storage.equals(this)) {
			return false;
		}
		return true;
	}

	public FluidSupplierTileEntity getStorage(final Target bc) {
		return getStorage(bc.position.x, bc.position.y, bc.position.z, bc.dimension);
	}

	private FluidSupplierTileEntity getStorage(final int x, final int y, final int z, final int dim) {
		final World destWorld = MinecraftServer.getServer().worldServerForDimension(dim);
		final TileEntity te = destWorld.getBlockTileEntity(x, y, z);
		if (te instanceof FluidSupplierTileEntity) {
			return (FluidSupplierTileEntity) te;
		}
		return null;
	}

	public List<Target> getMultiblock() {
		return multiblock;
	}
}
