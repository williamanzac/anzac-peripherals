package anzac.peripherals.tiles;

import static net.minecraftforge.common.ForgeDirection.getOrientation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import net.minecraft.block.Block;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.ForgeDirection;
import anzac.peripherals.AnzacPeripheralsCore;
import anzac.peripherals.annotations.PeripheralMethod;
import anzac.peripherals.utils.Position;
import anzac.peripherals.utils.Utils;
import buildcraft.api.gates.ActionManager;
import buildcraft.api.gates.ITrigger;
import buildcraft.api.gates.ITriggerParameter;
import buildcraft.transport.TileGenericPipe;
import dan200.computer.api.IComputerAccess;

public abstract class BaseRouterTileEntity extends BasePeripheralTileEntity {

	protected List<Trigger> triggers = new ArrayList<Trigger>();

	protected static class Trigger {
		private final ITrigger trigger;
		private final int parameter;
		private final ForgeDirection side;

		public Trigger(final ITrigger trigger, final int parameter, final ForgeDirection side) {
			this.trigger = trigger;
			this.parameter = parameter;
			this.side = side;
		}

		public ITrigger getTrigger() {
			return trigger;
		}

		public int getParameter() {
			return parameter;
		}

		public ITriggerParameter getTriggerParameter() {
			if (parameter <= 0) {
				return null;
			}
			final ITriggerParameter triggerParameter = trigger.createParameter();
			triggerParameter.set(Utils.getItemStack(parameter));
			return triggerParameter;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + parameter;
			result = prime * result + ((side == null) ? 0 : side.hashCode());
			result = prime * result + ((trigger == null) ? 0 : trigger.getUniqueTag().hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			Trigger other = (Trigger) obj;
			if (parameter != other.parameter)
				return false;
			if (side != other.side)
				return false;
			if (trigger == null) {
				if (other.trigger != null)
					return false;
			} else if (!trigger.getUniqueTag().equals(other.trigger.getUniqueTag()))
				return false;
			return true;
		}

		public String getUniqueTag() {
			return trigger.getUniqueTag();
		}

		public ForgeDirection getSide() {
			return side;
		}
	}

	@Override
	protected boolean requiresMount() {
		return false;
	}

	@PeripheralMethod
	public final Object contents() throws Exception {
		return contents(ForgeDirection.UNKNOWN);
	}

	@PeripheralMethod
	public final Object contents(final ForgeDirection direction) throws Exception {
		return contents(direction, direction.getOpposite());
	}

	@PeripheralMethod
	public abstract Object contents(final ForgeDirection direction, final ForgeDirection dir) throws Exception;

	@PeripheralMethod
	public final Object extractFrom(final ForgeDirection fromDir, final int uuid, final int amount) throws Exception {
		return extractFrom(fromDir, uuid, amount, fromDir.getOpposite());
	}

	@PeripheralMethod
	public abstract Object extractFrom(final ForgeDirection fromDir, final int uuid, final int amount,
			final ForgeDirection extractSide) throws Exception;

	@PeripheralMethod
	public final int routeTo(final ForgeDirection toDir, final int amount) throws Exception {
		return routeTo(toDir, toDir.getOpposite(), amount);
	}

	@PeripheralMethod
	public abstract int routeTo(final ForgeDirection toDir, final ForgeDirection insertDir, final int amount)
			throws Exception;

	@PeripheralMethod
	public abstract int sendTo(final String label, final int amount) throws Exception;

	@PeripheralMethod
	public abstract int requestFrom(final String label, final int uuid, final int amount) throws Exception;

	@PeripheralMethod
	public Object getAvailableTriggers(final ForgeDirection side) {
		AnzacPeripheralsCore.logger.info("getTriggers");
		final Position p = new Position(xCoord, yCoord, zCoord, side);
		p.moveForwards(1);
		// AnzacPeripheralsCore.logger.info("position: " + p);
		final int blockId = worldObj.getBlockId(p.x, p.y, p.z);
		final Block block = Block.blocksList[blockId];
		final TileEntity entity = worldObj.getBlockTileEntity(p.x, p.y, p.z);
		// AnzacPeripheralsCore.logger.info("blockid: " + blockId + ", block: " + block + ", entity: " + entity);
		if (block == null || entity == null) {
			return null;
		}
		final LinkedList<ITrigger> triggers = ActionManager.getNeighborTriggers(block, entity);
		if (entity instanceof TileGenericPipe) {
			// AnzacPeripheralsCore.logger.info("is pipe");
			triggers.addAll(ActionManager.getPipeTriggers(((TileGenericPipe) entity).getPipe()));
		}
		// AnzacPeripheralsCore.logger.info("triggers: " + triggers);
		final Map<String, Map<String, Object>> table = new HashMap<String, Map<String, Object>>();
		for (final ITrigger trigger : triggers) {
			final String uniqueTag = trigger.getUniqueTag();
			final String description = trigger.getDescription();
			final boolean hasParameter = trigger.hasParameter();
			final boolean requiresParameter = trigger.requiresParameter();
			final HashMap<String, Object> map = new HashMap<String, Object>();
			map.put("description", description);
			map.put("hasParameter", hasParameter);
			map.put("requiresParameter", requiresParameter);
			table.put(uniqueTag, map);
		}
		AnzacPeripheralsCore.logger.info("table: " + table);
		return table;
	}

