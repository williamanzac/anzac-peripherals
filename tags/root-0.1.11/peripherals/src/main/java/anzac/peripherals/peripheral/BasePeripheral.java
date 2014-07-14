package anzac.peripherals.peripheral;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.ArrayUtils;

import anzac.peripherals.AnzacPeripheralsCore;
import anzac.peripherals.annotations.Peripheral;
import anzac.peripherals.annotations.PeripheralMethod;
import anzac.peripherals.tiles.BasePeripheralTileEntity;
import anzac.peripherals.tiles.PeripheralEvent;
import anzac.peripherals.utils.ClassUtils;
import dan200.computercraft.api.lua.ILuaContext;
import dan200.computercraft.api.peripheral.IComputerAccess;
import dan200.computercraft.api.peripheral.IPeripheral;
import dan200.computercraft.api.turtle.ITurtleAccess;

public abstract class BasePeripheral implements IPeripheral {

	protected final Set<IComputerAccess> computers = new HashSet<IComputerAccess>();

	protected BasePeripheralTileEntity entity;
	protected ITurtleAccess turtle;

	public BasePeripheral(final BasePeripheralTileEntity entity) {
		this.entity = entity;
	}

	protected abstract BasePeripheralTileEntity getEntity();

	@Override
	public String[] getMethodNames() {
		return methodNames().toArray(new String[0]);
	}

	protected abstract List<String> methodNames();

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

		return ClassUtils.callPeripheralMethod(this, methodName, arguments);
	}

	@Override
	public void attach(final IComputerAccess computer) {
		AnzacPeripheralsCore.addPeripheralLabel(computer.getID(), getLabel(), entity);
		computers.add(computer);
	}

	@Override
	public void detach(final IComputerAccess computer) {
		AnzacPeripheralsCore.removePeripheralLabel(computer.getID(), getLabel());
		computers.remove(computer);
	}

	public boolean isConnected() {
		return !computers.isEmpty();
	}

	/**
	 * Get the label for this peripheral
	 * 
	 * @return the label
	 */
	@PeripheralMethod
	public String getLabel() {
		return entity.getLabel();
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
		entity.setLabel(label);
		for (final IComputerAccess computer : computers) {
			AnzacPeripheralsCore.addPeripheralLabel(computer.getID(), label, entity);
		}
	}

	public void queueEvent(final String event, final Object... parameters) {
		for (final IComputerAccess computer : computers) {
			final Object[] clone = ArrayUtils.clone(parameters);
			ArrayUtils.add(clone, 0, computer.getAttachmentName());
			computer.queueEvent(event, clone);
		}
	}

	public void queueEvent(final PeripheralEvent event, final Object... parameters) {
		event.fire(computers, parameters);
	}

	@Override
	public boolean equals(IPeripheral other) {
		if (other instanceof BasePeripheral) {
			return entity.equals(((BasePeripheral) other).entity);
		}
		return false;
	}
}