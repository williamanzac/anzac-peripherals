package anzac.peripherals.utils;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import net.minecraft.item.ItemStack;
import net.minecraftforge.common.ForgeDirection;
import anzac.peripherals.tiles.CraftingRouterTileEntity.CraftingRecipe;

public class TypeConverter {
	public static Object[] convertArguments(final Object[] arguments, final Method method) throws Exception {
		if (arguments == null) {
			return null;
		}
		final Object[] parameters = new Object[arguments.length];
		for (int i = 0; i < arguments.length; i++) {
			parameters[i] = convertArgument(arguments[i], method.getParameterTypes()[i]);
		}
		return parameters;
	}

	public static Object[] convertReturn(final Object object, final Class<?> toClass) throws Exception {
		if (object == null) {
			return null;
		}
		if (object.getClass().isArray()) {
			final Class<?> componentType = object.getClass().getComponentType();
			// TODO convert array of complex objects to map of maps
			return (Object[]) object;
		}
		if (toClass.isAssignableFrom(CraftingRecipe.class)) {
			return new Object[] { convertCraftingRecipeToMap((CraftingRecipe) object) };
		}
		// TODO convert complex object to map
		return new Object[] { object };
	}

	public static Object convertArgument(final Object object, final Class<?> toClass) throws Exception {
		if (object == null) {
			return null;
		} else if (toClass.isAssignableFrom(String.class)) {
			return convertToString(object);
		} else if (toClass.isAssignableFrom(Integer.class) || toClass.isAssignableFrom(int.class)) {
			return convertToInt(object);
		} else if (toClass.isAssignableFrom(Long.class) || toClass.isAssignableFrom(long.class)) {
			return convertToLong(object);
		} else if (toClass.isAssignableFrom(Boolean.class) || toClass.isAssignableFrom(boolean.class)) {
			return convertToBoolean(object);
		} else if (toClass.isAssignableFrom(ForgeDirection.class)) {
			return convertToDirection(object);
		} else if (toClass.isEnum()) {
			return convertToEnum(object, toClass);
		} else if (toClass.isAssignableFrom(CraftingRecipe.class)) {
			return convertToCraftingRecipe((Map<?, ?>) object);
		}
		throw new Exception("Expected argument of type " + toClass.getName() + " got " + object.getClass());
	}

	private static String convertToString(final Object argument) throws Exception {
		return argument.toString();
	}

	private static int convertToInt(final Object argument) throws Exception {
		if (argument instanceof Number) {
			return ((Number) argument).intValue();
		} else if (argument instanceof String) {
			return Integer.parseInt((String) argument);
		}
		throw new Exception("Expected a Number");
	}

	private static boolean convertToBoolean(final Object argument) throws Exception {
		if (argument instanceof Boolean) {
			return ((Boolean) argument).booleanValue();
		} else if (argument instanceof String) {
			return Boolean.parseBoolean((String) argument);
		}
		throw new Exception("Expected a Boolean");
	}

	private static long convertToLong(final Object argument) throws Exception {
		if (argument instanceof Number) {
			return ((Number) argument).longValue();
		} else if (argument instanceof String) {
			return Long.parseLong((String) argument);
		}
		throw new Exception("Expected a Number");
	}

	private static ForgeDirection convertToDirection(final Object argument) throws Exception {
		if (argument instanceof Number) {
			return ForgeDirection.getOrientation(((Number) argument).intValue());
		} else if (argument instanceof String) {
			return ForgeDirection.valueOf(((String) argument).toUpperCase());
		}
		throw new Exception("Expected a Direction");
	}

	@SuppressWarnings("unchecked")
	private static <E extends Enum<?>> E convertToEnum(final Object argument, final Class<?> eClass) throws Exception {
		final E[] enumConstants = (E[]) eClass.getEnumConstants();
		if (argument instanceof Number) {
			final int ord = ((Number) argument).intValue();
			if (ord >= 0 && ord < enumConstants.length) {
				return enumConstants[ord];
			}
		} else if (argument instanceof String) {
			final String name = ((String) argument).toUpperCase();
			for (final E e : enumConstants) {
				if (e.name().equals(name)) {
					return e;
				}
			}
		}
		throw new Exception("Unexpected value");
	}

	private static Map<?, ?> convertCraftingRecipeToMap(final CraftingRecipe recipe) {
		final Map<String, Map<?, ?>> table = new HashMap<String, Map<?, ?>>();

		final Map<String, Integer> outputTable = new HashMap<String, Integer>();
		outputTable.put("uuid", Utils.getUUID(recipe.craftResult));
		outputTable.put("count", recipe.craftResult.stackSize);

		final Map<Integer, Map<String, Integer>> inputTable = new HashMap<Integer, Map<String, Integer>>();
		for (int i = 0; i < recipe.craftMatrix.getSizeInventory(); i++) {
			final ItemStack stackInSlot = recipe.craftMatrix.getStackInSlot(i);
			if (stackInSlot != null) {
				final Map<String, Integer> itemTable = new HashMap<String, Integer>();
				itemTable.put("uuid", Utils.getUUID(stackInSlot));
				itemTable.put("count", stackInSlot.stackSize);
				inputTable.put(i, itemTable);
			}
		}

		table.put("output", outputTable);
		table.put("input", inputTable);
		return table;
	}

	@SuppressWarnings("unchecked")
	private static CraftingRecipe convertToCraftingRecipe(final Map<?, ?> table) {
		final CraftingRecipe recipe = new CraftingRecipe();
		if (table.containsKey("output")) {
			final Map<String, Double> outputTable = (Map<String, Double>) table.get("output");
			final int uuid = outputTable.get("uuid").intValue();
			final int count = outputTable.get("count").intValue();
			recipe.craftResult = Utils.getItemStack(uuid, count);
		}
		if (table.containsKey("input")) {
			final Map<Double, Map<String, Double>> inputTable = (Map<Double, Map<String, Double>>) table.get("input");
			for (final Entry<Double, Map<String, Double>> entry : inputTable.entrySet()) {
				final Map<String, Double> itemTable = entry.getValue();
				final int uuid = itemTable.get("uuid").intValue();
				final int count = itemTable.get("count").intValue();
				final ItemStack itemStack = Utils.getItemStack(uuid, count);
				recipe.craftMatrix.setInventorySlotContents(entry.getKey().intValue(), itemStack);
			}
		}
		return recipe;
	}
}
