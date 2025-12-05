package de.jackson.creatoros.service;

import de.jackson.creatoros.domain.PlayerProgress;
import de.jackson.creatoros.storage.PlayerProgressStorage;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PlayerProgressService {

    private final PlayerProgressStorage storage;
    private final Map<UUID, PlayerProgress> players = new HashMap<>();

    public PlayerProgressService(PlayerProgressStorage storage) {
        this.storage = storage;
    }

    public void load() {
        players.clear();
        players.putAll(storage.loadAll());
    }

    public void save() {
        storage.saveAll(players);
    }

    public PlayerProgress getOrCreate(UUID uuid) {
        return players.computeIfAbsent(uuid, PlayerProgress::new);
    }

    public Map<UUID, PlayerProgress> getAll() {
        return Collections.unmodifiableMap(players);
    }
}
