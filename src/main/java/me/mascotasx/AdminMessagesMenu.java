package me.mascotasx;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;

import java.util.ArrayList;
import java.util.List;

/** Edita los mensajes del chat (messages.* en lang.yml) desde el juego. */
public class AdminMessagesMenu extends AdminBase {
    private static final int PER_PAGE = 28;
    private final int page;
    private final List<String> keys = new ArrayList<>();

    public AdminMessagesMenu(MascotasX plugin, Player p, int page) {
        super(plugin, p, title("messages", "%page%", String.valueOf(page)));
        this.page = page;
    }

    @Override
    protected void build() {
        fill();
        keys.clear();
        keys.addAll(plugin.lang().keys("messages"));
        int start = (page - 1) * PER_PAGE;
        for (int i = 0; i < PER_PAGE && start + i < keys.size(); i++) {
            String k = keys.get(start + i);
            inv.setItem(grid(i), it(Material.PAPER, "messages.entry", "%key%", k, "%value%", plugin.lang().raw("messages." + k)));
        }
        if (page > 1) inv.setItem(45, prevBtn());
        if (start + PER_PAGE < keys.size()) inv.setItem(53, nextBtn());
        inv.setItem(49, back());
    }

    @Override
    protected void onClick(InventoryClickEvent e) {
        int slot = e.getRawSlot();
        if (slot == 49) { new AdminMenu(plugin, player).open(); return; }
        if (slot == 45 && page > 1) { new AdminMessagesMenu(plugin, player, page - 1).open(); return; }
        if (slot == 53 && page * PER_PAGE < keys.size()) { new AdminMessagesMenu(plugin, player, page + 1).open(); return; }
        for (int i = 0; i < PER_PAGE; i++) {
            if (grid(i) != slot) continue;
            int idx = (page - 1) * PER_PAGE + i;
            if (idx >= keys.size()) return;
            String path = "messages." + keys.get(idx);
            String key = keys.get(idx);
            if (e.isRightClick()) {
                plugin.lang().reset(path); // vuelve al original del jar
                build();
            } else {
                plugin.listener().awaitInput(player, prompt("message", "%key%", key), text -> {
                    plugin.lang().set(path, text);
                    say("msg-updated");
                    new AdminMessagesMenu(plugin, player, page).open();
                });
            }
            return;
        }
    }
}
