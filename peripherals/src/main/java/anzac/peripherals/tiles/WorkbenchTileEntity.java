package anzac.peripherals.tiles;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.inventory.InventoryCraftResult;
import net.minecraft.inventory.SlotCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.CraftingManager;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import anzac.peripherals.AnzacPeripheralsCore;
import anzac.peripherals.utils.Utils;
import dan200.computer.api.IComputerAccess;
import dan200.computer.api.ILuaContext;

public class WorkbenchTileEntity extends BasePeripheralTileEntity implements IInventory, ISidedInventory {
	private static final String INVENTORY = "inventory";
	private static final String SLOT = "Slot";
	private static final String MATRIX = "matrix";
	private static final int[] SLOT_ARRAY = Utils.createSlotArray(0, 24);

	private static enum Method {
		craft, setRecipe, listContents;

		public static String[] methodNames() {
			final Method[] values = Method.values();
			final String[] methods = new String[values.length];
			for (final Method method : values) {
				methods[method.ordinal()] = method.name();
			}
			return methods;
		}

		public static Method getMethod(final int ordinal) {
			for (final Method method : Method.values()) {
				if (method.ordinal() == ordinal) {
					return method;
				}
			}
			return null;
		}
	}

	public InternalInventoryCrafting craftMatrix = new InternalInventoryCrafting(3, this);
	private final ItemStack[] inventory = new ItemStack[24];
	public InventoryCraftResult craftResult = new InventoryCraftResult();
	private SlotCrafting craftSlot;
	private InternalPlayer internalPlayer;

	@Override
	public String getType() {
		return "Workbench";
	}

	@Override
	public String[] getMethodNames() {
		return Method.methodNames();
	}

	@SuppressWarnings("unchecked")
	@Override
	public Object[] callMethod(final IComputerAccess computer, final ILuaContext context, final int method,
			final Object[] arguments) throws Exception {
		switch (Method.getMethod(method)) {
		case craft:
			craft();
			break;
		case setRecipe:
			if (arguments.length == 0 || arguments[0] == null || !(arguments[0] instanceof Map)) {
				throw new Exception("expected pattern");
			}
			craftMatrix.clear();
			final Map<Double, Double> table = (HashMap<Double, Double>) arguments[0];
			for (final Entry<Double, Double> entry : table.entrySet()) {
				craftMatrix.setInventorySlotContents(entry.getKey().intValue(),
						Utils.getUUID(entry.getValue().intValue(), 1));
			}
			return new Object[] { craftResult.getStackInSlot(0) != null };
		case listContents:
			final Map<Integer, Integer> invTab = new HashMap<Integer, Integer>();
			for (int slot = 0; slot < inventory.length; slot++) {
				final ItemStack stack = inventory[slot];
				if (stack != null) {
					final int uuid = Utils.getUUID(stack);
					if (invTab.containsKey(uuid)) {
						final int count = invTab.get(uuid);
						invTab.put(uuid, count + stack.stackSize);
					} else {
						invTab.put(uuid, stack.stackSize);
					}
				}
			}
			return new Object[] { invTab };
		}
		return null;
	}

	public void updateCraftingRecipe() {
		final ItemStack matchingRecipe = CraftingManager.getInstance().findMatchingRecipe(craftMatrix, worldObj);
		craftResult.setInventorySlotContents(0, matchingRecipe);
	}

	private void craft() throws Exception {
		if (internalPlayer == null) {
			internalPlayer = new InternalPlayer(this);
			craftSlot = new SlotCrafting(internalPlayer, craftMatrix, craftResult, 0, 0, 0);
		}
		updateCraftingRecipe();
		final ItemStack resultStack = craftResult.getStackInSlot(0);
		AnzacPeripheralsCore.logger.info("craftResult: " + resultStack);
		if (resultStack == null) {
			throw new Exception("nothing to craft");
		}
		if (!craftMatrix.hasIngredients()) {
			throw new Exception("does not have ingredients");
		}
		if (!hasSpace(resultStack)) {
			throw new Exception("Not enough space in output");
		}
		craftMatrix.setCrafting(true);
		craftSlot.onPickupFromSlot(internalPlayer, resultStack);
		craftMatrix.setCrafting(false);
		final List<ItemStack> output = new ArrayList<ItemStack>();
		output.add(resultStack);
		final ItemStack notifyStack = resultStack.copy();
		final InventoryPlayer playerInventory = internalPlayer.inventory;
		final ItemStack[] mainInventory = playerInventory.mainInventory;
		for (int i = 0; i < mainInventory.length; i++) {
			final ItemStack itemStack = mainInventory[i];
			if (itemStack != null) {
				output.add(itemStack);
				mainInventory[i] = null;
			}
		}
		for (ItemStack itemStack : output) {
			for (int j = 15; j < getSizeInventory(); j++) {
				final ItemStack stackInSlot = getStackInSlot(j);
				if (stackInSlot == null) {
					setInventorySlotContents(j, itemStack.copy());
					itemStack = null;
					break;
				} else {
					final boolean merged = Utils.mergeItemStack(itemStack, stackInSlot);
					if (merged) {
						setInventorySlotContents(j, stackInSlot);
						if (itemStack.stackSize == 0) {
							break;
						}
					}
				}
			}
			if (itemStack != null && itemStack.stackSize > 0) {
				// no space, where to put it?
				// should not be able to get here.
				throw new Exception("Not enough space left in output: " + itemStack.stackSize);
			}
		}
		for (final IComputerAccess computer : computers.keySet()) {
			computer.queueEvent("crafted", new Object[] { Utils.getUUID(notifyStack), notifyStack.stackSize });
		}
	}

