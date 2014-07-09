package anzac.peripherals.gui;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.StatCollector;

import org.lwjgl.opengl.GL11;

import anzac.peripherals.inventory.TeleporterContainer;
import anzac.peripherals.tiles.TeleporterTileEntity;

public class TeleporterGUI extends GuiContainer {

	public static final ResourceLocation gui = new ResourceLocation("anzac", "textures/gui/turtle_teleporter.png");

	private final TeleporterTileEntity entity;
	private final int numRows = 2;
	private final int numCols = 2;

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

		final int sizeInventory = entity.getSizeInventory();
		for (int r = 0; r < numRows; r++) {
			for (int c = 0; c < numCols; c++) {
				if (r * numCols + c >= sizeInventory) {
					drawTexturedModalRect(x + 115 + 18 * c, y + 18 + 18 * r, 176, 42, 18, 18);
				}
			}
		}

		final float energyStored = entity.getStoredEnergy();
		final float maxEnergyStored = entity.getMaxEnergy();
		final int energy = (int) (40f * (energyStored / maxEnergyStored));
		if (energy > 0) {
			drawTexturedModalRect(x + 46, y + 59 - energy, 205, 41 - energy, 12, energy);
		}

		if (isPointInRegion(46, 19, 12, 40, i, j)) {
			final List<String> list = new ArrayList<String>();
			list.add("Stored: " + (int) energyStored + " / " + (int) maxEnergyStored);
			drawHoveringText(list, i, j, fontRenderer);
		}
	}

	@Override
	protected void drawGuiContainerForegroundLayer(final int par1, final int par2) {
		fontRenderer.drawString("Turtle Teleporter", 8, 6, 4210752);
		fontRenderer.drawString(StatCollector.translateToLocal("container.inventory"), 8, (ySize - 96) + 2, 4210752);
	}
}
