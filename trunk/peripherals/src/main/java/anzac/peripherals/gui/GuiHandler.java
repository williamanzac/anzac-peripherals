package anzac.peripherals.gui;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import anzac.peripherals.inventory.CraftingRouterContainer;
import anzac.peripherals.inventory.ItemRouterContainer;
import anzac.peripherals.inventory.ItemStorageContainer;
import anzac.peripherals.inventory.RecipeStorageContainer;
import anzac.peripherals.inventory.WorkbenchContainer;
import anzac.peripherals.tiles.CraftingRouterTileEntity;
import anzac.peripherals.tiles.ItemRouterTileEntity;
import anzac.peripherals.tiles.ItemStorageTileEntity;
import anzac.peripherals.tiles.RecipeStorageTileEntity;
import anzac.peripherals.tiles.WorkbenchTileEntity;
import cpw.mods.fml.common.network.IGuiHandler;

public class GuiHandler implements IGuiHandler {

	@Override
	public Object getServerGuiElement(final int ID, final EntityPlayer player, final World world, final int x,
			final int y, final int z) {
		final TileEntity tileEntity = world.getBlockTileEntity(x, y, z);
		if (tileEntity instanceof RecipeStorageTileEntity) {
			return new RecipeStorageContainer(player.inventory, (RecipeStorageTileEntity) tileEntity);
		}
		if (tileEntity instanceof WorkbenchTileEntity) {
			return new WorkbenchContainer(player.inventory, (WorkbenchTileEntity) tileEntity);
		}
		if (tileEntity instanceof CraftingRouterTileEntity) {
			return new CraftingRouterContainer(player.inventory, (CraftingRouterTileEntity) tileEntity);
		}
		if (tileEntity instanceof ItemRouterTileEntity) {
			return new ItemRouterContainer(player.inventory, (ItemRouterTileEntity) tileEntity);
		}
		if (tileEntity instanceof ItemStorageTileEntity) {
			return new ItemStorageContainer(player.inventory, (ItemStorageTileEntity) tileEntity);
		}
		return null;
	}

	@Override
	public Object getClientGuiElement(final int ID, final EntityPlayer player, final World world, final int x,
			final int y, final int z) {
		final TileEntity tileEntity = world.getBlockTileEntity(x, y, z);
		if (tileEntity instanceof RecipeStorageTileEntity) {
			return new RecipeStorageGUI(player.inventory, (RecipeStorageTileEntity) tileEntity);
		}
		if (tileEntity instanceof WorkbenchTileEntity) {
			return new WorkbenchGUI(player.inventory, (WorkbenchTileEntity) tileEntity);
		}
		if (tileEntity instanceof CraftingRouterTileEntity) {
			return new CraftingRouterGUI(player.inventory, (CraftingRouterTileEntity) tileEntity);
		}
		if (tileEntity instanceof ItemRouterTileEntity) {
			return new ItemRouterGUI(player.inventory, (ItemRouterTileEntity) tileEntity);
		}
		if (tileEntity instanceof ItemStorageTileEntity) {
			return new ItemStorageGUI(player.inventory, (ItemStorageTileEntity) tileEntity);
		}
		return null;
	}
}
