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
import anzac.peripherals.annotations.PeripheralMethod;
import anzac.peripherals.utils.Utils;
import dan200.computer.api.IComputerAccess;

public class WorkbenchTileEntity extends BasePeripheralTileEntity implements IInventory, ISidedInventory {

	private static final String INVENTORY = "inventory";
	private static final String SLOT = "Slot";
	private static final String MATRIX = "matrix";
	private static final int[] SLOT_ARRAY = Utils.createSlotArray(0, 24);

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
	protected List<String> methodNames() {
		return getMethodNames(WorkbenchTileEntity.class);
	}

	public void updateCraftingRecipe() {
		final ItemStack matchingRecipe = CraftingManager.getInstance().findMatchingRecipe(craftMatrix, worldObj);
		craftResult.setInventorySlotContents(0, matchingRecipe);
	}

	@PeripheralMethod
	public boolean setRecipe(final Map<Double, Double> recipe) {
		craftMatrix.clear();
		for (final Entry<Double, Double> entry : recipe.entrySet()) {
			craftMatrix.setInventorySlotContents(entry.getKey().intValue(),
					Utils.getItemStack(entry.getValue().intValue(), 1));
		}
		updateCraftingRecipe();
		return craftResult.getStackInSlot(0) != null;
	}

	@PeripheralMethod
	public void clear() {
		craftMatrix.clear();
	}

	@PeripheralMethod
	public Object contents() throws Exception {
		final Map<Integer, Integer> table = new HashMap<Integer, Integer>();
		for (final ItemStack stackInSlot : inventory) {
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
	public void craft() throws Exception {
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
		for (final IComputerAccess computer : computers) {
			computer.queueEvent("crafted", new Object[] { computer.getAttachmentName(), Utils.getUUID(notifyStack),
					notifyStack.stackSize });
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

	@Override
	protected boolean requiresMount() {
		return false;
	}
}
