
package com.xt.bcloud.td;

import com.xt.bcloud.td.http.Request;
import com.xt.bcloud.worker.Cattle;
import java.util.List;

/**
 * 任务过滤器, 用于将指定任务分配给特定的服务器进行处理.
 * 此服务器将是一个链状结构，处理的顺序将依照定义的顺序逐级处理。过滤后的结果集
 * 将传入下一个过滤器进行处理。
 * 如果任何一个实现返回空，或者抛出异常，此“请求”将被列入错误请求。
 * 应用此过滤器的场景可能有:升级控制, 测试服务等情形.
 * @author albert
 */
public interface TaskFilter {

    /**
     * 根据当前的请求,过滤得到可处理此请求的"服务器实例"。如果返回的实例为空，
     * 表示不能处理此请求，系统将此请求列入“错误请求”之中。
     * @param request 请求实例，不为空。
     * @param cattles 可选的“服务器实例”
     * @return 多个
     */
    public List<Cattle> filter(Request request, List<Cattle> cattles);
}
