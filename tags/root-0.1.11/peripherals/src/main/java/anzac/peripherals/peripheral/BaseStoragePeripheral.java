package anzac.peripherals.peripheral;

import anzac.peripherals.annotations.PeripheralMethod;
import anzac.peripherals.tiles.BaseStorageTileEntity;
import anzac.peripherals.tiles.BaseStorageTileEntity.FilterMode;

public abstract class BaseStoragePeripheral extends BasePeripheral {

	public BaseStoragePeripheral(final BaseStorageTileEntity entity) {
		super(entity);
	}

	@Override
	protected BaseStorageTileEntity getEntity() {
		return (BaseStorageTileEntity) entity;
	}

	/**
	 * @return
	 * @throws Exception
	 */
	@PeripheralMethod
	public Integer[] listFilter() throws Exception {
		return getEntity().listFilter();
	}

	/**
	 * @param id
	 * @throws Exception
	 */
	@PeripheralMethod
	public void removeFilter(final int id) throws Exception {
		getEntity().removeFilter(id);
	}

	/**
	 * @param id
	 * @throws Exception
	 */
	@PeripheralMethod
	public void addFilter(final int id) throws Exception {
		getEntity().addFilter(id);
	}

	/**
	 * @return
	 */
	@PeripheralMethod
	public FilterMode getFilterMode() {
		return getEntity().getFilterMode();
	}

	/**
	 * @param mode
	 */
	@PeripheralMethod
	public void setFilterMode(final FilterMode mode) {
		getEntity().setFilterMode(mode);
	}
}
