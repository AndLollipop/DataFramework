package lypop.com.objectsqlite;


import java.util.List;

import lypop.com.objectsqlite.db.BaseDao;

/**
 * Created by Administrator on 2017/1/9 0009.
 */

public class UserDao extends BaseDao {
    @Override
    protected String createTable() {

        return "create table if not exists tb_user1(user_Id int,name varchar(20),password varchar(10))";
    }


    @Override
    public List query(String sql) {


        return null;
    }
}
