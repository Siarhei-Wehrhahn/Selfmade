package de.jackson.creatoros.domain;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Quest {

    private final String id;
    private String name;
    private String description;
    private QuestType type;

    // simpler Zielwert, z.B. 64 Items sammeln oder 10 Mobs töten
    private int targetAmount;

    // parameter je nach Typ
    private String targetId; // z.B. "DIAMOND", "ZOMBIE", "PLAINS", etc.

    private boolean repeatable;

    private final List<Reward> rewards = new ArrayList<>();

    public Quest(String name, String description, QuestType type) {
        this(UUID.randomUUID().toString(), name, description, type);
    }

    public Quest(String id, String name, String description, QuestType type) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.type = type;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public QuestType getType() {
        return type;
    }

    public void setType(QuestType type) {
        this.type = type;
    }

    public int getTargetAmount() {
        return targetAmount;
    }

    public void setTargetAmount(int targetAmount) {
        this.targetAmount = targetAmount;
    }

    public String getTargetId() {
        return targetId;
    }

    public void setTargetId(String targetId) {
        this.targetId = targetId;
    }

    public boolean isRepeatable() {
        return repeatable;
    }

    public void setRepeatable(boolean repeatable) {
        this.repeatable = repeatable;
    }

    public List<Reward> getRewards() {
        return rewards;
    }
}
