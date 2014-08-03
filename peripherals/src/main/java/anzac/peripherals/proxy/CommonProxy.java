package anzac.peripherals.proxy;

import static anzac.peripherals.AnzacPeripheralsCore.chargeBlock;
import static anzac.peripherals.AnzacPeripheralsCore.chargeBlockId;
import static anzac.peripherals.AnzacPeripheralsCore.component;
import static anzac.peripherals.AnzacPeripheralsCore.componentId;
import static anzac.peripherals.AnzacPeripheralsCore.hdd;
import static anzac.peripherals.AnzacPeripheralsCore.hddId;
import static anzac.peripherals.AnzacPeripheralsCore.instance;
import static anzac.peripherals.AnzacPeripheralsCore.modifyComputercraft;
import static anzac.peripherals.AnzacPeripheralsCore.peripheralBlock;
import static anzac.peripherals.AnzacPeripheralsCore.peripheralBlockId;
import static anzac.peripherals.AnzacPeripheralsCore.teleporterBlock;
import static anzac.peripherals.AnzacPeripheralsCore.teleporterBlockId;
import static anzac.peripherals.utils.Utils.nextUpgradeId;
import static dan200.computercraft.api.ComputerCraftAPI.registerBundledRedstoneProvider;
import static dan200.computercraft.api.ComputerCraftAPI.registerPeripheralProvider;
import static net.minecraftforge.client.MinecraftForgeClient.registerItemRenderer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumToolMaterial;
import net.minecraft.item.Item;
import net.minecraft.item.ItemAxe;
import net.minecraft.item.ItemFlintAndSteel;
import net.minecraft.item.ItemHoe;
import net.minecraft.item.ItemPickaxe;
import net.minecraft.item.ItemShears;
import net.minecraft.item.ItemSpade;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemSword;
import net.minecraft.item.ItemTool;
import net.minecraft.item.crafting.CraftingManager;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.network.packet.Packet;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.ForgeSubscribe;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.oredict.ShapedOreRecipe;
import net.minecraftforge.oredict.ShapelessOreRecipe;
import anzac.peripherals.AnzacPeripheralsCore;
import anzac.peripherals.blocks.BlockFactory;
import anzac.peripherals.blocks.BlockType;
import anzac.peripherals.blocks.ChargeBlock;
import anzac.peripherals.blocks.PeripheralBlock;
import anzac.peripherals.blocks.TeleporterBlock;
import anzac.peripherals.gui.GuiHandler;
import anzac.peripherals.items.ComponentItem;
import anzac.peripherals.items.HDDItem;
import anzac.peripherals.items.ItemFactory;
import anzac.peripherals.items.ItemType;
import anzac.peripherals.providers.AnzacBundledRedstoneProvider;
import anzac.peripherals.providers.AnzacPeripheralProvider;
import anzac.peripherals.render.RenderComponentItem;
import anzac.peripherals.render.RenderHDD;
import anzac.peripherals.upgrade.DropConsumer;
import anzac.peripherals.upgrade.FlintUpgrade;
import anzac.peripherals.upgrade.FurnaceUpgrade;
import anzac.peripherals.upgrade.GenericToolUpgrade;
import anzac.peripherals.upgrade.HoeUpgrade;
import anzac.peripherals.upgrade.ItemSupplyUpgrade;
import anzac.peripherals.upgrade.ShearingUpgrade;
import anzac.peripherals.upgrade.SwordUpgrade;
import anzac.peripherals.upgrade.WrenchUpgrade;
import anzac.peripherals.utils.ClassUtils;
import anzac.peripherals.utils.Utils;
import buildcraft.api.tools.IToolWrench;
import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.common.network.PacketDispatcher;
import cpw.mods.fml.common.network.Player;
import cpw.mods.fml.common.registry.GameRegistry;
import dan200.computercraft.api.ComputerCraftAPI;
import dan200.computercraft.api.turtle.ITurtleUpgrade;

public class CommonProxy {
	private final static Map<Entity, DropConsumer> dropConsumers = new WeakHashMap<Entity, DropConsumer>();

	public void preInit() throws Exception {
		registerItems();
	}

