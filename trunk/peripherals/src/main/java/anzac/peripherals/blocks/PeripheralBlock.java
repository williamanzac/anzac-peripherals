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
import net.minecraftforge.common.ForgeDirection;
import net.minecraftforge.fluids.FluidContainerRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidHandler;
import anzac.peripherals.AnzacPeripheralsCore;
import anzac.peripherals.tiles.FluidRouterTileEntity;
import anzac.peripherals.tiles.FluidStorageTileEntity;
import anzac.peripherals.tiles.ItemRouterTileEntity;
import anzac.peripherals.tiles.ItemStorageTileEntity;
import anzac.peripherals.tiles.RecipeStorageTileEntity;
import anzac.peripherals.tiles.WorkbenchTileEntity;
import anzac.peripherals.utils.Utils;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import dan200.ComputerCraft;

public class PeripheralBlock extends BlockContainer {

	@SideOnly(Side.CLIENT)
	private Icon workbenchIconTop;
	@SideOnly(Side.CLIENT)
	private Icon workbenchIconFront;
	@SideOnly(Side.CLIENT)
	private Icon routerIcon;
	@SideOnly(Side.CLIENT)
	private Icon routerIconSide;
	@SideOnly(Side.CLIENT)
	private Icon routerFluidIcon;
	@SideOnly(Side.CLIENT)
	private Icon routerFluidIconSide;
	@SideOnly(Side.CLIENT)
	private Icon itemStorageSide;
	@SideOnly(Side.CLIENT)
	private Icon itemStorageFront;
	@SideOnly(Side.CLIENT)
	private Icon fluidStorageSide;

	public PeripheralBlock(final int blockId, final Material material) {
		super(blockId, material);
		setCreativeTab(CreativeTabs.tabDecorations);
		setStepSound(Block.soundStoneFootstep);
		setHardness(1.5F);
		setResistance(10.0F);
		setUnlocalizedName("anzacperipheral");
		setTextureName("anzac:crafting_table");
	}

