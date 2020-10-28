# 基础组件包

该包没有太多的功能，主要用于固定依赖版本：

## 使用举例
```groovy
    compile "com.labijie.infra:commons-snowflaker-starter:$infra_commons_version"
```

## 服务器环境兼容性：
zookeeper: 3.5.x - 3.6.0 （不兼容 3.4.x）
kafka: 2.6.x (为了和 spring-kafka 保持一致)

> spring kafka 兼容性看这里： https://spring.io/projects/spring-kafka

## 开发环境兼容性：

|组件|版本|说明|
|--------|--------|--------|
|   kotlin    |      1.4.10    |           |
|   jdk    |      1.8   |           |
|   spring boot    |      2.3.4.RELEASE    |           |
|  spring cloud    |      Hoxton.SR8    |   通过 BOM 控制版本，因为 cloud 组件版本混乱，无法统一指定  |
|   spring framework    |      5.2.9.RELEASE   |           |
|   spring dpendency management    |      1.0.10.RELEASE    |           |

一切开始的地方，所有组件的依赖项.

## 发布到自己的 Nexus

在项目根目录下新建 gradle.properties 文件，添加如下内容

```text
PUB_USER=[nexus user name]
PUB_PWD=[nexus password]
PUB_URL=http://XXXXXXX/repository/maven-releases/
```
运行  **gradle publish**
