package anzac.peripherals.peripheral;

import java.util.List;
import java.util.Map;

import anzac.peripherals.annotations.Peripheral;
import anzac.peripherals.annotations.PeripheralMethod;
import anzac.peripherals.tiles.FluidStorageTileEntity;
import anzac.peripherals.utils.ClassUtils;

@Peripheral(type = "FluidStorage")
public class FluidStoragePeripheral extends BaseStoragePeripheral {

	public FluidStoragePeripheral(final FluidStorageTileEntity entity) {
		super(entity);
	}

	@Override
	protected List<String> methodNames() {
		return ClassUtils.getMethodNames(FluidStoragePeripheral.class);
	}

	@Override
	protected FluidStorageTileEntity getEntity() {
		return (FluidStorageTileEntity) super.getEntity();
	}

	/**
	 * @return
	 * @throws Exception
	 */
	@PeripheralMethod
	public Map<Integer, Map<String, Integer>> contents() throws Exception {
		return getEntity().contents();
	}
}
