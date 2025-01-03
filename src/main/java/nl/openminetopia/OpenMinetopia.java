package nl.openminetopia;

import co.aikar.commands.MessageType;
import co.aikar.commands.PaperCommandManager;
import com.jazzkuh.inventorylib.loader.InventoryLoader;
import com.jazzkuh.inventorylib.objects.Menu;
import com.jeff_media.customblockdata.CustomBlockData;
import lombok.Getter;
import lombok.Setter;
import nl.openminetopia.configuration.*;
import nl.openminetopia.modules.ModuleManager;
import nl.openminetopia.modules.banking.BankingModule;
import nl.openminetopia.modules.chat.ChatModule;
import nl.openminetopia.modules.color.ColorModule;
import nl.openminetopia.modules.core.CoreModule;
import nl.openminetopia.modules.data.DataModule;
import nl.openminetopia.modules.detectiongates.DetectionModule;
import nl.openminetopia.modules.fitness.FitnessModule;
import nl.openminetopia.modules.places.PlacesModule;
import nl.openminetopia.modules.player.PlayerModule;
import nl.openminetopia.modules.plots.PlotModule;
import nl.openminetopia.modules.prefix.PrefixModule;
import nl.openminetopia.modules.scoreboard.ScoreboardModule;
import nl.openminetopia.modules.staff.StaffModule;
import nl.openminetopia.modules.teleporter.TeleporterModule;
import nl.openminetopia.utils.ChatUtils;
import nl.openminetopia.utils.placeholderapi.OpenMinetopiaExpansion;
import org.bstats.bukkit.Metrics;
import org.bstats.charts.SimplePie;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.plugin.java.JavaPlugin;

public final class OpenMinetopia extends JavaPlugin {

    @Getter
    private static OpenMinetopia instance;

    @Getter
    private static ModuleManager moduleManager;

    @Getter
    @Setter
    private static PaperCommandManager commandManager;

    @Getter
    @Setter
    private static DefaultConfiguration defaultConfiguration;

    @Getter
    @Setter
    private static MessageConfiguration messageConfiguration;

    @Getter
    @Setter
    private static LevelCheckConfiguration levelcheckConfiguration;

    @Getter
    @Setter
    private static ColorsConfiguration colorsConfiguration;

    @Getter
    @Setter
    private static BankingConfiguration bankingConfiguration;

    @Getter
    @Setter
    private static FitnessConfiguration fitnessConfiguration;

    @Override
    public void onEnable() {
        instance = this;

        commandManager = new PaperCommandManager(this);
        moduleManager = new ModuleManager();

        CustomBlockData.registerListener(this);

        defaultConfiguration = new DefaultConfiguration(getDataFolder());
        defaultConfiguration.saveConfiguration();

        if (defaultConfiguration.isMetricsEnabled()) {
            Metrics metrics = new Metrics(this, 23547);
            metrics.addCustomChart(new SimplePie("storage", () -> defaultConfiguration.getDatabaseType().toString()));
        }

        messageConfiguration = new MessageConfiguration(getDataFolder());
        messageConfiguration.saveConfiguration();

        levelcheckConfiguration = new LevelCheckConfiguration(getDataFolder());
        levelcheckConfiguration.saveConfiguration();

        colorsConfiguration = new ColorsConfiguration(getDataFolder());
        colorsConfiguration.saveConfiguration();

        bankingConfiguration = new BankingConfiguration(getDataFolder());
        bankingConfiguration.saveConfiguration();

        fitnessConfiguration = new FitnessConfiguration(getDataFolder());
        fitnessConfiguration.saveConfiguration();

        moduleManager.register(
                new CoreModule(),
                new DataModule(),
                new BankingModule(),
                new PlayerModule(),
                new FitnessModule(),
                new StaffModule(),
                new PrefixModule(),
                new ChatModule(),
                new ColorModule(),
                new PlacesModule(),
                new ScoreboardModule(),
                new PlotModule(),
                new TeleporterModule(),
                new DetectionModule()
        );

        commandManager.enableUnstableAPI("help");
        commandManager.setFormat(MessageType.HELP, 1, ChatColor.GOLD);
        commandManager.setFormat(MessageType.HELP, 2, ChatColor.YELLOW);
        commandManager.setFormat(MessageType.HELP, 3, ChatColor.GRAY);

        Menu.init(this);
        InventoryLoader.setFormattingProvider(message -> ChatUtils.color("<red>" + message));

        if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            new OpenMinetopiaExpansion().register();
            getLogger().info("Registered PlaceholderAPI expansion.");
        }
    }

    @Override
    public void onDisable() {
        moduleManager.disable();
    }
}