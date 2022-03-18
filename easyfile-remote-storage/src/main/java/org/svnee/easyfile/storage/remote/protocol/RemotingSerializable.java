package org.svnee.easyfile.storage.remote.protocol;

import org.svnee.easyfile.storage.remote.common.JSONUtil;
import java.nio.charset.Charset;

public abstract class RemotingSerializable {

    private static final Charset CHARSET_UTF8 = Charset.forName("UTF-8");

    public static byte[] encode(final Object obj) {
        final String json = toJson(obj);
        if (json != null) {
            return json.getBytes(CHARSET_UTF8);
        }
        return null;
    }

    public static String toJson(final Object obj) {
        return JSONUtil.toJSONString(obj);
    }

    public static <T> T decode(final byte[] data, Class<T> classOfT) {
        final String json = new String(data, CHARSET_UTF8);
        return fromJson(json, classOfT);
    }

    public static <T> T fromJson(String json, Class<T> classOfT) {
        return JSONUtil.parseObject(json, classOfT);
    }

    public byte[] encode() {
        final String json = this.toJson();
        if (json != null) {
            return json.getBytes(CHARSET_UTF8);
        }
        return null;
    }

    public String toJson() {
        return toJson(false);
    }
}
