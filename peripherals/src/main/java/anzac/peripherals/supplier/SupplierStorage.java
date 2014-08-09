package anzac.peripherals.supplier;

import java.util.UUID;

import net.minecraft.nbt.NBTTagCompound;

public interface SupplierStorage {

	public void readFromNBT(final NBTTagCompound tagCompound);

	public void writeToNBT(final NBTTagCompound tagCompound);

	public UUID getOwner();
}
