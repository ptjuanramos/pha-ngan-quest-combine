package com.kpnquest.listplayercompletions;

import jakarta.inject.Singleton;

import java.util.List;

@Singleton
public class ListPlayerCompletionsService {

    private final PlayerCompletionsRepository repository;

    public ListPlayerCompletionsService(PlayerCompletionsRepository repository) {
        this.repository = repository;
    }

    public List<PlayerCompletionResponse> list(Long playerId) {
        return repository.findByPlayerIdOrderByCompletedAt(playerId).stream()
            .map(c -> new PlayerCompletionResponse(c.missionId(), c.completedAt()))
            .toList();
    }
}