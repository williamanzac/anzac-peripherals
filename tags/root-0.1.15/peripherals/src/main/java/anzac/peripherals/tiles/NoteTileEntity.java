package anzac.peripherals.tiles;

import net.minecraft.block.material.Material;
import anzac.peripherals.AnzacPeripheralsCore;
import anzac.peripherals.peripheral.NotePeripheral;

public class NoteTileEntity extends BasePeripheralTileEntity {

	public static enum Instrument {
		harp, bd, snare, hat, bassattack
	}

	public NoteTileEntity() throws Exception {
		super(NotePeripheral.class);
	}

	public void playNote(final Instrument instrument, final int note) {
		if (worldObj.getBlockMaterial(xCoord, yCoord + 1, zCoord) == Material.air) {
			worldObj.addBlockEvent(xCoord, yCoord, zCoord, AnzacPeripheralsCore.peripheralBlockId,
					instrument.ordinal(), note);
		}
	}

	@Override
	public boolean receiveClientEvent(final int instrument, final int note) {
		final float inflate = (float) Math.pow(2.0D, (note - 12) / 12.0D);
		playSound("note." + Instrument.values()[instrument], 3.0F, inflate);
		worldObj.spawnParticle("note", xCoord + 0.5D, yCoord + 1.2D, zCoord + 0.5D, note / 24.0D, 0.0D, 0.0D);
		return true;
	}

	public void playSound(final String sound, final float volume, final float pitch) {
		worldObj.playSoundEffect(xCoord + 0.5D, yCoord + 0.5D, zCoord + 0.5D, sound, volume, pitch);
	}
}
