package me.math3w.bazaar.api.bazaar;

import me.zort.containr.GUI;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public interface ProductCategory {
    ItemStack getIcon();

    void setIcon(ItemStack icon);

    String getName();

    void setName(String name);

    List<Product> getProducts();
    
    GUI getMenu();

    Category getCategory();
}
