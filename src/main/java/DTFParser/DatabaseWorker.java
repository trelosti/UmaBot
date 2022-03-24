package DTFParser;

import java.net.URI;
import java.net.URISyntaxException;
import java.sql.*;

public class DatabaseWorker implements Recordable<String> {
    private Statement statement;

    DatabaseWorker() {
        try {
            Class.forName("org.postgresql.Driver");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        try {
            Connection con = getConnection();
            this.statement = con.createStatement();
        } catch (SQLException | URISyntaxException e) {
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
