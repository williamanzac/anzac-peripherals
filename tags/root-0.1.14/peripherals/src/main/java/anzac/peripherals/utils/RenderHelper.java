package anzac.peripherals.utils;

import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Icon;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;

import org.lwjgl.opengl.GL11;

public final class RenderHelper {

	public static final double RENDER_OFFSET = 1.0D / 1024.0D;
	public static final ResourceLocation MC_BLOCK_SHEET = new ResourceLocation("textures/atlas/blocks.png");
	public static final ResourceLocation MC_ITEM_SHEET = new ResourceLocation("textures/atlas/items.png");
	public static final ResourceLocation MC_FONT_DEFAULT = new ResourceLocation("textures/font/ascii.png");
	public static final ResourceLocation MC_FONT_ALTERNATE = new ResourceLocation("textures/font/ascii_sga.png");

	private RenderHelper() {
	}

	public static final TextureManager engine() {
		return Minecraft.getMinecraft().renderEngine;
	}

	public static final Tessellator tessellator() {
		return Tessellator.instance;
	}

	public static void setColor3ub(final int color) {
		GL11.glColor3ub((byte) (color >> 16 & 0xFF), (byte) (color >> 8 & 0xFF), (byte) (color & 0xFF));
	}

	public static void setColor4ub(final int color) {
		GL11.glColor4ub((byte) (color >> 24 & 0xFF), (byte) (color >> 16 & 0xFF), (byte) (color >> 8 & 0xFF),
				(byte) (color & 0xFF));
	}

	public static void resetColor() {
		GL11.glColor4f(1F, 1F, 1F, 1F);
	}

	public static void renderItemAsBlock(final RenderBlocks renderer, final ItemStack item, final double translateX,
			final double translateY, final double translateZ) {
		final Tessellator tessellator = tessellator();
		final Block block = Block.stone;
		final Icon texture = item.getIconIndex();

		if (texture == null) {
			return;
		}
		renderer.setRenderBoundsFromBlock(block);
		GL11.glTranslated(translateX, translateY, translateZ);
		tessellator.startDrawingQuads();

		tessellator.setNormal(0.0F, -1.0F, 0.0F);
		renderer.renderFaceYNeg(block, 0.0D, 0.0D, 0.0D, texture);

		tessellator.setNormal(0.0F, 1.0F, 0.0F);
		renderer.renderFaceYPos(block, 0.0D, 0.0D, 0.0D, texture);

		tessellator.setNormal(0.0F, 0.0F, -1.0F);
		renderer.renderFaceZNeg(block, 0.0D, 0.0D, 0.0D, texture);

		tessellator.setNormal(0.0F, 0.0F, 1.0F);
		renderer.renderFaceZPos(block, 0.0D, 0.0D, 0.0D, texture);

		tessellator.setNormal(-1.0F, 0.0F, 0.0F);
		renderer.renderFaceXNeg(block, 0.0D, 0.0D, 0.0D, texture);

		tessellator.setNormal(1.0F, 0.0F, 0.0F);
		renderer.renderFaceXPos(block, 0.0D, 0.0D, 0.0D, texture);

		tessellator.draw();
	}

	public static void renderItemIn2D(final Icon icon) {
		ItemRenderer.renderItemIn2D(Tessellator.instance, icon.getMaxU(), icon.getMinV(), icon.getMinU(),
				icon.getMaxV(), icon.getIconWidth(), icon.getIconHeight(), 0.0625F);
	}

	public static void renderIcon(final Icon icon, final double z) {
		Tessellator.instance.startDrawingQuads();
		Tessellator.instance.addVertexWithUV(0, 16, z, icon.getMinU(), icon.getMaxV());
		Tessellator.instance.addVertexWithUV(16, 16, z, icon.getMaxU(), icon.getMaxV());
		Tessellator.instance.addVertexWithUV(16, 0, z, icon.getMaxU(), icon.getMinV());
		Tessellator.instance.addVertexWithUV(0, 0, z, icon.getMinU(), icon.getMinV());
		Tessellator.instance.draw();
	}

	public static void renderIcon(final int x, final int y, final int z, final Icon icon, final int width,
			final int height) {
		final Tessellator tessellator = Tessellator.instance;
		tessellator.startDrawingQuads();
		tessellator.addVertexWithUV(x, y + height, z, icon.getMinU(), icon.getMaxV());
		tessellator.addVertexWithUV(x + width, y + height, z, icon.getMaxU(), icon.getMaxV());
		tessellator.addVertexWithUV(x + width, y, z, icon.getMaxU(), icon.getMinV());
		tessellator.addVertexWithUV(x, y, z, icon.getMinU(), icon.getMinV());
		tessellator.draw();
	}

	public static final Icon getFluidTexture(final Fluid fluid) {
		if (fluid == null) {
			return FluidRegistry.LAVA.getIcon();
		}
		return fluid.getIcon();
	}

	public static final Icon getFluidTexture(final FluidStack fluid) {
		if (fluid == null || fluid.getFluid() == null || fluid.getFluid().getIcon(fluid) == null) {
			return FluidRegistry.LAVA.getIcon();
		}
		return fluid.getFluid().getIcon(fluid);
	}

	public static final void bindItemTexture(final ItemStack stack) {
		engine().bindTexture(stack.getItemSpriteNumber() == 0 ? MC_BLOCK_SHEET : MC_ITEM_SHEET);
	}

	public static final void bindTexture(final ResourceLocation texture) {
		engine().bindTexture(texture);
	}

	public static final void setBlockTextureSheet() {
		bindTexture(MC_BLOCK_SHEET);
	}

	public static final void setItemTextureSheet() {
		bindTexture(MC_ITEM_SHEET);
	}

	public static final void setDefaultFontTextureSheet() {
		bindTexture(MC_FONT_DEFAULT);
	}

	public static final void setSGAFontTextureSheet() {
		bindTexture(MC_FONT_ALTERNATE);
	}
}
