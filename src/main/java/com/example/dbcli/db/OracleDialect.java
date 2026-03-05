package com.example.dbcli.db;

import com.example.dbcli.config.DataSourceConfig;

public class OracleDialect implements Dialect {
    @Override
    public String ddlSql(DataSourceConfig source, String table) {
        String owner = resolveOwner(source);
        if (owner.isBlank()) {
            return "";
        }
        String t = IdentifierUtil.trimIdentifier(table).toUpperCase();
        return "SELECT DBMS_METADATA.GET_DDL('TABLE', '" + IdentifierUtil.safeLiteral(t) + "', '" + IdentifierUtil.safeLiteral(owner) + "') AS DDL FROM DUAL";
    }

    @Override
    public String listTablesSql(DataSourceConfig source) {
        String owner = resolveOwner(source);
        if (owner.isBlank()) {
            return "";
        }
        return "SELECT OBJECT_NAME FROM ALL_OBJECTS WHERE OWNER = '" + IdentifierUtil.safeLiteral(owner) + "' AND OBJECT_TYPE IN ('TABLE','VIEW') ORDER BY OBJECT_NAME";
    }

    @Override
    public String normalizeSchema(String schema) {
        if (schema == null || schema.isBlank()) {
            return null;
        }
        return schema.trim().toUpperCase();
    }

    private String resolveOwner(DataSourceConfig source) {
        String owner = source.getSchema();
        if (owner == null || owner.isBlank()) {
            owner = source.getUsername();
        }
        if (owner == null) {
            return "";
        }
        return owner.trim().toUpperCase();
    }
}
