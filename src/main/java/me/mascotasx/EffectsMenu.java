package me.mascotasx;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;

import java.util.ArrayList;
import java.util.List;

/** Efectos propios de cada mascota. Solo se puede tener 1 equipado a la vez. */
public class EffectsMenu extends Menu {
    private final String petId;
    private final List<String> keys = new ArrayList<>();

    public EffectsMenu(MascotasX plugin, Player p, String petId) {
        super(plugin, p, plugin.lang().s("titles.effects"));
        this.petId = petId;
    }

    @Override
    protected void build() {
        fill();
        keys.clear();
        PetData d = data(petId);
        if (d == null) { player.closeInventory(); return; }
        ConfigurationSection es = plugin.manager().effectSection(petId);
        if (es != null) keys.addAll(es.getKeys(false));
        for (int i = 0; i < keys.size() && i < 28; i++) {
            String k = keys.get(i);
            String name = es.getString(k + ".name", k);
            int req = es.getInt(k + ".level", 1);
            Lang L = plugin.lang();
            if (d.level < req) {
                inv.setItem(grid(i), L.item(Material.BARRIER, "items.effects.locked", "%name%", name, "%level%", String.valueOf(req)));
                continue;
            }
            Material m = Material.matchMaterial(es.getString(k + ".icon", "POTION"));
            boolean on = k.equals(d.effect);
            inv.setItem(grid(i), Items.glow(L.item(m == null ? Material.POTION : m, "items.effects.effect", "%name%", name,
                    "%state%", L.s(on ? "words.equipped" : "words.not-equipped"),
                    "%action%", L.s(on ? "words.act-remove" : "words.act-equip")), on));
        }
        inv.setItem(52, plugin.lang().item(Material.BARRIER, "items.common.back"));
    }

    @Override
    public void click(InventoryClickEvent e) {
        int slot = e.getRawSlot();
        if (slot == 52) { new DataMenu(plugin, player, petId).open(); return; }
        PetData d = data(petId);
        ConfigurationSection es = plugin.manager().effectSection(petId);
        if (d == null || es == null) return;
        for (int i = 0; i < keys.size() && i < 28; i++) {
            if (grid(i) != slot) continue;
            String k = keys.get(i);
            int req = es.getInt(k + ".level", 1);
            if (d.level < req) { plugin.msg(player, "locked", "%level%", String.valueOf(req)); return; }
            plugin.manager().clearEffects(player);          // quita el efecto anterior
            d.effect = k.equals(d.effect) ? null : k;       // equipar / desequipar (siempre solo 1)
            plugin.manager().save(player.getUniqueId());
            build();
            return;
        }
    }
}
