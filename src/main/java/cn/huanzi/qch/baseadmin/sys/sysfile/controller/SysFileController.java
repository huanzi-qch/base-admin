package cn.huanzi.qch.baseadmin.sys.sysfile.controller;

import cn.huanzi.qch.baseadmin.common.pojo.Result;
import cn.huanzi.qch.baseadmin.sys.sysfile.service.SysFileService;
import cn.huanzi.qch.baseadmin.sys.sysfile.vo.SysFileVo;
import cn.huanzi.qch.baseadmin.util.UUIDUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.support.StandardMultipartHttpServletRequest;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

/**
 * 附件表 Controller
 *
 * 作者：Auto Generator By 'huanzi-qch'
 * 生成日期：2022-11-04 10:49:08
 *
 * 详情请戳：https://www.cnblogs.com/huanzi-qch/p/15294673.html
 */
@RestController
@RequestMapping("/sys/sysFile/")
public class SysFileController {
    @Autowired
    private SysFileService sysFileService;

    //日期格式化
    private final SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMM");

    /**
     * 文件存储根路径
     */
    @Value("${file.upload-path}")
    private String uploadPath;

    /**
     * 应用路径
     */
    @Value("${server.servlet.context-path:}")
    private String contextPath;

    /**
     * 端口
     */
    @Value("${server.port}")
    private String port;

    /**
     * 测试页面
     */
    @GetMapping("test")
    public ModelAndView test(){
        return new ModelAndView("filetest");
    }

    /**
     * ueditor 百度富文本后台接口
     */
    @RequestMapping("ueditor")
    public HashMap<String,String> ueditor(HttpServletRequest request,HttpServletResponse response,@RequestParam("action") String action) throws IOException, ServletException {
        //获取上传配置，转发静态资源文件
        if("config".equals(action)){
            request.getRequestDispatcher("/common/ueditor/config.json").forward(request,response);
            return null;
        }

        //文件上传
        if("ueUpload".equals(action)){
            MultipartFile file = ((StandardMultipartHttpServletRequest) request).getFile("file");
            assert file != null;

            //调用我们的上传文件接口
            SysFileVo upload = upload(file).getData();

            //封装ueditor上传成功返回值
            HashMap<String,String> hashMap = new HashMap<>();
            hashMap.put("state","SUCCESS");
            //前缀，根据实际情况设置
            String prefix =  "http://" + InetAddress.getLocalHost().getHostAddress() + ":" + port + contextPath;
            hashMap.put("url",prefix+"/sys/sysFile/show/"+upload.getId());
            hashMap.put("type",upload.getFileType());
            hashMap.put("size",upload.getFileSize());
            hashMap.put("title",upload.getFileName());

            return hashMap;
        }

        //我这里仅处理的文件上传的情况，其他情况处理自行扩展

        return null;
    }

    /**
     * 上传
     */
    @PostMapping("upload")
    public Result<SysFileVo> upload(@RequestParam("file") MultipartFile file) throws IOException {
        String originalFilename = file.getOriginalFilename();

        /*
            把附件表id当做文件名保存文件
            1、可以避免上传同名文件时造成冲突
            2、文件名无业务含义，附件安全性高
         */
        String fileId = UUIDUtil.getUuid();

        //文件名称
        String fileName = originalFilename.substring(0,originalFilename.lastIndexOf("."));

        //文件类型，后缀名
        String fileType = originalFilename.substring(originalFilename.lastIndexOf(".") + 1);

        //文件大小（MB），保留两位小数点
        double size = file.getSize() / 1024.00 / 1024.00;
        String fileSize = String.format("%.2f",size)+"MB";

        //保存路径，按年月份文件夹
        String path = simpleDateFormat.format(new Date());
        //如果文件夹不存在，创建文件夹
        File pathFile = new File(uploadPath + path);
        if(!pathFile.exists()){
            pathFile.mkdir();
        }

        //保存文件，例如：E:\fj\20221027\123.txt
        file.transferTo(new File(uploadPath  + path + "\\" + fileId + "." + fileType));

        //保存附件表，做好映射关联关系
        SysFileVo sysFileVo = new SysFileVo();
        sysFileVo.setId(fileId);
        sysFileVo.setFileName(fileName);
        sysFileVo.setFileType(fileType);
        sysFileVo.setFileSize(fileSize);
        sysFileVo.setFilePath(path);

        Result<SysFileVo> sysFile = sysFileService.save(sysFileVo);

        return sysFile;
    }

    /**
     * 下载
     */
    @PostMapping("download/{fileId}")
    public ResponseEntity<byte[]> downLoad(@PathVariable String fileId) throws IOException {
        //根据附件id查询附件表
        SysFileVo sysFileVo = sysFileService.get(fileId).getData();
        
        if(sysFileVo == null){
            throw new RuntimeException("下载错误：附件可能已经不存在！");
        }
        String fileName = sysFileVo.getFileName();
        String fileType = sysFileVo.getFileType();
        String path = sysFileVo.getFilePath();

        File file = new File(uploadPath  + path + "\\" + fileId + "." + fileType);
        byte[] fileBytes = new byte[Math.toIntExact(file.length())];
        new FileInputStream(file).read(fileBytes);

        //设置响应头
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        headers.setContentDispositionFormData("attachment", new String(fileName.getBytes(StandardCharsets.UTF_8),StandardCharsets.ISO_8859_1) + "." + fileType);

        //下载文件
        return new ResponseEntity<>(fileBytes, headers, HttpStatus.CREATED);
    }

    /**
     * 显示
     * 浏览器预览，仅支持图片、txt、pdf等，不支持Word、Excel
     */
    @GetMapping("show/{fileId}")
    public void show(HttpServletRequest request, HttpServletResponse response, @PathVariable String fileId) throws ServletException, IOException {
        //根据附件id查询附件表
        SysFileVo sysFileVo = sysFileService.get(fileId).getData();

        if(sysFileVo == null){
            throw new RuntimeException("显示错误：附件可能已经不存在！");
        }
        String fileType = sysFileVo.getFileType();
        String path = sysFileVo.getFilePath();

        //转发附件路径映射接口
        request.getRequestDispatcher("/sys/file/truepath/" + path + "/" + fileId + "." + fileType).forward(request,response);
    }
    
    /**
     * 删除
     */
    @DeleteMapping("delete/{fileId}")
    public Result<Boolean> delete(@PathVariable String fileId) throws ServletException, IOException {
        //根据附件id查询附件表
        SysFileVo sysFileVo = sysFileService.get(fileId).getData();

        if(sysFileVo == null){
            throw new RuntimeException("删除错误：附件可能已经不存在！");
        }
        String fileType = sysFileVo.getFileType();
        String path = sysFileVo.getFilePath();

        //删除物理文件
        File file = new File(uploadPath  + path + "\\" + fileId + "." + fileType);
        boolean delete = file.delete();

        //删除附件表
        sysFileService.delete(fileId);

        return Result.of(delete);
    }
}

