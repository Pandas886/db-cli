package com.example.dbcli.db;

import com.example.dbcli.config.DataSourceConfig;

public class KingBaseDialect implements Dialect {
    @Override
    public String ddlSql(DataSourceConfig source, String table) {
        String schema = source.getSchema();
        if (schema == null || schema.isBlank()) {
            schema = "public";
        }
        String schemaLit = IdentifierUtil.safeLiteral(schema);
        String tableLit = IdentifierUtil.safeLiteral(IdentifierUtil.trimIdentifier(table));

        return "SELECT DBMS_METADATA.GET_DDL('TABLE', '" + tableLit + "', '" + schemaLit + "')";
    }

    @Override
    public String listTablesSql(DataSourceConfig source) {
        String schema = source.getSchema();
        if (schema == null || schema.isBlank()) {
            schema = "public";
        }
        return "SELECT table_name FROM information_schema.tables WHERE table_schema = '" + IdentifierUtil.safeLiteral(schema) + "' ORDER BY table_name";
    }

    @Override
    public String normalizeSchema(String schema) {
        if (schema == null || schema.isBlank()) {
            return "public";
        }
        return schema;
    }
}
