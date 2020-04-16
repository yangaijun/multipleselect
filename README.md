# multipleselect
java mybatis 实现简单多表通用查询

## 中后台 vue ElementUI 組件版

(文檔地址)[http://115.159.65.195:8080/vefdoc/#/start]

####　mybatis-puls 图形化数据库生成XML，PO，Service，Controller工具

(GITHUB)[https://github.com/yangaijun/generate-mybatis-plus]

歡迎交流

### 简介

	实现项目中比较基本的多表通用查询。

​	实现简单的实体类操作多表,  首先你的项目是使用了mybatis-plus 才可以使用。

​	 不做任何更改，也不会对项目产生任何影响，与手写XML 功能一样。

​     通过解析实体，调用通用的XML来实现多表查询， 提供一个设计多表查询的思路，复杂的Sql嵌套等目前并不支持。

#### 目前支持：

​	 left join方式，能关联的两张表的实体中关联字段名称必须一样，数据库字段可以不一样可以通@TableField注解来解决，right join 换个位置喽 其它方式还没有）

​    where 基本查询条件, sql函数 等

​	分页 查询

​	order 排序

​	简易 group by, 还没有Having哦

可以用来三两句搞定一些简单关联查询业务，解决不需要写的代码

### 设计说明

* 如何关联表？

  ​		找第一张表注解为 TableId （mybatis-plus 注解）的属性名， 到每二张表找同样的属性名， 如果没找到，反过来找，如果还没找到，挨个属性找。以此类推，实现关联的前提条件是 主从表的实体关联列名必须是一样的

  ```java
  // user 表
  @TableId
  private Integer userId
  // address 表
  @TableId
  private Integer addressId
  private Integer userId
  //那么自动条件为  user.user_id = address.user_id
  //也可以是
  @TableId(value="id")
  private Integer userId
  // address 表
  @TableId(value="id")
  private Integer addressId
  @TableField(value="test_user_id")
  private Integer userId
  //目前只有left join
  //那么自动条件为  user.id = address.test_user_id
  //如果符合这设计条件，你就往里扔就完事了
  
  ```

### 使用说明

   1.将 com.freedomen.multipselect 包放到你的项目中

   2.使  com.freedomen.multipselect.mapper里的xml 要被扫描到，或手动配置  

3. com.freedomen.multipselect.service也要被发现

```java
//引入service
@Autowired
private MultipleService multipleService;
//表关联， 关联用户表和地址表，查找 用户表的所有字段和地址表的所有字段
MultipleSelect multipleSelect = MultipleSelect.newInstance("${1}", new User(), new Address());

multipleSelect
	.where("${0}")
    .like("userName", "张三");

multipleService.mulSelect(multipleSelect);
```

#### 查找字段

```java
//MultipleSelect.newInstance 的第一个参数是所要查找的字段
//${0} 或 ${user} 表是第一张表的所有字段  ${0}.userName或${user}.userName表示userName字段， 默认第一张表的字段全部都返回的。 ${}中间的参数可以是后面实体的下标，也可以是表名 如user、user_address
//字段中有@TableField(exist=false)注解也是被跳过的
//下面是要订单表的所有信息 和用户的姓名与号码 和地址

MultipleSelect.newInstance("${1}.userName,${1}.userPhone,${2}", new Orders(), new User(), new Address());
```

####  查找条件

* eq:  =
* notEq:  !=
* like:  LIKE  （前置已经加了 '%'）
* between:  between
* and:  改变接下来的连接方式为 AND练级（默认）
* andOnce:  改变接下来一个的连接方式为 AND
* or:  改变接下来的连接方式为 OR
* orOnce 改变接下来一个的连接方式为 OR
* division：括号  ，不支持括号嵌套括号
* in:  IN
* notIn:  NOT IN
* notLike:   NOT LIKE
* isNull:  IS NULL
* isNotNull: IS NOT NULL
* sql: 简易自定义带sql代码片段
* ...

```java
//实例好 查找实体后可以操作实体

//注意： 如何实体内属性有值  将会以 eq方式and连接做为where 条件
/*
可以关联的必要条件
Orders:
	@TableId //或者数据库字段为其它@TableId(value="id")
	private Long ordersId;
	private Long userId;
	...
User: 
	@TableId
	private Long userId;
	...
Address:
	@TableId
	private Long addressId;
	private Long userId;
	...
*/
MultipleSelect multipleSelect = MultipleSelect.newInstance("${1}.userName,${1}.userPhone,${2}", new Orders(), new User(), new Address());

multipleSelect
	.where("${0}") //哪张表
	.eq("ordersId", 1) //并且 订单id = 1
	.like("ordersName", "cmcc") //并且 订单名称 like ''%cmcc'
	.or() //改变后续操作关系为 OR， 默认为AND
    .notEq("orderSno", "123"); //或者 orderSno 不等于 '123'
    
multipleSelect
	.where("${1}") //哪张表接着用户表 默认and连接  可以 .or()改为 OR
	.in("userId", [1, 2, 3]); // 并且userId in [1, 2, 3]
    
multipleSelect
	.where("${2}")
    .or()
	.like("adressDetails", "江苏"); //或者  地址 like '江苏'

multipleService.mulSelect(multipleSelect); //查询

//括号
multipleSelect.where("${0}")
    .eq("componyId", 1)
    .division()
    .like("userName", "abcd")
    .or()
    .like("userPhone", "abcd");
// 部分sql: compony_id = 1 and (user_name = 'abcd' or user_phone = 'abcd')    
```

#### 排序

```java
//MultipleSelect.setOrderBy(...columns)
MultipleSelect.setOrderBy("${0}.createTime", "${1}.ordersName desc", "${2}.userId asc", ...)
```

#### 分组

```java
//分组一般都要结合聚集函数使用，可以使用的：AVG, COUNT, MAX, MIN, SUM
/**统计用户订单总额*/ 
// 聚集函数使用 函数名:${表名/下标}.属性名; 不可以重命名哦， 下面的sum 字段仍然是 price
MultipleSelect.newInstance("${1}, sum:${0}.price", new Orders(), new User());
//(...columns)
MultipleSelect.setGoupBy("${0}.userId", ...);
```

#### SQL 方法使用

```java
//如  查找创建日期为 2019年10月 的订单;
//两个问号对应两个参数, 其中使用的仍然是实体的Filed名，不是数据表的字段名
multipleSelect.where("${orders}").sql("year(createTime)=? and month(createTime)=?", new Object[]{2019, 10});
```

#### 分页

```java
//MultipleSelect.setPage(pageNo, pageSize);
MultipleSelect.setPage(1, 15); //第一页 每页 15条
```

#### multipleService.mulSelect返回结果

```java
//MultipleResult
/*	原型
	private List<Map<String, Object>> data; //结果数据
	private Integer pageNo; 	//如果设置了分页 会有
	private Integer pageSize; 	//如果设置了分页 会有
	private Integer total;		//如果设置了分页 会有
*/
```

#### 逻辑删除

```java
//默认是读取  mybatis-plus 的 TableLogic 注解 0 未删除，
//如果不是用 0 表示未删除， 可以修改 MultipleSelect 的 setCustomWhere 方法中的下面这段中的 0 
 
if (logic != null)
    sb.append(" AND ")
    .append(te.getNickName())
    .append(".")
    .append(logic)
    .append(" = ")
    .append("0");

```

