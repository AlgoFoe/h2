package model;

import java.util.ArrayList;
import java.util.List;

public class Table {
    public final String name;
    public final List<Column> columns;
    public final List<Row> rows;

    public Table(String name, List<Column> columns) {
        this.name = name;
        this.columns = columns;
        this.rows = new ArrayList<>();
    }
}
