package xyz.yaungyue.secondhand.config;

import cn.dev33.satoken.stp.StpUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Enumeration;

/**
 * 请求日志拦截器
 * 记录每个接口调用的详细信息
 */
@Slf4j
@Component
public class RequestLoggingInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 记录请求开始时间
        long startTime = System.currentTimeMillis();
        request.setAttribute("startTime", startTime);
        
        // 获取请求基本信息
        String method = request.getMethod();
        String uri = request.getRequestURI();
        String queryString = request.getQueryString();
        String clientIp = getClientIpAddress(request);
        String userAgent = request.getHeader("User-Agent");
        
        // 获取Sa-Token用户信息（如果已登录）
        String loginId = null;
        String tokenValue = null;
        try {
            if (StpUtil.isLogin()) {
                loginId = String.valueOf(StpUtil.getLoginId());
                tokenValue = StpUtil.getTokenValue();
            }
        } catch (Exception e) {
            // 如果获取失败，说明未登录或token无效
            loginId = "未登录";
            tokenValue = "无";
        }
        
        // 记录请求参数
        StringBuilder params = new StringBuilder();
        Enumeration<String> paramNames = request.getParameterNames();
        while (paramNames.hasMoreElements()) {
            String paramName = paramNames.nextElement();
            String[] paramValues = request.getParameterValues(paramName);
            if (paramValues != null) {
                for (int i = 0; i < paramValues.length; i++) {
                    if (params.length() > 0) {
                        params.append(", ");
                    }
                    // 敏感信息脱敏处理
                    String displayValue = maskSensitiveData(paramName, paramValues[i]);
                    params.append(paramName).append("=").append(displayValue);
                }
            }
        }
        
        // 记录请求头信息（关键头部）
        StringBuilder headers = new StringBuilder();
        String[] importantHeaders = {"Authorization", "Content-Type", "Accept", "X-Requested-With"};
        for (String headerName : importantHeaders) {
            String headerValue = request.getHeader(headerName);
            if (headerValue != null) {
                if (headers.length() > 0) {
                    headers.append(", ");
                }
                headers.append(headerName).append("=").append(maskAuthorizationHeader(headerName, headerValue));
            }
        }
        
        // 输出请求日志（简洁版）
        log.info("请求开始 - 方法: {}, 路径: {}{}, 客户端IP: {}, 用户ID: {}, 参数: {}",
                method, uri, queryString != null ? "?" + queryString : "", clientIp, loginId,
                params.length() > 0 ? params.toString() : "无");
        
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        // 获取处理结束时间
        long startTime = (Long) request.getAttribute("startTime");
        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;
        
        // 获取响应信息
        int status = response.getStatus();
        String method = request.getMethod();
        String uri = request.getRequestURI();
        
        // 获取Sa-Token用户信息
        String loginId = "未登录";
        try {
            if (StpUtil.isLogin()) {
                loginId = String.valueOf(StpUtil.getLoginId());
            }
        } catch (Exception e) {
            // 忽略异常
        }
        
        // 输出响应日志（简洁版）
        String logMessage = String.format("请求结束 - 方法: %s, 路径: %s, 用户ID: %s, 状态: %d, 耗时: %dms",
                method, uri, loginId, status, duration);

        // 根据状态码记录不同级别的日志
        if (status >= 200 && status < 300) {
            log.info(logMessage);
        } else if (status >= 400 && status < 500) {
            log.warn(logMessage);
        } else if (status >= 500) {
            log.error(logMessage);
            if (ex != null) {
                log.error("异常详情: ", ex);
            }
        }
    }
    
    /**
     * 获取客户端真实IP地址
     */
    private String getClientIpAddress(HttpServletRequest request) {
        String[] headers = {
            "X-Forwarded-For",
            "X-Real-IP", 
            "Proxy-Client-IP",
            "WL-Proxy-Client-IP",
            "HTTP_X_FORWARDED_FOR",
            "HTTP_X_FORWARDED",
            "HTTP_X_CLUSTER_CLIENT_IP",
            "HTTP_CLIENT_IP",
            "HTTP_FORWARDED_FOR",
            "HTTP_FORWARDED",
            "HTTP_VIA",
            "REMOTE_ADDR"
        };
        
        for (String header : headers) {
            String ip = request.getHeader(header);
            if (ip != null && ip.length() != 0 && !"unknown".equalsIgnoreCase(ip)) {
                // 处理多个IP的情况（如：192.168.1.1, 192.168.1.2）
                if (ip.contains(",")) {
                    ip = ip.split(",")[0].trim();
                }
                return ip;
            }
        }
        
        return request.getRemoteAddr();
    }
    
    /**
     * 对敏感数据进行脱敏处理
     */
    private String maskSensitiveData(String paramName, String value) {
        if (value == null) {
            return "null";
        }
        
        // 密码类字段脱敏
        if (paramName.toLowerCase().contains("password") || 
            paramName.toLowerCase().contains("pwd")) {
            return "******";
        }
        
        // 手机号脱敏
        if (paramName.toLowerCase().contains("phone") && value.matches("\\d{11}")) {
            return value.replaceAll("(\\d{3})\\d{4}(\\d{4})", "$1****$2");
        }
        
        // 邮箱脱敏
        if (paramName.toLowerCase().contains("email") && value.contains("@")) {
            return value.replaceAll("(\\w?)(\\w+)(\\w)(@\\w+\\.[a-z]+(\\.[a-z]+)?)", "$1****$3$4");
        }
        
        // Token脱敏
        if (paramName.toLowerCase().contains("token")) {
            return maskToken(value);
        }
        
        // 普通字段截取长度
        if (value.length() > 50) {
            return value.substring(0, 50) + "...";
        }
        
        return value;
    }
    
    /**
     * 对Authorization头进行脱敏
     */
    private String maskAuthorizationHeader(String headerName, String value) {
        if ("Authorization".equalsIgnoreCase(headerName) && value != null) {
            return maskToken(value);
        }
        return value;
    }
    
    /**
     * 对Token进行脱敏显示
     */
    private String maskToken(String token) {
        if (token == null || token.isEmpty()) {
            return "无";
        }
        if (token.length() <= 10) {
            return "****";
        }
        return token.substring(0, 5) + "****" + token.substring(token.length() - 5);
    }
}