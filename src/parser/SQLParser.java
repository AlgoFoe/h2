package parser;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SQLParser {

    public static Query parse(String rawQuery) {
        String query = rawQuery.trim().toUpperCase();

        if (query.startsWith("CREATE TABLE")) {
            return parseCreateTable(rawQuery);
        } else if (query.startsWith("INSERT INTO")) {
            return parseInsert(rawQuery);
        } else if (query.startsWith("SELECT")) {
            return parseSelect(rawQuery);
        }

        throw new IllegalArgumentException("Unsupported query: " + rawQuery);
    }

    private static CreateTableQuery parseCreateTable(String query) {
        Pattern pattern = Pattern.compile(
                "CREATE TABLE\\s+(IF NOT EXISTS\\s+)?(\\w+)\\s*\\((.+)\\)",
                Pattern.CASE_INSENSITIVE
        );
        Matcher matcher = pattern.matcher(query);

        if (!matcher.find()) {
            throw new IllegalArgumentException("Invalid CREATE TABLE syntax");
        }
        boolean ifNotExists = matcher.group(1) != null;
        String tableName = matcher.group(2);
        String columnsStr = matcher.group(3);

        List<CreateTableQuery.ColumnDefinition> columns = new ArrayList<>();
        List<String> columnDefs = splitColumns(columnsStr);

        for (String columnDef : columnDefs) {
            String[] parts = columnDef.trim().split("\\s+", 2);
            if (parts.length < 2) {
                throw new IllegalArgumentException("Invalid column definition: " + columnDef);
            }
            String name = parts[0].trim();
            String type = parts[1].trim();
            columns.add(new CreateTableQuery.ColumnDefinition(name, type));
        }

        return new CreateTableQuery(tableName, columns, ifNotExists);
    }

    private static List<String> splitColumns(String input) {
        List<String> result = new ArrayList<>();
        int bracketLevel = 0;
        StringBuilder current = new StringBuilder();

        for (int i = 0; i < input.length(); i++) {
            char c = input.charAt(i);

            if (c == ',' && bracketLevel == 0) {
                result.add(current.toString().trim());
                current.setLength(0);
            } else {
                if (c == '(') bracketLevel++;
                else if (c == ')') bracketLevel--;
                current.append(c);
            }
        }

        if (current.length() > 0) {
            result.add(current.toString().trim());
        }

        return result;
    }


    private static InsertQuery parseInsert(String query) {
        // Pattern: INSERT INTO tableName VALUES (val1, val2, ...)
        Pattern pattern = Pattern.compile("INSERT INTO\\s+(\\w+)\\s+VALUES\\s*\\((.+)\\)", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(query);

        if (!matcher.find()) {
            throw new IllegalArgumentException("Invalid INSERT syntax");
        }

        String tableName = matcher.group(1);
        String valuesStr = matcher.group(2);

        List<String> values = new ArrayList<>();
        String[] valueParts = valuesStr.split(",");

        for (String value : valueParts) {
            String cleanValue = value.trim();
            // Remove quotes if present
            if (cleanValue.startsWith("'") && cleanValue.endsWith("'")) {
                cleanValue = cleanValue.substring(1, cleanValue.length() - 1);
            }
            values.add(cleanValue);
        }

        return new InsertQuery(tableName, values);
    }

    private static SelectQuery parseSelect(String query) {
        // Pattern: SELECT columns FROM tableName
        Pattern pattern = Pattern.compile("SELECT\\s+(.+?)\\s+FROM\\s+(\\w+)", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(query);

        if (!matcher.find()) {
            throw new IllegalArgumentException("Invalid SELECT syntax");
        }

        String columnsStr = matcher.group(1).trim();
        String tableName = matcher.group(2);

        List<String> columns;
        if ("*".equals(columnsStr)) {
            columns = List.of("*");
        } else {
            columns = Arrays.asList(columnsStr.split(","));
            columns.replaceAll(String::trim);
        }

        return new SelectQuery(columns, tableName);
    }
}