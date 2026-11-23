package me.mascotasx;

import io.papermc.paper.world.WeatheringCopperState;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.Keyed;
import org.bukkit.Registry;
import org.bukkit.entity.*;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

/** Cosmeticos visuales que dependen del tipo de mob (colores, variantes). */
public final class Cosmetics {
    private Cosmetics() {}

    private static <T extends Keyed> List<T> list(Registry<T> r) {
        List<T> l = new ArrayList<>();
        for (T t : r) l.add(t);
        return l;
    }

    public static boolean hasColor(Class<?> c) {
        return Sheep.class.isAssignableFrom(c) || Wolf.class.isAssignableFrom(c) || Cat.class.isAssignableFrom(c);
    }

    public static boolean isAgeable(Class<?> c) {
        return Ageable.class.isAssignableFrom(c);
    }

    public static int variantCount(Class<?> c) {
        if (Axolotl.class.isAssignableFrom(c)) return Axolotl.Variant.values().length;
        if (Rabbit.class.isAssignableFrom(c)) return Rabbit.Type.values().length;
        if (Fox.class.isAssignableFrom(c)) return Fox.Type.values().length;
        if (Parrot.class.isAssignableFrom(c)) return Parrot.Variant.values().length;
        if (Horse.class.isAssignableFrom(c)) return Horse.Color.values().length;
        if (Llama.class.isAssignableFrom(c)) return Llama.Color.values().length;
        if (MushroomCow.class.isAssignableFrom(c)) return MushroomCow.Variant.values().length;
        if (Cat.class.isAssignableFrom(c)) return list(Registry.CAT_VARIANT).size();
        if (Wolf.class.isAssignableFrom(c)) return list(Registry.WOLF_VARIANT).size();
        if (Frog.class.isAssignableFrom(c)) return list(Registry.FROG_VARIANT).size();
        if (Villager.class.isAssignableFrom(c)) return list(Registry.VILLAGER_TYPE).size();
        return 0;
    }

    public static void apply(LivingEntity e, PetData d) {
        DyeColor dye = DyeColor.values()[Math.floorMod(d.color, DyeColor.values().length)];
        if (e instanceof Sheep s) s.setColor(dye);
        if (e instanceof Wolf w) { w.setTamed(true); w.setCollarColor(dye); }
        if (e instanceof Cat c) { c.setTamed(true); c.setCollarColor(dye); }

        int n = variantCount(e.getClass());
        if (n == 0) return;
        int i = Math.floorMod(d.variant, n);
        if (e instanceof Axolotl a) a.setVariant(Axolotl.Variant.values()[i]);
        else if (e instanceof Rabbit r) r.setRabbitType(Rabbit.Type.values()[i]);
        else if (e instanceof Fox f) f.setFoxType(Fox.Type.values()[i]);
        else if (e instanceof Parrot p) p.setVariant(Parrot.Variant.values()[i]);
        else if (e instanceof Horse h) h.setColor(Horse.Color.values()[i]);
        else if (e instanceof Llama l) l.setColor(Llama.Color.values()[i]);
        else if (e instanceof MushroomCow m) m.setVariant(MushroomCow.Variant.values()[i]);
        else if (e instanceof Cat c) c.setCatType(list(Registry.CAT_VARIANT).get(i));
        else if (e instanceof Wolf w) w.setVariant(list(Registry.WOLF_VARIANT).get(i));
        else if (e instanceof Frog fr) fr.setVariant(list(Registry.FROG_VARIANT).get(i));
        else if (e instanceof Villager v) v.setVillagerType(list(Registry.VILLAGER_TYPE).get(i));
    }

    // ================================================================== cosmeticos unicos por mob
    // Solo cosas que existen en Minecraft vanilla para ese mob (objetos en mano, armaduras, bloque del enderman, etc.).

    public record Extra(String key, String name, Material icon, List<String> labels) {}

    private static final Set<EntityType> ARMOR = EnumSet.of(EntityType.ZOMBIE, EntityType.HUSK, EntityType.DROWNED, EntityType.ZOMBIE_VILLAGER,
            EntityType.ZOMBIFIED_PIGLIN, EntityType.PIGLIN, EntityType.PIGLIN_BRUTE, EntityType.SKELETON, EntityType.STRAY,
            EntityType.BOGGED, EntityType.WITHER_SKELETON);
    private static final Set<EntityType> HOLD = EnumSet.of(EntityType.ZOMBIE, EntityType.HUSK, EntityType.DROWNED, EntityType.ZOMBIE_VILLAGER,
            EntityType.ZOMBIFIED_PIGLIN, EntityType.PIGLIN, EntityType.PIGLIN_BRUTE, EntityType.SKELETON, EntityType.STRAY,
            EntityType.BOGGED, EntityType.WITHER_SKELETON, EntityType.PILLAGER, EntityType.VINDICATOR, EntityType.EVOKER,
            EntityType.WITCH, EntityType.VEX, EntityType.ALLAY, EntityType.COPPER_GOLEM, EntityType.FOX);

