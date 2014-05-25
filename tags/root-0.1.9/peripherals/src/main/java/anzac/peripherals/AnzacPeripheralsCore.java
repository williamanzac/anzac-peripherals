package anzac.peripherals;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.CraftingManager;
import net.minecraft.item.crafting.IRecipe;
import net.minecraftforge.common.Configuration;
import net.minecraftforge.common.Property;

import org.apache.commons.lang3.StringUtils;

import anzac.peripherals.blocks.BlockFactory;
import anzac.peripherals.blocks.ChargeBlock;
import anzac.peripherals.blocks.PeripheralBlock;
import anzac.peripherals.blocks.TeleporterBlock;
import anzac.peripherals.gui.GuiHandler;
import anzac.peripherals.items.ComponentItem;
import anzac.peripherals.items.HDDItem;
import anzac.peripherals.items.ItemFactory;
import anzac.peripherals.network.PacketHandler;
import anzac.peripherals.providers.AnzacBundledRedstoneProvider;
import anzac.peripherals.providers.AnzacPeripheralProvider;
import anzac.peripherals.tiles.BasePeripheralTileEntity;
import anzac.peripherals.tiles.ChargeStationTileEntity;
import anzac.peripherals.tiles.CraftingRouterTileEntity;
import anzac.peripherals.tiles.FluidRouterTileEntity;
import anzac.peripherals.tiles.FluidStorageTileEntity;
import anzac.peripherals.tiles.ItemRouterTileEntity;
import anzac.peripherals.tiles.ItemStorageTileEntity;
import anzac.peripherals.tiles.RecipeStorageTileEntity;
import anzac.peripherals.tiles.RedstoneTileEntity;
import anzac.peripherals.tiles.TeleporterTileEntity;
import anzac.peripherals.tiles.WorkbenchTileEntity;
import anzac.peripherals.utils.ClassUtils;
import anzac.peripherals.utils.Utils;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.Mod.Instance;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.network.NetworkMod;
import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.common.registry.GameRegistry;
import dan200.computercraft.api.ComputerCraftAPI;

@Mod(modid = AnzacPeripheralsCore.MOD_ID, name = "ANZAC Peripherals", version = "0.1.6", dependencies = "required-after:ComputerCraft;required-after:BuildCraft|Energy")
@NetworkMod(clientSideRequired = true, serverSideRequired = true, channels = { "anzac" }, packetHandler = PacketHandler.class)
public class AnzacPeripheralsCore {
	private static final int DEFAULT_HDD_ID = 1339;
	private static final int DEFAULT_COMPONENT_ID = 1337;
	private static final int DEFAULT_PERIPHERAL_ID = 1336;
	private static final int DEFAULT_CHARGE_ID = 1338;
	private static final int DEFAULT_TELEPORTER_ID = 1340;
	private static final int DEFAULT_STORAGE_SIZE = 1024000;
	private static final int DEFAULT_MJ_MULTIPLIER = 20;

	public static final String MOD_ID = "ANZACPeripherals";

	public static Logger logger;

	@Instance(value = MOD_ID)
	public static AnzacPeripheralsCore instance;

	public static Block peripheralBlock;
	public static int peripheralBlockId;
	public static Block chargeBlock;
	public static int chargeBlockId;
	public static Block teleporterBlock;
	public static int teleporterBlockId;

	public static Item component;
	public static int componentId;
	public static Item hdd;
	public static int hddId;

	public static final Map<Integer, Set<String>> computerLabels = new HashMap<Integer, Set<String>>();
	public static final Map<String, BasePeripheralTileEntity> peripheralLabels = new HashMap<String, BasePeripheralTileEntity>();

	public static int storageSize;
	public static int mjMultiplier;

	public static void addPeripheralLabel(final int computerId, final String label,
			final BasePeripheralTileEntity entity) {
		// AnzacPeripheralsCore.logger.info("addPeripheralLabel; id: " + computerId + ", label: " + label + ", entity: "
		// + entity);
		if (StringUtils.isNotBlank(label)) {
			AnzacPeripheralsCore.logger.info("not blank");
			if (!computerLabels.containsKey(computerId)) {
				// AnzacPeripheralsCore.logger.info("create new set");
				computerLabels.put(computerId, new HashSet<String>());
			}
			// AnzacPeripheralsCore.logger.info("adding label => computer");
			computerLabels.get(computerId).add(label);
			// AnzacPeripheralsCore.logger.info("adding entity => label");
			peripheralLabels.put(label, entity);
		}
	}

