package anzac.peripherals.tiles;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import anzac.peripherals.items.ComponentItem;
import anzac.peripherals.items.ItemType;
import anzac.peripherals.tiles.TeleporterTileEntity.Target;
import anzac.peripherals.utils.Position;

public class SimpleTargetInventory extends SimpleInventory {
	private final Target[] targets;
	private final TeleporterTarget entity;

	public SimpleTargetInventory(final int type, final TeleporterTarget entity) {
		super(maxTargets(type));
		targets = new Target[maxTargets(type)];
		this.entity = entity;
	}

	private static int maxTargets(final int type) {
		switch (type) {
		case 1:
			return 1;
		case 2:
			return 2;
		case 3:
			return 4;
		}
		return 0;
	}

	@Override
	public int getInventoryStackLimit() {
		return 1;
	}

	public Target[] getTargets() {
		// AnzacPeripheralsCore.logger.info("targets: " + targets + "isRemote: " + worldObj.isRemote);
		return targets;
	}

	@Override
	public String getInvName() {
		return entity.getInvName();
	}

	@Override
	public boolean isInvNameLocalized() {
		return entity.isInvNameLocalized();
	}

	@Override
	public boolean isItemValidForSlot(final int i, final ItemStack itemstack) {
		return itemstack.getItem() instanceof ComponentItem
				&& itemstack.getItemDamage() == ItemType.TELEPORTER_CARD.getMeta() && itemstack.hasTagCompound();
	}

	@Override
	public void onInventoryChanged() {
		for (int i = 0; i < getSizeInventory(); i++) {
			final ItemStack stack = getStackInSlot(i);
			if (stack != null && stack.hasTagCompound()) {
				final NBTTagCompound tagCompound = stack.getTagCompound();
				final Target target = new Target();
				final int x = tagCompound.getInteger("linkx");
				final int y = tagCompound.getInteger("linky");
				final int z = tagCompound.getInteger("linkz");
				final int d = tagCompound.getInteger("linkd");
				target.position = new Position(x, y, z);
				target.dimension = d;
				targets[i] = target;
			} else {
				targets[i] = null;
			}
		}
		super.onInventoryChanged();
	}
}
