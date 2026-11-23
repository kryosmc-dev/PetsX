package me.mascotasx;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;

import java.util.UUID;

public class AdminPlayerMenu extends AdminBase {
    private final UUID target;

    public AdminPlayerMenu(MascotasX plugin, Player p, UUID target) {
        super(plugin, p, title("player"));
        this.target = target;
    }

    private Player tp() { return plugin.getServer().getPlayer(target); }

    @Override
    protected void build() {
        fill();
        Player t = tp();
        if (t == null) { player.closeInventory(); return; }
        Profile pr = plugin.manager().profile(target);
        PetData d = pr.active == null ? null : pr.owned.get(pr.active);
        Lang L = plugin.lang();
        inv.setItem(4, it(Material.PLAYER_HEAD, "player.head", "%player%", t.getName(), "%count%", String.valueOf(pr.owned.size())));
        inv.setItem(20, it(Material.CHEST, "player.give"));
        inv.setItem(22, it(Material.EXPERIENCE_BOTTLE, "player.level", "%status%", d == null ? L.s("admin.menu.player.level-none")
                : L.s("admin.menu.player.level-current", "%level%", String.valueOf(d.level), "%max%", String.valueOf(plugin.manager().maxLevel()))));
        inv.setItem(24, it(Material.TNT, "player.wipe"));
        inv.setItem(49, back());
    }

    @Override
    protected void onClick(InventoryClickEvent e) {
        Player t = tp();
        if (t == null) { player.closeInventory(); return; }
        PetManager m = plugin.manager();
        switch (e.getRawSlot()) {
            case 20 -> new AdminPetPickMenu(plugin, player, AdminPetPickMenu.Mode.GIVE, 1, target).open();
            case 22 -> {
                Profile pr = m.profile(target);
                PetData d = pr.active == null ? null : pr.owned.get(pr.active);
                if (d == null) { say("no-active"); return; }
                int step = e.isShiftClick() ? 5 : 1;
                d.level = Math.max(1, Math.min(m.maxLevel(), d.level + (e.isLeftClick() ? step : -step)));
                d.xp = 0;
                m.refresh(t);
                m.save(target);
                build();
            }
            case 24 -> {
                if (!e.isShiftClick()) { say("shift-confirm"); return; }
                m.wipe(t);
                say("wiped", "%player%", t.getName());
                build();
            }
            case 49 -> new AdminPlayersMenu(plugin, player).open();
            default -> { }
        }
    }
}
