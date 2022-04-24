package world.xuewei.config;

import com.baidu.aip.face.AipFace;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author XUEW
 * @apiNote 百度人脸识别配置
 * @date 2022/4/19 上午10:29
 */
@Configuration
public class BaiduFaceConfig {
    @Value("${baidu.aip.app-id}")
    private String appId;
    @Value("${baidu.aip.app-key}")
    private String appKey;
    @Value("${baidu.aip.secret-key}")
    private String secretKey;
    @Value("${baidu.aip.conn-timeout}")
    private Integer connTimeout;
    @Value("${baidu.aip.socket-timeout}")
    private Integer socketTimeout;

    @Bean
    public AipFace aipFace() {
        AipFace client = new AipFace(appId, appKey, secretKey);
        client.setConnectionTimeoutInMillis(connTimeout);
        client.setSocketTimeoutInMillis(socketTimeout);
        return client;
    }
}
