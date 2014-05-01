package anzac.peripherals.blocks;

import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Icon;
import net.minecraft.world.World;
import anzac.peripherals.AnzacPeripheralsCore;
import anzac.peripherals.tiles.ChargeStationTileEntity;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class ChargeBlock extends BlockContainer {

	@SideOnly(Side.CLIENT)
	private Icon diamondIcon;
	@SideOnly(Side.CLIENT)
	private Icon ironIcon;
	@SideOnly(Side.CLIENT)
	private Icon goldIcon;

	public ChargeBlock(final int blockId, final Material material) {
		super(blockId, material);
		setCreativeTab(CreativeTabs.tabDecorations);
		setStepSound(Block.soundMetalFootstep);
		setHardness(4.5F);
		setResistance(10.0F);
		setUnlocalizedName("anzacchargestation");
		setTextureName("anzac:charge");
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

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public void getSubBlocks(final int par1, final CreativeTabs par2CreativeTabs, final List par3List) {
		par3List.add(new ItemStack(par1, 1, 1));
		par3List.add(new ItemStack(par1, 1, 2));
		par3List.add(new ItemStack(par1, 1, 3));
	}

	@Override
	public TileEntity createTileEntity(final World world, final int metadata) {
		return new ChargeStationTileEntity(metadata);
	}

	@Override
	public int damageDropped(int par1) {
		return par1;
	}
}