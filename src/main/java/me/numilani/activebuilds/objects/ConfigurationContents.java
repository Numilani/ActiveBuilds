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

}
