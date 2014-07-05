package anzac.peripherals.peripheral;

import java.util.List;

import anzac.peripherals.annotations.Peripheral;
import anzac.peripherals.annotations.PeripheralMethod;
import anzac.peripherals.tiles.NoteTileEntity;
import anzac.peripherals.tiles.NoteTileEntity.Instrument;
import anzac.peripherals.utils.ClassUtils;

@Peripheral(type = "Note")
public class NotePeripheral extends BasePeripheral {

	public NotePeripheral(NoteTileEntity entity) {
		super(entity);
	}

	@Override
	protected NoteTileEntity getEntity() {
		return (NoteTileEntity) entity;
	}

	@Override
	protected List<String> methodNames() {
		return ClassUtils.getMethodNames(NotePeripheral.class);
	}

	/**
	 * @param instrument
	 * @param note
	 * @throws Exception
	 */
	@PeripheralMethod
	public void playNote(final Instrument instrument, final int note) throws Exception {
		if (note < 0 || note > 24) {
			throw new Exception("note must be between: 0-24");
		}
		getEntity().playNote(instrument, note);
	}

	@PeripheralMethod
	public void playSound(final String sound) {
		playSound(sound, 1f, 1f);
	}

	@PeripheralMethod
	public void playSound(final String sound, final float volume) {
		playSound(sound, volume, 1f);
	}

	@PeripheralMethod
	public void playSound(final String sound, final float volume, final float pitch) {
		getEntity().playSound(sound, volume, pitch);
	}
}
