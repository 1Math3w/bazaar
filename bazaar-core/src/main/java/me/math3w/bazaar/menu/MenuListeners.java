package me.math3w.bazaar.menu;

import me.math3w.bazaar.BazaarPlugin;
import me.math3w.bazaar.api.menu.MenuHistory;
import me.zort.containr.GUI;
import me.zort.containr.GUIRepository;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;

public class MenuListeners implements Listener {
    private final BazaarPlugin plugin;

    public MenuListeners(BazaarPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onGuiOpen(InventoryOpenEvent event) {
        Player player = (Player) event.getPlayer();
        String playerName = player.getName();

        if (!GUIRepository.OPENED_GUIS.containsKey(playerName)) return;

        GUI gui = GUIRepository.OPENED_GUIS.get(playerName);
        MenuHistory menuHistory = plugin.getMenuHistory();

        GUI currentMenu = menuHistory.getCurrent(player).orElse(null);
        if (currentMenu != null && gui.getInventory().equals(currentMenu.getInventory())) return;

        menuHistory.addGui(player, gui);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onInventoryClose(InventoryCloseEvent event) {
        Player player = (Player) event.getPlayer();

        if (GUIRepository.OPENED_GUIS.containsKey(player.getName())) return;

        //Needs to be checked a tick later to do not clear inventory if it's just reopened
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            if (!GUIRepository.OPENED_GUIS.containsKey(player.getName())) return;
            Inventory openedInventory = GUIRepository.OPENED_GUIS.get(player.getName()).getInventory();

            if (!event.getInventory().equals(openedInventory)) return;
            if (openedInventory.equals(player.getOpenInventory().getTopInventory())) return;

            GUIRepository.remove(event.getPlayer().getName());
            plugin.getMenuHistory().clearHistory(player);
        }, 1);
    }

    /*
    GUI needs to be frozen when player clicks air item stack because ContainrGUI can't handle these items and item-nbt will throw NPE because of this
    ContainrGUI handles only null item stacks and in older versions null item stack is always replaced with air by bukkit and can't be set to null
     */
    @EventHandler(priority = EventPriority.LOW)
    public void freezeGuiOnAirClick(InventoryClickEvent event) {
        Player player = (Player) event.getWhoClicked();
        String playerName = player.getName();
        ItemStack item = event.getCurrentItem();

        if (!GUIRepository.hasOpen(player)) return;

        //Allow player to drag item into edit menu
        if (!isEditMenu(event.getView())) {
            event.setCancelled(true);
        }

        if (item == null || item.getType() != Material.AIR) return;

        GUI gui = GUIRepository.OPENED_GUIS.get(playerName);
        gui.setFrozen(true);
        Bukkit.getScheduler().runTaskLater(plugin, () -> gui.setFrozen(false), 1);
    }

    private boolean isEditMenu(InventoryView inventory) {
        return inventory.getTitle().startsWith("Edit");
    }
}
