package anzac.peripherals.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.ChunkCoordinates;
import net.minecraft.util.EnumMovingObjectType;
import net.minecraft.util.Facing;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeDirection;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidHandler;
import net.minecraftforge.oredict.OreDictionary;
import anzac.peripherals.tiles.InternalPlayer;
import buildcraft.api.inventory.ISpecialInventory;
import buildcraft.api.transport.IPipeTile;
import buildcraft.api.transport.IPipeTile.PipeType;
import dan200.computercraft.api.turtle.ITurtleAccess;

public class Utils {
	private static final List<ForgeDirection> directions = new ArrayList<ForgeDirection>(
			Arrays.asList(ForgeDirection.VALID_DIRECTIONS));
	private static final Random RANDOM = new Random();
	private static final int FIRST_UPGRADE_ID = 26714;
	private static int nextTurtleId = FIRST_UPGRADE_ID;

	public static int[] createSlotArray(final int first, final int count) {
		final int[] slots = new int[count];
		for (int k = first; k < first + count; k++) {
			slots[k - first] = k;
		}
		return slots;
	}

	public static Item getItem(final String classname, final String name) {
		return ClassUtils.getField(classname, name, Item.class);
	}

	public static Block getBlock(final String classname, final String name) {
		return ClassUtils.getField(classname, name, Block.class);
	}

	/**
	 * merges provided ItemStack with the first avaliable one in the container/player inventory
	 */
	public static boolean mergeItemStack(final ItemStack sourceStack, final ItemStack targetStack) {
		boolean merged = false;

		if (sourceStack.isStackable()) {
			if (stacksMatch(targetStack, sourceStack)) {
				final int l = targetStack.stackSize + sourceStack.stackSize;

				if (l <= sourceStack.getMaxStackSize()) {
					sourceStack.stackSize = 0;
					targetStack.stackSize = l;
					merged = true;
				} else if (targetStack.stackSize < sourceStack.getMaxStackSize()) {
					sourceStack.stackSize -= sourceStack.getMaxStackSize() - targetStack.stackSize;
					targetStack.stackSize = sourceStack.getMaxStackSize();
					merged = true;
				}
			}
		}

		return merged;
	}

	public static boolean stacksMatch(final ItemStack targetStack, final ItemStack sourceStack) {
		return OreDictionary.itemMatches(targetStack, sourceStack, false);
	}

	public static boolean stacksMatch(final FluidStack targetStack, final FluidStack sourceStack) {
		return targetStack == null && sourceStack == null ? true : targetStack == null || sourceStack == null ? false
				: targetStack.isFluidEqual(sourceStack);
	}

	public static boolean canMergeItemStack(final ItemStack sourceStack, final ItemStack targetStack) {
		boolean merged = false;

		if (sourceStack.isStackable()) {
			if (stacksMatch(targetStack, sourceStack)) {
				final int l = targetStack.stackSize + sourceStack.stackSize;

				if (l <= sourceStack.getMaxStackSize()) {
					sourceStack.stackSize = 0;
					merged = true;
				} else if (targetStack.stackSize < sourceStack.getMaxStackSize()) {
					sourceStack.stackSize -= sourceStack.getMaxStackSize() - targetStack.stackSize;
					merged = true;
				}
			}
		}

		return merged;
	}

	public static int addToRandomPipe(final World world, final int x, final int y, final int z, final ItemStack stack) {
		return addToPipe(world, x, y, z, randomDirection(), stack);
	}

	private static ForgeDirection randomDirection() {
		return directions.get(RANDOM.nextInt(directions.size()));
	}

	public static int addToPipe(final World world, final int x, final int y, final int z, final ForgeDirection side,
			final ItemStack stack) {
		final Position pos = new Position(x, y, z, side);
		pos.moveForwards(1);

		final TileEntity tile = world.getBlockTileEntity(pos.x, pos.y, pos.z);
		final ForgeDirection opposite = side.getOpposite();
		if (tile instanceof IPipeTile) {
			final IPipeTile pipe = (IPipeTile) tile;
			if (pipe.getPipeType() != PipeType.ITEM) {
				return 0;
			}
			if (!pipe.isPipeConnected(opposite)) {
				return 0;
			}
			final int injected = pipe.injectItem(stack, true, opposite);
			return injected;
		}
		return 0;
	}

