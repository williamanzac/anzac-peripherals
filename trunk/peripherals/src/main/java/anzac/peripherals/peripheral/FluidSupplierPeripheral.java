package anzac.peripherals.peripheral;

import java.util.List;

import net.minecraftforge.common.ForgeDirection;
import anzac.peripherals.annotations.Peripheral;
import anzac.peripherals.annotations.PeripheralMethod;
import anzac.peripherals.tiles.FluidRouterTileEntity.TankInfo;
import anzac.peripherals.tiles.FluidSupplierTileEntity;
import anzac.peripherals.tiles.PeripheralEvent;
import anzac.peripherals.tiles.TeleporterTileEntity.Target;
import anzac.peripherals.utils.ClassUtils;

@Peripheral(type = "FluidSupplier", events = { PeripheralEvent.fluid_route })
public class FluidSupplierPeripheral extends BaseRouterPeripheral {

	public FluidSupplierPeripheral(FluidSupplierTileEntity entity) {
		super(entity);
	}

	@Override
	protected List<String> methodNames() {
		return ClassUtils.getMethodNames(FluidSupplierPeripheral.class);
	}

	@Override
	protected FluidSupplierTileEntity getEntity() {
		return (FluidSupplierTileEntity) super.getEntity();
	}

	/**
	 * Transfer {@code amount} amount of fluid from the internal tank to the tanks connected on {@code toDir} side.
	 * 
	 * @param toDir
	 *            the side the tanks are connected to.
	 * @param amount
	 *            the amount of fluid to transfer.
	 * @return the actual amount transferred.
	 * @throws Exception
	 */
	@PeripheralMethod
	public int routeTo(final ForgeDirection toDir, final int amount) throws Exception {
		return routeTo(toDir, toDir.getOpposite(), amount);
	}

	/**
	 * Transfer {@code amount} amount of fluid from the internal tank to the {@code side} side of the tanks connected on
	 * {@code toDir} side.
	 * 
	 * @param toDir
	 *            the side the tanks are connected to.
	 * @param insertDir
	 *            the side the tank to insert the fluid from.
	 * @param amount
	 *            the amount of fluid to transfer.
	 * @return the actual amount transferred.
	 * @throws Exception
	 */
	@PeripheralMethod
	public int routeTo(final ForgeDirection toDir, final ForgeDirection insetDir, final int amount) throws Exception {
		return getEntity().routeTo(toDir, insetDir, amount);
	}

	/**
	 * Will return a table containing the uuid and count of the fluid in the internal tank.
	 * 
	 * @return A table of the internal contents.
	 * @throws Exception
	 */
	@PeripheralMethod
	public TankInfo[] contents() throws Exception {
		return contents(ForgeDirection.UNKNOWN);
	}

	/**
	 * Will return a table containing the uuid and count of each fluid in the tanks connected to {@code direction} side
	 * of this block.
	 * 
	 * @param direction
	 *            which side of this block to examine the tanks of.
	 * @return A table of the contents of the connected tanks.
	 * @throws Exception
	 */
	@PeripheralMethod
	public TankInfo[] contents(final ForgeDirection direction) throws Exception {
		return contents(direction, direction.getOpposite());
	}

	/**
	 * Will return a table containing the uuid and count of each fluid in the tanks connected to <code>direction</code>
	 * side of this block and limited the to those tanks accessible from <code>side</code> side.
	 * 
	 * @param direction
	 *            which side of this block to examine the tanks of.
	 * @param dir
	 *            which side of the tanks to examine.
	 * @return A table of the contents of the connected tanks.
	 * @throws Exception
	 */
	@PeripheralMethod
	public TankInfo[] contents(final ForgeDirection direction, final ForgeDirection dir) throws Exception {
		return getEntity().contents(direction, dir);
	}

	/**
	 * Extract {@code amount} amount of fluid with {@code uuid} from the tanks connected to {@code fromDir} side.
	 * 
	 * @param fromDir
	 *            which side of this block to extract from.
	 * @param uuid
	 *            the uuid of the fluid to extract.
	 * @param amount
	 *            the amount of the fluid to extract.
	 * @return The actual amount extracted.
	 * @throws Exception
	 */
	@PeripheralMethod
	public int extractFrom(final ForgeDirection fromDir, final int uuid, final int amount) throws Exception {
		return extractFrom(fromDir, uuid, amount, fromDir.getOpposite());
	}

	/**
	 * Extract {@code amount} amount of fluid with {@code uuid} from the {@code side} side of the tanks connected to
	 * {@code fromDir} side.
	 * 
	 * @param fromDir
	 *            which side of this block to extract from.
	 * @param uuid
	 *            the uuid of the fluid to extract.
	 * @param amount
	 *            the amount of fluid to extract.
	 * @param extractSide
	 *            which side of the tanks to extract from.
	 * @return The actual amount extracted.
	 * @throws Exception
	 */
	@PeripheralMethod
	public int extractFrom(final ForgeDirection fromDir, final int uuid, final int amount,
			final ForgeDirection extractSide) throws Exception {
		return getEntity().extractFrom(fromDir, uuid, amount, extractSide);
	}

	/**
	 * Transfer {@code amount} amount of fluid from the internal tank to another connected peripheral with {@code label}
	 * label. The peripheral must be connected to the same computer.
	 * 
	 * @param label
	 *            the label of the peripheral.
	 * @param amount
	 *            the amount of fluid to transfer.
	 * @return the actual amount transferred.
	 * @throws Exception
	 */
	@PeripheralMethod
	public int sendTo(final String label, final int amount) throws Exception {
		return getEntity().sendTo(label, amount);
	}

	/**
	 * Transfer {@code amount} amount of fluid from another connected peripheral with {@code label} label to the
	 * internal tank. The peripheral must be connected to the same computer.
	 * 
	 * @param label
	 *            the label of the peripheral.
	 * @param uuid
	 *            the uuid of the fluid to transfer.
	 * @param amount
	 *            the amount of fluid to transfer.
	 * @return the actual amount transferred.
	 * @throws Exception
	 */
	@PeripheralMethod
	public int requestFrom(String label, int uuid, int amount) throws Exception {
		return getEntity().requestFrom(label, uuid, amount);
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
	 * Get the currently configured target.
	 * 
	 * @return The target for this supplier.
	 */
	@PeripheralMethod
	public Target getTarget() {
		return getEntity().getTarget();
	}
}
