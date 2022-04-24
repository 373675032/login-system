package world.xuewei.service;

import com.baidu.aip.face.AipFace;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import world.xuewei.dto.ResponseCode;
import world.xuewei.dto.ResponseResult;

import java.util.HashMap;
import java.util.Map;

/**
 * @author XUEW
 * @apiNote 人脸登录服务
 * @date 2022/4/19 上午10:21
 */
@Slf4j
@Service
public class FaceService {
    @Autowired
    private AipFace client;

    @Value("${baidu.aip.accept-score}")
    private Integer faceAcceptScore;

    private static String GROUP_ID = "login";

    public ResponseResult detectByBase64(String image, boolean isQuality) {
        HashMap<String, String> options = new HashMap<>();

        StringBuffer sb = new StringBuffer("gender,face_type");
        if (isQuality) {
            sb.append(",quality");
        }
        options.put("face_field", sb.toString());
        options.put("max_face_num", "1");
        options.put("face_type", "LIVE");

        // 人脸检测
        JSONObject res = client.detect(image, "BASE64", options);
        log.info("res: {}", res);
        // 校验错误
        Integer errorCode = (Integer) res.get("error_code");
        if (errorCode != 0) {
            log.info("错误码：{}，错误信息：{}", errorCode, res.get("error_msg"));
            return ResponseResult.failure(ResponseCode.OPERATION_ERROR.getCode(), (String) res.get("error_msg"));
        }

        // 取第一条记录
        JSONArray faceList = (JSONArray) ((JSONObject) res.get("result")).get("face_list");
        if (faceList.length() == 0) {
            return ResponseResult.failure(ResponseCode.NOT_MATCH_FACE);
        }
        JSONObject faceMap = (JSONObject) faceList.get(0);

        // 人脸置信度，0~1
        if (Double.valueOf(faceMap.get("face_probability").toString()) < 0.9) {
            return ResponseResult.failure(ResponseCode.NOT_DETECT_FACE);
        }

        // 判断真人、卡通
        String faceTpe = ((JSONObject) faceMap.get("face_type")).get("type").toString();
        if (!"human".equals(faceTpe)) {
            return ResponseResult.failure(ResponseCode.NOT_REAL_FACE);
        }

        if (isQuality) {
            // 质量限制，参考官方推荐 http://ai.baidu.com/docs#/Face-Java-SDK/ca2bad80

            // 人脸旋转
            JSONObject angel = (JSONObject) faceMap.get("angle");
            if (Double.valueOf(angel.get("yaw").toString()) > 20 ||
                    Double.valueOf(angel.get("pitch").toString()) > 20 ||
                    Double.valueOf(angel.get("roll").toString()) > 20) {
                return ResponseResult.failure(ResponseCode.FACE_ANGEL_BAD);
            }

            JSONObject quality = (JSONObject) faceMap.get("quality");
            // 模糊度范围
            if (Double.valueOf(quality.get("blur").toString()) >= 0.7) {
                return ResponseResult.failure(ResponseCode.FACE_BLUR_BAD);
            }
            // 光照范围
            if ((Integer) quality.get("illumination") <= 40) {
                return ResponseResult.failure(ResponseCode.FACE_ILLUMINATION_BAD);
            }
            // 人脸完整度
            if ((Integer) quality.get("completeness") != 1) {
                return ResponseResult.failure(ResponseCode.FACE_ILLUMINATION_BAD);
            }
            // 遮挡范围
            JSONObject occlusion = (JSONObject) quality.get("occlusion");
            if (Double.valueOf(occlusion.get("left_eye").toString()) > 0.6 ||
                    Double.valueOf(occlusion.get("right_eye").toString()) > 0.6) {
                return ResponseResult.failure(ResponseCode.FACE_EYE_OCCLUSION_BAD);
            }
            if (Double.valueOf(occlusion.get("nose").toString()) > 0.7) {
                return ResponseResult.failure(ResponseCode.FACE_NOSE_OCCLUSION_BAD);
            }
            if (Double.valueOf(occlusion.get("mouth").toString()) > 0.7) {
                return ResponseResult.failure(ResponseCode.FACE_MOUTH_OCCLUSION_BAD);
            }
            if (Double.valueOf(occlusion.get("left_cheek").toString()) > 0.8 ||
                    Double.valueOf(occlusion.get("right_cheek").toString()) > 0.8) {
                return ResponseResult.failure(ResponseCode.FACE_CHEEK_OCCLUSION_BAD);
            }
            if (Double.valueOf(occlusion.get("chin_contour").toString()) > 0.6) {
                return ResponseResult.failure(ResponseCode.FACE_CHIN_OCCLUSION_BAD);
            }
        }

        // 返回数据
        Map<String, String> resultMap = new HashMap<>();
        // faceToken
        resultMap.put("face_token", (String) faceMap.get("face_token"));
        // 性别
        resultMap.put("gender", (String) ((JSONObject) faceMap.get("gender")).get("type"));

        return ResponseResult.success(resultMap);
    }

