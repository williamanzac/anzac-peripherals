package anzac.peripherals.tiles;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.packet.Packet;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.ForgeDirection;
import anzac.peripherals.AnzacPeripheralsCore;
import anzac.peripherals.annotations.PeripheralMethod;
import anzac.peripherals.network.PacketHandler;
import dan200.computer.api.ComputerCraftAPI;
import dan200.computer.api.IComputerAccess;
import dan200.computer.api.ILuaContext;
import dan200.computer.api.IMount;
import dan200.computer.api.IPeripheral;

public abstract class BasePeripheralTileEntity extends TileEntity implements IPeripheral {

	private static final List<String> METHOD_NAMES = getMethodNames(BasePeripheralTileEntity.class);
	protected final Map<IComputerAccess, Computer> computers = new HashMap<IComputerAccess, Computer>();

	private class Computer {
		private final String name;

		public Computer(final String name) {
			this.name = name;
		}

		private String mount;
	}

	protected IMount mount;
	private int id = -1;
	protected String label;

	@Override
	public String[] getMethodNames() {
		return methodNames().toArray(new String[0]);
	}

	protected List<String> methodNames() {
		return METHOD_NAMES;
	}

	@Override
	public Object[] callMethod(final IComputerAccess computer, final ILuaContext context, final int method,
			final Object[] arguments) throws Exception {
		final String methodName = getMethodNames()[method];

		final Object ret = callMethod(methodName, arguments);

		if (ret.getClass().isArray()) {
			return (Object[]) ret;
		}
		return ret == null ? null : new Object[] { ret };
	}

	private Object callMethod(final String methodName, final Object[] arguments) throws Exception {
		final Method method = getMethodByName(getClass(), methodName, arguments.length);
		method.setAccessible(true);
		final Object[] parameters = new Object[arguments.length];
		for (int i = 0; i < arguments.length; i++) {
			parameters[i] = convertArgument(arguments[i], method.getParameterTypes()[i]);
		}
		return method.invoke(this, parameters);
	}

	private Object convertArgument(final Object object, final Class<?> toClass) throws Exception {
		if (toClass.isAssignableFrom(String.class)) {
			return convertToString(object);
		} else if (toClass.isAssignableFrom(Integer.class) || toClass.isAssignableFrom(int.class)) {
			return convertToInt(object);
		} else if (toClass.isAssignableFrom(ForgeDirection.class)) {
			return convertToDirection(object);
		}
		throw new Exception("Expected argument of type " + toClass.getName());
	}

	@Override
	public boolean canAttachToSide(final int side) {
		return true;
	}

	@Override
	public void attach(final IComputerAccess computer) {
		AnzacPeripheralsCore.computerPeripheralMap.put(computer.getID(), this);
		computers.put(computer, new Computer(computer.getAttachmentName()));
		createMount();
	}

	@Override
	public void detach(final IComputerAccess computer) {
		AnzacPeripheralsCore.computerPeripheralMap.remove(computer.getID());
		computers.remove(computer);
	}

	protected boolean isConnected() {
		return !computers.isEmpty();
	}

	@Override
	public Packet getDescriptionPacket() {
		return PacketHandler.createTileEntityPacket("anzac", PacketHandler.ID_TILE_ENTITY, this);
	}

	protected synchronized void createMount() {
		if (requiresMount()) {
			if (id < 0) {
				id = ComputerCraftAPI.createUniqueNumberedSaveDir(worldObj, "anzac/hdd");
			}
			mount = ComputerCraftAPI.createSaveDirMount(worldObj, "anzac/hdd/" + id, AnzacPeripheralsCore.storageSize);
		}
	}

	protected abstract boolean requiresMount();

	@Override
	public void readFromNBT(final NBTTagCompound nbtTagCompound) {
		super.readFromNBT(nbtTagCompound);

		// read id
		if (nbtTagCompound.hasKey("hddId")) {
			id = nbtTagCompound.getInteger("hddId");
		}
		if (id >= 0 && isConnected() && mount == null && requiresMount()) {
			createMount();
		}

		// read label
		if (nbtTagCompound.hasKey("label")) {
			label = nbtTagCompound.getString("label");
		}
	}

	@Override
	public void writeToNBT(final NBTTagCompound nbtTagCompound) {
		super.writeToNBT(nbtTagCompound);

		// write id
		if (requiresMount()) {
			nbtTagCompound.setInteger("hddId", id);
		}

		// write label
		if (label != null) {
			nbtTagCompound.setString("label", label);
		}
	}

	protected synchronized void unmountDisk() {
		mount = null;
		return;
	}

	public boolean hasLabel() {
		return label != null;
	}

	@PeripheralMethod
	public String getLabel() {
		return label;
	}

	@PeripheralMethod
	protected void setLabel(final String label) {
		this.label = label;
	}

	protected String convertToString(final Object argument) throws Exception {
		if (argument instanceof String) {
			return (String) argument;
		}
		throw new Exception("Expected a String");
	}

	protected int convertToInt(final Object argument) throws Exception {
		if (argument instanceof Number) {
			return ((Number) argument).intValue();
		} else if (argument instanceof String) {
			return Integer.parseInt(((String) argument).toUpperCase());
		}
		throw new Exception("Expected a Number");
	}

	protected ForgeDirection convertToDirection(final Object argument) throws Exception {
		if (argument instanceof Number) {
			return ForgeDirection.getOrientation(((Number) argument).intValue());
		} else if (argument instanceof String) {
			return ForgeDirection.valueOf(((String) argument).toUpperCase());
		}
		throw new Exception("Expected a Direction");
	}

	protected static <T extends BasePeripheralTileEntity> List<Method> getPeripheralMethods(final Class<T> pClass) {
		final List<Method> methods = new ArrayList<Method>();
		for (final Method method : pClass.getDeclaredMethods()) {
			if (method.isAnnotationPresent(PeripheralMethod.class)) {
				methods.add(method);
			}
		}
		return methods;
	}

	protected static <T extends BasePeripheralTileEntity> List<String> getMethodNames(final Class<T> pClass) {
		final List<String> methods = new ArrayList<String>();
		for (final Method method : pClass.getDeclaredMethods()) {
			if (method.isAnnotationPresent(PeripheralMethod.class) && !methods.contains(method.getName())) {
				methods.add(method.getName());
			}
		}
		return methods;
	}

	protected static <T extends BasePeripheralTileEntity> Method getMethodByName(final Class<T> pClass,
			final String name, final int argCount) throws Exception {
		for (final Method method : pClass.getDeclaredMethods()) {
			if (method.isAnnotationPresent(PeripheralMethod.class) && method.getName().equals(name)
					&& method.getParameterTypes().length == argCount) {
				return method;
			}
		}
		throw new Exception("Unable to find a method called " + name + " with " + argCount + " arguments");
	}
}
