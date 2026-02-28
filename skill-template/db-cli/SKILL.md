---
name: db-cli
description: Query relational databases from a datasource JSON file using the bundled dbcli binary. Use when tasks require listing datasource names, listing tables, viewing table DDL, or executing SQL against MySQL, PostgreSQL, ClickHouse, SQLite, SQL Server, Snowflake, Doris, StarRocks, DM, GBase, or KingBase.
---

# DB CLI Skill

Use this skill to run `dbcli` commands from a datasource config JSON file.

## Datasource Configuration

所有数据源信息统一存储在 `assets/my-datasources.json` 文件中：

- **如果文件不存在**：首次使用时会自动创建空配置模板
- **如果文件已存在**：直接从该文件加载数据源配置
- **添加新数据源**：编辑 `assets/my-datasources.json` 文件，在 `sources` 数组中添加新配置

配置格式示例：
```json
{
  "sources": [
    {
      "name": "mysql-prod",
      "driverClass": "com.mysql.cj.jdbc.Driver",
      "jdbcUrl": "jdbc:mysql://host:3306/db?useSSL=false",
      "username": "root",
      "password": "xxx",
      "database": "db",
      "schema": "",
      "dialect": "mysql"
    }
  ]
}
```

## Quick Workflow

1. Run bootstrap once to unpack bundled runtime.
: macOS/Linux: `scripts/bootstrap_dbcli.sh`
: Windows PowerShell: `scripts/bootstrap_dbcli.ps1`
2. Datasource config is automatically managed at `assets/my-datasources.json`.
3. Run dbcli wrapper with command arguments.
: macOS/Linux: `scripts/run_dbcli.sh ...`
: Windows PowerShell: `scripts/run_dbcli.ps1 ...`

## Commands

macOS/Linux:

**Using default config path (recommended):**

```bash
# List all configured data sources
scripts/run_dbcli.sh list-sources

# List tables in a database
scripts/run_dbcli.sh list-tables -s mysql-test

# Show table DDL
scripts/run_dbcli.sh show-ddl -s mysql-test my_table

# Execute SQL query
scripts/run_dbcli.sh query -s mysql-test --sql "select * from my_table limit 10"
```

**Using custom config path:**

```bash
scripts/run_dbcli.sh list-sources --config assets/my-datasources.json
scripts/run_dbcli.sh list-tables --config assets/my-datasources.json -s mysql-test
scripts/run_dbcli.sh show-ddl --config assets/my-datasources.json -s mysql-test orders
scripts/run_dbcli.sh query --config assets/my-datasources.json -s mysql-test --sql "select * from orders limit 10"
```

Windows PowerShell:

```powershell
# Using default config path
./scripts/run_dbcli.ps1 list-sources
./scripts/run_dbcli.ps1 list-tables -s mysql-test
./scripts/run_dbcli.ps1 show-ddl -s mysql-test my_table
./scripts/run_dbcli.ps1 query -s mysql-test --sql "select * from my_table limit 10"

# Using custom config path
./scripts/run_dbcli.ps1 list-sources --config assets/my-datasources.json
./scripts/run_dbcli.ps1 list-tables --config assets/my-datasources.json -s mysql-test
./scripts/run_dbcli.ps1 show-ddl --config assets/my-datasources.json -s mysql-test orders
./scripts/run_dbcli.ps1 query --config assets/my-datasources.json -s mysql-test --sql "select * from orders limit 10"
```

## Bundled Binary

- Platform bundle file: `assets/dbcli-bundle.tar.gz`
- Extraction path: `assets/runtime/dbcli-macos-arm64`
- Binary path: `assets/runtime/dbcli-macos-arm64/bin/dbcli`

## Notes

- Keep credentials in config files, not prompts.
- Use `list-sources` first when source name is unknown.
- Use `show-ddl` before generating complex SQL.
