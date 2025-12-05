package de.jackson.creatoros.domain;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class PlayerProgress {

    private final UUID playerId;
    private final Map<String, Integer> questProgress = new HashMap<>();
    private final Set<String> completedQuests = new HashSet<>();

    public PlayerProgress(UUID playerId) {
        this.playerId = playerId;
    }

    public UUID getPlayerId() {
        return playerId;
    }

    public int getProgress(String questId) {
        return questProgress.getOrDefault(questId, 0);
    }

    public void setProgress(String questId, int value) {
        questProgress.put(questId, value);
    }

    public Map<String, Integer> getAllProgress() {
        return questProgress;
    }

    public Set<String> getCompletedQuests() {
        return completedQuests;
    }

    public boolean isCompleted(String questId) {
        return completedQuests.contains(questId);
    }

    public void markCompleted(String questId) {
        completedQuests.add(questId);
    }
}