	public void init() {
		registerTileEntities();
		registerForgeHandlers();
	}

	public void postInit() {
		registerRecpies();
	}

	public void sendToServer(final Packet packet) {
		PacketDispatcher.sendPacketToServer(packet);
	}

	public void sendToAllPlayers(final Packet packet) {
		PacketDispatcher.sendPacketToAllPlayers(packet);
	}

	public void sendToPlayer(final EntityPlayer player, final Packet packet) {
		PacketDispatcher.sendPacketToPlayer(packet, (Player) player);
	}

	private void registerItems() throws Exception {
		peripheralBlock = BlockFactory.registerBlocks(PeripheralBlock.class, peripheralBlockId);
		chargeBlock = BlockFactory.registerBlocks(ChargeBlock.class, chargeBlockId);
		teleporterBlock = BlockFactory.registerBlocks(TeleporterBlock.class, teleporterBlockId);
		component = ItemFactory.registerItems(ComponentItem.class, componentId);
		hdd = ItemFactory.registerItems(HDDItem.class, hddId);
	}

	private void registerTileEntities() {
		NetworkRegistry.instance().registerGuiHandler(instance, new GuiHandler());

		BlockFactory.registerTileEntities(PeripheralBlock.class);
		BlockFactory.registerTileEntities(ChargeBlock.class);
		BlockFactory.registerTileEntities(TeleporterBlock.class);

		registerPeripheralProvider(new AnzacPeripheralProvider());
		registerBundledRedstoneProvider(new AnzacBundledRedstoneProvider());
		registerTurtleUpgrades();

		registerItemRenderer(AnzacPeripheralsCore.component.itemID, RenderComponentItem.instance);
		registerItemRenderer(AnzacPeripheralsCore.hdd.itemID, RenderHDD.instance);
	}

	private void registerTurtleUpgrades() {
		// ComputerCraftAPI.registerTurtleUpgrade(new BucketUpgrade());
		for (final Item item : Item.itemsList) {
			if (item instanceof ItemShears) {
				registerTurtleUpgrade(new ShearingUpgrade((ItemShears) item, nextUpgradeId()));
			}
			if (item instanceof ItemSword) {
				if (!((ItemSword) item).getToolMaterialName().equals(EnumToolMaterial.EMERALD.name())) {
					registerTurtleUpgrade(new SwordUpgrade((ItemSword) item, nextUpgradeId()));
				}
			}
			if (item instanceof ItemAxe || ClassUtils.instanceOf(item, "cofh.item.ItemAxeAdv")) {
				if (!((ItemTool) item).getToolMaterialName().equals(EnumToolMaterial.EMERALD.name())) {
					registerTurtleUpgrade(new GenericToolUpgrade((ItemTool) item, nextUpgradeId(), "Felling"));
				}
			}
			if (item instanceof ItemPickaxe || ClassUtils.instanceOf(item, "cofh.item.ItemPickaxeAdv")) {
				if (!((ItemTool) item).getToolMaterialName().equals(EnumToolMaterial.EMERALD.name())) {
					registerTurtleUpgrade(new GenericToolUpgrade((ItemTool) item, nextUpgradeId(), "Mining"));
				}
			}
			if (item instanceof ItemSpade || ClassUtils.instanceOf(item, "cofh.item.ItemShovelAdv")) {
				if (!((ItemTool) item).getToolMaterialName().equals(EnumToolMaterial.EMERALD.name())) {
					registerTurtleUpgrade(new GenericToolUpgrade((ItemTool) item, nextUpgradeId(), "Digging"));
				}
			}
			if (item instanceof ItemHoe) {
				if (!((ItemHoe) item).getMaterialName().equals(EnumToolMaterial.EMERALD.name())) {
					registerTurtleUpgrade(new HoeUpgrade((ItemHoe) item, nextUpgradeId()));
				}
			}
			if (item instanceof IToolWrench) {
				registerTurtleUpgrade(new WrenchUpgrade((IToolWrench) item, nextUpgradeId()));
			}
			if (item instanceof ItemFlintAndSteel) {
				registerTurtleUpgrade(new FlintUpgrade((ItemFlintAndSteel) item, nextUpgradeId()));
			}
		}
		registerTurtleUpgrade(new FurnaceUpgrade(nextUpgradeId()));
		registerTurtleUpgrade(new ItemSupplyUpgrade(nextUpgradeId()));
	}

