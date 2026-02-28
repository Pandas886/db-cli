package com.example.dbcli.db;

import com.example.dbcli.config.DataSourceConfig;

public class PostgresFamilyDialect implements Dialect {
    @Override
    public String ddlSql(DataSourceConfig source, String table) {
        // Keep empty to avoid pg extension/version dependency; metadata fallback remains portable.
        return "";
    }

    @Override
    public String listTablesSql(DataSourceConfig source) {
        String schema = source.getSchema();
        if (schema == null || schema.isBlank()) {
            schema = "public";
        }
        return "SELECT tablename FROM pg_catalog.pg_tables WHERE schemaname = '" + IdentifierUtil.safeLiteral(schema) + "'";
    }

    @Override
    public String normalizeSchema(String schema) {
        if (schema == null || schema.isBlank()) {
            return "public";
        }
        return schema;
    }
}
