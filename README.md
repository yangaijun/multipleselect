# multipleselect
java mybatis 多表查询

### 简介

 	实现简单的实体类操作多表,  首先你的项目是使用了mybatis-plus 才可以使用

### 设计说明

* 如何关联表？

  找第一张表注解为 TableId （mybatis-plus 注解）的属性名， 到每二张表找同样的属性名， 如果没找到，反过来找，如果还没找到，挨个属性找。以此类推，实现关联的前提条件是 主从表的关联例名必须一样

  ```java
  // user 表
  @TableId
  private Integer userId
  // address 表
  @TableId
  private Integer addressId
  private Integer userId
  ```

  

  

​			

### 使用说明

将 com.freedomen.multipselect 包放到你的项目中，使  com.freedomen.multipselect.mapper里的xml 要被扫描到，或手动配置，  com.freedomen.multipselect.service也要被发现

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

//下面是要订单表的所有信息 和用户的姓名与号码 和地址
MultipleSelect.newInstance("${1}.userName,${1}.userPhone,${2}", new Orders(), new User(), new Address());
```

####  查找条件

* eq:  =
* notEq:  !=
* like:  LIKE  （前置已经加了 '%'）
* between:  between
* and:  改变连接方式为 AND练级（默认）
* or:  改变 连接方式为 OR
* division：括号
* in:  IN
* notIn:  NOT IN
* notLike:   NOT LIKE
* ...等等

```java
//实例好 查找实体后可以操作实体
//注意： 如何实体内属性有值  将会以 eq方式and连接做为where 条件
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

```

#### 排序

```java
//MultipleSelect.setOrderBy(...columns)
MultipleSelect.setOrderBy("${1}.ordersName desc", "${2}.userId asc", ...)
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

