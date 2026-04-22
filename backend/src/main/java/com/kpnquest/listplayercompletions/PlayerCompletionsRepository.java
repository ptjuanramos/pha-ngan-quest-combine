package com.kpnquest.listplayercompletions;

import io.micronaut.data.jdbc.annotation.JdbcRepository;
import io.micronaut.data.model.query.builder.sql.Dialect;
import io.micronaut.data.repository.GenericRepository;

import java.util.List;

@JdbcRepository(dialect = Dialect.SQL_SERVER)
public interface PlayerCompletionsRepository extends GenericRepository<PlayerCompletion, Long> {
    List<PlayerCompletion> findByPlayerIdOrderByCompletedAt(Long playerId);
}