package anzac.peripherals.gui;

import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.StatCollector;

import org.lwjgl.opengl.GL11;

import anzac.peripherals.inventory.CraftingRouterContainer;
import anzac.peripherals.tiles.CraftingRouterTileEntity;

public class CraftingRouterGUI extends GuiContainer {

	public static final ResourceLocation gui = new ResourceLocation("anzac", "textures/gui/recipe_storage.png");

	private final CraftingRouterTileEntity tileEntity;

	public CraftingRouterGUI(final InventoryPlayer inventoryPlayer, final CraftingRouterTileEntity tileEntity) {
		super(new CraftingRouterContainer(inventoryPlayer, tileEntity));
		ySize = 170;
		this.tileEntity = tileEntity;
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(final float f, final int i, final int j) {
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		this.mc.renderEngine.bindTexture(gui);
		final int x = (width - xSize) / 2;
		final int y = (height - ySize) / 2;
		this.drawTexturedModalRect(x, y, 0, 0, xSize, ySize);
	}

	@Override
	protected void drawGuiContainerForegroundLayer(final int par1, final int par2) {
		final String title = tileEntity.hasLabel() ? tileEntity.getLabel() : "Crafting Router";
		fontRenderer.drawString(title, 8, 6, 4210752);
		fontRenderer.drawString(StatCollector.translateToLocal("container.inventory"), 8, (ySize - 96) + 2, 4210752);
	}
}
