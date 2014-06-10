package anzac.peripherals.upgrade;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import dan200.computercraft.api.turtle.ITurtleAccess;
import dan200.computercraft.api.turtle.TurtleCommandResult;

public class BucketUpgrade extends ToolTurtleUpgrade {

	public BucketUpgrade() {
		super(new ItemStack(Item.bucketEmpty), 26714, "Fluid");
	}

	@Override
	protected TurtleCommandResult attack(ITurtleAccess turtle, int direction) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected TurtleCommandResult dig(ITurtleAccess turtle, int direction) {
		// TODO Auto-generated method stub
		return null;
	}
}
