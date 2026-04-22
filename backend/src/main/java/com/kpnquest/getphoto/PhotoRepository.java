package com.kpnquest.getphoto;

import com.kpnquest.shared.domain.Photo;
import io.micronaut.data.jdbc.annotation.JdbcRepository;
import io.micronaut.data.model.query.builder.sql.Dialect;
import io.micronaut.data.repository.GenericRepository;

import java.util.Optional;

@JdbcRepository(dialect = Dialect.SQL_SERVER)
public interface PhotoRepository extends GenericRepository<Photo, Long> {
    Optional<Photo> findByPlayerIdAndMissionId(Long playerId, Integer missionId);
}