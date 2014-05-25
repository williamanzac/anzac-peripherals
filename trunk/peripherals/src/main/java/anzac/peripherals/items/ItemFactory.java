package anzac.peripherals.items;

import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.minecraft.client.renderer.texture.IconRegister;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Icon;
import anzac.peripherals.AnzacPeripheralsCore;
import anzac.peripherals.annotations.Items;
import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.common.registry.LanguageRegistry;

public class ItemFactory {

	public static <I extends Item> I registerItems(final Class<I> itemClass, final int id) throws Exception {
		final Constructor<I> constructor = itemClass.getConstructor(int.class);
		final I item2 = constructor.newInstance(id);

		if (itemClass.isAnnotationPresent(Items.class)) {
			GameRegistry.registerItem(item2, itemClass.getAnnotation(Items.class).key(), AnzacPeripheralsCore.MOD_ID);
			final ItemType[] items = itemClass.getAnnotation(Items.class).value();
			for (final ItemType item : items) {
				addName(item2, item);
			}
		}
		return item2;
	}

	private static void addName(final Item item2, final ItemType item) {
		LanguageRegistry.addName(new ItemStack(item2, 1, item.getMeta()), item.getTitle());
	}

	@SuppressWarnings("rawtypes")
	public static void getSubItems(final Class<? extends Item> itemClass, final int id, final List list) {
		if (itemClass.isAnnotationPresent(Items.class)) {
			final ItemType[] items = itemClass.getAnnotation(Items.class).value();
			for (final ItemType item : items) {
				addItem(id, list, item);
			}
		}
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private static void addItem(final int id, final List list, final ItemType item) {
		list.add(new ItemStack(id, 1, item.getMeta()));
	}

	public static String getUnlocalizedName(final ItemStack itemStack) {
		final Class<? extends Item> itemClass = itemStack.getItem().getClass();
		if (itemClass.isAnnotationPresent(Items.class)) {
			final ItemType[] items = itemClass.getAnnotation(Items.class).value();
			for (final ItemType item : items) {
				if (item.getMeta() == itemStack.getItemDamage()) {
					return item.getKey();
				}
			}
			return itemClass.getAnnotation(Items.class).key();
		}
		return "item.anzac." + itemClass.getSimpleName().toLowerCase();
	}

	private static Map<Class<? extends Item>, Map<Integer, Icon>> itemIcons = new HashMap<Class<? extends Item>, Map<Integer, Icon>>();

	public static void registerIcons(final Class<? extends Item> itemClass, final IconRegister iconRegister) {
		if (!itemIcons.containsKey(itemClass)) {
			itemIcons.put(itemClass, new HashMap<Integer, Icon>());
		}
		if (itemClass.isAnnotationPresent(Items.class)) {
			final ItemType[] items = itemClass.getAnnotation(Items.class).value();
			for (final ItemType item : items) {
				addIcon(itemClass, iconRegister, item);
			}
		}
	}

	private static void addIcon(final Class<? extends Item> itemClass, final IconRegister iconRegister,
			final ItemType item) {
		final Icon icon = iconRegister.registerIcon(item.getIconKey());
		itemIcons.get(itemClass).put(item.getMeta(), icon);
	}

	public static Icon getIcon(final Class<? extends Item> itemClass, final int meta) {
		final Map<Integer, Icon> map = itemIcons.get(itemClass);
		return map != null ? map.get(meta) : null;
	}
}
