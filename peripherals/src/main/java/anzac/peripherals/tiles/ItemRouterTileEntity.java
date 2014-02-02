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
import anzac.peripherals.annotations.PeripheralMethod;
import anzac.peripherals.utils.Position;
import anzac.peripherals.utils.Utils;
import dan200.computer.api.IComputerAccess;

public class ItemRouterTileEntity extends BasePeripheralTileEntity implements IInventory, ISidedInventory {

	private static final List<String> METHOD_NAMES = getMethodNames(ItemRouterTileEntity.class);

	private ItemStack itemSlot;

	@Override
	public String getType() {
		return "ItemRouter";
	}

	@Override
	protected List<String> methodNames() {
		final List<String> methodNames = super.methodNames();
		methodNames.addAll(METHOD_NAMES);
		return methodNames;
	}

	@PeripheralMethod
	private Object contents() throws Exception {
		return contents(ForgeDirection.UNKNOWN);
	}

	@PeripheralMethod
	private Object contents(final ForgeDirection direction) throws Exception {
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
		final ForgeDirection opposite = direction.getOpposite();
		final int[] slots;
		if (handler instanceof ISidedInventory) {
			slots = ((ISidedInventory) handler).getAccessibleSlotsFromSide(opposite.ordinal());
		} else {
			slots = Utils.createSlotArray(0, handler.getSizeInventory());
		}
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

	@PeripheralMethod
	private Object routeFrom(final ForgeDirection fromDir, final int uuid, final int amount) throws Exception {
		final Position pos = new Position(xCoord, yCoord, zCoord, fromDir);
		pos.moveForwards(1);
		final TileEntity te = worldObj.getBlockTileEntity(pos.x, pos.y, pos.z);
		if (te == null || !(te instanceof IInventory)) {
			throw new Exception("Inventory not found");
		}
		final IInventory inv = (IInventory) te;
		final ItemStack stackToFind = Utils.getUUID(uuid);
		for (int i = 0; i < inv.getSizeInventory(); i++) {
			final ItemStack stackInSlot = inv.getStackInSlot(i);
			if (Utils.stacksMatch(stackInSlot, stackToFind)) {
				final ItemStack extracted = inv.decrStackSize(i, amount);
				setInventorySlotContents(0, extracted);
				return extracted.stackSize;
			}
		}
		return null;
	}

	@PeripheralMethod
	private int routeTo(final ForgeDirection toDir, final int amount) {
		final ItemStack copy = itemSlot.copy();
		copy.stackSize = amount;
		final int amount1 = copy.stackSize;
		routeTo(toDir, copy);
		final int toDec = amount1 - copy.stackSize;
		if (toDec > 0) {
			decrStackSize(0, toDec);
		}
		return amount - copy.stackSize;
	}

	@Override
	public void attach(final IComputerAccess computer) {
		super.attach(computer);
		AnzacPeripheralsCore.itemRouterMap.put(computer.getID(), this);
	}

	@Override
	public void detach(final IComputerAccess computer) {
		AnzacPeripheralsCore.itemRouterMap.remove(computer.getID());
		super.detach(computer);
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

	protected boolean isAllowed(final ItemStack itemStack) {
		return true;
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
		if (stack == null) {
			return;
		}
		if (stack.stackSize > getInventoryStackLimit()) {
			stack.stackSize = getInventoryStackLimit();
		}
		for (final IComputerAccess computer : computers.keySet()) {
			computer.queueEvent("item_sort", new Object[] { Utils.getUUID(stack), stack.stackSize });
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
		return isConnected() && isAllowed(itemstack);
	}

	@Override
	public int[] getAccessibleSlotsFromSide(final int var1) {
		return isConnected() ? new int[] { 0 } : new int[0];
	}

	@Override
	public boolean canInsertItem(final int i, final ItemStack itemstack, final int j) {
		return isConnected() && isAllowed(itemstack);
	}

	@Override
	public boolean canExtractItem(final int i, final ItemStack itemstack, final int j) {
		// cannot extract
		return false;
	}

	@Override
	public void updateEntity() {
		if (worldObj == null) { // sanity check
			return;
		}
		super.updateEntity();
		if (!worldObj.isRemote) {
			if (itemSlot != null && itemSlot.stackSize > 0 && worldObj.getTotalWorldTime() % 10 == 0 && isConnected()) {
				// routeItem();
			}
		}
	}

	protected void routeTo(final ForgeDirection side, final ItemStack copy) {
		copy.stackSize -= Utils.addToInventory(worldObj, xCoord, yCoord, zCoord, side, copy);

		if (copy.stackSize > 0) {
			copy.stackSize -= Utils.addToPipe(worldObj, xCoord, yCoord, zCoord, side, copy);
		}
	}

	@Override
	protected boolean requiresMount() {
		return false;
	}
}
