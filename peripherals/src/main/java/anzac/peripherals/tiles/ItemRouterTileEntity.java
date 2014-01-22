package anzac.peripherals.tiles;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.packet.Packet;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.ForgeDirection;
import net.minecraftforge.oredict.OreDictionary;
import anzac.peripherals.AnzacPeripheralsCore;
import anzac.peripherals.network.PacketHandler;
import anzac.peripherals.utils.Position;
import anzac.peripherals.utils.Utils;
import dan200.computer.api.IComputerAccess;
import dan200.computer.api.ILuaContext;
import dan200.computer.api.IPeripheral;

public class ItemRouterTileEntity extends TileEntity implements IPeripheral, IInventory, ISidedInventory {

	public ItemStack itemSlot;
	private final Map<IComputerAccess, Computer> computers = new HashMap<IComputerAccess, Computer>();
	private final Map<ItemStack, ForgeDirection> itemRules = new HashMap<ItemStack, ForgeDirection>();
	private ForgeDirection defaultRoute = ForgeDirection.UNKNOWN;
	private String label;

	protected static enum Method {
		addRule, removeRule, listRules, setDefault, extract, getLabel, setLabel;

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

	private class Computer {
		private final String name;

		public Computer(final String name) {
			this.name = name;
		}

		private String mount;
	}

	@Override
	public String getType() {
		return "ItemRouter";
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
		case addRule:
			ret = false;
			itemRules.put(new ItemStack(((Double) arguments[0]).intValue(), 1, OreDictionary.WILDCARD_VALUE),
					ForgeDirection.getOrientation(((Double) arguments[1]).intValue()));
			ret = true;
			break;
		case removeRule:
			final Integer id = ((Double) arguments[0]).intValue();
			ret = false;
			for (final Entry<ItemStack, ForgeDirection> entry : itemRules.entrySet()) {
				final ItemStack key = entry.getKey();
				if (key.itemID == id) {
					itemRules.remove(key);
					ret = true;
					break;
				}
			}
			break;
		case listRules:
			final List<String> lines = new ArrayList<String>();
			for (final Entry<ItemStack, ForgeDirection> entry : itemRules.entrySet()) {
				lines.add(entry.getKey().itemID + "=>" + entry.getValue());
			}
			return lines.toArray();
		case setDefault:
			defaultRoute = ForgeDirection.getOrientation(((Double) arguments[0]).intValue());
			ret = true;
			break;
		case extract:
			final ForgeDirection fromDir = ForgeDirection.getOrientation(((Double) arguments[0]).intValue());
			final int extractId = ((Double) arguments[1]).intValue();
			final int meta = ((Double) arguments[2]).intValue();
			final int amount = ((Double) arguments[3]).intValue();
			final Position pos = new Position(xCoord, yCoord, zCoord, fromDir);
			pos.moveForwards(1);
			final TileEntity te = worldObj.getBlockTileEntity((int) pos.x, (int) pos.y, (int) pos.z);
			if (te == null || !(te instanceof IInventory)) {
				throw new Exception("Inventory not found");
			}
			final IInventory inv = (IInventory) te;
			final ItemStack stackToFind = new ItemStack(extractId, amount, meta != -1 ? meta
					: OreDictionary.WILDCARD_VALUE);
			for (int i = 0; i < inv.getSizeInventory(); i++) {
				final ItemStack stackInSlot = inv.getStackInSlot(i);
				if (Utils.stacksMatch(stackInSlot, stackToFind)) {
					final ItemStack extracted = inv.decrStackSize(i, amount);
					setInventorySlotContents(0, extracted);
					ret = extracted.stackSize;
					break;
				}
			}
			break;
		case getLabel:
			ret = label;
			break;
		case setLabel:
			label = (String) arguments[0];
			break;
		}
		return ret == null ? null : new Object[] { ret };
	}

	@Override
	public boolean canAttachToSide(final int side) {
		return true;
	}

	@Override
	public void attach(final IComputerAccess computer) {
		AnzacPeripheralsCore.itemRouterMap.put(computer.getID(), this);
		computers.put(computer, new Computer(computer.getAttachmentName()));
	}

	@Override
	public void detach(final IComputerAccess computer) {
		AnzacPeripheralsCore.itemRouterMap.remove(computer.getID());
		computers.remove(computer);
	}

