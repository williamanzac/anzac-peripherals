package anzac.peripherals.tiles;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
import net.minecraftforge.common.ForgeDirection;
import anzac.peripherals.AnzacPeripheralsCore;
import anzac.peripherals.peripheral.WorkbenchPeripheral;
import anzac.peripherals.tiles.ItemRouterTileEntity.StackInfo;
import anzac.peripherals.tiles.RecipeStorageTileEntity.Recipe;
import anzac.peripherals.utils.Utils;
import buildcraft.api.inventory.ISpecialInventory;

public class WorkbenchTileEntity extends BasePeripheralTileEntity implements IInventory, ISidedInventory,
		ISpecialInventory {

	public WorkbenchTileEntity() throws Exception {
		super(WorkbenchPeripheral.class);
	}

	private static final String INVENTORY = "inventory";
	private static final String SLOT = "Slot";
	private static final String MATRIX = "matrix";
	private static final int[] SLOT_ARRAY = Utils.createSlotArray(0, 24);

	public InternalInventoryCrafting craftMatrix = new InternalInventoryCrafting(3, this);
	private final ItemStack[] inventory = new ItemStack[24];
	public InventoryCraftResult craftResult = new InventoryCraftResult();
	private SlotCrafting craftSlot;
	private InternalPlayer internalPlayer;

	public void updateCraftingRecipe() {
		final ItemStack matchingRecipe = CraftingManager.getInstance().findMatchingRecipe(craftMatrix, worldObj);
		craftResult.setInventorySlotContents(0, matchingRecipe);
	}

	public boolean setRecipe(final Recipe recipe) {
		craftMatrix.clear();
		for (int i = 0; i < recipe.craftMatrix.length; i++) {
			StackInfo info = recipe.craftMatrix[i];
			if (info != null) {
				craftMatrix.setInventorySlotContents(i, Utils.getItemStack(info));
			}
		}
		updateCraftingRecipe();
		return craftResult.getStackInSlot(0) != null;
	}

	public void clear() {
		craftMatrix.clear();
	}

	public StackInfo[] contents() throws Exception {
		final Map<Integer, StackInfo> table = new HashMap<Integer, StackInfo>();
		for (final ItemStack stackInSlot : inventory) {
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

	public ItemStack craft() throws Exception {
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
		return notifyStack;
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
	public ItemStack getStackInSlotOnClosing(final int i) {
		final ItemStack stack = getStackInSlot(i);
		if (stack != null) {
			setInventorySlotContents(i, null);
		}
		return stack;
	}

	@Override
	public void setInventorySlotContents(final int slot, final ItemStack stack) {
		inventory[slot] = stack;
		if (stack == null) {
			return;
		}
		final int inventoryStackLimit = getInventoryStackLimit();
		if (stack.stackSize > inventoryStackLimit) {
			stack.stackSize = inventoryStackLimit;
		}
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
		return SLOT_ARRAY;
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
	public int addItem(final ItemStack stack, final boolean doAdd, final ForgeDirection from) {
		return Utils.addItem(this, stack, doAdd, from);
	}

	@Override
	public ItemStack[] extractItem(final boolean doRemove, final ForgeDirection from, final int maxItemCount) {
		final List<ItemStack> items = new ArrayList<ItemStack>();
		int total = 0;
		for (final int slot : getAccessibleSlotsFromSide(from.ordinal())) {
			final ItemStack stackInSlot = getStackInSlot(slot);
			if (stackInSlot != null && canExtractItem(slot, stackInSlot, from.ordinal())) {
				final ItemStack copy;
				if (doRemove) {
					copy = stackInSlot;
				} else {
					copy = stackInSlot.copy();
				}
				total += copy.stackSize;
				items.add(copy);
				if (total > maxItemCount) {
					final int over = total - maxItemCount;
					copy.stackSize -= over;
				} else if (doRemove) {
					inventory[slot] = null;
				}
			}
			if (total >= maxItemCount) {
				break;
			}
		}
		onInventoryChanged();
		return items.toArray(new ItemStack[items.size()]);
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
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((craftMatrix == null) ? 0 : craftMatrix.hashCode());
		result = prime * result + ((craftResult == null) ? 0 : craftResult.hashCode());
		result = prime * result + ((craftSlot == null) ? 0 : craftSlot.hashCode());
		result = prime * result + ((internalPlayer == null) ? 0 : internalPlayer.hashCode());
		result = prime * result + Arrays.hashCode(inventory);
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
		final WorkbenchTileEntity other = (WorkbenchTileEntity) obj;
		if (craftMatrix == null) {
			if (other.craftMatrix != null)
				return false;
		} else if (!craftMatrix.equals(other.craftMatrix))
			return false;
		if (craftResult == null) {
			if (other.craftResult != null)
				return false;
		} else if (!craftResult.equals(other.craftResult))
			return false;
		if (craftSlot == null) {
			if (other.craftSlot != null)
				return false;
		} else if (!craftSlot.equals(other.craftSlot))
			return false;
		if (internalPlayer == null) {
			if (other.internalPlayer != null)
				return false;
		} else if (!internalPlayer.equals(other.internalPlayer))
			return false;
		if (!Arrays.equals(inventory, other.inventory))
			return false;
		return true;
	}
}
