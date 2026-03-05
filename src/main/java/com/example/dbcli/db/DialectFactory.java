package com.example.dbcli.db;

import com.example.dbcli.config.DataSourceConfig;

public final class DialectFactory {
    private DialectFactory() {
    }

    public static Dialect forSource(DataSourceConfig source) {
        String d = source.getDialect();
        if (d == null || d.isBlank()) {
            return new GenericDialect();
        }

        return switch (d.toLowerCase()) {
            case "mysql", "mariadb", "tidb", "oceanbase-mysql", "gbase8a", "gbase-mysql" -> new MySqlDialect();
            case "postgres", "postgresql", "supabase", "gbase8c" -> new PostgresFamilyDialect();
            case "clickhouse" -> new ClickHouseDialect();
            case "dm", "dameng" -> new DmDialect();
            case "sqlite", "sqlite3" -> new SqliteDialect();
            case "snowflake" -> new SnowflakeDialect();
            case "sqlserver", "mssql" -> new SqlServerDialect();
            case "oracle", "oracledb" -> new OracleDialect();
            case "doris", "starrocks" -> new DorisLikeDialect();
            case "kingbase", "kingbase8", "kingbasees" -> new KingBaseDialect();
            default -> new GenericDialect();
        };
    }
}
