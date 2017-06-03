package lypop.com.objectsqlite;


import lypop.com.objectsqlite.db.annotation.LField;
import lypop.com.objectsqlite.db.annotation.LTable;

/**
 * Created by Administrator on 2017/1/9 0009.
 */
@LTable("tb_user1")
public class User {

    public int user_Id=0;

    public Integer getUser_Id() {
        return user_Id;
    }

    public void setUser_Id(Integer user_Id) {
        this.user_Id = user_Id;
    }

    public User(Integer id, String name, String password) {
        user_Id= id;
        this.name = name;
        this.password = password;
    }
    public User( ) {
    }

    @LField("name")
    public String name;
    //123456
    @LField("password")
    public String password;

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

    @Override
    public String toString() {
        return "name  "+name+"  password "+password;
    }
}