	public static int addToRandomInventory(final World world, final int x, final int y, final int z,
			final ItemStack stack) {
		return addToInventory(world, x, y, z, randomDirection(), stack);
	}

	public static int addToRandomFluidHandler(final World world, final int x, final int y, final int z,
			final FluidStack stack) {
		return addToFluidHandler(world, x, y, z, randomDirection(), stack);
	}

	public static void transferToSlot(final IInventory inv, final int slot, final ItemStack stack) {
		final ItemStack stackInSlot = inv.getStackInSlot(slot);
		if (stackInSlot == null) {
			final ItemStack targetStack = stack.copy();
			inv.setInventorySlotContents(slot, targetStack);
			stack.stackSize -= targetStack.stackSize;
		} else {
			final boolean merged = mergeItemStack(stack, stackInSlot);
			if (merged) {
				inv.setInventorySlotContents(slot, stackInSlot);
			}
		}
	}

	public static int addToInventory(final World world, final int x, final int y, final int z,
			final ForgeDirection side, final ItemStack stack) {
		return addToInventory(world, x, y, z, side, side.getOpposite(), stack);
	}

	public static int addToInventory(final World world, final int x, final int y, final int z,
			final ForgeDirection side, final ForgeDirection insertSide, final ItemStack stack) {
		final Position pos = new Position(x, y, z, side);
		pos.moveForwards(1);

		final TileEntity tileInventory = world.getBlockTileEntity(pos.x, pos.y, pos.z);
		if (tileInventory != null && tileInventory instanceof IInventory) {
			final IInventory inv = (IInventory) tileInventory;
			return addToInventory(insertSide, stack, inv);
		}
		return 0;
	}

	public static int addToInventory(final ForgeDirection insertSide, final ItemStack stack, final IInventory inv) {
		final ItemStack copy = stack.copy();
		final int[] availableSlots;
		if (inv instanceof ISpecialInventory) {
			return ((ISpecialInventory) inv).addItem(copy, true, insertSide);
		} else if (inv instanceof ISidedInventory) {
			final ISidedInventory sidedInventory = (ISidedInventory) inv;
			availableSlots = sidedInventory.getAccessibleSlotsFromSide(insertSide.ordinal());
		} else {
			availableSlots = createSlotArray(0, inv.getSizeInventory());
		}
		for (final int slot : availableSlots) {
			transferToSlot(inv, slot, copy);
			if (copy.stackSize == 0) {
				break;
			}
		}
		return stack.stackSize - copy.stackSize;
	}

	public static int addToFluidHandler(final World world, final int x, final int y, final int z,
			final ForgeDirection side, final FluidStack stack) {
		return addToFluidHandler(world, x, y, z, side, side.getOpposite(), stack);
	}

	public static int addToFluidHandler(final World world, final int x, final int y, final int z,
			final ForgeDirection side, final ForgeDirection insertSide, final FluidStack stack) {
		final Position pos = new Position(x, y, z, side);
		pos.moveForwards(1);

		final TileEntity tileInventory = world.getBlockTileEntity(pos.x, pos.y, pos.z);
		if (tileInventory != null && tileInventory instanceof IFluidHandler) {
			final FluidStack copy = stack.copy();
			final IFluidHandler tank = (IFluidHandler) tileInventory;
			if (tank.canFill(insertSide, copy.getFluid())) {
				copy.amount -= tank.fill(insertSide, copy, true);
			}
			return stack.amount - copy.amount;
		}
		return 0;
	}

	public static ItemStack consumeItem(final ItemStack stack) {
		if (stack.stackSize == 1) {
			if (stack.getItem().hasContainerItem()) {
				return stack.getItem().getContainerItemStack(stack);
			} else {
				return null;
			}
		} else {
			stack.splitStack(1);
			return stack;
		}
	}

