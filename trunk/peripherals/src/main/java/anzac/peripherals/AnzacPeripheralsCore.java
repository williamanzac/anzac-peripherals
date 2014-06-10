package anzac.peripherals;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraftforge.common.Configuration;
import net.minecraftforge.common.Property;

import org.apache.commons.lang3.StringUtils;

import anzac.peripherals.network.PacketHandler;
import anzac.peripherals.proxy.CommonProxy;
import anzac.peripherals.tiles.BasePeripheralTileEntity;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.Mod.Instance;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.network.NetworkMod;

@Mod(modid = AnzacPeripheralsCore.MOD_ID, name = "ANZAC Peripherals", version = Version.VERSION, dependencies = "required-after:ComputerCraft;required-after:BuildCraft|Core;after:BuildCraft|Energy;after:BuildCraft|Factory;after:BuildCraft|Transport;after:ThermalExpansion")
@NetworkMod(clientSideRequired = true, serverSideRequired = true, channels = { "anzac" }, packetHandler = PacketHandler.class)
public class AnzacPeripheralsCore {
	private static final int DEFAULT_HDD_ID = 1339;
	private static final int DEFAULT_COMPONENT_ID = 1337;
	private static final int DEFAULT_PERIPHERAL_ID = 1336;
	private static final int DEFAULT_CHARGE_ID = 1338;
	private static final int DEFAULT_TELEPORTER_ID = 1340;
	private static final int DEFAULT_STORAGE_SIZE = 1024000;
	private static final int DEFAULT_MJ_MULTIPLIER = 20;
	private static final boolean DEFAULT_MODIFY = true;

	public static final String MOD_ID = "ANZACPeripherals";

	public static Logger logger;

	@Instance(value = MOD_ID)
	public static AnzacPeripheralsCore instance;

	@SidedProxy(clientSide = "anzac.peripherals.proxy.ClientProxy", serverSide = "anzac.peripherals.proxy.CommonProxy")
	public static CommonProxy proxy;

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
	public static boolean modifyComputercraft;

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

		Property property = configuration.get("general", "hdd.size", DEFAULT_STORAGE_SIZE,
				"The disk space limit for Hard Disk Drives");
		storageSize = property.getInt(DEFAULT_STORAGE_SIZE);

		property = configuration.get("general", "mj.multiplier", DEFAULT_MJ_MULTIPLIER,
				"Use to convert between mj and turtle moves");
		mjMultiplier = property.getInt(DEFAULT_MJ_MULTIPLIER);

		property = configuration.get("general", "modify.computercraft", DEFAULT_MODIFY, "Modify ComputerCraft recpies");
		modifyComputercraft = property.getBoolean(DEFAULT_MODIFY);

		final Property peripheralId = configuration.getBlock("peripheral.id", DEFAULT_PERIPHERAL_ID,
				"The Block ID for the peripherals");
		peripheralBlockId = peripheralId.getInt(DEFAULT_PERIPHERAL_ID);

		final Property chargeId = configuration.getBlock("charge.id", DEFAULT_CHARGE_ID,
				"The Block ID for the charging stations");
		chargeBlockId = chargeId.getInt(DEFAULT_CHARGE_ID);

		final Property teleporterId = configuration.getBlock("teleporter.id", DEFAULT_TELEPORTER_ID,
				"The Block ID for the teleporters");
		teleporterBlockId = teleporterId.getInt(DEFAULT_TELEPORTER_ID);

		final Property propertyCPUId = configuration.getItem("component.id", DEFAULT_COMPONENT_ID,
				"The Item Id for Components");
		componentId = propertyCPUId.getInt(DEFAULT_COMPONENT_ID);

		final Property propertyHDDId = configuration.getItem("hdd.id", DEFAULT_HDD_ID, "The Item Id for HDDs");
		hddId = propertyHDDId.getInt(DEFAULT_HDD_ID);

		configuration.save();

		proxy.preInit();
	}

	@EventHandler
	public void init(final FMLInitializationEvent event) {
		proxy.init();
	}

	@EventHandler
	public void postInit(final FMLPostInitializationEvent event) {
		proxy.postInit();
	}
}
