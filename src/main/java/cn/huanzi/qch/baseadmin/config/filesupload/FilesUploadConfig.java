package cn.huanzi.qch.baseadmin.config.filesupload;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * 附件Config配置
 *
 * 详情请戳：https://www.cnblogs.com/huanzi-qch/p/15294673.html
 */
@Component
public class FilesUploadConfig implements WebMvcConfigurer {
    /**
     * 附件存储路径
     */
    @Value("${file.upload-path}")
    private String uploadPath;

    /**
     * 附件路径映射，映射后可直接通过接口访问文件
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/sys/file/truepath/**").addResourceLocations("file:"+uploadPath);
    }
}
