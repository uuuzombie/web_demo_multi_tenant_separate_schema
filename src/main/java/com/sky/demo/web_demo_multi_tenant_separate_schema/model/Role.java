package com.sky.demo.web_demo_multi_tenant_separate_schema.model;

import java.io.Serializable;

import com.google.common.base.Objects;

/**
 * Created by rg on 8/2/15.
 */
public class Role implements Serializable {

    private static final long serialVersionUID = -8483070874893973263L;
    private int id;
    private String roleName;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getRoleName() {
        return roleName;
    }

    public void setRoleName(String roleName) {
        this.roleName = roleName;
    }

    @Override
    public String toString() {
        return Objects.toStringHelper(this)
                .add("id", id)
                .add("roleName", roleName)
                .toString();
    }
}
