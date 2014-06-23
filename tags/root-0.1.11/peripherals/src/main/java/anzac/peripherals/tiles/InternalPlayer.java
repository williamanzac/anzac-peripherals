package anzac.peripherals.tiles;

import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ChunkCoordinates;
import net.minecraft.world.World;
import net.minecraftforge.common.FakePlayer;

public final class InternalPlayer extends FakePlayer {

	public InternalPlayer(final TileEntity tileEntity) {
		this(tileEntity.worldObj);
		posX = tileEntity.xCoord;
		posY = tileEntity.yCoord + 1;
		posZ = tileEntity.zCoord;
	}

	public InternalPlayer(final World world) {
		super(world, "[Anzac]");
	}

	@Override
	public ChunkCoordinates getPlayerCoordinates() {
		return null;
	}

	public void loadInventory(ItemStack copy) {
		inventory.currentItem = 0;
		inventory.setInventorySlotContents(0, copy);
	}
}