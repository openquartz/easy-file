<div align=center><img src="/doc/image/logo.jpg"/></div>

<div align=center> Make BigData Export Easier </div>

# EasyFile

- English | [中文](./README.md)
- ![Stars](https://img.shields.io/github/stars/openquartz/easy-file?style=social)
- ![License](https://img.shields.io/github/license/openquartz/easy-file)

> Note: This project is not published to Maven Central yet. Please add it to your local/private repository.

**Home**: https://openquartz.github.io/  
**GitHub**: https://github.com/openquartz/easy-file

**One-liner**: An out-of-the-box solution for large data exports: sync/async, pagination/streaming, cache reuse, compression, i18n and observability. Stable, efficient, easy to integrate.

## Why EasyFile
- Large exports easily timeout and cause memory/CPU spikes impacting production stability
- Results are not reusable, leading to redundant compute and I/O waste
- Export tasks lack observability: no progress or metrics
- Developers must handle both query and file generation logic, increasing complexity

EasyFile provides standardized capabilities and best practices to export large files safely and reliably.

## Features
- Sync/Async export, paginated export, streaming export
- Export result cache reuse, file compression
- Multiple async triggering mechanisms
- i18n internationalization (dynamic headers by locale)
- Monitoring & metrics (Micrometer/Prometheus)
- Friendly APIs and configs; easy to adopt and extend

## Modes
- Local (recommended): provide local storage mappers; data stored in local DB; fewer dependencies
- Remote: deploy `easyfile-server` and call remotely; suitable for multi-service sharing and centralized management

## Quick Start
- Add dependency: publish the artifacts to your local/private repository
- Choose a mode: include the corresponding Starter (Local/Remote)
- Implement an export executor: write download logic using the unified API
- Run and verify: start sample or your app; check export and progress

See [Quick Start](doc/QuickStart.md) and sample projects.

## Relation to EasyExcel
- EasyExcel focuses on Excel file I/O and parsing
- EasyFile focuses on the stability and observability of the “export process” for large files; they can work together
- Use EasyExcel or other libs for file generation; EasyFile governs the export process and reuse

Comparison and effects:
1. Full query of 1M + EasyExcel export (`com.openquartz.easyfile.example.downloader.StudentDownloadDemoExecutor`)  
![Full Export + EasyExcel](./doc/image/FullDownloadMemory.png)
2. Paginated export (`com.openquartz.easyfile.example.downloader.StudentPageDownloadDemoExecutor`)  
![Paginated Export](./doc/image/PageDownloadMemory.png)
File size comparison:  
![File Size Comparison](./doc/image/PageDownloadSize2FullDownloadSize.png)

## Architecture & Modules
- easyfile-common: common
- easyfile-core: core
- easyfile-metrics: metrics
  - easyfile-metrics-api
  - easyfile-metrics-promethes
- easyfile-storage: storage
  - easyfile-storage-api
  - easyfile-storage-remote
  - easyfile-storage-local
- easyfile-spring-boot-starter: starter collection
  - parent / local / remote
- easyfile-server: remote storage server
- easyfile-ui: admin UI (optional)
- easyfile-example: examples (local / remote)

Sequence diagram:  
![Download Sequence](./doc/image/sequence.png)

## Docs & Resources
- Quick Start: doc/QuickStart.md
- Examples: easyfile-example
- Metrics: easyfile-metrics (Micrometer/Prometheus)
- Home: https://openquartz.github.io/

## Contributing & Promotion
- If you find it helpful, please Star the project
- Register as an EasyFile user: https://github.com/openquartz/easy-file/issues/1
- Submit Issues/PRs to grow the ecosystem

<div align="center">

[![Star History Chart](https://api.star-history.com/svg?repos=openquartz/easy-file&type=Date)](https://www.star-history.com/#openquartz/easy-file&Date)

</div>
