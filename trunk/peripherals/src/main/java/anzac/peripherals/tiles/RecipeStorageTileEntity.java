package anzac.peripherals.tiles;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.minecraft.inventory.InventoryCraftResult;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.CraftingManager;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import anzac.peripherals.AnzacPeripheralsCore;
import anzac.peripherals.annotations.PeripheralMethod;
import anzac.peripherals.utils.Utils;
import dan200.computer.api.IComputerAccess;
import dan200.computer.api.IWritableMount;

public class RecipeStorageTileEntity extends BasePeripheralTileEntity {
	private static final List<String> METHOD_NAMES = getMethodNames(RecipeStorageTileEntity.class);

	public InventoryCrafting craftMatrix = new InternalInventoryCrafting(3);
	public InventoryCraftResult craftResult = new InventoryCraftResult();

	public void onCraftMatrixChanged() {
		final ItemStack matchingRecipe = CraftingManager.getInstance().findMatchingRecipe(craftMatrix, worldObj);
		craftResult.setInventorySlotContents(0, matchingRecipe);
		final int uuid = Utils.getUUID(craftResult.getStackInSlot(0));
		for (final IComputerAccess computer : computers.keySet()) {
			computer.queueEvent("recipe_changed", new Object[] { uuid });
		}
	}

	@PeripheralMethod
	private Object[] getRecipes() throws Exception {
		final List<String> recipes = new ArrayList<String>();
		if (mount == null) {
			throw new Exception("No disk loaded");
		}
		mount.list(".", recipes);
		return recipes.toArray();
	}

	@Override
	public String getType() {
		return "RecipeStorage";
	}

	@Override
	protected List<String> methodNames() {
		final List<String> methodNames = super.methodNames();
		methodNames.addAll(METHOD_NAMES);
		return methodNames;
	}

	@PeripheralMethod
	private Map<Integer, Integer> loadRecipe(final int id) throws Exception {
		if (mount == null) {
			throw new Exception("No disk loaded");
		}
		final Map<Integer, Integer> table = new HashMap<Integer, Integer>();
		final InputStream stream = mount.openForRead(String.valueOf(id));
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

	@PeripheralMethod
	private boolean removeRecipe(final int id) throws Exception {
		if (mount == null) {
			throw new Exception("No disk loaded");
		}
		((IWritableMount) mount).delete(String.valueOf(id));
		return true;
	}

	@PeripheralMethod
	private boolean storeRecipe() throws Exception {
		if (mount == null) {
			throw new Exception("No disk loaded");
		}
		final String name = String.valueOf(Utils.getUUID(craftResult.getStackInSlot(0)));
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
	}

	@Override
	public void detach(final IComputerAccess computer) {
		AnzacPeripheralsCore.storageMap.remove(computer.getID());
		super.detach(computer);
	}

	@Override
	public void readFromNBT(final NBTTagCompound tagCompound) {
		super.readFromNBT(tagCompound);

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
	protected boolean requiresMount() {
		return true;
	}
}
