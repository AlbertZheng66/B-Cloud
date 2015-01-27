package com.xt.bcloud.comm;

import com.xt.core.exception.SystemException;
import com.xt.core.log.LogWriter;
import com.xt.core.utils.IOHelper;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;
import org.apache.log4j.Logger;

/**
 *UnZip -- print or unzip a JAR or PKZIP file using java.util.zip. Command-line
 * version: extracts files.
 *
 * @author Ian Darwin, Ian@DarwinSys.com $Id: UnZip.java,v 1.7 2004/03/07
 *         17:40:35 ian Exp $
 */
public class UnZip {
    /** Cache of paths we've mkdir()ed. */
    protected SortedSet dirsMade;

    private final Logger logger = Logger.getLogger(UnZip.class);

    /** Construct an UnZip object. */
    public UnZip() {
    }

    /** For a given Zip file, process each entry. */
    public void unZip(String fileName, File basePath) {
        dirsMade = new TreeSet();
        try {
            ZipFile zippy = new ZipFile(fileName);
            Enumeration all = zippy.entries();
            while (all.hasMoreElements()) {
                ZipEntry entry = (ZipEntry) all.nextElement();
                getFile(entry, basePath, zippy.getInputStream(entry), true);
            }
        } catch (IOException err) {
            throw new SystemException("解压缩文件时出现异常。", err);
        }
    }

    /** For a given Zip file, process each entry. */
    public void unZip(InputStream inputStream, File basePath) {
        dirsMade = new TreeSet();
        try {
            ZipInputStream zis = new ZipInputStream(inputStream);
            ZipEntry zipEntry = null;
            while ((zipEntry = zis.getNextEntry()) != null) {
                getFile(zipEntry, basePath, zis, false);
            }
        } catch (IOException err) {
            throw new SystemException("解压缩文件时出现异常。", err);
        }
    }

    /**
     * Process one file from the zip, given its name. Either print the name, or
     * create the file on disk.
     */
    private void getFile(ZipEntry e, File basePath, InputStream is, boolean closeInputStream) throws IOException {
        String zipName = e.getName();

        if (zipName.startsWith("/")) {
            zipName = zipName.substring(1);
        }
        // if a directory, just return. We mkdir for every file,
        // since some widely-used Zip creators don't put out
        // any directory entries, or put them in the wrong place.
        if (zipName.endsWith("/")) {
            return;
        }
        // Else must be a file; open the file for output
        // Get the directory part.
        int ix = zipName.lastIndexOf('/');
        if (ix > 0) {
            String dirName = zipName.substring(0, ix);
            if (!dirsMade.contains(dirName)) {
                File d = new File(basePath, dirName);
                // If it already exists as a dir, don't do anything
                if (!(d.exists() && d.isDirectory())) {
                    // Try to create the directory, warn if it fails
                    // LogWriter.debug(logger, "Creating Directory: ", dirName);
                    if (!d.mkdirs()) {
                        throw new SystemException(String.format("不能创建目录[%s]。", dirName));
                    }
                    dirsMade.add(dirName);
                }
            }
        }
        // LogWriter.debug(logger, "Creating ", zipName);
        File zipFile  = new File(basePath, zipName);
        FileOutputStream os = new FileOutputStream(zipFile);
        IOHelper.i2o(is, os, closeInputStream, true);
    }
}