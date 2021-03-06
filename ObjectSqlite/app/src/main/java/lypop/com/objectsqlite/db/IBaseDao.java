package lypop.com.objectsqlite.db;

import java.util.List;

/**
 * Created by yzl on 2017/6/3.
 */

public interface IBaseDao<T> {
    /**
     * 插入数据
     *
     * @param entity
     * @return
     */
    Long insert(T entity);

    /**
     * 更新数据
     *
     * @param entity
     * @param where
     * @return
     */
    int update(T entity, T where);

    /**
     * 删除数据
     *
     * @param where
     * @return
     */
    int delete(T where);

    /**
     * 查询数据
     *
     * @param where
     * @return
     */
    List<T> query(T where);

    List<T> query(T where, String orderBy, Integer startIndex, Integer limit);

    List<T> query(String sql);
}
