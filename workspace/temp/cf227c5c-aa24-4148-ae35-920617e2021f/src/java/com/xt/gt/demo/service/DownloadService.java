
package com.xt.gt.demo.service;

import com.xt.core.exception.ServiceException;
import com.xt.core.service.AbstractService;
import com.xt.core.utils.IOHelper;
import com.xt.gt.chb.proc.result.DownloadedFileInfo;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;

/**
 *
 * @author albert
 */
public class DownloadService  extends AbstractService {

    public DownloadService() {
    }

    /**
     * 下载一个 Excel 文件。
     * @return
     * @throws java.io.FileNotFoundException
     * @throws java.io.UnsupportedEncodingException
     */
    public DownloadedFileInfo downloadExcel() throws FileNotFoundException, UnsupportedEncodingException {
        // 读入一个表文件
        FileInputStream is = new FileInputStream("e:\\顺义局10月份统计月报.xls");
        
        // 设置下载文件的相关信息（文件的类型、输入流、文件名称）
        DownloadedFileInfo fd = new DownloadedFileInfo("application/ms-excel", is, "顺义局10月份统计月报.xls");
        fd.setContentLength(86528);
        return fd;
    }

     /**
     * 上传一个或者多个文件
     * @param userId
     * @param picture1
     * @param picture2
     */
    public boolean uploadFiles(String userId, InputStream picture1, InputStream picture2) {
        System.out.println("userId=" + userId);
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        System.out.println("picture1=" + userId);
        IOHelper.i2o(picture1, os, true, true);
        System.out.println("picture1=" + os.toString());
        ByteArrayOutputStream os2 = new ByteArrayOutputStream();
        IOHelper.i2o(picture2, os2, true, true);
        System.out.println("picture2=" + os2.toString());
        return true;
    }

    /**
     * 下载一个图片文件（在Image标签中显示图片）
     * @param userId
     * @return
     */
    public InputStream showPicture (String userId) {
        InputStream is = getClass().getClassLoader().getSystemResourceAsStream("/868.0.jpg");
            // return
        if (is == null) {
            try {
                // 显示一个默认的图片
                is = new FileInputStream("E:\\work\\xthinker\\gt_html\\src\\java\\868.0.jpg");
            } catch (FileNotFoundException ex) {
                 throw new ServiceException("读文件错误", ex);
            }
        }
        return is;
    }

    /**
     * 下载一个普通的文本文件
     * @param userId
     * @return
     */
    public InputStream downloadTxt(String userId) {
        try {
            return new FileInputStream("e:\\aa.txt");
        } catch (FileNotFoundException ex) {
            throw new ServiceException("读文件错误", ex);
            // 显示一个默认的图片
        }
    }

}
