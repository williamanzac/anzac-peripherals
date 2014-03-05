package anzac.peripherals;

import static cpw.mods.fml.common.Loader.isModLoaded;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.CraftingManager;
import net.minecraft.item.crafting.IRecipe;
import net.minecraftforge.common.Configuration;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.Property;

import org.apache.commons.lang3.StringUtils;

import anzac.peripherals.blocks.PeripheralBlock;
import anzac.peripherals.gui.GuiHandler;
import anzac.peripherals.items.CPUItem;
import anzac.peripherals.items.HDDItem;
import anzac.peripherals.items.PeripheralItem;
import anzac.peripherals.network.PacketHandler;
import anzac.peripherals.tiles.BasePeripheralTileEntity;
import anzac.peripherals.tiles.FluidRouterTileEntity;
import anzac.peripherals.tiles.FluidStorageTileEntity;
import anzac.peripherals.tiles.ItemRouterTileEntity;
import anzac.peripherals.tiles.ItemStorageTileEntity;
import anzac.peripherals.tiles.RecipeStorageTileEntity;
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
import cpw.mods.fml.common.registry.LanguageRegistry;

@Mod(modid = AnzacPeripheralsCore.MOD_ID, name = "ANZACPeripherals", version = "0.0.1", dependencies = "required-after:ComputerCraft;after:CCTurtle;after:BuildCraft|Energy")
@NetworkMod(clientSideRequired = true, serverSideRequired = false, channels = { "anzac" }, packetHandler = PacketHandler.class)
public class AnzacPeripheralsCore {
	private static final int DEFAULT_HDD_ID = 4339;
	private static final int DEFAULT_CPU_ID = 4337;
	private static final int DEFAULT_PERIPHERAL_ID = 4336;
	private static final int DEFAULT_STORAGE_SIZE = 1024000;

	static final String MOD_ID = "ANZACPeripherals";

	public static Logger logger;

	@Instance(value = MOD_ID)
	public static AnzacPeripheralsCore instance;

	public static Block peripheralBlock;

	public static Item cpu;
	public static Item hdd;

	public static final Map<Integer, Set<String>> computerLabels = new HashMap<Integer, Set<String>>();
	public static final Map<String, BasePeripheralTileEntity> peripheralLabels = new HashMap<String, BasePeripheralTileEntity>();

	public static int storageSize;

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
	public void preInit(final FMLPreInitializationEvent event) {
		Configuration configuration = new Configuration(event.getSuggestedConfigurationFile());
		logger = event.getModLog();

		try {
			configuration.load();

			Property propertySize = configuration.get("general", "hdd.size", DEFAULT_STORAGE_SIZE,
					"The disk space limit for Hard Disk Drives");
			storageSize = propertySize.getInt(DEFAULT_STORAGE_SIZE);

			Property propertyId = configuration.getBlock("peripheral.id", DEFAULT_PERIPHERAL_ID,
					"The Block ID for the peripherals");
			peripheralBlock = new PeripheralBlock(propertyId.getInt(DEFAULT_PERIPHERAL_ID), Material.rock);
			MinecraftForge.setBlockHarvestLevel(peripheralBlock, "pickaxe", 0);
			GameRegistry.registerBlock(peripheralBlock, PeripheralItem.class, "anzacperipheral", MOD_ID);

			Property propertyCPUId = configuration.getItem("cpu.id", DEFAULT_CPU_ID, "The Item Id for CPUs");
			cpu = new CPUItem(propertyCPUId.getInt(DEFAULT_CPU_ID));
			GameRegistry.registerItem(cpu, "anzaccpu", MOD_ID);

			Property propertyHDDId = configuration.getItem("hdd.id", DEFAULT_HDD_ID, "The Item Id for HDDs");
			hdd = new HDDItem(propertyHDDId.getInt(DEFAULT_HDD_ID));
			GameRegistry.registerItem(hdd, "hdd", MOD_ID);
		} finally {
			if (configuration.hasChanged()) {
				configuration.save();
			}
		}
		NetworkRegistry.instance().registerGuiHandler(this, new GuiHandler());

		GameRegistry.registerTileEntity(WorkbenchTileEntity.class, "anzac.peripherals.tiles.WorkbenchTitleEntity");
		GameRegistry.registerTileEntity(RecipeStorageTileEntity.class,
				"anzac.peripherals.tiles.RecipeStorageTitleEntity");
		GameRegistry.registerTileEntity(ItemRouterTileEntity.class, "anzac.peripherals.tiles.ItemRouterTileEntity");
		GameRegistry.registerTileEntity(FluidRouterTileEntity.class, "anzac.peripherals.tiles.FluidRouterTileEntity");
		GameRegistry.registerTileEntity(ItemStorageTileEntity.class, "anzac.peripherals.tiles.ItemStorageTileEntity");
		GameRegistry.registerTileEntity(FluidStorageTileEntity.class, "anzac.peripherals.tiles.FluidStorageTileEntity");
	}

