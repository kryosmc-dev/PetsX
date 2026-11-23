package me.mascotasx;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;

import java.util.ArrayList;
import java.util.List;

public class AdminEffectsMenu extends AdminBase {
    private final String petId;
    private final List<String> keys = new ArrayList<>();

    public AdminEffectsMenu(MascotasX plugin, Player p, String petId) {
        super(plugin, p, title("pet-effects"));
        this.petId = petId;
    }

    @Override
    protected void build() {
        fill();
        keys.clear();
        ConfigurationSection es = plugin.manager().effectSection(petId);
        if (es != null) keys.addAll(es.getKeys(false));
        for (int i = 0; i < keys.size() && i < 28; i++) {
            String k = keys.get(i);
            Material m = Material.matchMaterial(es.getString(k + ".icon", "POTION"));
            inv.setItem(grid(i), it(m == null ? Material.POTION : m, "effects.effect", "%name%", es.getString(k + ".name", k),
                    "%type%", es.getString(k + ".type", "?"), "%amp%", String.valueOf(es.getInt(k + ".amplifier", 0) + 1),
                    "%level%", String.valueOf(es.getInt(k + ".level", 1))));
        }
        PetType pt = plugin.manager().type(petId);
        inv.setItem(4, it(Material.PAPER, "effects.info", "%name%", pt == null ? petId : pt.name()));
        inv.setItem(49, back());
    }

    @Override
    protected void onClick(InventoryClickEvent e) {
        int slot = e.getRawSlot();
        if (slot == 49) { new AdminPetPickMenu(plugin, player, AdminPetPickMenu.Mode.EFFECTS, 1, null).open(); return; }
        ConfigurationSection es = plugin.manager().effectSection(petId);
        if (es == null) return;
        for (int i = 0; i < keys.size() && i < 28; i++) {
            if (grid(i) != slot) continue;
            double d = delta(e, 1);
            if (d == 0) return;
            String k = keys.get(i);
            plugin.manager().setEffectLevel(petId, k, es.getInt(k + ".level", 1) + (int) d);
            build();
            return;
        }
    }
}
