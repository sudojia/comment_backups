package cn.imzjw.comment;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static cn.imzjw.comment.utils.MD5Util.toMD5;

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
     * json 名称
     */
    private static final String TWIKOO_COMMENT_JSON = "twikoo-comment.json";

    /**
     * 入口函数
     *
     * @param args 参数
     */
    public static void main(String[] args) {
        if (args.length == 0) {
            LOGGER.warn("请在 Secrets 中填写 PASSWORD 和 TWIKOO_URL...");
        }
        if (args.length == 2) {
            requestTwikoo(args[0], args[1]);
        } else {
            LOGGER.warn("PASSWORD 和 TWIKOO_URL 变量必须填写, 缺一不可");
        }
    }

    /**
     * 请求数据
     *
     * @param password  password
     * @param twikooUrl twikoo 地址
     */
    public static void requestTwikoo(String password, String twikooUrl) {
        JSONObject jsonBody = new JSONObject();
        jsonBody.put("accessToken", toMD5(password));
        jsonBody.put("collection", "comment");
        jsonBody.put("event", "COMMENT_EXPORT_FOR_ADMIN");
        // 创建 HttpClient 实例
        HttpClient client = HttpClients.createDefault();
        // 创建 HttpPost 实例
        HttpPost post = new HttpPost(twikooUrl);
        // 设置请求体
        post.setEntity(new StringEntity(jsonBody.toString(), StandardCharsets.UTF_8));
        // 设置请求头，指定发送的内容类型为 JSON
        post.setHeader("Content-Type", "application/json");
        // 发送请求并获取响应
        try {
            HttpResponse response = client.execute(post);
            HttpEntity entity = response.getEntity();
            // 检查响应状态码
            if (200 == response.getStatusLine().getStatusCode()) {
                // 将响应内容保存到本地文件
                saveToFile(EntityUtils.toString(entity));
                LOGGER.info(TWIKOO_COMMENT_JSON + " 文件已保存到本地");
            } else {
                LOGGER.error("请求失败，状态码：" + response.getStatusLine().getStatusCode());
            }
        } catch (IOException e) {
            LOGGER.error("请求 Twikoo 接口失败", e);
        }
    }

    /**
     * 将 json 内容保存到文件
     *
     * @param content 内容
     */
    private static void saveToFile(String content) {
        try {
            File file = new File(TWIKOO_COMMENT_JSON);
            try (FileOutputStream fos = new FileOutputStream(file)) {
                fos.write(content.getBytes());
            }
            // LOGGER.info("文件保存成功：" + file.getAbsolutePath());
            LOGGER.info("创建 json 文件，正在写入数据...");
        } catch (IOException e) {
            LOGGER.error("文件保存失败", e);
        }
    }
}
