package anzac.peripherals.utils;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

public class ClassUtils {

	private static final Map<String, Class<?>> classMap = new HashMap<String, Class<?>>();

	public static Class<?> getClass(final String classname) throws ClassNotFoundException {
		if (!classMap.containsKey(classname)) {
			final Class<?> clazz = Class.forName(classname);
			classMap.put(classname, clazz);
		}
		return classMap.get(classname);
	}

	@SuppressWarnings("unchecked")
	public static <T> T getField(final String classname, final String name, final Class<T> type) {
		try {
			Class<?> clazz = getClass(classname);
			do {
				try {
					final Field field = clazz.getDeclaredField(name);
					field.setAccessible(true);
					return (T) field.get(null);
				} catch (final Throwable e) {
				}
				clazz = clazz.getSuperclass();
			} while (clazz != null);
		} catch (final ClassNotFoundException e1) {
			e1.printStackTrace();
		}
		return null;
	}
}
