package world.xuewei.controller;

import cn.hutool.core.date.DateTime;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import world.xuewei.dto.ResponseResult;
import world.xuewei.entity.User;
import world.xuewei.service.UserService;
import world.xuewei.utils.Assert;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import javax.servlet.http.HttpSession;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * @apiNote 邮箱验证码登录
 * @date 2022/4/15 下午3:59
 * @author XUEW
 */
@Slf4j
@Controller
public class EmailLoginController {

    @Autowired
    private JavaMailSenderImpl mailSender;

    @Autowired
    private UserService userService;

    /**
     * 验证码登录
     */
    @GetMapping("/loginEmail")
    public String loginSms(String email, String code, HttpSession session) {
        Map<String,Object> codeData = (Map<String, Object>) session.getAttribute("EMAIL_CODE" + email);
        if (codeData == null) {
            throw new RuntimeException("登录失败：尚未发送验证码");
        }
        String sentCode = (String) codeData.get("code");
        Calendar calendar = Calendar.getInstance();
        calendar.setTime((Date) codeData.get("time"));
        calendar.add(Calendar.MINUTE, 5);
        if (System.currentTimeMillis() > calendar.getTime().getTime()) {
            session.removeAttribute("EMAIL_CODE" + email);
            throw new RuntimeException("登录失败：验证码已经超时");
        }
        if (!sentCode.equals(code)) {
            throw new RuntimeException("登录失败：验证码错误");
        }

        // 查询用户
        List<User> users = userService.query(User.builder().email(email).build());
        User user = null;
        if (Assert.isEmpty(users)) {
            // 尚未注册

            user = User.builder()
                    .email(email)
                    .name("注册用户"+ DateTime.now())
                    .img("https://moti-cloud-v2.oss-cn-beijing.aliyuncs.com/%E9%82%AE%E7%AE%B1.png")
                    .build();
            userService.save(user);
        } else {
            user = users.get(0);
        }
        session.setAttribute("user", user);
        return "redirect:/success";
    }

    /**
     * 发送邮箱验证码
     */
    @PostMapping("/sendEmailCode")
    @ResponseBody
    public ResponseResult sendSmsCode(String email, Map<String,Object> map, HttpSession session) {
        if (StrUtil.isEmpty(email)) {
            return ResponseResult.failure(500, "邮箱不能为空");
        }
        // 生成随机验证码
        String verifyCode = RandomUtil.randomNumbers(6);

        map.put("email", email);
        map.put("code", verifyCode);
        map.put("time", new Date());
        session.setAttribute("EMAIL_CODE" + email, map);

        MimeMessage mimeMessage = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(mimeMessage);
        try {
            helper.setSubject("用户验证");
            helper.setText("<h5 >您的动态验证码为："+ verifyCode +"，5分钟内有效，若非本人操作，请勿泄露。</h5>",true);
            helper.setFrom("373675032@qq.com");
            helper.setTo(email);
        } catch (MessagingException e) {
            e.printStackTrace();
        }
        mailSender.send(mimeMessage);
        return ResponseResult.success();
    }
}
