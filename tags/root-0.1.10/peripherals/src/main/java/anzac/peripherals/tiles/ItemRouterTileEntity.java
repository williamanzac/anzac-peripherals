package anzac.peripherals.tiles;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.ForgeDirection;
import anzac.peripherals.AnzacPeripheralsCore;
import anzac.peripherals.annotations.Peripheral;
import anzac.peripherals.annotations.PeripheralMethod;
import anzac.peripherals.utils.ClassUtils;
import anzac.peripherals.utils.Position;
import anzac.peripherals.utils.Utils;
import buildcraft.api.inventory.ISpecialInventory;
import dan200.computercraft.api.peripheral.IPeripheral;

@Peripheral(type = "ItemRouter", events = { PeripheralEvent.item_route })
public class ItemRouterTileEntity extends BaseRouterTileEntity implements IInventory, ISidedInventory,
		ISpecialInventory {

	protected ItemStack itemSlot;

	@Override
	protected List<String> methodNames() {
		return ClassUtils.getMethodNames(ItemRouterTileEntity.class);
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

	/**
	 * Will return a table containing the uuid and count of each item in the internal cache.
	 * 
	 * @return A table of the internal contents.
	 * @throws Exception
	 */
	@PeripheralMethod
	public Map<?, ?> contents() throws Exception {
		return contents(ForgeDirection.UNKNOWN);
	}

	/**
	 * Will return a table containing the uuid and count of each item in the inventory connected to {@code direction}
	 * side of this block.
	 * 
	 * @param direction
	 *            which side of this block to examine the inventory of.
	 * @return A table of the contents of the connected inventory.
	 * @throws Exception
	 */
	@PeripheralMethod
	public Map<?, ?> contents(final ForgeDirection direction) throws Exception {
		return contents(direction, direction.getOpposite());
	}

	/**
	 * Will return a table containing the uuid and count of each item in the inventory connected to {@code direction}
	 * side of this block and limited the examined slot to those accessible from {@code side} side.
	 * 
	 * @param direction
	 *            which side of this block to examine the inventory of.
	 * @param dir
	 *            which side of the inventory to examine.
	 * @return A table of the contents of the connected inventory.
	 * @throws Exception
	 */
	@PeripheralMethod
	public Map<Integer, Integer> contents(final ForgeDirection direction, final ForgeDirection dir) throws Exception {
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
		final Map<Integer, Integer> table = new HashMap<Integer, Integer>();
		for (final int i : slots) {
			final ItemStack stackInSlot = handler.getStackInSlot(i);
			if (stackInSlot != null) {
				final int uuid = Utils.getUUID(stackInSlot);
				final int amount = stackInSlot.stackSize;
				if (table.containsKey(uuid)) {
					final int a = table.get(uuid);
					table.put(uuid, a + amount);
				} else {
					table.put(uuid, amount);
				}
			}
		}
		AnzacPeripheralsCore.logger.info("table:" + table);
		return table;
	}

	/**
	 * Extract {@code amount} number of items with {@code uuid} from the inventory connected to {@code fromDir} side.
	 * 
	 * @param fromDir
	 *            which side of this block to extract from.
	 * @param uuid
	 *            the uuid of the items to extract.
	 * @param amount
	 *            the number of items to extract.
	 * @return The actual number of items extracted.
	 * @throws Exception
	 */
	@PeripheralMethod
	public int extractFrom(final ForgeDirection fromDir, final int uuid, final int amount) throws Exception {
		return extractFrom(fromDir, uuid, amount, fromDir.getOpposite());
	}

	/**
	 * Extract {@code amount} number of items with {@code uuid} from the {@code side} side of the inventory connected to
	 * {@code fromDir} side.
	 * 
	 * @param fromDir
	 *            which side of this block to extract from.
	 * @param uuid
	 *            the uuid of the items to extract.
	 * @param amount
	 *            the number of items to extract.
	 * @param extractSide
	 *            which side of the inventory to extract from.
	 * @return The actual number of items extracted.
	 * @throws Exception
	 */
	@PeripheralMethod
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
		final int[] slots = accessibleSlots(extractSide, inv);
		for (final int i : slots) {
			final ItemStack stackInSlot = inv.getStackInSlot(i);
			if (Utils.stacksMatch(stackInSlot, stackToFind)) {
				final ItemStack extracted = inv.decrStackSize(i, amount);
				setInventorySlotContents(0, extracted);
				return extracted.stackSize;
			}
		}
		return 0;
	}

	/**
	 * Transfer {@code amount} number of items from the internal cache to the inventory connected on {@code toDir} side.
	 * 
	 * @param toDir
	 *            the side the inventory is connected to.
	 * @param amount
	 *            the number of items to transfer.
	 * @return the actual number of items transferred.
	 * @throws Exception
	 */
	@PeripheralMethod
	public int routeTo(final ForgeDirection toDir, final int amount) throws Exception {
		return routeTo(toDir, toDir.getOpposite(), amount);
	}

	/**
	 * Transfer {@code amount} number of items from the internal cache to the {@code side} side of the inventory
	 * connected on {@code toDir} side.
	 * 
	 * @param toDir
	 *            the side the inventory is connected to.
	 * @param insertDir
	 *            the side the inventory to insert the items from.
	 * @param amount
	 *            the number of items to transfer.
	 * @return the actual number of items transferred.
	 * @throws Exception
	 */
	@PeripheralMethod
	public int routeTo(final ForgeDirection toDir, final ForgeDirection insertDir, final int amount) {
		final ItemStack copy = itemSlot.copy();
		copy.stackSize = amount;
		final int amount1 = copy.stackSize;
		copy.stackSize -= Utils.addToInventory(worldObj, xCoord, yCoord, zCoord, toDir, insertDir, copy);

		if (copy.stackSize > 0) {
			copy.stackSize -= Utils.addToPipe(worldObj, xCoord, yCoord, zCoord, toDir, copy);
		}
		final int toDec = amount1 - copy.stackSize;
		if (toDec > 0) {
			decrStackSize(0, toDec);
		}
		return amount - copy.stackSize;
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
			PeripheralEvent.item_route.fire(computers, Utils.getUUID(stack), stack.stackSize);
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

	/**
	 * Transfer {@code amount} number of items from the internal cache to another connected peripheral with
	 * {@code label} label. The peripheral must be connected to the same computer.
	 * 
	 * @param label
	 *            the label of the peripheral.
	 * @param amount
	 *            the number of items to transfer.
	 * @return the actual number of items transferred.
	 * @throws Exception
	 */
	@PeripheralMethod
	public int sendTo(final String label, final int amount) throws Exception {
		if (itemSlot == null || itemSlot.stackSize == 0) {
			throw new Exception("No Items to transfer");
		}
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
		final ItemStack copy = itemSlot.copy();
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

	/**
	 * Transfer {@code amount} amount of fluid from another connected peripheral with {@code label} label to the
	 * internal tank. The peripheral must be connected to the same computer.
	 * 
	 * @param label
	 *            the label of the peripheral.
	 * @param uuid
	 *            the uuid of the fluid to transfer.
	 * @param amount
	 *            the amount of fluid to transfer.
	 * @return the actual amount transferred.
	 * @throws Exception
	 */
	@PeripheralMethod
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
				final ItemStack extracted = inv.decrStackSize(i, amount);
				setInventorySlotContents(0, extracted);
				return extracted.stackSize;
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

	@Override
	public boolean equals(final IPeripheral other) {
		return equals((Object) other);
	}
}
