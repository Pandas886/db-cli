package com.example.dbcli.db;

import com.example.dbcli.config.DataSourceConfig;

public class ClickHouseDialect implements Dialect {
    @Override
    public String ddlSql(DataSourceConfig source, String table) {
        String db = source.getDatabase();
        String t = IdentifierUtil.trimIdentifier(table);
        if (db != null && !db.isBlank()) {
            return "SHOW CREATE TABLE " + normalizeDatabase(db) + "." + normalizeTable(t);
        }
        return "SHOW CREATE TABLE " + normalizeTable(t);
    }

    @Override
    public String listTablesSql(DataSourceConfig source) {
        String db = source.getDatabase();
        if (db != null && !db.isBlank()) {
            return "SELECT name FROM system.tables WHERE database = '" + IdentifierUtil.safeLiteral(db) + "' ORDER BY name";
        }
        return "SHOW TABLES";
    }

    @Override
    public String normalizeTable(String table) {
        return "`" + IdentifierUtil.trimIdentifier(table) + "`";
    }

    @Override
    public String normalizeDatabase(String database) {
        return "`" + IdentifierUtil.trimIdentifier(database) + "`";
    }
}
