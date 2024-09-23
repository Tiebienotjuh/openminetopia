package nl.openminetopia.api.player.fitness;

import com.craftmend.storm.api.enums.Where;
import nl.openminetopia.api.player.objects.MinetopiaPlayer;
import nl.openminetopia.modules.data.storm.StormDatabase;
import nl.openminetopia.modules.data.storm.models.FitnessBoosterModel;
import nl.openminetopia.modules.data.storm.models.FitnessModel;
import nl.openminetopia.modules.fitness.objects.FitnessBooster;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class FitnessManager {

    private static FitnessManager instance;

    public static FitnessManager getInstance() {
        if (instance == null) {
            instance = new FitnessManager();
        }
        return instance;
    }

    public void setFitness(MinetopiaPlayer player, int amount) {
        StormDatabase.getInstance().updateModel(player, FitnessModel.class, fitnessModel -> fitnessModel.setTotal(amount));
    }

    public CompletableFuture<Integer> getFitnessGainedByDrinking(MinetopiaPlayer player) {
        return StormDatabase.getInstance().getModelData(player, FitnessModel.class, query -> {
        }, model -> true, FitnessModel::getFitnessGainedByDrinking, 0);
    }

    public CompletableFuture<Integer> getFitnessGainedByHealth(MinetopiaPlayer player) {
        return StormDatabase.getInstance().getModelData(player, FitnessModel.class, query -> {
        }, model -> true, FitnessModel::getFitnessGainedByHealth, 0);
    }

    public CompletableFuture<Double> getDrinkingPoints(MinetopiaPlayer player) {
        return StormDatabase.getInstance().getModelData(player, FitnessModel.class, query -> {
        }, model -> true, FitnessModel::getDrinkingPoints, 0.0);
    }

    public void setDrinkingPoints(MinetopiaPlayer player, double amount) {
        StormDatabase.getInstance().updateModel(player, FitnessModel.class, fitnessModel -> fitnessModel.setDrinkingPoints(amount));
    }

    public void setHealthPoints(MinetopiaPlayer player, int amount) {
        StormDatabase.getInstance().updateModel(player, FitnessModel.class, fitnessModel -> fitnessModel.setHealthPoints(amount));
    }

    /**
     * Set fitness gained
     */
    public void setFitnessGainedByHealth(MinetopiaPlayer player, int amount) {
        StormDatabase.getInstance().updateModel(player, FitnessModel.class, fitnessModel -> fitnessModel.setFitnessGainedByHealth(amount));
    }

    public void setFitnessGainedByDrinking(MinetopiaPlayer player, int amount) {
        StormDatabase.getInstance().updateModel(player, FitnessModel.class, fitnessModel -> fitnessModel.setFitnessGainedByDrinking(amount));
    }

    public void setFitnessGainedByClimbing(MinetopiaPlayer player, int amount) {
        StormDatabase.getInstance().updateModel(player, FitnessModel.class, fitnessModel -> fitnessModel.setFitnessGainedByClimbing(amount));
    }

    public void setFitnessGainedBySprinting(MinetopiaPlayer player, int amount) {
        StormDatabase.getInstance().updateModel(player, FitnessModel.class, fitnessModel -> fitnessModel.setFitnessGainedBySprinting(amount));
    }

    public void setFitnessGainedByFlying(MinetopiaPlayer player, int amount) {
        StormDatabase.getInstance().updateModel(player, FitnessModel.class, fitnessModel -> fitnessModel.setFitnessGainedByFlying(amount));
    }

    public void setFitnessGainedBySwimming(MinetopiaPlayer player, int amount) {
        StormDatabase.getInstance().updateModel(player, FitnessModel.class, fitnessModel -> fitnessModel.setFitnessGainedBySwimming(amount));
    }

    public void setFitnessGainedByWalking(MinetopiaPlayer player, int amount) {
        StormDatabase.getInstance().updateModel(player, FitnessModel.class, fitnessModel -> fitnessModel.setFitnessGainedByWalking(amount));
    }

    /**
     * Add fitness booster
     */

    public void addFitnessBooster(MinetopiaPlayer player, FitnessBooster booster) {
        StormDatabase.getExecutorService().submit(() -> {
            try {
                FitnessBoosterModel fitnessBoosterModel = new FitnessBoosterModel();
                fitnessBoosterModel.setUniqueId(player.getUuid());
                fitnessBoosterModel.setFitness(booster.getAmount());
                fitnessBoosterModel.setExpiresAt(booster.getExpiresAt());

                StormDatabase.getInstance().saveStormModel(fitnessBoosterModel);
            } catch (Exception exception) {
                exception.printStackTrace();
            }
        });
    }

    public void removeFitnessBooster(MinetopiaPlayer player, FitnessBooster booster) {
        StormDatabase.getInstance().deleteModel(player, FitnessBoosterModel.class, model -> model.getId() == booster.getId());
    }

    public CompletableFuture<List<FitnessBooster>> getFitnessBoosters(MinetopiaPlayer player) {
        CompletableFuture<List<FitnessBooster>> completableFuture = new CompletableFuture<>();

        findPlayerFitnessBoosts(player).thenAccept(fitnessBoosters -> {
            List<FitnessBooster> prefixes = new ArrayList<>();
            for (FitnessBoosterModel fitnessBoosterModel : fitnessBoosters) {
                prefixes.add(new FitnessBooster(fitnessBoosterModel.getId(), fitnessBoosterModel.getFitness(), fitnessBoosterModel.getExpiresAt()));
            }
            completableFuture.complete(prefixes);
        }).exceptionally(ex -> {
            completableFuture.completeExceptionally(ex);
            return null;
        });

        return completableFuture;
    }

    private CompletableFuture<List<FitnessBoosterModel>> findPlayerFitnessBoosts(MinetopiaPlayer player) {
        CompletableFuture<List<FitnessBoosterModel>> completableFuture = new CompletableFuture<>();
        StormDatabase.getExecutorService().submit(() -> {
            try {
                Collection<FitnessBoosterModel> prefixesModel = StormDatabase.getInstance().getStorm().buildQuery(FitnessBoosterModel.class)
                        .where("uuid", Where.EQUAL, player.getUuid().toString())
                        .execute()
                        .join();

                completableFuture.complete(new ArrayList<>(prefixesModel));
            } catch (Exception exception) {
                exception.printStackTrace();
                completableFuture.completeExceptionally(exception);
            }
        });
        return completableFuture;
    }


}