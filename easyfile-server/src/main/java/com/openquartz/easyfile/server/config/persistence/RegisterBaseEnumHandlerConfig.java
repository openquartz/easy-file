package com.openquartz.easyfile.server.config.persistence;

import com.baomidou.mybatisplus.autoconfigure.ConfigurationCustomizer;
import com.baomidou.mybatisplus.core.MybatisConfiguration;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.type.TypeHandlerRegistry;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import com.openquartz.easyfile.common.constants.Constants;
import com.openquartz.easyfile.common.dictionary.BaseEnum;
import com.openquartz.easyfile.common.util.StringUtils;
import com.openquartz.easyfile.server.utils.ClassUtil;

/**
 * @author svnee
 */
@Slf4j
@Component
public class RegisterBaseEnumHandlerConfig implements ConfigurationCustomizer {

    @Value("${easyfile.server.mybatis.type-enums-package:}")
    private String typeEnumsPackage;

    @Override
    public void customize(MybatisConfiguration configuration) {

        if (StringUtils.isNotBlank(typeEnumsPackage)) {
            log.info("[RegisterEnumHandlerConfig] -------->>>>>>>>>> init mybatis self enum, dir:{}", typeEnumsPackage);
            String[] enumPackagesDirArr = typeEnumsPackage.split(Constants.COLON_SPLITTER);
            TypeHandlerRegistry typeHandlerRegistry = configuration.getTypeHandlerRegistry();
            final Set<Class<? extends BaseEnum>> allSubEnumClassSet = ClassUtil
                .getAllSubEnumClass(BaseEnum.class, enumPackagesDirArr);
            log.info("[RegisterEnumHandlerConfig] -------->>>>>>>>>> load mybatis self enum:{}", allSubEnumClassSet);
            allSubEnumClassSet.forEach(clazz -> typeHandlerRegistry.register(clazz, BaseEnumHandler.class));
            log.info("[RegisterEnumHandlerConfig] -------->>>>>>>>>> load self enum end, dir:{}", typeEnumsPackage);
        }
    }
}
