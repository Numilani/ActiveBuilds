package me.numilani.activebuilds.objects;

import lombok.Data;
import org.bukkit.Location;

@Data
public class Building {
    public int Id;
    public BuildingType Type;
    public String Name;
    public Location InputLocation;
    public Location OutputLocation;
}
