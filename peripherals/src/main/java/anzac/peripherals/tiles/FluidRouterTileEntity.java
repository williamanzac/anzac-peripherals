package anzac.peripherals.tiles;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.ForgeDirection;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidContainerRegistry;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fluids.FluidTankInfo;
import net.minecraftforge.fluids.IFluidHandler;
import anzac.peripherals.AnzacPeripheralsCore;
import anzac.peripherals.utils.Position;
import anzac.peripherals.utils.Utils;
import dan200.computer.api.IComputerAccess;
import dan200.computer.api.ILuaContext;

public class FluidRouterTileEntity extends BasePeripheralTileEntity implements
		IFluidHandler {

	public FluidTank fluidTank = new FluidTank(FluidContainerRegistry.BUCKET_VOLUME);
	private final Map<FluidStack, ForgeDirection> fluidRules = new HashMap<FluidStack, ForgeDirection>();
	private ForgeDirection defaultRoute = ForgeDirection.UNKNOWN;
	private String label;

	private static enum Method {
		addRule, removeRule, listRules, setDefault, extract, getLabel, setLabel;

		public static String[] methodNames() {
			final Method[] values = Method.values();
			final String[] methods = new String[values.length];
			for (final Method method : values) {
				methods[method.ordinal()] = method.name();
			}
			return methods;
		}

		public static Method getMethod(final int ordinal) {
			for (final Method method : Method.values()) {
				if (method.ordinal() == ordinal) {
					return method;
				}
			}
			return null;
		}
	}

	@Override
	public String getType() {
		return "FluidRouter";
	}

	@Override
	public String[] getMethodNames() {
		return Method.methodNames();
	}

	@Override
	public Object[] callMethod(final IComputerAccess computer, final ILuaContext context, final int method,
			final Object[] arguments) throws Exception {
		Object ret = null;
		switch (Method.getMethod(method)) {
		case addRule:
			ret = false;
			fluidRules.put(new FluidStack(((Double) arguments[0]).intValue(), 1),
					ForgeDirection.getOrientation(((Double) arguments[1]).intValue()));
			ret = true;
			break;
		case removeRule:
			final Integer id = ((Double) arguments[0]).intValue();
			ret = false;
			for (final Entry<FluidStack, ForgeDirection> entry : fluidRules.entrySet()) {
				final FluidStack key = entry.getKey();
				if (key.fluidID == id) {
					fluidRules.remove(key);
					ret = true;
					break;
				}
			}
			break;
		case listRules:
			final List<String> lines = new ArrayList<String>();
			for (final Entry<FluidStack, ForgeDirection> entry : fluidRules.entrySet()) {
				lines.add(entry.getKey().fluidID + "=>" + entry.getValue());
			}
			return lines.toArray();
		case setDefault:
			defaultRoute = ForgeDirection.getOrientation(((Double) arguments[0]).intValue());
			ret = true;
			break;
		case extract:
			final ForgeDirection fromDir = ForgeDirection.getOrientation(((Double) arguments[0]).intValue());
			final int extractId = ((Double) arguments[1]).intValue();
			final int amount = ((Double) arguments[2]).intValue();
			final Position pos = new Position(xCoord, yCoord, zCoord, fromDir);
			pos.moveForwards(1);
			final TileEntity te = worldObj.getBlockTileEntity((int) pos.x, (int) pos.y, (int) pos.z);
			if (te == null || !(te instanceof IFluidHandler)) {
				throw new Exception("Fluid Handler not found");
			}
			final IFluidHandler inv = (IFluidHandler) te;
			final ForgeDirection opposite = fromDir.getOpposite();
			if (inv.canDrain(opposite, FluidRegistry.getFluid(extractId))) {
				final FluidStack fluidStack = inv.drain(opposite, amount, true);
				fill(fromDir, fluidStack, true);
				ret = fluidStack.amount;
			}
			break;
		case getLabel:
			ret = label;
			break;
		case setLabel:
			label = (String) arguments[0];
			break;
		}
		return ret == null ? null : new Object[] { ret };
	}

	@Override
	public void attach(final IComputerAccess computer) {
		super.attach(computer);
		AnzacPeripheralsCore.fluidRouterMap.put(computer.getID(), this);
	}

	@Override
	public void detach(final IComputerAccess computer) {
		AnzacPeripheralsCore.fluidRouterMap.remove(computer.getID());
		super.detach(computer);
	}

	@Override
	public void readFromNBT(final NBTTagCompound tagCompound) {
		super.readFromNBT(tagCompound);

		// read label
		if (tagCompound.hasKey("label")) {
			label = tagCompound.getString("label");
		}

		// read tank
		if (tagCompound.hasKey("fluid")) {
			final NBTTagCompound tagFluid = (NBTTagCompound) tagCompound.getTag("fluid");
			fluidTank.readFromNBT(tagFluid);
		}

		// read default Route
		final int d = tagCompound.getInteger("default");
		defaultRoute = ForgeDirection.getOrientation(d);

		// read rules
		final NBTTagList ruleList = tagCompound.getTagList("rules");
		for (byte entry = 0; entry < ruleList.tagCount(); entry++) {
			final NBTTagCompound fluidTag = (NBTTagCompound) ruleList.tagAt(entry);
			final int direction = fluidTag.getInteger("direction");
			final FluidStack stack = FluidStack.loadFluidStackFromNBT(fluidTag);
			fluidRules.put(stack, ForgeDirection.getOrientation(direction));
		}
	}

	@Override
	public void writeToNBT(final NBTTagCompound tagCompound) {
		super.writeToNBT(tagCompound);

		// write label
		if (label != null) {
			tagCompound.setString("label", label);
		}

		// write tank
		if (fluidTank != null) {
			final NBTTagCompound tagFluid = new NBTTagCompound();
			fluidTank.writeToNBT(tagFluid);
			tagCompound.setTag("fluid", tagFluid);
		}

		// write default Route
		tagCompound.setInteger("default", defaultRoute.ordinal());

		// write rules
		final NBTTagList ruleList = new NBTTagList();
		for (final Entry<FluidStack, ForgeDirection> entry : fluidRules.entrySet()) {
			final NBTTagCompound fluidTag = new NBTTagCompound();
			fluidTag.setInteger("direction", entry.getValue().ordinal());
			entry.getKey().writeToNBT(fluidTag);
			ruleList.appendTag(fluidTag);
		}
		tagCompound.setTag("rules", ruleList);
	}

	@Override
	public int fill(final ForgeDirection from, final FluidStack resource, final boolean doFill) {
		if (resource == null) {
			return 0;
		}
		final int fill = fluidTank.fill(resource, doFill);
		if (fill > 0 && doFill) {
			for (final IComputerAccess computer : computers.keySet()) {
				computer.queueEvent("fluid_sort", new Object[] { resource.fluidID, resource.amount });
			}
		}
		return fill;
	}

	@Override
	public FluidStack drain(final ForgeDirection from, final FluidStack resource, final boolean doDrain) {
		// cannot drain
		return null;
	}

	@Override
	public FluidStack drain(final ForgeDirection from, final int maxDrain, final boolean doDrain) {
		// cannot drain
		return null;
	}

	@Override
	public boolean canFill(final ForgeDirection from, final Fluid fluid) {
		return true;
	}

	@Override
	public boolean canDrain(final ForgeDirection from, final Fluid fluid) {
		return false;
	}

	@Override
	public FluidTankInfo[] getTankInfo(final ForgeDirection from) {
		return new FluidTankInfo[] { fluidTank.getInfo() };
	}

	@Override
	public void updateEntity() {
		if (worldObj == null) { // sanity check
			return;
		}

		super.updateEntity();
		if (!worldObj.isRemote) {
			if (fluidTank != null && fluidTank.getFluid() != null && fluidTank.getFluid().amount > 0
					&& worldObj.getTotalWorldTime() % 10 == 0 && isConnected()) {
				routeFluid();
			}
		}
	}

	private void routeFluid() {
		ForgeDirection side = defaultRoute;
		final FluidStack copy = fluidTank.getFluid().copy();
		copy.amount = 10;
		for (final Entry<FluidStack, ForgeDirection> entry : fluidRules.entrySet()) {
			final FluidStack key = entry.getKey();
			if (Utils.stacksMatch(key, copy)) {
				side = entry.getValue();
				break;
			}
		}

		if (side == ForgeDirection.UNKNOWN) {
			defaultRoute(copy);
		} else {
			routeTo(side, copy);
		}
		if (copy.amount == 0) {
			fluidTank.drain(10, true);
		}
	}

	protected void routeTo(final ForgeDirection side, final FluidStack copy) {
		copy.amount -= Utils.addToFluidHandler(worldObj, xCoord, yCoord, zCoord, side, copy);
	}

	protected void defaultRoute(final FluidStack copy) {
		copy.amount -= Utils.addToRandomFluidHandler(worldObj, xCoord, yCoord, zCoord, copy);
	}
}
