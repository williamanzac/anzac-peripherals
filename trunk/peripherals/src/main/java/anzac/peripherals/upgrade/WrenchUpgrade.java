package anzac.peripherals.upgrade;

import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeDirection;
import net.minecraftforge.oredict.OreDictionary;
import anzac.peripherals.tiles.InternalPlayer;
import anzac.peripherals.utils.Position;
import anzac.peripherals.utils.Utils;
import buildcraft.api.tools.IToolWrench;
import dan200.computercraft.api.turtle.ITurtleAccess;
import dan200.computercraft.api.turtle.TurtleCommandResult;

public class WrenchUpgrade extends ToolTurtleUpgrade {

	public WrenchUpgrade(final IToolWrench tool, final int upgradeId) {
		super(new ItemStack((Item) tool, 1, OreDictionary.WILDCARD_VALUE), upgradeId, "Wrench");
	}

	@Override
	protected boolean canAttackEntity(final ITurtleAccess turtle, final Entity entityHit) {
		return true;
	}

	@Override
	protected TurtleCommandResult attack(final ITurtleAccess turtle, final Entity entityHit) {
		final InternalPlayer turtlePlayer = Utils.createPlayer(turtle.getWorld(), turtle, turtle.getDirection());
		turtlePlayer.loadInventory(getCraftingItem().copy());
		if (entityHit.interactFirst(turtlePlayer)) {
			return TurtleCommandResult.success();
		}
		return TurtleCommandResult.failure("Cannot interaccy with entity");
	}

	@Override
	protected boolean canDigBlock(final World world, final int x, final int y, final int z,
			final ForgeDirection opposite) {
		return true;
	}

	@Override
	protected TurtleCommandResult dig(final ITurtleAccess turtle, final ForgeDirection direction) {
		final World world = turtle.getWorld();
		final Position pos = new Position(turtle.getPosition());
		pos.orientation = direction;
		final int opposite = direction.getOpposite().ordinal();
		pos.moveForwards(1);
		final InternalPlayer turtlePlayer = Utils.createPlayer(world, turtle, turtle.getDirection());
		final ItemStack craftingItem = getCraftingItem();
		turtlePlayer.loadInventory(craftingItem.copy());

		final int id = world.getBlockId(pos.x, pos.y, pos.z);
		final Block block = Block.blocksList[id];
		if (block != null
				&& block.onBlockActivated(world, pos.x, pos.y, pos.z, turtlePlayer, opposite, 0.0F, 0.0F, 0.0F)) {
			return TurtleCommandResult.success();
		}

		if (craftingItem.getItem().onItemUse(craftingItem, turtlePlayer, world, pos.x, pos.y, pos.z, opposite, 0.0F,
				0.0F, 0.0F)) {
			return TurtleCommandResult.success();
		}
		return TurtleCommandResult.failure();
	}
}
