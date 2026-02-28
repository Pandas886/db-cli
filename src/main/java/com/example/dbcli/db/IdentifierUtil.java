package com.example.dbcli.db;

public final class IdentifierUtil {
    private IdentifierUtil() {
    }

    public static String safeLiteral(String raw) {
        return raw == null ? "" : raw.replace("'", "''");
    }

    public static String trimIdentifier(String raw) {
        if (raw == null) {
            return "";
        }
        return raw.trim().replace("`", "").replace("\"", "");
    }
}
