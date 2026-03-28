package xyz.yaungyue.secondhand.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import xyz.yaungyue.secondhand.model.entity.AIConfig;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

/**
 * AI配置Mapper
 */
@Mapper
public interface AIConfigMapper extends BaseMapper<AIConfig> {

    /**
     * 获取启用的默认配置
     */
    @Select("SELECT * FROM sys_ai_config WHERE enabled = 1 AND is_default = 1 LIMIT 1")
    AIConfig selectDefaultConfig();

    /**
     * 获取启用的配置（当没有默认配置时）
     */
    @Select("SELECT * FROM sys_ai_config WHERE enabled = 1 ORDER BY id LIMIT 1")
    AIConfig selectFirstEnabledConfig();
}
