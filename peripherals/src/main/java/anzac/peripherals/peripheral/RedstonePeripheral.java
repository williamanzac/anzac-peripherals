package anzac.peripherals.peripheral;

import java.util.List;

import net.minecraftforge.common.ForgeDirection;
import anzac.peripherals.annotations.Peripheral;
import anzac.peripherals.annotations.PeripheralMethod;
import anzac.peripherals.tiles.RedstoneTileEntity;
import anzac.peripherals.utils.ClassUtils;

@Peripheral(type = "Redstone", hasGUI = false)
public class RedstonePeripheral extends BasePeripheral {

	public RedstonePeripheral(RedstoneTileEntity entity) {
		super(entity);
	}

	@Override
	protected RedstoneTileEntity getEntity() {
		return (RedstoneTileEntity) entity;
	}

	@Override
	protected List<String> methodNames() {
		return ClassUtils.getMethodNames(RedstonePeripheral.class);
	}

	/**
	 * @param side
	 * @param on
	 */
	@PeripheralMethod
	public void setOutput(final ForgeDirection side, final boolean on) {
		setAnalogOutput(side, on ? 15 : 0);
	}

	/**
	 * @param side
	 * @return
	 */
	@PeripheralMethod
	public boolean getOutput(final ForgeDirection side) {
		return getAnalogInput(side) > 0;
	}

	/**
	 * @param side
	 * @return
	 */
	@PeripheralMethod
	public boolean getInput(final ForgeDirection side) {
		return getAnalogInput(side) > 0;
	}

	/**
	 * @param side
	 * @param value
	 */
	@PeripheralMethod
	public void setBundledOutput(final ForgeDirection side, final int value) {
		getEntity().setBundledOutput(side, value);
	}

	/**
	 * @param side
	 * @return
	 */
	@PeripheralMethod
	public int getBundledOutput(final ForgeDirection side) {
		return getEntity().getBundledOutput(side);
	}

	/**
	 * @param side
	 * @return
	 */
	@PeripheralMethod
	public int getBundledInput(final ForgeDirection side) {
		return getEntity().getBundledInput(side);
	}

	/**
	 * @param side
	 * @param mask
	 * @return
	 */
	@PeripheralMethod
	public boolean testBundledInput(final ForgeDirection side, final int mask) {
		return getEntity().testBundledInput(side, mask);
	}

	/**
	 * @param side
	 * @param value
	 */
	@PeripheralMethod
	public void setAnalogOutput(final ForgeDirection side, final int value) {
		getEntity().setAnalogOutput(side, value);
	}

	/**
	 * @param side
	 * @param value
	 */
	@PeripheralMethod
	public void setAnalogueOutput(final ForgeDirection side, final int value) {
		setAnalogOutput(side, value);
	}

	/**
	 * @param side
	 * @return
	 */
	@PeripheralMethod
	public int getAnalogOutput(final ForgeDirection side) {
		return getEntity().getAnalogOutput(side);
	}

	/**
	 * @param side
	 * @return
	 */
	@PeripheralMethod
	public int getAnalogueOutput(final ForgeDirection side) {
		return getAnalogOutput(side);
	}

	/**
	 * @param side
	 * @return
	 */
	@PeripheralMethod
	public int getAnalogInput(final ForgeDirection side) {
		return getEntity().getAnalogInput(side);
	}

	/**
	 * @param side
	 * @param value
	 * @return
	 */
	@PeripheralMethod
	public int getAnalogueInput(final ForgeDirection side, final int value) {
		return getAnalogInput(side);
	}
}
