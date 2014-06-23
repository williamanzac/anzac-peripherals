package anzac.peripherals.providers;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeDirection;
import anzac.peripherals.tiles.RedstoneTileEntity;
import dan200.computercraft.api.redstone.IBundledRedstoneProvider;

public class AnzacBundledRedstoneProvider implements IBundledRedstoneProvider {

	@Override
	public int getBundledRedstoneOutput(World world, int x, int y, int z, int side) {
		TileEntity entity = world.getBlockTileEntity(x, y, z);
		if (entity instanceof RedstoneTileEntity) {
			return ((RedstoneTileEntity) entity).getBundledOutput(ForgeDirection.values()[side]);
		}
		return -1;
	}

}
