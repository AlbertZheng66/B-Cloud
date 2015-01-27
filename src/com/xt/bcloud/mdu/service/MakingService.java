package com.xt.bcloud.mdu.service;

import com.xt.bcloud.comm.CloudUtils;
import com.xt.bcloud.comm.UnZip;
import com.xt.bcloud.comm.Zip;
import com.xt.bcloud.mdu.*;
import com.xt.bcloud.mdu.command.VarProcessorUtils;
import com.xt.core.exception.ServiceException;
import com.xt.core.log.LogWriter;
import com.xt.core.service.AbstractService;
import com.xt.core.utils.DateUtils;
import com.xt.core.utils.IOHelper;
import com.xt.core.utils.SqlUtils;
import com.xt.gt.jt.proc.result.DownloadedFileInfo;
import com.xt.gt.sys.SystemConfiguration;
import java.io.*;
import java.util.*;
import net.sf.json.JSONObject;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;

/**
 *
 * @author Albert
 */
public class MakingService extends AbstractService {

    private static final long serialVersionUID = 897973150043521890L;
    private static final String basePath = SystemConfiguration.getInstance().readString("mdu.templates.basePath", "WEB-INF/templates/");

//    public void saveAppServerTemplate(AppServerTemplate asTemplate, InputStream templateFile) {
//        assertNotNull(asTemplate, false);
//        if (StringUtils.isEmpty(asTemplate.getOid())) {
//            addAppServerTemplate(asTemplate, templateFile);
//        } else {
//            updateAppServerTemplate(asTemplate, templateFile);
//        }
//    }
    private void saveTemplateFile(AppServerTemplate asTemplate, InputStream templateFile) {
        if (templateFile == null) {
            // throw new ServiceException("ģ���ļ�����Ϊ��");
            return;
        }
        String fileName = String.format("%s_%s_%s.zip",
                asTemplate.getName(), asTemplate.getVersion(),
                DateUtils.toDateStr(Calendar.getInstance(), "yyyyMMdd.HHmmss"));
        // �������õ�����
        String storePath = basePath + fileName;
        fileService.createNewFile(storePath);
        OutputStream os = fileService.writeTo(storePath, false);
        long fileSize = IOHelper.i2o(templateFile, os);
        if (fileSize > 0) {
            asTemplate.setFileSize(fileSize);
            asTemplate.setStorePath(storePath);
        } else {
            fileService.delete(storePath);
        }
    }

    public List<ParamsVO> loadParams(AppServerTemplate asTemplate) {
        assertNotNull(asTemplate, false);
        if (StringUtils.isEmpty(asTemplate.getParams())) {
            return Collections.EMPTY_LIST;
        }
        List<ParamsVO> paramsVO = new ArrayList();
        JSONObject jsonObject = JSONObject.fromObject(asTemplate.getParams());
        if (jsonObject instanceof JSONObject) {
            for (Iterator it = jsonObject.keys(); it.hasNext();) {
                String key = String.valueOf(it.next());
                String value = jsonObject.getString(key);
                ParamsVO vo = createParamsVO(key, value);
                paramsVO.add(vo);
            }
        }
        return paramsVO;
    }

    public String buildParams(List<ParamsVO> params) {
        if (params == null || params.isEmpty()) {
            return "{}";
        }
        JSONObject obj = new JSONObject();
        for (Iterator<ParamsVO> it = params.iterator(); it.hasNext();) {
            ParamsVO paramsVO = it.next();
            String value = createValue(paramsVO);
            obj.put(paramsVO.getName(), value);
        }
        return obj.toString();
    }

    private String createValue(ParamsVO paramsVO) {
        StringBuilder strBld = new StringBuilder();
        switch (paramsVO.getType()) {
            case PORT_RANGE:
                strBld.append("${_port[").append(paramsVO.getFrom()).append(",").append(paramsVO.getTo()).append("]}");
                break;
            case INCREMENT:
                strBld.append("${_inc[").append(paramsVO.getFrom()).append("]}");
                break;
            default:
                strBld.append(paramsVO.getFrom());
                break;
        }
        return strBld.toString();
    }

