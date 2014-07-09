package anzac.peripherals.upgrade;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Facing;
import net.minecraft.util.Icon;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeDirection;
import net.minecraftforge.common.ForgeHooks;
import anzac.peripherals.proxy.CommonProxy;
import anzac.peripherals.tiles.InternalPlayer;
import anzac.peripherals.utils.Position;
import anzac.peripherals.utils.Utils;
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
	public ItemStack getCraftingItem() {
		return toolStack.copy();
	}

	@Override
	public final IPeripheral createPeripheral(final ITurtleAccess turtle, final TurtleSide side) {
		return null;
	}

	@Override
	public final TurtleCommandResult useTool(final ITurtleAccess turtle, final TurtleSide side, final TurtleVerb verb,
			final int direction) {
		switch (verb) {
		case Attack:
			return attack(turtle, direction);
		case Dig:
			return dig(turtle, ForgeDirection.values()[direction]);
		}
		return TurtleCommandResult.failure("Unsupported action");
	}

	protected TurtleCommandResult attack(final ITurtleAccess turtle, final int direction) {
		final World world = turtle.getWorld();
		final InternalPlayer turtlePlayer = Utils.createPlayer(world, turtle, direction);

		final Vec3 turtlePos = world.getWorldVec3Pool().getVecFromPool(turtlePlayer.posX, turtlePlayer.posY,
				turtlePlayer.posZ);
		final Vec3 rayDir = turtlePlayer.getLook(1.0F);
		final Vec3 rayStart = turtlePos.addVector(rayDir.xCoord * 0.4D, rayDir.yCoord * 0.4D, rayDir.zCoord * 0.4D);
		final Entity hitEntity = Utils.findEntity(world, rayStart, rayDir, 1.1D);
		if (hitEntity == null) {
			return TurtleCommandResult.failure("Nothing to attack here");
		}
		if (!canAttackEntity(turtle, hitEntity)) {
			return TurtleCommandResult.failure();
		}

		return attack(turtle, hitEntity);
	}

	protected TurtleCommandResult dig(final ITurtleAccess turtle, final ForgeDirection direction) {
		final World world = turtle.getWorld();
		final Position pos = new Position(turtle.getPosition());
		pos.orientation = direction;
		final ForgeDirection oppositeDir = direction.getOpposite();
		final int opposite = oppositeDir.ordinal();
		final Position newPos = new Position(pos);
		final InternalPlayer turtlePlayer = Utils.createPlayer(world, turtle, turtle.getDirection());
		final ItemStack craftingItem = getCraftingItem().copy();
		turtlePlayer.loadInventory(craftingItem);

		// try using the item first
		newPos.moveForwards(1);
		final Item item = craftingItem.getItem();
		if (item.onItemUse(craftingItem, turtlePlayer, world, newPos.x, newPos.y, newPos.z, opposite, 0.0F, 0.0F, 0.0F)) {
			return TurtleCommandResult.success();
		}
		newPos.moveForwards(1);
		if (item.onItemUse(craftingItem, turtlePlayer, world, newPos.x, newPos.y, newPos.z, opposite, 0.0F, 0.0F, 0.0F)) {
			return TurtleCommandResult.success();
		}
		if (direction.ordinal() >= 2) {
			newPos.orientation = ForgeDirection.DOWN;
			newPos.moveForwards(1);
			if (item.onItemUse(craftingItem, turtlePlayer, world, newPos.x, newPos.y, newPos.z, 0, 0.0F, 0.0F, 0.0F)) {
				return TurtleCommandResult.success();
			}
		}

		// try breaking the block
		pos.moveForwards(1);
		final int x = pos.x;
		final int y = pos.y;
		final int z = pos.z;

		final int id = world.getBlockId(x, y, z);
		if (Block.blocksList[id] == null || world.isAirBlock(x, y, z)) {
			return TurtleCommandResult.failure();
		}

		if (!canDigBlock(world, x, y, z, oppositeDir)) {
			return TurtleCommandResult.failure();
		}

		return dig(turtle, x, y, z, oppositeDir);
	}

	protected void storeOrDrop(final ITurtleAccess turtle, final ItemStack stack) {
		stack.stackSize -= Utils.addToInventory(ForgeDirection.UNKNOWN, stack, turtle.getInventory());
		if (stack.stackSize > 0) {
			dropItemStack(turtle, stack);
		}
	}

	protected void dropItemStack(final ITurtleAccess turtle, final ItemStack stack) {
		final World world = turtle.getWorld();
		final Position pos = new Position(turtle.getPosition());
		pos.orientation = ForgeDirection.values()[Facing.oppositeSide[turtle.getDirection()]];
		final EntityItem entityItem = new EntityItem(world, pos.x, pos.y, pos.z, stack.copy());
		entityItem.motionX = (pos.orientation.offsetX * 0.7D + world.rand.nextFloat() * 0.2D - 0.1D);
		entityItem.motionY = (pos.orientation.offsetY * 0.7D + world.rand.nextFloat() * 0.2D - 0.1D);
		entityItem.motionZ = (pos.orientation.offsetZ * 0.7D + world.rand.nextFloat() * 0.2D - 0.1D);
		entityItem.delayBeforeCanPickup = 30;
		world.spawnEntityInWorld(entityItem);
	}

	protected List<ItemStack> getBlockDrops(final World world, final int x, final int y, final int z,
			final ForgeDirection opposite) {
		final int id = world.getBlockId(x, y, z);
		final int meta = world.getBlockMetadata(x, y, z);
		final Block block = Block.blocksList[id];
		if (block == null || !ForgeHooks.canToolHarvestBlock(block, meta, getCraftingItem())) {
			return new ArrayList<ItemStack>();
		}
		return Block.blocksList[id].getBlockDropped(world, x, y, z, meta, 0);
	}

	protected boolean canAttackEntity(final ITurtleAccess turtle, final Entity entityHit) {
		final InternalPlayer turtlePlayer = Utils.createPlayer(turtle.getWorld(), turtle, turtle.getDirection());
		return entityHit.canAttackWithItem() && !entityHit.hitByEntity(turtlePlayer);
	}

	protected boolean canDigBlock(final World world, final int x, final int y, final int z,
			final ForgeDirection opposite) {
		final int bid = world.getBlockId(x, y, z);
		final int meta = world.getBlockMetadata(x, y, z);
		final Block block = Block.blocksList[bid];

		return ForgeHooks.canToolHarvestBlock(block, meta, getCraftingItem());
	}

	@Override
	public final Icon getIcon(final ITurtleAccess turtle, final TurtleSide side) {
		return getCraftingItem().getIconIndex();
	}

	@Override
	public final void update(final ITurtleAccess turtle, final TurtleSide side) {
		// nothing to do for a tool
	}

	protected TurtleCommandResult attack(final ITurtleAccess turtle, final Entity entityHit) {
		final InternalPlayer turtlePlayer = Utils.createPlayer(turtle.getWorld(), turtle, turtle.getDirection());
		turtlePlayer.loadInventory(getCraftingItem().copy());

		CommonProxy.setEntityDropConsumer(entityHit, new DropConsumer() {
			@Override
			public void consumeDrop(final Entity entity, final ItemStack drop) {
				storeOrDrop(turtle, drop);
			}
		});
		turtlePlayer.attackTargetEntityWithCurrentItem(entityHit);

		CommonProxy.clearEntityDropConsumer(entityHit);
		return TurtleCommandResult.success();
	}

	private TurtleCommandResult dig(final ITurtleAccess turtle, final int x, final int y, final int z,
			final ForgeDirection opposite) {
		final World world = turtle.getWorld();
		for (final ItemStack stack : getBlockDrops(world, x, y, z, opposite)) {
			storeOrDrop(turtle, stack);
		}

		final int meta = world.getBlockMetadata(x, y, z);
		final int id = world.getBlockId(x, y, z);
		world.playAuxSFX(2001, x, y, z, id + meta * Block.blocksList.length);
		world.setBlockToAir(x, y, z);
		return TurtleCommandResult.success();
	}
}