package anzac.peripherals.peripheral;

import java.util.List;

import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.FurnaceRecipes;
import net.minecraft.util.Facing;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeDirection;
import anzac.peripherals.annotations.Peripheral;
import anzac.peripherals.annotations.PeripheralMethod;
import anzac.peripherals.tiles.BasePeripheralTileEntity;
import anzac.peripherals.utils.ClassUtils;
import anzac.peripherals.utils.Position;
import anzac.peripherals.utils.Utils;
import dan200.computercraft.api.turtle.ITurtleAccess;

@Peripheral(type = "Furnace")
public class FurnaceUpgradePeripheral extends BasePeripheral {

	private static final int FUEL_TO_CONSUME = 10;

	public FurnaceUpgradePeripheral(final ITurtleAccess turtle) {
		super(turtle);
	}

	@Override
	protected BasePeripheralTileEntity getEntity() {
		return null;// return null as only need turtle
	}

	@Override
	protected List<String> methodNames() {
		return ClassUtils.getMethodNames(FurnaceUpgradePeripheral.class);
	}

	/**
	 * Smelt the currently selected item from the turtle's inventory. The output will be put in to the first available
	 * slot or it will be dropped on to the ground.
	 * 
	 * @param amount
	 *            How many of the item to smelt.
	 * @throws Exception
	 *             Return an error if the currently selected slot is empty, if there is not enough fuel left to smelt an
	 *             item or if the current item cannot be smelted.
	 */
	@PeripheralMethod
	public void smelt(final int amount) throws Exception {
		final int selectedSlot = turtle.getSelectedSlot();
		final ItemStack stackInSlot = turtle.getInventory().getStackInSlot(selectedSlot);
		if (stackInSlot == null || stackInSlot.stackSize <= 0) {
			throw new Exception("Selected slot is empty");
		}
		final ItemStack itemstack = FurnaceRecipes.smelting().getSmeltingResult(stackInSlot);
		if (itemstack == null) {
			throw new Exception("No Smelting recipe found");
		}
		for (int i = 0; i < amount; i++) {
			if (stackInSlot == null || stackInSlot.stackSize <= 0) {
				throw new Exception("Selected slot is empty");
			}
			if (!turtle.consumeFuel(FUEL_TO_CONSUME)) {
				throw new Exception("Not enough fuel");
			}
			storeOrDrop(itemstack.copy());
			stackInSlot.stackSize--;
		}
	}

	protected void storeOrDrop(final ItemStack stack) {
		stack.stackSize -= Utils.addToInventory(ForgeDirection.UNKNOWN, stack, turtle.getInventory());
		if (stack.stackSize > 0) {
			dropItemStack(stack);
		}
	}

	protected void dropItemStack(final ItemStack stack) {
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

	@Override
	public String getLabel() {
		// override as this peripheral does not need a label, it is part of a turtle
		return null;
	}

	@Override
	public void setLabel(final String label) {
		// override as this peripheral does not need a label, it is part of a turtle
	}
}
