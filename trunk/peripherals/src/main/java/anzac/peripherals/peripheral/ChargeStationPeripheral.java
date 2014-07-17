package anzac.peripherals.peripheral;

import java.util.List;

import anzac.peripherals.annotations.Peripheral;
import anzac.peripherals.annotations.PeripheralMethod;
import anzac.peripherals.tiles.ChargeStationTileEntity;
import anzac.peripherals.utils.ClassUtils;

@Peripheral(type = "ChargeStation")
public class ChargeStationPeripheral extends BasePeripheral {

	public ChargeStationPeripheral(final ChargeStationTileEntity entity) {
		super(entity);
	}

	@Override
	protected List<String> methodNames() {
		return ClassUtils.getMethodNames(ChargeStationPeripheral.class);
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

	@Override
	protected ChargeStationTileEntity getEntity() {
		return (ChargeStationTileEntity) entity;
	}
}
