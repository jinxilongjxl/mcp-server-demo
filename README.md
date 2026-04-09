# MCP Server Demo

基于 Spring Boot 和 Spring AI 构建的 MCP（Model Context Protocol）服务器示例，提供天气查询相关工具。

## 技术栈

- Java 21
- Spring Boot 3.5.13
- Spring AI 1.0.0（MCP Server Starter）
- NWS Weather API（https://api.weather.gov）

## 提供的工具（Tools）

| 工具名称 | 描述 | 参数 |
|---------|------|------|
| `getWeatherForecast` | 根据经纬度获取天气预报 | `latitude` (double), `longitude` (double) |
| `getAlerts` | 获取美国指定州的天气预警 | `state` (string, 两位州代码，如 CA、NY) |

## 构建

```bash
mvn clean package
```

## 启动方式

### 1. STDIO 方式（推荐用于 MCP 客户端集成）

STDIO 模式下，MCP 服务器通过标准输入/输出与客户端通信，适用于 Cline、Claude Desktop 等 MCP 客户端直接调用。

```bash
java -Dspring.ai.mcp.server.stdio=true -jar target/mcp-server-demo-0.0.1-SNAPSHOT.jar
```

#### 在 Cline 中配置 STDIO 方式

在 `cline_mcp_settings.json` 中添加：

```json
{
  "mcpServers": {
    "mcp-server-demo": {
      "autoApprove": [],
      "disabled": false,
      "timeout": 60,
      "type": "stdio",
      "command": "java",
      "args": [
        "-Dspring.ai.mcp.server.stdio=true",
        "-jar",
        "/path/to/mcp-server-demo-0.0.1-SNAPSHOT.jar"
      ]
    }
  }
}
```

> 将 `/path/to/` 替换为 JAR 文件的实际路径。

#### 在 Claude Desktop 中配置 STDIO 方式

在 `claude_desktop_config.json` 中添加：

```json
{
  "mcpServers": {
    "mcp-server-demo": {
      "command": "java",
      "args": [
        "-Dspring.ai.mcp.server.stdio=true",
        "-jar",
        "/path/to/mcp-server-demo-0.0.1-SNAPSHOT.jar"
      ]
    }
  }
}
```

### 2. HTTP 方式（Streamable HTTP）

HTTP 模式下，MCP 服务器作为 Web 服务运行，客户端通过 HTTP 连接。

```bash
java -jar target/mcp-server-demo-0.0.1-SNAPSHOT.jar
```

默认监听端口为 `8080`，MCP 端点为 `http://localhost:8080/mcp`。

#### 在 Cline 中配置 HTTP 方式

```json
{
  "mcpServers": {
    "mcp-server-demo": {
      "autoApprove": [],
      "disabled": false,
      "timeout": 60,
      "type": "streamableHttp",
      "url": "http://localhost:8080/mcp"
    }
  }
}
```

## 使用 MCP Inspector 调试

```bash
npx @modelcontextprotocol/inspector
```

在 Inspector 中配置 STDIO 命令或连接 HTTP 端点即可调试工具调用。

## 注意事项

- 天气数据来自美国国家气象局（NWS）API，仅支持美国地区的经纬度和州代码。
- STDIO 模式运行时，确保在执行 `mvn clean` 前先停止所有运行中的 JAR 进程，否则会因文件锁定导致清理失败。