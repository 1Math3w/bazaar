package me.math3w.bazaar.bazaar;

import me.math3w.bazaar.BazaarPlugin;
import me.math3w.bazaar.api.BazaarAPI;
import me.math3w.bazaar.api.bazaar.Bazaar;
import me.math3w.bazaar.api.bazaar.Category;
import me.math3w.bazaar.api.bazaar.Product;
import me.math3w.bazaar.api.bazaar.ProductCategory;
import me.math3w.bazaar.api.menu.ConfigurableMenuItem;
import me.math3w.bazaar.bazaar.category.CategoryImpl;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class BazaarImpl implements Bazaar {
    private final BazaarPlugin bazaarPlugin;
    private final List<Category> categories;

    public BazaarImpl(BazaarPlugin bazaarPlugin) {
        this.bazaarPlugin = bazaarPlugin;
        this.categories = bazaarPlugin.getBazaarConfig().getCategories().stream()
                .map(categoryConfiguration -> new CategoryImpl(this, categoryConfiguration))
                .collect(Collectors.toList());
    }

    @Override
    public void open(Player player) {
        open(player, categories.get(0));
    }

    @Override
    public void open(Player player, Category category) {
        category.getMenu().open(player);
    }

    @Override
    public void openEdit(Player player, Category category) {
        category.getEditMenu().open(player);
    }

    @Override
    public void openSearch(Player player, String filter) {
        bazaarPlugin.getBazaarConfig().getSearchMenuConfiguration().getMenu(this, filter, false).open(player);
    }

    @Override
    public void openEditSearch(Player player, String filter, ConfigurableMenuItem searchItem) {
        bazaarPlugin.getBazaarConfig().getSearchMenuConfiguration().getMenu(this, "", true).open(player);
    }

    @Override
    public void openOrders(Player player) {
        bazaarPlugin.getBazaarConfig().getOrdersMenuConfiguration().getMenu(getBazaarApi(), false).open(player);
    }

    @Override
    public void openEditOrders(Player player) {
        bazaarPlugin.getBazaarConfig().getOrdersMenuConfiguration().getMenu(getBazaarApi(), true).open(player);
    }

    @Override
    public void openProduct(Player player, Product product) {
        bazaarPlugin.getBazaarConfig().getProductMenuConfiguration().getMenu(product, false).open(player);
    }

    @Override
    public void openProductEdit(Player player, Product product) {
        bazaarPlugin.getBazaarConfig().getProductMenuConfiguration().getMenu(product, true).open(player);
    }

    @Override
    public List<Category> getCategories() {
        return categories;
    }

    @Override
    public void saveConfig() {
        bazaarPlugin.getBazaarConfig().save();
    }

    @Override
    public BazaarAPI getBazaarApi() {
        return bazaarPlugin;
    }

    @Override
    public Product getProduct(String id) {
        return getProducts(product -> product.getId().equals(id)).stream().findAny().orElse(null);
    }

    @Override
    public List<Product> getProducts() {
        return getProducts(product -> true);
    }

    @Override
    public List<Product> getProducts(Predicate<Product> filter) {
        List<Product> products = new ArrayList<>();

        for (Category category : categories) {
            for (ProductCategory productCategory : category.getProductCategories()) {
                for (Product product : productCategory.getProducts()) {
                    if (!filter.test(product)) continue;
                    products.add(product);
                }
            }
        }

        return products;
    }

    @Override
    public Map<Product, Integer> getProductsInInventory(Player player) {
        Map<Product, Integer> productsInInventory = new HashMap<>();

        for (Product product : getProducts()) {
            int totalAmount = getProductAmountInInventory(product, player);
            if (totalAmount > 0) {
                productsInInventory.put(product, totalAmount);
            }
        }

        return productsInInventory;
    }

    @Override
    public int getProductAmountInInventory(Product product, Player player) {
        int amount = 0;

        for (ItemStack itemStack : player.getInventory().getContents()) {
            if (itemStack == null) continue;

            if (itemStack.isSimilar(product.getItem())) {
                amount += itemStack.getAmount();
            }
        }

        return amount;
    }
}
