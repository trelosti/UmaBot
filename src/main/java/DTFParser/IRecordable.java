package DTFParser;

import java.sql.SQLException;

public interface IRecordable<T> {
    int insertIntoTable(String tableName, String rowName, T value) throws SQLException;
    boolean checkIfValueExists(String tableName, String rowName, T value) throws SQLException;
    void deleteAndResetAllRows(String tableName, String columnName);
    int getCountOfRows(String schema, String tableName);
}
