package anzac.peripherals.upgrade;

import java.util.Arrays;
import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemShears;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeDirection;
import net.minecraftforge.common.IShearable;
import anzac.peripherals.annotations.TurtleUpgrade;
import dan200.computercraft.api.turtle.ITurtleAccess;
import dan200.computercraft.api.turtle.TurtleCommandResult;

@TurtleUpgrade(adjective = "Shearing")
public class ShearingUpgrade extends ToolTurtleUpgrade {
	private static final List<Block> SHEAR_DIG = Arrays.asList(new Block[] { Block.cloth, Block.web });

	public ShearingUpgrade(final ItemShears shears, final int id) {
		super(new ItemStack(shears), id);
	}

	@Override
	protected boolean canDigBlock(final World world, final int x, final int y, final int z,
			final ForgeDirection opposite) {
		final Block block = Block.blocksList[world.getBlockId(x, y, z)];
		return ((block instanceof IShearable)) || (SHEAR_DIG.contains(block));
	}

	@Override
	protected List<ItemStack> getBlockDrops(final World world, final int x, final int y, final int z,
			final ForgeDirection opposite) {
		final Block block = Block.blocksList[world.getBlockId(x, y, z)];
		return SHEAR_DIG.contains(block) ? super.getBlockDrops(world, x, y, z, opposite) : ((IShearable) block)
				.onSheared(getCraftingItem(), world, x, y, z, 0);
	}

	@Override
	protected boolean canAttackEntity(final ITurtleAccess turtle, final Entity entityHit) {
		return (entityHit instanceof IShearable)
				&& ((IShearable) entityHit).isShearable(getCraftingItem(), entityHit.worldObj, (int) entityHit.posX,
						(int) entityHit.posY, (int) entityHit.posZ);
	}

	@Override
	protected TurtleCommandResult attack(final ITurtleAccess turtle, final Entity entityHit) {
		for (final ItemStack stack : ((IShearable) entityHit).onSheared(getCraftingItem(), entityHit.worldObj,
				(int) entityHit.posX, (int) entityHit.posY, (int) entityHit.posZ, 0)) {
			storeOrDrop(turtle, stack);
		}
		return TurtleCommandResult.success();
	}
}
