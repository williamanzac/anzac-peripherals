package anzac.peripherals.gui;

import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.StatCollector;

import org.lwjgl.opengl.GL11;

import anzac.peripherals.inventory.ItemSupplierContainer;
import anzac.peripherals.tiles.ItemSupplierTileEntity;

public class ItemSupplierGUI extends GuiContainer {

	public static final ResourceLocation gui = new ResourceLocation("anzac", "textures/gui/item_supply.png");

	private final ItemSupplierTileEntity tileEntity;

	public ItemSupplierGUI(final InventoryPlayer inventoryPlayer, final ItemSupplierTileEntity tileEntity) {
		super(new ItemSupplierContainer(inventoryPlayer, tileEntity));
		ySize = 166;
		this.tileEntity = tileEntity;
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(final float f, final int i, final int j) {
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		this.mc.renderEngine.bindTexture(gui);
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
	}

	@Override
	protected void drawGuiContainerForegroundLayer(final int par1, final int par2) {
		final String title = tileEntity.hasLabel() ? tileEntity.getLabel() : "Item Supplier";
		fontRenderer.drawString(title, 8, 6, 4210752);
		fontRenderer.drawString(StatCollector.translateToLocal("container.inventory"), 8, (ySize - 96) + 2, 4210752);
	}
}
