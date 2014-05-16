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
import net.minecraft.inventory.InventoryCraftResult;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.CraftingManager;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import anzac.peripherals.annotations.Peripheral;
import anzac.peripherals.annotations.PeripheralMethod;
import anzac.peripherals.utils.Utils;
import dan200.computer.api.IWritableMount;

/**
 * This block allows you to store recipes on its internal HDD. The interface is similar to that of a vanilla crafting
 * table. This block is only usable when connected to a Computer. You must have all the items required for the recipe
 * and they are consumed when storing the recipe.The {@link PeripheralEvent#recipe_changed} event is fired when a valid
 * recipe has been defined. To save the recipe you need to call the {@link #storeRecipe()} method from a connected
 * Computer.The {@link #loadRecipe(int)} method can be used to load a recipe in to a variable. That variable can then be
 * used to {@link WorkbenchTileEntity#setRecipe(Map)} on a connected {@link WorkbenchTileEntity}.
 * 
 * @author Tony
 */
@Peripheral(type = "RecipeStorage", events = { PeripheralEvent.recipe_changed })
public class RecipeStorageTileEntity extends BasePeripheralTileEntity {

	public InventoryCrafting craftMatrix = new InternalInventoryCrafting(3);
	public InventoryCraftResult craftResult = new InventoryCraftResult();

	public void onCraftMatrixChanged() {
		final ItemStack matchingRecipe = CraftingManager.getInstance().findMatchingRecipe(craftMatrix, worldObj);
		craftResult.setInventorySlotContents(0, matchingRecipe);
		final int uuid = Utils.getUUID(craftResult.getStackInSlot(0));
		PeripheralEvent.recipe_changed.fire(computers, uuid);
	}

	/**
	 * Returns a list of the currently known recipes.
	 * 
	 * @return An array of all the stored recipe uuids.
	 * @throws Exception
	 */
	@PeripheralMethod
	public Object[] getRecipes() throws Exception {
		final List<String> recipes = new ArrayList<String>();
		if (getMount() == null) {
			throw new Exception("No disk loaded");
		}
		getMount().list(".", recipes);
		return recipes.toArray();
	}

	@Override
	protected List<String> methodNames() {
		return getMethodNames(RecipeStorageTileEntity.class);
	}

	/**
	 * Will return the definition for the specified recipe.
	 * 
	 * @param id
	 *            The uuid for the recipe to get.
	 * @return A table defining the recipe.
	 * @throws Exception
	 */
	@PeripheralMethod
	public Map<Integer, Integer> loadRecipe(final int id) throws Exception {
		if (getMount() == null) {
			throw new Exception("No disk loaded");
		}
		final Map<Integer, Integer> table = new HashMap<Integer, Integer>();
		final InputStream stream = getMount().openForRead(String.valueOf(id));
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

	/**
	 * Will remove the specified recipe from the internal HDD.
	 * 
	 * @param id
	 *            The uuid of the recipe to remove.
	 * @return {@code true} if the recipe was successfully removed.
	 * @throws Exception
	 */
	@PeripheralMethod
	public boolean removeRecipe(final int id) throws Exception {
		if (getMount() == null) {
			throw new Exception("No disk loaded");
		}
		((IWritableMount) getMount()).delete(String.valueOf(id));
		return true;
	}

	/**
	 * Will add the current recipe to the internal HDD.
	 * 
	 * @return {@code true} if the recipe was successfully added.
	 * @throws Exception
	 */
	@PeripheralMethod
	public boolean storeRecipe() throws Exception {
		if (getMount() == null) {
			throw new Exception("No disk loaded");
		}
		final String name = String.valueOf(Utils.getUUID(craftResult.getStackInSlot(0)));
		final OutputStream stream = ((IWritableMount) getMount()).openForWrite(name);
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

	public boolean isUseableByPlayer(final EntityPlayer entityplayer) {
		return isConnected() && worldObj.getBlockTileEntity(xCoord, yCoord, zCoord) == this
				&& entityplayer.getDistanceSq(xCoord + 0.5D, yCoord + 0.5D, zCoord + 0.5D) <= 64.0D;
	}
}
