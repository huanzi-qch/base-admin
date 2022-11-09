package cn.huanzi.qch.baseadmin.sys.sysfile.service;

import cn.huanzi.qch.baseadmin.common.service.CommonServiceImpl;
import cn.huanzi.qch.baseadmin.sys.sysfile.pojo.SysFile;
import cn.huanzi.qch.baseadmin.sys.sysfile.repository.SysFileRepository;
import cn.huanzi.qch.baseadmin.sys.sysfile.vo.SysFileVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

/**
 * 附件表 ServiceImpl
 *
 * 作者：Auto Generator By 'huanzi-qch'
 * 生成日期：2022-11-04 10:49:08
 */
@Service
@Transactional
public class SysFileServiceImpl extends CommonServiceImpl<SysFileVo, SysFile, String> implements SysFileService{

    @PersistenceContext
    private EntityManager em;
    
    @Autowired
    private SysFileRepository sysFileRepository;

}

