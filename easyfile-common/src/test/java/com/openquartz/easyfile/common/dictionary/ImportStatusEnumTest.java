package com.openquartz.easyfile.common.dictionary;

import org.junit.Assert;
import org.junit.Test;

public class ImportStatusEnumTest {

    @Test
    public void fromCode_resolvesPartialSuccess() {
        Assert.assertEquals(ImportStatusEnum.PARTIAL_SUCCESS,
                ImportStatusEnum.fromCode("partial_success"));
    }

    @Test
    public void codes_matchExportStyle() {
        Assert.assertEquals("none", ImportStatusEnum.NONE.getCode());
        Assert.assertEquals("executing", ImportStatusEnum.EXECUTING.getCode());
        Assert.assertEquals("success", ImportStatusEnum.SUCCESS.getCode());
        Assert.assertEquals("fail", ImportStatusEnum.FAIL.getCode());
        Assert.assertEquals("revoke", ImportStatusEnum.CANCEL.getCode());
        Assert.assertEquals("invalid", ImportStatusEnum.INVALID.getCode());
    }
}
