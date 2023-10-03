package me.numilani.activebuilds.commands;

import cloud.commandframework.annotations.Argument;
import cloud.commandframework.annotations.CommandMethod;
import me.numilani.activebuilds.ActiveBuilds;
import me.numilani.activebuilds.utils.BlockLocationHelper;
import org.bukkit.Material;
import org.bukkit.block.Container;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.sql.SQLException;

public class BuildCommandHandler {
    private ActiveBuilds plugin;

    public BuildCommandHandler(ActiveBuilds plugin) {
        this.plugin = plugin;
    }

    @CommandMethod("ab buildingtypes")
    public void getBuildingTypeInfo(CommandSender sender){
        sender.sendMessage("=== BUILDING TYPES ===");
        for (var type : plugin.cfg.getBuildings())
        {
            sender.sendMessage(String.format("++ %s ++", type.name));
            for (var input : type.getMaterialsConsumed())
            {
                sender.sendMessage(String.format(" - Consumes %s %s", input.getAmount(), input.getType()));
            }
            for (var output : type.getMaterialsAwarded())
            {
                sender.sendMessage(String.format(" - Outputs %s %s", output.getAmount(), output.getType()));
            }
        }
    }

    @CommandMethod("ab building create <name> <type>")
    public void createBuilding(CommandSender sender, @Argument("name") String name, @Argument("type") String type) throws SQLException {
        var x = plugin.cfg.getBuildings().stream().filter(bldg -> bldg.name.equals(type)).findFirst();
        if (!x.isPresent()){
            sender.sendMessage("Unknown building type!");
            return;
        }

        // TODO: write a check for the building name here after the data layer is written
        if (plugin.dataSource.getBuilding(name) != null){
            sender.sendMessage("A building with this name already exists!");
            return;
        }

        // TODO: create building here
        plugin.dataSource.createBuilding(name, type);

        sender.sendMessage(String.format("Building %s created! Designate input/output chests with /ab building update %s (input|output)", name, name, name));


    }

    @CommandMethod("ab building update <name> input")
    public void setBuildingInput(CommandSender sender, @Argument("name") String name) throws SQLException {
        if (!(sender instanceof Player)){
            sender.sendMessage("You must run this command as a player!");
            return;
        }

        var target = ((Player) sender).getTargetBlock(null, 8);
        if (target.getType() != Material.CHEST){
            sender.sendMessage("That is not a chest!");
            return;
        }

        plugin.dataSource.updateBuildingInputLocation(name, BlockLocationHelper.getSerializedLocation(target.getLocation()));
        sender.sendMessage(String.format("Input chest for building %s set to chest at %s, %s, %s", name, target.getLocation().getBlockX(), target.getLocation().getBlockY(), target.getLocation().getBlockZ()));
    }

    @CommandMethod("ab building update <name> output")
    public void setBuildingOutput(CommandSender sender, @Argument("name") String name) throws SQLException {
        if (!(sender instanceof Player)){
            sender.sendMessage("You must run this command as a player!");
            return;
        }

        var target = ((Player) sender).getTargetBlock(null, 8);
        if (target.getType() != Material.CHEST){
            sender.sendMessage("That is not a chest!");
            return;
        }

        plugin.dataSource.updateBuildingOutputLocation(name, BlockLocationHelper.getSerializedLocation(target.getLocation()));
        sender.sendMessage(String.format("Output chest for building %s set to chest at %s, %s, %s", name, target.getLocation().getBlockX(), target.getLocation().getBlockY(), target.getLocation().getBlockZ()));
    }

    @CommandMethod("ab building delete <name>")
    public void deleteBuilding(CommandSender sender, @Argument("name") String name){
        try{
            plugin.dataSource.removeBuilding(name);
            sender.sendMessage(String.format("Removed building \"%s\"!", name));
        }catch (Exception ex){
            sender.sendMessage(String.format("Could not remove building \"%s\"! Does it exist?", name));
            plugin.getLogger().warning(String.format("Could not remove building \"%s\"!", name));
        }
    }

    @CommandMethod("ab debug info")
    public void PrintDebugInfo(CommandSender sender) throws SQLException {
        sender.sendMessage(String.format("Update interval: %s min", plugin.cfg.getCheckInterval()));
        sender.sendMessage(String.format("Last building update: %s", plugin.dataSource.getLastBuildingUpdateTime().toString()));
    }

    @CommandMethod("ab debug run-now")
    public void RunNow(CommandSender sender) throws SQLException {

        plugin.buildingService.runBuildingUpdate();
        sender.sendMessage("Buildings updated!");
    }

    @CommandMethod("ab debug list-buildings")
    public void listBuildings(CommandSender sender) throws SQLException{
        var buildings = plugin.dataSource.getAllBuildings();

        if (buildings == null){
            sender.sendMessage("No buildings found!");
            return;
        }

        sender.sendMessage("=== Buildings ===");
        for (var bldg : buildings)
        {
            sender.sendMessage(String.format("%s", bldg.Name));
            sender.sendMessage(String.format("  Type: %s", bldg.Type.name));
            sender.sendMessage(String.format("  InLoc: (%s,%s,%s)", bldg.getInputLocation().getBlockX(), bldg.getInputLocation().getBlockY(), bldg.getInputLocation().getBlockZ()));
            sender.sendMessage(String.format("  OutLoc: (%s,%s,%s)", bldg.getOutputLocation().getBlockX(), bldg.getOutputLocation().getBlockY(), bldg.getOutputLocation().getBlockZ()));
        }
    }

    @CommandMethod("ab reload")
    public void ReloadConfig(CommandSender sender){
        plugin.loadConfig(true);
        sender.sendMessage("Config reloaded!");
    }
}
