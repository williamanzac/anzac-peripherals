package anzac.peripherals.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeDirection;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidHandler;
import net.minecraftforge.oredict.OreDictionary;
import buildcraft.api.transport.IPipeTile;
import buildcraft.api.transport.IPipeTile.PipeType;

public class Utils {
	private static final List<ForgeDirection> directions = new ArrayList<ForgeDirection>(
			Arrays.asList(ForgeDirection.VALID_DIRECTIONS));
	private static final Random RANDOM = new Random();

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
		return mergeItemStack(sourceStack, targetStack, false);
	}

	public static boolean mergeItemStack(final ItemStack sourceStack, final ItemStack targetStack,
			final boolean ignoreMax) {
		boolean merged = false;

		if (sourceStack.isStackable()) {
			if (stacksMatch(targetStack, sourceStack)) {
				final int l = targetStack.stackSize + sourceStack.stackSize;

				if (ignoreMax || l <= sourceStack.getMaxStackSize()) {
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
		pos.moveForwards(1.0);

		final TileEntity tile = world.getBlockTileEntity((int) pos.x, (int) pos.y, (int) pos.z);
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
			inv.setInventorySlotContents(slot, stack);
			stack.stackSize = 0;
		} else {
			final boolean merged = Utils.mergeItemStack(stack, stackInSlot);
			if (merged) {
				inv.setInventorySlotContents(slot, stackInSlot);
			}
		}
	}

	public static int addToInventory(final World world, final int x, final int y, final int z,
			final ForgeDirection side, final ItemStack stack) {
		final Position pos = new Position(x, y, z, side);
		pos.moveForwards(1.0);

		final TileEntity tileInventory = world.getBlockTileEntity((int) pos.x, (int) pos.y, (int) pos.z);
		if (tileInventory != null && tileInventory instanceof IInventory) {
			final IInventory inv = (IInventory) tileInventory;
			final ItemStack copy = stack.copy();
			if (tileInventory instanceof ISidedInventory) {
				final ISidedInventory sidedInventory = (ISidedInventory) inv;
				final int opposite = side.getOpposite().ordinal();
				final int[] availableSlots = sidedInventory.getAccessibleSlotsFromSide(opposite);
				for (final int slot : availableSlots) {
					if (sidedInventory.canInsertItem(slot, copy, opposite)) {
						transferToSlot(sidedInventory, slot, copy);
						if (copy.stackSize == 0) {
							break;
						}
					}
				}
			} else {
				for (int j = 0; j < inv.getSizeInventory(); j++) {
					transferToSlot(inv, j, copy);
					if (copy.stackSize == 0) {
						break;
					}
				}
			}
			return stack.stackSize - copy.stackSize;
		}
		return 0;
	}

	public static int addToFluidHandler(final World world, final int x, final int y, final int z,
			final ForgeDirection side, final FluidStack stack) {
		final Position pos = new Position(x, y, z, side);
		pos.moveForwards(1.0);

		final TileEntity tileInventory = world.getBlockTileEntity((int) pos.x, (int) pos.y, (int) pos.z);
		if (tileInventory != null && tileInventory instanceof IFluidHandler) {
			final ForgeDirection opposite = side.getOpposite();
			final FluidStack copy = stack.copy();
			final IFluidHandler tank = (IFluidHandler) tileInventory;
			if (tank.canFill(opposite, copy.getFluid())) {
				copy.amount -= tank.fill(opposite, copy, true);
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
		return (stack.getItemDamage() << 15) + stack.itemID;
	}

	public static ItemStack getUUID(final int uuid) {
		return getUUID(uuid, 1);
	}

	public static ItemStack getUUID(final int uuid, final int stackSize) {
		final int meta = uuid >> 15;
		final int id = uuid & 32767;
		return new ItemStack(id, stackSize, meta);
	}

	public static int getUUID(final FluidStack stack) {
		return stack.fluidID;
	}
}