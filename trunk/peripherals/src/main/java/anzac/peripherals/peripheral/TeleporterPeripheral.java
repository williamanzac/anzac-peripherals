package anzac.peripherals.peripheral;

import java.util.List;

import anzac.peripherals.annotations.Peripheral;
import anzac.peripherals.annotations.PeripheralMethod;
import anzac.peripherals.tiles.TeleporterTileEntity;
import anzac.peripherals.tiles.TeleporterTileEntity.Target;
import anzac.peripherals.utils.ClassUtils;

@Peripheral(type = "Teleporter")
public class TeleporterPeripheral extends BasePeripheral {

	public TeleporterPeripheral(final TeleporterTileEntity entity) {
		super(entity);
	}

	@Override
	protected TeleporterTileEntity getEntity() {
		return (TeleporterTileEntity) entity;
	}

	@Override
	protected List<String> methodNames() {
		return ClassUtils.getMethodNames(TeleporterPeripheral.class);
	}

	/**
	 * Get the amount of stored energy inside of this peripheral.
	 * 
	 * @return Returns how many units of fuel are currently stored in it.
	 */
	@PeripheralMethod
	public float getStoredEnergy() {
		return getEntity().getStoredEnergy();
	}

	/**
	 * Get the maximum amount of energy that can be stored inside of this peripheral.
	 * 
	 * @return Returns how many units of fuel can be stored in it.
	 */
	@PeripheralMethod
	public float getMaxEnergy() {
		return getEntity().getMaxEnergy();
	}

	/**
	 * Get a list of the currently configured targets.
	 * 
	 * @return Returns an array of teleporter targets.
	 */
	@PeripheralMethod
	public Target[] getTargets() {
		return getEntity().getTargets();
	}

	/**
	 * Teleport a turtle next to this peripheral to the specified target.
	 * 
	 * @param index
	 *            The numerical index of the target in the array.
	 * @throws Exception
	 *             Returns an error if the destination is blocked or invalid, there are no turtles next to it or if
	 *             there is not enough power.
	 */
	@PeripheralMethod
	public void teleport(final int index) throws Exception {
		getEntity().teleport(index);
	}
}
