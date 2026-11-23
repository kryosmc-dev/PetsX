package me.mascotasx;

import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public class CosmeticsMenu extends Menu {
    private final String petId;

    public CosmeticsMenu(MascotasX plugin, Player p, String petId) {
        super(plugin, p, plugin.lang().s("titles.cosmetics"));
        this.petId = petId;
    }

    private int mountLevel() { return plugin.getConfig().getInt("cosmetics.mount-level", 10); }
    private int hatLevel() { return plugin.getConfig().getInt("cosmetics.hat-level", 5); }

    private String state(boolean b) { return plugin.lang().s(b ? "words.state-on" : "words.state-off"); }

    private String status(boolean ok, int req) {
        return plugin.lang().s(ok ? "words.click-use" : "words.need-level", "%level%", String.valueOf(req));
    }

    @Override
    protected void build() {
        fill();
        PetData d = data(petId);
        PetType t = plugin.manager().type(petId);
        if (d == null || t == null) { player.closeInventory(); return; }
        Class<?> cls = t.entity().getEntityClass();
        if (cls == null) cls = org.bukkit.entity.Entity.class;

        Lang L = plugin.lang();
        inv.setItem(10, L.item(Material.SADDLE, "items.cosmetics.mount", "%status%", status(d.level >= mountLevel(), mountLevel())));
        inv.setItem(11, L.item(Material.DIAMOND_HELMET, "items.cosmetics.hat", "%status%", status(d.level >= hatLevel(), hatLevel())));
        inv.setItem(12, L.item(Material.NAME_TAG, "items.cosmetics.name", "%name%", d.displayName(t)));
        inv.setItem(13, L.item(Material.CHORUS_FLOWER, "items.cosmetics.flip", "%state%", state(d.upside)));

        if (Cosmetics.isAgeable(cls))
            inv.setItem(14, L.item(Material.WHEAT, "items.cosmetics.baby", "%state%", state(d.baby)));
        else
            inv.setItem(14, L.item(Material.BARRIER, "items.cosmetics.baby-unavailable"));
        inv.setItem(15, L.item(Material.COOKIE, "items.cosmetics.pet"));

        int next = 19;
        if (Cosmetics.hasColor(cls)) {
            DyeColor dye = DyeColor.values()[Math.floorMod(d.color, 16)];
            Material m = Material.matchMaterial(dye.name() + "_DYE");
            inv.setItem(next++, L.item(m == null ? Material.INK_SAC : m, "items.cosmetics.color", "%current%", dye.name().toLowerCase().replace('_', ' ')));
        }
        int vc = Cosmetics.variantCount(cls);
        if (vc > 0) {
            inv.setItem(next, L.item(Material.PAINTING, "items.cosmetics.variant", "%index%", String.valueOf(Math.floorMod(d.variant, vc) + 1), "%total%", String.valueOf(vc)));
        }
        // Cosmeticos unicos de este mob (solo cosas posibles en vanilla)
        List<Cosmetics.Extra> ex = Cosmetics.extras(t);
        for (int i = 0; i < ex.size() && i < 14; i++) {
            Cosmetics.Extra x = ex.get(i);
            String base = "extras." + t.entity().name() + "." + x.key();
            List<String> labels = L.autoList(base + ".labels", x.labels());
            int idx = Math.floorMod(d.extra.getOrDefault(x.key(), 0), labels.size());
            ItemStack cu = d.custom.get(x.key());
            String cur = cu != null ? L.s("words.custom", "%item%", cu.getType().name().toLowerCase().replace('_', ' ')) : labels.get(idx);
            inv.setItem(extraSlot(i), L.item(x.icon(), "items.cosmetics.extra", "%name%", L.auto(base + ".name", x.name()),
                    "%current%", cur, "%index%", String.valueOf(idx + 1), "%total%", String.valueOf(labels.size())));
        }
        L.flush();
        inv.setItem(52, L.item(Material.BARRIER, "items.common.back"));
    }

    private static int extraSlot(int i) { return i < 7 ? 28 + i : 37 + (i - 7); }

    @Override
    public void click(InventoryClickEvent e) {
        PetData d = data(petId);
        PetType t = plugin.manager().type(petId);
        if (d == null || t == null) return;
        Class<?> cls = t.entity().getEntityClass();
        if (cls == null) cls = org.bukkit.entity.Entity.class;
        int slot = e.getRawSlot();

        if (slot == 52) { new DataMenu(plugin, player, petId).open(); return; }

        switch (slot) {
            case 10, 11 -> {
                boolean mount = slot == 10;
                int req = mount ? mountLevel() : hatLevel();
                if (d.level < req) { plugin.msg(player, "locked", "%level%", String.valueOf(req)); return; }
                LivingEntity pet = plugin.manager().isActive(player, petId) ? plugin.manager().activeEntity(player) : null;
                if (pet == null) { plugin.msg(player, "need-active"); return; }
                if (mount) {
                    if (player.getVehicle() == pet) player.leaveVehicle();
                    else { player.removePassenger(pet); pet.addPassenger(player); }
                } else {
                    if (pet.getHeight() > 1.6) { plugin.msg(player, "too-big"); return; }
                    if (player.getPassengers().contains(pet)) player.removePassenger(pet);
                    else { if (player.getVehicle() == pet) player.leaveVehicle(); player.addPassenger(pet); }
                }
                player.closeInventory();
            }
            case 12 -> plugin.listener().awaitName(player, petId);
            case 13 -> { d.upside = !d.upside; changed(); }
            case 14 -> { if (Cosmetics.isAgeable(cls)) { d.baby = !d.baby; changed(); } }
            case 15 -> {
                LivingEntity pet = plugin.manager().isActive(player, petId) ? plugin.manager().activeEntity(player) : null;
                if (pet == null) { plugin.msg(player, "need-active"); return; }
                pet.getWorld().spawnParticle(Particle.HEART, pet.getLocation().add(0, pet.getHeight() + 0.3, 0), 6, 0.4, 0.3, 0.4);
                player.playSound(player.getLocation(), Sound.ENTITY_CAT_PURR, 1f, 1f);
                plugin.msg(player, "petted");
            }
            case 19 -> {
                if (Cosmetics.hasColor(cls)) { d.color = Math.floorMod(d.color + 1, 16); changed(); }
                else if (Cosmetics.variantCount(cls) > 0) { d.variant++; changed(); }
            }
            case 20 -> { if (Cosmetics.hasColor(cls) && Cosmetics.variantCount(cls) > 0) { d.variant++; changed(); } }
            default -> {
                List<Cosmetics.Extra> ex = Cosmetics.extras(t);
                for (int i = 0; i < ex.size() && i < 14; i++) {
                    if (extraSlot(i) != slot) continue;
                    Cosmetics.Extra x = ex.get(i);
                    int n = x.labels().size();
                    int cur = Math.floorMod(d.extra.getOrDefault(x.key(), 0), n);
                    int next = e.isShiftClick() ? 0 : e.isRightClick() ? Math.floorMod(cur - 1, n) : Math.floorMod(cur + 1, n);
                    d.extra.put(x.key(), next);
                    d.custom.remove(x.key()); // al usar las opciones de la lista se quita el objeto libre
                    changed();
                    return;
                }
            }
        }
    }

    private void changed() {
        plugin.manager().save(player.getUniqueId());
        plugin.manager().refresh(player);
        build();
    }
}
