package world.xuewei.controller;

import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSON;
import cn.hutool.json.JSONUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import world.xuewei.entity.User;
import world.xuewei.service.UserService;
import world.xuewei.utils.Assert;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.List;

/**
 * 微博 第三方登录
 * @date 下午3:59 2022/4/15
 * @author XUEW
 */
@Slf4j
@Controller
public class WeiBoLoginController {

    @Value("${weibo.client_id}")
    private String clientId;

    @Value("${weibo.secret}")
    private String secret;

    @Value("${weibo.redirect}")
    private String redirect;

    @Autowired
    private UserService userService;

    /**
     * 请求微博登录
     */
    @RequestMapping("/loginByWeiBo")
    public void loginByWeiBo(HttpServletResponse response) throws Exception {        // QQ回调URL
        // 微博认证服务器地址
        String url = "https://api.weibo.com/oauth2/authorize";
        // 请求认证服务器
        response.sendRedirect(String.format("%s?response_type=code&client_id=%s&redirect_uri=%s", url, clientId, redirect));
    }

    /**
     * 微博登录回调
     */
    @RequestMapping("/weibo")
    public String weiBo(String code, HttpSession session) {
        String url = "https://api.weibo.com/oauth2/access_token?client_id=" + clientId + "&client_secret=" + secret + "&grant_type=authorization_code&redirect_uri=" + redirect + "&code=" + code;
        log.info("accessTokenUrl = {}", url);

        String resp = HttpUtil.post(url, "");
        log.info("accessTokenResp = {}", resp);

        JSON json = JSONUtil.parse(resp);
        String token = json.getByPath("access_token").toString();
        String uid = json.getByPath("uid").toString();
        log.info("token = {}", token);
        log.info("uid = {}", uid);

        String userInfoUrl = "https://api.weibo.com/2/users/show.json?uid=" + uid + "&access_token=" + token;
        String userInfo = HttpUtil.get(userInfoUrl);
        log.info("userInfo = {}", userInfo);
        JSON info = JSONUtil.parse(userInfo);
        String nickname = (String) info.getByPath("name");
        String img = (String) info.getByPath("profile_image_url");

        // 根据uId获取微博用户
        User user = null;
        List<User> userList = userService.query(User.builder().weiboId(uid).build());
        if (Assert.notEmpty(userList)) {
            user = userList.get(0);
        } else {
            user = User.builder().img(img).name(nickname).weiboId(uid).build();
            userService.save(user);
        }
        session.setAttribute("user", user);
        return "redirect:/success";
    }
}