	protected void registerTurtleUpgrade(ITurtleUpgrade upgrade) {
		ComputerCraftAPI.registerTurtleUpgrade(upgrade);
	}

	private void registerRecpies() {
		// New Items
		final ItemStack basicStack = new ItemStack(component, 1, ItemType.BASIC_PROCESSOR.getMeta());
		final ItemStack advancedStack = new ItemStack(component, 1, ItemType.ADVANCED_PROCESSOR.getMeta());
		final ItemStack platterStack = new ItemStack(component, 1, ItemType.PLATTER.getMeta());
		final ItemStack spindleStack = new ItemStack(component, 1, ItemType.SPINDLE.getMeta());
		final ItemStack teleportCardStack = new ItemStack(component, 1, ItemType.TELEPORTER_CARD.getMeta());
		final ItemStack basicFrameStack = new ItemStack(component, 1, ItemType.BASIC_PERIPHERAL_FRAME.getMeta());
		final ItemStack advancedFrameStack = new ItemStack(component, 1, ItemType.ADVANCED_PERIPHERAL_FRAME.getMeta());
		final ItemStack teleporterFrameStack = new ItemStack(component, 1, ItemType.TELEPORTER_FRAME.getMeta());

		final ItemStack hddStack = new ItemStack(hdd);

		final ItemStack benchStack = new ItemStack(peripheralBlockId, 1, BlockType.WORKBENCH.getMeta());
		final ItemStack storageStack = new ItemStack(peripheralBlockId, 1, BlockType.RECIPE_STORAGE.getMeta());
		final ItemStack itemRouterStack = new ItemStack(peripheralBlockId, 1, BlockType.ITEM_ROUTER.getMeta());
		final ItemStack fluidRouterStack = new ItemStack(peripheralBlockId, 1, BlockType.FLUID_ROUTER.getMeta());
		final ItemStack itemStorageStack = new ItemStack(peripheralBlockId, 1, BlockType.ITEM_STORAGE.getMeta());
		final ItemStack fluidStorageStack = new ItemStack(peripheralBlockId, 1, BlockType.FLUID_STORAGE.getMeta());
		final ItemStack redstonePeripheralStack = new ItemStack(peripheralBlockId, 1,
				BlockType.REDSTONE_CONTROL.getMeta());
		final ItemStack craftingRouterStack = new ItemStack(peripheralBlockId, 1, BlockType.CRAFTING_ROUTER.getMeta());
		final ItemStack noteBlockStack = new ItemStack(peripheralBlockId, 1, BlockType.NOTE_BLOCK.getMeta());
		final ItemStack itemSupplierStack = new ItemStack(peripheralBlockId, 1, BlockType.ITEM_SUPPLIER.getMeta());
		final ItemStack fluidSupplierStack = new ItemStack(peripheralBlockId, 1, BlockType.FLUID_SUPPLIER.getMeta());

		final ItemStack chargeIronStack = new ItemStack(chargeBlockId, 1, ItemType.CHARGE_IRON.getMeta());
		final ItemStack chargeGoldStack = new ItemStack(chargeBlockId, 1, ItemType.CHARGE_GOLD.getMeta());
		final ItemStack chargeDiamondStack = new ItemStack(chargeBlockId, 1, ItemType.CHARGE_DIAMOND.getMeta());

		final ItemStack teleportIronStack = new ItemStack(teleporterBlockId, 1, ItemType.TELEPORTER_IRON.getMeta());
		final ItemStack teleportGoldStack = new ItemStack(teleporterBlockId, 1, ItemType.TELEPORTER_GOLD.getMeta());
		final ItemStack teleportDiamondStack = new ItemStack(teleporterBlockId, 1,
				ItemType.TELEPORTER_DIAMOND.getMeta());

		// ComputerCraft items
		final Block blockPeripheral = GameRegistry.findBlock("ComputerCraft", "CC-Peripheral");
		// final Item pocketItem = Utils.getItem("dan200.computercraft.ComputerCraft$Items", "pocketComputer");
		final ItemStack driveStack = new ItemStack(blockPeripheral, 1, 0);

		// BuildCraft items
		final Item itemPipeWood = Utils.getItem("buildcraft.BuildCraftTransport", "pipeItemsWood");
		final Item itemPipeDiamond = Utils.getItem("buildcraft.BuildCraftTransport", "pipeItemsDiamond");
		final Item fluidPipeWood = Utils.getItem("buildcraft.BuildCraftTransport", "pipeFluidsWood");
		final Item fluidPipeIron = Utils.getItem("buildcraft.BuildCraftTransport", "pipeFluidsIron");
		final Block tankBlock = GameRegistry.findBlock("BuildCraft|Factory", "tankBlock");

		// new recipes
		addShapedRecipe(basicStack, " r ", "rir", " r ", 'r', "dustRedstone", 'i', "ingotIron");
		addShapedRecipe(advancedStack, " r ", "rgr", " r ", 'r', "dustRedstone", 'g', "ingotGold");
		addShapelessRecipe(platterStack, "ingotIron", "dustRedstone");
		addShapedRecipe(spindleStack, "ddd", "did", "ddd", 'd', platterStack, 'i', "ingotIron");
		addShapelessRecipe(hddStack, spindleStack, basicStack, "ingotIron");
		addShapelessRecipe(teleportCardStack, basicStack, Item.paper);
		addShapedRecipe(basicFrameStack, "sis", "ibi", "sis", 's', "stone", 'i', "ingotIron", 'b', basicStack);
		addShapedRecipe(advancedFrameStack, "sgs", "gag", "sgs", 's', "stone", 'g', "ingotGold", 'a', advancedStack);
		addShapedRecipe(teleporterFrameStack, "oeo", "epe", "oeo", 'o', Block.obsidian, 'p', redstonePeripheralStack,
				'e', Item.enderPearl);

		addShapedRecipe(benchStack, "w", "p", "c", 'w', Block.workbench, 'p', basicFrameStack, 'c', Block.chest);
		addShapedRecipe(storageStack, "w", "p", "d", 'w', Block.workbench, 'p', basicFrameStack, 'd', driveStack);
		addShapedRecipe(itemStorageStack, "c", "p", "d", 'c', Block.chest, 'p', advancedFrameStack, 'd', driveStack);
		addShapedRecipe(fluidStorageStack, "t", "p", "d", 't', tankBlock, 'p', advancedFrameStack, 'd', driveStack);
		addShapedRecipe(redstonePeripheralStack, " r ", "rpr", " r ", 'r', "blockRedstone", 'p', advancedFrameStack);

		addShapelessRecipe(itemRouterStack, itemPipeDiamond, advancedFrameStack, itemPipeWood, driveStack);
		addShapelessRecipe(fluidRouterStack, fluidPipeIron, advancedFrameStack, fluidPipeWood, driveStack);
		addShapedRecipe(craftingRouterStack, "w", "p", "r", 'w', Block.workbench, 'p', basicFrameStack, 'r',
				itemRouterStack);

		addShapedRecipe(noteBlockStack, "wns", "nbn", "gna", 'n', Block.music, 'w', "plankWood", 's', "stone", 'b',
				basicFrameStack, 'g', Block.glass, 'a', Block.sand);

		addShapedRecipe(itemSupplierStack, "t", "p", "r", 'w', teleporterFrameStack, 'p', advancedFrameStack, 'r',
				itemRouterStack);
		addShapedRecipe(fluidSupplierStack, "t", "p", "r", 'w', teleporterFrameStack, 'p', advancedFrameStack, 'r',
				fluidRouterStack);

		addShapedRecipe(chargeIronStack, "i i", " f ", "i i", 'i', "ingotIron", 'f', redstonePeripheralStack);
		addShapedRecipe(chargeGoldStack, "g g", " f ", "g g", 'f', chargeIronStack, 'g', "ingotGold");
		addShapedRecipe(chargeDiamondStack, "d d", " f ", "d d", 'd', Item.diamond, 'f', chargeGoldStack);

		addShapedRecipe(teleportIronStack, "i i", " f ", "i i", 'i', "ingotIron", 'f', teleporterFrameStack);
		addShapedRecipe(teleportGoldStack, "g g", " f ", "g g", 'f', teleportIronStack, 'g', "ingotGold");
		addShapedRecipe(teleportDiamondStack, "d d", " f ", "d d", 'd', Item.diamond, 'f', teleportGoldStack);

		if (modifyComputercraft) {
			modifyComputercraft();
		}
	}

