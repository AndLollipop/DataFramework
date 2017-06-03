package lypop.com.objectsqlite;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import java.util.List;

import lypop.com.objectsqlite.db.BaseDaoFactory;

public class MainActivity extends AppCompatActivity {
    UserDao userDao;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        BaseDaoFactory factory = BaseDaoFactory.getInstance();
        userDao = factory.getBaseDaoHelper(UserDao.class, User.class);
    }

    public void add(View view) {
        User user = new User(0, "AA", "123");
        userDao.insert(user);
    }

    public void delete(View view) {
        User user = new User(0,null,null);
        userDao.delete(user);
    }

    public void update(View view) {
        User user = new User();
        user.setName("AA");

        User where = new User(0, "BBB", "123");
        userDao.update(user, where);
    }

    public void query(View view) {
        User u = new User();
        u.setUser_Id(0);
        u.setPassword("123");
        List<User> list = userDao.query(u);
        for (User us : list) {
            System.out.println("name:" + us.getName() + "    password:" + us.getPassword());
        }
    }
}
