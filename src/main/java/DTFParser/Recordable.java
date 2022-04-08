package DTFParser;

import java.sql.SQLException;

public interface Recordable<T> {
    int insertIntoTable(String tableName, String rowName, T value) throws SQLException;
    boolean checkIfValueExists(String tableName, String rowName, T value) throws SQLException;
    void deleteAllRows(String tableName);
}
