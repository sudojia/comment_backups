package cn.imzjw.comment;

import cn.imzjw.comment.utils.CommonService;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * 自动定时备份 Twikoo 评论数据
 *
 * @author <a href="https://blog.imzjw.cn">@小嘉的部落格</a>
 * @date 2024-4-26
 */
public class TwikooBackups {
    /**
     * 获取日志记录器对象
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(TwikooBackups.class);
    /**
     * 创建 HttpClient 实例
     */
    private static final CloseableHttpClient HTTP_CLIENT = HttpClients.createDefault();

    /**
     * 入口函数
     *
     * @param args 参数
     */
    public static void main(String[] args) {
        if (args.length < 2) {
            LOGGER.error("请在 Secrets 中填写 PASSWORD 和 TWIKOO_URL...");
            return;
        }
        requestTwikoo(args[0], args[1]);
    }

    /**
     * 请求数据
     *
     * @param password  用于身份验证的密码。
     * @param twikooUrl Twikoo的URL地址，指定请求的目标位置。
     */
    public static void requestTwikoo(String password, String twikooUrl) {
        try {
            HttpResponse response = HTTP_CLIENT.execute(CommonService.getPostRequest(password, twikooUrl));
            // 获取状态码，用于检测 URL 是否具有有效性
            int statusCode = response.getStatusLine().getStatusCode();
            if (statusCode != HttpStatus.SC_OK) {
                LOGGER.error("URL 无效，状态码：{}", statusCode);
                return;
            }
            CommonService.checkResponseCode(response);
        } catch (IOException e) {
            // 记录任何在发送请求或处理响应时发生的I/O错误
            LOGGER.error("写入文件或发送请求失败：", e);
        }
    }
}