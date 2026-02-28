package com.example.dbcli.db;

import com.example.dbcli.config.DataSourceConfig;

public class SqliteDialect implements Dialect {
    @Override
    public String ddlSql(DataSourceConfig source, String table) {
        String t = IdentifierUtil.safeLiteral(IdentifierUtil.trimIdentifier(table));
        return "SELECT sql FROM sqlite_master WHERE type='table' AND name='" + t + "'";
    }

    @Override
    public String listTablesSql(DataSourceConfig source) {
        return "SELECT name FROM sqlite_master WHERE type IN ('table','view') AND name NOT LIKE 'sqlite_%' ORDER BY name";
    }
}