	public static void removePeripheralLabel(final int computerId, final String label) {
		// AnzacPeripheralsCore.logger.info("removePeripheralLabel; id: " + computerId + ", label:" + label);
		if (StringUtils.isNotBlank(label)) {
			peripheralLabels.remove(label);
			final Set<String> set = computerLabels.get(computerId);
			if (set != null) {
				set.remove(label);
			}
		}
	}

	@EventHandler
	public void preInit(final FMLPreInitializationEvent event) throws Exception {
		final Configuration configuration = new Configuration(event.getSuggestedConfigurationFile());
		logger = event.getModLog();

		configuration.load();

		Property propertySize = configuration.get("general", "hdd.size", DEFAULT_STORAGE_SIZE,
				"The disk space limit for Hard Disk Drives");
		storageSize = propertySize.getInt(DEFAULT_STORAGE_SIZE);

		propertySize = configuration.get("general", "mj.multiplier", DEFAULT_MJ_MULTIPLIER,
				"Use to convert between mj and turtle moves");
		mjMultiplier = propertySize.getInt(DEFAULT_MJ_MULTIPLIER);

		final Property peripheralId = configuration.getBlock("peripheral.id", DEFAULT_PERIPHERAL_ID,
				"The Block ID for the peripherals");
		peripheralBlockId = peripheralId.getInt(DEFAULT_PERIPHERAL_ID);
		peripheralBlock = BlockFactory.registerBlocks(PeripheralBlock.class, peripheralBlockId);

		final Property chargeId = configuration.getBlock("charge.id", DEFAULT_CHARGE_ID,
				"The Block ID for the charging stations");
		chargeBlockId = chargeId.getInt(DEFAULT_CHARGE_ID);
		chargeBlock = BlockFactory.registerBlocks(ChargeBlock.class, chargeBlockId);

		final Property teleporterId = configuration.getBlock("teleporter.id", DEFAULT_TELEPORTER_ID,
				"The Block ID for the teleporters");
		teleporterBlockId = teleporterId.getInt(DEFAULT_TELEPORTER_ID);
		teleporterBlock = BlockFactory.registerBlocks(TeleporterBlock.class, teleporterBlockId);

		final Property propertyCPUId = configuration.getItem("component.id", DEFAULT_COMPONENT_ID,
				"The Item Id for Components");
		componentId = propertyCPUId.getInt(DEFAULT_COMPONENT_ID);
		component = ItemFactory.registerItems(ComponentItem.class, componentId);

		final Property propertyHDDId = configuration.getItem("hdd.id", DEFAULT_HDD_ID, "The Item Id for HDDs");
		hddId = propertyHDDId.getInt(DEFAULT_HDD_ID);
		hdd = ItemFactory.registerItems(HDDItem.class, hddId);

		configuration.save();
	}

	@EventHandler
	public void load(final FMLInitializationEvent event) {
		NetworkRegistry.instance().registerGuiHandler(instance, new GuiHandler());

		GameRegistry.registerTileEntity(WorkbenchTileEntity.class, "anzac.peripherals.tiles.WorkbenchTitleEntity");
		GameRegistry.registerTileEntity(RecipeStorageTileEntity.class,
				"anzac.peripherals.tiles.RecipeStorageTitleEntity");
		GameRegistry.registerTileEntity(ItemRouterTileEntity.class, "anzac.peripherals.tiles.ItemRouterTileEntity");
		GameRegistry.registerTileEntity(FluidRouterTileEntity.class, "anzac.peripherals.tiles.FluidRouterTileEntity");
		GameRegistry.registerTileEntity(ItemStorageTileEntity.class, "anzac.peripherals.tiles.ItemStorageTileEntity");
		GameRegistry.registerTileEntity(FluidStorageTileEntity.class, "anzac.peripherals.tiles.FluidStorageTileEntity");
		GameRegistry.registerTileEntity(RedstoneTileEntity.class, "anzac.peripherals.tiles.ReadstoneTileEntity");
		GameRegistry.registerTileEntity(CraftingRouterTileEntity.class,
				"anzac.peripherals.tiles.CraftingRouterTileEntity");
		GameRegistry.registerTileEntity(ChargeStationTileEntity.class,
				"anzac.peripherals.tiles.ChargeStationTileEntity");
		GameRegistry.registerTileEntity(TeleporterTileEntity.class, "anzac.peripherals.tiles.TeleporterTileEntity");

		ComputerCraftAPI.registerPeripheralProvider(new AnzacPeripheralProvider());
		ComputerCraftAPI.registerBundledRedstoneProvider(new AnzacBundledRedstoneProvider());
	}

