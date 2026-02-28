package com.example.dbcli.db;

import com.example.dbcli.config.DataSourceConfig;

public class GenericDialect implements Dialect {
    @Override
    public String ddlSql(DataSourceConfig source, String table) {
        return "";
    }

    @Override
    public String listTablesSql(DataSourceConfig source) {
        return "";
    }
}
