package DTFParser;

import java.net.URI;
import java.net.URISyntaxException;
import java.sql.*;

public class DatabaseWorker implements IRecordable<String> {
    Connection con;

    DatabaseWorker() {
        try {
            Class.forName("org.postgresql.Driver");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        try {
            con = getConnection();
            if (con != null) {
                System.out.println("You've successfully connected to database");
            } else {
                System.out.println("Failed to make connection to database");
            }
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
        System.out.println("try to check");
        boolean exists = false;
        Statement statement = null;
        ResultSet rs = null;
        try {
            statement = con.createStatement();
            rs = statement.executeQuery(String.format("select true from %s where %s like '%s'", tableName, rowName, value));
            if (rs.next()) {
                exists = rs.getBoolean(1);
            }
        }
        catch (SQLException e) {
            System.out.println("fail check");
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
    public void deleteAndResetAllRows(String tableName, String columnName) {
        Statement statement;
        try {
            statement = con.createStatement();
            statement.executeUpdate(String.format("delete from %s", tableName));
            statement.executeUpdate(String.format("alter sequence %s_%s_seq RESTART WITH 1", tableName, columnName));

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public int getCountOfRows(String schemaName, String tableName) {
        Statement statement;
        ResultSet res;
        int count = 0;
        try {
            statement = con.createStatement();
            res = statement.executeQuery(String.format("SELECT count(*) AS exact_count FROM %s.%s;", schemaName, tableName));
            if (res.next()) {
                count = res.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return count;
   }

    private static Connection getConnection() throws URISyntaxException, SQLException {
//        URI dbUri = null;
//        try {
//            dbUri = new URI(System.getenv("DATABASE_URL"));
//        } catch (URISyntaxException e) {
//            e.printStackTrace();
//        }
//
//        String username = dbUri.getUserInfo().split(":")[0];
//        String password = dbUri.getUserInfo().split(":")[1];
//        String dbUrl = "jdbc:postgresql://" + dbUri.getHost() + ':' + dbUri.getPort() + dbUri.getPath() + "?sslmode=require";
//
//        System.out.println("connected");
//        return DriverManager.getConnection(dbUrl, username, password);

        String dbUrl = System.getenv("JDBC_DATABASE_URL");
        return DriverManager.getConnection(dbUrl);
    }
}
