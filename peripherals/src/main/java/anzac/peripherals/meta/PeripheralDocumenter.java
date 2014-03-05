package anzac.peripherals.meta;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;

import anzac.peripherals.tiles.BasePeripheralTileEntity;
import anzac.peripherals.tiles.FluidRouterTileEntity;
import anzac.peripherals.tiles.FluidStorageTileEntity;
import anzac.peripherals.tiles.ItemRouterTileEntity;
import anzac.peripherals.tiles.ItemStorageTileEntity;
import anzac.peripherals.tiles.RecipeStorageTileEntity;
import anzac.peripherals.tiles.WorkbenchTileEntity;

public class PeripheralDocumenter {

	@SuppressWarnings("unchecked")
	private static final List<Class<? extends BasePeripheralTileEntity>> classes = Arrays.asList(
			FluidRouterTileEntity.class, FluidStorageTileEntity.class, ItemRouterTileEntity.class,
			ItemStorageTileEntity.class, RecipeStorageTileEntity.class, WorkbenchTileEntity.class);

	public static void main(String[] args) throws InstantiationException, IllegalAccessException {
		for (Class<? extends BasePeripheralTileEntity> clazz : classes) {
			BasePeripheralTileEntity instance = clazz.newInstance();
			String type = instance.getType();
			List<Method> methods = BasePeripheralTileEntity.getPeripheralMethods(clazz);
			for (Method method : methods) {

			}
		}
	}

}
