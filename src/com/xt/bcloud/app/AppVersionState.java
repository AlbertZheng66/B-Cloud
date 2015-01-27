package com.xt.bcloud.app;

import com.xt.core.conv.impl.Ab;

/**
 * ����һ��Ӧ�ð汾��״̬��
 * @author albert
 */
public enum AppVersionState {

    /**
     * �Ѿ�ע�ᣬ���ǻ�û�з����İ汾
     */
    @Ab("RE")
    REGISTERED,

    /**
     * ��������(��������һ���汾�������У����ϵͳֻ��һ���汾�����У��Ҵ��ڲ���״̬��
     * ��ʱӦ�õ�״̬Ϊ��ע�ᡱ���ߡ�ֹͣ��״̬)��
     */
    @Ab("R")
    RUNNING,

    /**
     * �˰汾�����ڲ��Խ׶����У�ֻ��ĳЩ�������û���
     * ��ע��Ӧͨ�� URL ���б�ʶ���ߡ�Cookie�����б�ʶ���ṩ����
     * �������������Ϊ������Ա���ṩ������֤�Ľ׶Ρ�
     */
    @Ab("T")
    TESTING,

    /**
     * �˰汾���������Ĺ��̵��У�Ӧ�ÿ��������ʣ�������û����Ӱ�죩��
     * TODO: ���״̬�Ƿ������⣬��Ҫ��ϸ���ǣ���
     */
    @Ab("U")
    UPGRADING,

    /**
     * ������ͣ״̬��ע�⣺��ʱ���ڴ�Ӧ�õ�����ʵ���������У���
     * ���Խ�����Ӧ������Ϊ��ͣ״̬��Ҳ��������ΪӦ�ð汾������
     * ״̬��Ӱ��Ӧ�õ�״̬�����磺һ��Ӧ��ֻ��һ����ǰ���еİ汾��
     * ��ֻ��һ��ʵ�������������Ӧ�ð汾��������Ӧ�ô�����ͣ״̬�����ַ�ʽ�д���ϸ���ǣ�����
     */
    @Ab("P")
    PAUSED,

    /**
     * ��Ӧ���Ѿ�ֹͣ�ṩ����ϵͳ��ֹͣ��Ӧ�õ���������ʵ����
     */
    @Ab("S")
    STOPED;
    
//    /**
//     * ��Ӧ���Ѿ����Ƴ���ϵͳ��ɾ����Ӧ�е�����������á�
//     */
//    @Ab("RM")
//    REMOVED;

    @Override
    public String toString() {
        switch (this) {
            case REGISTERED:
                return "ע��";
            case RUNNING:
                return "����";
            case PAUSED:
                return "��ͣ";
            case STOPED:
                return "ֹͣ";
//            case REMOVED:
//                return "�Ƴ�";
            case TESTING:
                return "����";
            case UPGRADING:
                return "����";
        }
        return super.toString();
    }
}
