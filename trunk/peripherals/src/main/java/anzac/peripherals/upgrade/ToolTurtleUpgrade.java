package anzac.peripherals.upgrade;

import net.minecraft.block.Block;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Icon;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeHooks;
import anzac.peripherals.tiles.InternalPlayer;
import dan200.computercraft.api.peripheral.IPeripheral;
import dan200.computercraft.api.turtle.ITurtleAccess;
import dan200.computercraft.api.turtle.ITurtleUpgrade;
import dan200.computercraft.api.turtle.TurtleCommandResult;
import dan200.computercraft.api.turtle.TurtleSide;
import dan200.computercraft.api.turtle.TurtleUpgradeType;
import dan200.computercraft.api.turtle.TurtleVerb;

public abstract class ToolTurtleUpgrade implements ITurtleUpgrade {

	private final ItemStack toolStack;
	private final int upgradeId;
	private final String adjective;

	public ToolTurtleUpgrade(final ItemStack toolStack, final int upgradeId, final String adjective) {
		super();
		this.toolStack = toolStack;
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
		return TurtleUpgradeType.Tool;
	}

	@Override
	public final ItemStack getCraftingItem() {
		return toolStack.copy();
	}

	@Override
	public final IPeripheral createPeripheral(final ITurtleAccess turtle, final TurtleSide side) {
		return null;
	}

	@Override
	public final TurtleCommandResult useTool(final ITurtleAccess turtle, final TurtleSide side, final TurtleVerb verb,
			final int direction) {
		// create fake player, use left click for attach, right click for dig
		// create fake player, use side for left or right click, use dig for shift
		switch (verb) {
		case Attack:
			return attack(turtle, direction);
		case Dig:
			return dig(turtle, direction);
		}
		return TurtleCommandResult.failure("Unsupported action");
	}

	@Override
	public final Icon getIcon(final ITurtleAccess turtle, final TurtleSide side) {
		return toolStack.getIconIndex();
	}

	@Override
	public final void update(final ITurtleAccess turtle, final TurtleSide side) {
		// nothing to do for a tool
	}

	protected boolean canBreakBlock(final World world, final int x, final int y, final int z) {
		final int bid = world.getBlockId(x, y, z);
		final Block block = Block.blocksList[bid];
		if ((bid == 0) || (bid == Block.bedrock.blockID) || (block.getBlockHardness(world, x, y, z) <= -1.0F)) {
			return false;
		}
		return true;
	}

	protected boolean canHarvestBlock(final World world, final int x, final int y, final int z) {
		final int bid = world.getBlockId(x, y, z);
		final int meta = world.getBlockMetadata(x, y, z);
		final Block block = Block.blocksList[bid];

		final InternalPlayer turtlePlayer = new InternalPlayer(world);
		turtlePlayer.loadInventory(toolStack.copy());
		return ForgeHooks.canHarvestBlock(block, turtlePlayer, meta);
	}

	protected float getDamageMultiplier() {
		return 1.0f;
	}

	protected abstract TurtleCommandResult attack(final ITurtleAccess turtle, final int direction);

	protected abstract TurtleCommandResult dig(final ITurtleAccess turtle, final int direction);
}