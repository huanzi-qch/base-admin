package cn.huanzi.qch.baseadmin.sys.sysfile.pojo;

import lombok.Data;
import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;

/**
 * 附件表 实体类
 *
 * 作者：Auto Generator By 'huanzi-qch'
 * 生成日期：2022-11-04 10:49:08
 */
@Entity
@Table(name = "sys_file")
@Data
public class SysFile implements Serializable {
    @Id
        private String id;//表主键，附件id

    private String fileName;//附件名称

    private String fileType;//附件类型

    private String fileSize;//附件大小（MB）

    private String filePath;//附件路径

  
}

