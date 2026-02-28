package com.example.dbcli.cli;

import com.example.dbcli.config.ConfigLoader;
import com.example.dbcli.config.DataSourceConfig;
import com.example.dbcli.db.DatabaseClient;
import com.example.dbcli.db.QueryResult;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

import java.nio.file.Path;
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
                CliApp.QueryCmd.class
        }
)
public class CliApp implements Runnable {
    @Option(
            names = {"-c", "--config"},
            description = "Datasource config json path",
            scope = CommandLine.ScopeType.INHERIT
    )
    Path config;

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

    @Command(name = "list-sources", description = "List datasource names from config")
    static class ListSourcesCmd implements Callable<Integer> {
        @CommandLine.ParentCommand
        CliApp parent;

        @Override
        public Integer call() {
            Map<String, DataSourceConfig> sources = parent.loadSources();
            new TreeSet<>(sources.keySet()).forEach(System.out::println);
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
            tables.forEach(System.out::println);
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
            System.out.println(ddl);
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

            if (!result.columns().isEmpty()) {
                System.out.println(TextTable.render(result.columns(), result.rows()));
                System.out.println("rows: " + result.rows().size());
            } else {
                System.out.println("updateCount: " + result.updateCount());
            }
            return 0;
        }
    }
}
