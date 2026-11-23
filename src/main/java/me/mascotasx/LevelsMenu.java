package me.mascotasx;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;

import java.util.ArrayList;
import java.util.List;

/**
 * Camino de niveles como en la imagen de referencia: solo los slots del camino llevan cristal,
 * el resto queda vacio. 23 niveles por pagina, el camino sube en zigzag desde abajo.
 */
public class LevelsMenu extends Menu {
    private static final int PER_PAGE = 23;
    private final String petId;
    private final int page;

    public LevelsMenu(MascotasX plugin, Player p, String petId, int page) {
        super(plugin, p, title(plugin, petId, page));
        this.petId = petId;
        this.page = page;
    }

    private static int pages(MascotasX plugin) {
        return Math.max(1, (int) Math.ceil(plugin.manager().maxLevel() / (double) PER_PAGE));
    }

    private static String title(MascotasX plugin, String petId, int page) {
        PetType t = plugin.manager().type(petId);
        return plugin.lang().s("titles.levels", "%pet%", t == null ? petId : t.name(),
                "%page%", String.valueOf(page), "%pages%", String.valueOf(pages(plugin)));
    }

    /** Camino: fila 4 (izq->der), sube por la derecha, fila 2 (der->izq), sube por la izquierda, fila 0 (izq->der). */
    private static int pathSlot(int i) {
        if (i < 7) return 36 + 1 + i;          // 37..43
        if (i == 7) return 34;                 // conector derecho
        if (i < 15) return 18 + (7 - (i - 8)); // 25..19
        if (i == 15) return 10;                // conector izquierdo
        return 1 + (i - 16);                   // 1..7
    }

    @Override
    protected void build() {
        inv.clear(); // sin relleno: solo el camino, como en la referencia
        PetData d = data(petId);
        PetType t = plugin.manager().type(petId);
        if (d == null || t == null) { player.closeInventory(); return; }
        int max = plugin.manager().maxLevel();
        int start = (page - 1) * PER_PAGE + 1;

        for (int i = 0; i < PER_PAGE && start + i <= max; i++) {
            int lvl = start + i;
            boolean reached = d.level >= lvl;
            Material m = reached ? plugin.icon("levels.reached", Material.LIME_STAINED_GLASS_PANE)
                    : lvl == d.level + 1 ? plugin.icon("levels.next", Material.YELLOW_STAINED_GLASS_PANE)
                    : plugin.icon("levels.locked", Material.PINK_STAINED_GLASS_PANE);

            Lang L = plugin.lang();
            List<String> rw = new ArrayList<>();
            ConfigurationSection r = plugin.getConfig().getConfigurationSection("levels.rewards." + lvl);
            if (r != null) {
                double money = r.getDouble("money", 0);
                if (money > 0) rw.add(L.s("words.reward-money", "%money%", Text.money(money)));
                List<String> desc = r.getStringList("description");
                for (String s : desc) rw.add(L.s("words.reward-text", "%text%", s));
                if (desc.isEmpty() && !r.getStringList("commands").isEmpty()) rw.add(L.s("words.reward-special"));
            }
            ConfigurationSection es = plugin.manager().effectSection(petId);
            if (es != null) for (String k : es.getKeys(false))
                if (es.getInt(k + ".level", 1) == lvl) rw.add(L.s("words.reward-effect", "%effect%", es.getString(k + ".name", k)));
            if (lvl == plugin.getConfig().getInt("cosmetics.hat-level", 5)) rw.add(L.s("words.reward-hat"));
            if (lvl == plugin.getConfig().getInt("cosmetics.mount-level", 10)) rw.add(L.s("words.reward-mount"));

            String levelName = L.s("words.level-name", "%name%", d.displayName(t), "%level%", String.valueOf(lvl));
            inv.setItem(pathSlot(i), Items.glow(L.item(m, lvl, "items.levels.level",
                    "%levelname%", levelName, "%level%", String.valueOf(lvl), "%name%", d.displayName(t),
                    "%bonus%", Text.num(lvl * plugin.manager().bonus()),
                    "%rewards%", String.join("\n", rw),
                    "%status%", L.s(reached ? "words.unlocked" : "words.locked"),
                    "%xpneeded%", reached ? "" : L.s("words.level-xp", "%xp%", String.valueOf((int) plugin.manager().need(lvl - 1)))), d.level == lvl));
        }
        if (page > 1) inv.setItem(45, plugin.lang().item(Material.ARROW, "items.common.prev"));
        if (page < pages(plugin)) inv.setItem(53, plugin.lang().item(Material.ARROW, "items.common.next"));
        inv.setItem(49, plugin.lang().item(Material.BARRIER, "items.common.back"));
    }

    @Override
    public void click(InventoryClickEvent e) {
        int slot = e.getRawSlot();
        if (slot == 49) new DataMenu(plugin, player, petId).open();
        else if (slot == 45 && page > 1) new LevelsMenu(plugin, player, petId, page - 1).open();
        else if (slot == 53 && page < pages(plugin)) new LevelsMenu(plugin, player, petId, page + 1).open();
    }
}