	@SuppressWarnings("unchecked")
	@EventHandler
	public void postInit(final FMLPostInitializationEvent event) {
		// Vanilla items
		final ItemStack stoneStack = new ItemStack(Block.stone);
		final ItemStack goldIngotStack = new ItemStack(Item.ingotGold);
		final ItemStack redstoneStack = new ItemStack(Item.redstone);
		final ItemStack redstoneBlockStack = new ItemStack(Block.blockRedstone);
		final ItemStack ironIngotStack = new ItemStack(Item.ingotIron);
		// final ItemStack glassStack = new ItemStack(Block.glass);
		final ItemStack glassPaneStack = new ItemStack(Block.thinGlass);
		final ItemStack goldNuggetStack = new ItemStack(Item.goldNugget);
		final ItemStack chestStack = new ItemStack(Block.chest);
		final ItemStack workbenchStack = new ItemStack(Block.workbench);
		final ItemStack enderPearlStack = new ItemStack(Item.enderPearl);
		final ItemStack inkStack = new ItemStack(Item.dyePowder, 1, 0);
		final ItemStack paperStack = new ItemStack(Item.paper);
		final ItemStack diamondStack = new ItemStack(Item.diamond);
		final ItemStack obsidianStack = new ItemStack(Block.obsidian);
		// final ItemStack diamondBlockStack = new ItemStack(Block.blockDiamond);
		// final ItemStack ironBlockStack = new ItemStack(Block.blockIron);
		// final ItemStack goldBlockStack = new ItemStack(Block.blockGold);
		// final ItemStack quartzStack = new ItemStack(Item.netherQuartz);

		// New Items
		final ItemStack basicStack = new ItemStack(component, 1, 0);
		final ItemStack advancedStack = new ItemStack(component, 1, 1);
		final ItemStack platterStack = new ItemStack(component, 1, 2);
		final ItemStack spindleStack = new ItemStack(component, 1, 3);
		final ItemStack teleportCardStack = new ItemStack(component, 1, 4);

		final ItemStack hddStack = new ItemStack(hdd);

		final ItemStack benchStack = new ItemStack(peripheralBlock, 1, 0);
		final ItemStack storageStack = new ItemStack(peripheralBlock, 1, 1);
		final ItemStack itemRouterStack = new ItemStack(peripheralBlock, 1, 2);
		final ItemStack fluidRouterStack = new ItemStack(peripheralBlock, 1, 3);
		final ItemStack itemStorageStack = new ItemStack(peripheralBlock, 1, 4);
		final ItemStack fluidStorageStack = new ItemStack(peripheralBlock, 1, 5);
		final ItemStack redstonePeripheralStack = new ItemStack(peripheralBlock, 1, 6);
		final ItemStack craftingRouterStack = new ItemStack(peripheralBlock, 1, 7);

		final ItemStack chargeIronStack = new ItemStack(chargeBlock, 1, 1);
		final ItemStack chargeGoldStack = new ItemStack(chargeBlock, 1, 2);
		final ItemStack chargeDiamondStack = new ItemStack(chargeBlock, 1, 3);

		final ItemStack teleportIronStack = new ItemStack(teleporterBlock, 1, 1);
		final ItemStack teleportGoldStack = new ItemStack(teleporterBlock, 1, 2);
		final ItemStack teleportDiamondStack = new ItemStack(teleporterBlock, 1, 3);

		// ComputerCraft items
		final Block blockPeripheral = Utils.getBlock("dan200.computercraft.ComputerCraft$Blocks", "peripheral");
		final Block blockComputer = Utils.getBlock("dan200.computercraft.ComputerCraft$Blocks", "computer");
		final Block blockTurtle = Utils.getBlock("dan200.computercraft.ComputerCraft$Blocks", "turtle");
		final Block blockAdvancedTurtle = Utils.getBlock("dan200.computercraft.ComputerCraft$Blocks", "turtleAdvanced");
		final Item diskItem = Utils.getItem("dan200.computercraft.ComputerCraft$Items", "disk");
		final Block cableBlock = Utils.getBlock("dan200.computercraft.ComputerCraft$Blocks", "cable");
		final ItemStack diskStack = new ItemStack(diskItem);
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

		// BuildCraft items
		final Item itemPipeWood = Utils.getItem("buildcraft.BuildCraftTransport", "pipeItemsWood");
		final Item itemPipeDiamond = Utils.getItem("buildcraft.BuildCraftTransport", "pipeItemsDiamond");
		final Item fluidPipeWood = Utils.getItem("buildcraft.BuildCraftTransport", "pipeFluidsWood");
		final Item fluidPipeIron = Utils.getItem("buildcraft.BuildCraftTransport", "pipeFluidsIron");
		final Block tankBlock = Utils.getBlock("buildcraft.BuildCraftFactory", "tankBlock");

		final ItemStack pipeWoodStack = new ItemStack(itemPipeWood);
		final ItemStack pipeDiamondStack = new ItemStack(itemPipeDiamond);
		final ItemStack fluidWoodStack = new ItemStack(fluidPipeWood);
		final ItemStack fluidIronStack = new ItemStack(fluidPipeIron);
		final ItemStack tankStack = new ItemStack(tankBlock);

		// new recipes
		GameRegistry.addShapedRecipe(basicStack, " r ", "rir", " r ", 'r', redstoneStack, 'i', ironIngotStack);
		GameRegistry.addShapedRecipe(advancedStack, " r ", "rgr", " r ", 'r', redstoneStack, 'g', goldIngotStack);
		GameRegistry.addShapelessRecipe(platterStack, ironIngotStack, redstoneStack);
		GameRegistry.addShapedRecipe(spindleStack, "ddd", "did", "ddd", 'd', platterStack, 'i', ironIngotStack);
		GameRegistry.addShapelessRecipe(hddStack, spindleStack, basicStack, ironIngotStack);

		GameRegistry.addShapedRecipe(benchStack, "sws", "sbs", "scs", 's', stoneStack, 'w', workbenchStack, 'b',
				basicStack, 'c', chestStack);
		GameRegistry.addShapedRecipe(storageStack, "sws", "sbs", "sds", 's', stoneStack, 'w', workbenchStack, 'b',
				basicStack, 'd', driveStack);
		GameRegistry.addShapedRecipe(itemStorageStack, "scs", "sas", "sds", 's', stoneStack, 'c', chestStack, 'a',
				advancedStack, 'd', driveStack);
		GameRegistry.addShapedRecipe(fluidStorageStack, "sts", "sas", "sds", 's', stoneStack, 't', tankStack, 'a',
				advancedStack, 'd', driveStack);
		GameRegistry.addShapedRecipe(redstonePeripheralStack, "srs", "rbr", "srs", 's', stoneStack, 'r',
				redstoneBlockStack, 'b', basicStack);

		GameRegistry.addShapedRecipe(itemRouterStack, "dsw", "sas", "sDs", 's', stoneStack, 'd', pipeDiamondStack, 'a',
				advancedStack, 'w', pipeWoodStack, 'D', driveStack);
		GameRegistry.addShapedRecipe(fluidRouterStack, "dsw", "sas", "sDs", 's', stoneStack, 'd', fluidIronStack, 'a',
				advancedStack, 'w', fluidWoodStack, 'D', driveStack);
		GameRegistry.addShapedRecipe(craftingRouterStack, "scs", "sbs", "srs", 's', stoneStack, 'c', workbenchStack,
				'b', basicStack, 'r', itemRouterStack);

		GameRegistry.addShapedRecipe(chargeIronStack, "i i", " r ", "i i", 'i', ironIngotStack, 'r',
				redstonePeripheralStack);
		GameRegistry.addShapedRecipe(chargeGoldStack, "g g", " i ", "g g", 'i', chargeIronStack, 'g', goldIngotStack);
		GameRegistry.addShapedRecipe(chargeDiamondStack, "d d", " g ", "d d", 'd', diamondStack, 'g', chargeGoldStack);

		GameRegistry.addShapedRecipe(teleportIronStack, "opo", "pip", "opo", 'i', chargeIronStack, 'o', obsidianStack,
				'p', enderPearlStack);
		GameRegistry.addShapedRecipe(teleportGoldStack, "opo", "pgp", "opo", 'g', chargeGoldStack, 'o', obsidianStack,
				'p', enderPearlStack);
		GameRegistry.addShapedRecipe(teleportDiamondStack, "opo", "pdp", "opo", 'd', chargeDiamondStack, 'o',
				obsidianStack, 'p', enderPearlStack);
		GameRegistry.addShapelessRecipe(teleportCardStack, basicStack, paperStack);

		// modify Recipes
		final List<IRecipe> recipeList = CraftingManager.getInstance().getRecipeList();
		for (final Iterator<IRecipe> i = recipeList.iterator(); i.hasNext();) {
			final IRecipe recipe = i.next();
			final ItemStack recipeOutput = recipe.getRecipeOutput();
			if (recipeOutput == null) {
				continue;
			}
			if (recipeOutput.isItemEqual(computerStack) || recipeOutput.isItemEqual(advancedComputerStack)
					|| recipeOutput.isItemEqual(advancedMonitorsStack) || recipeOutput.isItemEqual(monitorStack)
					|| recipeOutput.isItemEqual(modemStack) || recipeOutput.isItemEqual(wirelessStack)
					|| recipeOutput.isItemEqual(cableStack) || recipeOutput.isItemEqual(driveStack)
					|| recipeOutput.isItemEqual(printerStack) || recipeOutput.isItemEqual(turtleStack)
					|| recipeOutput.isItemEqual(advancedTurtleStack) || recipeOutput.isItemEqual(diskStack)) {
				i.remove();
			}
		}
		GameRegistry.addShapedRecipe(monitorStack, "XXX", "XYX", "XZX", 'X', stoneStack, 'Y', basicStack, 'Z',
				glassPaneStack);
		GameRegistry.addShapedRecipe(advancedMonitorStack, "XXX", "XYX", "XZX", 'X', goldNuggetStack, 'Y',
				advancedStack, 'Z', glassPaneStack);
		GameRegistry.addShapedRecipe(computerStack, "XDX", "XYX", "XZX", 'X', stoneStack, 'Y', basicStack, 'Z',
				monitorStack, 'D', hddStack);
		GameRegistry.addShapedRecipe(advancedComputerStack, "XDX", "XYX", "XZX", 'X', goldIngotStack, 'Y',
				advancedStack, 'Z', advancedMonitorStack, 'D', hddStack);
		GameRegistry.addShapelessRecipe(diskStack, platterStack, paperStack);
		GameRegistry.addShapedRecipe(driveStack, "XXX", "XYX", "X X", 'X', stoneStack, 'Y', basicStack);
		GameRegistry
				.addShapedRecipe(printerStack, "XXX", "XYX", "XIX", 'X', stoneStack, 'Y', basicStack, 'I', inkStack);
		GameRegistry.addShapedRecipe(modemStack, "XXX", "XYX", "XXX", 'X', stoneStack, 'Y', basicStack);
		GameRegistry.addShapedRecipe(wirelessStack, "XXX", "XYX", "XEX", 'X', stoneStack, 'Y', basicStack, 'E',
				enderPearlStack);
		GameRegistry.addShapedRecipe(cablesStack, "XXX", "YYY", "XXX", 'X', stoneStack, 'Y', redstoneStack);

		final boolean turtlesNeedFuel = ClassUtils.getField("dan200.computercraft.ComputerCraft", "turtlesNeedFuel",
				boolean.class);
		final Block engineBlock = GameRegistry.findBlock("BuildCraft|Energy", "engineBlock");
		final ItemStack engineStack = new ItemStack(engineBlock, turtlesNeedFuel ? 1 : 0, 1);
		GameRegistry.addShapedRecipe(turtleStack, "xcx", "xex", "x@x", 'x', ironIngotStack, 'c', chestStack, 'e',
				engineStack, '@', computerStack);
		GameRegistry.addShapedRecipe(advancedTurtleStack, "xcx", "xex", "x@x", 'x', goldIngotStack, 'c', chestStack,
				'e', engineStack, '@', advancedComputerStack);
	}
}
