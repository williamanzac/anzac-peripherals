package anzac.peripherals.tiles;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.packet.Packet;
import net.minecraft.tileentity.TileEntity;
import anzac.peripherals.AnzacPeripheralsCore;
import anzac.peripherals.annotations.Peripheral;
import anzac.peripherals.annotations.PeripheralMethod;
import anzac.peripherals.network.PacketHandler;
import anzac.peripherals.utils.ClassUtils;
import dan200.computercraft.api.filesystem.IMount;
import dan200.computercraft.api.lua.ILuaContext;
import dan200.computercraft.api.peripheral.IComputerAccess;
import dan200.computercraft.api.peripheral.IPeripheral;

public abstract class BasePeripheralTileEntity extends TileEntity implements IPeripheral {

	protected final Set<IComputerAccess> computers = new HashSet<IComputerAccess>();

	public SimpleDiscInventory discInv = new SimpleDiscInventory(this, true);

	@Override
	public String[] getMethodNames() {
		return methodNames().toArray(new String[0]);
	}

	protected List<String> methodNames() {
		return ClassUtils.getMethodNames(BasePeripheralTileEntity.class);
	}

	@Override
	public final String getType() {
		final Peripheral annotation = getClass().getAnnotation(Peripheral.class);
		final String type = annotation.type();
		return type;
	}

	@Override
	public Object[] callMethod(final IComputerAccess computer, final ILuaContext context, final int method,
			final Object[] arguments) throws Exception {
		final String methodName = getMethodNames()[method];

		return ClassUtils.callPeripheralMethod(this, methodName, arguments);
	}

	@Override
	public void attach(final IComputerAccess computer) {
		AnzacPeripheralsCore.addPeripheralLabel(computer.getID(), getLabel(), this);
		computers.add(computer);
		worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
	}

	@Override
	public void detach(final IComputerAccess computer) {
		AnzacPeripheralsCore.removePeripheralLabel(computer.getID(), getLabel());
		computers.remove(computer);
		worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
	}

	protected boolean isConnected() {
		return !computers.isEmpty();
	}

	@Override
	public Packet getDescriptionPacket() {
		return PacketHandler.createTileEntityPacket("anzac", PacketHandler.ID_TILE_ENTITY, this);
	}

	@Override
	public void readFromNBT(final NBTTagCompound nbtTagCompound) {
		super.readFromNBT(nbtTagCompound);

		// read disc
		if (nbtTagCompound.hasKey("disc")) {
			discInv.readFromNBT(nbtTagCompound.getCompoundTag("disc"));
		}
	}

	@Override
	public void writeToNBT(final NBTTagCompound nbtTagCompound) {
		super.writeToNBT(nbtTagCompound);

		// write disc
		final NBTTagCompound discTag = new NBTTagCompound();
		discInv.writeToNBT(discTag);
		nbtTagCompound.setCompoundTag("disc", discTag);
	}

	public boolean hasLabel() {
		return discInv.hasLabel();
	}

	/**
	 * Get the label for this peripheral
	 * 
	 * @return the label
	 */
	@PeripheralMethod
	public String getLabel() {
		return discInv.getLabel();
	}

	/**
	 * Sets the label for this peripheral
	 * 
	 * @param label
	 *            The label to use
	 */
	@PeripheralMethod
	public void setLabel(final String label) {
		for (final IComputerAccess computer : computers) {
			AnzacPeripheralsCore.removePeripheralLabel(computer.getID(), getLabel());
		}
		discInv.setLabel(label);
		for (final IComputerAccess computer : computers) {
			AnzacPeripheralsCore.addPeripheralLabel(computer.getID(), label, this);
		}
	}

	protected IMount getMount() {
		return discInv.getMount();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((discInv == null) ? 0 : discInv.hashCode());
		return result;
	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		final BasePeripheralTileEntity other = (BasePeripheralTileEntity) obj;
		if (discInv == null) {
			if (other.discInv != null)
				return false;
		} else if (!discInv.equals(other.discInv))
			return false;
		return true;
	}
}
