package anzac.peripherals;

import static cpw.mods.fml.common.Loader.isModLoaded;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.CraftingManager;
import net.minecraft.item.crafting.IRecipe;
import net.minecraftforge.common.MinecraftForge;
import anzac.peripherals.blocks.PeripheralBlock;
import anzac.peripherals.gui.GuiHandler;
import anzac.peripherals.items.CPUItem;
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

@Mod(modid = "anzac.peripherals", name = "ANZAC Peripherals", version = "0.0.1", dependencies = "required-after:ComputerCraft;after:CCTurtle;after:BuildCraft|Energy")
@NetworkMod(clientSideRequired = true, serverSideRequired = false, channels = { "anzac" }, packetHandler = PacketHandler.class)
public class AnzacPeripheralsCore {
	public static Logger logger;

	@Instance(value = "anzac.peripherals")
	public static AnzacPeripheralsCore instance;

	public Block peripheralBlock;

	public static Item cpu;
	public Item hdd;

	public static final Map<Integer, WorkbenchTileEntity> workbenchMap = new HashMap<Integer, WorkbenchTileEntity>();
	public static final Map<Integer, RecipeStorageTileEntity> storageMap = new HashMap<Integer, RecipeStorageTileEntity>();
	public static final Map<Integer, ItemRouterTileEntity> itemRouterMap = new HashMap<Integer, ItemRouterTileEntity>();
	public static final Map<Integer, FluidRouterTileEntity> fluidRouterMap = new HashMap<Integer, FluidRouterTileEntity>();
	public static final Map<Integer, BasePeripheralTileEntity> computerPeripheralMap = new HashMap<Integer, BasePeripheralTileEntity>();

	public static int storageSize;

	@EventHandler
	public void preInit(final FMLPreInitializationEvent event) {
		logger = event.getModLog();
		peripheralBlock = new PeripheralBlock(496, Material.rock);
		cpu = new CPUItem(497);
		hdd = new Item(499).setCreativeTab(CreativeTabs.tabRedstone).setMaxStackSize(64).setUnlocalizedName("hdd")
				.setTextureName("anzac:hdd");
		NetworkRegistry.instance().registerGuiHandler(this, new GuiHandler());
	}

	@EventHandler
	public void load(final FMLInitializationEvent event) {
		MinecraftForge.setBlockHarvestLevel(peripheralBlock, "pickaxe", 0);
		GameRegistry.registerBlock(peripheralBlock, PeripheralItem.class, "anzacperipheral");

		GameRegistry.registerTileEntity(WorkbenchTileEntity.class, "anzac.peripherals.tiles.WorkbenchTitleEntity");
		GameRegistry.registerTileEntity(RecipeStorageTileEntity.class,
				"anzac.peripherals.tiles.RecipeStorageTitleEntity");
		GameRegistry.registerTileEntity(ItemRouterTileEntity.class, "anzac.peripherals.tiles.ItemRouterTileEntity");
		GameRegistry.registerTileEntity(FluidRouterTileEntity.class, "anzac.peripherals.tiles.FluidRouterTileEntity");
		GameRegistry.registerTileEntity(ItemStorageTileEntity.class, "anzac.peripherals.tiles.ItemStorageTileEntity");
		GameRegistry.registerTileEntity(FluidStorageTileEntity.class, "anzac.peripherals.tiles.FluidStorageTileEntity");
	}

