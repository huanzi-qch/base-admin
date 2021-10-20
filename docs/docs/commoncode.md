## 一套通用代码 <br/>
　　项目中，我们每张单表都有基础的get、save（插入/更新）、list、page、delete接口，但是这样每个单表都要写着一套代码，重复而繁杂，我们应该写一套通用common代码，每个单表去继承从而实现这套基础接口，同时，我们应该用Vo去接收、传输数据，实体负责与数据库表映射，单表的增删改查接口，直接继承这一套通用代码即可实现，无需再重复编写，大大提升开发效率！<br/>
```
|-- base-admin
    |-- src
    |   |-- main
    |   |   |-- java
    |   |   |   |-- cn
    |   |   |       |-- huanzi
    |   |   |           |-- qch
    |   |   |               |-- baseadmin
    |   |   |                   |-- BaseAdminApplication.java   【APP启动类】
    |   |   |                   |-- 省略其他部分...
    |   |   |                   |-- common  【一套通用代码，单表继承即可实现CRUD、分页等基础接口】
    |   |   |                   |   |-- controller  【通用Controller】
    |   |   |                   |   |   |-- CommonController.java
    |   |   |                   |   |-- pojo        【公用实体类、例如统一返回对象Result】
    |   |   |                   |   |   |-- HolidayVo.java
    |   |   |                   |   |   |-- IpVo.java
    |   |   |                   |   |   |-- MonitorVo.java
    |   |   |                   |   |   |-- PageCondition.java
    |   |   |                   |   |   |-- PageInfo.java
    |   |   |                   |   |   |-- ParameterRequestWrapper.java
    |   |   |                   |   |   |-- Result.java
    |   |   |                   |   |-- repository  【通用repository】
    |   |   |                   |   |   |-- CommonRepository.java
    |   |   |                   |   |-- service     【通用service】
    |   |   |                   |       |-- CommonService.java
    |   |   |                   |       |-- CommonServiceImpl.java
    |-- 省略其他部分...
```
## 单表使用
　　单表直接继承common通用代码，即可实现CRUD、分页等基础接口（例如sys_user表）<br/>
```
@Data
public class SysUserVo extends PageCondition implements Serializable {
    //省略其他代码...
}
```
```
@RestController
@RequestMapping("/sys/sysUser/")
public class SysUserController extends CommonController<SysUserVo, SysUser, String> {
    //省略其他代码...
}
```
```
public interface SysUserService extends CommonService<SysUserVo, SysUser, String> {
    //省略其他代码...
}

@Service
@Transactional
public class SysUserServiceImpl extends CommonServiceImpl<SysUserVo, SysUser, String> implements SysUserService {
    //省略其他代码...
}
```
```
@Repository
public interface SysUserRepository extends CommonRepository<SysUser, String> {
    //省略其他代码...
}
```
　　注意：jpa原生的save方法，更新的时候是全属性进行updata，如果实体类的属性没有值它会帮你更新成null，如果你想更新部分字段请在通用CommonServiceImpl使用这个save方法，我这里是在调用save之前先查询数据库获取完整对象，将要更新的值复制到最终传入save方法的对象中，从而实现局部更新<br/>
　　另外，直接调用EntityManager的merge，也是传什么就保存什么<br/>
```
@PersistenceContext
private EntityManager em;

//注意：直接调用EntityManager的merge，传进去的实体字段是什么就保存什么
E e = em.merge(entity);
em.flush();
```

>　　单表增、删、改、查基础功能相似，代码高度相似，我们新增一个单表操作的步骤：复制、粘贴、修改文件夹、文件名、类名、修改传参实体对象...，为了实现快速开发，我们应该使用代码生成器自动生成一套单表的基础增、删、改、查接口