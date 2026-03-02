package com.example.dbcli.cli;

import org.junit.jupiter.api.Test;
import picocli.CommandLine;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TestCommandTest {
    @Test
    void listSourcesReturnsDialectInJsonMode() throws Exception {
        Path tempDir = Files.createTempDirectory("dbcli-list-sources");
        Path configFile = tempDir.resolve("datasources.json");
        Files.writeString(configFile, """
                {
                  "sources": [
                    {
                      "name": "mysql-main",
                      "jdbcUrl": "jdbc:mysql://127.0.0.1:3306/demo",
                      "dialect": "mysql",
                      "database": "demo_db",
                      "schema": ""
                    },
                    {
                      "name": "pg-main",
                      "jdbcUrl": "jdbc:postgresql://127.0.0.1:5432/demo",
                      "dialect": "postgresql",
                      "database": "app",
                      "schema": "public"
                    }
                  ]
                }
                """, StandardCharsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        PrintStream originalOut = System.out;
        try {
            System.setOut(new PrintStream(out, true, StandardCharsets.UTF_8));
            int code = new CommandLine(new CliApp()).execute(
                    "-c", configFile.toString(),
                    "-f", "json",
                    "list-sources"
            );
            String output = out.toString(StandardCharsets.UTF_8);
            assertEquals(0, code);
            assertTrue(output.contains("\"sources\""));
            assertTrue(output.contains("\"source\": \"mysql-main\""));
            assertTrue(output.contains("\"dialect\": \"mysql\""));
            assertTrue(output.contains("\"database\": \"demo_db\""));
            assertTrue(output.contains("\"schema\": \"\""));
            assertTrue(output.contains("\"source\": \"pg-main\""));
            assertTrue(output.contains("\"dialect\": \"postgresql\""));
            assertTrue(output.contains("\"database\": \"app\""));
            assertTrue(output.contains("\"schema\": \"public\""));
            assertTrue(output.contains("\"sourceNames\""));
        } finally {
            System.setOut(originalOut);
        }
    }

    @Test
    void returnsOkWhenDatasourceConnectionIsHealthy() throws Exception {
        Path tempDir = Files.createTempDirectory("dbcli-test-ok");
        Path dbFile = tempDir.resolve("ok.db");
        Path configFile = tempDir.resolve("datasources.json");
        Files.writeString(configFile, """
                {
                  "sources": [
                    {
                      "name": "sqlite-ok",
                      "driverClass": "org.sqlite.JDBC",
                      "jdbcUrl": "jdbc:sqlite:%s",
                      "dialect": "sqlite"
                    }
                  ]
                }
                """.formatted(dbFile.toAbsolutePath().toString().replace("\\", "\\\\")), StandardCharsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        PrintStream originalOut = System.out;
        try {
            System.setOut(new PrintStream(out, true, StandardCharsets.UTF_8));
            int code = new CommandLine(new CliApp()).execute(
                    "-c", configFile.toString(),
                    "test",
                    "-s", "sqlite-ok"
            );
            assertEquals(0, code);
            assertTrue(out.toString(StandardCharsets.UTF_8).contains("OK"));
        } finally {
            System.setOut(originalOut);
        }
    }

    @Test
    void returnsFailedWhenDatasourceConnectionIsUnhealthyInJsonMode() throws Exception {
        Path tempDir = Files.createTempDirectory("dbcli-test-fail");
        Path configFile = tempDir.resolve("datasources.json");
        Files.writeString(configFile, """
                {
                  "sources": [
                    {
                      "name": "bad-url",
                      "jdbcUrl": "jdbc:invalid://127.0.0.1:1/db"
                    }
                  ]
                }
                """, StandardCharsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        PrintStream originalOut = System.out;
        try {
            System.setOut(new PrintStream(out, true, StandardCharsets.UTF_8));
            int code = new CommandLine(new CliApp()).execute(
                    "-c", configFile.toString(),
                    "-f", "json",
                    "test",
                    "-s", "bad-url"
            );
            String output = out.toString(StandardCharsets.UTF_8);
            assertEquals(1, code);
            assertTrue(output.contains("\"source\""));
            assertTrue(output.contains("\"bad-url\""));
            assertTrue(output.contains("\"ok\": false"));
            assertTrue(output.contains("\"error\""));
        } finally {
            System.setOut(originalOut);
        }
    }

    @Test
    void listTablesReturnsErrorWhenDialectSqlFails() throws Exception {
        Path tempDir = Files.createTempDirectory("dbcli-list-tables-fail");
        Path dbFile = tempDir.resolve("dialect-mismatch.db");
        Path configFile = tempDir.resolve("datasources.json");
        Files.writeString(configFile, """
                {
                  "sources": [
                    {
                      "name": "sqlite-as-mysql",
                      "driverClass": "org.sqlite.JDBC",
                      "jdbcUrl": "jdbc:sqlite:%s",
                      "dialect": "mysql",
                      "database": "demo"
                    }
                  ]
                }
                """.formatted(dbFile.toAbsolutePath().toString().replace("\\", "\\\\")), StandardCharsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ByteArrayOutputStream err = new ByteArrayOutputStream();
        PrintStream originalOut = System.out;
        PrintStream originalErr = System.err;
        try {
            System.setOut(new PrintStream(out, true, StandardCharsets.UTF_8));
            System.setErr(new PrintStream(err, true, StandardCharsets.UTF_8));
            int code = new CommandLine(new CliApp()).execute(
                    "-c", configFile.toString(),
                    "-f", "json",
                    "list-tables",
                    "-s", "sqlite-as-mysql"
            );
            String errorOutput = err.toString(StandardCharsets.UTF_8);
            assertEquals(1, code);
            assertTrue(errorOutput.contains("syntax error"), errorOutput);
        } finally {
            System.setOut(originalOut);
            System.setErr(originalErr);
        }
    }
}
