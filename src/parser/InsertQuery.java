package parser;
import java.util.List;

public class InsertQuery extends Query {
    public final String tableName;
    public final List<String> values;

    public InsertQuery(String tableName, List<String> values) {
        this.tableName = tableName;
        this.values = values;
    }

    @Override
    public String getType() {
        return "INSERT";
    }
}