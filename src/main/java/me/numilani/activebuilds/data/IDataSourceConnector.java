package me.numilani.activebuilds.data;

import me.numilani.activebuilds.objects.Building;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;

public interface IDataSourceConnector {
    void initDatabase() throws SQLException;
    LocalDateTime getLastBuildingUpdateTime() throws SQLException;
    void setLastBuildingUpdateTime() throws SQLException;
    void createBuilding(String name, String buildingType) throws SQLException;
    Building getBuilding(String name) throws SQLException;
    List<Building> getAllBuildings() throws SQLException;
    void updateBuildingInputLocation(String name, String location) throws SQLException;
    void updateBuildingOutputLocation(String name, String location) throws SQLException;
    void removeBuilding(String name) throws SQLException;
}
