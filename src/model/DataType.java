package model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DataType {
    private final String baseType;
    private final Integer length;
    private final Integer precision;
    private final Integer scale;

    private static final Pattern VARCHAR_PATTERN = Pattern.compile("VARCHAR\\((\\d+)\\)", Pattern.CASE_INSENSITIVE);
    private static final Pattern CHAR_PATTERN = Pattern.compile("CHAR\\((\\d+)\\)", Pattern.CASE_INSENSITIVE);
    private static final Pattern DECIMAL_PATTERN = Pattern.compile("DECIMAL\\((\\d+)(?:,(\\d+))?\\)", Pattern.CASE_INSENSITIVE);

    public DataType(String typeString) {
        String normalized = typeString.trim().toUpperCase();
        // Handle VARCHAR
        Matcher varcharMatcher = VARCHAR_PATTERN.matcher(normalized);
        if (varcharMatcher.matches()) {
            this.baseType = "VARCHAR";
            this.length = Integer.parseInt(varcharMatcher.group(1));
            this.precision = null;
            this.scale = null;
            return;
        }

        // Handle CHAR
        Matcher charMatcher = CHAR_PATTERN.matcher(normalized);
        if (charMatcher.matches()) {
            this.baseType = "CHAR";
            this.length = Integer.parseInt(charMatcher.group(1));
            this.precision = null;
            this.scale = null;
            return;
        }

        // Handle DECIMAL
        Matcher decimalMatcher = DECIMAL_PATTERN.matcher(normalized);
        if (decimalMatcher.matches()) {
            this.baseType = "DECIMAL";
            this.length = null;
            this.precision = Integer.parseInt(decimalMatcher.group(1));
            this.scale = decimalMatcher.group(2) != null ? Integer.parseInt(decimalMatcher.group(2)) : 0;
            return;
        }

        // Handle simple types
        switch (normalized) {
            case "INT":
            case "INTEGER":
                this.baseType = "INTEGER";
                break;
            case "BIGINT":
                this.baseType = "BIGINT";
                break;
            case "SMALLINT":
                this.baseType = "SMALLINT";
                break;
            case "TEXT":
                this.baseType = "TEXT";
                break;
            case "BOOLEAN":
            case "BOOL":
                this.baseType = "BOOLEAN";
                break;
            case "DATE":
                this.baseType = "DATE";
                break;
            case "TIMESTAMP":
                this.baseType = "TIMESTAMP";
                break;
            case "FLOAT":
                this.baseType = "FLOAT";
                break;
            case "DOUBLE":
                this.baseType = "DOUBLE";
                break;
            case "BLOB":
                this.baseType = "BLOB";
                break;
            default:
                throw new IllegalArgumentException("Unsupported data type: " + typeString);
        }

        this.length = null;
        this.precision = null;
        this.scale = null;
    }

    public String getBaseType() {
        return baseType;
    }

    public Integer getLength() {
        return length;
    }

    public Integer getPrecision() {
        return precision;
    }

    public Integer getScale() {
        return scale;
    }

    public boolean isValid() {
        return baseType != null;
    }

    public String validateValue(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null; // Allow null values for now
        }

        try {
            switch (baseType) {
                case "INTEGER":
                    int intVal = Integer.parseInt(value);
                    return String.valueOf(intVal);

                case "BIGINT":
                    long longVal = Long.parseLong(value);
                    return String.valueOf(longVal);

                case "SMALLINT":
                    short shortVal = Short.parseShort(value);
                    return String.valueOf(shortVal);

                case "VARCHAR":
                    if (value.length() > length) {
                        throw new IllegalArgumentException("VARCHAR value exceeds maximum length: " + length);
                    }
                    return value;

                case "CHAR":
                    if (value.length() > length) {
                        throw new IllegalArgumentException("CHAR value exceeds maximum length: " + length);
                    }
                    // Pad with spaces to fixed length
                    return String.format("%-" + length + "s", value);

                case "TEXT":
                    return value;

                case "BOOLEAN":
                    String lowerValue = value.toLowerCase();
                    if (lowerValue.equals("true") || lowerValue.equals("1") || lowerValue.equals("yes")) {
                        return "true";
                    } else if (lowerValue.equals("false") || lowerValue.equals("0") || lowerValue.equals("no")) {
                        return "false";
                    } else {
                        throw new IllegalArgumentException("Invalid boolean value: " + value);
                    }

                case "DATE":
                    LocalDate.parse(value, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
                    return value;

                case "TIMESTAMP":
                    // 2 timestamp formats are allowed for now
                    try {
                        LocalDateTime.parse(value, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
                    } catch (DateTimeParseException e) {
                        LocalDateTime.parse(value, DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss"));
                    }
                    return value;

                case "DECIMAL":
                    BigDecimal decimal = new BigDecimal(value);
                    decimal = decimal.stripTrailingZeros();

                    int valueScale = decimal.scale();
                    int digitsBeforeDecimal = decimal.precision() - valueScale;

                    if (digitsBeforeDecimal > (precision - scale)) {
                        throw new IllegalArgumentException("DECIMAL value exceeds max digits before decimal: allowed " + (precision - scale));
                    }
                    if (valueScale > scale) {
                        throw new IllegalArgumentException("DECIMAL value exceeds scale: allowed " + scale);
                    }

                    return decimal.setScale(scale, BigDecimal.ROUND_HALF_UP).toPlainString();

                case "FLOAT":
                    float floatVal = Float.parseFloat(value);
                    return String.valueOf(floatVal);

                case "DOUBLE":
                    double doubleVal = Double.parseDouble(value);
                    return String.valueOf(doubleVal);

                case "BLOB":
                    // For simplicity, treat as base64 encoded string
                    return value;

                default:
                    throw new IllegalArgumentException("Unsupported data type for validation: " + baseType);
            }
        } catch (NumberFormatException | DateTimeParseException e) {
            throw new IllegalArgumentException("Invalid value '" + value + "' for type " + baseType + ": " + e.getMessage());
        }
    }

    @Override
    public String toString() {
        switch (baseType) {
            case "VARCHAR":
                return "VARCHAR(" + length + ")";
            case "CHAR":
                return "CHAR(" + length + ")";
            case "DECIMAL":
                return "DECIMAL(" + precision + "," + scale + ")";
            default:
                return baseType;
        }
    }
}
