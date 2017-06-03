package lypop.com.objectsqlite.db;

import android.database.sqlite.SQLiteDatabase;
import android.os.Environment;

import static android.icu.lang.UCharacter.GraphemeClusterBreak.T;
import static android.os.Build.VERSION_CODES.M;

/**
 * Created by yzl on 2017/6/3.
 */

public class BaseDaoFactory {
    private static BaseDaoFactory factory = new BaseDaoFactory();
    private String sqliteDataBasePath = null;
    private SQLiteDatabase sqLiteDatabase;

    private BaseDaoFactory() {
        sqliteDataBasePath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/teacher.db";
        openDatabase();//打开数据库如果没有则创建
    }

    private void openDatabase() {
        sqLiteDatabase = SQLiteDatabase.openOrCreateDatabase(sqliteDataBasePath, null);
    }

    public static BaseDaoFactory getInstance() {
        return factory;
    }

    public synchronized <M extends BaseDao<T>, T> M getBaseDaoHelper(Class<M> baseDao, Class<T> entity) {
        BaseDao baseD = null;
        try {
            baseD = baseDao.newInstance();
            baseD.init(entity,sqLiteDatabase);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return (M) baseD;
    }

}
