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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * QQ 第三方登录
 * @date 下午3:59 2022/4/15
 * @author XUEW
 */
@Slf4j
@Controller
public class QqLoginController {

    @Value("${qq.redirect}")
    private String redirect;

    @Value("${qq.app_id}")
    private String clientId;

    @Value("${qq.app_key}")
    private String secret;

    @Autowired
    private UserService userService;

    /**
     * 请求QQ登录
     */
    @RequestMapping("/loginByQQ")
    public void loginByQq(HttpServletResponse response) throws Exception {        // QQ回调URL
        // QQ认证服务器地址
        String url = "https://graph.qq.com/oauth2.0/authorize";
        // 请求QQ认证服务器
        response.sendRedirect(String.format("%s?response_type=code&client_id=%s&redirect_uri=%s", url, clientId, redirect));
    }

    /**
     * QQ登录回调
     */
    @RequestMapping("/qq")
    public String connection(String code, Map<String, Object> map, HttpSession session) {
        // 向QQ认证服务器申请令牌
        String url = "https://graph.qq.com/oauth2.0/token";
        String param = String.format("grant_type=authorization_code&code=%s&redirect_uri=%s&client_id=%s&client_secret=%s",
                code, redirect, clientId, secret);
        String result = HttpUtil.post(url, param);
        Map<String, String> params = params2Map(result);
        String accessToken = params.get("access_token");
        if (accessToken == null || "".equals(accessToken)) {
            throw new RuntimeException("登录失败：未获取到AccessToken！");
        }
        // 获取QQ用户的openId
        String meUrl = "https://graph.qq.com/oauth2.0/me";
        String openIdResp = HttpUtil.get(String.format("%s?access_token=%s", meUrl, accessToken));
        JSON parse = JSONUtil.parse(openIdResp.substring(openIdResp.indexOf("{"), openIdResp.indexOf("}") + 1));
        String openId = (String) parse.getByPath("openid");
        if (openId == null || "".equals(openId)) {
            throw new RuntimeException("登录失败：未获取到OpenId！");
        }
        // 获取用户的信息
        String infoUrl = "https://graph.qq.com/user/get_user_info";
        String infoResp = HttpUtil.get(String.format("%s?access_token=%s&oauth_consumer_key=%s&openid=%s", infoUrl, accessToken, clientId, openId));
        JSON infoObj = JSONUtil.parse(infoResp);

        // 根据OpenID获取QQ用户
        User user = null;
        List<User> userList = userService.query(User.builder().qqId(openId).build());
        if (Assert.notEmpty(userList)) {
            user = userList.get(0);
        } else {
            String nickname = (String) infoObj.getByPath("nickname");
            String sex = (String) infoObj.getByPath("gender");
            String province = (String) infoObj.getByPath("province");
            String city = (String) infoObj.getByPath("city");
            String img = (String) infoObj.getByPath("figureurl_2");
            user = User.builder().img(img).name(nickname).qqId(openId).build();
            userService.save(user);
        }
        session.setAttribute("user", user);
        return "redirect:/success";
    }

    public static Map<String, String> params2Map(String params) {
        Map<String, String> map = new HashMap<>();
        String[] tmp = params.trim().split("&");
        for (String param : tmp) {
            String[] kv = param.split("=");
            if (kv.length == 2) {
                map.put(kv[0], kv[1]);
            }
        }
        return map;
    }
}
