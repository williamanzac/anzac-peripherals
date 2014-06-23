package anzac.peripherals.upgrade;

import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemTool;

public class GenericToolUpgrade extends ToolTurtleUpgrade {

	public GenericToolUpgrade(final ItemTool tool, final int upgradeId, final String adjective) {
		super(new ItemStack(tool), upgradeId, adjective);
	}
}
