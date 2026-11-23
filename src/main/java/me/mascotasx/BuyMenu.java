package me.mascotasx;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public class BuyMenu extends Menu {
    private static final int PER_PAGE = 28;
    private final int page;

    public BuyMenu(MascotasX plugin, Player p, int page) {
        super(plugin, p, plugin.lang().s("titles.buy"));
        this.page = page;
    }

    private String sort() { return plugin.manager().shopSort(player.getUniqueId()); }

    private ItemStack item(PetType t, boolean own) {
        Lang L = plugin.lang();
        return Items.glow(L.item(t.icon(), "items.buy.pet", "%name%", t.name(), "%damage%", Text.num(t.damage()),
                "%price%", Text.money(plugin.manager().price(t)), "%status%", L.s(own ? "words.buy-owned" : "words.buy-click")), own);
    }

    private String sortName(String s) {
        return plugin.lang().s(switch (s) {
            case "damage" -> "words.sort-damage";
            case "price" -> "words.sort-price";
            default -> "words.sort-default";
        });
    }

    @Override
    protected void build() {
        fill();
        Profile pr = plugin.manager().profile(player.getUniqueId());
        String sort = sort();
        if (sort.equals("manual")) {
            for (PetType t : plugin.manager().types()) {
                if (t.page() != page || !t.enabled()) continue;
                inv.setItem(t.slot(), item(t, pr.owned.containsKey(t.id())));
            }
        } else {
            List<PetType> list = plugin.manager().shopOrder(sort);
            int start = (page - 1) * PER_PAGE;
            for (int i = 0; i < PER_PAGE && start + i < list.size(); i++) {
                PetType t = list.get(start + i);
                inv.setItem(grid(i), item(t, pr.owned.containsKey(t.id())));
            }
        }
        int pages = plugin.manager().shopPages(sort);
        if (page > 1) inv.setItem(45, plugin.lang().item(Material.ARROW, "items.common.prev"));
        if (page < pages) inv.setItem(53, plugin.lang().item(Material.ARROW, "items.common.next"));

        StringBuilder modes = new StringBuilder();
        for (String m : PetManager.SORTS) {
            if (modes.length() > 0) modes.append('\n');
            modes.append(plugin.lang().s(m.equals(sort) ? "words.sort-selected" : "words.sort-other", "%mode%", sortName(m)));
        }
        inv.setItem(4, plugin.lang().item(Material.HOPPER, "items.buy.sort", "%modes%", modes.toString()));
    }

    @Override
    public void click(InventoryClickEvent e) {
        int slot = e.getRawSlot();
        String sort = sort();
        int pages = plugin.manager().shopPages(sort);
        if (slot == 4) { plugin.manager().cycleShopSort(player.getUniqueId()); new BuyMenu(plugin, player, 1).open(); return; }
        if (slot == 45 && page > 1) { new BuyMenu(plugin, player, page - 1).open(); return; }
        if (slot == 53 && page < pages) { new BuyMenu(plugin, player, page + 1).open(); return; }

        PetType found = null;
        if (sort.equals("manual")) {
            for (PetType t : plugin.manager().types())
                if (t.page() == page && t.slot() == slot && t.enabled()) { found = t; break; }
        } else {
            List<PetType> list = plugin.manager().shopOrder(sort);
            for (int i = 0; i < PER_PAGE; i++) {
                if (grid(i) != slot) continue;
                int idx = (page - 1) * PER_PAGE + i;
                if (idx < list.size()) found = list.get(idx);
                break;
            }
        }
        if (found == null) return;
        if (plugin.manager().profile(player.getUniqueId()).owned.containsKey(found.id())) { plugin.msg(player, "already-owned"); return; }
        new ConfirmMenu(plugin, player, found.id(), page).open(); // la compra se hace al confirmar
    }
}
