package DTFParser;

public interface Recordable<T> {
    int insertIntoTable(String tableName, String rowName, T value);
    boolean checkIfValueExists(String tableName, String rowName, T value);
}
