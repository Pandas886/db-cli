package com.example.dbcli.db;

import com.example.dbcli.config.DataSourceConfig;

public class DmDialect implements Dialect {
    @Override
    public String ddlSql(DataSourceConfig source, String table) {
        String schema = source.getSchema();
        if (schema == null || schema.isBlank()) {
            schema = source.getUsername();
        }
        String t = IdentifierUtil.trimIdentifier(table).toUpperCase();
        String s = schema == null ? "" : schema.trim().toUpperCase();

        if (!s.isBlank()) {
            return "SELECT DBMS_METADATA.GET_DDL('TABLE', '" + IdentifierUtil.safeLiteral(t) + "', '" + IdentifierUtil.safeLiteral(s) + "') AS DDL FROM DUAL";
        }
        return "SELECT DBMS_METADATA.GET_DDL('TABLE', '" + IdentifierUtil.safeLiteral(t) + "') AS DDL FROM DUAL";
    }

    @Override
    public String listTablesSql(DataSourceConfig source) {
        String owner = source.getSchema();
        if (owner == null || owner.isBlank()) {
            owner = source.getUsername();
        }
        if (owner == null || owner.isBlank()) {
            return "";
        }
        return "SELECT TABLE_NAME FROM ALL_TABLES WHERE OWNER = '" + IdentifierUtil.safeLiteral(owner.toUpperCase()) + "' ORDER BY TABLE_NAME";
    }
}
