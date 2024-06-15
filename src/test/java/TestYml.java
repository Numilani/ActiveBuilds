import com.bergerkiller.bukkit.common.config.FileConfiguration;
import lombok.experimental.ExtensionMethod;
import me.numilani.activebuilds.objects.BuildingType;
import me.numilani.activebuilds.utils.ItemStackHelper;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestReporter;

import java.util.Arrays;
import java.util.logging.Logger;

@ExtensionMethod(ItemStackHelper.class)
public class TestYml {

    static final Logger logger = Logger.getLogger(TestYml.class.getName());

    @Test
    public void testYmlConfigBuilding(TestReporter testReporter){
        var cfgFile = new FileConfiguration("config.yml");

        cfgFile.set("settings.checkInterval", 90);

        cfgFile.set("buildings.testMine.name", "mine_stone-iron");
        cfgFile.set("buildings.testMine.inputs", Arrays.asList(new ItemStack(Material.STONE_PICKAXE, 1).toSimpleString(), new ItemStack(Material.STONE_PICKAXE, 1).toSimpleString(), new ItemStack(Material.STONE_PICKAXE, 1).toSimpleString(), new ItemStack(Material.TORCH, 8).toSimpleString()));
        cfgFile.set("buildings.testMine.outputs", Arrays.asList(new ItemStack(Material.COBBLESTONE, 192).toSimpleString(), new ItemStack(Material.IRON_ORE, 14).toSimpleString()));

        cfgFile.set("buildings.testMill.name", "lumbermill_spruce");
        cfgFile.set("buildings.testMill.inputs", Arrays.asList(new ItemStack(Material.STONE_AXE, 1).toSimpleString(), new ItemStack(Material.STONE_AXE, 1).toSimpleString(), new ItemStack(Material.STONE_AXE, 1).toSimpleString(), new ItemStack(Material.SPRUCE_SAPLING, 8).toSimpleString()));
        cfgFile.set("buildings.testMill.outputs", Arrays.asList(new ItemStack(Material.SPRUCE_LOG, 128).toSimpleString(), new ItemStack(Material.SPRUCE_LEAVES, 64).toSimpleString()));

        var buildings = cfgFile.getNode("buildings").getNodes();

        for (var building : buildings) {
            var bt = new BuildingType((String) building.get("name"));

            var inputs = building.getList("inputs");
            for (var input : inputs)
            {
                bt.MaterialsConsumed.add(ItemStackHelper.fromSimpleString((String)input));
            }

            var outputs = building.getList("outputs");
            for (var output : outputs)
            {
                bt.MaterialsAwarded.add(ItemStackHelper.fromSimpleString((String)output));
            }
            logger.info(String.format("%s is valid? %s", bt.Name, bt.isValid()));
        }
        logger.info(cfgFile.toString());
    }

}
