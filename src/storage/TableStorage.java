package storage;

import model.Column;
import model.Row;
import model.Table;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class TableStorage {
    private static final String DATA_DIR = "data";
    private static final String SCHEMA_SUFFIX = "_schema.csv";
    private static final String DATA_SUFFIX = "_data.csv";

    static {
        try {
            Files.createDirectories(Paths.get(DATA_DIR));
        } catch (IOException e) {
            throw new RuntimeException("Failed to create data directory", e);
        }
    }

    public static void saveTableToCSV(Table table) {
        try {
            saveSchema(table);
            saveData(table);
        } catch (IOException e) {
            throw new RuntimeException("Failed to save table: " + table.name, e);
        }
    }

    private static void saveSchema(Table table) throws IOException {
        Path schemaPath = Paths.get(DATA_DIR, table.name + SCHEMA_SUFFIX);

        try (FileWriter writer = new FileWriter(schemaPath.toFile());
             CSVPrinter printer = new CSVPrinter(writer, CSVFormat.DEFAULT)) {

            for (Column column : table.columns) {
                printer.printRecord(column.name, column.typeString);
            }
        }
    }

    private static void saveData(Table table) throws IOException {
        Path dataPath = Paths.get(DATA_DIR, table.name + DATA_SUFFIX);

        try (FileWriter writer = new FileWriter(dataPath.toFile());
             CSVPrinter printer = new CSVPrinter(writer, CSVFormat.DEFAULT)) {

            // Write header
            List<String> headers = new ArrayList<>();
            for (Column column : table.columns) {
                headers.add(column.name);
            }
            printer.printRecord(headers);

            // Write data
            for (Row row : table.rows) {
                List<String> rowValues = new ArrayList<>();
                for (String val : row.values) {
                    rowValues.add(val != null ? val : "NULL");
                }
                printer.printRecord(rowValues);
            }
        }
    }

    public static Table loadTableFromCSV(String tableName) {
        try {
            List<Column> columns = loadSchema(tableName);
            Table table = new Table(tableName, columns);
            loadData(table);
            return table;
        } catch (IOException e) {
            throw new RuntimeException("Failed to load table: " + tableName, e);
        }
    }

    private static List<Column> loadSchema(String tableName) throws IOException {
        Path schemaPath = Paths.get(DATA_DIR, tableName + SCHEMA_SUFFIX);

        if (!Files.exists(schemaPath)) {
            throw new RuntimeException("Table not found: " + tableName);
        }

        List<Column> columns = new ArrayList<>();

        try (FileReader reader = new FileReader(schemaPath.toFile());
             CSVParser parser = new CSVParser(reader, CSVFormat.DEFAULT)) {

            for (CSVRecord record : parser) {
                columns.add(new Column(record.get(0), record.get(1)));
            }
        }

        return columns;
    }

    private static void loadData(Table table) throws IOException {
        Path dataPath = Paths.get(DATA_DIR, table.name + DATA_SUFFIX);

        if (!Files.exists(dataPath)) {
            return;
        }

        try (FileReader reader = new FileReader(dataPath.toFile());
             CSVParser parser = new CSVParser(reader, CSVFormat.DEFAULT.withFirstRecordAsHeader())) {

            for (CSVRecord record : parser) {
                List<String> values = new ArrayList<>();
                for (int i = 0; i < table.columns.size(); i++) {
                    String rawValue = record.get(i);
                    Column column = table.columns.get(i);

                    // If value is "NULL", treat it as null
                    String value = rawValue.equalsIgnoreCase("NULL") ? null : rawValue;

                    // Validate value using DataType
                    String validatedValue = column.dataType.validateValue(value);
                    values.add(validatedValue);
                }
                table.rows.add(new Row(values));
            }
        }
    }


    public static boolean tableExists(String tableName) {
        Path schemaPath = Paths.get(DATA_DIR, tableName + SCHEMA_SUFFIX);
        return Files.exists(schemaPath);
    }
}