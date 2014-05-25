package anzac.peripherals.tiles;

import java.util.Arrays;
import java.util.List;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.ForgeDirection;
import anzac.peripherals.annotations.Peripheral;
import anzac.peripherals.annotations.PeripheralMethod;
import anzac.peripherals.utils.Position;
import dan200.computercraft.api.peripheral.IComputerAccess;
import dan200.computercraft.api.peripheral.IPeripheral;

@Peripheral(type = "Redstone", hasGUI = false)
public class RedstoneTileEntity extends BasePeripheralTileEntity {

	private final int[] input = new int[6];
	private int[] output = new int[6];
	private final int[] bundledInput = new int[6];
	private int[] bundledOutput = new int[6];

	@Override
	protected List<String> methodNames() {
		return getMethodNames(RedstoneTileEntity.class);
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
		if (bundledOutput[side.ordinal()] != value) {
			bundledOutput[side.ordinal()] = value;
			worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
		}
	}

	/**
	 * @param side
	 * @return
	 */
	@PeripheralMethod
	public int getBundledOutput(final ForgeDirection side) {
		return bundledOutput[side.ordinal()];
	}

	/**
	 * @param side
	 * @return
	 */
	@PeripheralMethod
	public int getBundledInput(final ForgeDirection side) {
		return bundledInput[side.ordinal()];
	}

	/**
	 * @param side
	 * @param mask
	 * @return
	 */
	@PeripheralMethod
	public boolean testBundledInput(final ForgeDirection side, final int mask) {
		return (input[side.ordinal()] & mask) == mask;
	}

	/**
	 * @param side
	 * @param value
	 */
	@PeripheralMethod
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
		return output[side.ordinal()];
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
		return input[side.ordinal()];
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

	// used by PeripheralBlock
	public int getOutput(final int side) {
		return output[side];
	}

	// used by PeripheralBlock
	public void setInput(final int side, final int inputStrength) {
		if (input[side] != inputStrength) {
			input[side] = inputStrength;
			for (final IComputerAccess computer : computers) {
				computer.queueEvent("redstone", new Object[] { computer.getAttachmentName(), side });
			}
		}
	}

	// used by PeripheralBlock
	public void setBundledInput(final int side, final int combination) {
		if (bundledInput[side] != combination) {
			bundledInput[side] = combination;
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
	public boolean equals(final IPeripheral other) {
		return equals((Object) other);
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
