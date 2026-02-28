---
name: db-cli
description: Query relational databases from a datasource JSON file using the bundled dbcli binary. Use when tasks require listing datasource names, listing tables, viewing table DDL, or executing SQL against MySQL, PostgreSQL, ClickHouse, SQLite, SQL Server, Snowflake, Doris, StarRocks, DM, GBase, or KingBase.
---

# DB CLI Skill

Use this skill to run `dbcli` commands from a datasource config JSON file.

## Quick Workflow

1. Run `scripts/bootstrap_dbcli.sh` once to unpack bundled runtime.
2. Prepare datasource config JSON (see `assets/datasources.example.json`).
3. Run `scripts/run_dbcli.sh` with command arguments.

## Commands

```bash
scripts/run_dbcli.sh list-sources --config /path/to/datasources.json
scripts/run_dbcli.sh list-tables --config /path/to/datasources.json -s mysql-demo
scripts/run_dbcli.sh show-ddl --config /path/to/datasources.json -s mysql-demo orders
scripts/run_dbcli.sh query --config /path/to/datasources.json -s mysql-demo --sql "select * from orders limit 10"
```

## Bundled Binary

- Platform bundle file: `assets/__BUNDLE_FILE__`
- Extraction path: `assets/runtime/__EXTRACTED_DIR__`
- Binary path: `assets/runtime/__BIN_REL_PATH__`

## Notes

- Keep credentials in config files, not prompts.
- Use `list-sources` first when source name is unknown.
- Use `show-ddl` before generating complex SQL.
