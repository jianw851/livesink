package com.liveget.sink.util;


import java.sql.*;
import java.util.List;

public class MySQLService {
    private static MySQLService INSTANCE = null;

    private static Connection connect = null;
    private static Statement statement = null;
    private static PreparedStatement preparedStatement = null;
    private static String insertStatementHead = null;
    private static String insertStatementBody = null;
    private static String insertTableName = null;
    private static int insertRecordCount = 0;
    private static String createSinkTableStatement = null;
    // private static ResultSet resultSet = null;

    private static String kafkaSinkTaskProgressTableName = null;
    private static String kafkaSinkTableFactTableName = null;

    private MySQLService(String host, String port, String database, String username, String password) throws ClassNotFoundException, SQLException {
        // This will load the MySQL driver, each DB has its own driver
        Class.forName("com.mysql.cj.jdbc.Driver");
        // Setup the connection with the DB
        connect = DriverManager.getConnection("jdbc:mysql://" + host + ":" + port + "/" + database +
                "?user=" + username + "&password=" + password +
                "&useUnicode=true&useJDBCCompliantTimezoneShift=true&useLegacyDatetimeCode=false&serverTimezone=UTC");
    }

    public static MySQLService getInstance(String host, String port, String database, String username, String password) throws SQLException, ClassNotFoundException {
        if (INSTANCE == null) {
            INSTANCE = new MySQLService(host, port, database, username, password);
        }
        INSTANCE.kafkaSinkTaskProgressTableName = System.getenv("KAFKA_SINK_TASK_PROGRESS_TABLE_NAME");
        INSTANCE.kafkaSinkTableFactTableName = System.getenv("KAFKA_SINK_TABLE_FACT_TABLE_NAME");
        INSTANCE.connect.setCatalog(database);
        return INSTANCE;
    }

    public static String getInsertStatementTableName() {return INSTANCE.insertTableName;}

    public static String getCreateSinkTableStatement() {return INSTANCE.createSinkTableStatement;}

    public static void setInsertStatementTableName(String topic, String dateStr) {
        insertTableName = topic.replaceAll("-", "\\$") + "_" + dateStr.replaceAll("-", "_");
    }


    public static void setCreateSinkTableStatement(String sql) {
        if (insertTableName != null) {
            createSinkTableStatement = String.format(sql, insertTableName);
        }
    }

    public static String getInsertStatementHead() { return insertStatementHead; }

    public static int getInsertRecordCount() { return insertRecordCount; }

    public static void setInsertStatementHead(String i) {
        insertStatementHead = String.format(i, insertTableName);
    }

    public static Boolean isCurrentInsertionQueueEmpty() {
        return insertStatementBody == null ||
                insertStatementHead == null ||
                insertRecordCount == 0;
    }

    public static void resetInsertStatement() {
        insertStatementHead = null;
        insertTableName = null;
        insertStatementBody = null;
        insertRecordCount = 0;
    }

    public static void apppendInsertStatementBody(String i) {
        if (insertStatementBody == null)
            insertStatementBody = i;
        else
            insertStatementBody = insertStatementBody + "," + i;
        insertRecordCount += 1;
    }

    public static ResultSet getSQLQueryResult(String sql) throws SQLException {
        ResultSet result = null;
        try {
            if(statement != null && (!statement.isClosed()))
                statement.close();
            statement = connect.createStatement();
            // Result set get the result of the SQL query
            result = statement.executeQuery(sql);
        } catch(Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    public static Boolean checkExistsKafkaSinkTaskProgress(String topic) throws SQLException {
        String sql = "select count(*) as record_count from %s where topic = '%s' ;";
        ResultSet result = getSQLQueryResult(String.format(sql, kafkaSinkTaskProgressTableName, topic));
        int size = 0;
        if(result != null) {
            result.next();
            size = result.getInt("record_count");
            return size > 0;
        }
        return false;
    }

    public static Boolean checkExistsKafkaSinkTableFact(String topic) throws SQLException {
        String sql = "select count(*) as record_count from %s where topic = '%s' and table_name = '%s';";
        ResultSet result = getSQLQueryResult(String.format(sql, kafkaSinkTableFactTableName, topic, insertTableName));
        int size = 0;
        if(result != null) {
            result.next();
            size = result.getInt("record_count");
            return size > 0;
        }
        return false;
    }


    public static void updateKafkaSinkTaskProgress(String topic, String createDateStr, String createDateTimeStr) throws SQLException {
        // kafka_sink_task_progress: topic, kafka_head_date, batch_head_date, batch_head_datetime, kafka_start_date, create_datetime, update_datetime
        String sql = "update %s set kafka_head_date = '%s', kafka_start_date = '%s', update_datetime = '%s' where topic = '%s';";
        executeSQLStatement(String.format(sql, kafkaSinkTaskProgressTableName, createDateStr, createDateStr, createDateTimeStr, topic));
    }


    public static void insertKafkaSinkTaskProgress(String topic, String createDateStr, String createDateTimeStr) throws SQLException {
        // kafka_sink_task_progress: topic, kafka_head_date, batch_head_date, batch_head_datetime, kafka_start_date, create_datetime, update_datetime
        String sql = "insert into %s (topic, kafka_head_date, kafka_start_date, create_datetime) values( '%s', '%s', '%s', '%s');";
        executeSQLStatement(String.format(sql, kafkaSinkTaskProgressTableName, topic, createDateStr, createDateStr, createDateTimeStr));
    }

    public static void insertKafkaSinkTableFact(String topic, String createDateTimeStr) throws SQLException {
        String sql = "insert into %s (topic, table_name, create_datetime) values( '%s', '%s', '%s');";
        executeSQLStatement(String.format(sql, kafkaSinkTableFactTableName, topic, insertTableName, createDateTimeStr));
    }

    public static void executeSQLStatement(String sql) throws SQLException {
        try {
            statement = connect.createStatement();
            statement.executeUpdate(sql);
            statement.close();
            statement = null;
        } catch (SQLException e ) {
            e.printStackTrace();
        }
    }

    public static void commitInsertionTask()  throws SQLException {
        try {
            statement = connect.createStatement();
            statement.executeUpdate(String.format("%s%s;", insertStatementHead, insertStatementBody));
            statement.close();
            statement = null;
            insertStatementBody = null;
            insertRecordCount = 0;
        } catch (SQLException e ) {
            e.printStackTrace();
        }
    }

    public static String getPlaybackSQL(List<String> topics, String condition) {
        String outter = "select topic, message, create_time from ( %s ) t where 1 > 0 %s order by create_time;";
        String embed = "select '%s' as topic, message, create_time from %s " ;
        String ret = "";
        for(int i = 0; i < topics.size(); ++i) {
            ret = ret + String.format(embed, topics.get(i), topics.get(i).replaceAll("-", "\\$"));
            if(i != topics.size()-1) {
                ret = ret + "union all ";
            }
        }
        ret = String.format(outter, ret, condition);
        return ret;
    }

}
