package com.example.dbcli.db;

import com.example.dbcli.config.DataSourceConfig;

public class SnowflakeDialect implements Dialect {
    @Override
    public String ddlSql(DataSourceConfig source, String table) {
        String t = IdentifierUtil.trimIdentifier(table);
        String schema = source.getSchema();
        String db = source.getDatabase();

        StringBuilder objectName = new StringBuilder();
        if (db != null && !db.isBlank()) {
            objectName.append(db.trim()).append('.');
        }
        if (schema != null && !schema.isBlank()) {
            objectName.append(schema.trim()).append('.');
        }
        objectName.append(t);

        return "SELECT GET_DDL('TABLE', '" + IdentifierUtil.safeLiteral(objectName.toString()) + "')";
    }

    @Override
    public String listTablesSql(DataSourceConfig source) {
        String db = source.getDatabase();
        String schema = source.getSchema();
        if (db == null || db.isBlank() || schema == null || schema.isBlank()) {
            return "SHOW TABLES";
        }
        return "SELECT table_name FROM " + db.trim() + ".information_schema.tables WHERE table_schema = '" + IdentifierUtil.safeLiteral(schema) + "' ORDER BY table_name";
    }
}
