//package org.svnee.easyfile.starter.annotation;
//
//import java.lang.annotation.Documented;
//import java.lang.annotation.ElementType;
//import java.lang.annotation.Retention;
//import java.lang.annotation.RetentionPolicy;
//import java.lang.annotation.Target;
//import org.springframework.core.annotation.AliasFor;
//
///**
// * 启动类
// *
// * @author svnee
// */
//@Target({ElementType.TYPE})
//@Retention(RetentionPolicy.RUNTIME)
//@Documented
//@EasyFileComponentScan
//public @interface EnableEasyFile {
//
//    /**
//     * 用于扫描带注释的 @FileExportExecutor 类的基本包
//     */
//    @AliasFor(annotation = EasyFileComponentScan.class, attribute = "basePackages")
//    String[] scanBasePackages() default {};
//
//    /**
//     * scanBasePackages() 的类型安全替代方法，
//     * 用于指定要扫描带注释的 @FileExportExecutor 类的包。
//     * 将扫描指定的每个类的包。
//     * @return 要扫描的基础包中的类
//     */
//    @AliasFor(annotation = EasyFileComponentScan.class, attribute = "basePackageClasses")
//    Class<?>[] scanBasePackageClasses() default {};
//
//}
