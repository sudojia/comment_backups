package cn.imzjw.comment.utils;

import cn.imzjw.comment.TwikooBackups;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
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
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * @author sudojia
 * @version 1.0
 * @description CommonService
 * @date 2024/7/7
 * @website https://blog.imzjw.cn
 * @copyright Copyright 2024 sudojia, Inc All rights reserved.
 */
public enum CommonService {
    /**
     * 获取日志记录器对象
     */
    ACCESS_TOKEN("accessToken"),
    COLLECTION("collection"),
    EVENT("event"),
    COMMENT("comment"),
    COMMENT_EXPORT_FOR_ADMIN("COMMENT_EXPORT_FOR_ADMIN"),
    TWIKOO_COMMENT_JSON("twikoo-comment");

    /**
     * 获取日志记录器对象
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(TwikooBackups.class);

    private final String value;

    CommonService(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    /**
     * 构建请求对象
     *
     * @param password  twikoo 密码
     * @param twikooUrl twikoo 地址
     * @return HttpPost 对象
     */
    public static HttpPost getPostRequest(String password, String twikooUrl) {
        JSONObject body = new JSONObject();
        CommonService.buildAdminCommentExportParams(MD5Util.toMD5(password)).forEach(body::put);
        HttpPost post = new HttpPost(twikooUrl);
        // 设置请求体
        post.setEntity(new StringEntity(body.toString(), StandardCharsets.UTF_8));
        // 设置请求头，指定发送的内容类型为 JSON
        post.setHeader("Content-Type", "application/json");
        return post;
    }

    /**
     * 处理成功的HTTP响应。
     *
     * @param response HTTP响应对象，包含服务器返回的数据。
     * @throws IOException 如果处理响应体时发生IO错误。
     */
    public static void checkResponseCode(HttpResponse response) throws IOException {
        // 将字符串解析为JSONObject
        String entity = EntityUtils.toString(response.getEntity());
        JSONObject data = new JSONObject(entity);
        // 获取响应中的错误码
        int errorCode = data.getInt("code");
        // 根据错误码采取不同的处理策略
        switch (errorCode) {
            case 1024:
            case 1001:
                LOGGER.warn(data.getString("message"));
                break;
            default:
                handleValidData(data, entity);
                break;
        }
    }

    /**
     * 将 json 内容保存到文件
     *
     * @param content json 响应内容
     */
    public static boolean saveToFile(String content) {
        try {
            // 获取当前日期
            String currentDate = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
            // 构建新的文件名，包含日期
            String fileNameWithDate = TWIKOO_COMMENT_JSON.getValue() + "-" + currentDate + ".json";
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
            // LOGGER.info("成功创建 json 文件，正在写入数据...");
            return true;
        } catch (IOException e) {
            LOGGER.error("创建 json 文件失败：", e);
            return false;
        }
    }

    /**
     * 构建请求参数 Map。
     *
     * @param password twikoo 密码
     * @return 包含请求参数的不可变 Map 对象
     */
    private static Map<String, String> buildAdminCommentExportParams(String password) {
        Map<String, String> params = new HashMap<>();
        params.put(ACCESS_TOKEN.getValue(), password);
        params.put(COLLECTION.getValue(), COMMENT.getValue());
        params.put(EVENT.getValue(), COMMENT_EXPORT_FOR_ADMIN.getValue());
        return Collections.unmodifiableMap(params);
    }

    /**
     * 处理有效数据，将数据保存到文件中。
     * 此方法用于检查传入的JSONObject是否包含有效的"data"数组，如果数组不为空，
     * 则尝试将数据保存到本地文件。如果保存成功，记录日志信息；如果保存失败，同样记录日志。
     * 如果"data"数组为空或不存在，则记录错误日志提示数据为空。
     *
     * @param data   包含待处理数据的JSONObject对象。
     * @param entity 评论数据内容
     */
    private static void handleValidData(JSONObject data, String entity) {
        // 检查data对象中是否包含"data"键，并且"data"数组不为空
        if (data.has("data") && !data.getJSONArray("data").isEmpty()) {
            // 写入内容到文件并保存到本地
            LOGGER.info(CommonService.saveToFile(entity) ? "文件已保存到本地" : "写入文件或保存失败");
        } else {
            // 如果"data"数组为空或不存在，记录错误日志
            LOGGER.error("data 数据为空，请检查是否有评论数据，或自行 PostMan 检查一下");
        }
    }
}
