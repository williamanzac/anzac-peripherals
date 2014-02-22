package anzac.peripherals.tiles;

import net.minecraftforge.common.ForgeDirection;
import anzac.peripherals.annotations.PeripheralMethod;

public abstract class BaseRouterTileEntity extends BasePeripheralTileEntity {

	@Override
	protected boolean requiresMount() {
		return false;
	}

	@PeripheralMethod
	public final Object contents() throws Exception {
		return contents(ForgeDirection.UNKNOWN);
	}

	@PeripheralMethod
	public final Object contents(final ForgeDirection direction) throws Exception {
		return contents(direction, direction.getOpposite());
	}

	@PeripheralMethod
	public abstract Object contents(final ForgeDirection direction, final ForgeDirection dir) throws Exception;

	@PeripheralMethod
	public final Object extractFrom(final ForgeDirection fromDir, final int uuid, final int amount) throws Exception {
		return extractFrom(fromDir, uuid, amount, fromDir.getOpposite());
	}

	@PeripheralMethod
	public abstract Object extractFrom(final ForgeDirection fromDir, final int uuid, final int amount,
			final ForgeDirection extractSide) throws Exception;

	@PeripheralMethod
	public final int routeTo(final ForgeDirection toDir, final int amount) throws Exception {
		return routeTo(toDir, toDir.getOpposite(), amount);
	}

	@PeripheralMethod
	public abstract int routeTo(final ForgeDirection toDir, final ForgeDirection insertDir, final int amount)
			throws Exception;

	@PeripheralMethod
	public abstract int sendTo(final String label, final int amount) throws Exception;
}
