package me.mascotasx;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;

public class MascotasX extends JavaPlugin {
    private static MascotasX instance;
    private PetManager manager;
    private EconomyHook economy;
    private PetListener listener;
    private Lang lang;

    public static MascotasX get() { return instance; }
    public PetManager manager() { return manager; }
    public EconomyHook economy() { return economy; }
    public PetListener listener() { return listener; }
    public Lang lang() { return lang; }

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();
        lang = new Lang(this);
        lang.load();
        if (!new File(getDataFolder(), "pets.yml").exists()) saveResource("pets.yml", false);

        setupEconomy();

        manager = new PetManager(this);
        manager.loadTypes();
        registerExtras();
        manager.startTasks();

        listener = new PetListener(this);
        getServer().getPluginManager().registerEvents(listener, this);
        getServer().getPluginManager().registerEvents(new MenuListener(), this);

        PetCommand cmd = new PetCommand(this);
        getCommand("mascotas").setExecutor(cmd);
        getCommand("mascotas").setTabCompleter(cmd);

        for (Player p : Bukkit.getOnlinePlayers()) listener.handleJoin(p);
    }

    @Override
    public void onDisable() {
        if (manager != null) manager.shutdown();
    }

    /** (Re)enlaza Vault segun config.yml (economy.enabled). */
    public void setupEconomy() {
        economy = null;
        if (getConfig().getBoolean("economy.enabled", true) && getServer().getPluginManager().getPlugin("Vault") != null) {
            try {
                VaultHook hook = new VaultHook();
                if (hook.setup(this)) economy = hook;
            } catch (Throwable t) {
                getLogger().warning("No se pudo enlazar con Vault: " + t.getMessage());
            }
        }
        if (economy == null) getLogger().warning("Sin economia (Vault): las mascotas se entregaran gratis.");
    }

    public void reload() {
        reloadConfig();
        lang.load();
        setupEconomy();
        manager.loadTypes();
        registerExtras();
    }

    /** Escribe en lang.yml los nombres y opciones de los cosmeticos unicos de cada mob para poder editarlos. */
    private void registerExtras() {
        try {
            for (PetType t : manager.types())
                for (Cosmetics.Extra x : Cosmetics.extras(t)) {
                    String base = "extras." + t.entity().name() + "." + x.key();
                    lang.auto(base + ".name", x.name());
                    lang.autoList(base + ".labels", x.labels());
                }
            lang.flush();
        } catch (Throwable ex) {
            getLogger().warning("No se pudieron registrar los cosmeticos en lang.yml: " + ex.getMessage());
        }
    }

    /** Icono personalizable (icons.<key> en config.yml, editable desde /mascotas admin). */
    public org.bukkit.Material icon(String key, org.bukkit.Material def) {
        String v = getConfig().getString("icons." + key);
        org.bukkit.Material m = v == null ? null : org.bukkit.Material.matchMaterial(v);
        return m == null || !m.isItem() ? def : m;
    }

    /** Envia un mensaje de lang.yml (messages.<key>) reemplazando pares %clave% -> valor. */
    public void msg(CommandSender to, String key, String... kv) {
        to.sendMessage(Text.c(lang.s("messages.prefix") + lang.s("messages." + key, kv)));
    }
}
