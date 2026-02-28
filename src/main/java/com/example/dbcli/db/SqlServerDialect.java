package com.example.dbcli.db;

import com.example.dbcli.config.DataSourceConfig;

public class SqlServerDialect implements Dialect {
    @Override
    public String ddlSql(DataSourceConfig source, String table) {
        // SQL Server table DDL extraction is non-trivial; use metadata fallback for stable behavior.
        return "";
    }

    @Override
    public String listTablesSql(DataSourceConfig source) {
        String schema = source.getSchema();
        if (schema == null || schema.isBlank()) {
            schema = "dbo";
        }
        return "SELECT TABLE_NAME FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_TYPE IN ('BASE TABLE','VIEW') AND TABLE_SCHEMA = '" + IdentifierUtil.safeLiteral(schema) + "' ORDER BY TABLE_NAME";
    }

    @Override
    public String normalizeSchema(String schema) {
        if (schema == null || schema.isBlank()) {
            return "dbo";
        }
        return schema;
    }
}
