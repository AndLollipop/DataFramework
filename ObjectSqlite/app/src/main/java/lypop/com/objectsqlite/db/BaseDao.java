package lypop.com.objectsqlite.db;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import lypop.com.objectsqlite.db.annotation.LField;
import lypop.com.objectsqlite.db.annotation.LTable;

/**
 * Created by yzl on 2017/6/3.
 */

public abstract class BaseDao<T> implements IBaseDao<T> {
    private Class<T> entityClass;//得到实体Bean的class
    private SQLiteDatabase sqLiteDatabase;//操作数据库

    private HashMap<String, Field> cacheMap;//维护这表名与成员变量名的映射关系
    private boolean isInit = true;//判断是否是第一次初始化
    private String table = null;//得到新建表的名字

    public synchronized boolean init(Class<T> t, SQLiteDatabase sqLiteDatabase) {
        if (isInit) {
            this.entityClass = t;
            this.sqLiteDatabase = sqLiteDatabase;
            if (t.getAnnotation(LTable.class) == null) {
                table = t.getSimpleName();
            } else {
                table = t.getAnnotation(LTable.class).value();
            }
            if (!sqLiteDatabase.isOpen()) {//判断数据库是否打开
                return false;
            }
            cacheMap = new HashMap<>();
            sqLiteDatabase.execSQL(createTable());
            initCacheMap();
            isInit = false;
        }
        return true;
    }

