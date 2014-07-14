package anzac.peripherals.upgrade;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.item.ItemHoe;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeDirection;
import anzac.peripherals.annotations.TurtleUpgrade;

@TurtleUpgrade(adjective = "Farming")
public class HoeUpgrade extends ToolTurtleUpgrade {

	public HoeUpgrade(final ItemHoe tool, final int upgradeId) {
		super(new ItemStack(tool), upgradeId);
	}

	@Override
	protected boolean canDigBlock(final World world, final int x, final int y, final int z,
			final ForgeDirection opposite) {
		final int bid = world.getBlockId(x, y, z);
		final Block block = Block.blocksList[bid];
		return block.blockMaterial == Material.plants || block.blockMaterial == Material.cactus
				|| block.blockMaterial == Material.pumpkin || block.blockMaterial == Material.leaves
				|| block.blockMaterial == Material.vine;
	}

	@Override
	protected List<ItemStack> getBlockDrops(final World world, final int x, final int y, final int z,
			final ForgeDirection opposite) {
		final int id = world.getBlockId(x, y, z);
		final int meta = world.getBlockMetadata(x, y, z);
		final Block block = Block.blocksList[id];
		if (block == null) {
			return new ArrayList<ItemStack>();
		}
		return Block.blocksList[id].getBlockDropped(world, x, y, z, meta, 0);
	}
}
