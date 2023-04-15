package com.openquartz.easyfile.starter.processor;

import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.lang.NonNull;
import com.openquartz.easyfile.common.event.ApplicationCompleteEvent;

/**
 * Application content post processor.
 *
 * @author svnee
 */
public class ApplicationContentPostProcessor implements ApplicationListener<ContextRefreshedEvent> {

    private final ApplicationContext applicationContext;

    private boolean executeOnlyOnce = true;

    public ApplicationContentPostProcessor(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    @Override
    public void onApplicationEvent(@NonNull ContextRefreshedEvent event) {
        synchronized (ApplicationContentPostProcessor.class) {
            if (executeOnlyOnce) {
                applicationContext.publishEvent(new ApplicationCompleteEvent(this));
                executeOnlyOnce = false;
            }
        }
    }

}
