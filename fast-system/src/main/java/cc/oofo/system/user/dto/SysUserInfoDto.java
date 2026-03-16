package cc.oofo.system.user.dto;

/**
 * 系统用户信息DTO
 * 
 * @author Sir丶雨轩
 * @since 2025/11/14
 */
public class SysUserInfoDto {

    private String id;
    private String username;
    private String email;
    private String phone;
    private String nickname;

    public String getId() {
        return id;
    }

    public void setId(String id) {
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

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }
}