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
import anzac.peripherals.AnzacPeripheralsCore;
import anzac.peripherals.peripheral.ItemSupplierPeripheral;
import anzac.peripherals.tiles.ItemRouterTileEntity.StackInfo;
import anzac.peripherals.tiles.TeleporterTileEntity.Target;
import anzac.peripherals.utils.Position;
import anzac.peripherals.utils.Utils;
import buildcraft.api.inventory.ISpecialInventory;
import buildcraft.api.power.IPowerReceptor;
import buildcraft.api.power.PowerHandler;
import buildcraft.api.power.PowerHandler.PowerReceiver;
import buildcraft.api.power.PowerHandler.Type;
import cofh.api.energy.IEnergyHandler;

public class ItemSupplierTileEntity extends BaseRouterTileEntity implements IInventory, ISidedInventory,
		ISpecialInventory, IPowerReceptor, IEnergyHandler, TeleporterTarget {

	protected ItemStack itemSlot;
	private static final int MJ = AnzacPeripheralsCore.mjMultiplier;
	private static final int RF_TO_MJ = 10;

	private final PowerHandler handler = new PowerHandler(this, Type.MACHINE);
	private SimpleTargetInventory targetInv;

	private List<Target> multiblock;
	private boolean multiblockDirty = false;

	public ItemSupplierTileEntity() throws Exception {
		super(ItemSupplierPeripheral.class);
		configure();
	}

	private int[] accessibleSlots(final ForgeDirection extractSide, final IInventory inv) {
		final int[] slots;
		if (inv instanceof ISidedInventory) {
			slots = ((ISidedInventory) inv).getAccessibleSlotsFromSide(extractSide.ordinal());
		} else {
			slots = Utils.createSlotArray(0, inv.getSizeInventory());
		}
		return slots;
	}

	public StackInfo[] contents(final ForgeDirection direction, final ForgeDirection dir) throws Exception {
		final TileEntity te;
		if (direction == ForgeDirection.UNKNOWN) {
			te = this;
		} else {
			final Position pos = new Position(xCoord, yCoord, zCoord, direction);
			pos.moveForwards(1);
			te = worldObj.getBlockTileEntity(pos.x, pos.y, pos.z);
			if (te == null || !(te instanceof IInventory)) {
				throw new Exception("Inventory not found");
			}
		}
		final IInventory handler = (IInventory) te;
		final int[] slots = accessibleSlots(dir, handler);
		final Map<Integer, StackInfo> table = new HashMap<Integer, StackInfo>();
		for (final int i : slots) {
			final ItemStack stackInSlot = handler.getStackInSlot(i);
			if (stackInSlot != null) {
				final int uuid = Utils.getUUID(stackInSlot);
				final int amount = stackInSlot.stackSize;
				if (table.containsKey(uuid)) {
					final StackInfo stackInfo = table.get(uuid);
					stackInfo.size += amount;
				} else {
					final StackInfo stackInfo = new StackInfo(uuid, amount);
					table.put(uuid, stackInfo);
				}
			}
		}
		AnzacPeripheralsCore.logger.info("table:" + table);
		return table.values().toArray(new StackInfo[table.size()]);
	}

	public int extractFrom(final ForgeDirection fromDir, final int uuid, final int amount,
			final ForgeDirection extractSide) throws Exception {
		if (getItemSlot() != null) {
			throw new Exception("Internal cache is not empty");
		}
		final Position pos = new Position(xCoord, yCoord, zCoord, fromDir);
		pos.moveForwards(1);
		final TileEntity te = worldObj.getBlockTileEntity(pos.x, pos.y, pos.z);
		if (te == null || !(te instanceof IInventory)) {
			throw new Exception("Inventory not found");
		}
		final IInventory inv = (IInventory) te;
		final ItemStack stackToFind = Utils.getItemStack(uuid);
		final int[] slots = accessibleSlots(ForgeDirection.UNKNOWN, inv);
		for (final int i : slots) {
			final ItemStack stackInSlot = inv.getStackInSlot(i);
			if (Utils.stacksMatch(stackInSlot, stackToFind)) {
				return addItem(stackInSlot, true, ForgeDirection.UNKNOWN);
			}
		}
		return 0;
	}

	public int routeTo(final ForgeDirection toDir, final ForgeDirection insertDir, final int amount) {
		for (int i = 0; i < getSizeInventory(); i++) {
			final ItemStack stackInSlot = getStackInSlot(i);
			if (stackInSlot != null) {
				final ItemStack copy = stackInSlot.copy();
				copy.stackSize = amount;
				final int amount1 = copy.stackSize;
				copy.stackSize -= Utils.routeTo(worldObj, xCoord, yCoord, zCoord, toDir, insertDir, copy);
				final int toDec = amount1 - copy.stackSize;
				if (toDec > 0) {
					decrStackSize(i, toDec);
				}
				return amount - copy.stackSize;
			}
		}
		return 0;
	}

	@Override
	public void readFromNBT(final NBTTagCompound tagCompound) {
		super.readFromNBT(tagCompound);

		// read disk slot
		if (tagCompound.hasKey("item")) {
			final NBTTagCompound tagItem = (NBTTagCompound) tagCompound.getTag("item");
			itemSlot = ItemStack.loadItemStackFromNBT(tagItem);
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

		// write item slot
		if (itemSlot != null) {
			final NBTTagCompound tagItem = new NBTTagCompound();
			itemSlot.writeToNBT(tagItem);
			tagCompound.setTag("item", tagItem);
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
	public int getSizeInventory() {
		return targetInv.getSizeInventory() + 1;
	}

	@Override
	public ItemStack getStackInSlot(final int i) {
		return i == 0 ? getItemSlot() : targetInv.getStackInSlot(i - 1);
	}

	@Override
	public void setInventorySlotContents(final int slot, final ItemStack stack) {
		if (slot == 0) {
			setItemSlot(stack);
			if (stack != null) {
				if (stack.stackSize > getInventoryStackLimit()) {
					stack.stackSize = getInventoryStackLimit();
				}
				queueEvent(PeripheralEvent.item_route, Utils.getUUID(stack), stack.stackSize);
			}
		} else {
			targetInv.setInventorySlotContents(slot - 1, stack);
		}
		onInventoryChanged();
	}

	@Override
	public ItemStack decrStackSize(final int slot, final int amt) {
		ItemStack stack = getStackInSlot(slot);
		if (stack != null) {
			if (stack.stackSize <= amt) {
				setInventorySlotContents(slot, null);
			} else {
				stack = stack.splitStack(amt);
				if (stack.stackSize == 0) {
					setInventorySlotContents(slot, null);
				}
			}
		}
		return stack;
	}

	@Override
	public ItemStack getStackInSlotOnClosing(final int slot) {
		final ItemStack stack = getStackInSlot(slot);
		if (stack != null) {
			setInventorySlotContents(slot, null);
		}
		return stack;
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
		return 64;
	}

	@Override
	public boolean isUseableByPlayer(final EntityPlayer entityplayer) {
		return isConnected() && worldObj.getBlockTileEntity(xCoord, yCoord, zCoord) == this
				&& entityplayer.getDistanceSq(xCoord + 0.5D, yCoord + 0.5D, zCoord + 0.5D) <= 64.0D;
	}

	@Override
	public void openChest() {
	}

	@Override
	public void closeChest() {
	}

	@Override
	public boolean isItemValidForSlot(final int i, final ItemStack itemstack) {
		return i == 0 ? isConnected() : targetInv.isItemValidForSlot(i - 1, itemstack);
	}

	@Override
	public int[] getAccessibleSlotsFromSide(final int var1) {
		return new int[] { 0 };
	}

	@Override
	public boolean canInsertItem(final int i, final ItemStack itemstack, final int j) {
		return isItemValidForSlot(i, itemstack);
	}

	@Override
	public boolean canExtractItem(final int i, final ItemStack itemstack, final int j) {
		// cannot extract
		return false;
	}

	public int sendTo(final String label, final int amount) throws Exception {
		if (amount <= 0) {
			throw new Exception("Amount must be greater than 0");
		}
		final BasePeripheralTileEntity entity = AnzacPeripheralsCore.peripheralLabels.get(label);
		if (entity == null) {
			throw new Exception("No entity found with label " + label);
		}
		if (!(entity instanceof IInventory)) {
			throw new Exception("Invalid target for label " + label);
		}
		for (int i = 0; i < getSizeInventory(); i++) {
			final ItemStack stackInSlot = getStackInSlot(i);
			if (stackInSlot != null) {
				final ItemStack copy = stackInSlot.copy();
				if (amount < copy.stackSize) {
					copy.stackSize = amount;
				}
				final int size = copy.stackSize;
				final int amount1 = copy.stackSize;
				copy.stackSize -= Utils.addToInventory(ForgeDirection.UNKNOWN, copy, (IInventory) entity);
				final int toDec = amount1 - copy.stackSize;
				if (toDec > 0) {
					decrStackSize(0, toDec);
				}
				return size - copy.stackSize;
			}
		}
		return 0;
	}

	public int requestFrom(final String label, final int uuid, final int amount) throws Exception {
		if (amount <= 0) {
			throw new Exception("Amount must be greater than 0");
		}
		final BasePeripheralTileEntity entity = AnzacPeripheralsCore.peripheralLabels.get(label);
		if (entity == null) {
			throw new Exception("No entity found with label " + label);
		}
		if (!(entity instanceof IInventory)) {
			throw new Exception("Invalid target for label " + label);
		}
		final IInventory inv = (IInventory) entity;
		final ItemStack stackToFind = Utils.getItemStack(uuid);
		final int[] slots = accessibleSlots(ForgeDirection.UNKNOWN, inv);
		for (final int i : slots) {
			final ItemStack stackInSlot = inv.getStackInSlot(i);
			if (Utils.stacksMatch(stackInSlot, stackToFind)) {
				return addItem(stackInSlot, true, ForgeDirection.UNKNOWN);
			}
		}
		return 0;
	}

	@Override
	public int addItem(final ItemStack stack, final boolean doAdd, final ForgeDirection from) {
		return Utils.addItem(this, stack, doAdd, from);
	}

	@Override
	public ItemStack[] extractItem(final boolean doRemove, final ForgeDirection from, final int maxItemCount) {
		// cannot extract
		return null;
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

	private ItemStack getItemSlot() {
		return getController().itemSlot;
	}

	private void setItemSlot(final ItemStack item) {
		getController().itemSlot = item;
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
		if (isContoller()) {
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
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((itemSlot == null) ? 0 : itemSlot.hashCode());
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
		final ItemSupplierTileEntity other = (ItemSupplierTileEntity) obj;
		if (itemSlot == null) {
			if (other.itemSlot != null)
				return false;
		} else if (!itemSlot.equals(other.itemSlot))
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
			final ItemSupplierTileEntity storage = getStorage(bc);
			if (storage != null) {
				storage.setMultiblock(null);
			}
		}
		multiblock = null;
	}

	private void formMultiblock() {
		final List<ItemSupplierTileEntity> blocks = new ArrayList<ItemSupplierTileEntity>();
		blocks.add(this);
		Target target = getTarget();
		if (target != null) {
			final ItemSupplierTileEntity cb1 = getStorage(target);
			if (cb1 != null && !blocks.contains(cb1)) {
				blocks.add(cb1);
			}
		}

		if (blocks.size() < 2) {
			return;
		}
		for (final ItemSupplierTileEntity cb : blocks) {
			cb.clearCurrentMultiblock();
		}
		final List<Target> mb = new ArrayList<Target>(blocks.size());
		for (final ItemSupplierTileEntity cb : blocks) {
			target = new Target();
			target.position = new Position(cb);
			target.dimension = cb.worldObj.provider.dimensionId;
			mb.add(target);
		}

		for (final ItemSupplierTileEntity cb : blocks) {
			cb.setMultiblock(mb);
		}
	}

	private void setMultiblock(final List<Target> mb) {
		multiblock = mb;
		if (multiblock != null && isMaster()) {
			for (final Target bc : multiblock) {
				final ItemSupplierTileEntity storage = getStorage(bc);
				if (storage != null) {
					storage.multiblockDirty = true;
				}
			}
		}

		// Forces an update
		worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
		// worldObj.setBlockMetadataWithNotify(xCoord, yCoord, zCoord, isMultiblock() ? 1 : 0, 2);
	}

	public ItemSupplierTileEntity getController() {
		if (multiblock == null || multiblock.isEmpty()) {
			return this;
		}
		final Target position = multiblock.get(0);
		final ItemSupplierTileEntity res = getStorage(position);
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
		final ItemSupplierTileEntity res = getStorage(target);
		if (res == null || res.multiblock == null || res.multiblock.isEmpty() || res.getTarget() == null) {
			return false;
		}
		final ItemSupplierTileEntity storage = getStorage(res.getTarget());
		if (storage == null || !storage.equals(this)) {
			return false;
		}
		return true;
	}

	public ItemSupplierTileEntity getStorage(final Target bc) {
		return getStorage(bc.position.x, bc.position.y, bc.position.z, bc.dimension);
	}

	private ItemSupplierTileEntity getStorage(final int x, final int y, final int z, final int dim) {
		final World destWorld = MinecraftServer.getServer().worldServerForDimension(dim);
		final TileEntity te = destWorld.getBlockTileEntity(x, y, z);
		if (te instanceof ItemSupplierTileEntity) {
			return (ItemSupplierTileEntity) te;
		}
		return null;
	}

	public List<Target> getMultiblock() {
		return multiblock;
	}
}
