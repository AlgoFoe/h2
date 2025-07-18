package parser;
import java.util.List;

public class CreateTableQuery extends Query {
    public final String tableName;
    public final List<ColumnDefinition> columns;
    public final boolean ifNotExists;

    public CreateTableQuery(String tableName, List<ColumnDefinition> columns) {
        this.tableName = tableName;
        this.columns = columns;
        this.ifNotExists = false;
    }
    public CreateTableQuery(String tableName, List<ColumnDefinition> columns, boolean ifNotExists) {
        this.tableName = tableName;
        this.columns = columns;
        this.ifNotExists = ifNotExists;
    }

    @Override
    public String getType() {
        return "CREATE_TABLE";
    }

    public static class ColumnDefinition {
        public final String name;
        public final String type;

        public ColumnDefinition(String name, String type) {
            this.name = name;
            this.type = type;
        }
    }
}