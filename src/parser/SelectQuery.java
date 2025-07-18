package parser;
import java.util.List;

public class SelectQuery extends Query {
    public final List<String> columns;
    public final String tableName;

    public SelectQuery(List<String> columns, String tableName) {
        this.columns = columns;
        this.tableName = tableName;
    }

    @Override
    public String getType() {
        return "SELECT";
    }
}