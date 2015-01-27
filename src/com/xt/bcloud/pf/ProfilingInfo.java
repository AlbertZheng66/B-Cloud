/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.xt.bcloud.pf;

import com.xt.bcloud.resource.TaskDispatcher;
import com.xt.bcloud.resource.server.ServerInfo;
import com.xt.bcloud.resource.server.ServerState;
import com.xt.gt.ui.table.ColumnInfo;
import java.io.Serializable;

/**
 *
 * @author Albert
 */
public class ProfilingInfo implements Serializable {
    private static final long serialVersionUID = 1091739359057671109L;

    /**
     * 服务器唯一标识
     */
    private String oid;
    /**
     * 自定义 ID 标识。
     */
    @ColumnInfo(title = "自定义标识")
    private String id;
    /**
     * 服务器名称（机器名称，便于管理）
     */
    @ColumnInfo(title = "名称")
    private String name;
    /**
     * 当前状态
     */
    @ColumnInfo(title = "当前状态")
    private ServerState state;

    public ProfilingInfo() {
    }

    public void load(ServerInfo serverInfo) {
        setOid(serverInfo.getOid());
        setId(serverInfo.getId());
        setName(serverInfo.getName());
        setState(serverInfo.getState());
    }
    
    public void load(TaskDispatcher taskDispatcher) {
        setOid(taskDispatcher.getOid());
        setId(taskDispatcher.getId());
        setName(taskDispatcher.getName());
        setState(taskDispatcher.getState());
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getOid() {
        return oid;
    }

    public void setOid(String oid) {
        this.oid = oid;
    }

    public ServerState getState() {
        return state;
    }

    public void setState(ServerState state) {
        this.state = state;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final ProfilingInfo other = (ProfilingInfo) obj;
        if ((this.oid == null) ? (other.oid != null) : !this.oid.equals(other.oid)) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 53 * hash + (this.oid != null ? this.oid.hashCode() : 0);
        return hash;
    }

    @Override
    public String toString() {
        return "ProfilingInfo{" + "oid=" + oid + ", id=" + id + ", name=" + name + ", state=" + state + '}';
    }
}
