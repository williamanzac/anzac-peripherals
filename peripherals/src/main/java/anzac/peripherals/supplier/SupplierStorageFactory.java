package anzac.peripherals.supplier;

import java.util.UUID;

public interface SupplierStorageFactory {
	public SupplierStorageType getType();

	public SupplierStorage create(final SupplierManager manager, final UUID id1, final UUID id2);
}
