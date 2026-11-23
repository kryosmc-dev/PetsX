package me.mascotasx;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class AdminPlayersMenu extends AdminBase {
    private final List<UUID> ids = new ArrayList<>();

    public AdminPlayersMenu(MascotasX plugin, Player p) { super(plugin, p, title("players")); }

    @Override
    protected void build() {
        fill();
        ids.clear();
        int i = 0;
        for (Player pl : Bukkit.getOnlinePlayers()) {
            if (i >= 28) break;
            Profile pr = plugin.manager().profile(pl.getUniqueId());
            ItemStack head = it(Material.PLAYER_HEAD, "players.player", "%player%", pl.getName(), "%count%", String.valueOf(pr.owned.size()),
                    "%active%", pr.active == null ? w("none-active") : pr.active);
            SkullMeta sm = (SkullMeta) head.getItemMeta();
            sm.setOwningPlayer(pl);
            head.setItemMeta(sm);
            inv.setItem(grid(i), head);
            ids.add(pl.getUniqueId());
            i++;
        }
        inv.setItem(49, back());
    }

    @Override
    protected void onClick(InventoryClickEvent e) {
        int slot = e.getRawSlot();
        if (slot == 49) { new AdminMenu(plugin, player).open(); return; }
        for (int i = 0; i < ids.size(); i++)
            if (grid(i) == slot) { new AdminPlayerMenu(plugin, player, ids.get(i)).open(); return; }
    }
}
