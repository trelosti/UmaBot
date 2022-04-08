package DTFParser;

import javax.swing.plaf.nimbus.State;
import java.net.URI;
import java.net.URISyntaxException;
import java.sql.*;

public class DatabaseWorker implements Recordable<String> {
    Connection con;

    DatabaseWorker() {
        try {
            Class.forName("org.postgresql.Driver");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        try {
            con = getConnection();
        } catch (SQLException | URISyntaxException e) {
            e.printStackTrace();
        }
    }

    @Override
    public int insertIntoTable(String tableName, String rowName, String link) throws SQLException {
        int res = 0;
        Statement statement = null;
        try {
            statement = con.createStatement();
            res = statement.executeUpdate(String.format("insert into %s(%s)" +
                                                  "values('%s')", tableName, rowName, link));
        } catch (SQLException e) {
            e.printStackTrace();
        }

        try {
            statement.close();
        } finally {
            if (statement != null)
            {
                statement.close();
            }
        }

        return res;
    }

    @Override
    public boolean checkIfValueExists(String tableName, String rowName, String value) throws SQLException {
        boolean exists = false;
        Statement statement = null;
        ResultSet rs = null;
        try {
            statement = con.createStatement();
            rs = statement.executeQuery(String.format("select true from %s where %s like '%s'", tableName, rowName, value));
            exists = rs.getBoolean(1);
        }
        catch (SQLException e) {
            e.printStackTrace();
        }

        try {
            rs.close();
            statement.close();
        } finally {
            if (statement != null)
            {
                statement.close();
            }
        }
        return exists;
    }

    @Override
    public void deleteAllRows(String tableName) {
        Statement statement;
        try {
            statement = con.createStatement();
            statement.executeUpdate(String.format("delete from %s", tableName));
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static Connection getConnection() throws URISyntaxException, SQLException {
        URI dbUri = null;
        try {
            dbUri = new URI(System.getenv("DATABASE_URL"));
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }

        String username = dbUri.getUserInfo().split(":")[0];
        String password = dbUri.getUserInfo().split(":")[1];
        String dbUrl = "jdbc:postgresql://" + dbUri.getHost() + ':' + dbUri.getPort() + dbUri.getPath() + "?sslmode=require";

        return DriverManager.getConnection(dbUrl, username, password);
    }
}
