package anzac.peripherals.tiles;

import java.util.List;

import net.minecraftforge.common.ForgeDirection;
import anzac.peripherals.annotations.Peripheral;
import anzac.peripherals.annotations.PeripheralMethod;
import anzac.peripherals.utils.Position;
import dan200.computer.api.IComputerAccess;

@Peripheral(type = "Redstone")
public class RedstoneTileEntity extends BasePeripheralTileEntity {

	private final int[] input = new int[6];
	private final int[] output = new int[6];

	@Override
	protected List<String> methodNames() {
		return getMethodNames(RedstoneTileEntity.class);
	}

	@PeripheralMethod
	public void setOutput(final ForgeDirection side, final boolean on) {
		setAnalogOutput(side, on ? 15 : 0);
	}

	@PeripheralMethod
	public boolean getOutput(final ForgeDirection side) {
		return getAnalogInput(side) > 0;
	}

	@PeripheralMethod
	public boolean getInput(final ForgeDirection side) {
		return getAnalogInput(side) > 0;
	}

	public void setBundledOutput(final ForgeDirection side, final int value) {
		worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
	}

	public int getBundledOutput(final ForgeDirection side) {
		return 0;
	}

	public int getBundledInput(final ForgeDirection side) {
		return 0;
	}

	public boolean testBundledInput(final ForgeDirection side, final int mask) {
		return (input[side.ordinal()] & mask) == mask;
	}

	@PeripheralMethod
	public void setAnalogOutput(final ForgeDirection side, final int value) {
		final int prev = output[side.ordinal()];
		output[side.ordinal()] = value;
		if (prev != value) {
			final Position p = new Position(xCoord, yCoord, zCoord, side);
			p.moveForwards(1);
			final int id = worldObj.getBlockId(xCoord, yCoord, zCoord);
			worldObj.notifyBlockOfNeighborChange(p.x, p.y, p.z, id);
			worldObj.notifyBlocksOfNeighborChange(p.x, p.y, p.z, id, side.ordinal());
		}
	}

	@PeripheralMethod
	public void setAnalogueOutput(final ForgeDirection side, final int value) {
		setAnalogOutput(side, value);
	}

	@PeripheralMethod
	public int getAnalogOutput(final ForgeDirection side) {
		return output[side.ordinal()];
	}

	@PeripheralMethod
	public int getAnalogueOutput(final ForgeDirection side) {
		return getAnalogOutput(side);
	}

	@PeripheralMethod
	public int getAnalogInput(final ForgeDirection side) {
		return input[side.ordinal()];
	}

	@PeripheralMethod
	public int getAnalogueInput(final ForgeDirection side, final int value) {
		return getAnalogInput(side);
	}

	// used by PeripheralBlock
	public int getOutput(final int side) {
		return output[side];
	}

	// used by PeripheralBlock
	public void setInput(final int side, final int inputStrength) {
		final int prev = input[side];
		input[side] = inputStrength;
		if (prev != inputStrength) {
			for (final IComputerAccess computer : computers) {
				computer.queueEvent("redstone", new Object[] { computer.getAttachmentName(), side });
			}
		}
	}

	// override these as it does not use a disc

	@Override
	public boolean hasLabel() {
		return false;
	}

	@Override
	public String getLabel() {
		return null;
	}

	@Override
	public void setLabel(final String label) {
	}
}
