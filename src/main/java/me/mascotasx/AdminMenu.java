package me.mascotasx;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;

public class AdminMenu extends AdminBase {
    public AdminMenu(MascotasX plugin, Player p) { super(plugin, p, title("panel")); }

    @Override
    protected void build() {
        fill();
        inv.setItem(10, it(Material.BONE, "panel.pets"));
        inv.setItem(12, it(Material.COMPARATOR, "panel.settings"));
        inv.setItem(14, it(Material.CHEST, "panel.rewards"));
        inv.setItem(16, it(Material.BREWING_STAND, "panel.effects"));
        inv.setItem(28, it(Material.PAINTING, "panel.customize"));
        inv.setItem(30, it(Material.WRITABLE_BOOK, "panel.messages"));
        inv.setItem(32, it(Material.PLAYER_HEAD, "panel.players"));
        inv.setItem(34, it(Material.LIME_DYE, "panel.reload"));
        inv.setItem(49, plugin.lang().item(Material.BARRIER, "items.common.close"));
    }

    @Override
    protected void onClick(InventoryClickEvent e) {
        switch (e.getRawSlot()) {
            case 10 -> new AdminPetPickMenu(plugin, player, AdminPetPickMenu.Mode.EDIT, 1, null).open();
            case 12 -> new AdminSettingsMenu(plugin, player).open();
            case 14 -> new AdminRewardsMenu(plugin, player, 1).open();
            case 16 -> new AdminPetPickMenu(plugin, player, AdminPetPickMenu.Mode.EFFECTS, 1, null).open();
            case 28 -> new AdminCustomizeMenu(plugin, player).open();
            case 30 -> new AdminMessagesMenu(plugin, player, 1).open();
            case 32 -> new AdminPlayersMenu(plugin, player).open();
            case 34 -> { plugin.reload(); say("reloaded"); }
            case 49 -> player.closeInventory();
            default -> { }
        }
    }
}
