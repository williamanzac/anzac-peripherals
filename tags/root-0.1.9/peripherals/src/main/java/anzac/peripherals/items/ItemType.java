package anzac.peripherals.items;

public enum ItemType {
	/**
	 * This item is used in most of the peripherals added by this mod. It is also used in the most of the modified
	 * ComputerCraft recipes.
	 */
	BASIC_PROCESSOR(0, "item.anzac.basiccpu", "Basic Processor", "anzac:basiccpu"),
	/**
	 * This item is used in some of the more advanced peripherals added by this mod. It is also used in some of the
	 * modified ComputerCraft recipes.
	 */
	ADVANCED_PROCESSOR(1, "item.anzac.advancedcpu", "Advanced Processor", "anzac:advancedcpu"),
	/**  */
	PLATTER(2, "item.anzac.platter", "Platter", "anzac:disc"),
	/**  */
	SPINDLE(3, "item.anzac.spindle", "Spindle", "anzac:discs"),
	/**  */
	TELEPORTER_CARD(4, "item.anzac.teleportercard", "Teleporter Card", "anzac:teleport_card"),
	/**  */
	CHARGE_IRON(1, "item.anzac.chargeiron", "Iron Charging Station", ""),
	/**  */
	CHARGE_GOLD(2, "item.anzac.chargegold", "Gold Charging Station", ""),
	/**  */
	CHARGE_DIAMOND(3, "item.anzac.chargediamond", "Diamond Charging Station", ""),
	/**  */
	WORKBENCH(0, "item.anzac.workbench", "Computerised Workbench", ""),
	/**  */
	RECIPE_STORAGE(1, "item.anzac.recipestorage", "Recipe Storage", ""),
	/**  */
	ITEM_ROUTER(2, "item.anzac.itemrouter", "Item Router", ""),
	/**  */
	FLUID_ROUTER(3, "item.anzac.fluidrouter", "Fluid Router", ""),
	/**  */
	ITEM_STORAGE(4, "item.anzac.itemstorage", "Item Storage", ""),
	/**  */
	FLUID_STORAGE(5, "item.anzac.fluidstorage", "Fluid Storage", ""),
	/**  */
	REDSTONE_CONTROL(6, "item.anzac.redstonecontrol", "Redstone Control", ""),
	/**  */
	CRAFTING_ROUTER(7, "item.anzac.craftingrouter", "Crafting Router", ""),
	/**  */
	TELEPORTER_IRON(1, "item.anzac.teleportiron", "Iron Turtle Teleporter", ""),
	/**  */
	TELEPORTER_GOLD(2, "item.anzac.teleportgold", "Gold Turtle Teleporter", ""),
	/**  */
	TELEPORTER_DIAMOND(3, "item.anzac.teleportdiamond", "Diamond Turtle Teleporter", ""),
	/**
	 * This item is used as a storage device by some of the peripherals added by this mod. It has a similar capacity to
	 * that of a standard ComputerCraft computer.
	 */
	HDD(0, "item.anzac.hdd", "Hard Disk", "anzac:hdd"), ;

	private final String key;

	private final String title;

	private final int meta;

	private final String iconKey;

	private ItemType(final int meta, final String key, final String title, final String iconKey) {
		this.iconKey = iconKey;
		this.key = key;
		this.meta = meta;
		this.title = title;
	}

	public String getKey() {
		return key;
	}

	public String getTitle() {
		return title;
	}

	public int getMeta() {
		return meta;
	}

	public String getIconKey() {
		return iconKey;
	}
}
