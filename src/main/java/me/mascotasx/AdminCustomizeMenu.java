package me.mascotasx;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.Locale;

/** Personalizar titulos de los menus y los iconos (fondo, camino de niveles y botones del menu de datos). */
public class AdminCustomizeMenu extends AdminBase {
    private record T(String key, int slot) {}
    private record Ic(String key, Material def, int slot) {}

    private static final List<T> TITLES = List.of(
            new T("buy", 10), new T("pets", 11), new T("data", 12),
            new T("cosmetics", 13), new T("levels", 14), new T("effects", 15), new T("confirm", 16));

    private static final List<Ic> ICONS = List.of(
            new Ic("filler", Material.GRAY_STAINED_GLASS_PANE, 28),
            new Ic("levels.reached", Material.LIME_STAINED_GLASS_PANE, 30),
            new Ic("levels.next", Material.YELLOW_STAINED_GLASS_PANE, 31),
            new Ic("levels.locked", Material.PINK_STAINED_GLASS_PANE, 32),
            new Ic("data.mute", Material.BELL, 37),
            new Ic("data.burn", Material.LAVA_BUCKET, 38),
            new Ic("data.cosmetics", Material.ARMOR_STAND, 39),
            new Ic("data.levels", Material.EXPERIENCE_BOTTLE, 40),
            new Ic("data.effects", Material.BREWING_STAND, 41),
            new Ic("data.attack", Material.DIAMOND_SWORD, 42),
            new Ic("data.summon", Material.ENDER_PEARL, 43),
            new Ic("data.back", Material.BARRIER, 44),
            new Ic("confirm.yes", Material.LIME_STAINED_GLASS_PANE, 45),
            new Ic("confirm.no", Material.RED_STAINED_GLASS_PANE, 46));

    public AdminCustomizeMenu(MascotasX plugin, Player p) { super(plugin, p, title("customize")); }

    private String titleName(T t) { return plugin.lang().s("admin.menu.customize.title-names." + t.key); }
    private String iconName(Ic ic) { return plugin.lang().s("admin.menu.customize.icon-names." + ic.key.replace('.', '-')); }

    @Override
    protected void build() {
        fill();
        Lang L = plugin.lang();
        inv.setItem(4, it(Material.BOOK, "customize.info"));
        for (T t : TITLES) {
            inv.setItem(t.slot, it(Material.PAPER, "customize.title", "%name%", titleName(t), "%value%", L.raw("titles." + t.key),
                    "%help%", L.s(t.key.equals("levels") ? "admin.menu.customize.title-help-levels" : "admin.menu.customize.title-help")));
        }
        for (Ic ic : ICONS) {
            Material cur = plugin.icon(ic.key, ic.def);
            inv.setItem(ic.slot, it(cur, "customize.icon", "%name%", iconName(ic), "%current%", cur.name().toLowerCase(Locale.ROOT).replace('_', ' ')));
        }
        inv.setItem(49, back());
    }

    @Override
    protected void onClick(InventoryClickEvent e) {
        int slot = e.getRawSlot();
        if (slot == 49) { new AdminMenu(plugin, player).open(); return; }
        for (T t : TITLES) {
            if (t.slot != slot) continue;
            if (e.isRightClick()) {
                plugin.lang().reset("titles." + t.key); // vuelve al valor original
                build();
            } else {
                plugin.listener().awaitInput(player, prompt("title", "%name%", titleName(t)), text -> {
                    plugin.lang().set("titles." + t.key, text);
                    say("title-updated");
                    new AdminCustomizeMenu(plugin, player).open();
                });
            }
            return;
        }
        for (Ic ic : ICONS) {
            if (ic.slot != slot) continue;
            if (e.isRightClick()) {
                plugin.getConfig().set("icons." + ic.key, null);
                plugin.saveConfig();
                build();
                return;
            }
            ItemStack hand = player.getInventory().getItemInMainHand();
            if (!hand.getType().isAir() && hand.getType().isItem()) {
                plugin.getConfig().set("icons." + ic.key, hand.getType().name());
                plugin.saveConfig();
                build();
            } else {
                plugin.listener().awaitInput(player, prompt("icon", "%name%", iconName(ic)), text -> {
                    Material m = Material.matchMaterial(text.trim().toUpperCase(Locale.ROOT).replace(' ', '_'));
                    if (m == null || !m.isItem()) say("bad-material");
                    else {
                        plugin.getConfig().set("icons." + ic.key, m.name());
                        plugin.saveConfig();
                        say("icon-updated");
                    }
                    new AdminCustomizeMenu(plugin, player).open();
                });
            }
            return;
        }
    }
}
