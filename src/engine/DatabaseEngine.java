package engine;

import model.Column;
import model.Row;
import model.Table;
import parser.*;
import storage.TableStorage;

import java.util.ArrayList;
import java.util.List;

public class DatabaseEngine {

    public static void execute(String sqlQuery) {
        try {
            Query query = SQLParser.parse(sqlQuery);

            switch (query.getType()) {
                case "CREATE_TABLE":
                    executeCreateTable((CreateTableQuery) query);
                    break;
                case "INSERT":
                    executeInsert((InsertQuery) query);
                    break;
                case "SELECT":
                    executeSelect((SelectQuery) query);
                    break;
                default:
                    System.out.println("Query not yet supported: " + query.getType());
            }
        } catch (Exception e) {
            System.err.println("Error executing query ==> " + e.getMessage());
        }
    }

    private static void executeCreateTable(CreateTableQuery query) {
        if (TableStorage.tableExists(query.tableName)) {
            if (query.ifNotExists) {
                System.out.println("Table already exists: " + query.tableName);
                return;
            } else {
                throw new RuntimeException("Table already exists: " + query.tableName);
            }
        }

        List<Column> columns = new ArrayList<>();
        try {
            for (CreateTableQuery.ColumnDefinition colDef : query.columns) {
                Column column = new Column(colDef.name, colDef.type);
                if (!column.dataType.isValid()) {
                    System.out.println("Invalid data type: " + colDef.type);
                    return;
                }
                columns.add(column);
            }

            Table table = new Table(query.tableName, columns);
            TableStorage.saveTableToCSV(table);

            System.out.println("Table created: " + query.tableName);
            System.out.println("Columns:");
            for (Column col : columns) {
                System.out.println("  " + col.name + " " + col.dataType.toString());
            }
        } catch (IllegalArgumentException e) {
            System.out.println("Error creating table: " + e.getMessage());
        }
    }

    private static void executeInsert(InsertQuery query) {
        if (!TableStorage.tableExists(query.tableName)) {
            System.out.println("Table does not exist: " + query.tableName);
            return;
        }

        Table table = TableStorage.loadTableFromCSV(query.tableName);

        if (query.values.size() != table.columns.size()) {
            System.out.println("Column count mismatch. Expected: " + table.columns.size() +
                    ", Got: " + query.values.size());
            return;
        }

        try {
            List<String> validatedValues = new ArrayList<>();
            for (int i = 0; i < query.values.size(); i++) {
                String value = query.values.get(i);
                Column column = table.columns.get(i);
                String validatedValue = column.dataType.validateValue(value);
                validatedValues.add(validatedValue);
            }

            table.rows.add(new Row(validatedValues));
            TableStorage.saveTableToCSV(table);

            System.out.println("Row inserted into: " + query.tableName);
        } catch (IllegalArgumentException e) {
            System.out.println("Error inserting row: " + e.getMessage());
        }
    }

    private static void executeSelect(SelectQuery query) {
        if (!TableStorage.tableExists(query.tableName)) {
            System.out.println("Table does not exist: " + query.tableName);
            return;
        }

        Table table = TableStorage.loadTableFromCSV(query.tableName);

        // Print header with type information
        if (query.columns.contains("*")) {
            for (Column col : table.columns) {
                System.out.print(col.name + " (" + col.dataType.toString() + ")\t");
            }
        } else {
            for (String colName : query.columns) {
                Column col = findColumn(table, colName);
                if (col != null) {
                    System.out.print(col.name + " (" + col.dataType.toString() + ")\t");
                }
            }
        }
        System.out.println();

        // Print separator
        if (query.columns.contains("*")) {
            for (Column col : table.columns) {
                System.out.print("---\t");
            }
        } else {
            for (String colName : query.columns) {
                if (findColumn(table, colName) != null) {
                    System.out.print("---\t");
                }
            }
        }
        System.out.println();

        // Print rows
        for (Row row : table.rows) {
            if (query.columns.contains("*")) {
                for (String value : row.values) {
                    System.out.print((value != null ? value : "NULL") + "\t");
                }
            } else {
                for (String colName : query.columns) {
                    int colIndex = findColumnIndex(table, colName);
                    if (colIndex >= 0) {
                        String value = row.values.get(colIndex);
                        System.out.print((value != null ? value : "NULL") + "\t");
                    }
                }
            }
            System.out.println();
        }
    }

    private static Column findColumn(Table table, String columnName) {
        for (Column col : table.columns) {
            if (col.name.equalsIgnoreCase(columnName)) {
                return col;
            }
        }
        return null;
    }

    private static int findColumnIndex(Table table, String columnName) {
        for (int i = 0; i < table.columns.size(); i++) {
            if (table.columns.get(i).name.equalsIgnoreCase(columnName)) {
                return i;
            }
        }
        return -1;
    }
}
