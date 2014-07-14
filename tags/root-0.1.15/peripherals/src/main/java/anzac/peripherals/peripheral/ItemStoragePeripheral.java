package anzac.peripherals.peripheral;

import java.util.List;

import anzac.peripherals.annotations.Peripheral;
import anzac.peripherals.annotations.PeripheralMethod;
import anzac.peripherals.tiles.ItemRouterTileEntity.StackInfo;
import anzac.peripherals.tiles.ItemStorageTileEntity;
import anzac.peripherals.utils.ClassUtils;

@Peripheral(type = "ItemStorage")
public class ItemStoragePeripheral extends BaseStoragePeripheral {

	public ItemStoragePeripheral(ItemStorageTileEntity entity) {
		super(entity);
	}

	@Override
	protected ItemStorageTileEntity getEntity() {
		return (ItemStorageTileEntity) super.getEntity();
	}

	@Override
	protected List<String> methodNames() {
		return ClassUtils.getMethodNames(ItemStoragePeripheral.class);
	}

	/**
	 * @return
	 * @throws Exception
	 */
	@PeripheralMethod
	public StackInfo[] contents() throws Exception {
		return getEntity().contents();
	}

	/**
	 * @return
	 */
	@PeripheralMethod
	public boolean isUseOreDict() {
		return getEntity().isUseOreDict();
	}

	/**
	 * @param useOreDict
	 */
	@PeripheralMethod
	public void setUseOreDict(final boolean useOreDict) {
		getEntity().setUseOreDict(useOreDict);
	}

	/**
	 * @return
	 */
	@PeripheralMethod
	public boolean isIgnoreMeta() {
		return getEntity().isIgnoreMeta();
	}

	/**
	 * @param ignoreMeta
	 */
	@PeripheralMethod
	public void setIgnoreMeta(final boolean ignoreMeta) {
		getEntity().setIgnoreMeta(ignoreMeta);
	}
}
