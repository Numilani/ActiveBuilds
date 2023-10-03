package me.numilani.activebuilds.objects;

import com.bergerkiller.bukkit.common.config.ConfigurationNode;
import lombok.Data;
import me.numilani.activebuilds.utils.ItemStackHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Data
public class ConfigurationContents {
    int CheckInterval;
    List<BuildingType> Buildings = new ArrayList<>();

    public void loadBuildings(Set<ConfigurationNode> buildings) throws Exception {

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

            if (!bt.isValid()){
                throw new Exception("Failure while loading building types from config file!");
            }
            Buildings.add(bt);
        }
    }
}
