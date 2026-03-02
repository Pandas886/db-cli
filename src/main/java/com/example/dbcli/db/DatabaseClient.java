package com.example.dbcli.db;

import com.example.dbcli.config.DataSourceConfig;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class DatabaseClient {
    public boolean testConnection(DataSourceConfig source, int timeoutSeconds) throws SQLException, ClassNotFoundException {
        try (Connection conn = open(source)) {
            if (timeoutSeconds > 0) {
                try {
                    return conn.isValid(timeoutSeconds);
                } catch (SQLFeatureNotSupportedException | AbstractMethodError ignored) {
                    // Old/non-standard drivers may not support connection validation.
                }
            }
            return !conn.isClosed();
        }
    }

    public List<String> listTables(DataSourceConfig source) throws SQLException, ClassNotFoundException {
        Dialect dialect = DialectFactory.forSource(source);
        List<String> dialectTables = listTablesFromDialectSql(source, dialect);
        if (!dialectTables.isEmpty()) {
            return dialectTables;
        }
        return listTablesFromMetadata(source, dialect);
    }

    public String showDdl(DataSourceConfig source, String table) throws SQLException, ClassNotFoundException {
        Dialect dialect = DialectFactory.forSource(source);
        String ddlSql = dialect.ddlSql(source, table);

        if (!ddlSql.isBlank()) {
            try (Connection conn = open(source);
                 Statement st = conn.createStatement();
                 ResultSet rs = st.executeQuery(ddlSql)) {
                if (rs.next()) {
                    ResultSetMetaData md = rs.getMetaData();
                    String firstNonEmpty = null;
                    for (int i = 1; i <= md.getColumnCount(); i++) {
                        Object val = rs.getObject(i);
                        if (val == null) {
                            continue;
                        }
                        String text = val.toString();
                        if (firstNonEmpty == null && !text.isBlank()) {
                            firstNonEmpty = text;
                        }
                        if (text.toLowerCase().contains("create table")) {
                            return text;
                        }
                    }
                    if (firstNonEmpty != null) {
                        return firstNonEmpty;
                    }
                }
            } catch (SQLException ignored) {
                // Fallback below for unsupported dialect SQL.
            }
        }

        return buildCreateTableFromMetadata(source, table);
    }

    public QueryResult query(DataSourceConfig source, String sql, int limit) throws SQLException, ClassNotFoundException {
        try (Connection conn = open(source);
             PreparedStatement ps = conn.prepareStatement(sql)) {
            boolean hasResultSet = ps.execute();
            if (!hasResultSet) {
                return new QueryResult(List.of(), List.of(), ps.getUpdateCount());
            }

            try (ResultSet rs = ps.getResultSet()) {
                ResultSetMetaData md = rs.getMetaData();
                int cols = md.getColumnCount();
                List<String> columns = new ArrayList<>(cols);
                for (int i = 1; i <= cols; i++) {
                    columns.add(md.getColumnLabel(i));
                }

                List<List<Object>> rows = new ArrayList<>();
                int count = 0;
                while (rs.next() && (limit <= 0 || count < limit)) {
                    List<Object> row = new ArrayList<>(cols);
                    for (int i = 1; i <= cols; i++) {
                        row.add(rs.getObject(i));
                    }
                    rows.add(row);
                    count++;
                }

                return new QueryResult(columns, rows, -1);
            }
        }
    }

    private List<String> listTablesFromDialectSql(DataSourceConfig source, Dialect dialect) throws SQLException, ClassNotFoundException {
        String sql = dialect.listTablesSql(source);
        if (sql == null || sql.isBlank()) {
            return List.of();
        }

        try (Connection conn = open(source);
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            Set<String> names = new LinkedHashSet<>();
            while (rs.next()) {
                for (int i = 1; i <= rs.getMetaData().getColumnCount(); i++) {
                    Object val = rs.getObject(i);
                    if (val != null) {
                        String name = val.toString().trim();
                        if (!name.isBlank()) {
                            names.add(name);
                            break;
                        }
                    }
                }
            }
            return toSortedList(names);
        }
    }

    private List<String> listTablesFromMetadata(DataSourceConfig source, Dialect dialect) throws SQLException, ClassNotFoundException {
        try (Connection conn = open(source)) {
            DatabaseMetaData meta = conn.getMetaData();
            String schemaPattern = normalizeEmpty(dialect.normalizeSchema(source.getSchema()));
            String catalog = normalizeEmpty(dialect.normalizeDatabase(source.getDatabase()));
            Set<String> tables = new LinkedHashSet<>();

            try (ResultSet rs = meta.getTables(catalog, schemaPattern, "%", new String[]{"TABLE", "VIEW"})) {
                while (rs.next()) {
                    String name = rs.getString("TABLE_NAME");
                    if (name != null && !name.isBlank()) {
                        tables.add(name);
                    }
                }
            }

            return toSortedList(tables);
        }
    }

    private String buildCreateTableFromMetadata(DataSourceConfig source, String table) throws SQLException, ClassNotFoundException {
        try (Connection conn = open(source)) {
            DatabaseMetaData meta = conn.getMetaData();
            String schemaPattern = normalizeEmpty(source.getSchema());
            String catalog = normalizeEmpty(source.getDatabase());

            List<String> defs = new ArrayList<>();
            try (ResultSet rs = meta.getColumns(catalog, schemaPattern, table, "%")) {
                while (rs.next()) {
                    String col = rs.getString("COLUMN_NAME");
                    String type = rs.getString("TYPE_NAME");
                    int size = rs.getInt("COLUMN_SIZE");
                    int digits = rs.getInt("DECIMAL_DIGITS");
                    int nullable = rs.getInt("NULLABLE");

                    StringBuilder d = new StringBuilder();
                    d.append("  ").append(col).append(" ").append(type);
                    if (size > 0 && !typeContainsNoLength(type)) {
                        d.append("(").append(size);
                        if (digits > 0) {
                            d.append(",").append(digits);
                        }
                        d.append(")");
                    }
                    if (nullable == DatabaseMetaData.columnNoNulls) {
                        d.append(" NOT NULL");
                    }
                    defs.add(d.toString());
                }
            }

            if (defs.isEmpty()) {
                throw new IllegalArgumentException("Table not found or no metadata access: " + table);
            }

            StringBuilder ddl = new StringBuilder();
            ddl.append("CREATE TABLE ").append(table).append(" (\n");
            for (int i = 0; i < defs.size(); i++) {
                ddl.append(defs.get(i));
                if (i < defs.size() - 1) {
                    ddl.append(",");
                }
                ddl.append("\n");
            }
            ddl.append(");");
            return ddl.toString();
        }
    }

    private boolean typeContainsNoLength(String type) {
        if (type == null) {
            return false;
        }
        String t = type.toLowerCase();
        return t.contains("text") || t.contains("date") || t.contains("time") || t.contains("blob") || t.contains("json");
    }

    private Connection open(DataSourceConfig source) throws SQLException, ClassNotFoundException {
        if (source.getDriverClass() != null && !source.getDriverClass().isBlank()) {
            Class.forName(source.getDriverClass());
        }
        Connection conn = DriverManager.getConnection(source.getJdbcUrl(), source.getUsername(), source.getPassword());
        if (source.getSchema() != null && !source.getSchema().isBlank()) {
            try {
                conn.setSchema(source.getSchema());
            } catch (Exception ignored) {
                // Not all JDBC drivers support setSchema.
            }
        }
        return conn;
    }

    private String normalizeEmpty(String s) {
        return (s == null || s.isBlank()) ? null : s;
    }

    private List<String> toSortedList(Set<String> names) {
        List<String> out = new ArrayList<>(names);
        out.sort(Comparator.naturalOrder());
        return out;
    }
}
