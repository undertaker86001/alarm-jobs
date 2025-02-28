package com.sucheon.alarm.functions;

import com.sucheon.alarm.event.RuleMatchResult;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.runtime.state.FunctionInitializationContext;
import org.apache.flink.runtime.state.FunctionSnapshotContext;
import org.apache.flink.streaming.api.checkpoint.CheckpointedFunction;
import org.apache.flink.streaming.api.functions.sink.RichSinkFunction;
import org.apache.flink.streaming.api.functions.sink.SinkFunction;
import org.apache.flink.table.api.TableSchema;
import org.apache.flink.types.Row;
import org.apache.flink.util.Preconditions;
import org.apache.flink.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.yandex.clickhouse.BalancedClickhouseDataSource;
import ru.yandex.clickhouse.settings.ClickHouseProperties;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.Arrays;
import java.util.Properties;

public class ClickHouseAppendSinkFunction extends RichSinkFunction<Row> implements CheckpointedFunction {
    private static final String USERNAME = "user";
    private static final String PASSWORD = "password";

    private static final Logger log = LoggerFactory.getLogger(ClickHouseAppendSinkFunction.class);
    private static final long serialVersionUID = 1L;

    private Connection connection;
    private BalancedClickhouseDataSource dataSource;
    private PreparedStatement pstat;

    private String address;
    private String username;
    private String password;

    private String database;
    private String table;

    private String prepareStatement;
    private Integer batchSize;
    private Long commitPadding;

    private Integer retries;
    private Long retryInterval;

    private Boolean ignoreInsertError;

    private Integer currentSize;
    private Long lastExecuteTime;

    public ClickHouseAppendSinkFunction(String address, String username, String password, String database, String table, TableSchema tableSchema, Integer batchSize, Long commitPadding, Integer retries, Long retryInterval, Boolean ignoreInsertError) {
        Preconditions.checkArgument(!StringUtils.isNullOrWhitespaceOnly(address), "请检查clickhouse url是否填写");
        Preconditions.checkArgument(!StringUtils.isNullOrWhitespaceOnly(username), "请检查clickhouse username是否填写！");
        Preconditions.checkArgument(!StringUtils.isNullOrWhitespaceOnly(password), "请检查clickhouse password是否填写！");
        Preconditions.checkArgument(!StringUtils.isNullOrWhitespaceOnly(username), "请检查clickhouse database是否填写！");
        Preconditions.checkArgument(!StringUtils.isNullOrWhitespaceOnly(username), "请检查clickhouse table是否填写!");

        this.address = address;
        this.database = database;
        this.table = table;
        this.username = username;
        this.password = password;
        this.prepareStatement = createPrepareStatement(tableSchema, database, table);
        this.batchSize = batchSize;
        this.commitPadding = commitPadding;
        this.retries = retries;
        this.retryInterval = retryInterval;
        this.ignoreInsertError = ignoreInsertError;
    }

    @Override
    public void open(Configuration parameters) throws Exception {
        super.open(parameters);
        Properties properties = new Properties();
        properties.setProperty(USERNAME, username);
        properties.setProperty(PASSWORD, password);
        ClickHouseProperties clickHouseProperties = new ClickHouseProperties(properties);
        dataSource = new BalancedClickhouseDataSource(address, clickHouseProperties);
        connection = dataSource.getConnection();
        pstat = connection.prepareStatement(prepareStatement);
        lastExecuteTime = System.currentTimeMillis();
        currentSize = 0;

    }

    @Override
    public void invoke(Row value, Context context) throws Exception {
        for (int i = 0; i < value.getArity(); i++) {
            pstat.setObject(i + 1, value.getField(i));
        }
        pstat.addBatch();
        currentSize++;
        if (currentSize >= batchSize || (System.currentTimeMillis() - lastExecuteTime) > commitPadding) {
            try {
                doExecuteRetries(retries, retryInterval);
            } catch (Exception e) {
                log.error("clickhouse-insert-error ( maxRetries:" + retries + " , retryInterval : " + retryInterval + " millisecond )" + e.getMessage());
            } finally {
                pstat.clearBatch();
                currentSize = 0;
                lastExecuteTime = System.currentTimeMillis();
            }
        }
    }

    public void doExecuteRetries(int count, long retryInterval) throws Exception {

        int retrySize = 0;
        Exception resultException = null;
        for (int i = 0; i < count; i++) {
            try {
                pstat.executeBatch();
                break;
            } catch (Exception e) {
                retrySize++;
                resultException = e;
            }
            try {
                Thread.sleep(retryInterval);
            } catch (InterruptedException e) {
                log.error("clickhouse retry interval exception : ",e);
            }
        }
        if (retrySize == count && !ignoreInsertError) {
            throw resultException;
        }
    }

    @Override
    public void snapshotState(FunctionSnapshotContext functionSnapshotContext) throws Exception {
        doExecuteRetries(retries, retryInterval);
    }

    @Override
    public void initializeState(FunctionInitializationContext functionInitializationContext) throws Exception {

    }

    public String createPrepareStatement(TableSchema tableSchema, String database, String table) {
        String[] fieldNames = tableSchema.getFieldNames();
        String columns = String.join(",", fieldNames);
        String questionMarks = Arrays.stream(fieldNames)
                .map(field -> "?")
                .reduce((left,right) -> left+","+right)
                .get();
        StringBuilder builder = new StringBuilder("insert into ");
        builder.append(database).append(".")
                .append(table).append(" ( ")
                .append(columns).append(" ) values ( ").append(questionMarks).append(" ) ");
        return builder.toString();

    }

}