    public ResponseResult registryByFaceToken(String faceToken, String userId) {
        HashMap<String, String> options = new HashMap<>(16);
        options.put("quality_control", "NORMAL");
        options.put("liveness_control", "NORMAL");
        // 人脸注册
        JSONObject res = client.addUser(faceToken, "FACE_TOKEN", GROUP_ID, userId, options);
        // 校验错误
        Integer errorCode = (Integer) res.get("error_code");
        if (errorCode != 0) {
            log.info("错误码：{}，错误信息：{}", errorCode, res.get("error_msg"));
            return ResponseResult.failure(ResponseCode.OPERATION_ERROR.getCode(), (String) res.get("error_msg"));
        }
        return ResponseResult.success("注册成功");
    }

    public ResponseResult searchByBase64(String image) {
        HashMap<String, String> options = new HashMap<>();
        // 图片质量控制 NONE: 不进行控制 LOW:较低的质量要求 NORMAL: 一般的质量要求 HIGH: 较高的质量要求
        options.put("quality_control", "NORMAL");
        // 活体检测控制 NONE: 不进行控制 LOW:较低的活体要求(高通过率 低攻击拒绝率)
        // NORMAL: 一般的活体要求(平衡的攻击拒绝率, 通过率) HIGH: 较高的活体要求(高攻击拒绝率 低通过率)
        options.put("liveness_control", "NORMAL");
        // 查找后返回的用户数量。返回相似度最高的几个用户，默认为1，最多返回20个。
        options.put("max_user_num", "1");

        // 人脸搜索
        JSONObject res = client.search(image, "BASE64", GROUP_ID, options);
        // 校验错误
        Integer errorCode = (Integer) res.get("error_code");
        if (errorCode != 0) {
            log.info("错误码：{}，错误信息：{}", errorCode, res.get("error_msg"));
            return ResponseResult.failure(ResponseCode.OPERATION_ERROR.getCode(), (String) res.get("error_msg"));
        }
        // 取第一条记录
        JSONArray userList = (JSONArray) ((JSONObject) res.get("result")).get("user_list");
        if (userList.length() == 0) {
            return ResponseResult.failure(ResponseCode.NOT_MATCH_FACE);
        }
        JSONObject userMap = (JSONObject) userList.get(0);

        // 人脸置信度校验
        Double score = Double.valueOf(userMap.get("score").toString());
        if (score < faceAcceptScore) {
            return ResponseResult.failure(ResponseCode.NOT_ACCORD_WITH_MIN_REQUIREMENT);
        }

        String userId = userMap.get("user_id").toString();

        return ResponseResult.success(userMap);
    }

    public ResponseResult updateByFaceToken(String faceToken, String userId) {
        // 传入可选参数调用接口
        HashMap<String, String> options = new HashMap<>(16);
        options.put("quality_control", "NORMAL");
        options.put("liveness_control", "NORMAL");

        // 人脸更新
        JSONObject res = client.updateUser(faceToken, "FACE_TOKEN", GROUP_ID, userId, options);
        // 校验错误
        Integer errorCode = (Integer) res.get("error_code");
        if (errorCode != 0) {
            log.info("错误码：{}，错误信息：{}", errorCode, res.get("error_msg"));
            return ResponseResult.failure(ResponseCode.OPERATION_ERROR.getCode(), (String) res.get("error_msg"));
        } else {

            return ResponseResult.success(faceToken);
        }
    }

}