	private void modifyComputercraft() {
		final ItemStack basicStack = new ItemStack(component, 1, ItemType.BASIC_PROCESSOR.getMeta());
		final ItemStack advancedStack = new ItemStack(component, 1, ItemType.ADVANCED_PROCESSOR.getMeta());

		// ComputerCraft items
		final Block blockPeripheral = GameRegistry.findBlock("ComputerCraft", "CC-Peripheral");
		final Block blockComputer = GameRegistry.findBlock("ComputerCraft", "CC-Computer");
		final Block blockTurtle = GameRegistry.findBlock("ComputerCraft", "CC-Turtle");
		final Block blockAdvancedTurtle = GameRegistry.findBlock("ComputerCraft", "CC-TurtleAdvanced");
		final Block cableBlock = GameRegistry.findBlock("ComputerCraft", "CC-Cable");
		// final Item pocketItem = Utils.getItem("dan200.computercraft.ComputerCraft$Items", "pocketComputer");
		final ItemStack turtleStack = new ItemStack(blockTurtle);
		final ItemStack advancedTurtleStack = new ItemStack(blockAdvancedTurtle);
		final ItemStack computerStack = new ItemStack(blockComputer, 1);
		final ItemStack advancedComputerStack = new ItemStack(blockComputer, 1, 16384);
		final ItemStack cableStack = new ItemStack(cableBlock, 1, 0);
		final ItemStack cablesStack = new ItemStack(cableBlock, 3, 0);
		final ItemStack modemStack = new ItemStack(cableBlock, 1, 1);
		final ItemStack driveStack = new ItemStack(blockPeripheral, 1, 0);
		final ItemStack wirelessStack = new ItemStack(blockPeripheral, 1, 1);
		final ItemStack monitorStack = new ItemStack(blockPeripheral, 1, 2);
		final ItemStack printerStack = new ItemStack(blockPeripheral, 1, 3);
		final ItemStack advancedMonitorsStack = new ItemStack(blockPeripheral, 4, 4);
		final ItemStack advancedMonitorStack = new ItemStack(blockPeripheral, 1, 4);
		// final ItemStack basicPocketStack = new ItemStack(pocketItem, 1, 0);
		// final ItemStack advancedPocketStack = new ItemStack(pocketItem, 1, 1);

		// modify Recipes
		final List<ItemStack> toModify = Arrays.asList(advancedMonitorsStack, cableStack);
		for (final ItemStack target : toModify) {
			findAndRemoveRecipe(target);
		}
		addShapedRecipe(advancedMonitorStack, "XXX", "XYX", "XZX", 'X', "nuggetGold", 'Y', advancedStack, 'Z',
				Block.thinGlass);
		addShapedRecipe(cablesStack, "XXX", "YYY", "XXX", 'X', "stone", 'Y', "dustRedstone");

		modifyShapedRecipe(monitorStack, "XXX", "XYX", "XZX", 'X', "stone", 'Y', basicStack, 'Z', Block.thinGlass);
		modifyShapedRecipe(computerStack, "XDX", "XYX", "XZX", 'X', "stone", 'Y', basicStack, 'Z', monitorStack, 'D',
				hdd);
		modifyShapedRecipe(advancedComputerStack, "XDX", "XYX", "XZX", 'X', "ingotGold", 'Y', advancedStack, 'Z',
				advancedMonitorStack, 'D', hdd);
		modifyShapedRecipe(driveStack, "XXX", "XYX", "X X", 'X', "stone", 'Y', basicStack);
		modifyShapedRecipe(printerStack, "XXX", "XYX", "XIX", 'X', "stone", 'Y', basicStack, 'I', "dyeBlack");
		modifyShapedRecipe(modemStack, "XXX", "XYX", "XXX", 'X', "stone", 'Y', basicStack);
		modifyShapedRecipe(wirelessStack, "XXX", "XYX", "XEX", 'X', "stone", 'Y', basicStack, 'E', Item.enderPearl);

		final boolean turtlesNeedFuel = ClassUtils.getField("dan200.computercraft.ComputerCraft", "turtlesNeedFuel",
				boolean.class);
		final Block engineBlock = GameRegistry.findBlock("BuildCraft|Energy", "engineBlock");
		final ItemStack engineStack = new ItemStack(engineBlock, turtlesNeedFuel ? 1 : 0, 1);
		modifyShapedRecipe(turtleStack, "xcx", "xex", "x@x", 'x', "ingotIron", 'c', Block.chest, 'e', engineStack, '@',
				computerStack);
		modifyShapedRecipe(advancedTurtleStack, "xcx", "xex", "x@x", 'x', "ingotGold", 'c', Block.chest, 'e',
				engineStack, '@', advancedComputerStack);
	}

