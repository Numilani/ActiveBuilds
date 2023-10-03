package me.numilani.activebuilds.objects;

import cloud.commandframework.types.tuples.Pair;
import lombok.Data;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Data
public class BuildingType {
    public String name;
    public List<ItemStack> MaterialsConsumed = new ArrayList<>();
    public List<ItemStack> MaterialsAwarded = new ArrayList<>();

    public BuildingType() {
    }

    public BuildingType(String name){
        this.name = name;
    }

    public BuildingType(String name, List<ItemStack> materialsConsumed, List<ItemStack> materialsAwarded) {
        this.name = name;
        MaterialsConsumed = materialsConsumed;
        MaterialsAwarded = materialsAwarded;
    }

    public boolean isValid(){
        if (!MaterialsConsumed.isEmpty() && !MaterialsAwarded.isEmpty() && !name.isBlank()){
            return true;
        }
        return false;
    }

}