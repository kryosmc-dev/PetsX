package me.mascotasx;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;

/** Confirmacion de compra (se abre desde BuyMenu): confirmar a la izquierda, mascota al centro, cancelar a la derecha. */
public class ConfirmMenu extends Menu {
    private static final int YES = 10, PET = 13, NO = 16;
    private final String petId;
    private final int page;

    public ConfirmMenu(MascotasX plugin, Player p, String petId, int page) {
        super(plugin, p, plugin.lang().s("titles.confirm"), 27);
        this.petId = petId;
        this.page = page;
    }

    @Override
    protected void build() {
        fill();
        PetType t = plugin.manager().type(petId);
        if (t == null || !t.enabled()) { player.closeInventory(); return; }

        Lang L = plugin.lang();
        String price = Text.money(plugin.manager().price(t));
        String[] kv = {"%name%", t.name(), "%damage%", Text.num(t.damage()), "%price%", price};
        inv.setItem(PET, L.item(t.icon(), "items.confirm.pet", kv));
        inv.setItem(YES, L.item(plugin.icon("confirm.yes", Material.LIME_STAINED_GLASS_PANE), "items.confirm.accept", kv));
        inv.setItem(NO, L.item(plugin.icon("confirm.no", Material.RED_STAINED_GLASS_PANE), "items.confirm.cancel", kv));
    }

    @Override
    public void click(InventoryClickEvent e) {
        switch (e.getRawSlot()) {
            case YES -> {
                PetType t = plugin.manager().type(petId);
                if (t != null && t.enabled()) plugin.manager().buy(player, t);
                new BuyMenu(plugin, player, page).open();
            }
            case NO -> new BuyMenu(plugin, player, page).open();
            default -> { }
        }
    }
}
