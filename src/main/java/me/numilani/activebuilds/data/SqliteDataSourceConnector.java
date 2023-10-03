package me.numilani.activebuilds.data;

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

        statement.execute("CREATE TABLE Buildings(name TEXT PRIMARY KEY, type TEXT, inputLocation TEXT, outputLocation TEXT)");
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

    public void createBuilding(String name, String buildingType) throws SQLException {
        var statement = conn.prepareStatement("INSERT INTO Buildings (name, type) VALUES (?, ?)");
        statement.setString(1, name);
        statement.setString(2, buildingType);

        statement.execute();
    }

    public Building getBuilding(String name) throws SQLException {
        var statement = conn.prepareStatement("SELECT name, type, inputLocation, outputLocation from Buildings WHERE name = ?");
        statement.setString(1, name);

        var x = statement.executeQuery();

        if (!x.next()) {
            plugin.getLogger().warning("Couldn't get building information! Does that building exist?");
            return null;
        }

        Building bldg = new Building();
        bldg.Name = x.getString(1);

        var storedType = x.getString(2);
        var type = plugin.cfg.getBuildings().stream().filter(a -> a.name.equals(storedType)).findFirst();
        // TODO: This TYPE_UNAVAILABLE trick is a quick and dirty hack, replace this with something more robust.
        bldg.Type = type.orElseGet(() -> new BuildingType("TYPE_UNAVAILABLE"));

        bldg.setInputLocation(BlockLocationHelper.getDeserializedLocation(x.getString(3)));
        bldg.setOutputLocation(BlockLocationHelper.getDeserializedLocation(x.getString(4)));

        return bldg;
    }

    public List<Building> getAllBuildings() throws SQLException{
        var retval = new ArrayList<Building>();

        var statement = conn.prepareStatement("SELECT name, type, inputLocation, outputLocation from Buildings");

        var x = statement.executeQuery();

        if (x.next()){
            Building bldg = new Building();
            bldg.Name = x.getString(1);

            var storedType = x.getString(2);
            var type = plugin.cfg.getBuildings().stream().filter(a -> a.name.equals(storedType)).findFirst();
            // TODO: This TYPE_UNAVAILABLE trick is a quick and dirty hack, replace this with something more robust.
            bldg.Type = type.orElseGet(() -> new BuildingType("TYPE_UNAVAILABLE"));

            bldg.setInputLocation(BlockLocationHelper.getDeserializedLocation(x.getString(3)));
            bldg.setOutputLocation(BlockLocationHelper.getDeserializedLocation(x.getString(4)));

            retval.add(bldg);
        }
        else{
            plugin.getLogger().warning("Couldn't get any building information! Have you created any buildings yet?");
            return null;
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
