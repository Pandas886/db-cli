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
}
