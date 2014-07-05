package anzac.peripherals.tiles;

import java.util.HashMap;
import java.util.Map;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.ForgeDirection;
import anzac.peripherals.AnzacPeripheralsCore;
import anzac.peripherals.peripheral.ItemRouterPeripheral;
import anzac.peripherals.utils.Position;
import anzac.peripherals.utils.Utils;
import buildcraft.api.inventory.ISpecialInventory;

public class ItemRouterTileEntity extends BaseRouterTileEntity implements IInventory, ISidedInventory,
		ISpecialInventory {

	public static class StackInfo {
		public int uuid;
		public int size;

		public StackInfo(int uuid, int stackSize) {
			this.uuid = uuid;
			this.size = stackSize;
		}
	}

	public ItemRouterTileEntity() throws Exception {
		super(ItemRouterPeripheral.class);
	}

	protected ItemRouterTileEntity(final Class<? extends ItemRouterPeripheral> class1) throws Exception {
		super(class1);
	}

	protected ItemStack itemSlot;

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
					StackInfo stackInfo = new StackInfo(uuid, amount);
					table.put(uuid, stackInfo);
				}
			}
		}
		AnzacPeripheralsCore.logger.info("table:" + table);
		return table.values().toArray(new StackInfo[table.size()]);
	}

	public int extractFrom(final ForgeDirection fromDir, final int uuid, final int amount,
			final ForgeDirection extractSide) throws Exception {
		if (itemSlot != null) {
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
	}

	@Override
	public int getSizeInventory() {
		return 1;
	}

	@Override
	public ItemStack getStackInSlot(final int i) {
		return itemSlot;
	}

	@Override
	public void setInventorySlotContents(final int slot, final ItemStack stack) {
		itemSlot = stack;
		if (stack != null) {
			if (stack.stackSize > getInventoryStackLimit()) {
				stack.stackSize = getInventoryStackLimit();
			}
			queueEvent(PeripheralEvent.item_route, Utils.getUUID(stack), stack.stackSize);
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
		return isConnected();
	}

	@Override
	public int[] getAccessibleSlotsFromSide(final int var1) {
		return new int[] { 0 };
	}

	@Override
	public boolean canInsertItem(final int i, final ItemStack itemstack, final int j) {
		return isConnected();
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
		final ItemRouterTileEntity other = (ItemRouterTileEntity) obj;
		if (itemSlot == null) {
			if (other.itemSlot != null)
				return false;
		} else if (!itemSlot.equals(other.itemSlot))
			return false;
		return true;
	}
}
