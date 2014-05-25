package anzac.peripherals.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import net.minecraft.item.ItemBlock;
import net.minecraft.tileentity.TileEntity;
import anzac.peripherals.blocks.BlockType;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Blocks {
	BlockType[] value() default {};

	String key() default "";

	String tool() default "";

	int toolLevel() default 0;

	Class<? extends ItemBlock> itemType() default ItemBlock.class;

	Class<? extends TileEntity> tileType() default TileEntity.class;
}
