package com.liveget.sink;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.sql.SQLException;
import java.time.Duration;
import java.util.*;

import com.liveget.sink.parser.EventTopic;
import com.liveget.sink.parser.EventType;
import com.liveget.sink.parser.Execution;
import com.liveget.sink.util.DateTimeUtils;
import com.liveget.sink.util.MySQLService;
import org.apache.log4j.Logger;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;

import org.apache.log4j.Logger;

public class LiveSink {

    private final static Logger logger = Logger.getLogger(LiveSink.class);


    // not intent to thread-safety
    private static LiveSink INSTANCE = null;

    // since this is a daily split sink, it need a current time
    private static String currentDateStr = "0000-00-00";

    // logger
    private static Properties configProperties = null;
    private static KafkaConsumer consumer = null;
    private static final Duration interval = Duration.ofMillis(1000);
    private static MySQLService dbService = null;
    private static String topic = null;


    /*
    1. create and init KafkaConsumer from property file
     */
    private LiveSink() throws SQLException, ClassNotFoundException {
        configProperties = new Properties();
        String bootstrapServer = System.getenv("BOOTSTRAP_SERVERS_CONFIG");
        if(bootstrapServer == null) {
            bootstrapServer = System.getenv("BOOTSTRAP_SERVICE_HOST") + ":" + System.getenv("BOOTSTRAP_SERVICE_PORT");
        }
        configProperties.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServer);
        configProperties.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.ByteArrayDeserializer");
        configProperties.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringDeserializer");
        // this group_id has to be <topic>_sink, so when restart the sink, it can continue to consume from the last time where it failed
        configProperties.put(ConsumerConfig.GROUP_ID_CONFIG, System.getenv("SINK_GROUP_ID"));
        consumer = new KafkaConsumer<>(configProperties);
        List<String> topicList = new ArrayList<>();
        // only 1 topic is allowed here
        topic = System.getenv("TOPIC");
        topicList.add(topic);
        consumer.subscribe(topicList);


        // instanciate MySQLService
        dbService = MySQLService.getInstance(
            System.getenv("DBHOST"),
            System.getenv("DBPORT"),
            System.getenv("DATABASE"),
            System.getenv("DBUSERNAME"),
            System.getenv("DBPASSWORD"));
    }

    private static LiveSink getInstance() throws SQLException, ClassNotFoundException {
        if(INSTANCE == null) {
            INSTANCE = new LiveSink();
        }
        return INSTANCE;
    }

    /*
    run LiveSink
    */
    private static void run() throws IOException, GeneralSecurityException {
        try {
            while(true) {
                ConsumerRecords records = consumer.poll(interval);
                for (Object record : records) {
                    // 1. parse record, get the table name, insert statement, current date
                    String recordDateStr = parseRecord((ConsumerRecord) record);
                    int compareResult = recordDateStr.compareToIgnoreCase(currentDateStr);
                    if (compareResult > 0) {
                        // new record Date is larger than current ingestion date
                        logger.info("new date, current:" + currentDateStr + " new record:" + recordDateStr);
                        currentDateStr = recordDateStr;
                        // 1.1 batch submit old data into data warehouse, and clear cache
                        if(!dbService.isCurrentInsertionQueueEmpty()) {
                            dbService.commitInsertionTask();
                            // 1.2 reset dbservice's insert statement and table
                            dbService.resetInsertStatement();
                            parseRecord((ConsumerRecord) record);
                        }
                        // 1.3 check kafka_sink_task_progress table, insert or update the table if necessary
                        processKafkaSinkTaskProgressTable();
                        // 1.4 check kafka_sink_table_fact table, insert a record if necessary

                        // 1.5 create kafka ingestion table if necessary
                        dbService.setCreateSinkTableStatement(Execution.getCreateSinkTableStatement());
                        dbService.executeSQLStatement(dbService.getCreateSinkTableStatement());
                    } else if (compareResult < 0) {
                        logger.info("time is not in order, current:" + currentDateStr + " new record:" + recordDateStr);
                    }
                    dbService.apppendInsertStatementBody(Execution.getSqlInsertValues());
                }
                // 4. finally batch insert data into the target ingestion table
                if(!dbService.isCurrentInsertionQueueEmpty()) {
                    logger.debug("insertion records: " + dbService.getInsertRecordCount());
                    dbService.commitInsertionTask();
                }
                // 5. never commit before successfully insert data into the data warehouse
                consumer.commitAsync();
            }
        } catch (Exception e) {
            logger.error(e.getStackTrace());
        } finally {
            consumer.close();
            System.out.println("Closed consumer gracefully!");
        }
    }

    private static void processKafkaSinkTaskProgressTable() throws SQLException {
        // 1. check if record exists
        if (dbService.checkExistsKafkaSinkTaskProgress(topic)) {
            // if exists, update the progres
            dbService.updateKafkaSinkTaskProgress(topic, DateTimeUtils.getCurrDateString(), DateTimeUtils.getCurrDateTimeString());
        } else {
            // if not exists, insert a record
            dbService.insertKafkaSinkTaskProgress(topic, DateTimeUtils.getCurrDateString(), DateTimeUtils.getCurrDateTimeString());
        }
    }

    private static void processKafkaSinkTableFactTable() throws SQLException {
        // check if record exists
        if(!dbService.checkExistsKafkaSinkTableFact(topic)) {
            // if not exists, insert one record to the table
            dbService.insertKafkaSinkTableFact(topic, DateTimeUtils.getCurrDateString());
        }
    }

    private static String parseRecord(ConsumerRecord msg) throws Exception {
        EventType eventType = EventTopic.parseEventTypeFromTopic(msg.topic());
        Execution.setEventType(eventType);
        String ret = DateTimeUtils.getCurrDateString();
        switch(eventType) {
            case PRICING:
                Execution.getPricing().setRecord(msg.value().toString());
                ret = Execution.getPricing().getSendDateStr();
                if(dbService.getInsertStatementHead() == null) {
                    if(dbService.getInsertStatementTableName() == null) {
                        dbService.setInsertStatementTableName(topic,  ret);
                    }
                    dbService.setInsertStatementHead(Execution.getPricing().getSQLInsertHead());
                }
                break;
            case SIGNAL:
                Execution.getSignal().setRecord(msg.value().toString());
                ret = Execution.getSignal().getIdentifiedDateStr();

                if(dbService.getInsertStatementHead() == null) {
                    if(dbService.getInsertStatementTableName() == null) {
                        dbService.setInsertStatementTableName(topic,  ret);
                    }
                    dbService.setInsertStatementHead(Execution.getPricing().getSQLInsertHead());
                }
                break;
            default:
                throw new Exception("EventListener::setRecord -> no such event type defined" + eventType.name());
        }
        return ret;
    }

    public static void main(String [] args) throws SQLException, ClassNotFoundException, IOException, GeneralSecurityException {
        LiveSink.getInstance().run();
    }

}
