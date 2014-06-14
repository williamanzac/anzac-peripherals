package anzac.peripherals.peripheral;

import java.util.List;
import java.util.Map;

import net.minecraftforge.common.ForgeDirection;
import anzac.peripherals.annotations.Peripheral;
import anzac.peripherals.annotations.PeripheralMethod;
import anzac.peripherals.tiles.CraftingRouterTileEntity;
import anzac.peripherals.utils.ClassUtils;

@Peripheral(type = "CraftingRouter")
public class CraftingRouterPeripheral extends ItemRouterPeripheral {

	public CraftingRouterPeripheral(CraftingRouterTileEntity entity) {
		super(entity);
	}

	@Override
	protected CraftingRouterTileEntity getEntity() {
		return (CraftingRouterTileEntity) super.getEntity();
	}

	@Override
	protected List<String> methodNames() {
		return ClassUtils.getMethodNames(CraftingRouterPeripheral.class);
	}

	/**
	 * @return
	 * @throws Exception
	 */
	@PeripheralMethod
	public Object[] getRecipes() throws Exception {
		return getEntity().getRecipes();
	}

	/**
	 * @param id
	 * @return
	 * @throws Exception
	 */
	@PeripheralMethod
	public Map<Integer, Integer> loadRecipe(final int id) throws Exception {
		return getEntity().loadRecipe(id);
	}

	/**
	 * @param id
	 * @return
	 * @throws Exception
	 */
	@PeripheralMethod
	public boolean removeRecipe(final int id) throws Exception {
		return getEntity().removeRecipe(id);
	}

	/**
	 * @return
	 * @throws Exception
	 */
	@PeripheralMethod
	public boolean addRecipe() throws Exception {
		return getEntity().addRecipe();
	}

	/**
	 * @param uuid
	 * @param side
	 * @throws Exception
	 */
	@PeripheralMethod
	public void craft(final int uuid, final ForgeDirection side) throws Exception {
		craft(uuid, side, side.getOpposite());
	}

	/**
	 * @param uuid
	 * @param side
	 * @param inputDir
	 * @throws Exception
	 */
	@PeripheralMethod
	public void craft(final int uuid, final ForgeDirection side, final ForgeDirection inputDir) throws Exception {
		getEntity().craft(uuid, side, inputDir);
	}
}
