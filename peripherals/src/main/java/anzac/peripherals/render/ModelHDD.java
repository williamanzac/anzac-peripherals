package anzac.peripherals.render;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;

public class ModelHDD extends ModelBase {
	// fields
	private final ModelRenderer shape1;

	public ModelHDD() {
		textureWidth = 64;
		textureHeight = 24;

		shape1 = new ModelRenderer(this, 0, 0);
		shape1.addBox(0F, 0F, 0F, 16, 8, 16);
		shape1.setRotationPoint(-8F, 0F, -8F);
		shape1.setTextureSize(64, 24);
		shape1.mirror = true;
		setRotation(shape1, 0F, 0F, 0F);
	}

	@Override
	public void render(final Entity entity, final float f, final float f1, final float f2, final float f3,
			final float f4, final float f5) {
		super.render(entity, f, f1, f2, f3, f4, f5);
		setRotationAngles(f, f1, f2, f3, f4, f5, entity);
		shape1.render(f5);
	}

	private void setRotation(final ModelRenderer model, final float x, final float y, final float z) {
		model.rotateAngleX = x;
		model.rotateAngleY = y;
		model.rotateAngleZ = z;
	}

	@Override
	public void setRotationAngles(float f, float f1, float f2, float f3, float f4, float f5, final Entity entity) {
		super.setRotationAngles(f, f1, f2, f3, f4, f5, entity);
	}
}
