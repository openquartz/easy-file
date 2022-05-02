//package org.svnee.easyfile.starter.processor;
//
//import static org.springframework.beans.factory.support.BeanDefinitionBuilder.rootBeanDefinition;
//
//import java.util.Arrays;
//import java.util.Collections;
//import java.util.LinkedHashSet;
//import java.util.Set;
//import org.springframework.beans.factory.config.BeanDefinition;
//import org.springframework.beans.factory.support.AbstractBeanDefinition;
//import org.springframework.beans.factory.support.BeanDefinitionBuilder;
//import org.springframework.beans.factory.support.BeanDefinitionReaderUtils;
//import org.springframework.beans.factory.support.BeanDefinitionRegistry;
//import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
//import org.springframework.core.annotation.AnnotationAttributes;
//import org.springframework.core.type.AnnotationMetadata;
//import org.springframework.util.ClassUtils;
//import org.svnee.easyfile.starter.annotation.EasyFileComponentScan;
//
//public class EasyFileComponentScanRegistrar implements ImportBeanDefinitionRegistrar {
//
//    public EasyFileComponentScanRegistrar() {
//    }
//
//    @Override
//    public void registerBeanDefinitions(AnnotationMetadata importingClassMetadata, BeanDefinitionRegistry registry) {
//                Set<String> packagesToScan = this.getPackagesToScan(importingClassMetadata);
//                this.registerServiceAnnotationBeanPostProcessor(packagesToScan, registry);
//    }
//
//    /**
//     * Registers {@link FileExportExecutorAnnotationBeanPostProcessor}
//     *
//     * @param packagesToScan packages to scan without resolving placeholders
//     * @param registry       {@link BeanDefinitionRegistry}
//     * @since 2.5.8
//     */
//    private void registerServiceAnnotationBeanPostProcessor(Set<String> packagesToScan, BeanDefinitionRegistry registry) {
//
//        BeanDefinitionBuilder builder = rootBeanDefinition(FileExportExecutorAnnotationBeanPostProcessor.class);
//        builder.addConstructorArgValue(packagesToScan);
//        builder.setRole(BeanDefinition.ROLE_INFRASTRUCTURE);
//        AbstractBeanDefinition beanDefinition = builder.getBeanDefinition();
//        BeanDefinitionReaderUtils.registerWithGeneratedName(beanDefinition, registry);
//    }
//
//    private Set<String> getPackagesToScan(AnnotationMetadata metadata) {
//        AnnotationAttributes attributes = AnnotationAttributes.fromMap(
//            metadata.getAnnotationAttributes(EasyFileComponentScan.class.getName()));
//        assert attributes != null;
//        String[] basePackages = attributes.getStringArray("basePackages");
//        Class<?>[] basePackageClasses = attributes.getClassArray("basePackageClasses");
//        String[] value = attributes.getStringArray("value");
//        // Appends value array attributes
//        Set<String> packagesToScan = new LinkedHashSet<>(Arrays.asList(value));
//        packagesToScan.addAll(Arrays.asList(basePackages));
//        for (Class<?> basePackageClass : basePackageClasses) {
//            packagesToScan.add(ClassUtils.getPackageName(basePackageClass));
//        }
//        if (packagesToScan.isEmpty()) {
//            return Collections.singleton(ClassUtils.getPackageName(metadata.getClassName()));
//        }
//        return packagesToScan;
//    }
//}
