package com.example.train_back.dto;

/**
 * 用户信息响应DTO
 */
public class UserInfoResponseDTO {
    private Integer id;
    private String username;
    private String email;
    private String phone;
    private String role;
    private String roleName; // 角色中文名称

    public UserInfoResponseDTO() {
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
        // 自动设置角色中文名称
        this.roleName = getRoleName(role);
    }

    public String getRoleName() {
        return roleName;
    }

    public void setRoleName(String roleName) {
        this.roleName = roleName;
    }

    /**
     * 根据角色代码获取角色中文名称
     */
    private String getRoleName(String role) {
        if (role == null) {
            return "未知";
        }
        return switch (role) {
            case "USER" -> "普通用户";
            case "STAFF" -> "工作人员";
            case "AUDITOR" -> "审核人员";
            default -> "未知";
        };
    }
}

