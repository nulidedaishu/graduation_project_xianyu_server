package xyz.yaungyue.secondhand.exception;

/**
 * 异常使用示例和最佳实践
 */
public class ExceptionUsageExample {
    
    /**
     * 示例1: 使用错误码枚举创建业务异常
     */
    public void exampleWithErrorCode() {
        // 使用预定义的错误码
        throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        
        // 或者带原因的异常
        // throw new BusinessException(ErrorCode.USER_NOT_FOUND, cause);
    }
    
    /**
     * 示例2: 使用自定义错误码和消息
     */
    public void exampleWithCustomCode() {
        // 自定义错误码
        throw new BusinessException(40101, "用户不存在");
        
        // 或者带原因的异常
        // throw new BusinessException(40101, "用户不存在", cause);
    }
    
    /**
     * 示例3: 权限异常使用
     */
    public void exampleUnauthorized() {
        // 使用默认的401错误码
        throw new UnauthorizedException("Token已过期");
        
        // 或者使用错误码枚举
        // throw new UnauthorizedException(ErrorCode.UNAUTHORIZED);
    }
    
    /**
     * 示例4: 服务层异常使用
     */
    public void exampleServiceException() {
        // 基础服务异常
        throw new ServiceException("系统繁忙，请稍后再试");
        
        // 带错误码的服务异常
        // throw new ServiceException(50001, "数据库连接异常");
    }
    
    /**
     * 示例5: 在Service层的实际应用
     */
    /*
    @Service
    public class UserServiceImpl implements UserService {
        
        public User findById(Long id) {
            User user = userMapper.selectById(id);
            if (user == null) {
                // 方式1: 使用错误码枚举
                throw new BusinessException(ErrorCode.USER_NOT_FOUND);
                
                // 方式2: 使用自定义错误码
                // throw new BusinessException(40101, "用户不存在");
            }
            return user;
        }
        
        public void updateUser(User user) {
            if (user.getStatus() == 0) {
                throw new BusinessException(40102, "用户已被禁用");
            }
            // 更新逻辑...
        }
        
        public void checkPermission(String token) {
            if (!isValidToken(token)) {
                throw new UnauthorizedException("无效的访问令牌");
            }
        }
    }
    */
    
    /**
     * 示例6: 在Controller层的应用
     */
    /*
    @RestController
    @RequestMapping("/api/users")
    public class UserController {
        
        @GetMapping("/{id}")
        public ApiResponse<User> getUser(@PathVariable Long id) {
            try {
                User user = userService.findById(id);
                return ApiResponse.success(user);
            } catch (BusinessException e) {
                // 全局异常处理器会自动处理并返回相应错误码
                throw e;
            }
        }
        
        @PostMapping("/login")
        public ApiResponse<LoginResponse> login(@RequestBody LoginRequest request) {
            try {
                // 登录逻辑...
                return ApiResponse.success(response);
            } catch (UnauthorizedException e) {
                // 会返回401状态码
                throw e;
            }
        }
    }
    */
}