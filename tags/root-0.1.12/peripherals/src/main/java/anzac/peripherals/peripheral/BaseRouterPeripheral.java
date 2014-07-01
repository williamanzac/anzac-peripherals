package anzac.peripherals.peripheral;

import java.util.Map;

import net.minecraftforge.common.ForgeDirection;
import anzac.peripherals.annotations.PeripheralMethod;
import anzac.peripherals.tiles.BaseRouterTileEntity;

public abstract class BaseRouterPeripheral extends BasePeripheral {

	public BaseRouterPeripheral(final BaseRouterTileEntity entity) {
		super(entity);
	}

	@Override
	protected BaseRouterTileEntity getEntity() {
		return (BaseRouterTileEntity) entity;
	}

	/**
	 * @param side
	 * @return table
	 */
	@PeripheralMethod
	public Map<String, Map<String, Object>> getAvailableTriggers(final ForgeDirection side) {
		return getEntity().getAvailableTriggers(side);
	}

	/**
	 * @param name
	 * @param uuid
	 * @param side
	 * @throws Exception
	 */
	@PeripheralMethod
	public void addTrigger(final String name, final int uuid, final ForgeDirection side) throws Exception {
		getEntity().addTrigger(name, uuid, side);
	}

	/**
	 * @param name
	 * @param uuid
	 * @param side
	 * @throws Exception
	 */
	@PeripheralMethod
	public void removeTrigger(final String name, final int uuid, final ForgeDirection side) throws Exception {
		getEntity().removeTrigger(name, uuid, side);
	}
}
