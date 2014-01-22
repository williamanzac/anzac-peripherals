package anzac.peripherals.tiles;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.InventoryCraftResult;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import anzac.peripherals.AnzacPeripheralsCore;
import anzac.peripherals.utils.Utils;
import dan200.computer.api.IComputerAccess;
import dan200.computer.api.ILuaContext;
import dan200.computer.api.IMedia;
import dan200.computer.api.IMount;
import dan200.computer.api.IWritableMount;
import dan200.computer.shared.ItemDisk;

public class RecipeStorageTileEntity extends BasePeripheralTileEntity implements IInventory {

	public InventoryCrafting craftMatrix = new InternalInventoryCrafting(3);
	public ItemStack diskSlot;
	private IMount mount;
	public InventoryCraftResult craftResult = new InventoryCraftResult();

	private static enum Method {
		load, store, remove, list, isDiskPresent, getDiskLabel, setDiskLabel, getDiskID;

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

	private List<String> getRecipes() throws Exception {
		final List<String> recipes = new ArrayList<String>();
		if (mount == null) {
			throw new Exception("No disk loaded");
		}
		mount.list(".", recipes);
		return recipes;
	}

	@Override
	public String getType() {
		return "RecipeStorage";
	}

	@Override
	public String[] getMethodNames() {
		return Method.methodNames();
	}

	@Override
	public Object[] callMethod(final IComputerAccess computer, final ILuaContext context, final int method,
			final Object[] arguments) throws Exception {
		Object ret = null;
		switch (Method.getMethod(method)) {
		case load:
			if (arguments.length < 1) {
				throw new Exception("too few arguments");
			}
			if (!(arguments[0] instanceof String)) {
				throw new Exception("Expected string");
			}
			ret = loadRecipe((String) arguments[0]);
			break;
		case store:
			ret = storeRecipe();
			break;
		case remove:
			if (arguments.length < 1) {
				throw new Exception("too few arguments");
			}
			if (!(arguments[0] instanceof String)) {
				throw new Exception("Expected string");
			}
			ret = removeRecipe((String) arguments[0]);
			break;
		case list:
			return getRecipes().toArray();
		case getDiskID:
			ret = getDiskId();
			break;
		case isDiskPresent:
			ret = diskSlot != null;
			break;
		case getDiskLabel:
			ret = getDiskLabel();
			break;
		case setDiskLabel:
			if (arguments.length < 1) {
				throw new Exception("too few arguments");
			}
			if (!(arguments[0] instanceof String)) {
				throw new Exception("Expected string");
			}
			setDiskLabel((String) arguments[0]);
		}
		return ret == null ? null : new Object[] { ret };
	}

	private Map<Integer, Integer> loadRecipe(final String id) throws Exception {
		if (mount == null) {
			throw new Exception("No disk loaded");
		}
		final Map<Integer, Integer> table = new HashMap<Integer, Integer>();
		final InputStream stream = mount.openForRead(id);
		final DataInputStream in = new DataInputStream(stream);
		try {
			for (int i = 0; i < craftMatrix.getSizeInventory(); i++) {
				final int uuid = in.readInt();
				if (uuid > 0) {
					table.put(i, uuid);
				}
			}
		} finally {
			in.close();
		}
		return table;
	}

	private synchronized void setDiskLabel(final String label) throws Exception {
		final IMedia contents = getMedia();
		if (contents != null) {
			if (!contents.setLabel(diskSlot, label)) {
				throw new Exception("Disk label cannot be changed");
			}
		}
	}

	private String getDiskLabel() {
		String label = null;
		final IMedia contents = getMedia();
		if (contents != null) {
			label = contents.getLabel(diskSlot);
		}
		return label;
	}

	private boolean removeRecipe(final String name) throws Exception {
		if (mount == null) {
			throw new Exception("No disk loaded");
		}
		((IWritableMount) mount).delete(name);
		return true;
	}

	private boolean storeRecipe() throws Exception {
		if (mount == null) {
			throw new Exception("No disk loaded");
		}
		final String name = craftResult.getStackInSlot(0).getUnlocalizedName();
		final OutputStream stream = ((IWritableMount) mount).openForWrite(name);
		final DataOutputStream out = new DataOutputStream(stream);
		try {
			for (int i = 0; i < craftMatrix.getSizeInventory(); i++) {
				final ItemStack stackInSlot = craftMatrix.getStackInSlot(i);
				if (stackInSlot != null) {
					out.writeInt(Utils.getUUID(stackInSlot));
				} else {
					out.writeInt(0);
				}
			}
			return true;
		} finally {
			out.close();
		}
	}

