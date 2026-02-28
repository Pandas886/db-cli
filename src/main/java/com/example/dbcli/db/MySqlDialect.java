package com.example.dbcli.db;

import com.example.dbcli.config.DataSourceConfig;

public class MySqlDialect implements Dialect {
    @Override
    public String ddlSql(DataSourceConfig source, String table) {
        String clean = IdentifierUtil.trimIdentifier(table);
        return "SHOW CREATE TABLE " + normalizeTable(clean);
    }

    @Override
    public String listTablesSql(DataSourceConfig source) {
        String db = source.getDatabase();
        if (db != null && !db.isBlank()) {
            return "SHOW FULL TABLES FROM " + normalizeDatabase(db);
        }
        return "SHOW FULL TABLES";
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
