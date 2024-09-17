package nl.openminetopia.api.player.objects;

import lombok.Getter;
import lombok.Setter;
import net.megavex.scoreboardlibrary.api.sidebar.Sidebar;
import nl.openminetopia.OpenMinetopia;
import nl.openminetopia.api.player.*;
import nl.openminetopia.configuration.DefaultConfiguration;
import nl.openminetopia.modules.data.storm.StormDatabase;
import nl.openminetopia.modules.data.storm.models.PlayerModel;
import nl.openminetopia.modules.fitness.objects.FitnessBooster;
import nl.openminetopia.modules.fitness.runnables.FitnessRunnable;
import nl.openminetopia.modules.player.runnables.PlaytimeRunnable;
import nl.openminetopia.modules.prefix.objects.Prefix;
import nl.openminetopia.modules.color.objects.PrefixColor;
import nl.openminetopia.utils.ChatUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.UUID;

@Getter
public class OnlineMinetopiaPlayer implements MinetopiaPlayer {

    private final UUID uuid;
    private final PlayerModel playerModel;

    private int playtime;
    private PlaytimeRunnable playtimeRunnable;

    private int level;

    private List<Prefix> prefixes;
    private Prefix activePrefix;

    private List<PrefixColor> prefixColors;
    private PrefixColor activePrefixColor;

    private int fitness;
    private double drinkingPoints;
    private int healthPoints;
    private @Setter long lastDrinkingTime;

    private int fitnessGainedByHealth;
    private int fitnessGainedByDrinking;
    private int fitnessGainedByClimbing;
    private int fitnessGainedByWalking;
    private int fitnessGainedBySprinting;
    private int fitnessGainedBySwimming;
    private int fitnessGainedByFlying;

    private List<FitnessBooster> fitnessBoosters;

    private FitnessRunnable fitnessRunnable;

    private final DefaultConfiguration configuration = OpenMinetopia.getDefaultConfiguration();

    public OnlineMinetopiaPlayer(UUID uuid, PlayerModel playerModel) {
        this.uuid = uuid;
        this.playerModel = playerModel;
    }

    public void load() {
        try {
            this.level = playerModel.getLevel();
            this.activePrefix = PrefixManager.getInstance().getPlayerActivePrefix(this).get();
            this.activePrefixColor = ColorManager.getInstance().getPlayerActivePrefixColor(this).get();
            this.prefixes = PrefixManager.getInstance().getPrefixes(this).get();
            this.prefixColors = ColorManager.getInstance().getPrefixColors(this).get();
            this.fitnessRunnable = new FitnessRunnable(getBukkit());
            this.drinkingPoints = FitnessManager.getInstance().getDrinkingPoints(this).get();
            this.fitnessGainedByDrinking = FitnessManager.getInstance().getFitnessGainedByDrinking(this).get();
            this.fitnessBoosters = FitnessManager.getInstance().getFitnessBoosters(this).get();
            this.playtimeRunnable = new PlaytimeRunnable(getBukkit());
            this.playtime = PlayerManager.getInstance().getPlaytime(this).get();
        } catch (Exception exception) {
            getBukkit().kick(ChatUtils.color("<red>Er is een fout opgetreden bij het laden van je gegevens. Probeer het later opnieuw."));
            exception.printStackTrace();
        }
        fitnessRunnable.runTaskTimer(OpenMinetopia.getInstance(), 0, 60 * 20L);
        playtimeRunnable.runTaskTimer(OpenMinetopia.getInstance(), 0, 20L);
    }

    public void save() {
        StormDatabase.getInstance().updateModel(this, PlayerModel.class, playerModel -> {
            playerModel.setLevel(level);
            playerModel.setActivePrefixId(activePrefix.getId());
            playerModel.setActivePrefixColorId(activePrefixColor.getId());
            playerModel.setPlaytime(playtime);
        });
    }

    @Override
    public Player getBukkit() {
        return Bukkit.getPlayer(uuid);
    }

    /* Playtime */

    /**
     * Sets the playtime in seconds
     *
     * @param seconds        The amount of seconds
     * @param updateDatabase If true, the playtime will be pushed to the database, otherwise it will only be set in the object
     *                       This should be set to false by default, and only set to true when the player logs out to prevent unnecessary database calls
     */
    @Override
    public void setPlaytime(int seconds, boolean updateDatabase) {
        this.playtime = seconds;
    }

    /* Level */

    @Override
    public void setLevel(int level) {
        this.level = level;
        LevelManager.getInstance().setLevel(this, level);
    }

    /* Prefix */

    @Override
    public void addPrefix(Prefix prefix) {
        prefixes.add(prefix);
        PrefixManager.getInstance().addPrefix(this, prefix);
    }

    @Override
    public void removePrefix(Prefix prefix) {
        prefixes.remove(prefix);
        PrefixManager.getInstance().removePrefix(this, prefix);
    }

