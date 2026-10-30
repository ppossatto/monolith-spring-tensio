package com.ppossatto.tensio;

import org.springframework.jdbc.core.JdbcTemplate;

/// Facts about the `db/test` fixture (`V1001__test_data.sql`) used by the assertions.
public final class TestData {

  public static final String ADMIN = "test@tensio.local";
  public static final String OPERATOR = "operator@tensio.local";
  public static final String INACTIVE = "inactive@tensio.local";
  public static final String PASSWORD = "secret";

  /// Non-decommissioned transformers (TT-01..TT-11).
  public static final long ACTIVE_TRANSFORMERS = 11;
  /// Analyses of all transformers, decommissioned included (12 x 2).
  public static final long ANALYSES = 24;

  private TestData() {
  }

  /// Id of a transformer by tag.
  public static long transformerId(JdbcTemplate jdbc, String tag) {
    return jdbc.queryForObject("select id from transformer where tag = ?", Long.class, tag);
  }

  /// Id of a substation by acronym.
  public static long substationId(JdbcTemplate jdbc, String acronym) {
    return jdbc.queryForObject("select id from substation where acronym = ?", Long.class, acronym);
  }

  /// Id of the latest analysis (10 days ago) of a transformer.
  public static long latestAnalysisId(JdbcTemplate jdbc, String tag) {
    return jdbc.queryForObject("""
       select a.id from analysis a join transformer t on t.id = a.transformer_id
       where t.tag = ? order by a.sample_date desc limit 1""", Long.class, tag);
  }

  /// Id of a user by username.
  public static long userId(JdbcTemplate jdbc, String username) {
    return jdbc.queryForObject("select id from app_user where username = ?", Long.class, username);
  }
}
