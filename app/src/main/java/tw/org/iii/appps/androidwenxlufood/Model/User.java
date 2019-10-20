package tw.org.iii.appps.androidwenxlufood.Model;

public class User {
    private String User,name,password,IsStaff,phone;

    public User() {
    }

    public User(String user, String name, String password, String isStaff,String phone) {
        User = user;
        this.name = name;
        this.password = password;
        IsStaff = isStaff;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getUser() {
        return User;
    }

    public void setUser(String user) {
        User = user;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getIsStaff() {
        return IsStaff;
    }

    public void setIsStaff(String isStaff) {
        IsStaff = isStaff;
    }
}