    @Override
    public void setActivePrefix(Prefix prefix) {
        this.activePrefix = prefix;
        PrefixManager.getInstance().setActivePrefixId(this, prefix.getId());
    }

    @Override
    public Prefix getActivePrefix() {
        if (activePrefix == null) {
            activePrefix = new Prefix(-1, configuration.getDefaultPrefix(), -1);
        }

        if (activePrefix.getExpiresAt() < System.currentTimeMillis() && activePrefix.getExpiresAt() != -1) {
            getBukkit().sendMessage(ChatUtils.color("<red>Je prefix <dark_red>" + activePrefix + " is verlopen!"));
            removePrefix(activePrefix);
            setActivePrefix(new Prefix(-1, configuration.getDefaultPrefix(), -1));
        }

        return activePrefix;
    }

    @Override
    public void addPrefixColor(PrefixColor color) {
        prefixColors.add(color);
        ColorManager.getInstance().addPrefixColor(this, color);
    }

    @Override
    public void removePrefixColor(PrefixColor color) {
        prefixColors.remove(color);
        ColorManager.getInstance().removePrefixColor(this, color);
    }

    @Override
    public void setActivePrefixColor(PrefixColor color) {
        this.activePrefixColor = color;
        ColorManager.getInstance().setActivePrefixColor(this, color);
    }

    @Override
    public PrefixColor getActivePrefixColor() {
        if (activePrefixColor == null || activePrefixColor.getId() == 0) {
            activePrefixColor = new PrefixColor(-1, "<gray>", -1);
        }

        if (activePrefixColor.getExpiresAt() < System.currentTimeMillis() && activePrefixColor.getExpiresAt() != -1) {
            getBukkit().sendMessage(ChatUtils.color("<red>Je prefix kleur <dark_red>" + activePrefixColor.getColor() + " is verlopen!"));
            removePrefixColor(activePrefixColor);
            setActivePrefixColor(new PrefixColor(-1, "<gray>", -1));
        }

        return activePrefixColor;
    }

    /* Fitness */

    @Override
    public void setFitness(int amount) {
        this.fitness = amount;
        FitnessManager.getInstance().setFitness(this, amount);
    }

    @Override
    public void setFitnessGainedByHealth(int amount) {
        this.fitnessGainedByHealth = amount;
        FitnessManager.getInstance().setFitnessGainedByHealth(this, amount);
    }

    @Override
    public void setHealthPoints(int points) {
        this.healthPoints = points;
        if (healthPoints >= 750 && fitnessGainedByHealth < configuration.getMaxFitnessByHealth()) {
            this.setFitnessGainedByHealth(fitnessGainedByHealth + 1);
            this.healthPoints = 0;
            FitnessManager.getInstance().setHealthPoints(this, 0);
            return;
        }
        FitnessManager.getInstance().setHealthPoints(this, points);
    }

    @Override
    public void setFitnessGainedByDrinking(int amount) {
        this.fitnessGainedByDrinking = amount;
        FitnessManager.getInstance().setFitnessGainedByDrinking(this, amount);
    }

    @Override
    public void setDrinkingPoints(double points) {
        this.drinkingPoints = points;
        if (drinkingPoints >= configuration.getDrinkingPointsPerFitnessPoint() && fitnessGainedByDrinking < configuration.getMaxFitnessByDrinking()) {
            this.setFitnessGainedByDrinking(fitnessGainedByDrinking + 1);
            this.setFitness(fitness + 1);
            this.drinkingPoints = 0;
            FitnessManager.getInstance().setDrinkingPoints(this, 0);
            return;
        }
        FitnessManager.getInstance().setDrinkingPoints(this, points);
    }

    @Override
    public void setFitnessGainedByClimbing(int points) {
        this.fitnessGainedByClimbing = points;
        FitnessManager.getInstance().setFitnessGainedByClimbing(this, points);
    }

    @Override
    public void setFitnessGainedByWalking(int points) {
        this.fitnessGainedByWalking = points;
        FitnessManager.getInstance().setFitnessGainedByWalking(this, points);
    }

    @Override
    public void setFitnessGainedBySprinting(int points) {
        this.fitnessGainedBySprinting = points;
        FitnessManager.getInstance().setFitnessGainedBySprinting(this, points);
    }

    @Override
    public void setFitnessGainedBySwimming(int points) {
        this.fitnessGainedBySwimming = points;
        FitnessManager.getInstance().setFitnessGainedBySwimming(this, points);
    }

    @Override
    public void setFitnessGainedByFlying(int points) {
        this.fitnessGainedByFlying = points;
        FitnessManager.getInstance().setFitnessGainedByFlying(this, points);
    }

    /* Fitness Boosters */

    public void addFitnessBooster(FitnessBooster booster) {
        fitnessBoosters.add(booster);
        FitnessManager.getInstance().addFitnessBooster(this, booster);
    }

    public void removeFitnessBooster(FitnessBooster booster) {
        fitnessBoosters.remove(booster);
        FitnessManager.getInstance().removeFitnessBooster(this, booster);
    }
}