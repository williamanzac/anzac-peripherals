package anzac.peripherals.utils;

import static anzac.peripherals.utils.TypeConverter.convertArguments;
import static anzac.peripherals.utils.TypeConverter.convertReturn;
import static java.lang.reflect.Modifier.isAbstract;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import anzac.peripherals.annotations.PeripheralMethod;
import dan200.computercraft.api.peripheral.IPeripheral;

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

	@SuppressWarnings("unchecked")
	public static <T> T getField(final Object object, final String name, final Class<T> type) {
		Class<?> clazz = object.getClass();
		do {
			try {
				final Field field = clazz.getDeclaredField(name);
				field.setAccessible(true);
				return (T) field.get(object);
			} catch (final Throwable e) {
			}
			clazz = clazz.getSuperclass();
		} while (clazz != null);
		return null;
	}

	public static <T> void setField(final String classname, final String name, final T value) {
		try {
			Class<?> clazz = getClass(classname);
			do {
				try {
					final Field field = clazz.getDeclaredField(name);
					field.setAccessible(true);
					field.set(null, value);
					return;
				} catch (final Throwable e) {
				}
				clazz = clazz.getSuperclass();
			} while (clazz != null);
		} catch (final ClassNotFoundException e1) {
			e1.printStackTrace();
		}
	}

	public static <T> void setField(final Object object, final String name, final T value) {
		// AnzacPeripheralsCore.logger.info("setting field " + name + " for object " + object + " to " + value);
		Class<?> clazz = object.getClass();
		// AnzacPeripheralsCore.logger.info("clazz: " + clazz);
		do {
			try {
				final Field field = clazz.getDeclaredField(name);
				// AnzacPeripheralsCore.logger.info("field: " + field);
				field.setAccessible(true);
				field.set(object, value);
				// AnzacPeripheralsCore.logger.info("set field");
				return;
			} catch (final Throwable e) {
			}
			clazz = clazz.getSuperclass();
			// AnzacPeripheralsCore.logger.info("parent clazz: " + clazz);
		} while (clazz != null);
	}

	@SuppressWarnings("rawtypes")
	private static Class[] argsToTypes(final Object[] args) {
		if (args == null) {
			return new Class[0];
		}
		final Class[] classes = new Class[args.length];
		for (int i = 0; i < args.length; i++) {
			classes[i] = args[i].getClass();
		}
		return classes;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static <R> R callMethod(final String classname, final String name, final Object[] args) {
		try {
			Class<?> clazz = getClass(classname);
			final Class[] classes = argsToTypes(args);
			do {
				try {
					final Method method = clazz.getDeclaredMethod(name, classes);
					method.setAccessible(true);
					return (R) method.invoke(null, args);
				} catch (final Throwable e) {
				}
				clazz = clazz.getSuperclass();
			} while (clazz != null);
		} catch (final ClassNotFoundException e1) {
			e1.printStackTrace();
		}
		return null;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static <R> R callMethod(final String classname, final String name, final Object[] args, final Class[] types) {
		try {
			Class<?> clazz = getClass(classname);
			do {
				try {
					final Method method = clazz.getDeclaredMethod(name, types);
					method.setAccessible(true);
					return (R) method.invoke(null, args);
				} catch (final Throwable e) {
				}
				clazz = clazz.getSuperclass();
			} while (clazz != null);
		} catch (final ClassNotFoundException e1) {
			e1.printStackTrace();
		}
		return null;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static <R> R callMethod(final Object object, final String name, final Object[] args) {
		// AnzacPeripheralsCore.logger.info("calling method");
		Class<?> clazz = object.getClass();
		// AnzacPeripheralsCore.logger.info("clazz:" + clazz);
		final Class[] classes = argsToTypes(args);
		do {
			try {
				// AnzacPeripheralsCore.logger.info("clazz: " + clazz);
				// AnzacPeripheralsCore.logger.info(Arrays.toString(clazz.getDeclaredMethods()));
				final Method method = clazz.getDeclaredMethod(name, classes);
				// AnzacPeripheralsCore.logger.info("method:" + method);
				method.setAccessible(true);
				// AnzacPeripheralsCore.logger.info("invoking and returning");
				return (R) method.invoke(object, args);
			} catch (final Throwable e) {
				e.printStackTrace();
			}
			clazz = clazz.getSuperclass();
			// AnzacPeripheralsCore.logger.info("parent clazz:" + clazz);
		} while (clazz != null);
		// AnzacPeripheralsCore.logger.info("returning null");
		return null;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static <R> R callMethod(final Object object, final String name, final Object[] args, final Class[] types) {
		// AnzacPeripheralsCore.logger.info("calling method");
		Class<?> clazz = object.getClass();
		// AnzacPeripheralsCore.logger.info("clazz:" + clazz);
		do {
			try {
				final Method method = clazz.getDeclaredMethod(name, types);
				// AnzacPeripheralsCore.logger.info("method:" + method);
				method.setAccessible(true);
				// AnzacPeripheralsCore.logger.info("invoking and returning");
				return (R) method.invoke(object, args);
			} catch (final Throwable e) {
			}
			clazz = clazz.getSuperclass();
			// AnzacPeripheralsCore.logger.info("parent clazz:" + clazz);
		} while (clazz != null);
		// AnzacPeripheralsCore.logger.info("returning null");
		return null;
	}

	public static boolean instanceOf(final Object object, final String className) {
		if (object == null) {
			return false;
		}
		try {
			final Class<?> clazz = getClass(className);
			return clazz.isAssignableFrom(object.getClass());
		} catch (final ClassNotFoundException e) {
			e.printStackTrace();
		}
		return false;
	}

	private static final Map<Class<?>, List<Method>> classMethods = new HashMap<Class<?>, List<Method>>();

	private static <T extends IPeripheral> List<Method> getPeripheralMethods(final Class<T> pClass) {
		if (!classMethods.containsKey(pClass)) {
			final List<Method> methods = new ArrayList<Method>();
			classMethods.put(pClass, methods);
			for (final Method method : pClass.getMethods()) {
				if (method.isAnnotationPresent(PeripheralMethod.class) && !isAbstract(method.getModifiers())) {
					methods.add(method);
				}
			}
		}
		return classMethods.get(pClass);
	}

	public static <T extends IPeripheral> List<String> getMethodNames(final Class<T> pClass) {
		final List<String> methods = new ArrayList<String>();
		for (final Method method : getPeripheralMethods(pClass)) {
			if (!methods.contains(method.getName())) {
				methods.add(method.getName());
			}
		}
		return methods;
	}

	public static <T extends IPeripheral> Method getMethodByName(final Class<T> pClass, final String name,
			final int argCount) throws Exception {
		for (final Method method : getPeripheralMethods(pClass)) {
			if (method.getName().equals(name) && method.getParameterTypes().length == argCount) {
				return method;
			}
		}
		throw new Exception("Unable to find a method called " + name + " with " + argCount + " arguments");
	}

	public static Object[] callPeripheralMethod(final IPeripheral object, final String methodName,
			final Object[] arguments) throws Exception {
		final Method method = getMethodByName(object.getClass(), methodName, arguments.length);
		method.setAccessible(true);
		final Object[] parameters = convertArguments(arguments, method);
		try {
			final Object ret = method.invoke(object, parameters);
			return convertReturn(ret, method.getReturnType());
		} catch (final InvocationTargetException e) {
			throw (Exception) e.getCause();
		}
	}
}
