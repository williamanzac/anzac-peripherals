package anzac.peripherals.utils;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import net.minecraftforge.common.ForgeDirection;
import anzac.peripherals.tiles.CraftingRouterTileEntity.CraftingRecipe;
import anzac.peripherals.tiles.FluidRouterTileEntity.TankInfo;
import anzac.peripherals.tiles.ItemRouterTileEntity.StackInfo;
import anzac.peripherals.tiles.RecipeStorageTileEntity.Recipe;
import anzac.peripherals.tiles.TeleporterTileEntity.Target;

public class TypeConverter {
	public static Object[] convertArguments(final Object[] arguments, final Method method) throws Exception {
		if (arguments == null) {
			return null;
		}
		final Object[] parameters = new Object[arguments.length];
		for (int i = 0; i < arguments.length; i++) {
			parameters[i] = luaToJava(arguments[i], method.getParameterTypes()[i]);
		}
		return parameters;
	}

	public static Object[] convertReturn(final Object object, final Class<?> toClass) throws Exception {
		if (object == null) {
			return null;
		}
		if (object.getClass().isArray()) {
			final Object[] objects = (Object[]) object;
			final Object[] rets = new Object[objects.length];
			for (int i = 0; i < objects.length; i++) {
				final Object value = objects[i];
				rets[i] = javaToLUA(value, value.getClass());
			}
			return rets;
		}
		final Object ret = javaToLUA(object, toClass);
		return new Object[] { ret };
	}

	public static Object javaToLUA(final Object object, final Class<?> toClass) {
		if (object == null) {
			return null;
		} else if (toClass.isAssignableFrom(CraftingRecipe.class)) {
			return convertCraftingRecipeToMap((CraftingRecipe) object);
		} else if (toClass.isAssignableFrom(TankInfo.class)) {
			return convertTankInfoToMap((TankInfo) object);
		} else if (toClass.isAssignableFrom(StackInfo.class)) {
			return convertStackInfoToMap((StackInfo) object);
		} else if (toClass.isAssignableFrom(Recipe.class)) {
			return convertRecipeToMap((Recipe) object);
		} else if (toClass.isAssignableFrom(Target.class)) {
			return convertTargetToMap((Target) object);
		}
		return object;
	}

	@SuppressWarnings("unchecked")
	public static Object luaToJava(final Object object, final Class<?> toClass) throws Exception {
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
		} else if (toClass.isAssignableFrom(Recipe.class)) {
			return convertToRecipe((Map<Double, Map<String, Double>>) object);
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
		table.put("output", convertStackInfoToMap(recipe.craftResult));
		table.put("input", convertRecipeToMap(recipe));
		return table;
	}

	@SuppressWarnings("unchecked")
	private static CraftingRecipe convertToCraftingRecipe(final Map<?, ?> table) {
		final CraftingRecipe recipe = new CraftingRecipe();
		if (table.containsKey("output")) {
			final Map<String, Double> outputTable = (Map<String, Double>) table.get("output");
			final int uuid = outputTable.get("uuid").intValue();
			final int count = outputTable.get("count").intValue();
			recipe.craftResult = new StackInfo(uuid, count);
		}
		if (table.containsKey("input")) {
			final Map<Double, Map<String, Double>> inputTable = (Map<Double, Map<String, Double>>) table.get("input");
			for (final Entry<Double, Map<String, Double>> entry : inputTable.entrySet()) {
				final Map<String, Double> itemTable = entry.getValue();
				final int uuid = itemTable.get("uuid").intValue();
				final int count = itemTable.get("count").intValue();
				final StackInfo itemStack = new StackInfo(uuid, count);
				recipe.craftMatrix[entry.getKey().intValue()] = itemStack;
			}
		}
		return recipe;
	}

	private static Map<String, Integer> convertTankInfoToMap(final TankInfo object) {
		final Map<String, Integer> map = new HashMap<String, Integer>();
		map.put("fluidId", object.fluidId);
		map.put("amount", object.amount);
		map.put("capacity", object.capacity);
		return map;
	}

	private static Map<String, Integer> convertStackInfoToMap(final StackInfo object) {
		final Map<String, Integer> map = new HashMap<String, Integer>();
		map.put("uuid", object.uuid);
		map.put("size", object.size);
		return map;
	}

	private static Map<Integer, Map<String, Integer>> convertRecipeToMap(final Recipe recipe) {
		final Map<Integer, Map<String, Integer>> inputTable = new HashMap<Integer, Map<String, Integer>>();
		for (int i = 0; i < recipe.craftMatrix.length; i++) {
			if (recipe.craftMatrix[i] != null) {
				inputTable.put(i, convertStackInfoToMap(recipe.craftMatrix[i]));
			}
		}
		return inputTable;
	}

	private static Recipe convertToRecipe(final Map<Double, Map<String, Double>> table) {
		final Recipe recipe = new Recipe();
		for (final Entry<Double, Map<String, Double>> entry : table.entrySet()) {
			final Map<String, Double> itemTable = entry.getValue();
			final int uuid = itemTable.get("uuid").intValue();
			final int count = itemTable.get("count").intValue();
			final StackInfo itemStack = new StackInfo(uuid, count);
			recipe.craftMatrix[entry.getKey().intValue()] = itemStack;
		}
		return recipe;
	}

	private static Map<String, Integer> convertTargetToMap(final Target target) {
		final Map<String, Integer> map = new HashMap<String, Integer>();
		map.put("x", target.position.x);
		map.put("y", target.position.y);
		map.put("z", target.position.z);
		map.put("dimension", target.dimension);
		return map;
	}
}
