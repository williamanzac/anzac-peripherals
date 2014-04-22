package anzac.peripherals.gui;

import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.StatCollector;

import org.lwjgl.opengl.GL11;

import anzac.peripherals.inventory.TeleporterContainer;
import anzac.peripherals.tiles.TeleporterTileEntity;

public class TeleporterGUI extends GuiContainer {

	public static final ResourceLocation gui = new ResourceLocation("anzac", "textures/gui/charge_station.png");

	private final TeleporterTileEntity entity;

	public TeleporterGUI(final InventoryPlayer inventoryPlayer, final TeleporterTileEntity tileEntity) {
		super(new TeleporterContainer(inventoryPlayer, tileEntity));
		entity = tileEntity;
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(final float f, final int i, final int j) {
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		this.mc.renderEngine.bindTexture(gui);
		final int x = (width - xSize) / 2;
		final int y = (height - ySize) / 2;
		this.drawTexturedModalRect(x, y, 0, 0, xSize, ySize);

		final float energyStored = entity.getStoredEnergy();
		final float maxEnergyStored = entity.getMaxEnergy();
		final int energy = (int) (40f * (energyStored / maxEnergyStored));
		if (energy > 0) {
			drawTexturedModalRect(x + 82, y + 59 - energy, 205, 41 - energy, 12, energy);
		}
		if (isPointInRegion(82, 19, 12, 40, i, j)) {
			drawCreativeTabHoveringText("Stored: " + (int) energyStored + " / " + (int) maxEnergyStored, i, j);
		}
	}

	@Override
	protected void drawGuiContainerForegroundLayer(final int par1, final int par2) {
		fontRenderer.drawString("Turtle Teleporter", 8, 6, 4210752);
		// final Target target = entity.getTarget();
		// if (target != null) {
		// fontRenderer.drawString("Target:", 8, 14, 4210752);
		// final Position position = target.getPosition();
		// fontRenderer.drawString("x:" + position.x, 14, 22, 4210752);
		// fontRenderer.drawString("y:" + position.y, 14, 30, 4210752);
		// fontRenderer.drawString("z:" + position.z, 14, 38, 4210752);
		// }
		fontRenderer.drawString(StatCollector.translateToLocal("container.inventory"), 8, (ySize - 96) + 2, 4210752);
	}
}
