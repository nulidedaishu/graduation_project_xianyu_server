package xyz.yaungyue.secondhand.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import xyz.yaungyue.secondhand.model.entity.File;
import xyz.yaungyue.secondhand.service.FileService;
import xyz.yaungyue.secondhand.mapper.FileMapper;
import org.springframework.stereotype.Service;

/**
* @author yaung
* @description 针对表【sys_file(文件上传记录表)】的数据库操作Service实现
* @createDate 2026-02-12 17:21:09
*/
@Service
public class FileServiceImpl extends ServiceImpl<FileMapper, File>
    implements FileService{

}




