package anzac.peripherals.upgrade;

import net.minecraft.item.ItemStack;
import net.minecraft.util.Icon;
import dan200.computercraft.api.turtle.ITurtleAccess;
import dan200.computercraft.api.turtle.ITurtleUpgrade;
import dan200.computercraft.api.turtle.TurtleCommandResult;
import dan200.computercraft.api.turtle.TurtleSide;
import dan200.computercraft.api.turtle.TurtleUpgradeType;
import dan200.computercraft.api.turtle.TurtleVerb;

public abstract class PeripheralTurtleUpgrade implements ITurtleUpgrade {
	private final ItemStack itemStack;
	private final int upgradeId;
	private final String adjective;

	public PeripheralTurtleUpgrade(final ItemStack itemStack, final int upgradeId, final String adjective) {
		super();
		this.itemStack = itemStack;
		this.upgradeId = upgradeId;
		this.adjective = adjective;
	}

	@Override
	public final int getUpgradeID() {
		return upgradeId;
	}

	@Override
	public final String getAdjective() {
		return adjective;
	}

	@Override
	public final TurtleUpgradeType getType() {
		return TurtleUpgradeType.Peripheral;
	}

	@Override
	public final ItemStack getCraftingItem() {
		return itemStack;
	}

	@Override
	public final TurtleCommandResult useTool(final ITurtleAccess turtle, final TurtleSide side, final TurtleVerb verb,
			final int direction) {
		return null;
	}

	@Override
	public final Icon getIcon(final ITurtleAccess turtle, final TurtleSide side) {
		return itemStack.getIconIndex();
	}
}
