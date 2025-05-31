<div align=center><img src="/doc/image/logo.jpg"/></div>

<div align=center> Make BigData Export Easier!!! </div>

-------

# EasyFile

[中文版本](./README_zh.md)

Make BigData Export Easier!!!

> **Note**: This project has not been published to the Maven central repository and needs to be manually added to the local or private repository.

## Welcome to Star!!!

**[Home](https://openquartz.github.io/)** \
**[GitHub](https://github.com/openquartz/easy-file)**

### Introduction

#### What is EasyFile?

EasyFile - aims to provide a more convenient file service, offering an integrated Web solution for exporting large files. It allows effortless export of data sets with millions of records or more.

#### Key Features

Supports (synchronous/asynchronous) export, file compression, streaming export, paginated export, export cache reuse, multi-group paginated export, multi-group streaming export, and multiple asynchronous triggering mechanisms. Also supports i18n internationalized exports (displaying different Excel headers based on language environments).

Optimized to reduce memory and CPU impact during file exports. Additional management capabilities are available for file services.

Provides developers with a more general, fast, and unified API implementation approach.

### Problems Solved

1. High memory consumption due to instantaneous loading of large volumes of data, leading to potential machine crashes.
2. Large files may cause HTTP timeouts, resulting in failed exports.
3. Export results under identical conditions cannot be reused, causing redundant generation and resource waste.
4. No monitoring mechanism when export tasks occur in batches.
5. Developers must handle both data query logic and file generation logic.
6. No visibility into execution progress for long-running export tasks.

### Framework Comparison

Compared to Alibaba's EasyExcel, they focus on different areas.

Alibaba EasyExcel is a tool for generating, exporting, and parsing Excel files.

EasyFile is a comprehensive solution for exporting large files, designed to solve issues like file reuse, export timeouts, out-of-memory errors, and sudden spikes in CPU/memory usage during large file exports.

Moreover, EasyFile is not limited to Excel exports; it can also manage CSV, PDF, Word, etc. (users need to integrate the base export/downloader class [BaseDownloadExecutor](file:///Users/jackxu/Documents/Code/github.com/openquartz/easy-file/easyfile-core/src/main/java/com/openquartz/easyfile/core/executor/BaseDownloadExecutor.java#L14-L67) to implement file generation logic).

Furthermore, EasyFile does not conflict with Alibaba EasyExcel and can be used together. You can extend the file generation logic using Alibaba EasyExcel.

1. Full query of 1 million records + export via EasyExcel ([com.openquartz.easyfile.example.downloader.StudentDownloadDemoExecutor](file:///Users/jackxu/Documents/Code/github.com/openquartz/easy-file/easyfile-example/easyfile-example-local/src/main/java/com/openquartz/easyfile/example/downloader/StudentDownloadDemoExecutor.java#L20-L47))
Memory chart:
![Full Export + EasyExcel](./doc/image/FullDownloadMemory.png)

2. Paginated export ([com.openquartz.easyfile.example.downloader.StudentPageDownloadDemoExecutor](file:///Users/jackxu/Documents/Code/github.com/openquartz/easy-file/easyfile-example/easyfile-example-local/src/main/java/com/openquartz/easyfile/example/downloader/StudentPageDownloadDemoExecutor.java#L23-L67))
Memory chart:
![Paginated Export](./doc/image/PageDownloadMemory.png)

File size comparison:
![File Size Comparison Chart](./doc/image/PageDownloadSize2FullDownloadSize.png)

### Software Architecture

EasyFile provides two modes:

**Local Mode (Recommended)**: Requires providing local API storage mappers. Data will be stored and managed in a local database.

**Remote Mode**: Requires deploying the `easyfile-server` service and setting the client to call the remote EasyFile domain.

### Code Structure

- `easyfile-common`: Public module service
- `easyfile-core`: Core service
- `easyfile-metrics`: Metrics support
    - `easyfile-metrics-api`: Metrics API protocol
    - `easyfile-metrics-prometheus`: Prometheus metrics implementation
- `easyfile-storage`: Storage service
    - `easyfile-storage-api`: Storage service API
    - `easyfile-storage-remote`: Remote storage calls
    - `easyfile-storage-local`: Local data source storage

- `easyfile-spring-boot-starter`: Collection of EasyFile starter modules
    - `easyfile-spring-boot-starter-parent`: Parent project for EasyFile starter
    - `easyfile-spring-boot-starter-local`: Starter package for Local mode
    - `easyfile-spring-boot-starter-remote`: Starter package for Remote mode

- `easyfile-server`: Remote storage server for EasyFile

- `easyfile-ui`: EasyFile-admin UI management service (optional)

- `easyfile-example`: Sample projects
    - `easyfile-example-local`: Local storage sample project
    - `easyfile-example-remote`: Remote storage sample project

### Sequence Diagram

![Download Sequence Diagram](./doc/image/sequence.png)

### Quick Start

[Quick Start Guide](doc/QuickStart.md)

### Promotion

If you also think this project has helped you, welcome to sign up for promotion!

[Click to register as an EasyFile user!](https://github.com/openquartz/easy-file/issues/1)

ღ( ´・ᴗ・` )ღ Many thanks to the following registered users. ღ( ´・ᴗ・` )ღ
