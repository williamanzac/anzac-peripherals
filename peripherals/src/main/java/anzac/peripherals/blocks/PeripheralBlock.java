package anzac.peripherals.blocks;

import java.util.List;
import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Facing;
import net.minecraft.util.Icon;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeDirection;
import net.minecraftforge.fluids.FluidContainerRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTankInfo;
import net.minecraftforge.fluids.IFluidHandler;
import anzac.peripherals.AnzacPeripheralsCore;
import anzac.peripherals.annotations.Peripheral;
import anzac.peripherals.tiles.CraftingRouterTileEntity;
import anzac.peripherals.tiles.FluidRouterTileEntity;
import anzac.peripherals.tiles.FluidStorageTileEntity;
import anzac.peripherals.tiles.ItemRouterTileEntity;
import anzac.peripherals.tiles.ItemStorageTileEntity;
import anzac.peripherals.tiles.RecipeStorageTileEntity;
import anzac.peripherals.tiles.RedstoneTileEntity;
import anzac.peripherals.tiles.WorkbenchTileEntity;
import anzac.peripherals.utils.Position;
import anzac.peripherals.utils.Utils;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class PeripheralBlock extends BlockContainer {

	@SideOnly(Side.CLIENT)
	private Icon workbenchIconTop;
	@SideOnly(Side.CLIENT)
	private Icon workbenchIconFront;
	@SideOnly(Side.CLIENT)
	private Icon routerIconSide;
	@SideOnly(Side.CLIENT)
	private Icon routerFluidIconSide;
	@SideOnly(Side.CLIENT)
	private Icon itemStorageSide;
	@SideOnly(Side.CLIENT)
	private Icon itemStorageFront;
	@SideOnly(Side.CLIENT)
	private Icon fluidStorageSide;
	@SideOnly(Side.CLIENT)
	private Icon genericSide;
	@SideOnly(Side.CLIENT)
	private Icon redstoneSide;

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
			}
		case 1: // recipe storage
			switch (side) {
			case 1:
				return workbenchIconTop;
			case 2:
				return itemStorageFront;
			case 3:
			case 4:
			case 5:
				return itemStorageSide;
			}
		case 2: // item router
			return routerIconSide;
		case 3: // fluid router
			return routerFluidIconSide;
		case 4: // item storage
			switch (side) {
			case 2:
				return itemStorageFront;
			case 3:
			case 4:
			case 5:
				return itemStorageSide;
			}
		case 5: // fluid storage
			return side > 1 ? fluidStorageSide : genericSide;
		case 6: // redstone
			return redstoneSide;
		case 7: // crafting router
			switch (side) {
			case 1:
				return workbenchIconTop;
			default:
				return routerIconSide;
			}
		}
		return genericSide;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void registerIcons(final IconRegister par1IconRegister) {
		blockIcon = par1IconRegister.registerIcon(this.getTextureName() + "_side");
		workbenchIconTop = par1IconRegister.registerIcon(this.getTextureName() + "_top");
		workbenchIconFront = par1IconRegister.registerIcon(this.getTextureName() + "_front");
		routerIconSide = par1IconRegister.registerIcon("anzac:router_side");
		routerFluidIconSide = par1IconRegister.registerIcon("anzac:router_fluid_side");
		itemStorageSide = par1IconRegister.registerIcon("anzac:storage_side");
		itemStorageFront = par1IconRegister.registerIcon("anzac:storage_front");
		fluidStorageSide = par1IconRegister.registerIcon("anzac:fluid_storage_side");
		genericSide = par1IconRegister.registerIcon("anzac:generic_side");
		redstoneSide = par1IconRegister.registerIcon("anzac:redstone_side");
	}

	@Override
	public boolean onBlockActivated(final World world, final int x, final int y, final int z,
			final EntityPlayer player, final int metadata, final float what, final float these, final float are) {
		final TileEntity tileEntity = world.getBlockTileEntity(x, y, z);
		if (tileEntity == null || player.isSneaking()) {
			return false;
		}
		if (tileEntity instanceof FluidStorageTileEntity) {
			final IFluidHandler tank = (IFluidHandler) tileEntity;
			if (handleFluids(player, tank)) {
				return true;
			}
		}
		if (tileEntity instanceof ItemStorageTileEntity) {
			final IInventory inventory = (IInventory) tileEntity;
			if (handleItems(player, inventory)) {
				return true;
			}
		}
		if (tileEntity.getClass().isAnnotationPresent(Peripheral.class)) {
			if (!tileEntity.getClass().getAnnotation(Peripheral.class).hasGUI()) {
				return false;
			}
		}
		player.openGui(AnzacPeripheralsCore.instance, 0, world, x, y, z);
		return true;
	}

	private boolean handleItems(final EntityPlayer player, final IInventory inventory) {
		return false;
	}

	private boolean handleFluids(final EntityPlayer player, final IFluidHandler tank) {
		final ItemStack current = player.inventory.getCurrentItem();
		if (current != null) {
			FluidStack liquid = FluidContainerRegistry.getFluidForFilledItem(current);

			// Handle filled containers
			if (liquid != null) {
				final int qty = tank.fill(ForgeDirection.UNKNOWN, liquid, true);

				if (qty != 0 && !player.capabilities.isCreativeMode) {
					player.inventory.setInventorySlotContents(player.inventory.currentItem, Utils.consumeItem(current));
				}

				return true;
			} else {
				// Handle empty containers
				final FluidTankInfo[] tankInfo = tank.getTankInfo(ForgeDirection.UNKNOWN);
				if (tankInfo != null && tankInfo.length != 0) {
					final FluidStack available = tankInfo[0].fluid;
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
		return false;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public void getSubBlocks(final int par1, final CreativeTabs par2CreativeTabs, final List par3List) {
		par3List.add(new ItemStack(par1, 1, 0));
		par3List.add(new ItemStack(par1, 1, 1));
		par3List.add(new ItemStack(par1, 1, 2));
		par3List.add(new ItemStack(par1, 1, 3));
		par3List.add(new ItemStack(par1, 1, 4));
		par3List.add(new ItemStack(par1, 1, 5));
		par3List.add(new ItemStack(par1, 1, 6));
		par3List.add(new ItemStack(par1, 1, 7));
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
		case 6:
			return new RedstoneTileEntity();
		case 7:
			return new CraftingRouterTileEntity();
		}
		return null;
	}

	@Override
	public boolean isBlockNormalCube(final World world, final int x, final int y, final int z) {
		return true;
	}

	@Override
	public boolean canConnectRedstone(final IBlockAccess world, final int x, final int y, final int z, final int side) {
		final TileEntity entity = world.getBlockTileEntity(x, y, z);
		if (entity instanceof RedstoneTileEntity) {
			return true;
		}
		return super.canConnectRedstone(world, x, y, z, side);
	}

	@Override
	public int isProvidingWeakPower(final IBlockAccess world, final int x, final int y, final int z, final int side) {
		final TileEntity entity = world.getBlockTileEntity(x, y, z);
		if (entity instanceof RedstoneTileEntity) {
			return ((RedstoneTileEntity) entity).getOutput(Facing.oppositeSide[side]);
		}
		return super.isProvidingWeakPower(world, x, y, z, side);
	}

	@Override
	public void onNeighborBlockChange(final World world, final int x, final int y, final int z, final int neighborId) {
		super.onNeighborBlockChange(world, x, y, z, neighborId);
		final TileEntity entity = world.getBlockTileEntity(x, y, z);
		if (entity instanceof RedstoneTileEntity) {
			for (final ForgeDirection direction : ForgeDirection.VALID_DIRECTIONS) {
				((RedstoneTileEntity) entity)
						.setInput(direction.ordinal(), getInputStrength(world, x, y, z, direction));
			}
		}
	}

	@Override
	public boolean canProvidePower() {
		return true;
	}

	// @Override
	// public boolean shouldCheckWeakPower(final World world, final int x, final int y, final int z, final int side) {
	// final TileEntity entity = world.getBlockTileEntity(x, y, z);
	// if (entity instanceof RedstoneTileEntity) {
	// return true;
	// }
	// return super.shouldCheckWeakPower(world, x, y, z, side);
	// }

	protected int getInputStrength(final World world, final int x, final int y, final int z, final ForgeDirection side) {
		final Position p = new Position(x, y, z, side);
		p.moveForwards(1);
		final int l1 = world.getIndirectPowerLevelTo(p.x, p.y, p.z, side.getOpposite().ordinal());
		return l1 >= 15 ? l1 : Math.max(l1,
				world.getBlockId(p.x, p.y, p.z) == Block.redstoneWire.blockID ? world.getBlockMetadata(p.x, p.y, p.z)
						: 0);
	}

	@Override
	public void updateTick(final World world, final int x, final int y, final int z, final Random random) {
		super.updateTick(world, x, y, z, random);
		final TileEntity entity = world.getBlockTileEntity(x, y, z);
		if (entity instanceof RedstoneTileEntity) {
			for (final ForgeDirection direction : ForgeDirection.VALID_DIRECTIONS) {
				((RedstoneTileEntity) entity)
						.setInput(direction.ordinal(), getInputStrength(world, x, y, z, direction));
			}
		}
	}

	@Override
	public int isProvidingStrongPower(final IBlockAccess world, final int x, final int y, final int z, final int side) {
		return isProvidingWeakPower(world, x, y, z, side);
	}

	@Override
	public int damageDropped(final int par1) {
		return par1;
	}

	// @Override
	// public void onNeighborTileChange(final World world, final int x, final int y, final int z, final int tileX,
	// final int tileY, final int tileZ) {
	// if (y == tileY) {
	// onNeighborBlockChange(world, x, y, z, world.getBlockId(tileX, tileY, tileZ));
	// }
	// }

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