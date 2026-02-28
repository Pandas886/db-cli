package com.example.dbcli.cli;

import java.util.ArrayList;
import java.util.List;

public final class TextTable {
    private TextTable() {
    }

    public static String render(List<String> headers, List<List<Object>> rows) {
        if (headers == null || headers.isEmpty()) {
            return "(no columns)";
        }

        int cols = headers.size();
        int[] widths = new int[cols];
        for (int i = 0; i < cols; i++) {
            widths[i] = headers.get(i) == null ? 0 : headers.get(i).length();
        }

        List<List<String>> normalized = new ArrayList<>();
        for (List<Object> row : rows) {
            List<String> line = new ArrayList<>(cols);
            for (int i = 0; i < cols; i++) {
                String val = i < row.size() && row.get(i) != null ? String.valueOf(row.get(i)) : "NULL";
                line.add(val);
                widths[i] = Math.max(widths[i], val.length());
            }
            normalized.add(line);
        }

        StringBuilder out = new StringBuilder();
        out.append(border(widths)).append("\n");
        out.append(renderRow(headers, widths)).append("\n");
        out.append(border(widths)).append("\n");
        for (List<String> row : normalized) {
            out.append(renderRow(row, widths)).append("\n");
        }
        out.append(border(widths));
        return out.toString();
    }

    private static String border(int[] widths) {
        StringBuilder sb = new StringBuilder("+");
        for (int w : widths) {
            sb.append("-").append("-".repeat(Math.max(0, w))).append("-+");
        }
        return sb.toString();
    }

    private static String renderRow(List<String> values, int[] widths) {
        StringBuilder sb = new StringBuilder("|");
        for (int i = 0; i < widths.length; i++) {
            String v = i < values.size() && values.get(i) != null ? values.get(i) : "";
            sb.append(" ").append(padRight(v, widths[i])).append(" |");
        }
        return sb.toString();
    }

    private static String padRight(String s, int width) {
        if (s.length() >= width) {
            return s;
        }
        return s + " ".repeat(width - s.length());
    }
}
