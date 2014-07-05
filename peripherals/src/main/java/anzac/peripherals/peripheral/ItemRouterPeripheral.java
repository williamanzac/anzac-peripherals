package anzac.peripherals.peripheral;

import java.util.List;

import net.minecraftforge.common.ForgeDirection;
import anzac.peripherals.annotations.Peripheral;
import anzac.peripherals.annotations.PeripheralMethod;
import anzac.peripherals.tiles.ItemRouterTileEntity;
import anzac.peripherals.tiles.ItemRouterTileEntity.StackInfo;
import anzac.peripherals.tiles.PeripheralEvent;
import anzac.peripherals.utils.ClassUtils;

@Peripheral(type = "ItemRouter", events = { PeripheralEvent.item_route })
public class ItemRouterPeripheral extends BaseRouterPeripheral {

	public ItemRouterPeripheral(final ItemRouterTileEntity entity) {
		super(entity);
	}

	@Override
	protected ItemRouterTileEntity getEntity() {
		return (ItemRouterTileEntity) super.getEntity();
	}

	@Override
	protected List<String> methodNames() {
		return ClassUtils.getMethodNames(ItemRouterPeripheral.class);
	}

	/**
	 * Will return a table containing the uuid and count of each item in the internal cache.
	 * 
	 * @return A table of the internal contents.
	 * @throws Exception
	 */
	@PeripheralMethod
	public StackInfo[] contents() throws Exception {
		return contents(ForgeDirection.UNKNOWN);
	}

	/**
	 * Will return a table containing the uuid and count of each item in the inventory connected to {@code direction}
	 * side of this block.
	 * 
	 * @param direction
	 *            which side of this block to examine the inventory of.
	 * @return A table of the contents of the connected inventory.
	 * @throws Exception
	 */
	@PeripheralMethod
	public StackInfo[] contents(final ForgeDirection direction) throws Exception {
		return contents(direction, direction.getOpposite());
	}

	/**
	 * Will return a table containing the uuid and count of each item in the inventory connected to {@code direction}
	 * side of this block and limited the examined slot to those accessible from {@code side} side.
	 * 
	 * @param direction
	 *            which side of this block to examine the inventory of.
	 * @param dir
	 *            which side of the inventory to examine.
	 * @return A table of the contents of the connected inventory.
	 * @throws Exception
	 */
	@PeripheralMethod
	public StackInfo[] contents(final ForgeDirection direction, final ForgeDirection dir) throws Exception {
		return getEntity().contents(direction, dir);
	}

	/**
	 * Extract {@code amount} number of items with {@code uuid} from the inventory connected to {@code fromDir} side.
	 * 
	 * @param fromDir
	 *            which side of this block to extract from.
	 * @param uuid
	 *            the uuid of the items to extract.
	 * @param amount
	 *            the number of items to extract.
	 * @return The actual number of items extracted.
	 * @throws Exception
	 */
	@PeripheralMethod
	public int extractFrom(final ForgeDirection fromDir, final int uuid, final int amount) throws Exception {
		return extractFrom(fromDir, uuid, amount, fromDir.getOpposite());
	}

	/**
	 * Extract {@code amount} number of items with {@code uuid} from the {@code side} side of the inventory connected to
	 * {@code fromDir} side.
	 * 
	 * @param fromDir
	 *            which side of this block to extract from.
	 * @param uuid
	 *            the uuid of the items to extract.
	 * @param amount
	 *            the number of items to extract.
	 * @param extractSide
	 *            which side of the inventory to extract from.
	 * @return The actual number of items extracted.
	 * @throws Exception
	 */
	@PeripheralMethod
	public int extractFrom(final ForgeDirection fromDir, final int uuid, final int amount,
			final ForgeDirection extractSide) throws Exception {
		return getEntity().extractFrom(fromDir, uuid, amount, extractSide);
	}

	/**
	 * Transfer {@code amount} number of items from the internal cache to the inventory connected on {@code toDir} side.
	 * 
	 * @param toDir
	 *            the side the inventory is connected to.
	 * @param amount
	 *            the number of items to transfer.
	 * @return the actual number of items transferred.
	 * @throws Exception
	 */
	@PeripheralMethod
	public int routeTo(final ForgeDirection toDir, final int amount) throws Exception {
		return routeTo(toDir, toDir.getOpposite(), amount);
	}

	/**
	 * Transfer {@code amount} number of items from the internal cache to the {@code side} side of the inventory
	 * connected on {@code toDir} side.
	 * 
	 * @param toDir
	 *            the side the inventory is connected to.
	 * @param insertDir
	 *            the side the inventory to insert the items from.
	 * @param amount
	 *            the number of items to transfer.
	 * @return the actual number of items transferred.
	 * @throws Exception
	 */
	@PeripheralMethod
	public int routeTo(final ForgeDirection toDir, final ForgeDirection insertDir, final int amount) {
		return getEntity().routeTo(toDir, insertDir, amount);
	}

	/**
	 * Transfer {@code amount} number of items from the internal cache to another connected peripheral with
	 * {@code label} label. The peripheral must be connected to the same computer.
	 * 
	 * @param label
	 *            the label of the peripheral.
	 * @param amount
	 *            the number of items to transfer.
	 * @return the actual number of items transferred.
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
	public int requestFrom(final String label, final int uuid, final int amount) throws Exception {
		return getEntity().requestFrom(label, uuid, amount);
	}
}
