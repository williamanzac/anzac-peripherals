package anzac.peripherals.blocks;

import java.util.List;
import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Icon;
import net.minecraft.world.World;
import anzac.peripherals.AnzacPeripheralsCore;
import anzac.peripherals.annotations.Blocks;
import anzac.peripherals.items.TeleporterItem;
import anzac.peripherals.peripheral.TeleporterPeripheral;
import anzac.peripherals.tiles.TeleporterTileEntity;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/**
 * This peripheral can use it internal power to teleport any turtle next to it. It can accept either MJ or RF power. A
 * memory card must be placed inside with the desired destination. It can even be in another dimension. An Iron
 * Teleporter can only have 1 destination and can store up to 250 fuel units. A Gold Teleporter can have 2 destinations
 * and stores up to 2500 units. A Diamond Teleporter can have 4 destination and stores up to 25000 units.
 * 
 * @author Tony
 */
@Blocks(itemType = TeleporterItem.class, key = "block.anzac.teleporter", tool = "pickaxe", toolLevel = 2, tileType = TeleporterTileEntity.class, peripheralType = TeleporterPeripheral.class)
public class TeleporterBlock extends BlockContainer {

	@SideOnly(Side.CLIENT)
	private Icon diamondIcon;
	@SideOnly(Side.CLIENT)
	private Icon ironIcon;
	@SideOnly(Side.CLIENT)
	private Icon goldIcon;

	public TeleporterBlock(final int blockId) {
		super(blockId, Material.rock);
		setCreativeTab(CreativeTabs.tabDecorations);
		setStepSound(Block.soundStoneFootstep);
		setHardness(40.0F);
		setResistance(1900.0F);
		setUnlocalizedName("anzacteleporter");
		setTextureName("anzac:teleport");
		setLightValue(0.5F);
	}

	@Override
	public TileEntity createNewTileEntity(final World world) {
		return null;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public Icon getIcon(final int side, final int metaData) {
		switch (metaData) {
		case 1: // iron
			return ironIcon;
		case 2: // gold
			return goldIcon;
		case 3: // diamond
			return diamondIcon;
		}
		return super.getIcon(side, metaData);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void registerIcons(final IconRegister par1IconRegister) {
		ironIcon = par1IconRegister.registerIcon(this.getTextureName() + "_iron_side");
		goldIcon = par1IconRegister.registerIcon(this.getTextureName() + "_gold_side");
		diamondIcon = par1IconRegister.registerIcon(this.getTextureName() + "_diamond_side");
	}

	@Override
	public boolean onBlockActivated(final World world, final int x, final int y, final int z,
			final EntityPlayer player, final int metadata, final float what, final float these, final float are) {
		final TileEntity tileEntity = world.getBlockTileEntity(x, y, z);
		if (tileEntity == null || player.isSneaking()) {
			return false;
		}
		player.openGui(AnzacPeripheralsCore.instance, 0, world, x, y, z);
		return true;
	}

	@SuppressWarnings({ "rawtypes" })
	@Override
	public void getSubBlocks(final int par1, final CreativeTabs par2CreativeTabs, final List par3List) {
		BlockFactory.getSubBlocks(getClass(), par1, par3List);
	}

	@Override
	public TileEntity createTileEntity(final World world, final int metadata) {
		try {
			return new TeleporterTileEntity(metadata);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public void randomDisplayTick(final World world, final int x, final int y, final int z, final Random random) {
		final TileEntity tileEntity = world.getBlockTileEntity(x, y, z);
		if (tileEntity != null && (tileEntity instanceof TeleporterTileEntity)) {
			final int num = world.getBlockMetadata(x, y, z);
			for (int l = 0; l < num; ++l) {
				final double d1 = y + random.nextFloat();
				final int i1 = random.nextInt(2) * 2 - 1;
				final int j1 = random.nextInt(2) * 2 - 1;
				final double d3 = (random.nextFloat() - 0.5D) * 0.125D;
				final double d5 = z + 0.5D + 0.25D * j1;
				final double d4 = random.nextFloat() * 1.0F * j1;
				final double d6 = x + 0.5D + 0.25D * i1;
				final double d2 = random.nextFloat() * 1.0F * i1;
				world.spawnParticle("portal", d6, d1, d5, d2, d3, d4);
			}
		}
	}

	@Override
	public int damageDropped(final int par1) {
		return par1;
	}
}