	public void addShapedRecipe(final ItemStack output, final Object... objects) {
		GameRegistry.addRecipe(new ShapedOreRecipe(output, objects));
	}

	public void addShapelessRecipe(final ItemStack output, final Object... objects) {
		GameRegistry.addRecipe(new ShapelessOreRecipe(output, objects));
	}

	public void modifyShapedRecipe(final ItemStack output, final Object... objects) {
		findAndRemoveRecipe(output);
		addShapedRecipe(output, objects);
	}

	@SuppressWarnings("unchecked")
	private void findAndRemoveRecipe(final ItemStack output) {
		final List<IRecipe> recipeList = CraftingManager.getInstance().getRecipeList();
		for (final Iterator<IRecipe> i = recipeList.iterator(); i.hasNext();) {
			final IRecipe recipe = i.next();
			final ItemStack recipeOutput = recipe.getRecipeOutput();
			if (recipeOutput == null) {
				continue;
			}
			if (recipeOutput.isItemEqual(output)) {
				i.remove();
				break;
			}
		}
	}

	public void modifyShapelessRecipe(final ItemStack output, final Object... objects) {
		findAndRemoveRecipe(output);
		addShapelessRecipe(output, objects);
	}

	protected void registerForgeHandlers() {
		final ForgeHandlers handlers = new ForgeHandlers();
		MinecraftForge.EVENT_BUS.register(handlers);
	}

