package de.jackson.creatoros.domain;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Season {

    private final String id;
    private String name;
    private String description;

    // ISO Strings, z.B. "2025-12-01"
    private String startDate;
    private String endDate;

    private SeasonStatus status;
    private final List<Quest> quests = new ArrayList<>();

    public Season(String name, String description, String startDate, String endDate) {
        this(UUID.randomUUID().toString(), name, description, startDate, endDate, SeasonStatus.PLANNED);
    }

    public Season(String id,
                  String name,
                  String description,
                  String startDate,
                  String endDate,
                  SeasonStatus status) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.startDate = startDate;
        this.endDate = endDate;
        this.status = status;
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

    public String getStartDate() {
        return startDate;
    }

    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }

    public String getEndDate() {
        return endDate;
    }

    public void setEndDate(String endDate) {
        this.endDate = endDate;
    }

    public SeasonStatus getStatus() {
        return status;
    }

    public void setStatus(SeasonStatus status) {
        this.status = status;
    }

    public List<Quest> getQuests() {
        return quests;
    }
}
