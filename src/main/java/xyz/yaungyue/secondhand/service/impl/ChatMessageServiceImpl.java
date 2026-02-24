package xyz.yaungyue.secondhand.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import xyz.yaungyue.secondhand.model.entity.ChatMessage;
import xyz.yaungyue.secondhand.service.ChatMessageService;
import xyz.yaungyue.secondhand.mapper.ChatMessageMapper;
import org.springframework.stereotype.Service;

/**
* @author yaung
* @description 针对表【bus_chat_message(聊天记录表)】的数据库操作Service实现
* @createDate 2026-02-12 17:21:41
*/
@Service
public class ChatMessageServiceImpl extends ServiceImpl<ChatMessageMapper, ChatMessage>
    implements ChatMessageService{

}




