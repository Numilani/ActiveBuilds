package me.numilani.activebuilds.objects;

import lombok.Data;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

@Data
public class BuildingType {
    public int Id;
    public String Name;
    public List<ItemStack> MaterialsConsumed = new ArrayList<>();
    public List<ItemStack> MaterialsAwarded = new ArrayList<>();

    public BuildingType() {
    }

    public BuildingType(String name){
        this.Name = name;
    }

    public BuildingType(String name, List<ItemStack> materialsConsumed, List<ItemStack> materialsAwarded) {
        this.Name = name;
        MaterialsConsumed = materialsConsumed;
        MaterialsAwarded = materialsAwarded;
    }

    public boolean isValid(){
        if (!MaterialsConsumed.isEmpty() && !MaterialsAwarded.isEmpty() && !Name.isBlank()){
            return true;
        }
        return false;
    }

}