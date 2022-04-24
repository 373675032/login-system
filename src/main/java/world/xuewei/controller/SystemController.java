package world.xuewei.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * @apiNote 系统跳转控制器
 * @date 2022/4/19 下午6:27
 * @author XUEW
 */
@Controller
public class SystemController {

    /**
     * 登录成功
     */
    @GetMapping("/success")
    public String success() {
        return "success";
    }
}
