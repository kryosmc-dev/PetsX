package me.mascotasx;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;

import java.util.ArrayList;
import java.util.List;

public class PetsMenu extends Menu {
    private static final int PER_PAGE = 28;
    private final int page;
    private final List<String> ids = new ArrayList<>();

    public PetsMenu(MascotasX plugin, Player p, int page) {
        super(plugin, p, plugin.lang().s("titles.pets"));
        this.page = page;
    }

    @Override
    protected void build() {
        fill();
        Lang L = plugin.lang();
        ids.clear();
        Profile pr = plugin.manager().profile(player.getUniqueId());
        for (String k : pr.owned.keySet()) if (plugin.manager().type(k) != null) ids.add(k); // omite mascotas retiradas del plugin
        int start = (page - 1) * PER_PAGE;
        for (int i = 0; i < PER_PAGE && start + i < ids.size(); i++) {
            String id = ids.get(start + i);
            PetData d = pr.owned.get(id);
            PetType t = plugin.manager().type(id);
            if (t == null) continue;
            boolean on = plugin.manager().isActive(player, id);
            inv.setItem(grid(i), Items.glow(L.item(t.icon(), "items.pets.pet",
                    "%name%", d.displayName(t),
                    "%level%", String.valueOf(d.level), "%max%", String.valueOf(plugin.manager().maxLevel()),
                    "%xpline%", xpLine(d),
                    "%damage%", Text.num(plugin.manager().damage(t, d)),
                    "%state%", L.s(on ? "words.summoned" : "words.stored"),
                    "%action%", L.s(on ? "words.act-store" : "words.act-summon")), on));
        }
        if (page > 1) inv.setItem(45, L.item(Material.ARROW, "items.common.prev"));
        if (start + PER_PAGE < ids.size()) inv.setItem(53, L.item(Material.ARROW, "items.common.next"));
        inv.setItem(49, L.item(Material.BARRIER, "items.common.close"));
    }

    /** Linea de progreso de XP (vacia si ya esta al maximo). Compartida con el menu de datos. */
    static String xpLine(MascotasX plugin, PetData d) {
        if (d.level >= plugin.manager().maxLevel()) return "";
        double need = plugin.manager().need(d.level);
        return plugin.lang().s("words.xp-line", "%bar%", Text.bar(d.xp / need, 14), "%xp%", String.valueOf((int) d.xp), "%need%", String.valueOf((int) need));
    }

    private String xpLine(PetData d) { return xpLine(plugin, d); }

    @Override
    public void click(InventoryClickEvent e) {
        int slot = e.getRawSlot();
        if (slot == 49) { player.closeInventory(); return; }
        if (slot == 45 && page > 1) { new PetsMenu(plugin, player, page - 1).open(); return; }
        if (slot == 53 && inv.getItem(53) != null && inv.getItem(53).getType() == Material.ARROW) { new PetsMenu(plugin, player, page + 1).open(); return; }
        for (int i = 0; i < PER_PAGE; i++) {
            if (grid(i) != slot) continue;
            int idx = (page - 1) * PER_PAGE + i;
            if (idx >= ids.size()) return;
            String id = ids.get(idx);
            if (e.isRightClick()) new DataMenu(plugin, player, id).open();
            else { plugin.manager().toggle(player, id); build(); }
            return;
        }
    }
}
