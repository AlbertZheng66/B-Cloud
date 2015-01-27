/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.xt.bcloud.td;

import com.xt.bcloud.td.http.Request;
import java.util.Set;
import java.util.concurrent.ConcurrentSkipListSet;
import junit.framework.TestCase;

/**
 *
 * @author albert
 */
public class CattleManagerTest extends TestCase {

    private final Set<String> publicHosts = new ConcurrentSkipListSet();
    
    public CattleManagerTest(String testName) {
        super(testName);
    }

    public void testFindCattle() {
    }

    public void testComputeHost() {
        publicHosts.add("aaa.com");
        publicHosts.add("aaa.cn");
        Request request = new Request();
        request.putHeader("Host", "bbb.com:80");
        assertEquals("bbb.com", this.computeHost(request));
        request.putHeader("Host", "aaa.com:80");
        request.setContextPath("/aaa/bbb");
        assertEquals("/aaa", this.computeHost(request));
        assertEquals("/bbb", request.getContextPath());
        request.setContextPath("/");
        assertEquals("/", this.computeHost(request));
        assertEquals("/", request.getContextPath());
    }

    /**
     * 计算请求使用的域名
     * @param request
     * @return
     */
    private String computeHost(Request request) {
        String host = request.getHost();
        if (publicHosts.contains(host)) {
            // 获得二级域名
            String contextPath = request.getContextPath();
            int index = contextPath.indexOf('/', 2);
            if (index < 0) {
                host = contextPath;
                request.setContextPath("/");
            } else {
                host = contextPath.substring(0, index);
                request.setContextPath(contextPath.substring(index));  // 需要以“/”开头
            }
        }
        return host;
    }

}
