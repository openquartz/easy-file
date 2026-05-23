package com.openquartz.easyfile.server.service;

import com.openquartz.easyfile.common.request.ImportRegisterRequest;
import com.openquartz.easyfile.common.request.ImportResultQueryRequest;
import com.openquartz.easyfile.common.response.AppTree;
import com.openquartz.easyfile.common.response.DownloadUrlResult;
import com.openquartz.easyfile.common.response.ImportResult;
import com.openquartz.easyfile.common.util.page.Pagination;

/**
 * 导入服务接口
 * 定义导入任务管理的核心业务方法
 *
 * @author svnee
 */
public interface ImportService {

    /**
     * 注册导入任务
     *
     * @param request 导入注册请求
     * @return 导入任务注册ID
     */
    Long registerImport(ImportRegisterRequest request);

    /**
     * 执行导入任务
     *
     * @param registerId 注册ID
     * @return 是否执行成功
     */
    boolean executeImport(Long registerId);

    /**
     * 查询导入状态
     *
     * @param registerId 注册ID
     * @return 导入状态信息
     */
    ImportResult queryImportStatus(Long registerId);

    /**
     * 查询导入进度
     *
     * @param registerId 注册ID
     * @return 导入进度信息
     */
    ImportResult queryImportProgress(Long registerId);

    /**
     * 分页查询导入记录
     *
     * @param request 查询请求
     * @return 导入记录分页结果
     */
    Pagination<ImportResult> listImportRecords(ImportResultQueryRequest request);

    /**
     * 重试失败的导入任务
     *
     * @param registerId 注册ID
     * @param operator 操作人
     * @return 是否重试成功
     */
    boolean retryImport(Long registerId, String operator);

    /**
     * 撤销导入任务
     *
     * @param registerId 注册ID
     * @param operator 操作人
     * @return 是否撤销成功
     */
    boolean revokeImport(Long registerId, String operator);

    /**
     * 获取导入结果文件
     *
     * @param registerId 注册ID
     * @return 结果文件下载信息
     */
    DownloadUrlResult getResultFile(Long registerId);

    /**
     * 获取应用树结构
     *
     * @return 应用树列表
     */
    java.util.List<AppTree> getAppTree();
}
