package anzac.peripherals.tiles;

import java.util.HashMap;
import java.util.Map;

import net.minecraft.network.packet.Packet;
import net.minecraft.tileentity.TileEntity;
import anzac.peripherals.AnzacPeripheralsCore;
import anzac.peripherals.network.PacketHandler;
import dan200.computer.api.IComputerAccess;
import dan200.computer.api.ILuaContext;
import dan200.computer.api.IPeripheral;

public abstract class BasePeripheralTileEntity extends TileEntity implements IPeripheral {
	protected final Map<IComputerAccess, Computer> computers = new HashMap<IComputerAccess, Computer>();

	private class Computer {
		private final String name;

		public Computer(final String name) {
			this.name = name;
		}

		private String mount;
	}

	@Override
	public String[] getMethodNames() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object[] callMethod(IComputerAccess computer, ILuaContext context, int method, Object[] arguments)
			throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean canAttachToSide(int side) {
		return true;
	}

	@Override
	public void attach(final IComputerAccess computer) {
		AnzacPeripheralsCore.computerPeripheralMap.put(computer.getID(), this);
		computers.put(computer, new Computer(computer.getAttachmentName()));
	}

	@Override
	public void detach(final IComputerAccess computer) {
		AnzacPeripheralsCore.computerPeripheralMap.remove(computer.getID());
		computers.remove(computer);
	}

	protected boolean isConnected() {
		return !computers.isEmpty();
	}

	@Override
	public Packet getDescriptionPacket() {
		return PacketHandler.createTileEntityPacket("anzac", PacketHandler.ID_TILE_ENTITY, this);
	}
}