	@Override
	public void readFromNBT(final NBTTagCompound tagCompound) {
		super.readFromNBT(tagCompound);

		// read label
		if (tagCompound.hasKey("label")) {
			label = tagCompound.getString("label");
		}

		// read disk slot
		if (tagCompound.hasKey("item")) {
			final NBTTagCompound tagItem = (NBTTagCompound) tagCompound.getTag("item");
			itemSlot = ItemStack.loadItemStackFromNBT(tagItem);
		}

		// read default Route
		final int d = tagCompound.getInteger("default");
		defaultRoute = ForgeDirection.getOrientation(d);

		// read rules
		final NBTTagList ruleList = tagCompound.getTagList("rules");
		for (byte entry = 0; entry < ruleList.tagCount(); entry++) {
			final NBTTagCompound itemTag = (NBTTagCompound) ruleList.tagAt(entry);
			final int direction = itemTag.getInteger("direction");
			final ItemStack stack = ItemStack.loadItemStackFromNBT(itemTag);
			itemRules.put(stack, ForgeDirection.getOrientation(direction));
		}
	}

	@Override
	public void writeToNBT(final NBTTagCompound tagCompound) {
		super.writeToNBT(tagCompound);

		// write label
		if (label != null) {
			tagCompound.setString("label", label);
		}

		// write item slot
		if (itemSlot != null) {
			final NBTTagCompound tagItem = new NBTTagCompound();
			itemSlot.writeToNBT(tagItem);
			tagCompound.setTag("item", tagItem);
		}

		// write default Route
		tagCompound.setInteger("default", defaultRoute.ordinal());

		// write rules
		final NBTTagList ruleList = new NBTTagList();
		for (final Entry<ItemStack, ForgeDirection> entry : itemRules.entrySet()) {
			final NBTTagCompound itemTag = new NBTTagCompound();
			itemTag.setInteger("direction", entry.getValue().ordinal());
			entry.getKey().writeToNBT(itemTag);
			ruleList.appendTag(itemTag);
		}
		tagCompound.setTag("rules", ruleList);
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
			computer.queueEvent("item_sort", new Object[] { stack.itemID, stack.stackSize });
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
		return label == null ? "" : label;
	}

	@Override
	public boolean isInvNameLocalized() {
		return label == null ? false : true;
	}

	@Override
	public int getInventoryStackLimit() {
		return 64;
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
		return true;
	}

	@Override
	public int[] getAccessibleSlotsFromSide(final int var1) {
		return new int[] { 0 };
	}

	@Override
	public boolean canInsertItem(final int i, final ItemStack itemstack, final int j) {
		return true;
	}

	@Override
	public boolean canExtractItem(final int i, final ItemStack itemstack, final int j) {
		return false;
	}

	@Override
	public void updateEntity() {
		if (worldObj == null) { // sanity check
			return;
		}
		if (!worldObj.isRemote) {
			if (itemSlot != null && itemSlot.stackSize > 0 && worldObj.getTotalWorldTime() % 10 == 0) {
				routeItem();
			}
		}
		super.updateEntity();
	}

	protected void routeItem() {
		ForgeDirection side = defaultRoute;
		final ItemStack copy = itemSlot.copy();
		copy.stackSize = 1;
		for (final Entry<ItemStack, ForgeDirection> entry : itemRules.entrySet()) {
			final ItemStack key = entry.getKey();
			if (Utils.stacksMatch(key, copy)) {
				side = entry.getValue();
				break;
			}
		}

		if (side == ForgeDirection.UNKNOWN) {
			defaultRoute(copy);
		} else {
			routeTo(side, copy);
		}
		if (copy.stackSize == 0) {
			decrStackSize(0, 1);
		}
	}

	protected void routeTo(final ForgeDirection side, final ItemStack copy) {
		copy.stackSize -= Utils.addToInventory(worldObj, xCoord, yCoord, zCoord, side, copy);

		if (copy.stackSize > 0) {
			copy.stackSize -= Utils.addToPipe(worldObj, xCoord, yCoord, zCoord, side, copy);
		}
	}

	protected void defaultRoute(final ItemStack copy) {
		copy.stackSize -= Utils.addToRandomInventory(worldObj, xCoord, yCoord, zCoord, copy);

		if (copy.stackSize > 0) {
			copy.stackSize -= Utils.addToRandomPipe(worldObj, xCoord, yCoord, zCoord, copy);
		}
	}

	@Override
	public Packet getDescriptionPacket() {
		return PacketHandler.createTileEntityPacket("anzac", PacketHandler.ID_TILE_ENTITY, this);
	}
}
