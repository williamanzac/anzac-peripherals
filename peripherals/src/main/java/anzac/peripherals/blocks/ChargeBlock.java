package anzac.peripherals.blocks;

import java.util.List;

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
import anzac.peripherals.items.ChargeItem;
import anzac.peripherals.peripheral.ChargeStationPeripheral;
import anzac.peripherals.tiles.ChargeStationTileEntity;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/**
 * This peripheral will transfer it's internal power to any turtles parked next to it. It can accept either MJ or RF
 * power. An Iron Charge Station can store a maximum of 25 units of fuel. A Gold Charge Station can store a maximum of
 * 250 units of fuel. A Diamond Charge Station can store up to 2500 units of fuel. By default 1 unit of fuel is equal to
 * 20 MJ.
 * 
 * @author Tony
 */
@Blocks(itemType = ChargeItem.class, key = "block.anzac.chargestation", tool = "pickaxe", toolLevel = 1, tileType = ChargeStationTileEntity.class, peripheralType = ChargeStationPeripheral.class)
public class ChargeBlock extends BlockContainer {

	@SideOnly(Side.CLIENT)
	private Icon diamondIcon;
	@SideOnly(Side.CLIENT)
	private Icon ironIcon;
	@SideOnly(Side.CLIENT)
	private Icon goldIcon;

	public ChargeBlock(final int blockId) {
		super(blockId, Material.iron);
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

	@SuppressWarnings({ "rawtypes" })
	@Override
	public void getSubBlocks(final int par1, final CreativeTabs par2CreativeTabs, final List par3List) {
		BlockFactory.getSubBlocks(getClass(), par1, par3List);
	}

	@Override
	public TileEntity createTileEntity(final World world, final int metadata) {
		try {
			return new ChargeStationTileEntity(metadata);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public int damageDropped(int par1) {
		return par1;
	}
}