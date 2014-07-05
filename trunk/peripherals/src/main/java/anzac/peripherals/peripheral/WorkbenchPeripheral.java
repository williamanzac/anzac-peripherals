package anzac.peripherals.peripheral;

import java.util.List;

import net.minecraft.item.ItemStack;
import anzac.peripherals.annotations.Peripheral;
import anzac.peripherals.annotations.PeripheralMethod;
import anzac.peripherals.tiles.ItemRouterTileEntity.StackInfo;
import anzac.peripherals.tiles.PeripheralEvent;
import anzac.peripherals.tiles.RecipeStorageTileEntity.Recipe;
import anzac.peripherals.tiles.WorkbenchTileEntity;
import anzac.peripherals.utils.ClassUtils;
import anzac.peripherals.utils.Utils;

@Peripheral(type = "Workbench", events = { PeripheralEvent.crafted })
public class WorkbenchPeripheral extends BasePeripheral {

	public WorkbenchPeripheral(final WorkbenchTileEntity entity) {
		super(entity);
	}

	@Override
	protected List<String> methodNames() {
		return ClassUtils.getMethodNames(WorkbenchPeripheral.class);
	}

	/**
	 * Will set the current recipe for this peripheral.
	 * 
	 * @param recipe
	 *            a table containing the definition of the recipe. The recipe can also be obtained from a connected
	 *            {@link RecipeStoragePeripheral} using the {@link RecipeStoragePeripheral#loadRecipe(int)} method.
	 * @return {@code true} if the recipe was successfully defined.
	 */
	@PeripheralMethod
	public boolean setRecipe(final Recipe recipe) {
		return getEntity().setRecipe(recipe);
	}

	/**
	 * Clears the current recipe.
	 */
	@PeripheralMethod
	public void clear() {
		getEntity().clear();
	}

	/**
	 * Will return a table with the uuid and count of each item in the internal cache.
	 * 
	 * @return A table of the internal contents.
	 * @throws Exception
	 */
	@PeripheralMethod
	public StackInfo[] contents() throws Exception {
		return getEntity().contents();
	}

	/**
	 * Will try and craft one unit of the specified recipe. The {@link PeripheralEvent#crafted} event will be fired if
	 * successful.
	 * 
	 * @throws Exception
	 */
	@PeripheralMethod
	public void craft() throws Exception {
		final ItemStack notifyStack = getEntity().craft();
		PeripheralEvent.crafted.fire(computers, Utils.getUUID(notifyStack), notifyStack.stackSize);
	}

	@Override
	protected WorkbenchTileEntity getEntity() {
		return (WorkbenchTileEntity) entity;
	}
}
