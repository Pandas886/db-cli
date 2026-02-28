# db-cli

Agent-first database CLI for AI skills. This tool is designed to be invoked by AI agents (for example Claude Code skills), not as a general-purpose human-first SQL client.

## Agent-First Purpose

- Act as an execution backend for AI agent skills.
- Provide stable commands and predictable outputs for agent tool-calling.
- Query databases from externally managed datasource JSON configs.

## What This CLI Does

- Read datasource config from one JSON file managed by another system.
- Resolve datasource by `name`.
- List tables/views.
- Show table DDL (native SQL when available, metadata fallback otherwise).
- Run SQL and print result as text table.

## Build

Requirements:

- JDK 21
- Maven 3.9+

```bash
mvn -DskipTests package
```

Run from Maven build output:

```bash
java -cp "target/db-cli-0.1.0.jar:target/lib/*" com.example.dbcli.Main --help
```

## Config format

See example: `examples/datasources.json`

Top-level format:

```json
{
  "sources": [
    {
      "name": "source-name",
      "driverClass": "com.mysql.cj.jdbc.Driver",
      "jdbcUrl": "jdbc:mysql://host:3306/db",
      "username": "user",
      "password": "pwd",
      "database": "db",
      "schema": "",
      "dialect": "mysql"
    }
  ]
}
```

Notes:

- `name` and `jdbcUrl` are required.
- Built-in JDBC drivers in package: MySQL, PostgreSQL, ClickHouse, SQLite, SQL Server, Snowflake.
- For vendor/private drivers (e.g. DM, GBase, KingBase), put `*.jar` under project `drivers/` before packaging. Release scripts will copy them into `app/lib` automatically.
- `dialect` supports: `mysql`, `postgresql`, `supabase`, `gbase8a`, `gbase8c`, `clickhouse`, `dm`, `sqlite`, `snowflake`, `sqlserver`, `doris`, `starrocks`, `kingbase`, `generic`.

## Commands (Agent Invocation)

```bash
# list datasource names
dbcli -c /path/to/datasources.json list-sources

# list tables
dbcli -c /path/to/datasources.json list-tables -s mysql-test

# show ddl
dbcli -c /path/to/datasources.json show-ddl -s mysql-test orders

# run query
dbcli -c /path/to/datasources.json query -s mysql-test --sql "select * from orders limit 5"

# machine-readable json output
dbcli -c /path/to/datasources.json -f json query -s mysql-test --sql "select * from orders limit 5"
```

Recommended usage: call this CLI from a skill wrapper script (`run_dbcli.sh` or `run_dbcli.ps1`) so the agent can execute it consistently across environments.

## No-JDK packaging

Build runtime image via `jlink`:

```bash
scripts/build-runtime.sh
```

Build app image via `jpackage`:

```bash
scripts/package-app.sh
```

Output:

- runtime: `target/runtime`
- app image: `target/dist/dbcli`
