import engine.DatabaseEngine;

public class Main {
    public static void main(String[] args) {
        DatabaseEngine.execute("CREATE TABLE users (id INT, name TEXT, val DECIMAL(5,3))");
//        DatabaseEngine.execute("INSERT INTO products VALUES (1, 'Bag', 10.40)");
//        DatabaseEngine.execute("INSERT INTO users VALUES (3,null, 67.89)");
//        DatabaseEngine.execute("INSERT INTO users VALUES (3, 'kento', 1.924)");

//        DatabaseEngine.execute("SELECT * FROM products");
//        DatabaseEngine.execute("SELECT name FROM users");
    }
}
