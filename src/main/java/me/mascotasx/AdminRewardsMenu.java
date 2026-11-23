package me.mascotasx;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;

import java.util.ArrayList;
import java.util.List;

public class AdminRewardsMenu extends AdminBase {
    private static final int PER_PAGE = 28;
    private final int page;

    public AdminRewardsMenu(MascotasX plugin, Player p, int page) {
        super(plugin, p, title("rewards", "%page%", String.valueOf(page)));
        this.page = page;
    }

    private String path(int lvl) { return "levels.rewards." + lvl; }

    @Override
    protected void build() {
        fill();
        int max = plugin.manager().maxLevel();
        int start = (page - 1) * PER_PAGE + 1;
        for (int i = 0; i < PER_PAGE && start + i <= max; i++) {
            int lvl = start + i;
            ConfigurationSection r = plugin.getConfig().getConfigurationSection(path(lvl));
            double money = r == null ? 0 : r.getDouble("money", 0);
            List<String> cmds = r == null ? List.of() : r.getStringList("commands");
            boolean has = money > 0 || !cmds.isEmpty();
            StringBuilder cl = new StringBuilder();
            if (cmds.isEmpty()) cl.append(plugin.lang().s("admin.menu.rewards.commands-header")).append(' ').append(w("no-commands"));
            else {
                cl.append(plugin.lang().s("admin.menu.rewards.commands-header"));
                for (String c : cmds) cl.append('\n').append(plugin.lang().s("admin.words.command-line", "%cmd%", c));
            }
            inv.setItem(grid(i), plugin.lang().item(has ? Material.EMERALD : Material.COAL, lvl, "admin.menu.rewards.level",
                    "%color%", has ? "<green>" : "<gray>", "%level%", String.valueOf(lvl), "%money%", Text.money(money), "%commands%", cl.toString()));
        }
        if (page > 1) inv.setItem(45, prevBtn());
        if (start + PER_PAGE <= max) inv.setItem(53, nextBtn());
        inv.setItem(49, back());
    }

    @Override
    protected void onClick(InventoryClickEvent e) {
        int slot = e.getRawSlot();
        if (slot == 49) { new AdminMenu(plugin, player).open(); return; }
        if (slot == 45 && page > 1) { new AdminRewardsMenu(plugin, player, page - 1).open(); return; }
        if (slot == 53 && (page * PER_PAGE) < plugin.manager().maxLevel()) { new AdminRewardsMenu(plugin, player, page + 1).open(); return; }
        for (int i = 0; i < PER_PAGE; i++) {
            if (grid(i) != slot) continue;
            int lvl = (page - 1) * PER_PAGE + 1 + i;
            if (lvl > plugin.manager().maxLevel()) return;
            ClickType ct = e.getClick();
            if (ct == ClickType.DROP) {
                plugin.listener().awaitInput(player, prompt("reward-command", "%level%", String.valueOf(lvl)), text -> {
                            String cmd = text, desc = null;
                            int bar = text.indexOf('|');
                            if (bar >= 0) { cmd = text.substring(0, bar).trim(); desc = text.substring(bar + 1).trim(); }
                            if (cmd.startsWith("/")) cmd = cmd.substring(1);
                            if (cmd.isEmpty()) { new AdminRewardsMenu(plugin, player, page).open(); return; }
                            List<String> cmds = new ArrayList<>(plugin.getConfig().getStringList(path(lvl) + ".commands"));
                            cmds.add(cmd);
                            plugin.getConfig().set(path(lvl) + ".commands", cmds);
                            if (desc != null && !desc.isEmpty()) {
                                List<String> ds = new ArrayList<>(plugin.getConfig().getStringList(path(lvl) + ".description"));
                                ds.add("<gray>" + desc);
                                plugin.getConfig().set(path(lvl) + ".description", ds);
                            }
                            plugin.saveConfig();
                            say("command-added", "%level%", String.valueOf(lvl));
                            new AdminRewardsMenu(plugin, player, page).open();
                        });
                return;
            }
            if (ct == ClickType.CONTROL_DROP) {
                plugin.getConfig().set(path(lvl) + ".commands", new ArrayList<String>());
                plugin.getConfig().set(path(lvl) + ".description", new ArrayList<String>());
            } else {
                double d = delta(e, 100);
                if (d == 0) return;
                double money = Math.max(0, plugin.getConfig().getDouble(path(lvl) + ".money", 0) + d);
                plugin.getConfig().set(path(lvl) + ".money", money);
            }
            plugin.saveConfig();
            build();
            return;
        }
    }
}
