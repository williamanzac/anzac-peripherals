package anzac.peripherals.upgrade;

import net.minecraft.block.Block;
import net.minecraft.client.renderer.texture.IconRegister;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Icon;
import anzac.peripherals.annotations.TurtleUpgrade;
import anzac.peripherals.peripheral.FurnaceUpgradePeripheral;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import dan200.computercraft.api.turtle.ITurtleAccess;
import dan200.computercraft.api.turtle.TurtleSide;

@TurtleUpgrade(adjective = "Smelting", peripheralType = FurnaceUpgradePeripheral.class)
public class FurnaceUpgrade extends PeripheralTurtleUpgrade implements UpgradeIcon {

	@SideOnly(Side.CLIENT)
	private Icon icon;

	public FurnaceUpgrade(final int upgradeId) {
		super(new ItemStack(Block.furnaceIdle), upgradeId);
	}

	@Override
	public void update(final ITurtleAccess turtle, final TurtleSide side) {
	}

	@Override
	public Icon getIcon(final ITurtleAccess turtle, final TurtleSide side) {
		return icon;
	}

	@SideOnly(Side.CLIENT)
	@Override
	public void registerIcons(IconRegister par1IconRegister) {
		icon = par1IconRegister.registerIcon("anzac:furnace_upgrade");
	}
}
