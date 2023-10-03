package me.numilani.activebuilds.utils;

import lombok.experimental.ExtensionMethod;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public class ItemStackHelper {

    public static String toSimpleString(ItemStack stack){
        return String.format("(%s, %s)", stack.getType().toString(), stack.getAmount());
    }

    public static ItemStack fromSimpleString(String str){
        String[] splits = str.substring(1, str.length()-1).split(", ");
        return new ItemStack(Material.getMaterial(splits[0]), Integer.parseInt(splits[1]));
    }
}