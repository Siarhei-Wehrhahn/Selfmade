package de.jackson.creatoros.service;

import de.jackson.creatoros.domain.Season;
import de.jackson.creatoros.storage.SeasonStorage;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class SeasonService {

    private final SeasonStorage storage;
    private final List<Season> seasons = new ArrayList<>();

    public SeasonService(SeasonStorage storage) {
        this.storage = storage;
    }

    public void loadSeasons() {
        seasons.clear();
        seasons.addAll(storage.loadSeasons());
    }

    public void saveSeasons() {
        storage.saveSeasons(seasons);
    }

    public List<Season> getSeasons() {
        return Collections.unmodifiableList(seasons);
    }

    public Optional<Season> getSeasonById(String id) {
        return seasons.stream()
                .filter(s -> s.getId().equals(id))
                .findFirst();
    }

    public void addSeason(Season season) {
        seasons.add(season);
    }

    public void removeSeason(String id) {
        seasons.removeIf(s -> s.getId().equals(id));
    }
}
