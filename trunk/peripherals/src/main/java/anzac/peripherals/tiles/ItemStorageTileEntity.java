package anzac.peripherals.tiles;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.common.ForgeDirection;
import net.minecraftforge.oredict.OreDictionary;
import anzac.peripherals.AnzacPeripheralsCore;
import anzac.peripherals.annotations.PeripheralMethod;
import anzac.peripherals.utils.Utils;
import buildcraft.api.inventory.ISpecialInventory;
import dan200.computer.api.IWritableMount;

public class ItemStorageTileEntity extends BaseStorageTileEntity implements IInventory, ISidedInventory,
		ISpecialInventory {

	private static final String INVENTORY = "inventory";
	private static final String SLOT = "Slot";
	private static final int maxStackSize = 128;
	private static final int maxCount = (AnzacPeripheralsCore.storageSize / 128) / maxStackSize;
	private static final int[] SLOT_ARRAY = Utils.createSlotArray(0, maxCount);

	private final ItemStack[] inventory = new ItemStack[maxCount];
	private boolean useOreDict = true;
	private boolean ignoreMeta = true;

	public ItemStorageTileEntity() {
	}

	@Override
	public String getType() {
		return "ItemStorage";
	}

	@Override
	protected List<String> methodNames() {
		return getMethodNames(ItemStorageTileEntity.class);
	}

	@Override
	@PeripheralMethod
	public Object contents() throws Exception {
		final Map<Integer, Integer> table = new HashMap<Integer, Integer>();
		for (int slot = 0; slot < getSizeInventory(); slot++) {
			final ItemStack stack = getStackInSlot(slot);
			if (stack != null) {
				final int uuid = Utils.getUUID(stack);
				final int amount = stack.stackSize;
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

	protected boolean isAllowed(final ItemStack stack) {
		final int id = getId(stack);
		return isAllowed(id);
	}

	private int getId(final ItemStack stack) {
		int id = -1;
		if (useOreDict) {
			id = OreDictionary.getOreID(stack);
		}
		if (id == -1) {
			if (ignoreMeta) {
				id = stack.itemID;
			} else {
				id = Utils.getUUID(stack);
			}
		}
		return id;
	}

	@Override
	public void readFromNBT(final NBTTagCompound nbtRoot) {
		super.readFromNBT(nbtRoot);

		// read inventory slots
		final NBTTagList list = nbtRoot.getTagList(INVENTORY);
		for (byte entry = 0; entry < list.tagCount(); entry++) {
			final NBTTagCompound itemTag = (NBTTagCompound) list.tagAt(entry);
			final int slot = itemTag.getInteger(SLOT);
			if (slot >= 0 && slot < getSizeInventory()) {
				final ItemStack stack = createStack(itemTag);
				setInventorySlotContents(slot, stack);
			}
		}
	}

	private ItemStack createStack(final NBTTagCompound itemTag) {
		final int uuid = itemTag.getInteger("uuid");
		final int size = itemTag.getInteger("count");
		final ItemStack stack = Utils.getItemStack(uuid, size);
		if (itemTag.hasKey("tag")) {
			stack.stackTagCompound = itemTag.getCompoundTag("tag");
		}
		return stack;
	}

	@Override
	public void writeToNBT(final NBTTagCompound nbtRoot) {
		super.writeToNBT(nbtRoot);

		// write craft slots
		final NBTTagList list = new NBTTagList();
		for (int slot = 0; slot < getSizeInventory(); slot++) {
			final ItemStack stack = getStackInSlot(slot);
			if (stack != null) {
				final NBTTagCompound itemTag = new NBTTagCompound();
				itemTag.setInteger(SLOT, slot);
				writeStack(stack, itemTag);
				list.appendTag(itemTag);
			}
		}
		nbtRoot.setTag(INVENTORY, list);
	}

	private void writeStack(final ItemStack stack, final NBTTagCompound itemTag) {
		itemTag.setInteger("uuid", Utils.getUUID(stack));
		itemTag.setInteger("count", stack.stackSize);
		if (stack.hasTagCompound()) {
			itemTag.setTag("tag", stack.stackTagCompound);
		}
	}

	@Override
	public void updateEntity() {
		super.updateEntity();

		// TODO add syncing to disc

		// if (worldObj.getTotalWorldTime() % 10 == 0) {
		// worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
		// // onInventoryChanged();
		// }
	}

	@Override
	public int getSizeInventory() {
		return maxCount;
	}

	@Override
	public ItemStack getStackInSlot(final int i) {
		if (getMount() != null && worldObj != null && !worldObj.isRemote) {
			// read from disk
			InputStream inputStream = null;
			DataInputStream in = null;
			try {
				if (!getMount().exists(String.valueOf(i))) {
					return null;
				}
				inputStream = getMount().openForRead(String.valueOf(i));
				in = new DataInputStream(inputStream);
				final NBTTagCompound tag = (NBTTagCompound) NBTBase.readNamedTag(in);
				return createStack(tag);
			} catch (final IOException e) {
				e.printStackTrace();
			} finally {
				try {
					if (in != null) {
						in.close();
					}
					if (inputStream != null) {
						inputStream.close();
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return inventory[i];
	}

	@Override
	public void setInventorySlotContents(final int slot, final ItemStack stack) {
		inventory[slot] = stack;
		if (stack != null) {
			final int inventoryStackLimit = getInventoryStackLimit();
			if (stack.stackSize > inventoryStackLimit) {
				stack.stackSize = inventoryStackLimit;
			}
		}
		if (getMount() != null && (getMount() instanceof IWritableMount) && worldObj != null && !worldObj.isRemote) {
			OutputStream outputStream = null;
			DataOutputStream out = null;
			try {
				if (stack == null) {
					((IWritableMount) getMount()).delete(String.valueOf(slot));
				} else {
					outputStream = ((IWritableMount) getMount()).openForWrite(String.valueOf(slot));
					out = new DataOutputStream(outputStream);
					final NBTTagCompound itemTag = new NBTTagCompound();
					writeStack(stack, itemTag);
					NBTBase.writeNamedTag(itemTag, out);
				}
			} catch (final IOException e) {
				e.printStackTrace();
			} finally {
				try {
					if (out != null) {
						out.close();
					}
					if (outputStream != null) {
						outputStream.close();
					}
				} catch (final IOException e) {
					e.printStackTrace();
				}
			}
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
				} else {
					setInventorySlotContents(slot, stack);
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
	public int[] getAccessibleSlotsFromSide(final int var1) {
		return SLOT_ARRAY;
	}

	@Override
	public boolean canInsertItem(final int i, final ItemStack itemstack, final int j) {
		if (!worldObj.isRemote) {
			return getMount() != null && isConnected() && isAllowed(itemstack);
		}
		return isAllowed(itemstack);
	}

	@Override
	public boolean canExtractItem(final int i, final ItemStack itemstack, final int j) {
		// cannot extract
		return false;
	}

	@Override
	public int getInventoryStackLimit() {
		return maxStackSize;
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
		if (!worldObj.isRemote) {
			return getMount() != null && isConnected() && isAllowed(itemstack);
		}
		return isAllowed(itemstack);
	}

	@Override
	@PeripheralMethod
	public void addFilter(final int id) throws Exception {
		int uuid = id;
		if (useOreDict) {
			final ItemStack itemStack = Utils.getItemStack(uuid);
			final int oreid = OreDictionary.getOreID(itemStack);
			if (oreid != -1) {
				uuid = oreid;
			}
		}
		if (ignoreMeta) {
			uuid = Utils.getId(uuid);
		}
		super.addFilter(uuid);
	}

	@PeripheralMethod
	public boolean isUseOreDict() {
		return useOreDict;
	}

	@PeripheralMethod
	public void setUseOreDict(final boolean useOreDict) {
		this.useOreDict = useOreDict;
	}

	@PeripheralMethod
	public boolean isIgnoreMeta() {
		return ignoreMeta;
	}

	@PeripheralMethod
	public void setIgnoreMeta(final boolean ignoreMeta) {
		this.ignoreMeta = ignoreMeta;
	}

	@Override
	public int addItem(final ItemStack stack, final boolean doAdd, final ForgeDirection from) {
		final ItemStack copy;
		final int size = stack.stackSize;
		if (doAdd) {
			copy = stack;
		} else {
			copy = stack.copy();
		}
		if (isAllowed(copy)) {
			for (final int slot : getAccessibleSlotsFromSide(from.ordinal())) {
				final ItemStack stackInSlot = getStackInSlot(slot);
				if (stackInSlot != null && Utils.stacksMatch(stackInSlot, copy)) {
					final ItemStack target = copy.copy();
					int l = stackInSlot.stackSize + copy.stackSize;
					target.stackSize = l;
					setInventorySlotContents(slot, target);
					copy.stackSize -= (target.stackSize - stackInSlot.stackSize);
				}
				if (copy.stackSize == 0) {
					break;
				}
			}
			if (copy.stackSize > 0) {
				for (final int slot : getAccessibleSlotsFromSide(from.ordinal())) {
					final ItemStack stackInSlot = getStackInSlot(slot);
					if (stackInSlot == null) {
						final ItemStack target = copy.copy();
						setInventorySlotContents(slot, target);
						copy.stackSize -= target.stackSize;
					}
					if (copy.stackSize == 0) {
						break;
					}
				}
			}
			onInventoryChanged();
			return size - copy.stackSize;
		}
		return 0;
	}

	@Override
	public ItemStack[] extractItem(final boolean doRemove, final ForgeDirection from, final int maxItemCount) {
		// cannot extract return empty array
		return new ItemStack[0];
	}
}