    /**
     * {"jmxPort":"${_port[20000,29999]}",
     * "serverPort":"${_port[30000,39999]}","serverRedirectPort":
     * "${_port[40000,49999]}", "appServNo":"${_inc[appServNo]}"}
     *
     * @param key
     * @param value
     * @return
     */
    private ParamsVO createParamsVO(String key, String value) {
        ParamsVO vo = new ParamsVO();
        vo.setName(key);
        if (StringUtils.isEmpty(value)) {
            value = "";
        }
        if (value.startsWith("${_port[")) {
            vo.setType(ParamsType.PORT_RANGE);
            processRange(value, vo, "${_port[", true);
        } else if (value.startsWith("${_inc[")) {
            vo.setType(ParamsType.INCREMENT);
            processRange(value, vo, "${_inc[", false);
        } else {
            vo.setType(ParamsType.STRING);
            vo.setFrom(value);
        }
        return vo;
    }

    private void processRange(String value, ParamsVO vo, String prefix, boolean multi) {
        String str = value.substring(prefix.length(), value.length() - 2);
        if (multi) {
            String[] ranges = str.split(",");
            vo.setFrom(ranges[0]);
            vo.setTo(ranges[1]);
        } else {
            vo.setFrom(str);
        }
    }

    /**
     * ע��Ӧ�÷�����ģ��
     *
     * @param asTemplate
     * @return
     */
    public void addAppServerTemplate(AppServerTemplate asTemplate, InputStream templateFile) {
        LogWriter.info2(logger, "ע��Ӧ�÷�����ģ��[%s]", asTemplate);
        asTemplate.setServerType(ServerType.APP_SERVER);
        asTemplate.setOid(UUID.randomUUID().toString());
        asTemplate.setInsertTime(Calendar.getInstance());
        //  У��
        assertNotNull(asTemplate, true);
        saveTemplateFile(asTemplate, templateFile);
        persistenceManager.insert(asTemplate);
    }

    /**
     * ע��Ӧ�÷�����ģ��
     *
     * @param asTemplate
     * @return
     */
    public void updateAppServerTemplate(AppServerTemplate asTemplate, InputStream templateFile) {
        LogWriter.info2(logger, "ע��Ӧ�÷�����ģ��[%s]", asTemplate);
        asTemplate.setServerType(ServerType.APP_SERVER);
        //  У��
        assertNotNull(asTemplate, true);
        saveTemplateFile(asTemplate, templateFile);
        persistenceManager.update(asTemplate);
    }

    /**
     * ע��Ӧ�÷�����ģ��
     *
     * @param asTemplate
     * @return
     */
    public void deleteAppServerTemplate(AppServerTemplate asTemplate) {
        LogWriter.info2(logger, "ע��Ӧ�÷�����ģ��[%s]", asTemplate);
        //  У��
        assertNotNull(asTemplate, true);
        persistenceManager.delete(asTemplate);
    }

    private void assertNotNull(AppServerTemplate asTemplate, boolean includingOid) {
        if (asTemplate == null) {
            throw new ServiceException("������ģ�岻��Ϊ�ա�");
        }
        if (includingOid && StringUtils.isEmpty(asTemplate.getOid())) {
            throw new ServiceException("������ģ����벻�ܡ�");
        }
    }

    /**
     * ��ʾӦ�÷�����ģ���б�
     *
     * @return
     */
    public List<AppServerTemplate> listAppServerTemplates() {
        LogWriter.info2(logger, "��ʾӦ�÷�����ģ���б�");
        return list(ServerType.APP_SERVER);
    }
    
    /**
     * ��ʾ���������ģ���б�
     *
     * @return
     */
    public List<AppServerTemplate> listTaskDispatcherServers() {
        LogWriter.info2(logger, "��ʾӦ�÷�����ģ���б�");
        return list(ServerType.TASK_DISPATCHER);
    }

    /**
     * ע��Ӧ�÷�����ģ��
     *
     * @param asTemplate
     * @return
     */
    public void addTaskDispatcherTemplate(AppServerTemplate asTemplate, InputStream templateFile) {
        LogWriter.info2(logger, "ע�����������ģ��[%s]", asTemplate);
        asTemplate.setOid(UUID.randomUUID().toString());
        asTemplate.setInsertTime(Calendar.getInstance());
        // У��
        assertNotNull(asTemplate, true);
        saveTemplateFile(asTemplate, templateFile);
        persistenceManager.insert(asTemplate);
    }

