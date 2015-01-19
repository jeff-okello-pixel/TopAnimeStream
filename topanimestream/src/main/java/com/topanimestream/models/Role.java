package com.topanimestream.models;

public class Role {
    private int RoleId;
    private String Name;
    private int RankOrder;
    private String Description;

    public Role() {
    }

    public Role(int roleId, String name, int rankOrder, String description) {
        RoleId = roleId;
        Name = name;
        RankOrder = rankOrder;
        Description = description;
    }

    public int getRoleId() {
        return RoleId;
    }

    public void setRoleId(int roleId) {
        RoleId = roleId;
    }

    public String getName() {
        return Name;
    }

    public void setName(String name) {
        Name = name;
    }

    public int getRankOrder() {
        return RankOrder;
    }

    public void setRankOrder(int rankOrder) {
        RankOrder = rankOrder;
    }

    public String getDescription() {
        return Description;
    }

    public void setDescription(String description) {
        Description = description;
    }
}
