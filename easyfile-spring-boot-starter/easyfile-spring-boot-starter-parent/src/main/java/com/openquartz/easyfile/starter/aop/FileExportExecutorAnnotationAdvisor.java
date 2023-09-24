package com.openquartz.easyfile.starter.aop;

import com.openquartz.easyfile.core.annotations.FileExportExecutor;
import com.openquartz.easyfile.core.executor.BaseExportExecutor;
import java.lang.reflect.Method;
import org.aopalliance.aop.Advice;
import org.springframework.aop.ClassFilter;
import org.springframework.aop.MethodMatcher;
import org.springframework.aop.Pointcut;
import org.springframework.aop.support.AbstractPointcutAdvisor;
import org.springframework.aop.support.ComposablePointcut;
import org.springframework.aop.support.StaticMethodMatcher;
import org.springframework.aop.support.annotation.AnnotationMatchingPointcut;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.lang.NonNull;

/**
 * AsyncFileExecutorAdvice
 *
 * @author svnee
 */
public class FileExportExecutorAnnotationAdvisor extends AbstractPointcutAdvisor implements BeanFactoryAware {

    private final transient Advice advice;
    private final transient Pointcut pointcut;

    private static final String EXPORT_METHOD_NAME = "export";
    private static final String EXPORT_RESULT_METHOD_NAME = "exportResult";

    public FileExportExecutorAnnotationAdvisor(FileExportInterceptor interceptor) {
        this.advice = interceptor;
        this.pointcut = buildPointcut();
    }

    private Pointcut buildPointcut() {
        Pointcut clazzPoint = new AnnotationMatchingPointcut(FileExportExecutor.class, false);
        FullyQualifiedNameMethodPoint exportResultPoint = new FullyQualifiedNameMethodPoint(
            BaseExportExecutor.class, EXPORT_RESULT_METHOD_NAME);
        FullyQualifiedNameMethodPoint exportPoint = new FullyQualifiedNameMethodPoint(
            BaseExportExecutor.class, EXPORT_METHOD_NAME);

        return new ComposablePointcut(exportPoint).union(exportResultPoint).intersection(clazzPoint);
    }

    private static class FullyQualifiedNameMethodPoint implements Pointcut {

        private final Class<?> parentClazz;
        private final String methodName;

        public FullyQualifiedNameMethodPoint(Class<?> parentClazz, String methodName) {
            this.parentClazz = parentClazz;
            this.methodName = methodName;
        }

        @Override
        @NonNull
        public ClassFilter getClassFilter() {
            return parentClazz::isAssignableFrom;
        }

        @Override
        @NonNull
        public MethodMatcher getMethodMatcher() {
            return new FullQualifiedNameMethodMatcher(methodName);
        }

        private static class FullQualifiedNameMethodMatcher extends StaticMethodMatcher {

            private final String methodName;

            public FullQualifiedNameMethodMatcher(String methodName) {
                this.methodName = methodName;
            }

            @Override
            public boolean matches(@NonNull Method method,@NonNull Class<?> targetClass) {
                return matchesMethod(method);
            }

            private boolean matchesMethod(Method method) {
                return method.getName().equals(methodName);
            }
        }
    }

    @Override
    @NonNull
    public Pointcut getPointcut() {
        return pointcut;
    }

    @Override
    @NonNull
    public Advice getAdvice() {
        return advice;
    }

    @Override
    public void setBeanFactory(@NonNull BeanFactory beanFactory) throws BeansException {
        if (this.advice instanceof BeanFactoryAware) {
            ((BeanFactoryAware) this.advice).setBeanFactory(beanFactory);
        }
    }
}
