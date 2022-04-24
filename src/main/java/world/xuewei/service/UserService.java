package world.xuewei.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import world.xuewei.dao.UserDao;
import world.xuewei.entity.User;
import world.xuewei.utils.Assert;
import world.xuewei.utils.BeanUtils;
import world.xuewei.utils.VariableNameUtils;

import java.util.List;
import java.util.Map;

/**
 * @apiNote 用户服务
 * @date 2022/4/19 下午3:35
 * @author XUEW
 */
@Service
public class UserService {

    @Autowired
    private UserDao userDao;

    /**
     * 保存、更新
     * @param user 用户
     * @return 用户
     */
    public User save(User user) {
        if (user.getId() == null) {
            userDao.insert(user);
        } else {
            userDao.updateById(user);
        }
        return user;
    }

    /**
     * 删除
     * @param id 主键
     * @return 影响行数
     */
    public int delete(Integer id) {
        return userDao.deleteById(id);
    }

    /**
     * 根据ID查询
     * @param id 主键
     * @return 用户
     */
    public User get(Integer id) {
        return userDao.selectById(id);
    }

    /**
     * 条件查询
     * @param user 用户
     * @return 结果
     */
    public List<User> query(User user) {
        QueryWrapper<User> wrapper = new QueryWrapper<>();
        if (Assert.notEmpty(user)) {
            Map<String, Object> bean2Map = BeanUtils.bean2Map(user);
            for (String key : bean2Map.keySet()) {
                if (Assert.isEmpty(bean2Map.get(key))) {
                    continue;
                }
                wrapper.eq(VariableNameUtils.humpToLine(key), bean2Map.get(key));
            }
        }
        return userDao.selectList(wrapper);
    }
}
