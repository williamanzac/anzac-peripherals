package anzac.peripherals.providers;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import anzac.peripherals.tiles.BasePeripheralTileEntity;
import dan200.computercraft.api.peripheral.IPeripheral;
import dan200.computercraft.api.peripheral.IPeripheralProvider;

public class AnzacPeripheralProvider implements IPeripheralProvider {

	@Override
	public IPeripheral getPeripheral(final World world, final int x, final int y, final int z, final int side) {
		final TileEntity entity = world.getBlockTileEntity(x, y, z);
		if (entity instanceof BasePeripheralTileEntity) {
			return ((BasePeripheralTileEntity) entity).getPeripheral();
		}
		return null;
	}
}
