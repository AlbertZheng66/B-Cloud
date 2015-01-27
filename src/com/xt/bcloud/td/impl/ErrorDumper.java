

package com.xt.bcloud.td.impl;

import java.io.File;

/**
 * 当出现异常的 HTTP 请求或者响应时，使用此导出其进行导出。
 * @author albert
 */
public class ErrorDumper extends DefaultDumper {

    /**
     * 导出文件的前缀
     */
    protected String prefix = "err-";

    public ErrorDumper() {
        rootPath = new File("e:\\err");

        if (!rootPath.exists()) {
            rootPath.mkdirs();
        }
        requestOutputStream = responseOutputStream = createFile(prefix);
    }


}
