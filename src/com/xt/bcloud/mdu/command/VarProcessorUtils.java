
package com.xt.bcloud.mdu.command;

import com.xt.core.log.LogWriter;
import java.io.File;
import java.util.Map;
import org.apache.log4j.Logger;

/**
 *
 * @author Albert
 */
public class VarProcessorUtils {
    
    
    private final static Logger logger = Logger.getLogger(VarProcessorUtils.class);
    
    static public void processVars(File file, final Map params) {
        if (file == null) {
            return;
        }
        if (file.isDirectory()) {
            File[] children = file.listFiles();
            if (children != null) {
                for (int i = 0; i < children.length; i++) {
                    File child = children[i];
                    processVars(child, params);
                }
            }
        } else {
            if (file.getName().endsWith(".vm")) {
                LogWriter.info2(logger, "开始处理模板文件[%s]", file.getName());
                // 去掉“.vm”作为目标文件名
                String fullName = file.getAbsolutePath();
                String targetFileName = fullName.substring(0, fullName.length() - 3);
                VarProcessor vp = new VarProcessor();
                LogWriter.info2(logger, "开始生成目标文件[%s]", targetFileName);
                vp.generate(fullName, targetFileName, params);
            }
        }
    }
    
}
