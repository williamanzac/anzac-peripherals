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
import anzac.peripherals.annotations.PeripheralMethod;
import anzac.peripherals.utils.Utils;
import dan200.computer.api.IWritableMount;

public class CraftingRouterTileEntity extends ItemRouterTileEntity {

	public SimpleInventory craftMatrix = new SimpleInventory(9);
	public SimpleInventory craftResult = new SimpleInventory(1);

	@Override
	public String getType() {
		return "CraftingRouter";
	}

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

	@PeripheralMethod
	public Object[] getRecipes() throws Exception {
		final List<String> recipes = new ArrayList<String>();
		if (getMount() == null) {
			throw new Exception("No disk loaded");
		}
		getMount().list(".", recipes);
		return recipes.toArray();
	}

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

	@PeripheralMethod
	public boolean removeRecipe(final int id) throws Exception {
		if (getMount() == null) {
			throw new Exception("No disk loaded");
		}
		((IWritableMount) getMount()).delete(String.valueOf(id));
		return true;
	}

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

	@PeripheralMethod
	public void craft(final int uuid, final ForgeDirection side) throws Exception {
		craft(uuid, side, side.getOpposite());
	}

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
}
