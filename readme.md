# 基础组件包

![maven central version](https://img.shields.io/maven-central/v/com.labijie.infra/commons-core?style=flat-square)
![workflow status](https://img.shields.io/github/actions/workflow/status/hongque-pro/infra-commons/build.yml?branch=main)
![license](https://img.shields.io/github/license/hongque-pro/infra-commons?style=flat-square)
![Static Badge](https://img.shields.io/badge/GraalVM-supported-green?style=flat&logoColor=blue&labelColor=orange)

该包没有太多的功能，主要用于固定依赖版本：

- Rfc6238

> 该包没有太多的功能，主要用于固定依赖版本

## 使用举例
```groovy
    compile "com.labijie.infra:commons-springboot-starter:$infra_commons_version"
```


## 开发环境兼容性：

Version: 3.1.x 

| 组件          | 版本          | 说明              |
|-------------|-------------|-----------------|
| kotlin      | 2.2.0       |                 |
| jdk         | 21          |                 |
| spring boot | 3.5.3       |                 |

---

infra_version 3.0.x

| 组件           | 版本     | 说明       |
|--------------|--------|----------|
| kotlin       | 1.9.21 |          |
| jdk          | 17     |          |
| spring boot  | 3.2.0  |          |

---

2.2 开始，可以使用 infra-bom 控制所有依赖项版本.

关于 infra-bom，请参考 https://github.com/hongque-pro/infra-bom

## 发布到自己的 Nexus

在项目根目录下新建 gradle.properties 文件，添加如下内容

```text
PUB_USER=[nexus user name]
PUB_PWD=[nexus password]
PUB_URL=http://XXXXXXX/repository/maven-releases/
```
运行  **gradle publish**
