## Downloader

Implement the interface: [com.openquartz.easyfile.core.executor.BaseDownloadExecutor](file:///Users/jackxu/Documents/Code/github.com/openquartz/easy-file/easyfile-core/src/main/java/com/openquartz/easyfile/core/executor/BaseDownloadExecutor.java#L14-L67)

And inject it into the Spring ApplicationContext, using the annotation [com.openquartz.easyfile.core.annotations.FileExportExecutor](file:///Users/jackxu/Documents/Code/github.com/openquartz/easy-file/easyfile-core/src/main/java/com/openquartz/easyfile/core/annotations/FileExportExecutor.java#L37-L72).

If synchronous export is required, you need to set the file's HttpResponse headers by implementing the interface [com.openquartz.easyfile.core.executor.BaseWrapperSyncResponseHeader](file:///Users/jackxu/Documents/Code/github.com/openquartz/easy-file/easyfile-core/src/main/java/com/openquartz/easyfile/core/executor/BaseWrapperSyncResponseHeader.java#L10-L19).

Example:

```java
import org.springframework.stereotype.Component;
import com.openquartz.easyfile.core.annotations.FileExportExecutor;
import com.openquartz.easyfile.common.bean.DownloaderRequestContext;
import com.openquartz.easyfile.core.executor.BaseDownloadExecutor;
import com.openquartz.easyfile.core.executor.BaseWrapperSyncResponseHeader;

@Component
@FileExportExecutor("ExampleExcelExecutor")
public class ExampleExcelExecutor implements BaseDownloadExecutor, BaseWrapperSyncResponseHeader {

  @Override
  public boolean enableAsync(DownloaderRequestContext context) {
    // Determine whether asynchronous mode is enabled
    return true;
  }

  @Override
  public void export(DownloaderRequestContext context) {
      // Logic for generating file download
  }
}
```

Class Inheritance Diagram:
![AbstractStreamDownloadExcelExecutor](image/AbstractStreamDownloadExcelExecutor.png)

#### Overview of Downloaders

1. **Pagination Download Support**

`com.openquartz.easyfile.core.executor.PageShardingDownloadExecutor`

Provides more convenient pagination support:

`com.openquartz.easyfile.core.executor.impl.AbstractPageDownloadExcelExecutor`

Must be used in conjunction with (`com.openquartz.easyfile.common.annotations.ExcelProperty`)

Multi-sheet group download support:
`com.openquartz.easyfile.core.executor.impl.AbstractMultiSheetPageDownloadExcelExecutor`

2. **Streaming Download Support**

`com.openquartz.easyfile.core.executor.StreamDownloadExecutor`

Provides more convenient streaming support:

`com.openquartz.easyfile.core.executor.impl.AbstractStreamDownloadExcelExecutor`

Multi-sheet group download support:
`com.openquartz.easyfile.core.executor.impl.AbstractMultiSheetStreamDownloadExcelExecutor`

Needs to be used in conjunction with (`com.openquartz.easyfile.common.annotations.ExcelProperty`)

#### Excel Enhancer Support

The system provides extended enhancement support for Excel files generated via framework exports, enabling users to enhance custom functionalities such as encryption, read-only locks, and watermarks.

Users can implement [com.openquartz.easyfile.core.executor.excel.ExcelIntensifier](file:///Users/jackxu/Documents/Code/github.com/openquartz/easy-file/easyfile-core/src/main/java/com/openquartz/easyfile/core/executor/excel/ExcelIntensifier.java#L9-L27) to create their own enhancements. For example:

```java
package com.openquartz.easyfile.example.excel;

import java.io.IOException;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import com.openquartz.easyfile.common.bean.BaseDownloaderRequestContext;
import com.openquartz.easyfile.example.utils.WaterMarkExcelUtil;
import com.openquartz.easyfile.core.executor.excel.ExcelIntensifier;

/**
 * Enhance Watermark
 * @author svnee
 **/
@Slf4j
public class WaterMarkExcelIntensifier implements ExcelIntensifier {

  @Override
  public void enhance(Workbook workbook, BaseDownloaderRequestContext context) {

        if (workbook instanceof XSSFWorkbook) {
            try {
                WaterMarkExcelUtil.printWaterMark((XSSFWorkbook) workbook, "Sensitive Content");
            } catch (IOException e) {
                log.error("[WaterMarkExcelIntensifier#enhance]", e);
            }
        } else if (workbook instanceof SXSSFWorkbook) {
            try {
                WaterMarkExcelUtil.printWaterMark((SXSSFWorkbook) workbook, "Sensitive Content");
            } catch (IOException e) {
                log.error("[WaterMarkExcelIntensifier#enhance]", e);
            }
        } else {
            throw new RuntimeException("This workbook does not support watermark!");
        }
    }
}
```

When using, override to provide an Excel enhancer:
```java
@Component
@FileExportExecutor(value = "StudentPageDownloadDemoExecutor")
public class StudentPageDownloadDemoExecutor extends AbstractPageDownloadExcelExecutor<Student> {

  @Resource
  private StudentMapper studentMapper;

  @Override
  public boolean enableAsync(BaseDownloaderRequestContext context) {
    return true;
  }

  @Override
  public String sheetPrefix() {
    return "Student Information";
  }

  @Override
  public PageTotal count(Map<String, Object> othersMap) {
    if (PageTotalContext.currentPageToTal(sheetPrefix()) != null) {
      return PageTotalContext.currentPageToTal(sheetPrefix());
    }
    int count = studentMapper.count();
    PageTotalContext.cache(sheetPrefix(), PageTotal.of(count, 100));
    return PageTotalContext.currentPageToTal(sheetPrefix());
  }

  @Override
  public Pair<Long, List<Student>> shardingData(BaseDownloaderRequestContext context, Page page, Long cursorId) {
    List<Student> studentList = studentMapper.findByMinIdLimit(cursorId, page.getPageSize());
    if (CollectionUtils.isEmpty(studentList)) {
      return Pair.of(cursorId, studentList);
    }
    cursorId = studentList.get(studentList.size() - 1).getId();
    return Pair.of(cursorId, studentList);
  }

  /**
   * Print watermark
   */
  @Override
  public List<ExcelIntensifier> enhanceExcel() {
    return Collections.singletonList(new WaterMarkExcelIntensifier());
  }
}
```

#### Rate-Limiting Executor

To apply rate limiting, implement `ExportLimitingExecutor`.

```java
package com.openquartz.easyfile.storage.expand;

import com.openquartz.easyfile.common.request.ExportLimitingRequest;

/**
 * Rate Limiting Service
 *
 * @author svnee
 */
public interface ExportLimitingExecutor {

  /**
   * Strategy
   *
   * @return Strategy code
   */
  String strategy();

  /**
   * Apply rate limiting
   *
   * @param request Request object
   */
  void limit(ExportLimitingRequest request);
}
```

#### Enable Caching

Export result caching aims to reuse large file exports and reduce unnecessary repeated exports of the same data, thus reusing successfully exported results as much as possible.

Exported data can mainly be categorized into three types:
1. Static Data (past data that no longer changes; multiple exports under the same conditions yield consistent results).
2. Dynamic Data (data currently changing; multiple exports under the same conditions yield inconsistent results).
3. A combination of static and dynamic data (some data unchanged while some continues to change).

Caching is primarily applicable to the first scenario.

1. To enable caching during implementation, override the cache-enabling method:

```java
/**
 * Enable export caching
 *
 * @param context Context
 * @return Whether to enable caching
 */
default boolean enableExportCache(BaseDownloaderRequestContext context){
        return false;
}
```

2. Provide a key for determining cache identity—used to check consistency. If the cache-key is empty, the cache will match all.

```java
/**
 * File Export Executor
 *
 * @author svnee
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
public @interface FileExportExecutor {

  /**
   * Executor code
   */
  String value();

  /**
   * Chinese description of the executor
   * Defaults to {@link #value()}
   */
  String desc() default "";

  /**
   * Whether to enable notifications
   */
  boolean enableNotify() default false;

  /**
   * Maximum server retry attempts
   * No retries when less than or equal to 0.
   */
  int maxServerRetry() default 0;

  /**
   * Cache Key
   *
   * @see BaseDownloaderRequestContext#getOtherMap() for the corresponding value of the key
   * If available, use dot notation to point to the final data field. E.g., a.b.c
   * Supports SpringEL expressions
   */
  String[] cacheKey() default {};
}

// Example usage
@FileExportExecutor(value = "studentStreamDownloadDemo", desc = "Student Export", cacheKey = {"#request.age"})
```

#### Sub-column Cell Export Support

Currently, mapping exports for 1:* are only supported up to two levels; three or more levels (e.g., 1:*:*) are not supported yet.

Excel supports exporting data cells in rows and columns for 1:* mappings. For example:
![MergeCellSheet](image/img.png)

However:
1. When exporting 1:* data, it is not recommended to export excessive amounts of data due to merge cell operations, which may significantly slow down Excel generation. It is advisable to keep row counts below 2K.
2. For very large datasets, it is recommended to use 1:1 cell exports:
![MergeCellSheet](image/one2one.png)

#### Multi-Sheet Group Export Support

For exporting grouped data across multiple sheets, EasyFile provides two executors:

- Streaming Multi-Sheet Group Export:
  [com.openquartz.easyfile.core.executor.impl.AbstractMultiSheetStreamDownloadExcelExecutor](file:///Users/jackxu/Documents/Code/github.com/openquartz/easy-file/easyfile-core/src/main/java/com/openquartz/easyfile/core/executor/impl/AbstractMultiSheetStreamDownloadExcelExecutor.java#L31-L140)

- Pagination-based Multi-Sheet Group Export:
  [com.openquartz.easyfile.core.executor.impl.AbstractMultiSheetPageDownloadExcelExecutor](file:///Users/jackxu/Documents/Code/github.com/openquartz/easy-file/easyfile-core/src/main/java/com/openquartz/easyfile/core/executor/impl/AbstractMultiSheetPageDownloadExcelExecutor.java#L34-L130)

#### Optimization Recommendations

EasyFile offers two processing methods for large file exports: paginated and streaming exports.

1. For large file exports, it is recommended to set a relatively high maximum number of rows per sheet, even up to the maximum allowed by Excel 2007 versions, to avoid frequent sheet creation leading to memory leaks and OOMs.
   Configuration: `easyfile.download.excel-max-sheet-rows`

2. When exporting large files, appropriately configure the number of rows cached in memory based on your system’s memory size. Avoid setting this too high.
   Configuration: `easyfile.download.excel-row-access-window-size`
   Setting this value too high puts pressure on memory; setting it too low increases disk I/O frequency and CPU usage.

3. Reasonably configure the number of rows fetched per query for paginated/streaming exports.
   - For paginated exports, pay attention to the page size configuration.
   - For streaming exports, pay attention to the enhanced data cache length configured via `com.openquartz.easyfile.core.executor.impl.AbstractStreamDownloadExcelExecutor.enhanceLength`.

#### Memory Performance Validation

Using local storage mode with JVM options `-Xms512M -Xmx512M -Xmn512M -Xss1M -XX:MetaspaceSize=256M -XX:MaxMetaspaceSize=256M`.

Exporting 1 million rows resulting in a 30079KB Excel file (2007 version), with buffer sizes set to 100 for both paginated and streaming exports.

Configuration:
```properties
easyfile.download.excel-max-sheet-rows=10000000
easyfile.download.excel-row-access-window-size=100
```

Memory usage for paginated exports:
![Paginated Export Memory Usage](image/PageExport.png)

Memory usage for streaming exports:
![Streaming Export Memory Usage](image/StreamExport.png)

#### Asynchronous Export Progress Support

For large asynchronous file exports, real-time progress reporting is supported through the entry point `com.openquartz.easyfile.core.executor.ExecuteProcessProbe.report`. Default implementations for streaming and paginated exports already support automatic progress reporting.

Custom implementations of [com.openquartz.easyfile.core.executor.BaseDownloadExecutor](file:///Users/jackxu/Documents/Code/github.com/openquartz/easy-file/easyfile-core/src/main/java/com/openquartz/easyfile/core/executor/BaseDownloadExecutor.java#L14-L67) require manual invocation of the progress-reporting API. The progress will be displayed in the "Execution Progress" column within the `EasyFile UI Management Interface`.

### Extension Points

#### Downloader Event Listener Support

After users submit an export request, `easy-file` publishes export start events ([com.openquartz.easyfile.core.intercept.listener.DownloadStartEvent](file:///Users/jackxu/Documents/Code/github.com/openquartz/easy-file/easyfile-core/src/main/java/com/openquartz/easyfile/core/intercept/listener/DownloadStartEvent.java#L10-L68)) before and after the export begins.

Users can customize the implementation of the interface [com.openquartz.easyfile.core.intercept.listener.DownloadStartListener](file:///Users/jackxu/Documents/Code/github.com/openquartz/easy-file/easyfile-core/src/main/java/com/openquartz/easyfile/core/intercept/listener/DownloadStartListener.java#L7-L16) and inject it into the Spring factory to handle these events.

Upon completion of the export submission, `easy-file` also publishes an export completion event ([com.openquartz.easyfile.core.intercept.listener.DownloadEndEvent](file:///Users/jackxu/Documents/Code/github.com/openquartz/easy-file/easyfile-core/src/main/java/com/openquartz/easyfile/core/intercept/listener/DownloadEndEvent.java#L10-L91)). Users can customize the implementation of the interface [com.openquartz.easyfile.core.intercept.listener.DownloadEndListener](file:///Users/jackxu/Documents/Code/github.com/openquartz/easy-file/easyfile-core/src/main/java/com/openquartz/easyfile/core/intercept/listener/DownloadEndListener.java#L7-L16) and inject it into the Spring factory.

**Note**: For asynchronous downloads, the end event ([com.openquartz.easyfile.core.intercept.listener.DownloadEndEvent](file:///Users/jackxu/Documents/Code/github.com/openquartz/easy-file/easyfile-core/src/main/java/com/openquartz/easyfile/core/intercept/listener/DownloadEndEvent.java#L10-L91)) occurs after submitting the async request, not after execution completes. For synchronous downloads, the event is published after the export method completes.

**Use Cases**: Custom business monitoring, timing metrics; traffic throttling interception for user submissions; special export parameter interception (e.g., security validation), etc.

#### Asynchronous Downloader Execution Interception Support

During actual logic processing for asynchronous downloads, `easy-file` provides interception support. Users can customize the implementation of the interface `com.openquartz.easyfile.core.intercept.DownloadExecutorInterceptor` and inject it into the Spring factory.

If there are multiple interceptors, the order of execution for pre-interceptors is from smallest to largest, while post-interceptors execute from largest to smallest.

**Use Cases**: Switching data sources before execution, read-write separation, custom business monitoring, unified logging, etc.