	@Override
	public TileEntity createNewTileEntity(final World world) {
		return null;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public Icon getIcon(final int side, final int metaData) {
		switch (metaData) {
		case 0: // workbench
			switch (side) {
			case 1:
				return workbenchIconTop;
			case 2:
				return workbenchIconFront;
			case 3:
				return blockIcon;
			default:
				return ComputerCraft.Blocks.computer.getIcon(side, 1);
			}
		case 1: // recipe storage
			return side == 1 ? workbenchIconTop : ComputerCraft.Blocks.peripheral.getIcon(side, 0);
		case 2: // item router
			switch (side) {
			case 0:
			case 1:
				return routerIcon;
			default:
				return routerIconSide;
			}
		case 3: // fluid router
			switch (side) {
			case 0:
			case 1:
				return routerFluidIcon;
			default:
				return routerFluidIconSide;
			}
		case 4: // item storage
			switch (side) {
			case 2:
				return itemStorageFront;
			case 3:
			case 4:
			case 5:
				return itemStorageSide;
			default:
				return ComputerCraft.Blocks.peripheral.getIcon(side, 0);
			}
		case 5: // fluid storage
			switch (side) {
			case 0:
			case 1:
				return ComputerCraft.Blocks.peripheral.getIcon(side, 0);
			default:
				return fluidStorageSide;
			}
		}
		return null;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void registerIcons(final IconRegister par1IconRegister) {
		blockIcon = par1IconRegister.registerIcon(this.getTextureName() + "_side");
		workbenchIconTop = par1IconRegister.registerIcon(this.getTextureName() + "_top");
		workbenchIconFront = par1IconRegister.registerIcon(this.getTextureName() + "_front");
		routerIcon = par1IconRegister.registerIcon("anzac:router");
		routerIconSide = par1IconRegister.registerIcon("anzac:router_side");
		routerFluidIcon = par1IconRegister.registerIcon("anzac:router_fluid");
		routerFluidIconSide = par1IconRegister.registerIcon("anzac:router_fluid_side");
		itemStorageSide = par1IconRegister.registerIcon("anzac:storage_side");
		itemStorageFront = par1IconRegister.registerIcon("anzac:storage_front");
		fluidStorageSide = par1IconRegister.registerIcon("anzac:fluid_storage_side");
	}

	@Override
	public boolean onBlockActivated(final World world, final int x, final int y, final int z,
			final EntityPlayer player, final int metadata, final float what, final float these, final float are) {
		if (world.isRemote) {
			return true;
		}
		final TileEntity tileEntity = world.getBlockTileEntity(x, y, z);
		if (tileEntity == null || player.isSneaking()) {
			return false;
		}
		if (tileEntity instanceof IFluidHandler) {
			final IFluidHandler tank = (IFluidHandler) tileEntity;
			final ItemStack current = player.inventory.getCurrentItem();
			if (current != null) {
				FluidStack liquid = FluidContainerRegistry.getFluidForFilledItem(current);

				// Handle filled containers
				if (liquid != null) {
					final int qty = tank.fill(ForgeDirection.UNKNOWN, liquid, true);

					if (qty != 0 && !player.capabilities.isCreativeMode) {
						player.inventory.setInventorySlotContents(player.inventory.currentItem,
								Utils.consumeItem(current));
					}

					return true;
				} else {
					// Handle empty containers
					final FluidStack available = tank.getTankInfo(ForgeDirection.UNKNOWN)[0].fluid;
					if (available != null) {
						final ItemStack filled = FluidContainerRegistry.fillFluidContainer(available, current);

						liquid = FluidContainerRegistry.getFluidForFilledItem(filled);

						if (liquid != null) {
							if (!player.capabilities.isCreativeMode) {
								if (current.stackSize > 1) {
									if (!player.inventory.addItemStackToInventory(filled))
										return false;
									else {
										player.inventory.setInventorySlotContents(player.inventory.currentItem,
												Utils.consumeItem(current));
									}
								} else {
									player.inventory.setInventorySlotContents(player.inventory.currentItem,
											Utils.consumeItem(current));
									player.inventory.setInventorySlotContents(player.inventory.currentItem, filled);
								}
							}
							tank.drain(ForgeDirection.UNKNOWN, liquid.amount, true);
							return true;
						}
					}
				}
			}
		}
		player.openGui(AnzacPeripheralsCore.instance, 0, world, x, y, z);
		return true;
	}

	@Override
	public void getSubBlocks(final int par1, final CreativeTabs par2CreativeTabs, final List par3List) {
		par3List.add(new ItemStack(par1, 1, 0));
		par3List.add(new ItemStack(par1, 1, 1));
		par3List.add(new ItemStack(par1, 1, 2));
		par3List.add(new ItemStack(par1, 1, 3));
		par3List.add(new ItemStack(par1, 1, 4));
		par3List.add(new ItemStack(par1, 1, 5));
	}

	@Override
	public TileEntity createTileEntity(final World world, final int metadata) {
		switch (metadata) {
		case 0:
			return new WorkbenchTileEntity();
		case 1:
			return new RecipeStorageTileEntity();
		case 2:
			return new ItemRouterTileEntity();
		case 3:
			return new FluidRouterTileEntity();
		case 4:
			return new ItemStorageTileEntity();
		case 5:
			return new FluidStorageTileEntity();
		}
		return null;
	}

	// @Override
	// public void onBlockAdded(final World world, final int x, final int y, final int z) {
	// if (world.isRemote) {
	// return;
	// }
	// final TileEntity entity = world.getBlockTileEntity(x, y, z);
	// if (entity instanceof FluidStorageTileEntity) {
	// final FluidStorageTileEntity tr = (FluidStorageTileEntity) entity;
	// tr.onBlockAdded();
	// }
	// }

	// @Override
	// public void onNeighborBlockChange(World world, int x, int y, int z, int blockId) {
	// if (world.isRemote) {
	// return;
	// }
	// final TileEntity entity = world.getBlockTileEntity(x, y, z);
	// if (entity instanceof FluidStorageTileEntity) {
	// final FluidStorageTileEntity te = (FluidStorageTileEntity) entity;
	// te.onNeighborBlockChange(blockId);
	// }
	// }

	// @Override
	// public boolean removeBlockByPlayer(World world, EntityPlayer player, int x, int y, int z) {
	// if (!world.isRemote) {
	// TileEntity te = world.getBlockTileEntity(x, y, z);
	// if (te instanceof FluidStorageTileEntity) {
	// FluidStorageTileEntity cb = (FluidStorageTileEntity) te;
	// cb.onBreakBlock();
	// }
	// }
	// return super.removeBlockByPlayer(world, player, x, y, z);
	// }
}