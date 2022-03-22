package org.svnee.easyfile.server.common.spi;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.stereotype.Component;
import org.svnee.easyfile.common.util.SpringContextUtil;
import org.svnee.easyfile.common.util.StringUtils;


/**
 * @author svnee
 */
@Component
public class SpiPostProcessor implements BeanPostProcessor {

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        Class<?> clazz = SpringContextUtil.getRealClass(bean);
        if (clazz.isAnnotationPresent(
            ComponentSpi.class) && bean instanceof ServiceProvider) {
            ComponentSpi spi = clazz.getAnnotation(ComponentSpi.class);
            if (StringUtils.isNotBlank(spi.providerName())) {
                SpiSupport
                    .register(spi.type(), spi.providerName(), (ServiceProvider) bean);
            } else {
                SpiSupport
                    .register(spi.type(), beanName, (ServiceProvider) bean);
            }

        }
        return bean;
    }
}
