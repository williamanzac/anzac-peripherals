package anzac.peripherals.render;

import java.util.EnumSet;
import java.util.Set;

import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.item.ItemStack;
import net.minecraftforge.client.IItemRenderer;

import org.lwjgl.opengl.GL11;

import anzac.peripherals.items.ItemType;
import anzac.peripherals.utils.RenderHelper;

public class RenderComponentItem implements IItemRenderer {

	public static RenderComponentItem instance = new RenderComponentItem();

	private final Set<ItemType> items = EnumSet.of(ItemType.ADVANCED_PERIPHERAL_FRAME, ItemType.BASIC_PERIPHERAL_FRAME,
			ItemType.TELEPORTER_FRAME);

	@Override
	public boolean handleRenderType(final ItemStack item, final ItemRenderType type) {
		for (final ItemType itemType : items) {
			if (itemType.getMeta() == item.getItemDamage()) {
				return true;
			}
		}
		return false;
	}

	@Override
	public boolean shouldUseRenderHelper(final ItemRenderType type, final ItemStack item,
			final ItemRendererHelper helper) {
		return true;
	}

	@Override
	public void renderItem(final ItemRenderType type, final ItemStack item, final Object... data) {
		double offset = -0.5;
		if (type == ItemRenderType.EQUIPPED || type == ItemRenderType.EQUIPPED_FIRST_PERSON) {
			offset = 0;
		} else if (type == ItemRenderType.ENTITY) {
			GL11.glScalef(0.5F, 0.5F, 0.5F);
		}
		RenderHelper.renderItemAsBlock((RenderBlocks) data[0], item, offset, offset, offset);
	}
}
