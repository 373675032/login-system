package world.xuewei.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * @author XUEW
 * @apiNote 用户实体
 * @date 2022/4/19 下午3:26
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User implements Serializable {

    private static final long serialVersionUID = -88300805957490445L;

    /**
     * 主键
     */
    @TableId(type = IdType.AUTO)
    private Integer id;

    /**
     * 名称
     */
    private String name;

    /**
     * 头像地址
     */
    private String img;

    /**
     * QQ唯一标识
     */
    @TableField("qq_id")
    private String qqId;

    /**
     * 微博唯一标识
     */
    @TableField("weibo_id")
    private String weiboId;

    /**
     * 人脸标识
     */
    @TableField("face_id")
    private String faceId;

    /**
     * 邮箱地址
     */
    private String email;

    /**
     * 手机号码
     */
    private String phone;

}
