<div align="center"><img src="/doc/image/logo.jpg"/></div>
<div align="center">Make BigData Export Easier</div>

# EasyFile

- [English](./README.md) | 中文
- ![Stars](https://img.shields.io/github/stars/openquartz/easy-file?style=social)
- ![License](https://img.shields.io/github/license/openquartz/easy-file)

> 本项目暂未发布到 Maven 中央仓库，可通过本地/私有仓库引入。

**主页**：https://openquartz.github.io/  
**GitHub**：https://github.com/openquartz/easy-file

**一句话简介**：面向大数据导出的开箱即用解决方案，覆盖同步/异步、分页/流式、缓存复用、压缩、国际化与可观测性，稳定高效，易集成。

## 为什么选择 EasyFile
- 大数据量导出易超时、内存/CPU 瞬时飙高，影响线上稳定性
- 结果不可复用、重复计算与 I/O 浪费
- 导出任务不可观测，缺少进度与监控
- 业务需同时关注查询与文件生成，开发复杂度高

EasyFile 针对以上痛点提供标准化能力与最佳实践，帮助你安全、稳定地完成大文件导出。

## 功能特性
- 同步/异步导出、分页导出、流式导出
- 导出结果缓存复用、文件压缩
- 多种异步触发机制
- i18n 国际化（按语言环境动态表头）
- 监控与指标（Micrometer/Prometheus）
- 代码与配置双友好，易于落地与扩展

## 模式选择
- Local（推荐）：提供本地存储 Mapper，数据落地本地数据库，快速启用、依赖少
- Remote：部署 easyfile-server，客户端远程调用，适合多服务共享与集中管控

## 快速开始
- 添加依赖：将工程发布到你本地或私有仓库
- 选择模式：引入对应 Starter（Local/Remote）
- 实现导出执行器：基于统一 API 编写下载逻辑
- 启动并验证：运行示例或你的项目，查看导出与进度

详见 [快速开始文档](doc/QuickStart_zh.md) 与示例工程。

## 与 EasyExcel 的关系
- EasyExcel 专注 Excel 文件读写与解析
- EasyFile 专注“大文件导出过程”的稳定性与可观测，二者可协同
- 文件生成可选用 EasyExcel 等库，EasyFile 负责过程治理与复用

对比示例与效果：
1. 全量 100w + EasyExcel 导出（`com.openquartz.easyfile.example.downloader.StudentDownloadDemoExecutor`）  
![全量导出+EasyExcel](./doc/image/FullDownloadMemory.png)
2. 分页导出（`com.openquartz.easyfile.example.downloader.StudentPageDownloadDemoExecutor`）  
![分页导出](./doc/image/PageDownloadMemory.png)
文件大小对比：  
![文件大小对比图](./doc/image/PageDownloadSize2FullDownloadSize.png)

## 架构与模块
- easyfile-common：公共模块
- easyfile-core：核心能力
- easyfile-metrics：指标
  - easyfile-metrics-api
  - easyfile-metrics-promethes
- easyfile-storage：存储
  - easyfile-storage-api
  - easyfile-storage-remote
  - easyfile-storage-local
- easyfile-spring-boot-starter：Starter 集合
  - parent / local / remote
- easyfile-server：远程存储服务端
- easyfile-ui：管理端（可选）
- easyfile-example：示例（local / remote）

时序图：  
![下载时序图](./doc/image/sequence.png)

## 文档与资源
- 快速开始：doc/QuickStart_zh.md
- 示例工程：easyfile-example
- 指标与监控：easyfile-metrics（Micrometer/Prometheus）
- 主页与更多资料：https://openquartz.github.io/

## 参与与推广
- 觉得有帮助，欢迎为项目 Star
- 注册成为 EasyFile 用户：https://github.com/openquartz/easy-file/issues/1
- 提交 Issue/PR，一起完善生态

<div align="center">

[![Star History Chart](https://api.star-history.com/svg?repos=openquartz/easy-file&type=Date)](https://www.star-history.com/#openquartz/easy-file&Date)

</div>
