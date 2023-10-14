package me.numilani.activebuilds.services;

import me.numilani.activebuilds.ActiveBuilds;
import org.bukkit.block.Container;
import org.bukkit.inventory.ItemStack;

import java.sql.SQLException;

public class BuildingService {
    private ActiveBuilds plugin;

    public BuildingService(ActiveBuilds plugin) {
        this.plugin = plugin;
    }

    public void runBuildingUpdate() throws SQLException {
        var buildings = plugin.dataSource.getAllBuildings();

        if (buildings.isEmpty()){
            plugin.getLogger().warning("There are no buildings to run!");
            return;
        }

        for (var bldg : buildings)
        {
            if (bldg.Type.Name.equals("TYPE_UNAVAILABLE" ) || bldg.InputLocation == null || bldg.OutputLocation == null){
                continue;
            }

            var inputChest = ((Container)bldg.InputLocation.getBlock().getState());
            var outputChest = ((Container)bldg.OutputLocation.getBlock().getState());

            // if any inputs are missing, don't award outputs
            var allInputsPresent = true;
            for (var input : bldg.Type.getMaterialsConsumed())
            {
                if (!inputChest.getInventory().contains(input)){
                    allInputsPresent = false;
                }
            }

            // if all inputs are present, take the inputs and give the outputs
            if (allInputsPresent){
                for (var input : bldg.Type.getMaterialsConsumed())
                {
                    inputChest.getInventory().removeItem(input);
                }
                for (var output : bldg.Type.getMaterialsAwarded())
                {
                    var x = outputChest.getInventory().addItem(new ItemStack(output));
                }
            }
        }
        plugin.dataSource.setLastBuildingUpdateTime();
    }
}
