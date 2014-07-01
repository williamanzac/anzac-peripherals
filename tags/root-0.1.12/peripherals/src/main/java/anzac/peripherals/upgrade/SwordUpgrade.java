package anzac.peripherals.upgrade;

import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemSword;

public class SwordUpgrade extends ToolTurtleUpgrade {

	public SwordUpgrade(final ItemSword tool, final int upgradeId) {
		super(new ItemStack(tool), upgradeId, "Melee");
	}
}
