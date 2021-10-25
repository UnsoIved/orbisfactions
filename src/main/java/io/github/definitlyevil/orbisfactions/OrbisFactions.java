package io.github.definitlyevil.orbisfactions;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import io.github.definitlyevil.orbisfactions.commands.FactionCommand;
import io.github.definitlyevil.orbisfactions.commands.OBFAdminCommand;
import io.github.definitlyevil.orbisfactions.listeners.ChunkListener;
import io.github.definitlyevil.orbisfactions.listeners.ClaimProtectionListener;
import io.github.definitlyevil.orbisfactions.listeners.PlayerProfileInitializerListener;
import io.github.definitlyevil.orbisfactions.tasks.UpdateStandingChunkTask;
import me.clip.placeholderapi.PlaceholderAPI;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public final class OrbisFactions extends JavaPlugin {

    private static OrbisFactions instance;
    public static OrbisFactions getInstance() {
        return instance;
    }

    private Economy economy;

    private ChunkCache chunkCache;

    private HikariDataSource dataSource = null;
    private ExecutorService executors = null;

    @Override
    public void onLoad() {
        instance = this;
    }

    @Override
    public void onEnable() {
        reloadConfig();

        chunkCache = new ChunkCache();

        getLogger().info("Registering listeners... ");
        getServer().getPluginManager().registerEvents(new PlayerProfileInitializerListener(), this);
        getServer().getPluginManager().registerEvents(new ChunkListener(chunkCache.getModifier()), this);
        getServer().getPluginManager().registerEvents(new ClaimProtectionListener(this, chunkCache), this);

        getLogger().info("Starting up tasks... ");
        getServer().getScheduler().runTaskTimer(this, new UpdateStandingChunkTask(), 20L, 20L);

        getLogger().info("Registering into PlaceholderAPI... ");
        PlaceholderAPI.registerPlaceholderHook("obf", new PAPIHook());

        getLogger().info("Getting economy service... ");
        RegisteredServiceProvider<Economy> provider = getServer().getServicesManager().getRegistration(Economy.class);
        if(provider == null) {
            getLogger().severe("Economy plugin not found! ");
            getServer().shutdown();
            return;
        }
        economy = provider.getProvider();

        getLogger().info("Registering commands... ");
        new OBFAdminCommand().setup(getCommand("obfadmin"));
        new FactionCommand().setup(getCommand("obf"));
    }

    @Override
    public void reloadConfig() {
        getLogger().info("Loading configurations... ");
        saveDefaultConfig();
        super.reloadConfig();

        getLogger().info("Loading database connections... ");
        if(dataSource != null) {
            dataSource.close();
            dataSource = null;
        }
        {
            HikariConfig config = new HikariConfig();
            config.setJdbcUrl(getConfig().getString("database.connection"));
            config.setUsername(getConfig().getString("database.username"));
            config.setPassword(getConfig().getString("database.password"));
            config.setMaximumPoolSize(getConfig().getInt("database.optimizations.max-pool-size"));
            config.setMinimumIdle(getConfig().getInt("database.optimizations.min-idle", 4));
            config.setMaxLifetime(getConfig().getInt("database.optimizations.max-pool-size"));
            config.setMaxLifetime(getConfig().getLong("database.optimizations.max-life-time", 10 * 60 * 1000L));
            getLogger().info("Testing MySQL connection... ");
            dataSource = new HikariDataSource(config);
            try {
                dataSource.getConnection().close();
            } catch (Exception ex) {
                getLogger().severe("Database connection failed! ");
                getServer().shutdown();
                return;
            }
            getLogger().info("Database connection success! ");
        }
        if(executors != null) {
            getLogger().info("Stopping old thread pool... ");
            executors.shutdown();
            executors = null;
        }
        {
            int threads = getConfig().getInt("threads");
            getLogger().info(String.format("Starting thread pool with %d threads... ", threads));
            executors = Executors.newFixedThreadPool(threads, r -> new Thread(r, "AsyncTasksOrbisFactions-" + System.currentTimeMillis()));
            getLogger().info("Thread pool started! ");
        }
    }

    public ChunkCache getChunkCache() {
        return chunkCache;
    }

    public void execute(Runnable runnable) {
        executors.execute(runnable);
    }

    public void primary(Runnable runnable) {
        Bukkit.getScheduler().runTaskLater(this, runnable, 1L);
    }

    public Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }

    public Economy getEconomy() {
        return economy;
    }
}
