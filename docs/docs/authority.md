## 权限管理 <br/>
　　增删改查<br/>
![](https://img2018.cnblogs.com/blog/1353055/201909/1353055-20190917113851313-806820342.gif)<br/>

## 动态权限加载 <br/>
　　权限的加载并不是写死在代码，而是动态从数据库读取，每次调用save方法时更新权限集合<br/>
　　1、妲己是ROLE_USER权限，权限内容为空，无权访问/sys/下面的路径（http://localhost:8888/sys/sysUser/get/1）<br/>
　　2、使用sa超级管理员进行权限管理编辑，给ROLE_USER的权限内容添加 /sys/**，妲己立即有权限访问（http://localhost:8888/sys/sysUser/get/1）<br/>
![](https://img2018.cnblogs.com/blog/1353055/201909/1353055-20190917111943507-31961761.gif)<br/>