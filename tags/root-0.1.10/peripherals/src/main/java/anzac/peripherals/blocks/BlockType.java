package anzac.peripherals.blocks;

import java.util.Map;

import net.minecraft.tileentity.TileEntity;
import anzac.peripherals.tiles.CraftingRouterTileEntity;
import anzac.peripherals.tiles.FluidRouterTileEntity;
import anzac.peripherals.tiles.FluidStorageTileEntity;
import anzac.peripherals.tiles.ItemRouterTileEntity;
import anzac.peripherals.tiles.ItemStorageTileEntity;
import anzac.peripherals.tiles.PeripheralEvent;
import anzac.peripherals.tiles.RecipeStorageTileEntity;
import anzac.peripherals.tiles.RedstoneTileEntity;
import anzac.peripherals.tiles.WorkbenchTileEntity;

public enum BlockType {
	/**
	 * This block allows you to craft items via a connected computer. The interface has a crafting area that can only be
	 * set via a connected Computer, it also has an internal input and output cache. This block is only usable when
	 * connected to a Computer. Use the {@link WorkbenchTileEntity#setRecipe(Map)} method to set the desired item to
	 * craft. The required items can be injected in to the internal cache or you can manually input the items. Use the
	 * {@link WorkbenchTileEntity#craft()} method to craft the item. The crafted item will automatically go in to the
	 * output cache. This peripheral should ignore item metadata of the supplied input items.
	 */
	WORKBENCH(0, "item.anzac.workbench", "Computerised Workbench", WorkbenchTileEntity.class),
	/**
	 * This block allows you to store recipes on its internal HDD. The interface is similar to that of a vanilla
	 * crafting table. This block is only usable when connected to a Computer. You must have all the items required for
	 * the recipe and they are consumed when storing the recipe.The {@link PeripheralEvent#recipe_changed} event is
	 * fired when a valid recipe has been defined. To save the recipe you need to call the
	 * {@link RecipeStorageTileEntity#storeRecipe()} method from a connected Computer.The
	 * {@link RecipeStorageTileEntity#loadRecipe(int)} method can be used to load a recipe in to a variable. That
	 * variable can then be used to {@link WorkbenchTileEntity#setRecipe(Map)} on a connected
	 * {@link WorkbenchTileEntity}.
	 */
	RECIPE_STORAGE(1, "item.anzac.recipestorage", "Recipe Storage", RecipeStorageTileEntity.class),
	/**
	 * This block allows you to control the flow of items via a connected computer.
	 */
	ITEM_ROUTER(2, "item.anzac.itemrouter", "Item Router", ItemRouterTileEntity.class),
	/**
	 * This block allows you to control the flow of fluid via a connected computer.
	 */
	FLUID_ROUTER(3, "item.anzac.fluidrouter", "Fluid Router", FluidRouterTileEntity.class),
	/**  */
	ITEM_STORAGE(4, "item.anzac.itemstorage", "Item Storage", ItemStorageTileEntity.class),
	/**  */
	FLUID_STORAGE(5, "item.anzac.fluidstorage", "Fluid Storage", FluidStorageTileEntity.class),
	/**  */
	REDSTONE_CONTROL(6, "item.anzac.redstonecontrol", "Redstone Control", RedstoneTileEntity.class),
	/**  */
	CRAFTING_ROUTER(7, "item.anzac.craftingrouter", "Crafting Router", CraftingRouterTileEntity.class),

	;
	private final String key;

	private final String title;

	private final int meta;

	private final String iconKey;

	private final Class<? extends TileEntity> tileType;

	private BlockType(String key, String title, int meta, String iconKey, Class<? extends TileEntity> tileType) {
		this.key = key;
		this.title = title;
		this.meta = meta;
		this.iconKey = iconKey;
		this.tileType = tileType;
	}

	private BlockType(int meta, String key, String title, Class<? extends TileEntity> tileType) {
		this(key, title, meta, "", tileType);
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
}