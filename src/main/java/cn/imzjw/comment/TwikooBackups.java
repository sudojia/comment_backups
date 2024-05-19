package cn.imzjw.comment;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

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
    private static final String TWIKOO_COMMENT_JSON = "twikoo-comment";
    /**
     * 请求头，指定发送的内容类型为 JSON
     */
    private static final String APPLICATION_JSON = "application/json";
    /**
     * 请求参数
     */
    private static final String ACCESS_TOKEN = "accessToken";
    /**
     * 请求参数
     */
    private static final String COLLENTION = "collection";
    /**
     * 请求参数
     */
    private static final String EVENT = "event";
    /**
     * 请求参数
     */
    private static final String COMMENT = "comment";
    /**
     * 请求参数
     */
    private static final String COMMENT_EXPORT_FOR_ADMIN = "COMMENT_EXPORT_FOR_ADMIN";
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
        try {
            // 发送请求并获取响应
            HttpResponse response = HTTP_CLIENT.execute(getPost(password, twikooUrl, new JSONObject()));
            HttpEntity entity = response.getEntity();
            // 检查响应状态码
            if (HttpStatus.SC_OK == response.getStatusLine().getStatusCode()) {
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
            // 获取当前日期
            String currentDate = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
            // 构建新的文件名，包含日期
            String fileNameWithDate = TWIKOO_COMMENT_JSON + "-" + currentDate + ".json";
            // 解析原始 JSON 字符串，提取 "data" 数组
            JSONArray dataArray = new JSONObject(content).getJSONArray("data");
            // 将 JSON 数组转换为字符串
            String data = dataArray.toString();
            // 写入数组字符串到文件
            File file = new File(fileNameWithDate);
            try (FileOutputStream fos = new FileOutputStream(file)) {
                fos.write(data.getBytes());
            }
            // LOGGER.info("文件保存成功：" + file.getAbsolutePath());
            LOGGER.info("创建 json 文件，正在写入数据...");
        } catch (IOException e) {
            LOGGER.error("创建 json 文件失败", e);
        }
    }

    /**
     * 获取 post 请求
     *
     * @param password  密码
     * @param twikooUrl twikoo 地址
     * @param jsonBody  jsonBody
     * @return post 请求
     */
    private static HttpPost getPost(String password, String twikooUrl, JSONObject jsonBody) {
        getParamMap(toMD5(password)).forEach(jsonBody::put);
        HttpPost post = new HttpPost(twikooUrl);
        // 设置请求体
        post.setEntity(new StringEntity(jsonBody.toString(), StandardCharsets.UTF_8));
        // 设置请求头，指定发送的内容类型为 JSON
        post.setHeader("Content-Type", APPLICATION_JSON);
        return post;
    }

    /**
     * 请求头集合
     *
     * @param password 密码
     * @return 请求头集合
     */
    private static Map<String, String> getParamMap(String password) {
        Map<String, String> paramMap = new HashMap<>();
        paramMap.put(ACCESS_TOKEN, password);
        paramMap.put(COLLENTION, COMMENT);
        paramMap.put(EVENT, COMMENT_EXPORT_FOR_ADMIN);
        return paramMap;
    }
}