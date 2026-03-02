package com.example.dbcli.cli;

import cn.hutool.json.JSONUtil;
import com.example.dbcli.config.ConfigLoader;
import com.example.dbcli.config.DataSourceConfig;
import com.example.dbcli.db.DatabaseClient;
import com.example.dbcli.db.QueryResult;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;
import java.util.concurrent.Callable;

@Command(
        name = "dbcli",
        mixinStandardHelpOptions = true,
        version = "dbcli 0.1.0",
        description = "CLI for querying external-managed datasources",
        subcommands = {
                CliApp.ListSourcesCmd.class,
                CliApp.ListTablesCmd.class,
                CliApp.ShowDdlCmd.class,
                CliApp.QueryCmd.class,
                CliApp.TestCmd.class
        }
)
public class CliApp implements Runnable {
    enum OutputFormat {
        text,
        json
    }

    @Option(
            names = {"-c", "--config"},
            description = "Datasource config json path",
            scope = CommandLine.ScopeType.INHERIT
    )
    Path config;

    @Option(
            names = {"-f", "--format"},
            description = "Output format: ${COMPLETION-CANDIDATES}",
            defaultValue = "text",
            scope = CommandLine.ScopeType.INHERIT
    )
    OutputFormat format;

    private final DatabaseClient db = new DatabaseClient();

    @Override
    public void run() {
        CommandLine.usage(this, System.out);
    }

    Map<String, DataSourceConfig> loadSources() {
        if (config == null) {
            throw new IllegalArgumentException("Missing required option: --config");
        }
        return ConfigLoader.loadSources(config);
    }

    DataSourceConfig sourceByName(Map<String, DataSourceConfig> sources, String sourceName) {
        DataSourceConfig source = sources.get(sourceName);
        if (source == null) {
            String available = String.join(", ", new TreeSet<>(sources.keySet()));
            throw new IllegalArgumentException("Source not found: " + sourceName + ". Available: " + available);
        }
        return source;
    }

    boolean isJsonOutput() {
        return format == OutputFormat.json;
    }

    static String toJson(Object payload) {
        return JSONUtil.toJsonPrettyStr(payload);
    }

    @Command(name = "list-sources", description = "List datasource names from config")
    static class ListSourcesCmd implements Callable<Integer> {
        @CommandLine.ParentCommand
        CliApp parent;

        @Override
        public Integer call() {
            Map<String, DataSourceConfig> sources = parent.loadSources();
            List<String> sorted = new ArrayList<>(new TreeSet<>(sources.keySet()));
            if (parent.isJsonOutput()) {
                Map<String, Object> payload = new LinkedHashMap<>();
                payload.put("sources", sorted);
                payload.put("count", sorted.size());
                System.out.println(CliApp.toJson(payload));
            } else {
                sorted.forEach(System.out::println);
            }
            return 0;
        }
    }

    @Command(name = "list-tables", description = "List tables/views of a datasource")
    static class ListTablesCmd implements Callable<Integer> {
        @CommandLine.ParentCommand
        CliApp parent;

        @Option(names = {"-s", "--source"}, required = true, description = "Datasource name")
        String source;

        @Override
        public Integer call() throws Exception {
            Map<String, DataSourceConfig> sources = parent.loadSources();
            DataSourceConfig ds = parent.sourceByName(sources, source);
            List<String> tables = parent.db.listTables(ds);
            if (parent.isJsonOutput()) {
                Map<String, Object> payload = new LinkedHashMap<>();
                payload.put("source", source);
                payload.put("tables", tables);
                payload.put("count", tables.size());
                System.out.println(CliApp.toJson(payload));
            } else {
                tables.forEach(System.out::println);
            }
            return 0;
        }
    }

    @Command(name = "show-ddl", description = "Show table DDL")
    static class ShowDdlCmd implements Callable<Integer> {
        @CommandLine.ParentCommand
        CliApp parent;

        @Option(names = {"-s", "--source"}, required = true, description = "Datasource name")
        String source;

        @Parameters(index = "0", description = "Table name")
        String table;