	@SuppressWarnings("unchecked")
	@EventHandler
	public void postInit(final FMLPostInitializationEvent event) {
		storageSize = 1024000; // TODO turn into property

		// Vanilla items
		final ItemStack stoneStack = new ItemStack(Block.stone);
		final ItemStack goldIngotStack = new ItemStack(Item.ingotGold);
		final ItemStack redstoneStack = new ItemStack(Item.redstone);
		final ItemStack ironIngotStack = new ItemStack(Item.ingotIron);
		final ItemStack glassPaneStack = new ItemStack(Block.thinGlass);
		final ItemStack goldNuggetStack = new ItemStack(Item.goldNugget);
		final ItemStack chestStack = new ItemStack(Block.chest);
		final ItemStack workbenchStack = new ItemStack(Block.workbench);

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
		final ItemStack monitorStack = new ItemStack(blockPeripheral, 1, 2);
		final ItemStack advancedMonitorsStack = new ItemStack(blockPeripheral, 4, 4);
		final ItemStack advancedMonitorStack = new ItemStack(blockPeripheral, 1, 4);
		final ItemStack diskStack = new ItemStack(diskItem);
		final ItemStack driveStack = new ItemStack(blockPeripheral, 1, 0);
		final ItemStack turtleStack = new ItemStack(blockTurtle);
		final ItemStack advancedTurtleStack = new ItemStack(blockAdvancedTurtle);
		final ItemStack computerStack = new ItemStack(blockComputer, 1);
		final ItemStack advancedComputerStack = new ItemStack(blockComputer, 1, 16384);
		final ItemStack cableStack = new ItemStack(cableBlock, 1, 0);
		final ItemStack modemStack = new ItemStack(cableBlock, 1, 1);

		final boolean bceLoaded = isModLoaded("BuildCraft|Energy");
		// final boolean bctLoaded = isModLoaded("BuildCraft|Transport");

		// for (Item item : Item.itemsList) {
		// if (item != null) {
		// logger.info("Item: " + item.itemID + "," + item.getUnlocalizedName());
		// }
		// }
		//
		// for (Block block : Block.blocksList) {
		// if (block != null) {
		// logger.info("Item: " + block.blockID + "," + block.getUnlocalizedName());
		// }
		// }

		// new recipes
		GameRegistry.addShapedRecipe(benchStack, "sws", "sbs", "scs", 's', stoneStack, 'w', workbenchStack, 'b',
				basicStack, 'c', chestStack);
		GameRegistry.addShapedRecipe(storageStack, "sws", "sbs", "sds", 's', stoneStack, 'w', workbenchStack, 'b',
				basicStack, 'd', driveStack);
		GameRegistry.addShapedRecipe(itemStorageStack, "srs", "sas", "sds", 's', stoneStack, 'r', itemRouterStack, 'a',
				advancedStack, 'd', hddStack);
		GameRegistry.addShapedRecipe(fluidStorageStack, "srs", "sas", "sds", 's', stoneStack, 'r', fluidRouterStack,
				'a', advancedStack, 'd', hddStack);

		final Item itemPipeWood = Utils.getItem("buildcraft.BuildCraftTransport", "pipeItemsWood");
		final Item itemPipeDiamond = Utils.getItem("buildcraft.BuildCraftTransport", "pipeItemsDiamond");
		final Item fluidPipeWood = Utils.getItem("buildcraft.BuildCraftTransport", "pipeFluidsWood");
		final Item fluidPipeIron = Utils.getItem("buildcraft.BuildCraftTransport", "pipeFluidsIron");
		final Block tankBlock = Utils.getBlock("buildcraft.BuildCraftFactory", "tankBlock");

		final ItemStack pipeWoodStack = itemPipeWood != null ? new ItemStack(itemPipeWood) : new ItemStack(
				Block.hopperBlock);
		final ItemStack pipeDiamondStack = itemPipeDiamond != null ? new ItemStack(itemPipeDiamond) : new ItemStack(
				Block.dropper);
		final ItemStack fluidWoodStack = fluidPipeWood != null ? new ItemStack(fluidPipeWood) : new ItemStack(
				Block.hopperBlock);
		final ItemStack fluidIronStack = fluidPipeIron != null ? new ItemStack(fluidPipeIron) : new ItemStack(
				Block.dropper);
		final ItemStack tankStack = new ItemStack(tankBlock != null ? tankBlock : Block.cauldron);

		GameRegistry.addShapedRecipe(itemRouterStack, "sds", "caf", "sws", 's', stoneStack, 'd', pipeDiamondStack, 'a',
				advancedStack, 'w', pipeWoodStack, 'c', chestStack, 'f', driveStack);
		GameRegistry.addShapedRecipe(fluidRouterStack, "sds", "taf", "sws", 's', stoneStack, 'd', fluidIronStack, 'a',
				advancedStack, 'w', fluidWoodStack, 't', tankStack, 'f', driveStack);

		GameRegistry.addShapedRecipe(basicStack, " r ", "rir", " r ", 'r', redstoneStack, 'i', ironIngotStack);
		GameRegistry.addShapedRecipe(advancedStack, " r ", "rgr", " r ", 'r', redstoneStack, 'g', goldIngotStack);
		GameRegistry.addShapedRecipe(hddStack, "ddd", "ddd", "ddd", 'd', diskStack);

		// modify Recipes
		final List<IRecipe> recipeList = CraftingManager.getInstance().getRecipeList();
		for (final Iterator<IRecipe> i = recipeList.iterator(); i.hasNext();) {
			final IRecipe recipe = i.next();
			final ItemStack recipeOutput = recipe.getRecipeOutput();
			if (recipeOutput == null) {
				continue;
			}
			if (recipeOutput.isItemEqual(computerStack)
					|| recipeOutput.isItemEqual(advancedComputerStack)
					|| recipeOutput.isItemEqual(advancedMonitorsStack)
					|| recipeOutput.isItemEqual(monitorStack)
					|| recipeOutput.isItemEqual(modemStack)
					|| recipeOutput.isItemEqual(cableStack)
					|| recipeOutput.isItemEqual(driveStack)
					|| (bceLoaded && (recipeOutput.isItemEqual(turtleStack) || recipeOutput
							.isItemEqual(advancedTurtleStack)))) {
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
		GameRegistry.addShapedRecipe(modemStack, "XXX", "XYX", "XXX", 'X', stoneStack, 'Y', basicStack);
		GameRegistry.addShapedRecipe(cableStack, "XXX", "YYY", "XXX", 'X', stoneStack, 'Y', redstoneStack);

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
