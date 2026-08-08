# Big Data Import Implementation Plan

> **Superseded for remaining work:** use
> [`2026-08-08-async-import-architecture.md`](./2026-08-08-async-import-architecture.md).
> Completed Tasks 1–3 below remain historical.

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Build the first executable import architecture slice that integrates with EasyFile's existing extension model, starting with executor discovery and auto-registration.

**Architecture:** Reuse export's discovery and startup registration pattern: annotation -> support registry -> bean post-processor -> startup listener -> storage service hook. Keep import execution/storage internals incremental and backward compatible.

**Tech Stack:** Java 8+, Spring Boot starter auto-configuration, EasyFile core/storage modules, Maven multi-module build.

---

### Task 1: Import Executor Annotation and Registry

**Files:**
- Create: `easyfile-core/src/main/java/com/openquartz/easyfile/core/annotations/FileImportExecutor.java`
- Create: `easyfile-core/src/main/java/com/openquartz/easyfile/core/executor/support/FileImportExecutorSupport.java`

- [x] **Step 1: Add import executor annotation**

Create `FileImportExecutor` with:
- unique `value` as import code
- optional `desc`
- `enableNotify`
- `maxServerRetry`

- [x] **Step 2: Add import executor registry**

Create `FileImportExecutorSupport` with:
- `register(importCode, annotation, executor)`
- `get(importCode)`
- `contains(importCode)`
- `importCodeList()`
- `getImportCodeMap()`
- `executorList()`

- [x] **Step 3: Compile affected modules**

Run: `mvn -pl easyfile-core -am -DskipTests compile`
Expected: compile success for `easyfile-core` and dependencies.

### Task 2: Bean Discovery Integration

**Files:**
- Modify: `easyfile-spring-boot-starter/easyfile-spring-boot-starter-parent/src/main/java/com/openquartz/easyfile/starter/processor/EasyFileBeanEnhancePostProcessor.java`

- [x] **Step 1: Register import executors in post-processor**

Add discovery branch for classes annotated with `@FileImportExecutor`.

- [x] **Step 2: Validate type and uniqueness**

Validation:
- annotated class must implement `BaseImportExecutor`
- `importCode` must be unique, like export code uniqueness behavior

- [x] **Step 3: Compile starter parent module**

Run: `mvn -pl easyfile-spring-boot-starter/easyfile-spring-boot-starter-parent -am -DskipTests compile`
Expected: compile success.

### Task 3: Startup Auto-Registration for Import Tasks

**Files:**
- Create: `easyfile-spring-boot-starter/easyfile-spring-boot-starter-parent/src/main/java/com/openquartz/easyfile/starter/processor/AutoRegisteredImportTaskListener.java`
- Modify: `easyfile-spring-boot-starter/easyfile-spring-boot-starter-parent/src/main/java/com/openquartz/easyfile/starter/spring/boot/autoconfig/EasyFileEnhanceCreatorAutoConfiguration.java`
- Modify: `easyfile-storage/easyfile-storage-api/src/main/java/com/openquartz/easyfile/storage/imports/ImportStorageService.java`
- Modify: `easyfile-common/src/main/java/com/openquartz/easyfile/common/request/AutoTaskRegisterRequest.java`

- [x] **Step 1: Add import code map support in auto register request**

Extend `AutoTaskRegisterRequest` with `importCodeMap` field to avoid forcing a new request class in this slice.

- [x] **Step 2: Add ImportStorageService auto registration hook**

Add default method:
`default void autoRegisterTask(AutoTaskRegisterRequest request) { }`

- [x] **Step 3: Implement import startup listener**

Create `AutoRegisteredImportTaskListener`:
- on `ApplicationReadyEvent`, collect `FileImportExecutorSupport.getImportCodeMap()`
- build `AutoTaskRegisterRequest` with `appId`, `unifiedAppId`, `importCodeMap`
- call `importStorageService.autoRegisterTask(request)`

- [x] **Step 4: Register listener bean in auto-configuration**

In `EasyFileEnhanceCreatorAutoConfiguration`, register listener with
`@ConditionalOnProperty(prefix = EasyFileDownloadProperties.PREFIX, name = "enable-auto-register", havingValue = "true")`.

- [x] **Step 5: Compile changed modules**

Run: `mvn -pl easyfile-common,easyfile-storage/easyfile-storage-api,easyfile-spring-boot-starter/easyfile-spring-boot-starter-parent -am -DskipTests compile`
Expected: compile success.

### Task 4: Plan Follow-up for Storage and Trigger Execution

**Files:**
- Modify later in next slice:
  - `easyfile-storage/easyfile-storage-local/**`
  - `sql/storage.sql`
  - `easyfile-core` import async handler classes

- [x] **Step 1: Keep first slice limited**

Do not introduce DB schema or trigger execution classes in this commit.

- [ ] **Step 2: Record next slice entry points**

Document in commit message and handoff that next slice implements:
- `LocalImportStorageServiceImpl` relocation/alignment
- `ImportTriggerService`
- DB trigger schedule adapter
