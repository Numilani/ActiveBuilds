package me.numilani.activebuilds.commands;

import io.github.bananapuncher714.nbteditor.NBTEditor;
import me.numilani.activebuilds.ActiveBuilds;
import me.numilani.activebuilds.utils.BlockLocationHelper;
import org.bukkit.Material;
import org.bukkit.block.Container;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.incendo.cloud.annotations.Argument;
import org.incendo.cloud.annotations.Command;
import org.incendo.cloud.annotations.Permission;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

@Permission("activebuilds.admin")
public class BuildCommandHandler {
    private ActiveBuilds plugin;

    public BuildCommandHandler(ActiveBuilds plugin) {
        this.plugin = plugin;
    }

    @Command("ab buildingtype list")
    public void getBuildingTypeInfo(CommandSender sender) throws SQLException {
        sender.sendMessage("=== BUILDING TYPES ===");
        for (var type : plugin.dataSource.getAllBuildingTypes(true))
        {
            sender.sendMessage(String.format("++ %s ++", type.Name));
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

    @Command("ab buildingtype create <name>")
    public void createBuildingType(CommandSender sender, @Argument("name") String name) throws SQLException {
        if (plugin.dataSource.getBuildingType(name, false) != null){
            sender.sendMessage("That buildingtype already exists!");
            return;
        }
        plugin.dataSource.createBuildingType(name);
        sender.sendMessage(String.format("Buildingtype %s created! Be sure to set its inputs/outputs!", name));
    }

    @Command("ab buildingtype update <name> inputs")
    public void setBuildingTypeInputs(CommandSender sender, @Argument("name") String name) throws SQLException{
        if (!(sender instanceof Player)){
            sender.sendMessage("You must run this command as a player!");
            return;
        }

        var target = ((Player) sender).getTargetBlock(null, 8);
        if (target.getType() != Material.CHEST){
            sender.sendMessage("That is not a chest!");
            return;
        }

        List<String> items = new ArrayList<>();
        var inputInv = ((Container)target.getState()).getInventory().getContents();
        for (var item : inputInv) {
            if (item == null) continue;
            var x = NBTEditor.getNBTCompound(item);
            items.add(x.toJson());
        }
        String serializedItems = "";
        for (int i = 0; i < items.size(); i++) {
            if (i > 0){
                serializedItems += "|";
            }
            serializedItems += items.get(i);
        }
        plugin.dataSource.updateBuildingTypeInputs(name, serializedItems);
    }

    @Command("ab buildingtype update <name> outputs")
    public void setBuildingTypeOutputs(CommandSender sender, @Argument("name") String name) throws SQLException{
        if (!(sender instanceof Player)){
            sender.sendMessage("You must run this command as a player!");
            return;
        }

        var target = ((Player) sender).getTargetBlock(null, 8);
        if (target.getType() != Material.CHEST){
            sender.sendMessage("That is not a chest!");
            return;
        }

        List<String> items = new ArrayList<>();
        var inputInv = ((Container)target.getState()).getInventory().getContents();
        for (var item : inputInv) {
            if (item == null) continue;
            var x = NBTEditor.getNBTCompound(item);
            items.add(x.toJson());
        }
        String serializedItems = "";
        for (int i = 0; i < items.size(); i++) {
            if (i > 0){
                serializedItems += "|";
            }
            serializedItems += items.get(i);
        }
        plugin.dataSource.updateBuildingTypeOutputs(name, serializedItems);
    }

    @Command("ab buildingtype remove <name>")
    public void removeBuildingType(CommandSender sender, @Argument("name") String name) throws SQLException{
        if (plugin.dataSource.getBuildingType(name, false) == null){
            sender.sendMessage("That buildingtype doesn't exist!");
            return;
        }
        plugin.dataSource.deleteBuildingType(name);
        sender.sendMessage(String.format("Buildingtype %s removed!", name));
    }

    @Command("ab building create <name> <type>")
    public void createBuilding(CommandSender sender, @Argument("name") String name, @Argument("type") String type) throws SQLException {
//        var x = plugin.cfg.getBuildings().stream().filter(bldg -> bldg.Name.equals(type)).findFirst();
        var x = plugin.dataSource.getAllBuildingTypes(true).stream().filter(bldg -> bldg.Name.equals(type)).findFirst();
        if (x.isEmpty()){
            sender.sendMessage("Unknown building type!");
            return;
        }

        if (plugin.dataSource.getBuilding(name) != null){
            sender.sendMessage("A building with this name already exists!");
            return;
        }

        // TODO: create building here
        plugin.dataSource.createBuilding(name, type);

        sender.sendMessage(String.format("Building %s created! Designate input/output chests with /ab building update %s (input|output)", name, name, name));


    }

    @Command("ab building list")
    public void listBuildings(CommandSender sender) throws SQLException{
        var buildings = plugin.dataSource.getAllBuildings();

        if (buildings.isEmpty()){
            sender.sendMessage("No buildings found!");
            return;
        }

        sender.sendMessage("=== Buildings ===");
        for (var bldg : buildings)
        {
            sender.sendMessage(String.format("%s", bldg.Name));
            sender.sendMessage(String.format("  Type: %s", bldg.Type.Name));
            sender.sendMessage(String.format("  InLoc: (%s,%s,%s)", bldg.getInputLocation().getBlockX(), bldg.getInputLocation().getBlockY(), bldg.getInputLocation().getBlockZ()));
            sender.sendMessage(String.format("  OutLoc: (%s,%s,%s)", bldg.getOutputLocation().getBlockX(), bldg.getOutputLocation().getBlockY(), bldg.getOutputLocation().getBlockZ()));
        }
    }

    @Command("ab building update <name> input")
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

    @Command("ab building update <name> output")
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

    @Command("ab building delete <name>")
    public void deleteBuilding(CommandSender sender, @Argument("name") String name){
        try{
            plugin.dataSource.removeBuilding(name);
            sender.sendMessage(String.format("Removed building \"%s\"!", name));
        }catch (Exception ex){
            sender.sendMessage(String.format("Could not remove building \"%s\"! Does it exist?", name));
            plugin.getLogger().warning(String.format("Could not remove building \"%s\"!", name));
        }
    }

    @Command("ab debug info")
    public void PrintDebugInfo(CommandSender sender) throws SQLException {
        sender.sendMessage(String.format("Update interval: %s min", plugin.cfg.getCheckInterval()));
        sender.sendMessage(String.format("Last building update: %s", plugin.dataSource.getLastBuildingUpdateTime().toString()));
    }

    @Command("ab debug list-all-types")
    public void getInvalidBuildingTypeInfo(CommandSender sender) throws SQLException {
        sender.sendMessage("=== BUILDING TYPES ===");
        for (var type : plugin.dataSource.getAllBuildingTypes(false))
        {
            sender.sendMessage(String.format("++ %s ++", type.Name));
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
    
    @Command("ab debug run-now")
    public void RunNow(CommandSender sender) throws SQLException {

        plugin.buildingService.runBuildingUpdate();
        sender.sendMessage("Buildings updated!");
    }

    @Command("ab reload")
    public void ReloadConfig(CommandSender sender){
        plugin.loadConfig(true);
        sender.sendMessage("Config reloaded!");
    }
}
