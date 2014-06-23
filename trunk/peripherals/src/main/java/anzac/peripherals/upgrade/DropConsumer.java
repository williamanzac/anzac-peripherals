package anzac.peripherals.upgrade;

import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;

public interface DropConsumer {
	public void consumeDrop(final Entity paramEntity, final ItemStack paramItemStack);
}
