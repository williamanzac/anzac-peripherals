package anzac.peripherals.tiles;

import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;

import org.apache.commons.lang3.StringUtils;

import anzac.peripherals.items.HDDItem;
import dan200.computer.api.IMedia;
import dan200.computer.api.IMount;

public class SimpleDiscInventory extends SimpleInventory {

	private final TileEntity te;
	private IMount mount;
	private final boolean hddOnly;

	public SimpleDiscInventory(final TileEntity te, final boolean hddOnly) {
		super(1);
		this.te = te;
		this.hddOnly = hddOnly;
	}

	@Override
	public int getInventoryStackLimit() {
		return 1;
	}

	@Override
	public boolean isItemValidForSlot(final int i, final ItemStack itemstack) {
		if (hddOnly) {
			return itemstack != null && itemstack.getItem() instanceof HDDItem;
		}
		return itemstack != null && itemstack.getItem() instanceof IMedia;
	}

	@Override
	public void onInventoryChanged() {
		final ItemStack stackInSlot = getHDDItem();
		if (stackInSlot != null) {
			createMount(stackInSlot);
		} else {
			unmountDisk();
		}
		super.onInventoryChanged();
	}

	protected synchronized void createMount(final ItemStack discStack) {
		if (te.worldObj != null && !te.worldObj.isRemote && discStack != null && discStack.getItem() instanceof IMedia) {
			final IMedia media = (IMedia) discStack.getItem();
			mount = media.createDataMount(discStack, te.worldObj);
		}
	}

	protected synchronized void unmountDisk() {
		mount = null;
	}

	public synchronized IMount getMount() {
		final ItemStack stackInSlot = getHDDItem();
		if (mount == null && stackInSlot != null) {
			createMount(stackInSlot);
		}
		return mount;
	}

	public boolean hasLabel() {
		return StringUtils.isNotBlank(getLabel());
	}

	public String getLabel() {
		final ItemStack stack = getHDDItem();
		if (stack != null && stack.getItem() instanceof IMedia) {
			final IMedia media = (IMedia) stack.getItem();
			final String label = media.getLabel(stack);
			return label;
		}
		return null;
	}

	public void setLabel(final String label) {
		final ItemStack stack = getHDDItem();
		if (stack != null && stack.getItem() instanceof IMedia) {
			final IMedia media = (IMedia) stack.getItem();
			media.setLabel(stack, label);
		}
	}

	@Override
	public String getInvName() {
		return getLabel();
	}

	@Override
	public boolean isInvNameLocalized() {
		return hasLabel();
	}

	public ItemStack getHDDItem() {
		return getStackInSlot(0);
	}
}
