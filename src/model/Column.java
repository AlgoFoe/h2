package model;

public class Column {
    public final String name;
    public final DataType dataType;
    public final String typeString; // e.g., "INT", "TEXT"

    public Column(String name, String typeString) {
        this.name = name;
        this.typeString = typeString;
        this.dataType = new DataType(typeString);
    }
    public String getType() {
        return typeString;
    }
}
