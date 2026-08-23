package com.openquartz.easyfile.common.request;

import lombok.Data;

@Data
public class ListRetryableRequest {

    private Integer lookBackHours;
    private Integer limit;
    private Integer orphanGraceSeconds;
    private String appId;

    public static ListRetryableRequest create(int lookBackHours, int limit, int orphanGraceSeconds) {
        ListRetryableRequest request = new ListRetryableRequest();
        request.setLookBackHours(lookBackHours);
        request.setLimit(limit);
        request.setOrphanGraceSeconds(orphanGraceSeconds);
        return request;
    }

    public static ListRetryableRequest create(int lookBackHours, int limit, int orphanGraceSeconds, String appId) {
        ListRetryableRequest request = create(lookBackHours, limit, orphanGraceSeconds);
        request.setAppId(appId);
        return request;
    }
}
