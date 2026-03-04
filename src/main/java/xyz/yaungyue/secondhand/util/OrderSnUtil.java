package xyz.yaungyue.secondhand.util;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 订单号生成工具
 *
 * @author yaung
 * @date 2026-02-26
 */
public class OrderSnUtil {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");
    private static final AtomicInteger SEQUENCE = new AtomicInteger(0);
    private static final Random RANDOM = new Random();

    /**
     * 生成订单号
     * 格式：年月日(8位) + 时间戳后5位 + 随机数(3位) + 序列号(4位)
     * 示例：2026022612345678901234
     *
     * @return 订单号
     */
    public static synchronized String generateOrderSn() {
        LocalDateTime now = LocalDateTime.now();

        // 日期部分：8位
        String datePart = now.format(DATE_FORMATTER);

        // 时间戳部分：取当前时间的秒和毫秒部分
        long timestamp = System.currentTimeMillis();
        String timePart = String.valueOf(timestamp).substring(8, 13);

        // 随机数部分：3位
        String randomPart = String.format("%03d", RANDOM.nextInt(1000));

        // 序列号部分：4位，防止同一毫秒重复
        int seq = SEQUENCE.incrementAndGet();
        if (seq > 9999) {
            SEQUENCE.set(0);
            seq = 0;
        }
        String seqPart = String.format("%04d", seq);

        return datePart + timePart + randomPart + seqPart;
    }
}
