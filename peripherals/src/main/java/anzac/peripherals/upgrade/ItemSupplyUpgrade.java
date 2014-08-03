package anzac.peripherals.upgrade;

import static anzac.peripherals.AnzacPeripheralsCore.peripheralBlockId;
import net.minecraft.client.renderer.texture.IconRegister;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Icon;
import anzac.peripherals.annotations.TurtleUpgrade;
import anzac.peripherals.blocks.BlockType;
import anzac.peripherals.peripheral.ItemSupplierPeripheral;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import dan200.computercraft.api.turtle.ITurtleAccess;
import dan200.computercraft.api.turtle.TurtleSide;

/**
 * 
 * @author Tony
 */
@TurtleUpgrade(adjective = "Supplier", peripheralType = ItemSupplierPeripheral.class)
public class ItemSupplyUpgrade extends PeripheralTurtleUpgrade implements UpgradeIcon {

	@SideOnly(Side.CLIENT)
	private Icon icon;

	public ItemSupplyUpgrade(final int upgradeId) {
		super(new ItemStack(peripheralBlockId, 1, BlockType.ITEM_SUPPLIER.getMeta()), upgradeId);
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
		icon = par1IconRegister.registerIcon("anzac:supplier_upgrade");
	}
}
