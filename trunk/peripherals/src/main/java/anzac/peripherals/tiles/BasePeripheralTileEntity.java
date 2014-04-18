package anzac.peripherals.tiles;

import static java.lang.reflect.Modifier.isAbstract;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.packet.Packet;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.ForgeDirection;
import anzac.peripherals.AnzacPeripheralsCore;
import anzac.peripherals.annotations.Peripheral;
import anzac.peripherals.annotations.PeripheralMethod;
import anzac.peripherals.network.PacketHandler;
import dan200.computer.api.IComputerAccess;
import dan200.computer.api.ILuaContext;
import dan200.computer.api.IMount;
import dan200.computer.api.IPeripheral;

public abstract class BasePeripheralTileEntity extends TileEntity implements IPeripheral {

	protected final Set<IComputerAccess> computers = new HashSet<IComputerAccess>();

	public SimpleDiscInventory discInv = new SimpleDiscInventory(this);

	@Override
	public String[] getMethodNames() {
		return methodNames().toArray(new String[0]);
	}

	protected List<String> methodNames() {
		return getMethodNames(BasePeripheralTileEntity.class);
	}

	@Override
	public final String getType() {
		final Peripheral annotation = getClass().getAnnotation(Peripheral.class);
		final String type = annotation.type();
		return type;
	}

	@Override
	public Object[] callMethod(final IComputerAccess computer, final ILuaContext context, final int method,
			final Object[] arguments) throws Exception {
		final String methodName = getMethodNames()[method];

		final Object ret = callMethod(methodName, arguments);

		if (ret == null) {
			return null;
		}
		if (ret.getClass().isArray()) {
			return (Object[]) ret;
		}
		return new Object[] { ret };
	}

	private Object callMethod(final String methodName, final Object[] arguments) throws Exception {
		final Method method = getMethodByName(getClass(), methodName, arguments.length);
		method.setAccessible(true);
		final Object[] parameters = new Object[arguments.length];
		for (int i = 0; i < arguments.length; i++) {
			parameters[i] = convertArgument(arguments[i], method.getParameterTypes()[i]);
		}
		try {
			return method.invoke(this, parameters);
		} catch (final InvocationTargetException e) {
			throw (Exception) e.getCause();
		}
	}

	private Object convertArgument(final Object object, final Class<?> toClass) throws Exception {
		if (toClass.isAssignableFrom(object.getClass())) {
			// no converting needed
			return object;
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
		}
		throw new Exception("Expected argument of type " + toClass.getName() + " got " + object.getClass());
	}

	@Override
	public boolean canAttachToSide(final int side) {
		return true;
	}

	@Override
	public void attach(final IComputerAccess computer) {
		AnzacPeripheralsCore.addPeripheralLabel(computer.getID(), getLabel(), this);
		computers.add(computer);
		worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
	}

	@Override
	public void detach(final IComputerAccess computer) {
		AnzacPeripheralsCore.removePeripheralLabel(computer.getID(), getLabel());
		computers.remove(computer);
		worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
	}

	protected boolean isConnected() {
		return !computers.isEmpty();
	}

	@Override
	public Packet getDescriptionPacket() {
		return PacketHandler.createTileEntityPacket("anzac", PacketHandler.ID_TILE_ENTITY, this);
	}

	@Override
	public void readFromNBT(final NBTTagCompound nbtTagCompound) {
		super.readFromNBT(nbtTagCompound);

		// read disc
		if (nbtTagCompound.hasKey("disc")) {
			discInv.readFromNBT(nbtTagCompound.getCompoundTag("disc"));
		}
	}

	@Override
	public void writeToNBT(final NBTTagCompound nbtTagCompound) {
		super.writeToNBT(nbtTagCompound);

		// write disc
		final NBTTagCompound discTag = new NBTTagCompound();
		discInv.writeToNBT(discTag);
		nbtTagCompound.setCompoundTag("disc", discTag);
	}

	public boolean hasLabel() {
		return discInv.hasLabel();
	}

	/**
	 * Get the label for this peripheral
	 * 
	 * @return the label
	 */
	@PeripheralMethod
	public String getLabel() {
		return discInv.getLabel();
	}

	/**
	 * Sets the label for this peripheral
	 * 
	 * @param label
	 *            The label to use
	 */
	@PeripheralMethod
	public void setLabel(final String label) {
		for (final IComputerAccess computer : computers) {
			AnzacPeripheralsCore.removePeripheralLabel(computer.getID(), getLabel());
		}
		discInv.setLabel(label);
		for (final IComputerAccess computer : computers) {
			AnzacPeripheralsCore.addPeripheralLabel(computer.getID(), label, this);
		}
	}

	protected IMount getMount() {
		return discInv.getMount();
	}

	private String convertToString(final Object argument) throws Exception {
		return argument.toString();
	}

	private int convertToInt(final Object argument) throws Exception {
		if (argument instanceof Number) {
			return ((Number) argument).intValue();
		} else if (argument instanceof String) {
			return Integer.parseInt((String) argument);
		}
		throw new Exception("Expected a Number");
	}

	private boolean convertToBoolean(final Object argument) throws Exception {
		if (argument instanceof Boolean) {
			return ((Boolean) argument).booleanValue();
		} else if (argument instanceof String) {
			return Boolean.parseBoolean((String) argument);
		}
		throw new Exception("Expected a Boolean");
	}

	private long convertToLong(final Object argument) throws Exception {
		if (argument instanceof Number) {
			return ((Number) argument).longValue();
		} else if (argument instanceof String) {
			return Long.parseLong((String) argument);
		}
		throw new Exception("Expected a Number");
	}

	private ForgeDirection convertToDirection(final Object argument) throws Exception {
		if (argument instanceof Number) {
			return ForgeDirection.getOrientation(((Number) argument).intValue());
		} else if (argument instanceof String) {
			return ForgeDirection.valueOf(((String) argument).toUpperCase());
		}
		throw new Exception("Expected a Direction");
	}

	@SuppressWarnings("unchecked")
	private <E extends Enum<?>> E convertToEnum(final Object argument, final Class<?> eClass) throws Exception {
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

	public static <T extends BasePeripheralTileEntity> List<Method> getPeripheralMethods(final Class<T> pClass) {
		final List<Method> methods = new ArrayList<Method>();
		for (final Method method : pClass.getMethods()) {
			if (method.isAnnotationPresent(PeripheralMethod.class)) {
				methods.add(method);
			}
		}
		return methods;
	}

	public static <T extends BasePeripheralTileEntity> List<String> getMethodNames(final Class<T> pClass) {
		final List<String> methods = new ArrayList<String>();
		for (final Method method : pClass.getMethods()) {
			if (method.isAnnotationPresent(PeripheralMethod.class) && !methods.contains(method.getName())) {
				methods.add(method.getName());
			}
		}
		return methods;
	}

	public static <T extends BasePeripheralTileEntity> Method getMethodByName(final Class<T> pClass, final String name,
			final int argCount) throws Exception {
		for (final Method method : pClass.getMethods()) {
			if (method.isAnnotationPresent(PeripheralMethod.class) && !isAbstract(method.getModifiers())
					&& method.getName().equals(name) && method.getParameterTypes().length == argCount) {
				return method;
			}
		}
		throw new Exception("Unable to find a method called " + name + " with " + argCount + " arguments");
	}
}
