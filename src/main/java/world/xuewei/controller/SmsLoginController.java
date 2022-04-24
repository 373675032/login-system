package world.xuewei.controller;

import cn.hutool.core.date.DateTime;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import com.github.qcloudsms.SmsSingleSender;
import com.github.qcloudsms.SmsSingleSenderResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import world.xuewei.dto.ResponseResult;
import world.xuewei.entity.User;
import world.xuewei.service.UserService;
import world.xuewei.utils.Assert;

import javax.servlet.http.HttpSession;
import java.util.*;

/**
 * @apiNote 短信验证码登录
 * @date 2022/4/15 下午3:59
 * @author XUEW
 */
@Slf4j
@Controller
public class SmsLoginController {

    @Value("${sms.app-id}")
    private Integer smsAppId;

    @Value("${sms.app-key}")
    private String smsAppKey;

    @Value("${sms.template-id}")
    private Integer smsTemplateId;

    @Value("${sms.sign: 454442}")
    private String smsSign;

    @Value("${sms.interval-min}")
    private String smsIntervalMin;

    @Value("${sms.valid-min}")
    private Integer smsValidMin;

    @Autowired
    private UserService userService;

    /**
     * 验证码登录
     */
    @GetMapping("/loginSms")
    public String loginSms(String phone, String code, HttpSession session) {
        Map<String,Object> codeData = (Map<String, Object>) session.getAttribute("PHONE_CODE" + phone);
        if (codeData == null) {
            throw new RuntimeException("登录失败：尚未发送验证码");
        }
        String sentCode = (String) codeData.get("code");
        Calendar calendar = Calendar.getInstance();
        calendar.setTime((new Date((Long) codeData.get("time"))));
        calendar.add(Calendar.MINUTE, smsValidMin);
        if (System.currentTimeMillis() > calendar.getTime().getTime()) {
            session.removeAttribute("PHONE_CODE" + phone);
            throw new RuntimeException("登录失败：验证码已经超时");
        }
        if (!sentCode.equals(code)) {
            throw new RuntimeException("登录失败：验证码错误");
        }
        // 查询用户
        List<User> users = userService.query(User.builder().phone(phone).build());
        User user = null;
        if (Assert.isEmpty(users)) {
            // 尚未注册
            user = User.builder()
                    .name("注册用户"+ DateTime.now())
                    .phone(phone)
                    .img("https://moti-cloud-v2.oss-cn-beijing.aliyuncs.com/phone.png")
                    .build();
            userService.save(user);
        } else {
            user = users.get(0);
        }
        session.setAttribute("user", user);
        return "redirect:/success";
    }

    /**
     * 发送短信验证码
     */
    @PostMapping("/sendSmsCode")
    @ResponseBody
    public ResponseResult sendSmsCode(String phone, Map<String,Object> map, HttpSession session) {
        if (StrUtil.isEmpty(phone)) {
            return ResponseResult.failure(500, "手机号不能为空");
        }

        // 生成随机验证码
        String verifyCode = RandomUtil.randomNumbers(6);

        map.put("phone", phone);
        map.put("code", verifyCode);
        map.put("time", System.currentTimeMillis());
        session.setAttribute("PHONE_CODE" + phone, map);
        SmsSingleSender sender = new SmsSingleSender(smsAppId, smsAppKey);
        ArrayList<String> params = new ArrayList<>();
        // 添加模板参数
        params.add(verifyCode);
        params.add(String.valueOf(smsValidMin));

        try {
            SmsSingleSenderResult result = sender.sendWithParam("86", phone, smsTemplateId, params, smsSign, "", "");
            if(result.result == 0) {
                map.put("phone", phone);
                map.put("code", verifyCode);
                map.put("time", System.currentTimeMillis());
                session.setAttribute("PHONE_CODE" + phone, map);
            } else {
                log.error("验证码发送失败，手机号：{}，错误信息：{}", phone, result.errMsg);
                return ResponseResult.failure(500, result.errMsg);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseResult.failure(500, "发送失败");
        }
        return ResponseResult.success();
    }
}