	private boolean hasSpace(final ItemStack itemStack) {
		final ItemStack tmpStack = itemStack.copy();
		for (int j = 15; j < getSizeInventory(); j++) {
			final ItemStack stackInSlot = getStackInSlot(j);
			if (stackInSlot == null) {
				return true;
			} else {
				final boolean merged = Utils.canMergeItemStack(tmpStack, stackInSlot);
				if (merged && tmpStack.stackSize == 0) {
					return true;
				}
			}
		}
		if (tmpStack.stackSize > 0) {
			// no space
			return false;
		}
		return true;
	}

	@Override
	public void attach(final IComputerAccess computer) {
		AnzacPeripheralsCore.workbenchMap.put(computer.getID(), this);
		super.attach(computer);
	}

	@Override
	public void detach(final IComputerAccess computer) {
		super.detach(computer);
		AnzacPeripheralsCore.workbenchMap.remove(computer.getID());
	}

	@Override
	public int getSizeInventory() {
		return inventory.length;
	}

	@Override
	public ItemStack getStackInSlot(final int i) {
		return inventory[i];
	}

	@Override
	public ItemStack decrStackSize(final int i, final int j) {
		final ItemStack stackInSlot = inventory[i];
		if (stackInSlot == null) {
			return null;
		}

		if (stackInSlot.stackSize <= j) {
			final ItemStack disk = stackInSlot;
			setInventorySlotContents(i, null);
			return disk;
		}

		final ItemStack part = stackInSlot.splitStack(j);
		if (stackInSlot.stackSize == 0) {
			setInventorySlotContents(i, null);
		} else {
			setInventorySlotContents(i, stackInSlot);
		}
		return part;
	}

	@Override
	public ItemStack getStackInSlotOnClosing(final int i) {
		return inventory[i];
	}

	@Override
	public void setInventorySlotContents(final int i, final ItemStack itemstack) {
		inventory[i] = itemstack;
		onInventoryChanged();
	}

	@Override
	public String getInvName() {
		return "";
	}

	@Override
	public boolean isInvNameLocalized() {
		return false;
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
	public int[] getAccessibleSlotsFromSide(final int side) {
		return isConnected() ? SLOT_ARRAY : new int[0];
	}

	@Override
	public boolean canInsertItem(final int slot, final ItemStack stack, final int side) {
		return isItemValidForSlot(slot, stack);
	}

	@Override
	public boolean canExtractItem(final int slot, final ItemStack stack, final int side) {
		return isConnected() && slot >= 15;
	}

	@Override
	public boolean isItemValidForSlot(final int slot, final ItemStack stack) {
		return isConnected() && slot < 15;
	}

	@Override
	public void readFromNBT(final NBTTagCompound tagCompound) {
		super.readFromNBT(tagCompound);

		// read craft slots
		NBTTagList list = tagCompound.getTagList(MATRIX);
		for (byte entry = 0; entry < list.tagCount(); entry++) {
			final NBTTagCompound itemTag = (NBTTagCompound) list.tagAt(entry);
			final int slot = itemTag.getByte(SLOT);
			if (slot >= 0 && slot < craftMatrix.getSizeInventory()) {
				final ItemStack stack = ItemStack.loadItemStackFromNBT(itemTag);
				craftMatrix.setInventorySlotContents(slot, stack);
			}
		}

		// read inventory slots
		list = tagCompound.getTagList(INVENTORY);
		for (byte entry = 0; entry < list.tagCount(); entry++) {
			final NBTTagCompound itemTag = (NBTTagCompound) list.tagAt(entry);
			final int slot = itemTag.getByte(SLOT);
			if (slot >= 0 && slot < getSizeInventory()) {
				final ItemStack stack = ItemStack.loadItemStackFromNBT(itemTag);
				setInventorySlotContents(slot, stack);
			}
		}
	}

	@Override
	public void writeToNBT(final NBTTagCompound tagCompound) {
		super.writeToNBT(tagCompound);

		// write craft slots
		NBTTagList list = new NBTTagList();
		for (byte slot = 0; slot < craftMatrix.getSizeInventory(); slot++) {
			final ItemStack stack = craftMatrix.getStackInSlot(slot);
			if (stack != null) {
				final NBTTagCompound itemTag = new NBTTagCompound();
				itemTag.setByte(SLOT, slot);
				stack.writeToNBT(itemTag);
				list.appendTag(itemTag);
			}
		}
		tagCompound.setTag(MATRIX, list);

		// write craft slots
		list = new NBTTagList();
		for (byte slot = 0; slot < getSizeInventory(); slot++) {
			final ItemStack stack = getStackInSlot(slot);
			if (stack != null) {
				final NBTTagCompound itemTag = new NBTTagCompound();
				itemTag.setByte(SLOT, slot);
				stack.writeToNBT(itemTag);
				list.appendTag(itemTag);
			}
		}
		tagCompound.setTag(INVENTORY, list);
	}
}
