package cn.huanzi.qch.baseadmin.common.pojo;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.thymeleaf.util.StringUtils;

/**
 * 分页条件
 */
@Data
public class PageCondition {
    private int page;//当前页码
    private int rows;//页面大小
    private String sidx;//排序字段
    private String sord;//排序方式

    /**
     * 获取JPA的分页查询对象
     */
    @JsonIgnore
    public PageRequest getPageable() {
        //处理非法页码
        if (page <= 0) {
            page = 1;
        }
        //处理非法页面大小
        if (rows <= 0) {
            rows = 10;
        }
        //处理排序
        if(!StringUtils.isEmpty(sidx) && !StringUtils.isEmpty(sord)){
            Direction direction = "desc".equals(sord.toLowerCase()) ? Direction.DESC : Direction.ASC;
            return PageRequest.of(page - 1, rows, new Sort(direction, sidx));
        }
        return PageRequest.of(page - 1, rows);
    }
}
