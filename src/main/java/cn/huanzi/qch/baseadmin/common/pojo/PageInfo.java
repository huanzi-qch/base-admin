package cn.huanzi.qch.baseadmin.common.pojo;

import cn.huanzi.qch.baseadmin.util.CopyUtil;
import lombok.Data;
import org.hibernate.query.internal.NativeQueryImpl;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.repository.support.PageableExecutionUtils;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import java.util.List;

/**
 * 分页对象
 */
@Data
public class PageInfo<M> {
    private int page;//当前页码
    private int pageSize;//页面大小
    private String sidx;//排序字段
    private String sord;//排序方式

    private List<M> rows;//分页结果
    private int records;//总记录数
    private int total;//总页数

    /**
     * 获取统一分页结果
     */
    public static <M> PageInfo<M> of(Page page, Class<M> entityModelClass) {
        int records = (int) page.getTotalElements();
        int pageSize = page.getSize();
        int total = records % pageSize == 0 ? records / pageSize : records / pageSize + 1;

        PageInfo<M> pageInfo = new PageInfo<>();
        pageInfo.setPage(page.getNumber() + 1);//页码
        pageInfo.setPageSize(pageSize);//页面大小

        String sortString = page.getSort().toString();
        if(!"UNSORTED".equals(sortString)){
            String[] split = sortString.split(":");
            pageInfo.setSidx(split[0].trim());//排序字段
            pageInfo.setSord(split[1].trim().toLowerCase());//排序方式
        }
        pageInfo.setRows(CopyUtil.copyList(page.getContent(), entityModelClass));//分页结果
        pageInfo.setRecords(records);//总记录数
        pageInfo.setTotal(total);//总页数
        return pageInfo;
    }

    /**
     * 获取JPA的分页对象
     */
    public static Page getJpaPage(Query query, PageRequest pageRequest, EntityManager em) {
        query.setFirstResult((int) pageRequest.getOffset());
        query.setMaxResults(pageRequest.getPageSize());

        //获取分页结果
        return PageableExecutionUtils.getPage(query.getResultList(), pageRequest, () -> {
            //设置countQuerySQL语句
            Query countQuery = em.createNativeQuery("select count(*) from ( " + ((NativeQueryImpl) query).getQueryString() + " ) count_table");
            //设置countQuerySQL参数
            query.getParameters().forEach(parameter -> countQuery.setParameter(parameter.getName(), query.getParameterValue(parameter.getName())));
            //返回一个总数
            return Long.valueOf(countQuery.getResultList().get(0).toString());
        });
    }
}