	@EventHandler
	public void load(final FMLInitializationEvent event) {
	}

	@SuppressWarnings("unchecked")
	@EventHandler
	public void postInit(final FMLPostInitializationEvent event) {
		// Vanilla items
		final ItemStack stoneStack = new ItemStack(Block.stone);
		final ItemStack goldIngotStack = new ItemStack(Item.ingotGold);
		final ItemStack redstoneStack = new ItemStack(Item.redstone);
		final ItemStack ironIngotStack = new ItemStack(Item.ingotIron);
		final ItemStack glassPaneStack = new ItemStack(Block.thinGlass);
		final ItemStack goldNuggetStack = new ItemStack(Item.goldNugget);
		final ItemStack chestStack = new ItemStack(Block.chest);
		final ItemStack workbenchStack = new ItemStack(Block.workbench);
		final ItemStack enderPearlStack = new ItemStack(Item.enderPearl);
		final ItemStack inkStack = new ItemStack(Item.dyePowder, 1, 0);

		// New Items
		final ItemStack basicStack = new ItemStack(cpu, 1, 0);
		final ItemStack advancedStack = new ItemStack(cpu, 1, 1);
		final ItemStack hddStack = new ItemStack(hdd);
		final ItemStack benchStack = new ItemStack(peripheralBlock, 1, 0);
		final ItemStack storageStack = new ItemStack(peripheralBlock, 1, 1);
		final ItemStack itemRouterStack = new ItemStack(peripheralBlock, 1, 2);
		final ItemStack fluidRouterStack = new ItemStack(peripheralBlock, 1, 3);
		final ItemStack itemStorageStack = new ItemStack(peripheralBlock, 1, 4);
		final ItemStack fluidStorageStack = new ItemStack(peripheralBlock, 1, 5);
		LanguageRegistry.addName(basicStack, "Basic Processor");
		LanguageRegistry.addName(advancedStack, "Advanced Processor");
		LanguageRegistry.addName(benchStack, "Computerised Workbench");
		LanguageRegistry.addName(hddStack, "Hard Disk");
		LanguageRegistry.addName(storageStack, "Recipe Storage");
		LanguageRegistry.addName(itemRouterStack, "Item Router");
		LanguageRegistry.addName(fluidRouterStack, "Fluid Router");
		LanguageRegistry.addName(itemStorageStack, "Item Storage");
		LanguageRegistry.addName(fluidStorageStack, "Fluid Storage");

		// ComputerCraft items
		final Block blockPeripheral = GameRegistry.findBlock("ComputerCraft", "CC-Peripheral");
		final Block blockComputer = GameRegistry.findBlock("ComputerCraft", "CC-Computer");
		final Block blockTurtle = GameRegistry.findBlock("CCTurtle", "CC-Turtle");
		final Block blockAdvancedTurtle = GameRegistry.findBlock("CCTurtle", "CC-TurtleAdvanced");
		final Item diskItem = Utils.getItem("dan200.ComputerCraft$Items", "disk");
		final Block cableBlock = Utils.getBlock("dan200.ComputerCraft$Blocks", "cable");
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

		// optional BuildCraft items
		final Item itemPipeWood = Utils.getItem("buildcraft.BuildCraftTransport", "pipeItemsWood");
		final Item itemPipeDiamond = Utils.getItem("buildcraft.BuildCraftTransport", "pipeItemsDiamond");
		final Item fluidPipeWood = Utils.getItem("buildcraft.BuildCraftTransport", "pipeFluidsWood");
		final Item fluidPipeIron = Utils.getItem("buildcraft.BuildCraftTransport", "pipeFluidsIron");
		final Block tankBlock = Utils.getBlock("buildcraft.BuildCraftFactory", "tankBlock");

		// swap for Vanilla items if needed
		final ItemStack pipeWoodStack = itemPipeWood != null ? new ItemStack(itemPipeWood) : new ItemStack(
				Block.hopperBlock);
		final ItemStack pipeDiamondStack = itemPipeDiamond != null ? new ItemStack(itemPipeDiamond) : new ItemStack(
				Block.dropper);
		final ItemStack fluidWoodStack = fluidPipeWood != null ? new ItemStack(fluidPipeWood) : new ItemStack(
				Item.bucketEmpty);
		final ItemStack fluidIronStack = fluidPipeIron != null ? new ItemStack(fluidPipeIron) : new ItemStack(
				Item.bucketEmpty);
		final ItemStack tankStack = new ItemStack(tankBlock != null ? tankBlock : Block.cauldron);

		final boolean bceLoaded = isModLoaded("BuildCraft|Energy");
		// final boolean bctLoaded = isModLoaded("BuildCraft|Transport");

		// for (Item item : Item.itemsList) {
		// if (item != null) {
		// logger.info("Item: " + item.itemID + "," +
		// item.getUnlocalizedName());
		// }
		// }
		//
		// for (Block block : Block.blocksList) {
		// if (block != null) {
		// logger.info("Item: " + block.blockID + "," +
		// block.getUnlocalizedName());
		// }
		// }

		// new recipes
		GameRegistry.addShapedRecipe(basicStack, " r ", "rir", " r ", 'r', redstoneStack, 'i', ironIngotStack);
		GameRegistry.addShapedRecipe(advancedStack, " r ", "rgr", " r ", 'r', redstoneStack, 'g', goldIngotStack);
		GameRegistry.addShapedRecipe(hddStack, "ddd", "dDd", "ddd", 'd', diskStack, 'D', driveStack);

		GameRegistry.addShapedRecipe(benchStack, "sws", "sbs", "scs", 's', stoneStack, 'w', workbenchStack, 'b',
				basicStack, 'c', chestStack);
		GameRegistry.addShapedRecipe(storageStack, "sws", "sbs", "sds", 's', stoneStack, 'w', workbenchStack, 'b',
				basicStack, 'd', hddStack);
		GameRegistry.addShapedRecipe(itemStorageStack, "scs", "sas", "sds", 's', stoneStack, 'c', chestStack, 'a',
				advancedStack, 'd', hddStack);
		GameRegistry.addShapedRecipe(fluidStorageStack, "sts", "sas", "sds", 's', stoneStack, 't', tankStack, 'a',
				advancedStack, 'd', hddStack);

		GameRegistry.addShapedRecipe(itemRouterStack, "sds", "sbs", "sws", 's', stoneStack, 'd', pipeDiamondStack, 'b',
				basicStack, 'w', pipeWoodStack);
		GameRegistry.addShapedRecipe(fluidRouterStack, "sds", "sbs", "sws", 's', stoneStack, 'd', fluidIronStack, 'b',
				basicStack, 'w', fluidWoodStack);

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
					|| recipeOutput.isItemEqual(printerStack) || bceLoaded
					&& (recipeOutput.isItemEqual(turtleStack) || recipeOutput.isItemEqual(advancedTurtleStack))) {
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
		GameRegistry.addShapedRecipe(driveStack, "XXX", "XYX", "X X", 'X', stoneStack, 'Y', basicStack);
		GameRegistry
				.addShapedRecipe(printerStack, "XXX", "XYX", "XIX", 'X', stoneStack, 'Y', basicStack, 'I', inkStack);
		GameRegistry.addShapedRecipe(modemStack, "XXX", "XYX", "XXX", 'X', stoneStack, 'Y', basicStack);
		GameRegistry.addShapedRecipe(wirelessStack, "XXX", "XYX", "XEX", 'X', stoneStack, 'Y', basicStack, 'E',
				enderPearlStack);
		GameRegistry.addShapedRecipe(cablesStack, "XXX", "YYY", "XXX", 'X', stoneStack, 'Y', redstoneStack);

		if (bceLoaded) {
			final boolean turtlesNeedFuel = ClassUtils.getField("dan200.CCTurtle", "turtlesNeedFuel", boolean.class);
			final Block engineBlock = GameRegistry.findBlock("BuildCraft|Energy", "engineBlock");
			final ItemStack engineStack = new ItemStack(engineBlock, turtlesNeedFuel ? 1 : 0, 1);
			GameRegistry.addShapedRecipe(turtleStack, "xcx", "xex", "x@x", 'x', ironIngotStack, 'c', chestStack, 'e',
					engineStack, '@', computerStack);
			GameRegistry.addShapedRecipe(advancedTurtleStack, "xcx", "xex", "x@x", 'x', goldIngotStack, 'c',
					chestStack, 'e', engineStack, '@', advancedComputerStack);
		}
	}
}
