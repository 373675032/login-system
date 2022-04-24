package world.xuewei.controller;

import cn.hutool.core.date.DateTime;
import cn.hutool.core.util.StrUtil;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import world.xuewei.dto.ResponseCode;
import world.xuewei.dto.ResponseResult;
import world.xuewei.entity.User;
import world.xuewei.service.FaceService;
import world.xuewei.service.UserService;

import javax.servlet.http.HttpSession;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author XUEW
 * @apiNote 人脸登录
 * @date 2022/4/15 下午6:04
 */
@Controller
public class FaceLoginController {

    @Autowired
    private FaceService faceService;

    @Autowired
    private UserService userService;

    Lock lock = new ReentrantLock();

/**
 * 人脸登录注册
 */
@PostMapping("/loginByFace")
@ResponseBody
public ResponseResult loginByFace(String data, HttpSession session) {
    String base64Prefix = "data:image/png;base64,";
    if (StrUtil.isEmpty(data)) {
        return ResponseResult.failure(ResponseCode.PARAMETER_ERROR);
    }
    if (data.startsWith(base64Prefix)) {
        data = data.substring(base64Prefix.length());
    }
    lock.lock();
    // 判断是否存在人脸库
    ResponseResult searchByBase64 = faceService.searchByBase64(data);
    if (searchByBase64.getCode() == 0) {
        // 存在人脸库，登录成功
        JSONObject result = (JSONObject) searchByBase64.getData();
        Integer id = Integer.valueOf(result.get("user_id") + "");
        session.setAttribute("user", userService.get(id));
        lock.unlock();
        return new ResponseResult(0, "登录成功", null);
    }
    // 不存在人脸库，则自动注册
    ResponseResult detectByBase64 = faceService.detectByBase64(data, true);
    if (detectByBase64.getCode() != 0) {
        lock.unlock();
        return detectByBase64;
    }
    Map<String, String> map = (Map<String, String>) detectByBase64.getData();
    String faceToken = map.get("face_token");
    // 注册用户
    User user = User.builder()
            .faceId(faceToken)
            .img("https://moti-cloud-v2.oss-cn-beijing.aliyuncs.com/%E4%BA%BA%E8%84%B8%E8%AF%86%E5%88%AB_o.png")
            .faceId(faceToken)
            .name("注册用户" + DateTime.now())
            .build();
    userService.save(user);
    session.setAttribute("user", user);
    faceService.registryByFaceToken(faceToken, user.getId() + "");
    lock.unlock();
    return ResponseResult.success();
}
}
