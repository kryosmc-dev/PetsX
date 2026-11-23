package me.mascotasx;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;

import java.util.List;

public class AdminPetEditMenu extends AdminBase {
    private static final List<String> ATTACKS = List.of("melee", "arrow", "fireball", "snowball", "spit", "potion", "wind", "fangs");
    private final String petId;

    public AdminPetEditMenu(MascotasX plugin, Player p, String petId) {
        super(plugin, p, title("edit"));
        this.petId = petId;
    }

    @Override
    protected void build() {
        fill();
        PetType t = plugin.manager().type(petId);
        if (t == null) { player.closeInventory(); return; }
        inv.setItem(4, it(t.icon(), "edit.info", "%name%", t.name(), "%id%", t.id(), "%entity%", t.entity().name()));
        inv.setItem(20, it(Material.GOLD_INGOT, "edit.price", "%base%", Text.money(t.price()), "%final%", Text.money(plugin.manager().price(t))));
        inv.setItem(22, it(Material.DIAMOND_SWORD, "edit.damage", "%damage%", Text.num(t.damage())));
        inv.setItem(24, it(t.enabled() ? Material.LIME_DYE : Material.GRAY_DYE, "edit.enabled", "%state%", w(t.enabled() ? "visible" : "hidden")));
        inv.setItem(29, it(Material.BOW, "edit.attack", "%attack%", t.attack()));
        inv.setItem(31, it(Material.FEATHER, "edit.flying", "%state%", w(t.flying() ? "opt-yes" : "opt-no")));
        inv.setItem(33, it(Material.NAME_TAG, "edit.shopname", "%name%", t.name()));
        inv.setItem(35, it(t.icon(), "edit.icon", "%current%", t.icon().name().toLowerCase().replace('_', ' ')));
        inv.setItem(38, it(Material.BOOK, "edit.page", "%page%", String.valueOf(t.page())));
        inv.setItem(40, it(Material.COMPASS, "edit.slot", "%slot%", String.valueOf(t.slot())));
        inv.setItem(49, back());
    }

    @Override
    protected void onClick(InventoryClickEvent e) {
        PetType t = plugin.manager().type(petId);
        if (t == null) return;
        PetManager m = plugin.manager();
        switch (e.getRawSlot()) {
            case 20 -> {
                double d = delta(e, 100);
                if (d == 0) return;
                m.setPetValue(petId, "price", Math.max(0, t.price() + d));
                build();
            }
            case 22 -> {
                double d = delta(e, 0.5);
                if (d == 0) return;
                m.setPetValue(petId, "damage", Math.max(0.5, Math.round((t.damage() + d) * 10) / 10.0));
                build();
            }
            case 24 -> { m.setPetValue(petId, "enabled", !t.enabled()); build(); }
            case 29 -> {
                int i = ATTACKS.indexOf(t.attack());
                m.setPetValue(petId, "attack", ATTACKS.get((i + 1) % ATTACKS.size()));
                build();
            }
            case 31 -> { m.setPetValue(petId, "flying", !t.flying()); build(); }
            case 33 -> plugin.listener().awaitInput(player, prompt("pet-name"), text -> {
                m.setPetValue(petId, "name", text);
                say("name-updated");
                new AdminPetEditMenu(plugin, player, petId).open();
            });
            case 35 -> {
                if (e.isRightClick()) { m.setPetValue(petId, "icon", null); build(); return; }
                Material hand = player.getInventory().getItemInMainHand().getType();
                if (hand.isAir() || !hand.isItem()) { say("hold-icon"); return; }
                m.setPetValue(petId, "icon", hand.name());
                build();
            }
            case 38 -> {
                int d = (int) delta(e, 1);
                if (d == 0) return;
                m.setPetValue(petId, "page", Math.max(1, t.page() + (d > 0 ? 1 : -1)));
                build();
            }
            case 40 -> {
                double d = delta(e, 1);
                if (d == 0) return;
                int step = e.isShiftClick() ? 9 : 1;
                m.setPetValue(petId, "slot", Math.max(0, Math.min(53, t.slot() + (d > 0 ? step : -step))));
                build();
            }
            case 49 -> new AdminPetPickMenu(plugin, player, AdminPetPickMenu.Mode.EDIT, 1, null).open();
            default -> { }
        }
    }
}
