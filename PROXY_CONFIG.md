# Gradle 代理和镜像配置说明

## 配置位置

配置文件位于 `gradle.properties` 和 `settings.gradle.kts`

## 使用方法

### 1. 使用国内镜像（推荐）

项目已配置阿里云镜像，无需额外配置即可使用。镜像会自动加速依赖下载。

### 2. 使用代理（如果需要）

如果需要使用代理，请编辑 `gradle.properties` 文件，取消以下配置的注释并设置您的代理地址：

```properties
# 代理配置
systemProp.http.proxyHost=127.0.0.1
systemProp.http.proxyPort=7890
systemProp.https.proxyHost=127.0.0.1
systemProp.https.proxyPort=7890
```

将 `127.0.0.1` 替换为您的代理服务器地址，将 `7890` 替换为您的代理端口。

### 3. 常见代理端口

- Clash: 7890
- V2Ray: 10808
- Shadowsocks: 1080
- 其他工具请查看您的代理软件设置

## 配置说明

## 验证配置

运行以下命令验证配置是否生效：

```bash
./gradlew dependencies
```

如果依赖能够正常下载，说明配置成功。

## 故障排除

### 问题1: 依赖下载失败

**解决方案**:
1. 检查网络连接
2. 尝试使用代理配置
3. 清理 Gradle 缓存: `./gradlew clean`

### 问题2: 代理连接失败

**解决方案**:
1. 确认代理软件正在运行
2. 检查代理地址和端口是否正确
3. 尝试关闭代理，使用镜像配置

### 问题3: 镜像不可用

**解决方案**:
1. 等待一段时间后重试
2. 使用代理配置
3. 手动下载依赖到本地 Maven 仓库

## 注意事项

1. **不要同时使用代理和镜像**: 如果配置了代理，Gradle 会优先使用代理
2. **IDE 设置**: 如果在 Android Studio 中使用，IDE 的代理设置会覆盖 `gradle.properties` 中的配置
3. **安全**: 不要在代码中硬编码代理信息，使用环境变量或配置文件

## 环境变量配置（可选）

也可以通过环境变量设置代理：

```bash
# Windows PowerShell
$env:HTTP_PROXY="http://127.0.0.1:7890"
$env:HTTPS_PROXY="http://127.0.0.1:7890"

# Windows CMD
set HTTP_PROXY=http://127.0.0.1:7890
set HTTPS_PROXY=http://127.0.0.1:7890

# Linux/Mac
export HTTP_PROXY=http://127.0.0.1:7890
export HTTPS_PROXY=http://127.0.0.1:7890
```

## 其他镜像源

如果阿里云镜像不可用，可以尝试其他镜像：

### 腾讯云镜像
```kotlin
maven { url = uri("https://mirrors.cloud.tencent.com/nexus/repository/maven-public/") }
```

### 华为云镜像
```kotlin
maven { url = uri("https://repo.huaweicloud.com/repository/maven/") }
```

### 清华大学镜像
```kotlin
maven { url = uri("https://mirrors.tuna.tsinghua.edu.cn/maven/") }
```