    private static final List<String> HAND_DEFAULT = List.of("WOODEN_SWORD", "STONE_SWORD", "IRON_SWORD", "GOLDEN_SWORD", "DIAMOND_SWORD",
            "NETHERITE_SWORD", "IRON_AXE", "DIAMOND_AXE", "IRON_PICKAXE", "DIAMOND_PICKAXE", "IRON_SHOVEL", "TRIDENT", "SHIELD", "TORCH",
            "LANTERN", "SOUL_LANTERN", "APPLE", "GOLDEN_APPLE", "CARROT", "BREAD", "COOKED_BEEF", "DIAMOND", "EMERALD", "GOLD_INGOT",
            "NETHERITE_INGOT", "AMETHYST_SHARD", "TOTEM_OF_UNDYING", "POPPY", "DANDELION", "BOOK", "COMPASS", "CLOCK", "SPYGLASS",
            "FISHING_ROD", "BONE", "STICK", "HEART_OF_THE_SEA", "NAUTILUS_SHELL", "BELL", "LEAD", "ENDER_PEARL", "FIREWORK_ROCKET", "COOKIE");
    private static final List<Material> HEADS = List.of(Material.LEATHER_HELMET, Material.CHAINMAIL_HELMET, Material.IRON_HELMET,
            Material.GOLDEN_HELMET, Material.DIAMOND_HELMET, Material.NETHERITE_HELMET, Material.TURTLE_HELMET, Material.CARVED_PUMPKIN,
            Material.ZOMBIE_HEAD, Material.SKELETON_SKULL, Material.WITHER_SKELETON_SKULL, Material.CREEPER_HEAD, Material.PIGLIN_HEAD,
            Material.DRAGON_HEAD);
    private static final String[] ARMOR_SETS = {"LEATHER", "CHAINMAIL", "IRON", "GOLDEN", "DIAMOND", "NETHERITE"};
    private static final int[] SLIME_SIZES = {2, 1, 3, 4};
    private static final int[] PHANTOM_SIZES = {0, 2, 4, 6};

    private static String pretty(Material m) { return m.name().toLowerCase(java.util.Locale.ROOT).replace('_', ' '); }

    private static List<Material> mats(String path, List<String> def, boolean block) {
        List<String> raw = MascotasX.get().getConfig().getStringList(path);
        if (raw.isEmpty()) raw = def;
        List<Material> out = new ArrayList<>();
        for (String s : raw) {
            Material m = Material.matchMaterial(s);
            if (m != null && (block ? m.isBlock() : m.isItem()) && !out.contains(m)) out.add(m);
        }
        return out;
    }
    private static List<Material> handItems() { return mats("cosmetics.hand-items", HAND_DEFAULT, false); }
    private static List<Villager.Type> biomes() { // llanura primero (el aspecto normal)
        List<Villager.Type> l = new ArrayList<>(list(Registry.VILLAGER_TYPE));
        l.remove(Villager.Type.PLAINS);
        l.add(0, Villager.Type.PLAINS);
        return l;
    }

    public static boolean canHold(PetType t) { return HOLD.contains(t.entity()); }
    public static boolean canWear(PetType t) { return ARMOR.contains(t.entity()); }
    /** Copia solo visual de un item del jugador (1 unidad). */
    public static ItemStack asCosmetic(ItemStack it) { ItemStack c = it.clone(); c.setAmount(1); return c; }

    private static List<Villager.Profession> professions() {
        List<Villager.Profession> l = new ArrayList<>();
        for (Villager.Profession p : list(Registry.VILLAGER_PROFESSION)) if (p != Villager.Profession.NONE) l.add(p);
        return l;
    }

    /** Arma de serie de cada mob cuando no se elige objeto (lo que llevaria en vanilla). */
    private static Material defaultHand(EntityType et) {
        return switch (et) {
            case VINDICATOR -> Material.IRON_AXE;
            case PIGLIN, ZOMBIFIED_PIGLIN -> Material.GOLDEN_SWORD;
            case PIGLIN_BRUTE -> Material.GOLDEN_AXE;
            case WITHER_SKELETON -> Material.STONE_SWORD;
            case VEX -> Material.IRON_SWORD;
            default -> null;
        };
    }