    /**
     * �������������ģ��
     *
     * @param asTemplate
     * @return
     */
    public void updateTaskDispatcher(AppServerTemplate asTemplate, InputStream templateFile) {
        LogWriter.info2(logger, "�������������ģ��[%s]", asTemplate);
        // У��
        assertNotNull(asTemplate, true);
        asTemplate.setServerType(ServerType.TASK_DISPATCHER);
        saveTemplateFile(asTemplate, templateFile);
        persistenceManager.update(asTemplate);
    }

    /**
     * ɾ�����������ģ��
     *
     * @param asTemplate
     * @return
     */
    public void deleteTaskDispatcher(AppServerTemplate asTemplate) {
        LogWriter.info2(logger, "ɾ�����������ģ��[%s]", asTemplate);
        // У��
        assertNotNull(asTemplate, true);
        persistenceManager.delete(asTemplate);
    }

    /**
     * ��ʾ���������ģ���б�
     *
     * @return
     */
    public List<AppServerTemplate> listTaskDispatchers() {
        LogWriter.info2(logger, "��ʾӦ�÷�����ģ���б�");
        return list(ServerType.TASK_DISPATCHER);
    }

    /**
     * ��ʾ���������ģ���б�
     *
     * @return
     */
    public List<AppServerTemplate> list(ServerType serverType) {
        LogWriter.info2(logger, "��ʾӦ�÷�����ģ���б�");
        String sql = "SERVER_TYPE=?";
        return persistenceManager.findAll(AppServerTemplate.class, sql,
                SqlUtils.getParams(serverType), null);
    }

    /**
     * ���ط�������Դģ��
     *
     * @param appServerTemplate
     * @return
     */
    public InputStream downloadStream(AppServerTemplate asTemplate) {
        assertNotNull(asTemplate, true);
        String fileName = asTemplate.getStorePath();
        if (StringUtils.isEmpty(fileName)) {
            throw new MduException(String.format("��ǰ������ģ��[%s]�ļ�Ϊ�ա�", asTemplate));
        }
        if (!fileService.exists(fileName)) {
            throw new MduException(String.format("�ļ�[%s]�����ڡ�", fileName));
        }
        LogWriter.info2(logger, "��ʼ�����ļ�[%s]", fileName);

        return fileService.read(fileName);
    }

    /**
     * ���ط�������Դģ��
     *
     * @param appServerTemplate
     * @return
     */
    public DownloadedFileInfo downloadPackage(AppServerTemplate asTemplate) {
        InputStream is = downloadStream(asTemplate);
        DownloadedFileInfo dfi = new DownloadedFileInfo(DownloadedFileInfo.FILE_TYPE_OCTET_STREAM,
                is, FilenameUtils.getName(asTemplate.getStorePath()));
        return dfi;
    }

    /**
     * ����ģ��
     *
     * @param asTemplate
     */
    public DownloadedFileInfo generatePackage(AppServerTemplate asTemplate, Map params) {
        InputStream is = downloadStream(asTemplate);
        // ����һ����ʱ����Ŀ¼
        UnZip unzip = new UnZip();

        // ����һ����ʱ����Ŀ¼
        File temp = CloudUtils.getTempDir();
        File workPath = new File(temp, "mdu_" + String.valueOf(System.currentTimeMillis()));
        workPath.mkdirs();

        // ��ѹ��������·��
        LogWriter.info2(logger, "����Դ����ѹ��������·��[%s]", workPath);
        unzip.unZip(is, workPath);

        // �滻�ļ�ģ���еı���
        LogWriter.info2(logger, "��ʼ�滻�ļ�ģ���еı���");
        VarProcessorUtils.processVars(workPath, params);
        try {
            File tempFile = File.createTempFile("aaa--", "zip");
            FileOutputStream baos = new FileOutputStream(tempFile);
            Zip zip = new Zip(workPath, baos);
            zip.zip();
            DownloadedFileInfo dfi = new DownloadedFileInfo(DownloadedFileInfo.FILE_TYPE_OCTET_STREAM,
                    new FileInputStream(tempFile),
                    FilenameUtils.getName(asTemplate.getStorePath()));
            tempFile.deleteOnExit();
            return dfi;
        } catch (IOException ex) {
            throw new MduException("�����ļ�ʱ�����쳣��", ex);
        } finally {
            // FIXME: ɾ������Ҫ���ļ�
            // Director
            // workPath.delete();
        }
    }
}
