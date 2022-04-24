package world.xuewei.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.springframework.stereotype.Repository;
import world.xuewei.entity.User;

/**
 * @apiNote 用户数据访问层
 * @date 2022/4/19 下午3:31
 * @author XUEW
 */
@Repository
public interface UserDao extends BaseMapper<User> {
    
}
