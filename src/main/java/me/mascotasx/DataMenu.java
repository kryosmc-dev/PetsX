package me.mascotasx;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

public class DataMenu extends Menu {
    private final String petId;

    public DataMenu(MascotasX plugin, Player p, String petId) {
        super(plugin, p, plugin.lang().s("titles.data"));
        this.petId = petId;
    }

    private String state(boolean b) { return plugin.lang().s(b ? "words.state-on" : "words.state-off"); }

    @Override
    protected void build() {
        fill();
        PetData d = data(petId);
        PetType t = plugin.manager().type(petId);
        if (d == null || t == null) { player.closeInventory(); return; }
        boolean on = plugin.manager().isActive(player, petId);

        Lang L = plugin.lang();
        // Informacion
        inv.setItem(4, L.item(t.icon(), "items.data.info", "%name%", d.displayName(t),
                "%level%", String.valueOf(d.level), "%max%", String.valueOf(plugin.manager().maxLevel()),
                "%xpline%", PetsMenu.xpLine(plugin, d), "%damage%", Text.num(plugin.manager().damage(t, d))));

        // Fila superior
        inv.setItem(10, Items.glow(L.item(plugin.icon("data.mute", Material.BELL), "items.data.mute", "%state%", state(d.muted)), d.muted));
        inv.setItem(11, Items.glow(L.item(plugin.icon("data.burn", d.burning ? Material.LAVA_BUCKET : Material.BUCKET), "items.data.burn", "%state%", state(d.burning)), d.burning));

        // Objetos libres: lo que el jugador quiera ponerle a su mascota (solo visual)
        if (Cosmetics.canHold(t)) inv.setItem(12, objectButton(d, "hand"));
        if (Cosmetics.canWear(t)) inv.setItem(13, objectButton(d, "head"));

        // Fila inferior
        inv.setItem(46, L.item(plugin.icon("data.cosmetics", Material.ARMOR_STAND), "items.data.cosmetics"));
        inv.setItem(47, L.item(plugin.icon("data.levels", Material.EXPERIENCE_BOTTLE), "items.data.levels"));
        inv.setItem(48, L.item(plugin.icon("data.effects", Material.BREWING_STAND), "items.data.effects"));
        inv.setItem(49, Items.glow(L.item(plugin.icon("data.attack", Material.DIAMOND_SWORD), "items.data.attack", "%state%", state(d.attack)), d.attack));
        inv.setItem(51, L.item(plugin.icon("data.summon", on ? Material.LEAD : Material.ENDER_PEARL), on ? "items.data.summon-on" : "items.data.summon-off"));
        inv.setItem(52, L.item(plugin.icon("data.back", Material.BARRIER), "items.common.back"));
    }

    @Override
    public void click(InventoryClickEvent e) {
        PetData d = data(petId);
        if (d == null) return;
        switch (e.getRawSlot()) {
            case 10 -> { d.muted = !d.muted; changed(); }
            case 11 -> { d.burning = !d.burning; changed(); }
            case 12, 13 -> {
                PetType t = plugin.manager().type(petId);
                if (t != null) setObject(d, t, e.getRawSlot() == 12 ? "hand" : "head", e);
            }
            case 46 -> new CosmeticsMenu(plugin, player, petId).open();
            case 47 -> new LevelsMenu(plugin, player, petId, 1).open();
            case 48 -> new EffectsMenu(plugin, player, petId).open();
            case 49 -> { d.attack = !d.attack; changed(); }
            case 51 -> { plugin.manager().toggle(player, petId); build(); }
            case 52 -> PetCommand.openPets(plugin, player);
            default -> { }
        }
    }

    private ItemStack objectButton(PetData d, String key) {
        Lang L = plugin.lang();
        ItemStack cu = d.custom.get(key);
        String cur = cu == null ? L.s("words.object-none") : cu.getType().name().toLowerCase().replace('_', ' ');
        Material m = cu == null ? plugin.icon("data." + key, Material.BARRIER) : cu.getType();
        ItemStack it = L.itemKeys(m, "items.data." + key + ".name", "items.data.object.lore", "%current%", cur);
        return cu == null ? it : Items.glow(it, true);
    }

    private void setObject(PetData d, PetType t, String key, InventoryClickEvent e) {
        if (key.equals("hand") ? !Cosmetics.canHold(t) : !Cosmetics.canWear(t)) return;
        if (e.isRightClick() || e.isShiftClick()) { d.custom.remove(key); d.extra.put(key, 0); changed(); return; }
        if (!plugin.getConfig().getBoolean("cosmetics.any-item", true)) { plugin.msg(player, "no-perm"); return; }
        ItemStack hand = player.getInventory().getItemInMainHand();
        if (hand.getType().isAir()) { plugin.msg(player, "hold-item"); return; }
        d.custom.put(key, Cosmetics.asCosmetic(hand));
        changed();
    }

    private void changed() {
        plugin.manager().save(player.getUniqueId());
        plugin.manager().refresh(player);
        build();
    }
}
