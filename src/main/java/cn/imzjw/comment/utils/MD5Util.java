package cn.imzjw.comment.utils;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * MD5 工具栏
 *
 * @author <a href="https://blog.imzjw.cn">@小嘉的部落格</a>
 * @date 2024-4-26
 */
public class MD5Util {

    /**
     * 将输入的字符串转换为MD5哈希值。
     *
     * @param original 输入的字符串
     * @return MD5哈希值，32位小写十六进制数
     */
    public static String toMD5(String original) {
        try {
            // 创建MessageDigest实例，指定MD5算法
            MessageDigest md = MessageDigest.getInstance("MD5");
            // 对原始字符串进行MD5加密
            md.update(original.getBytes());
            // 获取加密后的字节数组
            byte[] digest = md.digest();
            // 转换为十六进制表示
            StringBuilder sb = new StringBuilder();
            for (byte b : digest) {
                sb.append(String.format("%02x", b));
            }
            // 转换为小写
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("MD5 algorithm not found", e);
        }
    }
}
