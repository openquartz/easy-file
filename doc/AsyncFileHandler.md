## Asynchronous File Handler

**Class Inheritance Diagram**
![AbstractStreamDownloadExcelExecutor](image/AsyncFileHandlerAdapter.png)

The asynchronous file handler provides an adapter that needs to implement [com.openquartz.easyfile.core.executor.BaseAsyncFileHandler](file:///Users/jackxu/Documents/Code/github.com/openquartz/easy-file/easyfile-core/src/main/java/com/openquartz/easyfile/core/executor/BaseAsyncFileHandler.java#L13-L49).

The system provides a default adapter implementation. You only need to inherit from ([com.openquartz.easyfile.core.executor.AsyncFileHandlerAdapter](file:///Users/jackxu/Documents/Code/github.com/openquartz/easy-file/easyfile-core/src/main/java/com/openquartz/easyfile/core/executor/AsyncFileHandlerAdapter.java#L55-L440)) and provide the corresponding custom implementation.

The system supports three triggering methods (thread pool, DB-Schedule, DB-MQ).

### Thread Pool Handler

EasyFile also provides a default implementation ([com.openquartz.easyfile.starter.trigger.handler.DefaultAsyncFileHandler](file:///Users/jackxu/Documents/Code/github.com/openquartz/easy-file/easyfile-spring-boot-starter/easyfile-spring-boot-starter-parent/src/main/java/com/openquartz/easyfile/starter/trigger/handler/DefaultAsyncFileHandler.java#L29-L62)) using a thread pool to trigger asynchronous file processing.  
Additionally, the corresponding client configuration is provided; you need to configure (`easyfile.download.async-trigger-type=default`).

| Configuration Key                                                      | Description                                | Default Value |
| ------------------------------------------------------------------------ | ------------------------------------------- | -------------- |
| easyfile.default.async.download.handler.thread-pool.core-pool-size     | Core threads in default download thread pool | 10            |
| easyfile.default.async.download.handler.thread-pool.maximum-pool-size  | Max threads in default download thread pool | 20            |
| easyfile.default.async.download.handler.thread-pool.keep-alive-time    | Max idle time for default download thread pool (in seconds) | 30         |
| easyfile.default.async.download.handler.thread-pool.max-blocking-queue-size | Max blocking queue length for default download thread pool | 2048       |

### DB Trigger

Currently, only **local mode** is supported for file handlers. **Remote mode** is not yet supported. If used, it depends on the Maven POM:
```xml
<dependency>
    <groupId>com.openquartz</groupId>
    <artifactId>easyfile-spring-boot-starter-local</artifactId>
    <version>1.0.0</version>
</dependency>
```

To use a DB-triggered handler, database support is required. Therefore, execute the following SQL statement to create the table using the data source:

```sql
CREATE TABLE ef_async_download_trigger (
    id                BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT 'Id',
    register_id       BIGINT(20) NOT NULL DEFAULT -1 COMMENT 'Registration ID',
    trigger_status    VARCHAR(50) NOT NULL DEFAULT '' COMMENT 'Trigger status',
    start_time        DATETIME    NOT NULL COMMENT 'Start time',
    last_execute_time DATETIME    NOT NULL COMMENT 'Last execution time',
    trigger_count     INT(3) NOT NULL DEFAULT 0 COMMENT 'Trigger count',
    creating_owner    varchar(50) not null default '' comment 'Trigger owner',
    processing_owner  varchar(50) not null default '' comment 'Execution owner',
    UNIQUE `ux_register_id`(register_id),
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT 'Trigger Execution';
```

#### DB-Schedule Handler

##### DB + Schedule

Users can use the DB-based implementation (`com.openquartz.easyfile.starter.spring.boot.autoconfig.properties.ScheduleAsyncHandlerProperties`) by simply configuring the corresponding properties.

Default configurations are available; ensure `easyfile.download.async-trigger-type=schedule` is enabled.

```properties
easyfile.schedule.async.download.handler.enable=true
easyfile.schedule.async.download.handler.thread-pool-core-pool-size=2
easyfile.schedule.async.download.handler.thread-pool-thread-prefix=ScheduleAsyncHandler
easyfile.schedule.async.download.handler.max-execute-timeout=1600
easyfile.schedule.async.download.handler.max-trigger-count=5
easyfile.schedule.async.download.handler.schedule-period=10
easyfile.schedule.async.download.handler.trigger-offset=50
easyfile.schedule.async.download.handler.look-back-hours=2
easyfile.schedule.async.download.handler.max-archive-hours=24
```

| Configuration Key                                                      | Description                                | Default Value |
| ------------------------------------------------------------------------ | ------------------------------------------- | -------------- |
| easyfile.schedule.async.download.handler.enable                        | Whether to enable scheduled async handler   | false          |
| easyfile.schedule.async.download.handler.thread-pool-core-size         | Core threads per machine for scheduled async handler | 2      |
| easyfile.schedule.async.download.handler.thread-pool-thread-prefix     | Thread prefix for scheduled async handler   | ScheduleAsyncHandler |
| easyfile.schedule.async.download.handler.max-execute-timeout           | Max timeout for each scheduled execution (in seconds) | 1600  |
| easyfile.schedule.async.download.handler.max-trigger-count             | Max retry times for scheduling              | 5              |
| easyfile.schedule.async.download.handler.schedule-period               | Scheduling period (in seconds)              | 10             |
| easyfile.schedule.async.download.handler.trigger-offset                | Number of triggers per schedule run         | 50             |
| easyfile.schedule.async.download.handler.look-back-hours               | Time span for backtracking once (in hours)  | 2              |
| easyfile.schedule.async.download.handler.max-archive-hours             | Archive retention time after execution completed (in hours) | 24 |

##### Reaper Mechanism

![Reaper-Schedule](image/ReaperSchedule.png)

DB scheduling uses Reaper threads for scheduling, increasing high availability and scheduling efficiency while avoiding duplicate scheduling conflicts.

#### DB-MQ Handler

##### Disruptor + Compensation Mode

For disruptor and compensation mode, the system provides a disruptor-based trigger handler ([com.openquartz.easyfile.starter.trigger.handler.MqTriggerAsyncFileHandler](file:///Users/jackxu/Documents/Code/github.com/openquartz/easy-file/easyfile-spring-boot-starter/easyfile-spring-boot-starter-parent/src/main/java/com/openquartz/easyfile/starter/trigger/handler/MqTriggerAsyncFileHandler.java#L26-L106)).  
Configuration must be enabled with `easyfile.download.async-trigger-type=disruptor`.

```properties
easyfile.disruptor.async.download.handler.look-back-hours=2
easyfile.disruptor.async.download.handler.max-archive-hours=24
easyfile.disruptor.async.download.handler.max-execute-timeout=1600
easyfile.disruptor.async.download.handler.max-trigger-count=5
easyfile.disruptor.async.download.handler.ring-buffer-size=64
easyfile.disruptor.async.download.handler.schedule-period=10
easyfile.disruptor.async.download.handler.thread-pool-thread-prefix=DisruptorAsyncHandler
```

##### RocketMQ

For MQ handlers, the system provides a RocketMQ-based trigger handler ([com.openquartz.easyfile.starter.trigger.handler.MqTriggerAsyncFileHandler](file:///Users/jackxu/Documents/Code/github.com/openquartz/easy-file/easyfile-spring-boot-starter/easyfile-spring-boot-starter-parent/src/main/java/com/openquartz/easyfile/starter/trigger/handler/MqTriggerAsyncFileHandler.java#L26-L106)).  
Thus, the dependency jar (`rocket-client`) is required:

```xml
<dependency>
    <groupId>org.apache.rocketmq</groupId>
    <artifactId>rocketmq-client</artifactId>
    <version>4.9.3</version>
</dependency>
```

Also, enable the configuration: `easyfile.download.async-trigger-type=rocketmq`.

```properties
easyfile.rocketmq.async.download.handler.host=127.0.0.1.9876
easyfile.rocketmq.async.download.handler.topic=easyfile_mq_trigger
easyfile.rocketmq.async.download.handler.produce-group=p_async_handler_group
easyfile.rocketmq.async.download.handler.produce-latency-fault-enable=true
easyfile.rocketmq.async.download.handler.produce-timeout=1000
easyfile.rocketmq.async.download.handler.produce-try-times=5
easyfile.rocketmq.async.download.handler.consumer-group=c_async_handler_group
easyfile.rocketmq.async.download.handler.consumer-max-thread=3
easyfile.rocketmq.async.download.handler.consumer-min-thread=1
easyfile.rocketmq.async.download.handler.consume-concurrently-max-span=10
easyfile.rocketmq.async.download.handler.look-back-hours=2
easyfile.rocketmq.async.download.handler.offset=500
easyfile.rocketmq.async.download.handler.schedule-period=10
easyfile.rocketmq.async.download.handler.max-archive-hours=24
easyfile.rocketmq.async.download.handler.max-execute-timeout=1600
easyfile.rocketmq.async.download.handler.max-trigger-count=5
easyfile.rocketmq.async.download.handler.max-waiting-timeout=1600
```

##### Implementing Other MQ Triggers

1. Implement other MQ triggers by implementing [com.openquartz.easyfile.core.executor.trigger.MQTriggerProducer](file:///Users/jackxu/Documents/Code/github.com/openquartz/easy-file/easyfile-core/src/main/java/com/openquartz/easyfile/core/executor/trigger/MQTriggerProducer.java#L7-L16).

2. Implement custom logic for listening to MQ trigger messages.

3. Manually inject related classes into the Spring context.
Include implementations of the relevant classes in (`com.openquartz.easyfile.starter.spring.boot.autoconfig.MqAsyncFileHandlerAutoConfiguration`).


