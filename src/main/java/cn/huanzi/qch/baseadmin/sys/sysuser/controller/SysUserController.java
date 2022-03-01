package cn.huanzi.qch.baseadmin.sys.sysuser.controller;

import cn.huanzi.qch.baseadmin.annotation.Decrypt;
import cn.huanzi.qch.baseadmin.annotation.Encrypt;
import cn.huanzi.qch.baseadmin.common.controller.CommonController;
import cn.huanzi.qch.baseadmin.common.pojo.PageInfo;
import cn.huanzi.qch.baseadmin.common.pojo.Result;
import cn.huanzi.qch.baseadmin.sys.sysuser.pojo.SysUser;
import cn.huanzi.qch.baseadmin.sys.sysuser.service.SysUserService;
import cn.huanzi.qch.baseadmin.sys.sysuser.vo.SysUserVo;
import cn.huanzi.qch.baseadmin.util.SecurityUtil;
import cn.huanzi.qch.baseadmin.util.SysSettingUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.User;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/sys/sysUser/")
public class SysUserController extends CommonController<SysUserVo, SysUser, String> {
    @Autowired
    private SysUserService sysUserService;

    @Autowired
    private SecurityUtil securityUtil;

    @GetMapping("user")
    public ModelAndView user(){
        return new ModelAndView("sys/user/user","initPassword", SysSettingUtil.getSysSetting().getUserInitPassword());
    }

    @PostMapping("resetPassword")
    @Decrypt
    @Encrypt
    public Result<SysUserVo> resetPassword(SysUserVo sysUserVo){
        return sysUserService.resetPassword(sysUserVo.getUserId());
    }

    @PostMapping("pageOnLine")
    @Decrypt
    @Encrypt
    public Result<PageInfo<SysUserVo>> pageOnLine(SysUserVo sysUserVo){
        List<Object> allPrincipals = securityUtil.sessionRegistryGetAllPrincipals();
        ArrayList<SysUserVo> sysUserVoList = new ArrayList<>(allPrincipals.size());
        for (Object allPrincipal : allPrincipals) {
            User user = (User) allPrincipal;
            SysUserVo userVo = sysUserService.findByLoginName(user.getUsername()).getData();
            userVo.setPassword(null);
            sysUserVoList.add(userVo);
        }
        PageInfo<SysUserVo> pageInfo = new PageInfo<>();
        pageInfo.setPage(1);//页码
        pageInfo.setPageSize(10000);//页面大小
        pageInfo.setRows(sysUserVoList);//分页结果
        pageInfo.setRecords(sysUserVoList.size());//总记录数
        pageInfo.setTotal(1);//总页数
        return Result.of(pageInfo);
    }

    @DeleteMapping("forced/{loginName}")
    public Result<String> forced( @PathVariable("loginName") String loginName) {
        securityUtil.sessionRegistryRemoveUserAndRemoveUserTokensByUserName(loginName);
        return Result.of("操作成功");
    }
}