    private static List<String> names(List<Material> l, String first) {
        List<String> lb = new ArrayList<>(List.of(first));
        for (Material m : l) lb.add(pretty(m));
        return lb;
    }

    /** Cosmeticos unicos disponibles para esta mascota (vacio si el mob no tiene ninguno). */
    public static List<Extra> extras(PetType t) {
        List<Extra> l = new ArrayList<>();
        EntityType et = t.entity();
        if (HOLD.contains(et)) {
            boolean carrier = et == EntityType.ALLAY || et == EntityType.COPPER_GOLEM;
            l.add(new Extra("hand", carrier ? "Objeto que sostiene" : "Objeto en mano", Material.ITEM_FRAME, names(handItems(), "Normal")));
        }
        if (ARMOR.contains(et)) {
            l.add(new Extra("head", "Casco / cabeza", Material.IRON_HELMET, names(HEADS, "Ninguno")));
            l.add(new Extra("armor", "Armadura", Material.IRON_CHESTPLATE,
                    List.of("Ninguna", "Cuero", "Malla", "Hierro", "Oro", "Diamante", "Netherite")));
        }
        switch (et) {
            case WOLF -> l.add(new Extra("body", "Armadura de lobo", Material.WOLF_ARMOR, List.of("Ninguna", "Armadura de lobo")));
            case LLAMA, TRADER_LLAMA -> {
                List<String> lb = new ArrayList<>(List.of("Ninguna"));
                for (DyeColor c : DyeColor.values()) lb.add("alfombra " + c.name().toLowerCase(java.util.Locale.ROOT).replace('_', ' '));
                l.add(new Extra("body", "Alfombra", Material.WHITE_CARPET, lb));
            }
            case COPPER_GOLEM -> l.add(new Extra("weather", "Oxidación", Material.COPPER_BLOCK,
                    List.of("Nuevo", "Expuesto", "Desgastado", "Oxidado")));
            case SLIME, MAGMA_CUBE -> l.add(new Extra("size", "Tamaño", Material.SLIME_BALL,
                    List.of("Mediano (2)", "Pequeño (1)", "Grande (3)", "Enorme (4)")));
            case PHANTOM -> l.add(new Extra("size", "Tamaño", Material.PHANTOM_MEMBRANE, List.of("Normal", "Mediano", "Grande", "Enorme")));
            case ZOMBIE_VILLAGER -> {
                List<String> pr = new ArrayList<>(List.of("Ninguna"));
                for (Villager.Profession p : professions()) pr.add(p.getKey().getKey().replace('_', ' '));
                l.add(new Extra("profession", "Profesión", Material.EMERALD, pr));
                List<String> ty = new ArrayList<>();
                for (Villager.Type v : biomes()) ty.add(v.getKey().getKey().replace('_', ' '));
                l.add(new Extra("biome", "Bioma", Material.GRASS_BLOCK, ty));
            }
            case BEE -> l.add(new Extra("nectar", "Néctar", Material.HONEYCOMB, List.of("Sin néctar", "Con néctar")));
            case GOAT -> {
                l.add(new Extra("scream", "Voz", Material.GOAT_HORN, List.of("Normal", "Gritona")));
                l.add(new Extra("horns", "Cuernos", Material.BONE, List.of("Ambos", "Sin cuernos", "Solo izquierdo", "Solo derecho")));
            }
            case PANDA -> {
                List<String> lb = new ArrayList<>();
                for (Panda.Gene g : Panda.Gene.values()) lb.add(g.name().toLowerCase(java.util.Locale.ROOT));
                l.add(new Extra("gene", "Personalidad", Material.BAMBOO, lb));
            }
            case SNOW_GOLEM -> l.add(new Extra("pumpkin", "Calabaza", Material.CARVED_PUMPKIN, List.of("Con calabaza", "Sin calabaza")));
            case VEX -> l.add(new Extra("charge", "Modo ataque", Material.IRON_SWORD, List.of("Normal", "Cargando (ojos rojos)")));
            case ALLAY -> l.add(new Extra("dance", "Baile", Material.JUKEBOX, List.of("Quieto", "Bailando")));
            default -> { }
        }
        return l;
    }

