package anzac.peripherals.upgrade;

import java.lang.reflect.Constructor;

import net.minecraft.item.ItemStack;
import net.minecraft.util.Icon;
import anzac.peripherals.annotations.TurtleUpgrade;
import dan200.computercraft.api.peripheral.IPeripheral;
import dan200.computercraft.api.turtle.ITurtleAccess;
import dan200.computercraft.api.turtle.ITurtleUpgrade;
import dan200.computercraft.api.turtle.TurtleCommandResult;
import dan200.computercraft.api.turtle.TurtleSide;
import dan200.computercraft.api.turtle.TurtleUpgradeType;
import dan200.computercraft.api.turtle.TurtleVerb;

public abstract class PeripheralTurtleUpgrade implements ITurtleUpgrade {
	private final ItemStack itemStack;
	private final int upgradeId;
	private String adjective;
	private Constructor<? extends IPeripheral> constructor;

	public PeripheralTurtleUpgrade(final ItemStack itemStack, final int upgradeId) {
		super();
		this.itemStack = itemStack;
		this.upgradeId = upgradeId;
	}

	@Override
	public final int getUpgradeID() {
		return upgradeId;
	}

	@Override
	public final String getAdjective() {
		if (adjective == null) {
			final Class<? extends PeripheralTurtleUpgrade> class1 = getClass();
			final TurtleUpgrade annotation = class1.getAnnotation(TurtleUpgrade.class);
			adjective = annotation.adjective();
		}
		return adjective;
	}

	@Override
	public final TurtleUpgradeType getType() {
		return TurtleUpgradeType.Peripheral;
	}

	@Override
	public final ItemStack getCraftingItem() {
		return itemStack.copy();
	}

	@Override
	public final TurtleCommandResult useTool(final ITurtleAccess turtle, final TurtleSide side, final TurtleVerb verb,
			final int direction) {
		return null;
	}

	@Override
	public Icon getIcon(final ITurtleAccess turtle, final TurtleSide side) {
		return getCraftingItem().getIconIndex();
	}

	@Override
	public IPeripheral createPeripheral(final ITurtleAccess turtle, final TurtleSide side) {
		try {
			if (constructor == null) {
				final Class<? extends PeripheralTurtleUpgrade> class1 = getClass();
				final TurtleUpgrade annotation = class1.getAnnotation(TurtleUpgrade.class);
				final Class<? extends IPeripheral> peripheralType = annotation.peripheralType();
				constructor = peripheralType.getConstructor(ITurtleAccess.class);
			}
			return constructor.newInstance(turtle);
		} catch (final Exception e) {
			e.printStackTrace();
		}
		return null;
	}
}
