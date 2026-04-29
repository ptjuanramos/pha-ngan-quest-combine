package com.kpnquest.identifyplayer;

import io.micronaut.data.annotation.Query;
import io.micronaut.data.jdbc.annotation.JdbcRepository;
import io.micronaut.data.model.query.builder.sql.Dialect;
import io.micronaut.data.repository.CrudRepository;

import java.util.List;
import java.util.Optional;

@JdbcRepository(dialect = Dialect.SQL_SERVER)
public interface PlayerRepository extends CrudRepository<Player, Long> {

    @Query("SELECT * FROM players WHERE is_admin = 0")
    List<Player> findNonAdminPlayers();

    Optional<Player> findByUsername(String username);
}