        @Override
        public Integer call() throws Exception {
            Map<String, DataSourceConfig> sources = parent.loadSources();
            DataSourceConfig ds = parent.sourceByName(sources, source);
            String ddl = parent.db.showDdl(ds, table);
            if (parent.isJsonOutput()) {
                Map<String, Object> payload = new LinkedHashMap<>();
                payload.put("source", source);
                payload.put("table", table);
                payload.put("ddl", ddl);
                System.out.println(CliApp.toJson(payload));
            } else {
                System.out.println(ddl);
            }
            return 0;
        }
    }

    @Command(name = "query", description = "Execute SQL on a datasource")
    static class QueryCmd implements Callable<Integer> {
        @CommandLine.ParentCommand
        CliApp parent;

        @Option(names = {"-s", "--source"}, required = true, description = "Datasource name")
        String source;

        @Option(names = {"--sql"}, required = true, description = "SQL to execute")
        String sql;

        @Option(names = {"-n", "--limit"}, defaultValue = "200", description = "Max rows to print, 0 for no limit")
        int limit;

        @Override
        public Integer call() throws Exception {
            Map<String, DataSourceConfig> sources = parent.loadSources();
            DataSourceConfig ds = parent.sourceByName(sources, source);
            QueryResult result = parent.db.query(ds, sql, limit);

            if (parent.isJsonOutput()) {
                Map<String, Object> payload = new LinkedHashMap<>();
                payload.put("source", source);
                payload.put("sql", sql);
                payload.put("limit", limit);

                List<String> columns = result.columns();
                List<Map<String, Object>> rowObjects = new ArrayList<>();
                for (List<Object> row : result.rows()) {
                    Map<String, Object> rowObject = new LinkedHashMap<>();
                    int size = Math.min(columns.size(), row.size());
                    for (int i = 0; i < size; i++) {
                        rowObject.put(columns.get(i), row.get(i));
                    }
                    rowObjects.add(rowObject);
                }

                payload.put("columns", columns);
                payload.put("rows", rowObjects);
                payload.put("rowCount", rowObjects.size());
                payload.put("updateCount", result.updateCount());
                payload.put("truncated", limit > 0 && !columns.isEmpty() && rowObjects.size() >= limit);
                System.out.println(CliApp.toJson(payload));
            } else if (!result.columns().isEmpty()) {
                System.out.println(TextTable.render(result.columns(), result.rows()));
                System.out.println("rows: " + result.rows().size());
            } else {
                System.out.println("updateCount: " + result.updateCount());
            }
            return 0;
        }
    }

    @Command(name = "test", description = "Test datasource connection")
    static class TestCmd implements Callable<Integer> {
        @CommandLine.ParentCommand
        CliApp parent;

        @Option(names = {"-s", "--source"}, required = true, description = "Datasource name")
        String source;

        @Option(names = {"-t", "--timeout"}, defaultValue = "5", description = "Connection validation timeout seconds")
        int timeoutSeconds;

        @Override
        public Integer call() {
            try {
                Map<String, DataSourceConfig> sources = parent.loadSources();
                DataSourceConfig ds = parent.sourceByName(sources, source);
                boolean ok = parent.db.testConnection(ds, timeoutSeconds);

                if (parent.isJsonOutput()) {
                    Map<String, Object> payload = new LinkedHashMap<>();
                    payload.put("source", source);
                    payload.put("ok", ok);
                    payload.put("error", ok ? null : "Connection validation failed");
                    System.out.println(CliApp.toJson(payload));
                } else {
                    System.out.println(ok ? "OK" : "FAILED: Connection validation failed");
                }
                return ok ? 0 : 1;
            } catch (Exception e) {
                if (parent.isJsonOutput()) {
                    Map<String, Object> payload = new LinkedHashMap<>();
                    payload.put("source", source);
                    payload.put("ok", false);
                    payload.put("error", e.getMessage());
                    System.out.println(CliApp.toJson(payload));
                } else {
                    System.out.println("FAILED: " + e.getMessage());
                }
                return 1;
            }
        }
    }
}
