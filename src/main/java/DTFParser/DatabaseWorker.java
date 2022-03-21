package DTFParser;

import java.sql.*;

public class DatabaseWorker implements Recordable<String> {
    private String database;
    private String username;
    private String password;
    private Statement statement;

    DatabaseWorker(String database, String username, String password) {
        try {
            Class.forName("org.postgresql.Driver");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        try {
            Connection con = DriverManager.getConnection(
                    "jdbc:postgresql://localhost:5432/" + database,
                    username, password);
            this.statement = con.createStatement();


        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public int insertIntoTable(String tableName, String rowName, String link) {
        int res = 0;
        try {
            res = statement.executeUpdate(String.format("insert into %s(%s)" +
                                                  "values('%s')", tableName, rowName, link));
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return res;
    }

    @Override
    public boolean checkIfValueExists(String tableName, String rowName, String value) {
        boolean exists = false;
        try {
            ResultSet rs = statement.executeQuery(String.format("select true from %s where %s like '%s'", tableName, rowName, value));
            while (rs.next()) {
                exists = rs.getBoolean(1);
            }
        }
        catch (SQLException e) {
            e.printStackTrace();
        }

        return exists;
    }

    @Override
    public void deleteAllRows(String tableName) {
        try {
            statement.executeUpdate(String.format("delete from %s", tableName));
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
