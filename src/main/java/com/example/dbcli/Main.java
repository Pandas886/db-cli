package com.example.dbcli;

import com.example.dbcli.cli.CliApp;
import picocli.CommandLine;

public final class Main {
    private Main() {
    }

    public static void main(String[] args) {
        int code = new CommandLine(new CliApp()).execute(args);
        System.exit(code);
    }
}
