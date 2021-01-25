package cn.huanzi.qch.baseadmin.openapi.service;

import cn.huanzi.qch.baseadmin.common.pojo.Result;
import org.springframework.stereotype.Service;

@Service
public class OpenApiServiceImpl implements OpenApiService {
    @Override
    public Result<String> test() {
        return Result.of("无需登录的接口：OpenApi测试数据！");
    }
}
