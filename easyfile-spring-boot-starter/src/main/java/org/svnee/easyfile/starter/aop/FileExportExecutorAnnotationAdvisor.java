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
import org.svnee.easyfile.common.annotations.FileExportExecutor;
import org.svnee.easyfile.starter.executor.BaseDownloadExecutor;

/**
 * AsyncFileExecutorAdvice
 *
 * @author svnee
 */
public class FileExportExecutorAnnotationAdvisor extends AbstractPointcutAdvisor implements BeanFactoryAware {

    private final Advice advice;
    private final Pointcut pointcut;

    private static final String EXPORT_METHOD_NAME = "export";
    private static final String EXPORT_RESULT_METHOD_NAME = "exportResult";

    public FileExportExecutorAnnotationAdvisor(FileExportInterceptor interceptor) {
        this.advice = interceptor;
        this.pointcut = buildPointcut();
    }

    private Pointcut buildPointcut() {
        Pointcut clazzPoint = new AnnotationMatchingPointcut(FileExportExecutor.class, false);
        FullyQualifiedNameMethodPoint exportResultPoint = new FullyQualifiedNameMethodPoint(
            BaseDownloadExecutor.class, EXPORT_RESULT_METHOD_NAME);
        FullyQualifiedNameMethodPoint exportPoint = new FullyQualifiedNameMethodPoint(
            BaseDownloadExecutor.class, EXPORT_METHOD_NAME);

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
        public ClassFilter getClassFilter() {
            return parentClazz::isAssignableFrom;
        }

        @Override
        public MethodMatcher getMethodMatcher() {
            return new FullQualifiedNameMethodMatcher(methodName);
        }

        private static class FullQualifiedNameMethodMatcher extends StaticMethodMatcher {

            private final String methodName;

            public FullQualifiedNameMethodMatcher(String methodName) {
                this.methodName = methodName;
            }

            @Override
            public boolean matches(Method method, Class<?> targetClass) {
                return matchesMethod(method);
            }

            private boolean matchesMethod(Method method) {
                return method.getName().equals(methodName);
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
        if (this.advice instanceof BeanFactoryAware) {
            ((BeanFactoryAware) this.advice).setBeanFactory(beanFactory);
        }
    }
}
