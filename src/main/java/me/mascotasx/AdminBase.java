package me.mascotasx;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

/** Base de los menus del panel de administracion (exige el permiso mascotas.admin). */
public abstract class AdminBase extends Menu {
    protected AdminBase(MascotasX plugin, Player player, String title) {
        super(plugin, player, title);
    }

    @Override
    public void open() {
        if (!player.hasPermission("mascotas.admin")) { plugin.msg(player, "no-perm"); return; }
        super.open();
    }

    @Override
    public final void click(InventoryClickEvent e) {
        if (!player.hasPermission("mascotas.admin")) { player.closeInventory(); return; }
        onClick(e);
    }

    protected abstract void onClick(InventoryClickEvent e);

    /** Click izquierdo suma, derecho resta; con Shift x10. */
    protected static double delta(InventoryClickEvent e, double step) {
        double m = e.isShiftClick() ? 10 : 1;
        if (e.isLeftClick()) return step * m;
        if (e.isRightClick()) return -step * m;
        return 0;
    }

    protected static String title(String subKey, String... kv) {
        Lang L = MascotasX.get().lang();
        return L.s("admin.title", "%sub%", L.s("admin.subtitles." + subKey, kv));
    }

    /** Mensaje de admin: key dentro de admin.say-texts. */
    protected void say(String key, String... kv) {
        Lang L = plugin.lang();
        player.sendMessage(Text.c(L.s("admin.say", "%text%", L.s("admin.say-texts." + key, kv))));
    }

    /** Texto de aviso para entrada por chat (admin.prompts.*), ya con el prefijo ADMIN. */
    protected String prompt(String key, String... kv) {
        Lang L = plugin.lang();
        return L.s("admin.say", "%text%", L.s("admin.prompts." + key, kv));
    }

    protected String w(String key) { return plugin.lang().s("admin.words." + key); }

    protected ItemStack it(Material m, String key, String... kv) { return plugin.lang().item(m, "admin.menu." + key, kv); }

    protected ItemStack back() { return plugin.lang().item(Material.BARRIER, "items.common.back"); }
    protected ItemStack prevBtn() { return plugin.lang().item(Material.ARROW, "items.common.prev"); }
    protected ItemStack nextBtn() { return plugin.lang().item(Material.ARROW, "items.common.next"); }
}
