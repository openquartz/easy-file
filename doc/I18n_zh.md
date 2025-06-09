## 国际化多语言支持

（支持最新版本1.6.0 以上版本）

针对多语言场景，EasyFile 提供的内置的导出Executor提供了多语言支持。

如果需要导出列名多语言支持时，可以在`@ExcelProperty` 注解中的value 属性设置占位符号${key},例如：${student.name}.
<br>那么我们就可以针对student.name 配置i18n的多语言环境包文件。
<br>那么导出时需要请求集成到spring 支持的i18n环境中的`LocaleContextHolder`.并配置对应的`MessageResource`类注入到Spring容器中。

具体如何实现可以参考[EasyFile-example-local](https://github.com/openquartz/easy-file/tree/master/easyfile-example/easyfile-example-local)

### 使用国际化步骤
- 1、使用@ExcepProperty注解，在value属性中设置占位符，例如：${student.name}。
```java
public class Student {

    @Id
    @ExcelProperty("ID")
    private Long id;

    @ExcelProperty(value = "${student.name}", width = 8 * 512)
    private String name;

    @ExcelProperty(value = "${student.age}")
    private Integer age;
}
```
- 2、配置国际化文件到resources下。例如：messages_zh.properties、messages_en.properties。

messages_zh.properties
```properties
student.name=学生姓名
student.age=学生年龄
```
messages_en.properties
```properties
student.name=Student Name
student.age=Student Age
```
- 3、注入MessageResource到spring容器中。
```java
    @Bean
    public MessageSource messageSource() {
        ReloadableResourceBundleMessageSource messageSource = new ReloadableResourceBundleMessageSource();
        messageSource.setBasename("classpath:i18n/messages"); // 设置基础名，指向资源文件的位置
        messageSource.setDefaultEncoding("UTF-8"); // 设置默认编码
        return messageSource;
    }
```

- 4、设置spring i18n多语言环境
```java
    /**
     *  设置spring 语言解析器。设置默认语言
     */
    @Bean
    public LocaleResolver localeResolver() {
        SessionLocaleResolver slr = new SessionLocaleResolver();
        slr.setDefaultLocale(Locale.US); // 默认语言
        return slr;
    }

    /**
     * 使用URL 参数中的 lang 参数指定语言。
     * 也可使用session、cookie、请求头等方式。
     * 例如：http://localhost:9999/student/export/page?lang=zh
     */
    @Bean
    public LocaleChangeInterceptor localeChangeInterceptor() {
        LocaleChangeInterceptor lci = new LocaleChangeInterceptor();
        lci.setParamName("lang"); // URL参数名
        return lci;
    }

   /**
    * 设置多语言信息传递解析器
    * @param registry 拦截注册器
    */
   @Override
   public void addInterceptors(InterceptorRegistry registry) {
       registry.addInterceptor(localeChangeInterceptor()); 
   }
```
- 5、执行多语言导出。
  例如使用中文导出
```properties
http://localhost:9999/student/export/page?lang=zh
```
