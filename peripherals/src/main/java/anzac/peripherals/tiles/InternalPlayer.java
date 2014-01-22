package anzac.peripherals.tiles;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ChatMessageComponent;
import net.minecraft.util.ChunkCoordinates;

final class InternalPlayer extends EntityPlayer {

	public InternalPlayer(final TileEntity tileEntity) {
		super(tileEntity.worldObj, "[Anzac]");
		posX = tileEntity.xCoord;
		posY = tileEntity.yCoord + 1;
		posZ = tileEntity.zCoord;
	}

	@Override
	public void sendChatToPlayer(final ChatMessageComponent var1) {
	}

	@Override
	public boolean canCommandSenderUseCommand(final int var1, final String var2) {
		return false;
	}

	@Override
	public ChunkCoordinates getPlayerCoordinates() {
		return null;
	}
}