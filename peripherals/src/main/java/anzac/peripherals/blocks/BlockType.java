package anzac.peripherals.blocks;

import net.minecraft.tileentity.TileEntity;
import anzac.peripherals.peripheral.BasePeripheral;
import anzac.peripherals.peripheral.CraftingRouterPeripheral;
import anzac.peripherals.peripheral.FluidRouterPeripheral;
import anzac.peripherals.peripheral.FluidStoragePeripheral;
import anzac.peripherals.peripheral.FluidSupplierPeripheral;
import anzac.peripherals.peripheral.ItemRouterPeripheral;
import anzac.peripherals.peripheral.ItemStoragePeripheral;
import anzac.peripherals.peripheral.ItemSupplierPeripheral;
import anzac.peripherals.peripheral.NotePeripheral;
import anzac.peripherals.peripheral.RecipeStoragePeripheral;
import anzac.peripherals.peripheral.RedstonePeripheral;
import anzac.peripherals.peripheral.WorkbenchPeripheral;
import anzac.peripherals.tiles.CraftingRouterTileEntity;
import anzac.peripherals.tiles.FluidRouterTileEntity;
import anzac.peripherals.tiles.FluidStorageTileEntity;
import anzac.peripherals.tiles.FluidSupplierTileEntity;
import anzac.peripherals.tiles.ItemRouterTileEntity;
import anzac.peripherals.tiles.ItemStorageTileEntity;
import anzac.peripherals.tiles.ItemSupplierTileEntity;
import anzac.peripherals.tiles.NoteTileEntity;
import anzac.peripherals.tiles.PeripheralEvent;
import anzac.peripherals.tiles.RecipeStorageTileEntity;
import anzac.peripherals.tiles.RedstoneTileEntity;
import anzac.peripherals.tiles.WorkbenchTileEntity;

public enum BlockType {
	/**
	 * This block allows you to craft items via a connected computer. The interface has a crafting area that can only be
	 * set via a connected Computer, it also has an internal input and output cache. This block is only usable when
	 * connected to a Computer. Use the
	 * {@link WorkbenchPeripheral#setRecipe(anzac.peripherals.tiles.RecipeStorageTileEntity.Recipe)} method to set the
	 * desired item to craft. The required items can be injected in to the internal cache or you can manually input the
	 * items. Use the {@link WorkbenchPeripheral#craft()} method to craft the item. The crafted item will automatically
	 * go in to the output cache. This peripheral should ignore item metadata of the supplied input items.
	 */
	WORKBENCH(0, "item.anzac.workbench", "Computerised Workbench", WorkbenchTileEntity.class, WorkbenchPeripheral.class),
	/**
	 * This block allows you to store recipes on its internal HDD. The interface is similar to that of a vanilla
	 * crafting table. This block is only usable when connected to a Computer. You must have all the items required for
	 * the recipe and they are consumed when storing the recipe.The {@link PeripheralEvent#recipe_changed} event is
	 * fired when a valid recipe has been defined. To save the recipe you need to call the
	 * {@link RecipeStoragePeripheral#storeRecipe()} method from a connected Computer.The
	 * {@link RecipeStoragePeripheral#loadRecipe(int)} method can be used to load a recipe in to a variable. That
	 * variable can then be used to
	 * {@link WorkbenchPeripheral#setRecipe(anzac.peripherals.tiles.RecipeStorageTileEntity.Recipe)} on a connected
	 * {@link WorkbenchPeripheral}.
	 */
	RECIPE_STORAGE(1, "item.anzac.recipestorage", "Recipe Storage", RecipeStorageTileEntity.class,
			RecipeStoragePeripheral.class),
	/**
	 * This block allows you to control the flow of items via a connected computer.
	 */
	ITEM_ROUTER(2, "item.anzac.itemrouter", "Item Router", ItemRouterTileEntity.class, ItemRouterPeripheral.class),
	/**
	 * This block allows you to control the flow of fluid via a connected computer.
	 */
	FLUID_ROUTER(3, "item.anzac.fluidrouter", "Fluid Router", FluidRouterTileEntity.class, FluidRouterPeripheral.class),
	/**  */
	ITEM_STORAGE(4, "item.anzac.itemstorage", "Item Storage", ItemStorageTileEntity.class, ItemStoragePeripheral.class),
	/**  */
	FLUID_STORAGE(5, "item.anzac.fluidstorage", "Fluid Storage", FluidStorageTileEntity.class,
			FluidStoragePeripheral.class),
	/**  */
	REDSTONE_CONTROL(6, "item.anzac.redstonecontrol", "Redstone Control", RedstoneTileEntity.class,
			RedstonePeripheral.class),
	/**  */
	CRAFTING_ROUTER(7, "item.anzac.craftingrouter", "Crafting Router", CraftingRouterTileEntity.class,
			CraftingRouterPeripheral.class),
	/**  */
	NOTE_BLOCK(8, "item.anzac.noteblock", "Note Block", NoteTileEntity.class, NotePeripheral.class),
	/**  */
	ITEM_SUPPLIER(9, "item.anzac.itemsupplier", "Item Supplier", ItemSupplierTileEntity.class,
			ItemSupplierPeripheral.class),
	/**  */
	FLUID_SUPPLIER(10, "item.anzac.fluidsupplier", "Fluid Supplier", FluidSupplierTileEntity.class,
			FluidSupplierPeripheral.class), ;
	private final String key;

	private final String title;

	private final int meta;

	private final String iconKey;

	private final Class<? extends TileEntity> tileType;

	private final Class<? extends BasePeripheral> peripheralType;

	private BlockType(final String key, final String title, final int meta, final String iconKey,
			final Class<? extends TileEntity> tileType, final Class<? extends BasePeripheral> peripheralType) {
		this.key = key;
		this.title = title;
		this.meta = meta;
		this.iconKey = iconKey;
		this.tileType = tileType;
		this.peripheralType = peripheralType;
	}

	private BlockType(final int meta, final String key, final String title, final Class<? extends TileEntity> tileType,
			final Class<? extends BasePeripheral> peripheralType) {
		this(key, title, meta, "", tileType, peripheralType);
	}

	public String getKey() {
		return key;
	}

	public String getTitle() {
		return title;
	}

	public int getMeta() {
		return meta;
	}

	public String getIconKey() {
		return iconKey;
	}

	public Class<? extends TileEntity> getTileType() {
		return tileType;
	}

	public Class<? extends BasePeripheral> getPeripheralType() {
		return peripheralType;
	}
}