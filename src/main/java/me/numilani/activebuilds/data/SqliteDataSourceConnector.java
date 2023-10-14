package me.numilani.activebuilds.data;

import io.github.bananapuncher714.nbteditor.NBTEditor;
import me.numilani.activebuilds.ActiveBuilds;
import me.numilani.activebuilds.objects.Building;
import me.numilani.activebuilds.objects.BuildingType;
import me.numilani.activebuilds.utils.BlockLocationHelper;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SqliteDataSourceConnector implements IDataSourceConnector {
    private ActiveBuilds plugin;
    private String dbFilename = "activebuilds.db";
    public Connection conn;

    public SqliteDataSourceConnector(ActiveBuilds plugin) throws SQLException {
        this.plugin = plugin;
        conn = DriverManager.getConnection("jdbc:sqlite:" + new File(plugin.getDataFolder(), dbFilename).getPath());
    }

    @Override
    public void initDatabase() throws SQLException {
        var statement = conn.createStatement();

        statement.execute("CREATE TABLE SysInfo(name TEXT PRIMARY KEY, infoValue TEXT)");

        var s2 = conn.prepareStatement("INSERT INTO SysInfo (name, infoValue) VALUES ('lastUpdate', ?)");
        s2.setString(1, LocalDateTime.now(Clock.systemUTC()).toString());
        s2.executeUpdate();

        statement.execute("CREATE TABLE BuildingTypes(id INTEGER PRIMARY KEY AUTOINCREMENT, name TEXT, inputs TEXT, outputs TEXT)");

        statement.execute("CREATE TABLE Buildings(id INTEGER PRIMARY KEY AUTOINCREMENT, name TEXT, type INTEGER, inputLocation TEXT, outputLocation TEXT)");
    }

    public LocalDateTime getLastBuildingUpdateTime() throws SQLException {
        var statement = conn.createStatement();
        var x = statement.executeQuery("SELECT infoValue FROM SysInfo WHERE name = 'lastUpdate'");

        if (!x.next()) {
            plugin.getLogger().warning("Couldn't get last building update time!");
        }
        return LocalDateTime.parse(x.getString(1));
    }

    public void setLastBuildingUpdateTime() throws SQLException {
        var statement = conn.prepareStatement("UPDATE SysInfo SET infoValue = ? WHERE name = 'lastUpdate'");
        statement.setString(1, LocalDateTime.now(Clock.systemUTC()).toString());
        statement.executeUpdate();
    }

    public void createBuildingType(String name) throws SQLException{
        var statement = conn.prepareStatement("INSERT INTO BuildingTypes (name) VALUES (?)");
        statement.setString(1, name);

        statement.execute();
    }

    public BuildingType getBuildingType(String name, boolean onlyValidTypes) throws SQLException{
        var statement = conn.prepareStatement("SELECT id, name, inputs, outputs from BuildingTypes WHERE name = ?");
        statement.setString(1, name);

        var x = statement.executeQuery();

        if (!x.next()) {
            plugin.getLogger().warning("Couldn't get building type! Does that type exist (and is configured correctly)?");
            return null;
        }

        BuildingType bt = new BuildingType();
        bt.Id = x.getInt(1);
        bt.Name = x.getString(2);

        var inputList = Arrays.stream(x.getString(3).split("\\|")).toList();
        for (var nbtJson :
                inputList) {
            bt.MaterialsConsumed.add(NBTEditor.getItemFromTag(NBTEditor.getNBTCompound(nbtJson)));
        }

        var outputList = Arrays.stream(x.getString(4).split("\\|")).toList();
        for (var nbtJson :
                outputList) {
            bt.MaterialsAwarded.add(NBTEditor.getItemFromTag(NBTEditor.getNBTCompound(nbtJson)));
        }

        return bt;
    }

    public BuildingType getBuildingType(int id) throws SQLException{
        var statement = conn.prepareStatement("SELECT id, name, inputs, outputs from BuildingTypes WHERE id = ?");
        statement.setInt(1, id);

        var x = statement.executeQuery();

        if (!x.next()) {
            plugin.getLogger().warning("Couldn't get building type! Does that type exist (and is configured correctly)?");
            return null;
        }

        BuildingType bt = new BuildingType();
        bt.Id = x.getInt(1);
        bt.Name = x.getString(2);

        var inputList = Arrays.stream(x.getString(3).split("\\|")).toList();
        for (var nbtJson :
                inputList) {
            bt.MaterialsConsumed.add(NBTEditor.getItemFromTag(NBTEditor.getNBTCompound(nbtJson)));
        }

        var outputList = Arrays.stream(x.getString(4).split("\\|")).toList();
        for (var nbtJson :
                outputList) {
            bt.MaterialsAwarded.add(NBTEditor.getItemFromTag(NBTEditor.getNBTCompound(nbtJson)));
        }

        return bt;
    }

    public List<BuildingType> getAllBuildingTypes(boolean onlyValidTypes) throws SQLException{
        var retval = new ArrayList<BuildingType>();

        var statement = conn.prepareStatement("SELECT id, name, inputs, outputs from BuildingTypes");

        var x = statement.executeQuery();

        if (x.next()) {
            BuildingType bt = new BuildingType();
            bt.Id = x.getInt(1);
            bt.Name = x.getString(2);

            var inputList = Arrays.stream(x.getString(3).split("\\|")).toList();
            for (var nbtJson :
                    inputList) {
                bt.MaterialsConsumed.add(NBTEditor.getItemFromTag(NBTEditor.getNBTCompound(nbtJson)));
            }

            var outputList = Arrays.stream(x.getString(4).split("\\|")).toList();
            for (var nbtJson :
                    outputList) {
                bt.MaterialsAwarded.add(NBTEditor.getItemFromTag(NBTEditor.getNBTCompound(nbtJson)));
            }

            retval.add(bt);
        }
        else{
            plugin.getLogger().warning("Couldn't get building types!");
            return retval;
        }
        return retval;
    }

    public void updateBuildingTypeInputs(String name, String inputs) throws SQLException{
        var statement = conn.prepareStatement("UPDATE BuildingTypes SET inputs = ? where name = ?");
        statement.setString(1, inputs);
        statement.setString(2, name);

        statement.executeUpdate();
    }

    public void updateBuildingTypeOutputs(String name, String outputs) throws SQLException{
        var statement = conn.prepareStatement("UPDATE BuildingTypes SET outputs = ? where name = ?");
        statement.setString(1, outputs);
        statement.setString(2, name);

        statement.executeUpdate();
    }

    public void deleteBuildingType(String name) throws SQLException{
        var type = getBuildingType(name, false);
        for (Building bldg : getAllBuildings().stream().filter(a -> a.Type.Id == type.Id).toList())
        {
            removeBuilding(bldg.Name);
        }
        var statement = conn.prepareStatement("DELETE FROM BuildingTypes WHERE name = ?");
        statement.setString(1, name);

        statement.execute();
    }

    public void createBuilding(String name, String buildingType) throws SQLException {
        var statement = conn.prepareStatement("INSERT INTO Buildings (name, type) VALUES (?, ?)");
        statement.setString(1, name);
        statement.setInt(2, getBuildingType(buildingType, true).Id);

        statement.execute();
    }

    public Building getBuilding(String name) throws SQLException {
        var statement = conn.prepareStatement("SELECT id, name, type, inputLocation, outputLocation from Buildings WHERE name = ?");
        statement.setString(1, name);

        var x = statement.executeQuery();

        if (!x.next()) {
            plugin.getLogger().warning("Couldn't get building information! Does that building exist?");
            return null;
        }

        Building bldg = new Building();
        bldg.Id = x.getInt(1);
        bldg.Name = x.getString(2);

        bldg.Type = getBuildingType(x.getInt(3));

        // This TYPE_UNAVAILABLE trick is a quick and dirty hack, replace this with something more robust.
        //var type = plugin.cfg.getBuildings().stream().filter(a -> a.Name.equals(storedType)).findFirst();
        //bldg.Type = type.orElseGet(() -> new BuildingType("TYPE_UNAVAILABLE"));

        bldg.setInputLocation(BlockLocationHelper.getDeserializedLocation(x.getString(4)));
        bldg.setOutputLocation(BlockLocationHelper.getDeserializedLocation(x.getString(5)));

        return bldg;
    }

    public List<Building> getAllBuildings() throws SQLException{
        var retval = new ArrayList<Building>();

        var statement = conn.prepareStatement("SELECT id, name, type, inputLocation, outputLocation from Buildings");

        var x = statement.executeQuery();

        if (x.next()){
            Building bldg = new Building();
            bldg.Id = x.getInt(1);
            bldg.Name = x.getString(2);

            var storedType = x.getString(3);

            bldg.Type = getBuildingType(x.getInt(3));

            // This TYPE_UNAVAILABLE trick is a quick and dirty hack, replace this with something more robust.
            //var type = plugin.cfg.getBuildings().stream().filter(a -> a.Name.equals(storedType)).findFirst();
            //bldg.Type = type.orElseGet(() -> new BuildingType("TYPE_UNAVAILABLE"));

            bldg.setInputLocation(BlockLocationHelper.getDeserializedLocation(x.getString(4)));
            bldg.setOutputLocation(BlockLocationHelper.getDeserializedLocation(x.getString(5)));

            retval.add(bldg);
        }
        else{
            plugin.getLogger().warning("Couldn't get any building information! Have you created any buildings yet?");
            return retval;
        }

        return retval;
    }

    public void updateBuildingInputLocation(String name, String location) throws SQLException {
        var statement = conn.prepareStatement("UPDATE Buildings SET inputLocation = ? where name = ?");
        statement.setString(1, location);
        statement.setString(2, name);

        statement.executeUpdate();
    }

    public void updateBuildingOutputLocation(String name, String location) throws SQLException {
        var statement = conn.prepareStatement("UPDATE Buildings SET outputLocation = ? where name = ?");
        statement.setString(1, location);
        statement.setString(2, name);

        statement.executeUpdate();
    }

    public void removeBuilding(String name) throws SQLException {
        var statement = conn.prepareStatement("DELETE FROM Buildings WHERE name = ?");
        statement.setString(1, name);

        statement.execute();
    }
}