    /**
     * 初始化表和变量名的映射关系
     */
    private void initCacheMap() {
        Cursor cursor = null;
        try {
            String sql = "select * from " + this.table + " limit 1 , 0";
            cursor = sqLiteDatabase.rawQuery(sql, null);
            /**
             * 表的列名数组
             */
            String[] columnNames = cursor.getColumnNames();
            /**
             * 拿到Filed数组
             */
            Field[] fields = entityClass.getFields();
            for (Field field : fields) {
                field.setAccessible(true);
            }
            /**
             * 开始找对应关系
             */
            for (String column : columnNames) {
                Field columnField = null;
                for (Field field : fields) {
                    String fieldName = null;
                    if (field.getAnnotation(LField.class) != null) {
                        fieldName = field.getAnnotation(LField.class).value();
                    } else {
                        fieldName = field.getName();
                    }
                    /**
                     * 如果表的列名 等于了  成员变量的注解名字
                     */
                    if (fieldName.equals(column)) {
                        columnField = field;
                        break;
                    }
                }
                //如果找到相等的位置则将Field对象存入Map中
                if (columnField != null) {
                    cacheMap.put(column, columnField);
                }

            }
        } catch (Exception e) {

        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

    }

    /**
     * 将对象拥有的成员变量
     * 转换成  表的列名  ---》成员变量的值
     * 如  tb_name  ----> "张三"
     * 这样的map集合
     * User
     * name  "zhangsn"
     *
     * @param entity
     * @return
     */
    private Map<String, String> getValues(T entity) {
        Map<String, String> keyValue = new HashMap<>();
        Iterator<Field> iterator = cacheMap.values().iterator();
        while (iterator.hasNext()) {
            Field field = iterator.next();
            String cacheKey = null;
            String cacheValue = null;
            if (field.getAnnotation(LField.class) != null) {
                cacheKey = field.getAnnotation(LField.class).value();
            } else {
                cacheKey = field.getName();
            }
            try {
                if (null == field.get(entity)) {
                    continue;
                }
                cacheValue = field.get(entity).toString();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
            keyValue.put(cacheKey, cacheValue);
        }
        return keyValue;
    }

    @Override
    public Long insert(T entity) {
        Map<String, String> map = getValues(entity);
        ContentValues values = getContentValues(map);
        System.out.println("insert");
        Long result = sqLiteDatabase.insert(table, null, values);
        return result;
    }

    private ContentValues getContentValues(Map<String, String> map) {
        ContentValues contentValue = new ContentValues();
        Iterator<Map.Entry<String, String>> iterator = map.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, String> entry = iterator.next();
            contentValue.put(entry.getKey(), entry.getValue());
        }
        return contentValue;
    }


    @Override
    public int update(T entity, T where) {
        int reslut = -1;
        Map values = getValues(entity);
        /**
         * 将条件对象 转换map
         */
        Map whereClause = getValues(where);

        Condition condition = new Condition(whereClause);
        ContentValues contentValues = getContentValues(values);
        reslut = sqLiteDatabase.update(table, contentValues, condition.getWhereClause(), condition.getWhereArgs());
        return reslut;
    }

    @Override
    public int delete(T where) {
        Map map = getValues(where);

        Condition condition = new Condition(map);
        /**
         * id=1 数据
         * id=?      new String[]{String.value(1)}
         */
        int reslut = sqLiteDatabase.delete(table, condition.getWhereClause(), condition.getWhereArgs());
        return reslut;
    }

    @Override
    public List query(T where) {
        return query(where, null, null, null);
    }

    @Override
    public List query(T where, String orderBy, Integer startIndex, Integer limit) {
        Map map = getValues(where);
        String limitString = null;
        if (startIndex != null && limit != null) {
            limitString = startIndex + " , " + limit;
        }
        Condition condition = new Condition(map);
        Cursor cursor = sqLiteDatabase.query(table, null, condition.getWhereClause(), condition.getWhereArgs(), null, null, orderBy, limitString);
        List<T> result = getResult(cursor, where);
        cursor.close();
        return result;
    }

    private List<T> getResult(Cursor cursor, T where) {
        ArrayList list = new ArrayList();

        Object item;
        while (cursor.moveToNext()) {
            try {
                item = where.getClass().newInstance();
                /**
                 * 列名  name
                 * 成员变量名  Filed;
                 */
                Iterator iterator = cacheMap.entrySet().iterator();
                while (iterator.hasNext()) {
                    Map.Entry entry = (Map.Entry) iterator.next();
                    /**
                     * 得到列名
                     */
                    String colomunName = (String) entry.getKey();
                    /**
                     * 然后以列名拿到  列名在游标的位子
                     */
                    Integer colmunIndex = cursor.getColumnIndex(colomunName);
                    Field field = (Field) entry.getValue();
                    Class type = field.getType();
                    if (colmunIndex != -1) {
                        if (type == String.class) {
                            //反射方式赋值
                            field.set(item, cursor.getString(colmunIndex));
                        } else if (type == Double.class) {
                            field.set(item, cursor.getDouble(colmunIndex));
                        } else if (type == Integer.class) {
                            field.set(item, cursor.getInt(colmunIndex));
                        } else if (type == Long.class) {
                            field.set(item, cursor.getLong(colmunIndex));
                        } else if (type == byte[].class) {
                            field.set(item, cursor.getBlob(colmunIndex));
                            /*
                            不支持的类型
                             */
                        } else {
                            continue;
                        }
                    }

                }
                list.add(item);
            } catch (Exception e) {

            }

        }
        return list;
    }

    @Override
    public List query(String sql) {
        return null;
    }

    /**
     * 创建表
     *
     * @return
     */
    protected abstract String createTable();


    class Condition {
        /**
         * 查询条件
         * name=? && password =?
         */
        private String whereClause;//条件的key

        private String[] whereArgs;//条件的value

        public Condition(Map<String, String> map) {
            ArrayList list = new ArrayList();
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append(" 1=1 ");
            Set keys = map.keySet();
            Iterator iterator = keys.iterator();
            while (iterator.hasNext()) {
                String key = (String) iterator.next();
                String value = map.get(key);

                if (value != null) {
                    /*
                    拼接条件查询语句
                    1=1 and name =? and password=?
                     */
                    stringBuilder.append(" and " + key + " =?");
                    list.add(value);
                }
            }
            this.whereClause = stringBuilder.toString();
            this.whereArgs = (String[]) list.toArray(new String[list.size()]);

        }

        public String getWhereClause() {
            return whereClause;
        }

        public String[] getWhereArgs() {
            return whereArgs;
        }
    }
}
