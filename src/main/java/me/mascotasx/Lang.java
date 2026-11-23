package me.mascotasx;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * Textos del plugin (lang.yml). Todo lo que ve el jugador sale de aqui.
 * Las claves que falten se agregan solas desde el jar sin tocar lo que ya editaste.
 * Placeholders: pares %clave% -> valor. Un valor puede traer "\n" para expandirse en varias lineas.
 */
public class Lang {
    private final MascotasX plugin;
    private final File file;
    private YamlConfiguration cfg = new YamlConfiguration();
    private YamlConfiguration defaults = new YamlConfiguration();
    private boolean dirty;

    public Lang(MascotasX plugin) {
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), "lang.yml");
    }

    public void load() {
        boolean fresh = !file.exists();
        if (fresh) plugin.saveResource("lang.yml", false);
        cfg = YamlConfiguration.loadConfiguration(file);
        try (InputStreamReader r = new InputStreamReader(plugin.getResource("lang.yml"), StandardCharsets.UTF_8)) {
            defaults = YamlConfiguration.loadConfiguration(r);
        } catch (Exception ex) {
            plugin.getLogger().warning("No se pudo leer lang.yml del jar: " + ex.getMessage());
        }
        boolean changed = false;
        if (fresh) changed |= migrateOld();
        for (String k : defaults.getKeys(true)) {
            if (defaults.isConfigurationSection(k)) continue;
            if (!cfg.contains(k)) { cfg.set(k, defaults.get(k)); changed = true; }
        }
        if (changed) save();
    }

    /** Copia los textos que el admin ya habia personalizado en config.yml (messages.* y titles.*). */
    private boolean migrateOld() {
        boolean d = false;
        for (String sec : new String[]{"messages", "titles"}) {
            ConfigurationSection cs = plugin.getConfig().getConfigurationSection(sec);
            if (cs == null) continue;
            for (String k : cs.getKeys(false)) {
                String v = cs.getString(k);
                if (v != null && defaults.contains(sec + "." + k)) { cfg.set(sec + "." + k, v); d = true; }
            }
        }
        return d;
    }

    public void flush() { if (dirty) save(); }

    public void save() {
        dirty = false;
        try { cfg.save(file); } catch (IOException ex) { plugin.getLogger().warning("No se pudo guardar lang.yml: " + ex.getMessage()); }
    }

    // ------------------------------------------------------------ lectura
    public String raw(String key) { return cfg.getString(key, key); }

    private static String rep(String s, String[] kv) {
        for (int i = 0; i + 1 < kv.length; i += 2) s = s.replace(kv[i], kv[i + 1] == null ? "" : kv[i + 1]);
        return s;
    }

    public String s(String key, String... kv) { return rep(raw(key), kv); }

    /** Lista de lineas con placeholders; una linea con placeholder que queda vacia se omite. */
    public List<String> l(String key, String... kv) {
        List<String> out = new ArrayList<>();
        List<String> src = cfg.isList(key) ? cfg.getStringList(key) : new ArrayList<>();
        for (String line : src) {
            String r = rep(line, kv);
            if (r.isBlank() && !line.isBlank()) continue;
            for (String part : r.split("\n", -1)) out.add(part);
        }
        return out;
    }

    /** Objeto de menu: nombre = key.name, lore = key.lore */
    public ItemStack item(Material m, String key, String... kv) { return itemKeys(m, key + ".name", key + ".lore", kv); }

    /** Igual que item(), pero con claves de nombre y lore distintas (nombre distinto para no chocar con item(Material, String, String...)). */
    public ItemStack itemKeys(Material m, String nameKey, String loreKey, String... kv) {
        return Items.of(m, 1, s(nameKey, kv), l(loreKey, kv));
    }

    public ItemStack item(Material m, int amount, String key, String... kv) {
        return Items.of(m, amount, s(key + ".name", kv), l(key + ".lore", kv));
    }

    // ------------------------------------------------------------ escritura
    public void set(String key, String value) { cfg.set(key, value); save(); }

    public void reset(String key) {
        cfg.set(key, defaults.get(key));
        save();
    }

    public boolean hasDefault(String key) { return defaults.contains(key); }

    public List<String> keys(String section) {
        ConfigurationSection cs = cfg.getConfigurationSection(section);
        return cs == null ? new ArrayList<>() : new ArrayList<>(cs.getKeys(false));
    }

    // ------------------------------------------------------------ claves dinamicas (cosmeticos unicos)
    /** Devuelve el texto de key; si no existe lo crea con def. */
    public String auto(String key, String def) {
        if (!cfg.contains(key)) { cfg.set(key, def); dirty = true; }
        return cfg.getString(key, def);
    }

    /** Lista editable: si no existe se crea; si el admin cambio el numero de elementos se ignora y se usa def. */
    public List<String> autoList(String key, List<String> def) {
        if (!cfg.contains(key)) { cfg.set(key, new ArrayList<>(def)); dirty = true; }
        List<String> v = cfg.getStringList(key);
        return v.size() == def.size() ? v : def;
    }
}
