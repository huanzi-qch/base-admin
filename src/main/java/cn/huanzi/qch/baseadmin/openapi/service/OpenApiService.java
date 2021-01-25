package cn.huanzi.qch.baseadmin.openapi.service;

import cn.huanzi.qch.baseadmin.common.pojo.Result;

public interface OpenApiService {
    /**
     * open api test测试
     * @return 测试数据
     */
    Result<String> test();
}
