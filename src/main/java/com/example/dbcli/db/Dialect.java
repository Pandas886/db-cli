package com.example.dbcli.db;

import com.example.dbcli.config.DataSourceConfig;

public interface Dialect {
    String ddlSql(DataSourceConfig source, String table);

    default String listTablesSql(DataSourceConfig source) {
        return "";
    }

    default String normalizeTable(String table) {
        return table;
    }

    default String normalizeSchema(String schema) {
        return schema;
    }

    default String normalizeDatabase(String database) {
        return database;
    }
}
