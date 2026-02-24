package xyz.yaungyue.secondhand.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import xyz.yaungyue.secondhand.model.entity.Notice;
import xyz.yaungyue.secondhand.service.NoticeService;
import xyz.yaungyue.secondhand.mapper.NoticeMapper;
import org.springframework.stereotype.Service;

/**
* @author yaung
* @description 针对表【sys_notice(系统通知表)】的数据库操作Service实现
* @createDate 2026-02-12 17:21:09
*/
@Service
public class NoticeServiceImpl extends ServiceImpl<NoticeMapper, Notice>
    implements NoticeService{

}




