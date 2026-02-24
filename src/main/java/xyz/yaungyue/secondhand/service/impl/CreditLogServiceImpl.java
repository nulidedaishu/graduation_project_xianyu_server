package xyz.yaungyue.secondhand.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import xyz.yaungyue.secondhand.model.entity.CreditLog;
import xyz.yaungyue.secondhand.service.CreditLogService;
import xyz.yaungyue.secondhand.mapper.CreditLogMapper;
import org.springframework.stereotype.Service;

/**
* @author yaung
* @description 针对表【bus_credit_log(信用积分流水表)】的数据库操作Service实现
* @createDate 2026-02-12 17:21:41
*/
@Service
public class CreditLogServiceImpl extends ServiceImpl<CreditLogMapper, CreditLog>
    implements CreditLogService{

}




