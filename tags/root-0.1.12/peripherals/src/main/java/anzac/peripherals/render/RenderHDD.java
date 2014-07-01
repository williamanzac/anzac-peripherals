package anzac.peripherals.render;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.IItemRenderer;

import org.lwjgl.opengl.GL11;

public class RenderHDD implements IItemRenderer {

	public static final RenderHDD instance = new RenderHDD();
	public static final ResourceLocation texture = new ResourceLocation("anzac", "textures/items/hdd.png");

	private final ModelHDD modelHDD = new ModelHDD();

	@Override
	public boolean handleRenderType(final ItemStack item, final ItemRenderType type) {
		switch (type) {
		case EQUIPPED:
		case EQUIPPED_FIRST_PERSON:
		case INVENTORY:
			return true;
		default:
			return false;
		}
	}

	@Override
	public boolean shouldUseRenderHelper(final ItemRenderType type, final ItemStack item,
			final ItemRendererHelper helper) {
		return true;
	}

	@Override
	public void renderItem(final ItemRenderType type, final ItemStack item, final Object... data) {
		switch (type) {
		case INVENTORY:
			GL11.glPushMatrix();
			Minecraft.getMinecraft().renderEngine.bindTexture(texture);
			GL11.glTranslatef(0f, -0.4f, 0f);
			modelHDD.render((Entity) null, 0f, 0f, 0f, 0f, 0f, 0.0625f);
			GL11.glPopMatrix();
			break;
		case EQUIPPED:
		case EQUIPPED_FIRST_PERSON:
			GL11.glPushMatrix();
			Minecraft.getMinecraft().renderEngine.bindTexture(texture);
			GL11.glRotatef(-20f, 0f, 0f, 1f);
			GL11.glRotatef(-25f, 0f, 1f, 0f);
			GL11.glRotatef(10f, 1f, 0f, 0f);
			GL11.glTranslatef(0.4f, 0.5f, 0.4f);
			modelHDD.render((Entity) data[1], 0f, 0f, 0f, 0f, 0f, 0.0625f);
			GL11.glPopMatrix();
			break;
		default:
			break;
		}
	}
}
