package anzac.peripherals.tiles;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.ForgeDirection;
import anzac.peripherals.annotations.Peripheral;
import anzac.peripherals.annotations.PeripheralMethod;
import anzac.peripherals.utils.Utils;
import dan200.computercraft.api.filesystem.IWritableMount;

@Peripheral(type = "CraftingRouter")
public class CraftingRouterTileEntity extends ItemRouterTileEntity {

	public SimpleInventory craftMatrix = new SimpleInventory(9);
	public SimpleInventory craftResult = new SimpleInventory(1);

	@Override
	protected List<String> methodNames() {
		return getMethodNames(CraftingRouterTileEntity.class);
	}

	@Override
	public void readFromNBT(final NBTTagCompound tagCompound) {
		super.readFromNBT(tagCompound);
	}

	@Override
	public void writeToNBT(final NBTTagCompound tagCompound) {
		super.writeToNBT(tagCompound);
	}

	/**
	 * @return
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

	/**
	 * @param id
	 * @return
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
	 * @param id
	 * @return
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
	 * @return
	 * @throws Exception
	 */
	@PeripheralMethod
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
				} else {
					out.writeInt(0);
				}
			}
			return true;
		} finally {
			out.close();
		}
	}

	/**
	 * @param uuid
	 * @param side
	 * @throws Exception
	 */
	@PeripheralMethod
	public void craft(final int uuid, final ForgeDirection side) throws Exception {
		craft(uuid, side, side.getOpposite());
	}

	/**
	 * @param uuid
	 * @param side
	 * @param inputDir
	 * @throws Exception
	 */
	@PeripheralMethod
	public void craft(final int uuid, final ForgeDirection side, final ForgeDirection inputDir) throws Exception {
		final Map<Integer, Integer> recipe = loadRecipe(uuid);
		final ItemStack target = Utils.getItemStack(uuid);
		setRecipe(recipe);
		craftResult.setInventorySlotContents(0, target);
		final int amount = itemSlot.stackSize;
		routeTo(side, inputDir, amount);
		// for (final IComputerAccess computer : computers) {
		// computer.queueEvent("crafted", new Object[] { computer.getAttachmentName(), Utils.getUUID(notifyStack),
		// notifyStack.stackSize });
		// }
		clear();
	}

	private void setRecipe(final Map<Integer, Integer> recipe) {
		clear();
		for (final Entry<Integer, Integer> entry : recipe.entrySet()) {
			craftMatrix.setInventorySlotContents(entry.getKey(), Utils.getItemStack(entry.getValue(), 1));
		}
	}

	private void clear() {
		for (int i = 0; i < craftMatrix.getSizeInventory(); i++) {
			craftMatrix.setInventorySlotContents(i, null);
		}
		for (int i = 0; i < craftResult.getSizeInventory(); i++) {
			craftResult.setInventorySlotContents(i, null);
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
}
