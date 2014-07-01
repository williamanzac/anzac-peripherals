package anzac.peripherals.upgrade;

import net.minecraft.item.ItemFlintAndSteel;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeDirection;
import anzac.peripherals.tiles.InternalPlayer;
import anzac.peripherals.utils.Position;
import anzac.peripherals.utils.Utils;
import dan200.computercraft.api.turtle.ITurtleAccess;
import dan200.computercraft.api.turtle.TurtleCommandResult;

public class FlintUpgrade extends ToolTurtleUpgrade {

	public FlintUpgrade(final ItemFlintAndSteel tool, final int upgradeId) {
		super(new ItemStack(tool), upgradeId, "Firestarter");
	}

	@Override
	protected TurtleCommandResult dig(final ITurtleAccess turtle, final ForgeDirection direction) {
		final World world = turtle.getWorld();
		final Position pos = new Position(turtle.getPosition());
		pos.orientation = direction;
		pos.moveForwards(1);
		if (direction != ForgeDirection.UP) {
			pos.orientation = ForgeDirection.DOWN;
			pos.moveForwards(1);
		}
		final InternalPlayer turtlePlayer = Utils.createPlayer(world, turtle, turtle.getDirection());
		final ItemStack craftingItem = getCraftingItem().copy();
		turtlePlayer.loadInventory(craftingItem);

		if (craftingItem.getItem().onItemUse(craftingItem, turtlePlayer, world, pos.x, pos.y, pos.z,
				ForgeDirection.UP.ordinal(), 0.0F, 0.0F, 0.0F)) {
			return TurtleCommandResult.success();
		}
		return TurtleCommandResult.failure();
	}
}