	public static int getUUID(final ItemStack stack) {
		if (stack == null) {
			return -1;
		}
		return (stack.getItemDamage() << 15) + stack.itemID;
	}

	public static ItemStack getItemStack(final int uuid) {
		return getItemStack(uuid, 1);
	}

	public static ItemStack getItemStack(final int uuid, final int stackSize) {
		if (uuid == -1) {
			return null;
		}
		final int meta = getMeta(uuid);
		final int id = getId(uuid);
		return new ItemStack(id, stackSize, meta);
	}

	public static int getId(final int uuid) {
		return uuid & 32767;
	}

	public static int getMeta(final int uuid) {
		return uuid >> 15;
	}

	public static FluidStack getFluidStack(final int uuid, final int amount) {
		if (uuid == -1) {
			return null;
		}
		final Block block = Block.blocksList[uuid];
		final Fluid fluid = FluidRegistry.lookupFluidForBlock(block);
		return new FluidStack(fluid, amount);
	}

	public static int getUUID(final FluidStack stack) {
		if (stack == null) {
			return -1;
		}
		return getUUID(stack.getFluid());
	}

	public static int getUUID(final Fluid fluid) {
		if (fluid == null) {
			return -1;
		}
		return fluid.getBlockID();
	}

	public static int addItem(final ISpecialInventory inv, final ItemStack stack, final boolean doAdd,
			final ForgeDirection from) {
		final ItemStack copy;
		final int size = stack.stackSize;
		if (doAdd) {
			copy = stack;
		} else {
			copy = stack.copy();
		}
		final int[] availableSlots;
		if (inv instanceof ISidedInventory) {
			final ISidedInventory sidedInventory = (ISidedInventory) inv;
			availableSlots = sidedInventory.getAccessibleSlotsFromSide(from.ordinal());
		} else {
			availableSlots = createSlotArray(0, inv.getSizeInventory());
		}
		for (final int slot : availableSlots) {
			final ItemStack stackInSlot = inv.getStackInSlot(slot);
			if (stackInSlot != null && Utils.stacksMatch(stackInSlot, copy)) {
				final ItemStack target = copy.copy();
				final int l = stackInSlot.stackSize + copy.stackSize;
				target.stackSize = l;
				if (doAdd) {
					inv.setInventorySlotContents(slot, target);
				} else {
					final int inventoryStackLimit = inv.getInventoryStackLimit();
					if (target.stackSize > inventoryStackLimit) {
						target.stackSize = inventoryStackLimit;
					}
				}
				copy.stackSize -= (target.stackSize - stackInSlot.stackSize);
			}
			if (copy.stackSize == 0) {
				break;
			}
		}
		if (copy.stackSize > 0) {
			for (final int slot : availableSlots) {
				if (!inv.isItemValidForSlot(slot, stack)) {
					continue;
				}
				final ItemStack stackInSlot = inv.getStackInSlot(slot);
				if (stackInSlot == null) {
					final ItemStack target = copy.copy();
					if (doAdd) {
						inv.setInventorySlotContents(slot, target);
					} else {
						final int inventoryStackLimit = inv.getInventoryStackLimit();
						if (target.stackSize > inventoryStackLimit) {
							target.stackSize = inventoryStackLimit;
						}
					}
					copy.stackSize -= target.stackSize;
				}
				if (copy.stackSize == 0) {
					break;
				}
			}
		}
		return size - copy.stackSize;
	}

	public static int nextUpgradeId() {
		return nextTurtleId++;
	}

