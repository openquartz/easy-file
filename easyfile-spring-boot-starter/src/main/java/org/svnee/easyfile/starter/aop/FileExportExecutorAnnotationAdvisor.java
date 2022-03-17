package org.svnee.easyfile.starter.aop;

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
import org.svnee.easyfile.common.annotation.FileExportExecutor;
import org.svnee.easyfile.common.util.StringUtils;

/**
 * AsyncFileExecutorAdvice
 *
 * @author svnee
 */
public class FileExportExecutorAnnotationAdvisor extends AbstractPointcutAdvisor implements BeanFactoryAware {

    private final Advice advice;
    private final Pointcut pointcut;

    private static final String BASE_DOWNLOAD_EXECUTOR_NAME = "org.svnee.easyfile.starter.executor.BaseDownloadExecutor";
    private static final String EXPORT_METHOD_NAME = "export";
    private static final String EXPORT_RESULT_METHOD_NAME = "exportResult";

    public FileExportExecutorAnnotationAdvisor(FileExportInterceptor interceptor) {
        this.advice = interceptor;
        this.pointcut = buildPointcut();
    }

    private Pointcut buildPointcut() {
        Pointcut clazzPoint = new AnnotationMatchingPointcut(FileExportExecutor.class, true);
        FullyQualifiedNameMethodPoint exportResultPoint = new FullyQualifiedNameMethodPoint(
            BASE_DOWNLOAD_EXECUTOR_NAME, EXPORT_RESULT_METHOD_NAME);
        FullyQualifiedNameMethodPoint exportPoint = new FullyQualifiedNameMethodPoint(
            BASE_DOWNLOAD_EXECUTOR_NAME, EXPORT_METHOD_NAME);

        return new ComposablePointcut(exportPoint).union(exportResultPoint).intersection(clazzPoint);
    }


    private static class FullyQualifiedNameMethodPoint implements Pointcut {

        private final String className;
        private final String methodName;

        public FullyQualifiedNameMethodPoint(String className, String methodName) {
            this.className = className;
            this.methodName = methodName;
        }

        @Override
        public ClassFilter getClassFilter() {
            return ClassFilter.TRUE;
        }

        @Override
        public MethodMatcher getMethodMatcher() {
            return new FullQualifiedNameMethodMatcher(className, methodName);
        }

        private static class FullQualifiedNameMethodMatcher extends StaticMethodMatcher {

            private final String className;
            private final String methodName;

            public FullQualifiedNameMethodMatcher(String className, String methodName) {
                this.className = className;
                this.methodName = methodName;
            }

            @Override
            public boolean matches(Method method, Class<?> targetClass) {
                return matchesMethod(method);
            }

            private boolean matchesMethod(Method method) {
                boolean equals = method.getName().equals(methodName);
                return equals && className
                    .equals(method.getDeclaringClass().getName().replace("class", StringUtils.EMPTY).trim());
            }
        }
    }

    @Override
    public Pointcut getPointcut() {
        return pointcut;
    }

    @Override
    public Advice getAdvice() {
        return advice;
    }

    @Override
    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {

    }
}
