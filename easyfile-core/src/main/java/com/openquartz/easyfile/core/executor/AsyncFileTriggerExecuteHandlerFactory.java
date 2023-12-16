package com.openquartz.easyfile.core.executor;

import com.openquartz.easyfile.common.bean.IRequest;
import com.openquartz.easyfile.common.bean.Pair;
import com.openquartz.easyfile.common.exception.Asserts;
import com.openquartz.easyfile.common.exception.EasyFileException;
import com.openquartz.easyfile.common.util.ExceptionUtils;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import static com.openquartz.easyfile.core.exception.ExecutorErrorCode.EXECUTOR_DUPLICATE;
import static com.openquartz.easyfile.core.exception.ExecutorErrorCode.EXECUTOR_NOT_EXIST;

public class AsyncFileTriggerExecuteHandlerFactory {

    private final Map<Pair<Class<?>, Class<?>>, FileHandlerExecutor<? extends Executor,? extends IRequest>> executeHandlerMap = new HashMap<>();

    public synchronized void register(FileHandlerExecutor<? extends Executor, ? extends IRequest> executor) {

        try {

            Method method = executor.getClass().getDeclaredMethod("execute", Executor.class, IRequest.class, Long.class);
            Class<?>[] parameterTypeArr = method.getParameterTypes();
            Class<?> actualExecutorType = parameterTypeArr[0];
            Class<?> actualRequestType = parameterTypeArr[1];

            Asserts.isTrue(!executeHandlerMap.containsKey(Pair.of(actualExecutorType, actualRequestType)), EXECUTOR_DUPLICATE);

            executeHandlerMap.put(Pair.of(actualExecutorType, actualRequestType), executor);

        } catch (NoSuchMethodException e) {
            ExceptionUtils.rethrow(e);
        }
    }

    public synchronized FileHandlerExecutor<? extends Executor,? extends IRequest> get(Class<? extends Executor> executorClass, Class<? extends IRequest> requestClass) {

        FileHandlerExecutor<? extends Executor, ? extends IRequest> executor = executeHandlerMap.get(Pair.of(executorClass, requestClass));
        if (Objects.nonNull(executor)) {
            return executor;
        }

        for (Map.Entry<Pair<Class<?>, Class<?>>, FileHandlerExecutor<? extends Executor, ? extends IRequest>> entry : executeHandlerMap.entrySet()) {
            if (entry.getKey().getKey().isAssignableFrom(executorClass) && entry.getKey().getValue().isAssignableFrom(requestClass)) {
                return entry.getValue();
            }
        }
        throw new EasyFileException(EXECUTOR_NOT_EXIST);
    }

}
