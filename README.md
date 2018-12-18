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
