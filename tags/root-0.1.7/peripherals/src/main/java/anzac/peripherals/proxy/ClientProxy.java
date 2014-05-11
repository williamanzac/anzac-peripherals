package anzac.peripherals.proxy;

import net.minecraft.network.packet.Packet;
import cpw.mods.fml.common.network.PacketDispatcher;

public class ClientProxy extends Proxy {

	@Override
	public void sendToServer(final Packet packet) {
		PacketDispatcher.sendPacketToServer(packet);
	}
}
