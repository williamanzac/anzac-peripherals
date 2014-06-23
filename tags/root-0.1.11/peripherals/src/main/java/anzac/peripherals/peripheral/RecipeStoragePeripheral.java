package anzac.peripherals.peripheral;

import java.util.List;
import java.util.Map;

import anzac.peripherals.annotations.Peripheral;
import anzac.peripherals.annotations.PeripheralMethod;
import anzac.peripherals.tiles.PeripheralEvent;
import anzac.peripherals.tiles.RecipeStorageTileEntity;
import anzac.peripherals.utils.ClassUtils;

@Peripheral(type = "RecipeStorage", events = { PeripheralEvent.recipe_changed })
public class RecipeStoragePeripheral extends BasePeripheral {

	public RecipeStoragePeripheral(RecipeStorageTileEntity entity) {
		super(entity);
	}

	@Override
	protected RecipeStorageTileEntity getEntity() {
		return (RecipeStorageTileEntity) entity;
	}

	@Override
	protected List<String> methodNames() {
		return ClassUtils.getMethodNames(RecipeStoragePeripheral.class);
	}

	/**
	 * Returns a list of the currently known recipes.
	 * 
	 * @return An array of all the stored recipe uuids.
	 * @throws Exception
	 */
	@PeripheralMethod
	public Object[] getRecipes() throws Exception {
		return getEntity().getRecipes();
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
		return getEntity().loadRecipe(id);
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
		return getEntity().removeRecipe(id);
	}

	/**
	 * Will add the current recipe to the internal HDD.
	 * 
	 * @return {@code true} if the recipe was successfully added.
	 * @throws Exception
	 */
	@PeripheralMethod
	public boolean storeRecipe() throws Exception {
		return getEntity().storeRecipe();
	}
}
