结合你现在踩的坑：
SpringBoot 2.1.16 + Redisson + SpringDataRedis + 各种中间件版本打架、循环依赖、类版本缺失、无限递归栈溢出。
我给你一套**企业级、一劳永逸、彻底告别版本混乱**的标准化方案，日常开发直接照这套规范走，再也不会乱。

---

## 一、核心原则：不要手动乱写版本号（90% 冲突根源）
SpringBoot / SpringCloud 项目，**绝对不要零散手写版本**：
- 不要自己随便指定 `redisson、kafka、redis、mybatis` 版本
- 不要网上复制一段依赖就直接粘贴

### 正确做法：利用「版本统一管控」
1. SpringBoot 父工程 自带**依赖版本仲裁**
```xml
<parent>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-parent</artifactId>
    <version>2.1.16.RELEASE</version>
    <relativePath/>
</parent>
```
只要继承这个父工程：
- spring-data-redis
- spring-kafka
- spring-web
- jackson、logging 等
  **全部自动统一版本，无需手动写 version**

2. 只有「第三方非标组件」才需要手动匹配版本
   如：Redisson、Caffeine、Hutool、MyBatis-Plus、RocketMQ 这类
   👉 必须严格查表：**组件官方文档[https://mvnrepository.com/] → 对应 SpringBoot 大版本**

---

## 二、两套最高效落地方案（二选一，企业通用）

### 方案1：直接用 SpringBoot 仲裁（小型项目/单体，你当前项目首选）
#### 规则
1. 所有 `spring-boot-starter-xxx` **一律不写 version**
```xml
<!-- 正确 -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-redis</artifactId>
</dependency>

<!-- 错误，画蛇添足极易冲突 -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-redis</artifactId>
    <version>自定义乱填</version>
</dependency>
```

2. 外部中间件，只查「官方适配表」
   以你为例：
- SpringBoot **2.1.x**
- Redisson 固定适配：`3.13.x`
- Caffeine 适配 JDK8：`2.9.1`
- Hutool 选稳定版：`5.8.x`

> 以后遇到任何组件：
> 百度/官网搜：`组件名 + SpringBoot 2.1.x 适配版本`

---

### 方案2：统一版本锁定（中大型/多模块，彻底根治混乱）
在 pom 里统一 `<properties>` + `<dependencyManagement>`
统一约束所有组件版本，全局唯一，杜绝依赖漂移。

示例（适配你 SpringBoot 2.1.16）：
```xml
<properties>
    <redisson.version>3.13.6</redisson.version>
    <caffeine.version>2.9.1</caffeine.version>
    <hutool.version>5.8.25</hutool.version>
</properties>

<!-- 版本锁定，全局统一 -->
<dependencyManagement>
    <dependencies>
        <dependency>
            <groupId>org.redisson</groupId>
            <artifactId>redisson-spring-boot-starter</artifactId>
            <version>${redisson.version}</version>
        </dependency>
    </dependencies>
</dependencyManagement>
```
后续引用依赖**不用写版本**，自动受控。

---

## 三、3个神器：快速排查依赖冲突（高效极简）
### 1. Maven 依赖树（命令行万能）
```bash
# 完整依赖树
mvn dependency:tree

# 只查Redis相关，快速定位冲突
mvn dependency:tree -Dincludes=org.springframework.data:*

# 排查依赖冲突、jar包版本覆盖
mvn dependency:tree -Dverbose
```
作用：
看底层最终生效的 jar 版本，不是你写什么就是什么，Maven 有「就近原则/仲裁覆盖」。

### 2. IDEA Maven Helper 插件（强烈必装）
一键功能：
- 红色标红 = **版本冲突**
- 一键 exclude 排除冲突包
- 直观查看间接依赖

### 3. 官方版本对照表（永久收藏）
所有中间件官方都有：
- Redisson 官方：SpringBoot 版本对照表
- MyBatis-Plus 官网：适配 SpringBoot 版本
- SpringCloud  Alibaba 官网：版本矩阵

**开发前先查表，再引依赖，从源头避免问题**

---

## 四、针对你本次栈溢出问题的复盘（记住这条铁律）
你报错根源：
> SpringBoot 2.1.16（绑定 spring-data-redis 2.1.19）
> 强行使用了适配 2.7/3.x 的高版本 Redisson
> 底层类、接口不匹配 → 无限递归 → StackOverflow

### 黄金匹配口诀（直接背）
1. SpringBoot 2.1.x 👉 Redisson `3.13.x`
2. SpringBoot 2.2~2.6.x 👉 Redisson `3.15.x`
3. SpringBoot 2.7.x 👉 Redisson `3.17.x`
4. SpringBoot 3.x 👉 Redisson `3.20.x+`

---

## 五、给你一套「以后永远不乱」的开发流程
1. 定好 SpringBoot 版本，不再随意升级
2. 所有 starter 不手写版本
3. 三方组件：先查适配表 → 再引入
4. 出现报错，先执行 `mvn dependency:tree` 看实际依赖版本
5. 冲突优先 `exclusion` 排除多余间接依赖

---
