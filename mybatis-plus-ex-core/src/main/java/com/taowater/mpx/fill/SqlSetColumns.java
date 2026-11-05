package com.taowater.mpx.fill;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Pattern;

/** Reads assignment targets, without interpreting or rewriting SQL expressions. */
final class SqlSetColumns {

    private static final String IDENTIFIER = "(?:[\\p{L}_$][\\p{L}\\p{N}_$]*|`(?:``|[^`])+`|\"(?:\"\"|[^\"])+\"|\\[(?:\\]\\]|[^\\]])+\\])";
    private static final Pattern COLUMN = Pattern.compile(IDENTIFIER + "(?:\\s*\\.\\s*" + IDENTIFIER + ")*");

    private SqlSetColumns() {
    }

    static Set<String> read(String sql) {
        Set<String> columns = new LinkedHashSet<>();
        if (sql == null || sql.trim().isEmpty()) {
            return columns;
        }
        String clean = removeComments(sql);
        for (String assignment : split(clean, ',')) {
            List<String> parts = split(assignment, '=');
            if (parts.size() < 2) {
                throw invalid(sql);
            }
            String target = parts.get(0).trim();
            if (target.startsWith("(") && target.endsWith(")")) {
                for (String column : split(target.substring(1, target.length() - 1), ',')) {
                    add(columns, column, sql);
                }
            } else {
                add(columns, target, sql);
            }
        }
        return columns;
    }

    private static void add(Set<String> columns, String column, String sql) {
        if (!COLUMN.matcher(column.trim()).matches()) {
            throw invalid(sql);
        }
        columns.add(normalize(column));
    }

    static String normalize(String column) {
        List<String> names = split(column.trim(), '.');
        String name = names.get(names.size() - 1).trim();
        if (name.startsWith("`")) {
            name = name.substring(1, name.length() - 1).replace("``", "`");
        } else if (name.startsWith("\"")) {
            name = name.substring(1, name.length() - 1).replace("\"\"", "\"");
        } else if (name.startsWith("[")) {
            name = name.substring(1, name.length() - 1).replace("]]", "]");
        }
        return name.toLowerCase(Locale.ROOT);
    }

    private static List<String> split(String sql, char separator) {
        List<String> parts = new ArrayList<>();
        int start = 0;
        int depth = 0;
        for (int i = 0; i < sql.length(); i++) {
            char c = sql.charAt(i);
            if (c == '\'' || c == '"' || c == '`' || c == '[') {
                i = quotedEnd(sql, i);
            } else if (c == '(' || c == '{') {
                depth++;
            } else if (c == ')' || c == '}') {
                if (--depth < 0) {
                    throw invalid(sql);
                }
            } else if (depth == 0 && c == separator) {
                parts.add(sql.substring(start, i));
                start = i + 1;
            }
        }
        if (depth != 0) {
            throw invalid(sql);
        }
        parts.add(sql.substring(start));
        return parts;
    }

    private static String removeComments(String sql) {
        StringBuilder clean = new StringBuilder();
        for (int i = 0; i < sql.length(); i++) {
            char c = sql.charAt(i);
            if (c == '\'' || c == '"' || c == '`' || c == '[') {
                int end = quotedEnd(sql, i);
                clean.append(sql, i, end + 1);
                i = end;
            } else if (c == '/' && i + 1 < sql.length() && sql.charAt(i + 1) == '*') {
                int end = sql.indexOf("*/", i + 2);
                if (end < 0) {
                    throw invalid(sql);
                }
                clean.append(' ');
                i = end + 1;
            } else if (c == '-' && i + 1 < sql.length() && sql.charAt(i + 1) == '-') {
                while (i + 1 < sql.length() && sql.charAt(i + 1) != '\n' && sql.charAt(i + 1) != '\r') {
                    i++;
                }
                clean.append(' ');
            } else {
                clean.append(c);
            }
        }
        return clean.toString();
    }

    private static int quotedEnd(String sql, int start) {
        char quote = sql.charAt(start);
        char end = quote == '[' ? ']' : quote;
        for (int i = start + 1; i < sql.length(); i++) {
            char c = sql.charAt(i);
            if (c == '\\' && quote == '\'') {
                i++;
            } else if (c == end) {
                if (i + 1 < sql.length() && sql.charAt(i + 1) == end) {
                    i++;
                } else {
                    return i;
                }
            }
        }
        throw invalid(sql);
    }

    private static IllegalArgumentException invalid(String sql) {
        return new IllegalArgumentException("Cannot identify SET columns for wrapper fill; use explicit column assignments: " + sql);
    }
}