	public static InternalPlayer createPlayer(final World world, final ITurtleAccess turtle, final int direction) {
		final ChunkCoordinates position = turtle.getPosition();
		final InternalPlayer turtlePlayer = new InternalPlayer(world);
		turtlePlayer.posX = (position.posX + 0.5D);
		turtlePlayer.posY = (position.posY + 0.5D);
		turtlePlayer.posZ = (position.posZ + 0.5D);

		if (turtle.getPosition().equals(position)) {
			turtlePlayer.posX += 0.48D * Facing.offsetsXForSide[direction];
			turtlePlayer.posY += 0.48D * Facing.offsetsYForSide[direction];
			turtlePlayer.posZ += 0.48D * Facing.offsetsZForSide[direction];
		}

		if (direction > 2) {
			turtlePlayer.rotationYaw = toYawAngle(direction);
			turtlePlayer.rotationPitch = 0.0F;
		} else {
			turtlePlayer.rotationYaw = toYawAngle(turtle.getDirection());
			turtlePlayer.rotationPitch = toPitchAngle(direction);
		}
		turtlePlayer.prevPosX = turtlePlayer.posX;
		turtlePlayer.prevPosY = turtlePlayer.posY;
		turtlePlayer.prevPosZ = turtlePlayer.posZ;
		turtlePlayer.prevRotationPitch = turtlePlayer.rotationPitch;
		turtlePlayer.prevRotationYaw = turtlePlayer.rotationYaw;
		return turtlePlayer;
	}

	private static float toYawAngle(final int dir) {
		switch (dir) {
		case 2:
			return 180.0F;
		case 3:
			return 0.0F;
		case 4:
			return 90.0F;
		case 5:
			return 270.0F;
		}
		return 0.0F;
	}

	private static float toPitchAngle(final int dir) {
		switch (dir) {
		case 0:
			return 90.0F;
		case 1:
			return -90.0F;
		}
		return 0.0F;
	}

	@SuppressWarnings("unchecked")
	public static Entity findEntity(final World world, final Vec3 vecStart, final Vec3 vecDir, double distance) {
		Vec3 vecEnd = vecStart.addVector(vecDir.xCoord * distance, vecDir.yCoord * distance, vecDir.zCoord * distance);

		final MovingObjectPosition result = world.clip(vecStart.addVector(0.0D, 0.0D, 0.0D),
				vecEnd.addVector(0.0D, 0.0D, 0.0D));
		if (result != null && result.typeOfHit == EnumMovingObjectType.TILE) {
			distance = vecStart.distanceTo(result.hitVec);
			vecEnd = vecStart.addVector(vecDir.xCoord * distance, vecDir.yCoord * distance, vecDir.zCoord * distance);
		}

		final float xStretch = Math.abs(vecDir.xCoord) > 0.25D ? 0.0F : 1.0F;
		final float yStretch = Math.abs(vecDir.yCoord) > 0.25D ? 0.0F : 1.0F;
		final float zStretch = Math.abs(vecDir.zCoord) > 0.25D ? 0.0F : 1.0F;
		final AxisAlignedBB bigBox = AxisAlignedBB.getBoundingBox(Math.min(vecStart.xCoord, vecEnd.xCoord) - 0.375F
				* xStretch, Math.min(vecStart.yCoord, vecEnd.yCoord) - 0.375F * yStretch,
				Math.min(vecStart.zCoord, vecEnd.zCoord) - 0.375F * zStretch, Math.max(vecStart.xCoord, vecEnd.xCoord)
						+ 0.375F * xStretch, Math.max(vecStart.yCoord, vecEnd.yCoord) + 0.375F * yStretch,
				Math.max(vecStart.zCoord, vecEnd.zCoord) + 0.375F * zStretch);

		Entity closest = null;
		double closestDist = 99.0D;
		final List<Entity> list = world.getEntitiesWithinAABBExcludingEntity(null, bigBox);
		for (final Entity entity : list) {
			if (entity.canBeCollidedWith()) {
				final AxisAlignedBB littleBox = entity.boundingBox;
				if (littleBox.isVecInside(vecStart)) {
					closest = entity;
					closestDist = 0.0D;
				} else {
					final MovingObjectPosition littleBoxResult = littleBox.calculateIntercept(vecStart, vecEnd);
					if (littleBoxResult != null) {
						final double dist = vecStart.distanceTo(littleBoxResult.hitVec);
						if (closest == null || dist <= closestDist) {
							closest = entity;
							closestDist = dist;
						}
					} else if (littleBox.intersectsWith(bigBox)) {
						if (closest == null) {
							closest = entity;
							closestDist = distance;
						}
					}
				}
			}
		}
		if (closest != null && closestDist <= distance) {
			return closest;
		}
		return null;
	}
}
