package anzac.peripherals.tiles;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.packet.Packet;
import net.minecraft.tileentity.TileEntity;
import anzac.peripherals.network.PacketHandler;
import anzac.peripherals.peripheral.BasePeripheral;
import dan200.computercraft.api.filesystem.IMount;
import dan200.computercraft.api.peripheral.IPeripheral;

public abstract class BasePeripheralTileEntity extends TileEntity {

	public SimpleDiscInventory discInv = new SimpleDiscInventory(this, true);

	private final BasePeripheral peripheral;

	// private final Class<P> peripheralClass;

	public BasePeripheralTileEntity(final Class<? extends BasePeripheral> peripheralClass) throws Exception {
		peripheral = peripheralClass.getConstructor(getClass()).newInstance(this);
	}

	@Override
	public Packet getDescriptionPacket() {
		return PacketHandler.createTileEntityPacket("anzac", PacketHandler.ID_TILE_ENTITY, this);
	}

	protected boolean isConnected() {
		return peripheral.isConnected();
	}

	protected void queueEvent(final String event, final Object... parameters) {
		peripheral.queueEvent(event, parameters);
	}

	protected void queueEvent(final PeripheralEvent event, final Object... parameters) {
		peripheral.queueEvent(event, parameters);
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

	public String getLabel() {
		return discInv.getLabel();
	}

	public void setLabel(final String label) {
		discInv.setLabel(label);
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

	public IPeripheral getPeripheral() {
		return peripheral;
	}
}
