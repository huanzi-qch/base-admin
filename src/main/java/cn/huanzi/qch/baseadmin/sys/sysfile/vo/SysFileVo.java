package cn.huanzi.qch.baseadmin.sys.sysfile.vo;

import cn.huanzi.qch.baseadmin.common.pojo.PageCondition;
import lombok.Data;
import java.io.Serializable;
import java.util.Date;

/**
 * 附件表 Vo
 *
 * 作者：Auto Generator By 'huanzi-qch'
 * 生成日期：2022-11-04 10:49:08
 */
@Data
public class SysFileVo extends PageCondition implements Serializable {
    private String id;//表主键，附件id

    private String fileName;//附件名称

    private String fileType;//附件类型

    private String fileSize;//附件大小（MB）

    private String filePath;//附件路径

  
}

