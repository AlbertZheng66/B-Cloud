

package com.xt.bcloud.td.impl;

import java.io.File;

/**
 * �������쳣�� HTTP ���������Ӧʱ��ʹ�ô˵�������е�����
 * @author albert
 */
public class ErrorDumper extends DefaultDumper {

    /**
     * �����ļ���ǰ׺
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
