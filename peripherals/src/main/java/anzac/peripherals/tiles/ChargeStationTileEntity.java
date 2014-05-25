package anzac.peripherals.tiles;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeDirection;
import anzac.peripherals.AnzacPeripheralsCore;
import anzac.peripherals.annotations.Peripheral;
import anzac.peripherals.annotations.PeripheralMethod;
import anzac.peripherals.utils.ClassUtils;
import anzac.peripherals.utils.Position;
import buildcraft.api.power.IPowerReceptor;
import buildcraft.api.power.PowerHandler;
import buildcraft.api.power.PowerHandler.PowerReceiver;
import buildcraft.api.power.PowerHandler.Type;
import dan200.computercraft.api.peripheral.IPeripheral;
import dan200.computercraft.api.turtle.ITurtleAccess;

/**
 * @author Tony
 * 
 */
@Peripheral(type = "ChargeStation")
public class ChargeStationTileEntity extends BasePeripheralTileEntity implements IPowerReceptor {

	private static final int MJ = AnzacPeripheralsCore.mjMultiplier;
	private PowerHandler handler = new PowerHandler(this, Type.MACHINE);
	private int type;

	public ChargeStationTileEntity() {
	}

	public ChargeStationTileEntity(final int metadata) {
		this();
		type = metadata;
		configure();
	}

	@Override
	protected List<String> methodNames() {
		return getMethodNames(ChargeStationTileEntity.class);
	}

	private void configure() {
		final double maxStorage = 500 * Math.pow(10, type - 1);
		final double maxIn = Math.max(Math.pow(2, type + 6), maxStorage * 0.01);
		handler.configure(1f, (float) maxIn, MJ, (float) maxStorage);
		handler.configurePowerPerdition(0, 0);
	}

	/**
	 * @return
	 */
	@PeripheralMethod
	public float getStoredEnergy() {
		return handler.getEnergyStored();
	}

	public void setStoredEnergy(final float stored) {
		handler.setEnergy(stored);
	}

	/**
	 * @return
	 */
	@PeripheralMethod
	public float getMaxEnergy() {
		return handler.getMaxEnergyStored();
	}

	@Override
	public PowerReceiver getPowerReceiver(final ForgeDirection side) {
		return handler.getPowerReceiver();
	}

	@Override
	public void doWork(final PowerHandler workProvider) {
		// transferPower(workProvider);
	}

	@Override
	public World getWorld() {
		return this.worldObj;
	}

	@Override
	public void updateEntity() {
		super.updateEntity();

		if (handler.getEnergyStored() <= 0) {
			return;
		}

		if (worldObj.isRemote) {
			return;
		}

		if (worldObj.getTotalWorldTime() % 20 == 0) {
			// AnzacPeripheralsCore.logger.info("update; stored: " + handler.getEnergyStored());
			transferPower(handler);
		}
	}

	private void transferPower(final PowerHandler workProvider) {
		final List<ITurtleAccess> turtles = new ArrayList<ITurtleAccess>();
		for (final ForgeDirection direction : ForgeDirection.values()) {
			final Position position = new Position(xCoord, yCoord, zCoord, direction);
			position.moveForwards(1);
			final TileEntity entity = worldObj.getBlockTileEntity(position.x, position.y, position.z);
			if (ClassUtils.instanceOf(entity, "dan200.computercraft.shared.turtle.blocks.ITurtleTile")) {
				AnzacPeripheralsCore.logger.info("found turtle");
				turtles.add((ITurtleAccess) ClassUtils.callMethod(entity, "getAccess", null));
			}
		}
		// AnzacPeripheralsCore.logger.info("found turtles: " + turtles);
		if (!turtles.isEmpty()) {
			AnzacPeripheralsCore.logger.info("has turtle; stored: " + workProvider.getEnergyStored());
			for (final ITurtleAccess turtle : turtles) {
				final float useEnergy = workProvider.useEnergy(MJ, MJ, false);
				AnzacPeripheralsCore.logger.info("useEnergy: " + useEnergy);
				if (useEnergy != MJ) {
					continue;
				}
				int amount = (int) (useEnergy / AnzacPeripheralsCore.mjMultiplier);
				AnzacPeripheralsCore.logger.info("amount: " + amount);
				final int fuelLevel = turtle.getFuelLevel();
				AnzacPeripheralsCore.logger.info("fuelLevel: " + fuelLevel);
				final int fuelLimit = turtle.getFuelLimit();
				AnzacPeripheralsCore.logger.info("fuelLimit: " + fuelLimit);
				if (fuelLimit - amount <= fuelLevel) {
					amount = fuelLimit - fuelLevel;
				}
				AnzacPeripheralsCore.logger.info("amount: " + amount);
				turtle.addFuel(amount);
				final int mj = amount * AnzacPeripheralsCore.mjMultiplier;
				workProvider.useEnergy(mj, mj, true);
			}
		}
	}

	@Override
	public void readFromNBT(final NBTTagCompound par1nbtTagCompound) {
		super.readFromNBT(par1nbtTagCompound);

		if (par1nbtTagCompound.hasKey("type")) {
			type = par1nbtTagCompound.getInteger("type");
		}
		if (handler != null) {
			handler.readFromNBT(par1nbtTagCompound);
		} else {
			handler = new PowerHandler(this, Type.MACHINE);
		}
		configure();
	}

	@Override
	public void writeToNBT(final NBTTagCompound par1nbtTagCompound) {
		super.writeToNBT(par1nbtTagCompound);

		handler.writeToNBT(par1nbtTagCompound);
		par1nbtTagCompound.setInteger("type", type);
	}

	@Override
	public boolean equals(final IPeripheral other) {
		return equals((Object) other);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + type;
		return result;
	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		final ChargeStationTileEntity other = (ChargeStationTileEntity) obj;
		if (type != other.type)
			return false;
		return true;
	}
}
