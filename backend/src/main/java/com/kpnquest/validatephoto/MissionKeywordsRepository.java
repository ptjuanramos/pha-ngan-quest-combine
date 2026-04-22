package com.kpnquest.validatephoto;

import io.micronaut.data.jdbc.annotation.JdbcRepository;
import io.micronaut.data.model.query.builder.sql.Dialect;
import io.micronaut.data.repository.GenericRepository;

import java.util.Optional;

@JdbcRepository(dialect = Dialect.SQL_SERVER)
public interface MissionKeywordsRepository extends GenericRepository<MissionKeywords, Integer> {
    Optional<MissionKeywords> findById(Integer id);
}