    public static void applyExtras(LivingEntity e, PetData d, PetType t) {
        if (e instanceof CopperGolem cg) { // encerado: si se oxida del todo se convierte en estatua y la mascota desapareceria
            try { cg.setOxidizing(CopperGolem.Oxidizing.waxed()); } catch (Throwable ignored) { }
        }
        for (Extra x : extras(t)) {
            int idx = Math.floorMod(d.extra.getOrDefault(x.key(), 0), x.labels().size());
            try {
                applyOne(e, d, t, x.key(), idx);
            } catch (Throwable ex) {
                MascotasX.get().getLogger().fine("Cosmetico " + x.key() + " no aplicable a " + t.id() + ": " + ex.getMessage());
            }
        }
    }

    private static void applyOne(LivingEntity e, PetData d, PetType t, String key, int idx) {
        EntityEquipment eq = e.getEquipment();
        switch (key) {
            case "hand" -> {
                if (eq == null) return;
                ItemStack cu = d.custom.get("hand"); // objeto libre elegido por el jugador
                if (cu != null) eq.setItemInMainHand(cu.clone());
                else if (idx == 0) {
                    Material m = defaultHand(t.entity());
                    if ("arrow".equals(t.attack())) m = t.entity() == EntityType.PILLAGER ? Material.CROSSBOW : Material.BOW;
                    eq.setItemInMainHand(m == null ? new ItemStack(Material.AIR) : new ItemStack(m));
                } else eq.setItemInMainHand(new ItemStack(handItems().get(idx - 1)));
                eq.setItemInMainHandDropChance(0f);
            }
            case "head" -> {
                if (eq == null) return;
                ItemStack cu = d.custom.get("head");
                if (cu != null) eq.setHelmet(cu.clone());
                else eq.setHelmet(idx == 0 ? new ItemStack(Material.AIR) : new ItemStack(HEADS.get(idx - 1)));
                eq.setHelmetDropChance(0f);
            }
            case "armor" -> {
                if (eq == null) return;
                String set = idx == 0 ? null : ARMOR_SETS[idx - 1];
                eq.setChestplate(set == null ? new ItemStack(Material.AIR) : new ItemStack(Material.valueOf(set + "_CHESTPLATE")));
                eq.setLeggings(set == null ? new ItemStack(Material.AIR) : new ItemStack(Material.valueOf(set + "_LEGGINGS")));
                eq.setBoots(set == null ? new ItemStack(Material.AIR) : new ItemStack(Material.valueOf(set + "_BOOTS")));
                eq.setChestplateDropChance(0f);
                eq.setLeggingsDropChance(0f);
                eq.setBootsDropChance(0f);
            }
            case "body" -> {
                if (e instanceof Llama ll) {
                    ll.getInventory().setDecor(idx == 0 ? null : new ItemStack(Material.valueOf(DyeColor.values()[idx - 1].name() + "_CARPET")));
                } else if (eq != null) {
                    eq.setItem(EquipmentSlot.BODY, idx == 0 ? new ItemStack(Material.AIR) : new ItemStack(Material.WOLF_ARMOR));
                    eq.setDropChance(EquipmentSlot.BODY, 0f);
                }
            }
            case "weather" -> {
                if (e instanceof CopperGolem cg) cg.setWeatheringState(WeatheringCopperState.values()[idx]);
            }
            case "size" -> {
                if (e instanceof Slime sl) sl.setSize(SLIME_SIZES[idx]);
                else if (e instanceof Phantom ph) ph.setSize(PHANTOM_SIZES[idx]);
            }
            case "profession" -> {
                if (e instanceof ZombieVillager zv) zv.setVillagerProfession(idx == 0 ? Villager.Profession.NONE : professions().get(idx - 1));
            }
            case "biome" -> {
                if (e instanceof ZombieVillager zv) zv.setVillagerType(biomes().get(idx));
            }
            case "nectar" -> { if (e instanceof Bee b) b.setHasNectar(idx == 1); }
            case "scream" -> { if (e instanceof Goat g) g.setScreaming(idx == 1); }
            case "horns" -> {
                if (e instanceof Goat g) { g.setLeftHorn(idx == 0 || idx == 2); g.setRightHorn(idx == 0 || idx == 3); }
            }
            case "gene" -> {
                if (e instanceof Panda p) { Panda.Gene g = Panda.Gene.values()[idx]; p.setMainGene(g); p.setHiddenGene(g); }
            }
            case "pumpkin" -> { if (e instanceof Snowman sn) sn.setDerp(idx == 1); }
            case "charge" -> { if (e instanceof Vex v) v.setCharging(idx == 1); }
            case "dance" -> {
                if (e instanceof Allay a) { if (idx == 1) a.startDancing(); else a.stopDancing(); }
            }
            default -> { }
        }
    }
}