	public static void setEntityDropConsumer(final Entity entity, final DropConsumer consumer) {
		if (!dropConsumers.containsKey(entity)) {
			final boolean captured = entity.captureDrops;

			if (!captured) {
				entity.captureDrops = true;

				final ArrayList<EntityItem> items = entity.capturedDrops;

				if ((items == null) || (items.size() == 0)) {
					dropConsumers.put(entity, consumer);
				}
			}
		}
	}

	public static void clearEntityDropConsumer(final Entity entity) {
		if (dropConsumers.containsKey(entity)) {
			final boolean captured = entity.captureDrops;

			if (captured) {
				entity.captureDrops = false;

				final ArrayList<EntityItem> items = entity.capturedDrops;

				if (items != null) {
					dispatchEntityDrops(entity, items);
					items.clear();
				}
			}
			dropConsumers.remove(entity);
		}
	}

	private static void dispatchEntityDrops(final Entity entity, final ArrayList<EntityItem> drops) {
		final DropConsumer consumer = dropConsumers.get(entity);
		if (consumer != null) {
			final Iterator<EntityItem> it = drops.iterator();
			while (it.hasNext()) {
				final EntityItem entityItem = it.next();
				consumer.consumeDrop(entity, entityItem.getEntityItem());
			}
			drops.clear();
		}
	}

	public class ForgeHandlers {
		private ForgeHandlers() {
		}

		@ForgeSubscribe
		public void onEntityLivingDrops(final LivingDropsEvent event) {
			dispatchEntityDrops(event.entity, event.drops);
		}
	}
}