	@Override
	public void attach(final IComputerAccess computer) {
		super.attach(computer);
		AnzacPeripheralsCore.storageMap.put(computer.getID(), this);
		mountDisk();
	}

	@Override
	public void detach(final IComputerAccess computer) {
		unmountDisk();
		AnzacPeripheralsCore.storageMap.remove(computer.getID());
		super.detach(computer);
	}

	@Override
	public void readFromNBT(final NBTTagCompound tagCompound) {
		super.readFromNBT(tagCompound);

		// read disk slot
		if (tagCompound.hasKey("disk")) {
			final NBTTagCompound tagDisk = (NBTTagCompound) tagCompound.getTag("disk");
			diskSlot = ItemStack.loadItemStackFromNBT(tagDisk);
		}
		mount = null;

		// read craft slots
		final NBTTagList list = tagCompound.getTagList("matrix");
		for (byte entry = 0; entry < list.tagCount(); entry++) {
			final NBTTagCompound itemTag = (NBTTagCompound) list.tagAt(entry);
			final int slot = itemTag.getByte("Slot");
			if (slot >= 0 && slot < craftMatrix.getSizeInventory()) {
				final ItemStack stack = ItemStack.loadItemStackFromNBT(itemTag);
				craftMatrix.setInventorySlotContents(slot, stack);
			}
		}
	}

	@Override
	public void writeToNBT(final NBTTagCompound tagCompound) {
		super.writeToNBT(tagCompound);

		// write disk slot
		if (diskSlot != null) {
			final NBTTagCompound tagDisk = new NBTTagCompound();
			diskSlot.writeToNBT(tagDisk);
			tagCompound.setTag("disk", tagDisk);
		}

		// write craft slots
		final NBTTagList list = new NBTTagList();
		for (byte slot = 0; slot < craftMatrix.getSizeInventory(); slot++) {
			final ItemStack stack = craftMatrix.getStackInSlot(slot);
			if (stack != null) {
				final NBTTagCompound itemTag = new NBTTagCompound();
				itemTag.setByte("Slot", slot);
				stack.writeToNBT(itemTag);
				list.appendTag(itemTag);
			}
		}
		tagCompound.setTag("matrix", list);
	}

	@Override
	public int getSizeInventory() {
		return 1;
	}

	@Override
	public ItemStack getStackInSlot(final int i) {
		return diskSlot;
	}

	@Override
	public ItemStack decrStackSize(final int i, final int j) {
		if (diskSlot == null) {
			return null;
		}

		if (diskSlot.stackSize <= j) {
			final ItemStack disk = diskSlot;
			setInventorySlotContents(0, null);
			return disk;
		}

		final ItemStack part = diskSlot.splitStack(j);
		if (diskSlot.stackSize == 0) {
			setInventorySlotContents(0, null);
		} else {
			setInventorySlotContents(0, diskSlot);
		}
		return part;
	}

	@Override
	public ItemStack getStackInSlotOnClosing(final int i) {
		final ItemStack result = diskSlot;
		diskSlot = null;

		return result;
	}

	@Override
	public void setInventorySlotContents(final int i, final ItemStack itemstack) {
		if (this.worldObj.isRemote) {
			diskSlot = itemstack;
			onInventoryChanged();
			return;
		}

		synchronized (this) {
			if (diskSlot != null) {
				unmountDisk();
			}

			diskSlot = itemstack;
			onInventoryChanged();

			// updateAnim();

			if (diskSlot != null) {
				mountDisk();
			}
		}
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
		return 1;
	}

	@Override
	public boolean isUseableByPlayer(final EntityPlayer entityplayer) {
		return worldObj.getBlockTileEntity(xCoord, yCoord, zCoord) == this
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
		return itemstack.getItem() instanceof ItemDisk;
	}

	private synchronized IMedia getMedia() {
		if (diskSlot != null) {
			final Item item = diskSlot.getItem();
			if (item instanceof IMedia) {
				return (IMedia) item;
			}
		}
		return null;
	}

	private synchronized Integer getDiskId() {
		if (diskSlot != null) {
			final Item item = diskSlot.getItem();
			if (item instanceof ItemDisk) {
				return ((ItemDisk) item).getDiskID(diskSlot);
			}
		}
		return null;
	}

	private synchronized void mountDisk() {
		if (diskSlot == null) {
			return;
		}
		final IMedia contents = getMedia();
		if (contents == null) {
			return;
		}
		if (mount == null) {
			mount = contents.createDataMount(diskSlot, this.worldObj);
		}
	}

	private synchronized void unmountDisk() {
		mount = null;
		return;
	}
}
