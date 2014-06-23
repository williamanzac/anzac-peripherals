package anzac.peripherals.gui;

import java.lang.reflect.Constructor;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import cpw.mods.fml.common.network.IGuiHandler;

public class GuiHandler implements IGuiHandler {

	@Override
	public Object getServerGuiElement(final int ID, final EntityPlayer player, final World world, final int x,
			final int y, final int z) {
		final TileEntity tileEntity = world.getBlockTileEntity(x, y, z);
		final Class<? extends TileEntity> tileClass = tileEntity.getClass();
		final String containerClassName = tileClass.getSimpleName().replace("TileEntity", "Container");
		try {
			final Class<?> containerClass = Class.forName("anzac.peripherals.inventory." + containerClassName);
			final Constructor<?> constructor = containerClass.getConstructor(InventoryPlayer.class, tileClass);
			return constructor.newInstance(player.inventory, tileEntity);
		} catch (final ClassNotFoundException e) {
			// ignore
		} catch (final Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public Object getClientGuiElement(final int ID, final EntityPlayer player, final World world, final int x,
			final int y, final int z) {
		final TileEntity tileEntity = world.getBlockTileEntity(x, y, z);
		final Class<? extends TileEntity> tileClass = tileEntity.getClass();
		final String containerClassName = tileClass.getSimpleName().replace("TileEntity", "GUI");
		try {
			final Class<?> containerClass = Class.forName("anzac.peripherals.gui." + containerClassName);
			final Constructor<?> constructor = containerClass.getConstructor(InventoryPlayer.class, tileClass);
			return constructor.newInstance(player.inventory, tileEntity);
		} catch (final ClassNotFoundException e) {
			// ignore
		} catch (final Exception e) {
			e.printStackTrace();
		}
		return null;
	}
}
