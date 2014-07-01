package anzac.peripherals.tiles;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.ForgeDirection;
import anzac.peripherals.AnzacPeripheralsCore;
import anzac.peripherals.peripheral.CraftingRouterPeripheral;
import anzac.peripherals.utils.Utils;
import dan200.computercraft.api.filesystem.IWritableMount;

public class CraftingRouterTileEntity extends ItemRouterTileEntity {

	private static final int[] INPUT_ARRAY = Utils.createSlotArray(0, 9);

	public static class CraftingRecipe {
		public final SimpleInventory craftMatrix = new SimpleInventory(9);
		public ItemStack craftResult;
	}

	public CraftingRouterTileEntity() throws Exception {
		super(CraftingRouterPeripheral.class);
	}

	public final SimpleInventory craftMatrix = new SimpleInventory(9);
	public final SimpleInventory craftResult = new SimpleInventory(1);
	private final SimpleInventory input = new SimpleInventory(9);
	private CraftingRecipe target;

	@Override
	public void readFromNBT(final NBTTagCompound tagCompound) {
		super.readFromNBT(tagCompound);
		if (tagCompound.hasKey("targetRecipe")) {
			int uuid = tagCompound.getInteger("targetRecipe");
			try {
				target = loadRecipe(uuid);
			} catch (Exception e) {
				AnzacPeripheralsCore.logger.warning("Unable to read target Recipe: " + uuid);
			}
		}
	}

	@Override
	public void writeToNBT(final NBTTagCompound tagCompound) {
		super.writeToNBT(tagCompound);
		if (target != null) {
			tagCompound.setInteger("targetRecipe", Utils.getUUID(target.craftResult));
		}
	}

	public Object[] getRecipes() throws Exception {
		final List<String> recipes = new ArrayList<String>();
		if (getMount() == null) {
			throw new Exception("No disk loaded");
		}
		getMount().list(".", recipes);
		return recipes.toArray();
	}

	public CraftingRecipe loadRecipe(final int id) throws Exception {
		if (getMount() == null) {
			throw new Exception("No disk loaded");
		}
		final CraftingRecipe recipe = new CraftingRecipe();
		final InputStream stream = getMount().openForRead(String.valueOf(id));
		final DataInputStream in = new DataInputStream(stream);
		try {
			for (int i = 0; i < recipe.craftMatrix.getSizeInventory(); i++) {
				final int uuid = in.readInt();
				if (uuid > 0) {
					final int stackSize = in.readInt();
					recipe.craftMatrix.setInventorySlotContents(i, Utils.getItemStack(uuid, stackSize));
				} else {
					recipe.craftMatrix.setInventorySlotContents(i, null);
				}
			}
			final int stackSize = in.readInt();
			recipe.craftResult = Utils.getItemStack(id, stackSize);
		} finally {
			in.close();
		}
		return recipe;
	}

	public boolean removeRecipe(final int id) throws Exception {
		if (getMount() == null) {
			throw new Exception("No disk loaded");
		}
		((IWritableMount) getMount()).delete(String.valueOf(id));
		return true;
	}

	public boolean addRecipe() throws Exception {
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
					out.writeInt(stackInSlot.stackSize);
				} else {
					out.writeInt(0);
				}
			}
			out.writeInt(craftResult.getStackInSlot(0).stackSize);
			return true;
		} finally {
			out.close();
		}
	}

	public void setRecipe(final CraftingRecipe recipe) throws Exception {
		if (target != null) {
			throw new Exception("A recipe has already been set.");
		}
		target = recipe;
	}

	public void setRecipe(final int uuid) throws Exception {
		setRecipe(loadRecipe(uuid));
	}

	public void clearRecipe() throws Exception {
		for (int i = 0; i < input.getSizeInventory(); i++) {
			if (input.getStackInSlot(i) != null) {
				throw new Exception("Cannot clear the recipe. The internal cache is not empty.");
			}
		}
		target = null;
	}

	public void craft(final ForgeDirection side, final ForgeDirection inputDir) throws Exception {
		if (target == null) {
			throw new Exception("No recipe set");
		}
		for (int i = 0; i < input.getSizeInventory(); i++) {
			final ItemStack inStack = input.getStackInSlot(i);
			final ItemStack targetStack = target.craftMatrix.getStackInSlot(i);
			if (inStack == null && targetStack == null) {
				continue;
			}
			if (!Utils.stacksMatch(targetStack, inStack) || targetStack.stackSize != inStack.stackSize) {
				throw new Exception("Internal cache does not match the recipe.");
			}
		}
		routeTo(side, inputDir);
	}

	private void routeTo(final ForgeDirection toDir, final ForgeDirection insertDir) throws Exception {
		for (int i = 0; i < input.getSizeInventory(); i++) {
			final ItemStack stackInSlot = input.getStackInSlot(i);
			if (stackInSlot != null) {
				final ItemStack copy = stackInSlot.copy();
				final int size = copy.stackSize;
				copy.stackSize -= Utils.routeTo(worldObj, xCoord, yCoord, zCoord, toDir, insertDir, copy);
				final int toDec = size - copy.stackSize;
				if (toDec > 0) {
					input.decrStackSize(i, toDec);
				} else {
					throw new Exception("Unable to transfer all items to target.");
				}
			}
		}
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((craftMatrix == null) ? 0 : craftMatrix.hashCode());
		result = prime * result + ((craftResult == null) ? 0 : craftResult.hashCode());
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
		final CraftingRouterTileEntity other = (CraftingRouterTileEntity) obj;
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
		return true;
	}

	@Override
	public int getSizeInventory() {
		return target == null ? super.getSizeInventory() : input.getSizeInventory();
	}

	@Override
	public ItemStack getStackInSlot(final int i) {
		return target == null ? super.getStackInSlot(i) : input.getStackInSlot(i);
	}

	@Override
	public void setInventorySlotContents(final int slot, final ItemStack stack) {
		if (target == null) {
			super.setInventorySlotContents(slot, stack);
		} else {
			if (Utils.stacksMatch(target.craftResult, stack)) {
				itemSlot = stack;
				if (stack.stackSize > getInventoryStackLimit()) {
					stack.stackSize = getInventoryStackLimit();
				}
				queueEvent(PeripheralEvent.crafted, Utils.getUUID(stack), stack.stackSize);
				onInventoryChanged();
			} else {
				input.setInventorySlotContents(slot, stack);
			}
		}
	}

	@Override
	public int getInventoryStackLimit() {
		return target == null ? super.getInventoryStackLimit() : input.getInventoryStackLimit();
	}

	@Override
	public boolean isItemValidForSlot(final int i, final ItemStack itemstack) {
		if (target == null) {
			return super.isItemValidForSlot(i, itemstack);
		}
		return isConnected()
				&& (Utils.stacksMatch(itemstack, target.craftMatrix.getStackInSlot(i)) || Utils.stacksMatch(
						target.craftResult, itemstack));
	}

	@Override
	public int[] getAccessibleSlotsFromSide(final int var1) {
		return target == null ? super.getAccessibleSlotsFromSide(var1) : INPUT_ARRAY;
	}

	@Override
	public boolean canInsertItem(final int i, final ItemStack itemstack, final int j) {
		if (target == null) {
			return super.canInsertItem(i, itemstack, j);
		}
		return isConnected()
				&& (Utils.stacksMatch(itemstack, target.craftMatrix.getStackInSlot(i)) || Utils.stacksMatch(
						target.craftResult, itemstack));
	}
}
