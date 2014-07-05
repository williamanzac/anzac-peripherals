package anzac.peripherals.blocks;

import java.lang.reflect.Constructor;
import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.MinecraftForge;
import anzac.peripherals.AnzacPeripheralsCore;
import anzac.peripherals.annotations.Blocks;
import anzac.peripherals.annotations.Items;
import anzac.peripherals.items.ItemFactory;
import anzac.peripherals.items.ItemType;
import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.common.registry.LanguageRegistry;

public class BlockFactory {

	public static <B extends Block> B registerBlocks(final Class<B> blockClass, final int id) throws Exception {
		final Constructor<B> constructor = blockClass.getConstructor(int.class);
		final B block2 = constructor.newInstance(id);
		if (blockClass.isAnnotationPresent(Blocks.class)) {
			final Blocks blocks = blockClass.getAnnotation(Blocks.class);
			MinecraftForge.setBlockHarvestLevel(block2, blocks.tool(), blocks.toolLevel());
			GameRegistry.registerBlock(block2, blocks.itemType(), blocks.key(), AnzacPeripheralsCore.MOD_ID);
			final Class<? extends ItemBlock> itemClass = blocks.itemType();
			if (itemClass.isAnnotationPresent(Items.class)) {
				final ItemType[] items = itemClass.getAnnotation(Items.class).value();
				for (final ItemType item : items) {
					addName(block2, item);
				}
			}
		}
		return block2;
	}

	private static void addName(final Block block2, final ItemType item) {
		LanguageRegistry.addName(new ItemStack(block2, 1, item.getMeta()), item.getTitle());
	}

	@SuppressWarnings("rawtypes")
	public static void getSubBlocks(final Class<? extends Block> blockClass, final int id, final List list) {
		if (blockClass.isAnnotationPresent(Blocks.class)) {
			final Blocks blocks = blockClass.getAnnotation(Blocks.class);
			final Class<? extends ItemBlock> itemClass = blocks.itemType();
			ItemFactory.getSubItems(itemClass, id, list);
		}
	}

	public static TileEntity getTileEntity(final Class<? extends Block> blockClass, final int meta) {
		if (blockClass.isAnnotationPresent(Blocks.class)) {
			final BlockType[] blocks = blockClass.getAnnotation(Blocks.class).value();
			for (final BlockType block : blocks) {
				if (block.getMeta() == meta) {
					final Class<? extends TileEntity> tileClass = block.getTileType();
					try {
						return tileClass.newInstance();
					} catch (final Exception e) {
						e.printStackTrace();
					}
				}
			}
		}
		return null;
	}

	public static <B extends Block> void registerTileEntities(final Class<B> blockClass) {
		if (blockClass.isAnnotationPresent(Blocks.class)) {
			final Blocks blocks = blockClass.getAnnotation(Blocks.class);
			if (blocks.value() == null || blocks.value().length == 0) {
				final Class<? extends TileEntity> tileType = blocks.tileType();
				GameRegistry.registerTileEntity(tileType, tileType.getCanonicalName());
			} else {
				final BlockType[] blockTypes = blocks.value();
				for (final BlockType blockType : blockTypes) {
					final Class<? extends TileEntity> tileType = blockType.getTileType();
					GameRegistry.registerTileEntity(tileType, tileType.getCanonicalName());
				}
			}
		}
	}
}
