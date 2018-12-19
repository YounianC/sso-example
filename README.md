# sso-example
单点登录demo

# 模块
* a1 ：测试项目Application1
* ssoclient：sso客户端，用于宿主项目
* ssoserver：sso服务端，即真正登录项目

# 部署：
* a1 启动于tomcat 8081端口
* a1 启动于tomcat 8082端口，模拟其他项目
* ssoserver启动于tomcat 8080端口

# hosts
添加hosts：
```
127.0.0.1 server.com
127.0.0.1 a1.com
127.0.0.1 a2.com
```

重要，访问一定要使用hosts，否则多个项目都在localhost下会出现sessionid混乱问题。


# 最终效果

* 首次访问 a1.com:8081/index 会自动跳转到  http://server.com:8080/login?callback=http://a1.com:8081/index

* 在登录页面输入用户名密码即可登录，成功会自动调回 a1.com:8081/index 并附带token

* 再次访问 a1.com:8081/index 无需登录

* 访问 a2.com:8082/index 会自动跳转并且调回，无需操作即可登录。

* 访问a1注销链接：a1.com:8081/logout ，会相应注销其他子系统。