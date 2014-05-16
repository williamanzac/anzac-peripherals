package anzac.peripherals.proxy;

import net.minecraft.network.packet.Packet;
import cpw.mods.fml.common.SidedProxy;

public class Proxy {

	@SidedProxy(clientSide = "anzac.peripherals.proxy.ClientProxy", serverSide = "anzac.peripherals.proxy.Proxy")
	public static Proxy instance;

	public void sendToServer(final Packet packet) {
	}
}
