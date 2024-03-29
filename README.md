# gmall0105 本地修改版本

# gmall-user用户服务8080

```**gmall-user-service     8070**<br/>
**gmall-user-web         8071**<br/>
**gmall-manage-service   8072**<br/>
**gmall-manage-web       8073**<br/>
**gmall-item-web         8074**<br/>
**gmall-redission-test   8075**<br/>
**gmall-search-service   8076**<br/>
**gmall-search-web       8077**<br/>
**gmall-cart-service     8078**<br/>
**gmall-cart-web         8079**<br/>
**gmall-passport-web     8081**<br/>
**gmall-order-service    8083**<br/>
**gmall-order-web        8082**<br/>
**gmall-payment          8084**<br/>
**gware-manage           8085**<br/>

**gmall-secKill          8001**<br/>
```

对应5.24

#### 抽取api工程（负责管理项目中所有的接口和bean）

##### gmall-api：主要用于存放service和bean的包 service的实现类不放进来

    1 首先用maven创建一个gmall-api的工程
    2 引入tk通用mapper(映射类)
    3 将XXXService接口和所有的bean类都放到api中  
      （本工程是把interface和bean放在一个模块里面，也可以分开分别创建一个模块）
    4 service、service实现、controller、mapper、mapper.xml中所有的bean的引入全部修改引入路径
    问题：为什么要在这里引入？？因为映射类要跟着bean bean用到了通用mapper注解

#### 抽取util工程（就是‘前后端‘都要用到的）

    spring-boot-starter-test	测试(springboot有默认版本号)
    spring-boot-starter-web	    内含tomcat容器、HttpSevrletRequest等 (springboot有默认版本号)
    fastjson	                json工具
    commons-lang3	            方便好用的apache工具库
    commons-beanutils	        方便好用的apache处理实体bean工具库
    commons-codec	            方便好用的apache解码工具库
    httpclient	                restful调用客户端

##### 架构介绍

    1 项目中的通用框架，是所有应用工程需要引入的包
         例如：springboot、common-langs、common-beanutils
    2 基于soa的架构理念，项目分为web前端controller(webUtil)
        Jsp、thymeleaf、cookie工具类
        加入commonUtil
    3 基于soa的架构理念，项目分为web后端service(serviceUtil)
        Mybatis、mysql、redis
        加入commonUtil

##### 依赖策略

    1 新建一个web的前端controller模块的项目
    Controller = parent + api + webUtil
    2 新建一个web的后端service模块的项目
    service = parent + api + serviceUtil

#### 抽取gmall-web-util

    Thymeleaf/jsp/freemarker	springboot自带页面渲染工具(springboot有默认版本号)

#### 抽取gmall-service-util

    spring-boot-starter-jdbc	数据库驱动(springboot有默认版本号)
    mysql-connector-java	    数据库连接器(springboot有默认版本号)
    mybatis-spring-boot-starter	mybatis

##### 还有

  <dependency>
    <groupId>redis.clients</groupId>
    <artifactId>jedis</artifactId>
  </dependency>


为何要将gmall-user的service和web分开 因为并发量高的时候访问量会很大尤其是service层，所以要分开

dubbo框架放入到common util里面 别的就都能引用 因为web层和service层将来都需要使用dubbo进行通讯 dubbo框架的服务和消费者之间的通讯使用的是自己封装的dubbo的协议，不是http的  
而dubbo的consumer和provider与zookeeper注册中心的通讯使用的是zookeeper的协议，因为zokeeper不是dubbo写的 所以下面

# dubbo的通讯协议名称

spring.dubbo.protocol.name=dubbo

# zookeeper的通讯协议的名称

spring.dubbo.registry.protocol=zookeeper springcloud使用的是http的rest风格的协议

# dubbo的配置

# dubbo中的服务名称

spring.dubbo.application=user-web

# dubbo的通讯协议名称

spring.dubbo.protocol.name=dubbo

# zookeeper注册中心的地址

spring.dubbo.registry.address=192.168.222.20:2181

# zookeeper的通讯协议的名称

spring.dubbo.registry.protocol=zookeeper

# dubbo的服务的扫描路径

spring.dubbo.base-package=com.atguigu.gmall 

# 设置超时时间

spring.dubbo.consumer.timeout=600000

# 设置是否检查服务存在

spring.dubbo.consumer.check=false

#### consumeryexuyao xiang zookeeper注册中心注册

使用dubbo后 service为了能被调用需要使用dubbo的@service去扫描 import com.alibaba.dubbo.config.annotation.Service; eg. @Service public
class UserServiceImpl implements UserService

    <select id="selectSpuSaleAttrListCheckBySku" resultMap="selectSpuSaleAttrListCheckBySkuMap">
        SELECT
            sa.id as sa_id , sav.id as sav_id , sa.*,sav.*, if(ssav.sku_id,1,0) as isChecked
        FROM
            pms_product_sale_attr sa
        INNER JOIN pms_product_sale_attr_value sav ON sa.product_id = sav.product_id
        AND sa.sale_attr_id = sav.sale_attr_id
        AND sa.product_id = #{productId}
        LEFT JOIN pms_sku_sale_attr_value ssav ON sav.id = ssav.sale_attr_value_id
        AND ssav.sku_id = #{skuId}

    </select>

    <!--    autoMapping="true"     设置完主键id后其他的字段自动映射匹配-->
    <resultMap id="selectSpuSaleAttrListCheckBySkuMap" type="com.atguigu.gmall.bean.PmsProductSaleAttr"
               autoMapping="true">
        <result column="sa_id" property="id"></result>
        <!--        这个是属于嵌套的-->
        <collection property="spuSaleAttrValueList" ofType="com.atguigu.gmall.bean.PmsProductSaleAttrValue"
                    autoMapping="true">
            <result column="sav_id" property="id"></result>
        </collection>
    </resultMap>




关于请求和转发
如果直接return“”  是直接转发的意思   读的操作 允许反复刷新 没有数据写入
加上redirect才是重定向   法昂如购物车是写的操作 改变了数据库中的数据 不能反复写入