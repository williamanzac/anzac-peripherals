package anzac.peripherals.proxy;

import java.util.HashSet;
import java.util.Set;

import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.ForgeSubscribe;
import anzac.peripherals.upgrade.UpgradeIcon;
import dan200.computercraft.api.turtle.ITurtleUpgrade;

public class ClientProxy extends CommonProxy {

	private static Set<UpgradeIcon> upgrades = new HashSet<UpgradeIcon>();

	@Override
	protected void registerForgeHandlers() {
		super.registerForgeHandlers();
		final ForgeHandlers handlers = new ForgeHandlers();
		MinecraftForge.EVENT_BUS.register(handlers);
	}

	@Override
	protected void registerTurtleUpgrade(final ITurtleUpgrade upgrade) {
		super.registerTurtleUpgrade(upgrade);
		if (upgrade instanceof UpgradeIcon) {
			upgrades.add((UpgradeIcon) upgrade);
		}
	}

	public class ForgeHandlers {
		private ForgeHandlers() {
		}

		@ForgeSubscribe
		public void onPreTextureStitch(final TextureStitchEvent.Pre event) {
			for (final UpgradeIcon upgrade : upgrades) {
				upgrade.registerIcons(event.map);
			}
		}
	}
}
