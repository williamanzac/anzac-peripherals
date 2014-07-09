package anzac.peripherals.tiles;

import java.util.Arrays;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.ForgeDirection;
import anzac.peripherals.annotations.PeripheralMethod;
import anzac.peripherals.peripheral.RedstonePeripheral;
import anzac.peripherals.utils.Position;

public class RedstoneTileEntity extends BasePeripheralTileEntity {

	public RedstoneTileEntity() throws Exception {
		super(RedstonePeripheral.class);
	}

	private final int[] input = new int[6];
	private int[] output = new int[6];
	private final int[] bundledInput = new int[6];
	private int[] bundledOutput = new int[6];

	public void setBundledOutput(final ForgeDirection side, final int value) {
		if (bundledOutput[side.ordinal()] != value) {
			bundledOutput[side.ordinal()] = value;
			worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
		}
	}

	public int getBundledOutput(final ForgeDirection side) {
		return bundledOutput[side.ordinal()];
	}

	public int getBundledInput(final ForgeDirection side) {
		return bundledInput[side.ordinal()];
	}

	public boolean testBundledInput(final ForgeDirection side, final int mask) {
		return (input[side.ordinal()] & mask) == mask;
	}

	public void setAnalogOutput(final ForgeDirection side, final int value) {
		if (output[side.ordinal()] != value) {
			output[side.ordinal()] = value;
			final Position p = new Position(xCoord, yCoord, zCoord, side);
			p.moveForwards(1);
			final int id = worldObj.getBlockId(xCoord, yCoord, zCoord);
			worldObj.notifyBlockOfNeighborChange(p.x, p.y, p.z, id);
			worldObj.notifyBlocksOfNeighborChange(p.x, p.y, p.z, id, side.ordinal());
		}
	}

	/**
	 * @param side
	 * @return
	 */
	@PeripheralMethod
	public int getAnalogOutput(final ForgeDirection side) {
		return output[side.ordinal()];
	}

	/**
	 * @param side
	 * @return
	 */
	@PeripheralMethod
	public int getAnalogInput(final ForgeDirection side) {
		return input[side.ordinal()];
	}

	// used by PeripheralBlock
	public int getOutput(final int side) {
		return output[side];
	}

	// used by PeripheralBlock
	public void setInput(final int side, final int inputStrength) {
		if (input[side] != inputStrength) {
			input[side] = inputStrength;
			queueEvent("redstone", side);
		}
	}

	// used by PeripheralBlock
	public void setBundledInput(final int side, final int combination) {
		if (bundledInput[side] != combination) {
			bundledInput[side] = combination;
			queueEvent("redstone", side);
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

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + Arrays.hashCode(bundledInput);
		result = prime * result + Arrays.hashCode(bundledOutput);
		result = prime * result + Arrays.hashCode(input);
		result = prime * result + Arrays.hashCode(output);
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		RedstoneTileEntity other = (RedstoneTileEntity) obj;
		if (!Arrays.equals(bundledInput, other.bundledInput))
			return false;
		if (!Arrays.equals(bundledOutput, other.bundledOutput))
			return false;
		if (!Arrays.equals(input, other.input))
			return false;
		if (!Arrays.equals(output, other.output))
			return false;
		return true;
	}

	@Override
	public void readFromNBT(NBTTagCompound nbtTagCompound) {
		super.readFromNBT(nbtTagCompound);

		if (nbtTagCompound.hasKey("output")) {
			output = nbtTagCompound.getIntArray("output");
		}
		if (nbtTagCompound.hasKey("bundledOutput")) {
			bundledOutput = nbtTagCompound.getIntArray("bundledOutput");
		}
	}

	@Override
	public void writeToNBT(NBTTagCompound nbtTagCompound) {
		super.writeToNBT(nbtTagCompound);

		nbtTagCompound.setIntArray("output", output);
		nbtTagCompound.setIntArray("bundledOutput", bundledOutput);
	}
}
