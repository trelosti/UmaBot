package DTFParser;

public final class DatabaseInfo {
    private final static String DATABASE_NAME = "";
    private final static String USERNAME = "";
    private final static String PASSWORD = "";

    private DatabaseInfo() {}

    static String getDatabaseName() {
        return DATABASE_NAME;
    }

    static String getUsername() {
        return USERNAME;
    }

    static String getPassword() {
        return PASSWORD;
    }
}
