package com.example.dbcli.db;

import com.example.dbcli.config.DataSourceConfig;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class OracleDialectTest {
    @Test
    void oracleDialectBuildsOwnerBasedListTablesSql() {
        DataSourceConfig source = new DataSourceConfig();
        source.setDialect("oracle");
        source.setSchema("app_schema");

        Dialect dialect = DialectFactory.forSource(source);
        String sql = dialect.listTablesSql(source);

        assertTrue(sql.contains("ALL_OBJECTS"));
        assertTrue(sql.contains("OWNER = 'APP_SCHEMA'"));
        assertTrue(sql.contains("OBJECT_TYPE IN ('TABLE','VIEW')"));
    }

    @Test
    void oracleDialectUsesUsernameWhenSchemaMissing() {
        DataSourceConfig source = new DataSourceConfig();
        source.setDialect("oracle");
        source.setUsername("scott");

        Dialect dialect = DialectFactory.forSource(source);
        String sql = dialect.listTablesSql(source);

        assertTrue(sql.contains("OWNER = 'SCOTT'"));
    }

    @Test
    void oracleDialectBuildsDbmsMetadataDdlSql() {
        DataSourceConfig source = new DataSourceConfig();
        source.setDialect("oracle");
        source.setSchema("hr");

        Dialect dialect = DialectFactory.forSource(source);
        String sql = dialect.ddlSql(source, "employees");

        assertEquals("SELECT DBMS_METADATA.GET_DDL('TABLE', 'EMPLOYEES', 'HR') AS DDL FROM DUAL", sql);
    }
}