	@PeripheralMethod
	public void addTrigger(final String name, final int uuid, final ForgeDirection side) throws Exception {
		final ITrigger iTrigger = ActionManager.triggers.get(name);
		if (iTrigger == null) {
			throw new Exception(name + " is not a valid trigger");
		}
		if (iTrigger.requiresParameter() && uuid <= 0) {
			throw new Exception(name + " requires a parameter");
		}
		final Trigger trigger = new Trigger(iTrigger, uuid, side);
		triggers.add(trigger);
	}

	@PeripheralMethod
	public void removeTrigger(final String name, final int uuid, final ForgeDirection side) {
		for (final Iterator<Trigger> it = triggers.iterator(); it.hasNext();) {
			final Trigger trigger = it.next();
			if (trigger.getUniqueTag().equals(name) && trigger.getParameter() == uuid && trigger.getSide() == side) {
				it.remove();
			}
		}
	}

	@Override
	public void updateEntity() {
		super.updateEntity();

		if (worldObj.isRemote) {
			return;
		}

		if (worldObj.getTotalWorldTime() % 10 == 0) {
			resolveEvents();
		}
	}

	protected void resolveEvents() {
		// Computes the events depending on the triggers
		for (final Trigger trigger : triggers) {
			if (trigger != null) {
				if (isTriggerActive(trigger)) {
					for (final IComputerAccess computer : computers) {
						if (trigger.getParameter() <= 0) {
							computer.queueEvent(trigger.getUniqueTag(), new Object[] { computer.getAttachmentName() });
						} else {
							computer.queueEvent(trigger.getUniqueTag(), new Object[] { computer.getAttachmentName(),
									trigger.getParameter() });
						}
					}
				}
			}
		}
	}

	protected boolean isTriggerActive(final Trigger trigger) {
		if (trigger != null && trigger.getTrigger() != null) {
			final Position p = new Position(xCoord, yCoord, zCoord, trigger.getSide());
			p.moveForwards(1);
			AnzacPeripheralsCore.logger.info("position: " + p);
			final TileEntity tile = worldObj.getBlockTileEntity(p.x, p.y, p.z);

			if (tile != null) {
				final ITriggerParameter triggerParameter = trigger.getTriggerParameter();
				final ForgeDirection opposite = trigger.getSide().getOpposite();
				if (trigger.getTrigger().isTriggerActive(opposite, tile, triggerParameter)) {
					return true;
				}
			}
		}

		return false;
	}

	@Override
	public void readFromNBT(final NBTTagCompound nbtTagCompound) {
		super.readFromNBT(nbtTagCompound);

		if (nbtTagCompound.hasKey("triggers")) {
			triggers.clear();
			final NBTTagList tagList = nbtTagCompound.getTagList("triggers");
			for (int i = 0; i < tagList.tagCount(); ++i) {
				final NBTTagCompound tag = (NBTTagCompound) tagList.tagAt(i);
				if (tag.hasKey("tag")) {
					final ITrigger trigger = ActionManager.triggers.get(tag.getString("tag"));
					final int parameter = tag.getInteger("parameter");
					final int side = tag.getInteger("side");
					triggers.add(new Trigger(trigger, parameter, getOrientation(side)));
				}
			}
		}
	}

	@Override
	public void writeToNBT(final NBTTagCompound nbtTagCompound) {
		super.writeToNBT(nbtTagCompound);

		final NBTTagList tagList = new NBTTagList();
		for (final Trigger trigger : triggers) {
			final NBTTagCompound tag = new NBTTagCompound();
			if (trigger != null) {
				tag.setString("tag", trigger.getUniqueTag());
				tag.setInteger("parameter", trigger.getParameter());
				tag.setInteger("side", trigger.getSide().ordinal());
			}
			tagList.appendTag(tag);
		}
		nbtTagCompound.setTag("triggers", tagList);
	}
}
