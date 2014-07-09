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
	 * @return
	 */
	@PeripheralMethod
	public float getStoredEnergy() {
		return getEntity().getStoredEnergy();
	}

	/**
	 * @return
	 */
	@PeripheralMethod
	public float getMaxEnergy() {
		return getEntity().getMaxEnergy();
	}

	/**
	 * @return
	 */
	@PeripheralMethod
	public Target[] getTargets() {
		return getEntity().getTargets();
	}

	/**
	 * @param index
	 * @throws Exception
	 */
	@PeripheralMethod
	public void teleport(final int index) throws Exception {
		getEntity().teleport(index);
	}
}
