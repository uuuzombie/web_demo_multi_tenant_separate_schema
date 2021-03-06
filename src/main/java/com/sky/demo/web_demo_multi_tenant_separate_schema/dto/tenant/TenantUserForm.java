package com.sky.demo.web_demo_multi_tenant_separate_schema.dto.tenant;

import java.io.Serializable;

import com.google.common.base.Objects;
import com.sky.demo.web_demo_multi_tenant_separate_schema.model.TenantUser;

/**
 * Created by user on 16/9/18.
 */
public class TenantUserForm implements Serializable {

    private static final long serialVersionUID = 69462514897448375L;
    private int id;
    private TenantForm tenant;
    private String userName;
    private String createTime;
    private TenantUser.Status status;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public TenantForm getTenant() {
        return tenant;
    }

    public void setTenant(TenantForm tenant) {
        this.tenant = tenant;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getCreateTime() {
        return createTime;
    }

    public void setCreateTime(String createTime) {
        this.createTime = createTime;
    }

    public TenantUser.Status getStatus() {
        return status;
    }

    public void setStatus(TenantUser.Status status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return Objects.toStringHelper(this)
                .add("id", id)
                .add("tenant", tenant)
                .add("userName", userName)
                .add("createTime", createTime)
                .add("status", status)
                .toString();
    }
}
