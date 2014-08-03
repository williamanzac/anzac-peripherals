package anzac.peripherals.gui;

import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.util.Icon;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.StatCollector;
import net.minecraftforge.fluids.FluidTankInfo;

import org.lwjgl.opengl.GL11;

import anzac.peripherals.inventory.FluidSupplierContainer;
import anzac.peripherals.tiles.FluidSupplierTileEntity;

public class FluidSupplierGUI extends GuiContainer {

	public static final ResourceLocation gui = new ResourceLocation("anzac", "textures/gui/fluid_supply.png");

	private final FluidSupplierTileEntity tileEntity;

	public FluidSupplierGUI(final InventoryPlayer inventoryPlayer, final FluidSupplierTileEntity tileEntity) {
		super(new FluidSupplierContainer(inventoryPlayer, tileEntity));
		ySize = 166;
		this.tileEntity = tileEntity;
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(final float f, final int i, final int j) {
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		mc.renderEngine.bindTexture(gui);
		final int x = (width - xSize) / 2;
		final int y = (height - ySize) / 2;
		this.drawTexturedModalRect(x, y, 0, 0, xSize, ySize);

		final float energyStored = tileEntity.getStoredEnergy();
		final float maxEnergyStored = tileEntity.getMaxEnergy();
		final int energy = (int) (47f * (energyStored / maxEnergyStored));
		if (energy > 0) {
			drawTexturedModalRect(x + 62, y + 69 - energy, 192, 47 - energy, 16, energy);
		}
		if (isPointInRegion(62, 22, 16, 47, i, j)) {
			drawCreativeTabHoveringText("Stored: " + (int) energyStored + " / " + (int) maxEnergyStored, i, j);
		}

		final FluidTankInfo info = tileEntity.getInfo();
		// AnzacPeripheralsCore.logger.info("info: " + info);
		final int capacity = info.capacity;
		final int amount;
		if (info.fluid != null) {
			// AnzacPeripheralsCore.logger.info("fluid: " + info.fluid);
			amount = info.fluid.amount;
			float scale = Math.min(amount, capacity) / (float) capacity;
			// AnzacPeripheralsCore.logger.info("amount: " + amount + ", capacity: " + capacity + ", ratio: " + energy);
			mc.renderEngine.bindTexture(TextureMap.locationBlocksTexture);
			final Icon stillIcon = info.fluid.getFluid().getStillIcon();
			// AnzacPeripheralsCore.logger.info("icon: " + stillIcon);
			if (stillIcon != null) {
				for (int row = 0; row <= 47 / 16; row++) {
					// AnzacPeripheralsCore.logger.info("col: " + col + ", row: " + row);
					drawTexturedModelRectFromIcon(x + 26, 21 + y + row * 16, stillIcon, 16, 16);
				}
				this.mc.renderEngine.bindTexture(gui);
				drawTexturedModalRect(x + 26, y + 21, 26, 21, 16, 47 - (int) Math.floor(47 * scale) + 1);
				drawTexturedModalRect(x + 26, y + 22, 176, 0, 16, 47);
			}
		} else {
			amount = 0;
		}
		if (isPointInRegion(26, 22, 16, 47, i, j)) {
			drawCreativeTabHoveringText("Stored: " + amount + " / " + capacity, i, j);
		}
	}

	@Override
	protected void drawGuiContainerForegroundLayer(final int par1, final int par2) {
		final String title = tileEntity.hasLabel() ? tileEntity.getLabel() : "Fluid Supply";
		fontRenderer.drawString(title, 8, 6, 4210752);
		fontRenderer.drawString(StatCollector.translateToLocal("container.inventory"), 8, (ySize - 96) + 2, 4210752);
	}
}
