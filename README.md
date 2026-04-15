[AI零代码生成平台.md](https://github.com/user-attachments/files/26753738/AI.md)

# 用户登陆流程

1）**建立初始会话⁠⁠⁠**：前端与服务器建立连接后，***服务器会为该客户端创建一个初始的匿名 ﻿﻿﻿Session，并将其状态保⁢⁢⁢存下来***。这个 Session 的 I‍‍‍D 会作为唯一标识，返回给前端。

2）**登录成功，更新会话信息**：当⁠⁠⁠用户在前端输入正确的账号密码并提交到后端验证成功后，后端会 ***更新该用户的 Session***，将用户的登录信息（如用户 I﻿﻿﻿D、用户名等）保存到与该 Session 关联的存储中。同⁢⁢⁢时，***服务器会生成一个 Set-Cookie 的响应头，指示‍‍‍前端保存该用户的 Session ID***。

3）**前端保存 `C⁠⁠⁠ookie`**：前端接收到后端的响应后，浏览器会自动根据 `Set-C﻿﻿﻿ookie` 指令，将 `Sessi⁢⁢⁢on ID` 存储到浏览器的 Co‍‍‍okie 中，与该域名绑定。

4）**带 `Coo⁠⁠⁠kie` 的后续请求**：当前端再次向相同域名的服务器发送请求﻿﻿﻿时，***浏览器会自动在请求头中附⁢⁢⁢带之前保存的 `Cookie`***，‍‍‍其中包含 `Session ID`。

5）**后端验⁠⁠⁠证会话**：服务器接收到请求后，***从请求头中提﻿﻿﻿取 Session ⁢⁢⁢ID，找到对应的 `S‍‍‍ession` 数据***。

6）**获取会话⁠⁠⁠中存储的信息**：后端通过该 Session 获取之﻿﻿﻿前存储的用户信息（如登录⁢⁢⁢名、权限等），从而识别用‍‍‍户身份并执行相应的业务逻辑。

![img](./AI零代码生成平台.assets/ahLctL73Pp9LatVp-1774594401364-15.svg)

------









# MyBatis Flex

**优势：**

- 更灵活：MyBatis-Flex 提供了非常灵活的 QueryWrapper，支持**关联查询、多表查询、多主键、逻辑删除、乐观锁更新、数据填充、数据脱敏**等等。
- 更高的性能：MyBatis-Flex 通过独特的架构，没有任何 MyBatis 拦截器、**在 SQL 执行的过程中，没有任何的 SQL Parse，因此会带来指数级的性能增长**。

此外，在 Mybatis Flex 中，有了一个**名称为 `mybatis-flex-codegen` 的模块**，提供了可以通过数据库表，生成代码的功能。当我们把数据库表设计完成后， 就可以使用其**快速生成 Entity、Mapper、Service、Controller 代码**，能大幅提高我们的开发效率。

------



- **引入依赖：**

```xml
<dependency>
    <groupId>com.mybatis-flex</groupId>
    <artifactId>mybatis-flex-spring-boot3-starter</artifactId>
    <version>1.11.0</version>
</dependency>

<!-- 代码生成模块 -->
<dependency>
    <groupId>com.mybatis-flex</groupId>
    <artifactId>mybatis-flex-codegen</artifactId>
    <version>1.11.0</version>
</dependency>
<!-- 数据库连接池[HikariCP] -->
<dependency>
    <groupId>com.zaxxer</groupId>
    <artifactId>HikariCP</artifactId>
</dependency>
```

------



- **数据库连接配置**

```yml
# mysql
datasource:
  driver-class-name: com.mysql.cj.jdbc.Driver
  url: jdbc:mysql://192.168.44.128:3306/AI_code_creater
  username: root
  password: 123
```

------



- **开发代码生成器**

在 Myb⁠⁠⁠atis Flex 的代码生成器中，支持﻿﻿﻿如下 8 种类型的的⁢⁢⁢产物生成，我们只需要‍‍‍关注前 6 个就好：

- Entity 实体类 ✅
- Mapper 映射类 ✅
- Service 服务类 ✅
- ServiceImpl 服务实现类 ✅
- Controller 控制类 ✅
- MapperXml 文件 ✅
- TableDef 表定义辅助类
- package-info.java 文件

```Java
public class MyBatisCodeGenerator {

    // 需要生成的表名
    private static final String[] TABLE_NAMES = {"user"};

    public static void main(String[] args) {
        // 获取配置文件中的数据源信息
        Dict dict = YamlUtil.loadByPath("application.yml");
        Map<String, Object> dataSourceConfig = dict.getByPath("spring.datasource");
        String url = String.valueOf(dataSourceConfig.get("url"));
        String username = String.valueOf(dataSourceConfig.get("username"));
        String password = String.valueOf(dataSourceConfig.get("password"));

        // 配置数据源
        HikariDataSource dataSource = new HikariDataSource();
        dataSource.setJdbcUrl(url);
        dataSource.setUsername(username);
        dataSource.setPassword(password);

        // 创建配置内容
        GlobalConfig globalConfig = createGlobalConfig();

        // 通过 datasource 和 globalConfig 创建代码生成器
        Generator generator = new Generator(dataSource, globalConfig);

        // 生成代码
        generator.generate();
    }


    /**
     * 设置需要生成的内容
     * @return
     */
    private static GlobalConfig createGlobalConfig() {
        // 创建配置内容
        GlobalConfig globalConfig = new GlobalConfig();

        // 设置根包，建议先生成到一个临时目录下【这里设置临时目录为 genresult】，生成代码后，再移动到项目目录下
        globalConfig.getPackageConfig()
                .setBasePackage("com.ryy.aicodecreater.genresult");

        // 设置表前缀和只生成哪些表，setGenerateTable 未配置时，生成所有表
        globalConfig.getStrategyConfig()
                // 指定要生成的表名
                .setGenerateTable(TABLE_NAMES)
                // 设置逻辑删除的默认字段名称
                .setLogicDeleteColumn("isDelete");

        // 设置生成 entity 并启用 Lombok
        globalConfig.enableEntity()
                .setWithLombok(true)
                .setJdkVersion(21);

        // 设置生成 mapper
        globalConfig.enableMapper();
        globalConfig.enableMapperXml();

        // 设置生成 service
        globalConfig.enableService();
        globalConfig.enableServiceImpl();

        // 设置生成 controller
        globalConfig.enableController();

        // 设置生成时间和字符串为空，避免多余的代码改动
        globalConfig.getJavadocConfig()
            	// 设置作者
                .setAuthor("<a href=\"https://github.com/R-icher\">Richer</a>")
                .setSince("");
        return globalConfig;
    }
}
```

------









# 用户权限校验

一般会通过 **Spring AOP 切面 + 自定义权限校验注解** 实现统一的接口拦截和权限校验

1. **需要权限校验注解**

```Java
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface AuthCheck {

    /**
     * 必须有某个角色
     */
    String mustRole() default "";
}
```

------



2. **权限校验切面**

对于加了此注解的方法，需要获取当前请求的上下文，并拿到 `HttpServletRequest` 对象，从而解析到登录用户的角色

```Java
/**
 * 权限拦截器
 *
 * 基于 AOP + 自定义注解 @AuthCheck 实现统一的权限校验。
 * 在执行目标方法前，先判断当前登录用户是否具备所需角色。
 */
@Aspect
@Component
public class AuthInterceptor {

    /**
     * 用户服务，用于获取当前登录用户信息
     */
    @Resource
    private UserService userService;

    /**
     * 环绕通知：拦截所有带有 @AuthCheck 注解的方法，并进行权限校验
     *
     * @param joinPoint 切入点，表示当前被拦截的方法
     * @param authCheck 权限校验注解，可获取注解中声明的必须角色
     * @return 目标方法执行结果
     */
    @Around("@annotation(authCheck)")
    public Object doInterceptor(ProceedingJoinPoint joinPoint, AuthCheck authCheck) throws Throwable {

        // 获取注解中定义的必须具备的角色
        String mustRole = authCheck.mustRole();

        // 获取当前请求上下文
        RequestAttributes requestAttributes = RequestContextHolder.currentRequestAttributes();

        // 从请求上下文中取出 HttpServletRequest 对象
        HttpServletRequest request = ((ServletRequestAttributes) requestAttributes).getRequest();

        // 获取当前登录用户
        User loginUser = userService.getLoginUser(request);

        // 将注解中的角色字符串转换为对应的枚举值
        UserRoleEnum mustRoleEnum = UserRoleEnum.getEnumByValue(mustRole);

        // 如果注解未指定角色，或指定角色无效，则默认不需要权限，直接放行
        if (mustRoleEnum == null) {
            return joinPoint.proceed();
        }

        // =========================
        // 以下逻辑表示：必须具备指定权限才允许访问
        // =========================

        // 获取当前登录用户的角色枚举
        UserRoleEnum userRoleEnum = UserRoleEnum.getEnumByValue(loginUser.getUserRole());

        // 如果当前用户角色为空或无效，说明没有权限，抛出无权限异常
        if (userRoleEnum == null) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }

        // 如果接口要求管理员权限，但当前用户不是管理员，则拒绝访问
        if (UserRoleEnum.ADMIN.equals(mustRoleEnum) && !UserRoleEnum.ADMIN.equals(userRoleEnum)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }

        // 权限校验通过，继续执行目标方法
        return joinPoint.proceed();
    }
}
```

------



3. **使用方法**

只要给方法添加了 `@AuthCheck` 注解，就必须要登录，否则会抛出异常。

- **可以设置 ⁠⁠⁠`mustRole` 为管理员，﻿这样﻿仅管﻿理员才⁢能使用⁢该接口⁢：**

```Java
@AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
```

------









# 方案设计

**用户输入描述 → AI 大模型生成 → 提取生成内容 → 写入本地文件**

这个看似简单的流程，实际上涉及不少技术细节。比如：

- 如何实现和 AI 的对话？
- 如何设计有效的提示词？
- 如何确保 AI 输出的格式符合我们的要求？
- 如何处理生成的代码并保存到合适的位置？

------







# LangChain4j

1. 引入依赖

```xml
		<!-- LangChain4j 核心依赖 -->
		<dependency>
			<groupId>dev.langchain4j</groupId>
			<artifactId>langchain4j</artifactId>
			<version>1.1.0</version>
		</dependency>
		<dependency>
			<groupId>dev.langchain4j</groupId>
			<artifactId>langchain4j-open-ai-spring-boot-starter</artifactId>
			<version>1.1.0-beta7</version>
		</dependency>
```

------



2. **LangChain4j 和 Deepseek 整合**

```yml
# AI
langchain4j:
  open-ai:
    chat-model:
      base-url: https://api.deepseek.com
      api-key: sk-30e032140a494a4c9fbbf4ddc33e3687
      model-name: deepseek-chat
      log-requests: true
      log-responses: true
```

------



3. **开发 AI 服务**

- 系统提示词过长，需要**通过 `fromResource` 指定 prompt 系统提示词的所在目录**

```Java
public interface AiCodeGeneratorService {

    /**
     * 生成 HTML 代码
     *
     * @param userMessage 用户需求
     * @return 生成结果
     */
    @SystemMessage(fromResource = "prompt/codegen-html-system-prompt.txt")
    String generateHtmlCode(String userMessage);

    /**
     * 生成多文件代码
     *
     * @param userMessage 用户需求
     * @return 生成结果
     */
    @SystemMessage(fromResource = "prompt/codegen-multi-file-system-prompt.txt")
    String generateMultiFileCode(String userMessage);
}
```

------



4. **建工厂类来实例化刚才创建的 AI 服务**

```Java
@Configuration
public class AiCodeGeneratorServiceFactory {

    @Resource
    private ChatModel chatModel;

    @Bean
    public AiCodeGeneratorService aiCodeGeneratorService() {
        return AiServices.create(AiCodeGeneratorService.class, chatModel);
    }
}
```

------



- **测试类：**

```Java
@SpringBootTest
class AiCodeGeneratorServiceTest {

    @Resource
    private AiCodeGeneratorService aiCodeGeneratorService;

    @Test
    void generateHtmlCode() {
        String result = aiCodeGeneratorService.generateHtmlCode("做个程序员工作记录小工具");
        Assertions.assertNotNull(result);
    }

    @Test
    void generateMultiFileCode() {
        String result = aiCodeGeneratorService.generateMultiFileCode("做个程序员留言板");
        Assertions.assertNotNull(result);
    }
}
```

------



- **请求格式：**

```json
- method: POST
- url: https://api.deepseek.com/chat/completions
- headers: [Authorization: Beare...87], [User-Agent: langchain4j-openai], [Content-Type: application/json]
- body: {
  "model" : "deepseek-chat",
  "messages" : [ {
      
    // 从指定路径中读到的设定的系统 prompt
    "role" : "system",
    "content" : "你是一位资深的 Web 前端开发专家，... ..."
  }, {
    
    // 测试类中用户的输入
    "role" : "user",
    "content" : "做个程序员工作记录小工具"
  } ],
  "stream" : false
}
```

------







## 结构化输出

直接返回字符串的方式不便于后续解析代码并保存为文件。因此我们**需要将 AI 的输出转换为结构化的对象**

- **对于刚才的两个方法：【创建对应的返回参数】**

```Java
@Description("生成 HTML 代码文件的结果")
@Data
public class HtmlCodeResult {

    @Description("HTML 代码内容")
    private String htmlCode;

    @Description("对生成代码的说明")
    private String description;
}
```

------

```Java
@Description("生成多个前端代码文件的结果")
@Data
public class MultiFileCodeResult {

    @Description("HTML 代码内容")
    private String htmlCode;

    @Description("CSS 代码内容")
    private String cssCode;

    @Description("JavaScript 代码内容")
    private String jsCode;

    @Description("对生成代码的说明")
    private String description;
}
```

------



- **然后在原本的 AI 服务化中将原本的 String 类型转换为 `HtmlCodeResult` 和 `MultiFileCodeResult` 类型**

执行单⁠⁠⁠元测试，通过日志可以看到，**`LangC﻿﻿﻿hain4j` 自动⁢⁢⁢在我们的提示词【prompt】后面‍‍‍拼接了结构化输出的要求**：

<img src="./AI零代码生成平台.assets/K5sfHEHlYsl9s8oI.webp" alt="img" style="zoom: 33%;" />

------







## 提⁢高结构⁢化输出⁢的‍准确度‍和稳定性

1. 设置 max_tokens【**即设置输出长度，防止 AI 生成的 JSON 被半路截断**】

```yml
langchain4j:
  open-ai:
    chat-model:
      max-tokens: 8192
```

------



2. JSON Schema 配置【**强制以 `Json` 格式返回**】

***`deepseek` 支持的格式***：设置 `response-format` 参数为 `json_object`。

```yml
langchain4j:
  open-ai:
    chat-model:
      strict-json-schema: true
      response-format: json_object
```

------



3. 添加字段描述【**@Description 注解，让 AI 更好的理解要输出字段的内容**】

```Java
@Description("生成 HTML 代码文件的结果")
@Data
public class HtmlCodeResult {

    @Description("HTML代码")
    private String htmlCode;

    @Description("生成代码的描述")
    private String description;
}
```

------







## 保存到本地

- ***有了结构化⁠⁠⁠的输出对象，接下来就是将生成﻿的代﻿码保﻿存到本⁢地文⁢件系统。***

1. 创建一个枚举类，**统一区分不同的生成模式**

   - `HTML`：生成单个 `index.html`

   - `MULTI_FILE`：生成 `html + css + js` 多文件

```java
/**
 * 代码生成类型枚举
 *
 * 作用：
 * 1. 统一定义系统支持的代码生成模式
 * 2. 避免在业务代码中直接写 "html"、"multi_file" 这样的硬编码字符串
 * 3. 提供根据 value 反查枚举的方法，方便接收前端参数或请求参数后进行业务判断
 */
@Getter
public enum CodeGenTypeEnum {

    /**
     * 原生 HTML 模式
     * 一般表示只生成一个 HTML 文件，例如 index.html
     */
    HTML("原生 HTML 模式", "html"),

    /**
     * 原生多文件模式
     * 一般表示生成多个文件，例如 index.html、style.css、script.js
     */
    MULTI_FILE("原生多文件模式", "multi_file");

    /**
     * 给用户展示的文本说明
     * 例如：原生 HTML 模式、原生多文件模式
     */
    private final String text;

    /**
     * 枚举对应的业务值
     * 一般用于前后端传值、数据库存储、业务判断
     * 例如：html、multi_file
     */
    private final String value;

    /**
     * 枚举构造方法
     *
     * @param text  展示文本
     * @param value 业务值
     */
    CodeGenTypeEnum(String text, String value) {
        this.text = text;
        this.value = value;
    }

    /**
     * 根据 value 获取对应的枚举对象
     *
     * 使用场景：
     * 当前端传来字符串 "html" 或 "multi_file" 时，
     * 可以调用该方法转成对应的枚举值，便于后续统一处理。
     *
     * @param value 枚举的业务值
     * @return 匹配到的枚举对象；如果 value 为空或未匹配到，则返回 null
     */
    public static CodeGenTypeEnum getEnumByValue(String value) {
        // 如果传入值为空，直接返回 null
        if (ObjUtil.isEmpty(value)) {
            return null;
        }

        // 遍历所有枚举值，查找 value 相同的枚举对象
        for (CodeGenTypeEnum anEnum : CodeGenTypeEnum.values()) {
            if (anEnum.value.equals(value)) {
                return anEnum;
            }
        }

        // 没有匹配到时返回 null
        return null;
    }
}
```

------



2. 将生成的文件落入到本地的工具类

```Java
/**
 * 代码文件保存工具类
 *
 * 作用：
 * 1. 将 AI 生成的代码结果保存到本地文件系统
 * 2. 根据不同的生成模式，自动创建对应的文件结构
 * 3. 为每次生成创建唯一目录，避免文件覆盖
 *
 * 例如：
 * - HTML 模式：只保存一个 index.html
 * - 多文件模式：保存 index.html、style.css、script.js
 */
public class CodeFileSaver {

    /**
     * 文件保存的根目录
     *
     * System.getProperty("user.dir") 表示当前项目运行目录，
     * 最终保存路径类似：
     * 项目目录/tmp/code_output
     */
    private static final String FILE_SAVE_ROOT_DIR =
            System.getProperty("user.dir") + "/tmp/code_output";

    /**
     * 保存 HTML 模式的生成结果
     *
     * 适用场景：
     * AI 只生成单个 HTML 文件时使用。
     *
     * 执行流程：
     * 1. 创建唯一目录
     * 2. 将 result 中的 htmlCode 写入 index.html
     * 3. 返回保存后的目录
     *
     * @param result HTML 模式的代码生成结果
     * @return 保存文件的目录对象
     */
    public static File saveHtmlCodeResult(HtmlCodeResult result) {
        // 根据业务类型 html 创建唯一目录
        String baseDirPath = buildUniqueDir(CodeGenTypeEnum.HTML.getValue());

        // 将 HTML 内容写入 index.html 文件
        writeToFile(baseDirPath, "index.html", result.getHtmlCode());

        // 返回保存后的目录
        return new File(baseDirPath);
    }

    /**
     * 保存多文件模式的生成结果
     *
     * 适用场景：
     * AI 同时生成 HTML、CSS、JS 三部分代码时使用。
     *
     * 执行流程：
     * 1. 创建唯一目录
     * 2. 分别写入 index.html、style.css、script.js
     * 3. 返回保存后的目录
     *
     * @param result 多文件模式的代码生成结果
     * @return 保存文件的目录对象
     */
    public static File saveMultiFileCodeResult(MultiFileCodeResult result) {
        // 根据业务类型 multi_file 创建唯一目录
        String baseDirPath = buildUniqueDir(CodeGenTypeEnum.MULTI_FILE.getValue());

        // 分别写入三个文件
        writeToFile(baseDirPath, "index.html", result.getHtmlCode());
        writeToFile(baseDirPath, "style.css", result.getCssCode());
        writeToFile(baseDirPath, "script.js", result.getJsCode());

        // 返回保存后的目录
        return new File(baseDirPath);
    }

    /**
     * 构建唯一目录路径
     *
     * 目录格式：
     * tmp/code_output/业务类型_雪花ID
     *
     * 例如：
     * tmp/code_output/html_192837465
     * tmp/code_output/multi_file_192837466
     *
     * 这样做的目的：
     * 1. 区分不同生成类型
     * 2. 保证每次生成的目录唯一，防止覆盖历史文件
     *
     * @param bizType 业务类型，例如 html / multi_file
     * @return 创建好的唯一目录路径
     */
    private static String buildUniqueDir(String bizType) {
        // 使用 Hutool 的字符串格式化和雪花算法生成唯一目录名
        String uniqueDirName = StrUtil.format("{}_{}", bizType, IdUtil.getSnowflakeNextIdStr());

        // 拼接出完整目录路径
        String dirPath = FILE_SAVE_ROOT_DIR + File.separator + uniqueDirName;

        // 创建目录，如果目录不存在会自动创建
        FileUtil.mkdir(dirPath);

        return dirPath;
    }

    /**
     * 写入单个文件
     *
     * 例如：
     * dirPath = /tmp/code_output/html_123456
     * filename = index.html
     *
     * 最终写入路径：
     * /tmp/code_output/html_123456/index.html
     *
     * @param dirPath 目录路径
     * @param filename 文件名
     * @param content 文件内容
     */
    private static void writeToFile(String dirPath, String filename, String content) {
        // 拼接完整文件路径
        String filePath = dirPath + File.separator + filename;

        // 按 UTF-8 编码将字符串内容写入文件
        FileUtil.writeString(content, filePath, StandardCharsets.UTF_8);
    }
}
```

------







## 门面模式

**给复杂的子系统提供一个统一的对外入口。**

调用方 ***不需要关心内部有多少个类、多少步骤，只需要调用这个“门面”就行***。

比如你的项目里：

- AI 生成代码
- 创建目录
- 写入文件

这些步骤本来要分别调用多个类。
 如果用门面模式，就可以统一成一个方法，比如：

```Java
generateAndSaveCode(...)
```

外部只调这一个方法，不用管内部细节。

------

![img](./AI零代码生成平台.assets/ORVEUdvaktYXhFT6.webp)

------



- ***代码：***

```Java
/**
 * AI 代码生成门面类
 *
 * 作用：
 * 1. 作为“门面模式”的统一入口，对外屏蔽内部复杂流程
 * 2. 负责协调 AI 代码生成服务 和 文件保存工具类
 * 3. 调用方只需要传入用户需求和生成类型，即可完成“生成代码 + 保存文件”的完整流程
 */
@Service
public class AiCodeGeneratorFacade {

    /**
     * AI 代码生成服务
     * 负责调用大模型，根据用户提示词生成对应的代码结果。
     */
    @Resource
    private AiCodeGeneratorService aiCodeGeneratorService;

    /**
     * 统一入口：根据生成类型生成代码并保存到本地
     *
     * 处理流程：
     * 1. 校验生成类型是否为空
     * 2. 根据不同生成类型，调用不同的代码生成方法
     * 3. 将生成结果写入本地文件
     * 4. 返回保存后的目录
     *
     * @param userMessage 用户输入的提示词，用于告诉 AI 要生成什么内容
     * @param codeGenTypeEnum 代码生成类型，例如：HTML 模式、多文件模式
     * @return 保存生成代码的目录
     */
    public File generateAndSaveCode(String userMessage, CodeGenTypeEnum codeGenTypeEnum) {
        // 如果生成类型为空，直接抛出业务异常
        if (codeGenTypeEnum == null) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "生成类型为空");
        }

        // 根据生成类型，分发到不同的生成和保存逻辑
        return switch (codeGenTypeEnum) {
            case HTML -> generateAndSaveHtmlCode(userMessage);
            case MULTI_FILE -> generateAndSaveMultiFileCode(userMessage);
            // 其他类型暂不支持
            default -> {
                String errorMessage = "不支持的生成类型：" + codeGenTypeEnum.getValue();
                throw new BusinessException(ErrorCode.SYSTEM_ERROR, errorMessage);
            }
        };
    }

    /**
     * 生成 HTML 模式的代码并保存
     *
     * 处理流程：
     * 1. 调用 AI 服务生成 HTML 结果
     * 2. 将生成结果保存为本地文件
     * 3. 返回保存目录
     *
     * @param userMessage 用户提示词
     * @return 保存生成结果的目录
     */
    private File generateAndSaveHtmlCode(String userMessage) {
        // 调用 AI 生成 HTML 模式代码
        HtmlCodeResult result = aiCodeGeneratorService.generateHtmlCode(userMessage);
        // 调用之前的保存文件到本地的代码 CodeFileSaver，并返回目录对象
        return CodeFileSaver.saveHtmlCodeResult(result);
    }

    /**
     * 生成多文件模式的代码并保存
     *
     * 处理流程：
     * 1. 调用 AI 服务生成多文件代码结果
     * 2. 将 HTML、CSS、JS 分别保存到本地文件
     * 3. 返回保存目录
     *
     * @param userMessage 用户提示词
     * @return 保存生成结果的目录
     */
    private File generateAndSaveMultiFileCode(String userMessage) {
        // 调用 AI 生成多文件模式代码
        MultiFileCodeResult result = aiCodeGeneratorService.generateMultiFileCode(userMessage);
        // 调用之前的保存文件到本地的代码 CodeFileSaver，并返回目录对象
        return CodeFileSaver.saveMultiFileCodeResult(result);
    }
}
```

------









# SSE流式输出

1. 在配置文件中配置流式模型

```yml
langchain4j:
  open-ai:
    streaming-chat-model:
      base-url: https://api.deepseek.com
      api-key: <Your API Key>
      model-name: deepseek-chat
      max-tokens: 8192
      log-requests: true
      log-responses: true
```

------



2. 在创建 AI Service 的工厂类中注入流式模型：

```java
@Configuration
public class AiCodeGeneratorServiceFactory {

    @Resource
    private ChatModel chatModel;

    @Resource
    private StreamingChatModel streamingChatModel;

    @Bean
    public AiCodeGeneratorService aiCodeGeneratorService() {
        return AiServices.builder(AiCodeGeneratorService.class)
                .chatModel(chatModel)
                .streamingChatModel(streamingChatModel)
                .build();
    }
}
```

------



3. 在 A⁠⁠⁠I Service 中新增流式方﻿﻿法，﻿**跟之前方法的⁢⁢区别在⁢于返回值改‍‍为了 F‍lux 对象：**

```java
/**
 * 生成 HTML 代码（流式）
 *
 * @param userMessage 用户消息
 * @return 生成的代码结果
 */
@SystemMessage(fromResource = "prompt/codegen-html-system-prompt.txt")
Flux<String> generateHtmlCodeStream(String userMessage);

/**
 * 生成多文件代码（流式）
 *
 * @param userMessage 用户消息
 * @return 生成的代码结果
 */
@SystemMessage(fromResource = "prompt/codegen-multi-file-system-prompt.txt")
Flux<String> generateMultiFileCodeStream(String userMessage);
```

------



4. 自建代码解析器：【**由于流式输⁠⁠⁠出返回的是字符串片段，无法通过结构化输出获取相应的对象。**】

***通过正则表达式从完整字符串中提取到对应的代码块，并返回结构化输出对象***，这样可以复用之前的文件保存器。

```Java
/**
 * 代码解析器
 * 提供静态工具方法，用于从大模型返回的 Markdown 文本中
 * 提取不同类型的代码块内容，例如：
 * 1. HTML 单文件代码
 * 2. 多文件代码（HTML + CSS + JavaScript）
 * 
 * 适用场景：
 * 当 AI 返回如下格式内容时，可以通过本类自动解析出对应的代码内容：
 * <p>
 * ```html
 * <html>...</html>
 * ```
 * <p>
 * ```css
 * body { ... }
 * ```
 * <p>
 * ```javascript
 * console.log('hello');
 * ```
 *
 * @author yupi
 */
public class CodeParser {

    /**
     * 示例匹配内容：
     * ```html
     * <h1>Hello</h1>
     * ```
     */
    private static final Pattern HTML_CODE_PATTERN = Pattern.compile("```html\\s*\\n([\\s\\S]*?)```", Pattern.CASE_INSENSITIVE);
    private static final Pattern CSS_CODE_PATTERN = Pattern.compile("```css\\s*\\n([\\s\\S]*?)```", Pattern.CASE_INSENSITIVE);
    private static final Pattern JS_CODE_PATTERN = Pattern.compile("```(?:js|javascript)\\s*\\n([\\s\\S]*?)```", Pattern.CASE_INSENSITIVE);

    /**
     * 解析 HTML 单文件代码
     * 
     * 该方法主要用于处理仅返回一个 html 代码块的场景。
     * 如果能够从内容中提取出 html 代码块，则将提取结果设置到返回对象中；
     * 如果没有找到 html 代码块，则默认将整个原始内容作为 html 内容保存。
     * 
     * 适合以下情况：
     * 1. AI 返回的是 ```html ... ``` 代码块
     * 2. AI 直接返回纯 HTML 内容，而没有使用 Markdown 包裹
     *
     * @param codeContent 原始代码内容，通常是 AI 返回的完整文本
     * @return HTML 解析结果对象，包含提取后的 HTML 代码
     */
    public static HtmlCodeResult parseHtmlCode(String codeContent) {
        // 创建解析结果对象
        HtmlCodeResult result = new HtmlCodeResult();

        // 提取 HTML 代码块中的内容
        String htmlCode = extractHtmlCode(codeContent);

        // 如果成功提取到 HTML 代码，则去除首尾空白后设置到结果中
        if (htmlCode != null && !htmlCode.trim().isEmpty()) {
            result.setHtmlCode(htmlCode.trim());
        } else {
            // 如果没有找到 html 代码块，则将整个内容作为 HTML 代码处理
            result.setHtmlCode(codeContent.trim());
        }

        return result;
    }

    /**
     * 解析多文件代码（HTML + CSS + JS）
     * <p>
     * 该方法用于从一段混合内容中分别提取 HTML、CSS、JavaScript 代码块，
     * 并封装到 MultiFileCodeResult 中返回。
     * <p>
     * 例如输入内容可能为：
     * ```html
     * <div>Hello</div>
     * ```
     * <p>
     * ```css
     * div { color: red; }
     * ```
     * <p>
     * ```javascript
     * console.log('hello');
     * ```
     * <p>
     * 如果某一种代码块不存在，则对应字段不会被设置。
     *
     * @param codeContent 原始代码内容，通常是 AI 返回的完整文本
     * @return 多文件解析结果对象，包含提取后的 HTML、CSS 和 JS 代码
     */
    public static MultiFileCodeResult parseMultiFileCode(String codeContent) {
        // 创建多文件解析结果对象
        MultiFileCodeResult result = new MultiFileCodeResult();

        // 分别提取 HTML、CSS 和 JS 代码块内容
        String htmlCode = extractCodeByPattern(codeContent, HTML_CODE_PATTERN);
        String cssCode = extractCodeByPattern(codeContent, CSS_CODE_PATTERN);
        String jsCode = extractCodeByPattern(codeContent, JS_CODE_PATTERN);

        // 如果提取到了 HTML 代码，则设置到结果对象中
        if (htmlCode != null && !htmlCode.trim().isEmpty()) {
            result.setHtmlCode(htmlCode.trim());
        }

        // 如果提取到了 CSS 代码，则设置到结果对象中
        if (cssCode != null && !cssCode.trim().isEmpty()) {
            result.setCssCode(cssCode.trim());
        }

        // 如果提取到了 JS 代码，则设置到结果对象中
        if (jsCode != null && !jsCode.trim().isEmpty()) {
            result.setJsCode(jsCode.trim());
        }

        return result;
    }

    /**
     * 提取 HTML 代码块内容
     * <p>
     * 该方法会使用预定义的 HTML_CODE_PATTERN 正则表达式，
     * 从原始文本中查找第一个 html 代码块，并返回其中的代码内容。
     *
     * @param content 原始内容
     * @return 提取出的 HTML 代码；如果未匹配到则返回 null
     */
    private static String extractHtmlCode(String content) {
        // 创建正则匹配器
        Matcher matcher = HTML_CODE_PATTERN.matcher(content);

        // 如果找到匹配项，则返回第 1 个分组内容，即代码块内部内容
        if (matcher.find()) {
            return matcher.group(1);
        }

        // 未找到匹配内容，返回 null
        return null;
    }

    /**
     * 根据指定正则模式提取代码块内容
     * <p>
     * 这是一个通用提取方法，可用于提取任意类型的 Markdown 代码块内容，
     * 例如 HTML、CSS、JS 等。
     *
     * @param content 原始内容
     * @param pattern 正则模式，不同模式对应不同类型的代码块
     * @return 提取出的代码内容；如果未匹配到则返回 null
     */
    private static String extractCodeByPattern(String content, Pattern pattern) {
        // 根据指定正则创建匹配器
        Matcher matcher = pattern.matcher(content);

        // 如果找到匹配项，则返回第 1 个分组内容，即代码块内部内容
        if (matcher.find()) {
            return matcher.group(1);
        }

        // 未找到匹配内容，返回 null
        return null;
    }
}
```

------



5. **在门面类中增加流式调用 AI 的方法**

   - 都使用流式调用 AI 的方法：`generateHtmlCodeStream` 和 `generateMultiFileCodeStream`

   - 由于是 SSE 输出模式，因此**需要字符串拼接器将每段生成的内容都做好拼接**，等到触发了 `doOnComplete()` 代表 AI 已经全部生成完毕了，才执行**解析其中的 `html, css, js` 代码块**，然后 **保存到本地** 的逻辑。

```Java
/**
 * 统一入口：根据类型生成并保存代码（流式）
 *
 * @param userMessage     用户提示词
 * @param codeGenTypeEnum 生成类型
 */
public Flux<String> generateAndSaveCodeStream(String userMessage, CodeGenTypeEnum codeGenTypeEnum) {
    if (codeGenTypeEnum == null) {
        throw new BusinessException(ErrorCode.SYSTEM_ERROR, "生成类型为空");
    }
    return switch (codeGenTypeEnum) {
        case HTML -> generateAndSaveHtmlCodeStream(userMessage);
        case MULTI_FILE -> generateAndSaveMultiFileCodeStream(userMessage);
        default -> {
            String errorMessage = "不支持的生成类型：" + codeGenTypeEnum.getValue();
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, errorMessage);
        }
    };
}


/**
 * 生成 HTML 模式的代码并保存（流式）
 *
 * @param userMessage 用户提示词
 * @return 保存的目录
 */
private Flux<String> generateAndSaveHtmlCodeStream(String userMessage) {
    Flux<String> result = aiCodeGeneratorService.generateHtmlCodeStream(userMessage);
    // 需要一个字符串拼接器，用于当流式返回所有的代码之后，再保存代码
    StringBuilder codeBuilder = new StringBuilder();
    return result.doOnNext(chunk -> {
        // 实时收集并拼接代码片段
        codeBuilder.append(chunk);
        // 当触发了 doOnComplete() 方法，就代表 AI 已经将所有内容生成完毕了
    }).doOnComplete(() -> {
        try {
            // 当流式返回所有代码之后，保存代码
            String completeHtmlCode = codeBuilder.toString();
            // 解析出 Html 代码部分
            HtmlCodeResult htmlCodeResult = CodeParser.parseHtmlCode(completeHtmlCode);
            // 调用文件保存
            File savedDir = CodeFileSaver.saveHtmlCodeResult(htmlCodeResult);
            log.info("保存成功，路径为：" + savedDir.getAbsolutePath());
        } catch (Exception e) {
            log.error("保存失败: {}", e.getMessage());
        }
    });
}


/**
 * 生成多文件模式的代码并保存（流式）
 *
 * @param userMessage 用户提示词
 * @return 保存的目录
 */
private Flux<String> generateAndSaveMultiFileCodeStream(String userMessage) {
    Flux<String> result = aiCodeGeneratorService.generateMultiFileCodeStream(userMessage);
    // 当流式返回生成代码完成后，再保存代码
    StringBuilder codeBuilder = new StringBuilder();
    return result.doOnNext(chunk -> {
        // 实时收集代码片段
        codeBuilder.append(chunk);
    }).doOnComplete(() -> {
        // 流式返回完成后保存代码
        try {
            String completeMultiFileCode = codeBuilder.toString();
            MultiFileCodeResult multiFileResult = CodeParser.parseMultiFileCode(completeMultiFileCode);
            // 保存代码到文件
            File savedDir = CodeFileSaver.saveMultiFileCodeResult(multiFileResult);
            log.info("保存成功，路径为：" + savedDir.getAbsolutePath());
        } catch (Exception e) {
            log.error("保存失败: {}", e.getMessage());
        }
    });
}
```

------







# *设计模式优化

## 策略模式

- ***把不同算法封装成不同策略类，在运行时自由选择！！！***

策略模式定义⁠⁠⁠⁠⁠了一系列算法，**将每个算法封装起来，并让它们可﻿﻿﻿﻿﻿以相互替换**，使得算法的⁢⁢⁢⁢⁢变化不会影响使用算法的‍‍‍‍‍代码，让项目更好维护和扩展。

<img src="./AI零代码生成平台.assets/pilO8AUvNXolUU23.webp" alt="img" style="zoom: 50%;" />

------



<img src="./AI零代码生成平台.assets/UyZoz3t1b4Dv9KDD.webp" alt="img" style="zoom:50%;" />

1. **定义一套统一的 “代码解析规则入口”**，让不同的解析策略都遵守同一个规范。

不管你将来解析的是：HTML 单文件，多文件代码，Vue 代码，SQL 代码。只要它属于“代码解析策略”，就都要实现这个接口里的方法：这样 **外部调用时就不需要关心具体是哪一种解析器** 了。

```Java
/**
 * 代码解析器策略接口
 *
 * 这是策略模式中的“策略接口”。
 * 它的作用不是负责具体解析，而是定义所有解析策略必须遵守的统一规范。
 *
 * 也就是说：
 * 1. 不同类型的代码解析器都要实现这个接口
 * 2. 外部调用时只依赖接口，不依赖具体实现类
 * 3. 这样就可以在运行时灵活替换不同的解析策略
 *
 * 例如：
 * - HtmlCodeParser 实现该接口，用于解析 HTML 代码
 * - MultiFileCodeParser 实现该接口，用于解析多文件代码
 *
 * 通过这种方式，系统可以用统一的方法调用不同的解析器，
 * 这正是策略模式“定义一组算法，并将它们分别封装起来，使它们可以互相替换”的体现。
 *
 * @param <T> 解析结果类型
 *            使用泛型的目的是让不同策略可以返回不同的结果对象，
 *            例如 HtmlCodeResult、MultiFileCodeResult，
 *            从而提升代码的通用性和类型安全性
 *
 * @author Richer
 */
public interface CodeParser<T> {

    /**
     * 解析代码内容
     *
     * 这是策略接口中定义的统一入口方法。
     * 所有具体的解析器都必须实现这个方法，
     * 以保证外部可以用同一种方式调用不同的解析策略。
     *
     * 这里体现了策略模式的核心思想：
     * “接口统一行为，具体类实现不同算法”。
     *
     * 比如：
     * - HTML 解析器会在这里提取 html 代码块
     * - 多文件解析器会在这里分别提取 html、css、js 代码块
     *
     * 外部并不需要关心具体解析细节，
     * 只需要调用 parseCode 方法即可完成解析，从而实现解耦。
     *
     * @param codeContent 原始代码内容，通常是 AI 返回的完整文本内容
     * @return 解析后的结果对象，具体返回哪种类型由实现类决定
     */
    T parseCode(String codeContent);
}
```

------



2. 将原本的 `CodeParser` 拆分成 **`HtmlCodeParser`（解析单个 Html 文件）**和 **`MultiFileCodeParser`（多文件代码解析器）**

- **以 `HtmlCodeParser` 为例！！：**

```Java
public class HtmlCodeParser implements CodeParser<HtmlCodeResult> {

    private static final Pattern HTML_CODE_PATTERN = Pattern.compile("```html\\s*\\n([\\s\\S]*?)```", Pattern.CASE_INSENSITIVE);

    @Override
    public HtmlCodeResult parseCode(String codeContent) {
        // 创建解析结果对象
        HtmlCodeResult result = new HtmlCodeResult();

        // 提取 HTML 代码块中的内容
        String htmlCode = extractHtmlCode(codeContent);

        // 如果成功提取到 HTML 代码，则去除首尾空白后设置到结果中
        if (htmlCode != null && !htmlCode.trim().isEmpty()) {
            result.setHtmlCode(htmlCode.trim());
        } else {
            // 如果没有找到代码块，将整个内容作为HTML
            result.setHtmlCode(codeContent.trim());
        }
        return result;
    }

    /**
     * 提取HTML代码内容
     *
     * @param content 原始内容
     * @return HTML代码
     */
    private String extractHtmlCode(String content) {
        Matcher matcher = HTML_CODE_PATTERN.matcher(content);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return null;
    }
}
```

------



3. ***编写代码解析执行器，根据代码生成类型执行相应的解析逻辑：***

```java
/**
 * 代码解析执行器
 * 根据代码生成类型执行相应的解析逻辑
 *
 * @author Richer
 */
public class CodeParserExecutor {

    // 创建 html 代码解析器
    private static final HtmlCodeParser htmlCodeParser = new HtmlCodeParser();
    // 创建 多文件代码解析器
    private static final MultiFileCodeParser multiFileCodeParser = new MultiFileCodeParser();

    /**
     * 执行代码解析
     *
     * @param codeContent 代码内容
     * @param codeGenType 代码生成类型
     * @return 解析结果（HtmlCodeResult 或 MultiFileCodeResult）
     */
    public static Object executeParser(String codeContent, CodeGenTypeEnum codeGenType) {
        return switch (codeGenType) {
            case HTML -> htmlCodeParser.parseCode(codeContent);
            case MULTI_FILE -> multiFileCodeParser.parseCode(codeContent);
            default -> throw new BusinessException(ErrorCode.SYSTEM_ERROR, "不支持的代码生成类型: " + codeGenType);
        };
    }
}
```

------







## 模板方法模式

- ***主要是定义算法执行流程！！！***

模板方法模式 **⁠⁠⁠⁠⁠在抽象父类中定义了操作的标准流程**，将一些具体﻿﻿﻿﻿﻿实现步骤交给子类，使得⁢⁢⁢⁢⁢子类可以在不改变流程的‍‍‍‍‍情况下重新定义某些特定步骤。

<img src="./AI零代码生成平台.assets/dDshmS2Nt12U0OLd.webp" alt="img" style="zoom: 50%;" />

------



<img src="./AI零代码生成平台.assets/pg2kO6IcfF8XV6Fe.webp" alt="img" style="zoom:50%;" />

1. **构建父类模板方法**

***注意***：确定父类中的模板方法，可以抓住一个核心判断标准：**先找“流程骨架”，再找“可变步骤”。**

也就是说，模板方法不是随便挑一个父类方法就行，而是要找那个能够代表**某类业务统一执行顺序**的方法。

```java 
/**
 * 抽象代码文件保存器 - 模板方法模式
 *
 * @author Richer
 */
public abstract class CodeFileSaverTemplate<T> {

    // 文件保存根目录
    protected static final String FILE_SAVE_ROOT_DIR = System.getProperty("user.dir") + "/tmp/code_output";

    /**
     * 模板方法：保存代码的标准流程
     *
     * @param result 代码结果对象
     * @return 保存的目录
     */
    public final File saveCode(T result) {
        // 1. 验证输入
        validateInput(result);
        // 2. 构建唯一目录
        String baseDirPath = buildUniqueDir();
        // 3. 保存文件（具体实现由子类提供）
        saveFiles(result, baseDirPath);
        // 4. 返回目录文件对象
        return new File(baseDirPath);
    }


    /**
     * 验证输入参数（可由子类覆盖）
     *
     * @param result 代码结果对象
     */
    protected void validateInput(T result) {
        if (result == null) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "代码结果对象不能为空");
        }
    }


    /**
     * 构建唯一目录路径
     *
     * @return 目录路径
     */
    protected final String buildUniqueDir() {
        String codeType = getCodeType().getValue();
        String uniqueDirName = StrUtil.format("{}_{}", codeType, IdUtil.getSnowflakeNextIdStr());
        String dirPath = FILE_SAVE_ROOT_DIR + File.separator + uniqueDirName;
        FileUtil.mkdir(dirPath);
        return dirPath;
    }


    /**
     * 写入单个文件的工具方法【供子类在保存文件的时候调用】
     *
     * @param dirPath  目录路径
     * @param filename 文件名
     * @param content  文件内容
     */
    protected final void writeToFile(String dirPath, String filename, String content) {
        if (StrUtil.isNotBlank(content)) {
            String filePath = dirPath + File.separator + filename;
            FileUtil.writeString(content, filePath, StandardCharsets.UTF_8);
        }
    }


    /**
     * 获取代码类型（由子类实现）
     *
     * @return 代码生成类型
     */
    protected abstract CodeGenTypeEnum getCodeType();

    /**
     * 保存文件的具体实现（由子类实现）
     *
     * @param result      代码结果对象
     * @param baseDirPath 基础目录路径
     */
    protected abstract void saveFiles(T result, String baseDirPath);
}
```

------



2. ***将原本的 `CodeFileSaver` 拆分成 `HtmlCodeFileSaverTemplate` 和 `MultiFileCodeFileSaverTemplate` 模板方法来实现父类流程，保存不同类型的文件***
   - 以 `HtmlCodeFileSaverTemplate` 为例：

```java 
/**
 * HTML代码文件保存器
 * 
 * 该类继承抽象父类 CodeFileSaverTemplate，
 * 属于模板方法模式中的“具体子类”：
 * 1. 复用父类定义好的保存流程
 * 2. 实现当前类型对应的可变步骤
 *
 * 当前类主要负责：
 * 1. 指定当前保存器对应的代码生成类型为 HTML
 * 2. 实现 HTML 文件的具体保存逻辑
 * 3. 补充 HTML 代码内容的个性化校验
 *
 * @author Richer
 */
public class HtmlCodeFileSaverTemplate extends CodeFileSaverTemplate<HtmlCodeResult> {

    /**
     * 获取当前保存器支持的代码类型
     *
     * 该方法由父类定义为抽象方法，
     * 子类必须返回自己对应的代码生成类型。
     *
     * 这里返回 HTML，表示当前保存器专门用于保存 HTML 单文件代码。
     *
     * @return HTML 代码生成类型
     */
    @Override
    protected CodeGenTypeEnum getCodeType() {
        return CodeGenTypeEnum.HTML;
    }

    /**
     * 保存文件的具体实现
     *
     * 该方法是模板方法模式中的“可变步骤”之一，
     * 由子类决定具体要保存哪些文件、文件名是什么。
     *
     * 对于 HTML 单文件场景，只需要将 HTML 内容保存为 index.html 文件。
     *
     * @param result HTML 代码解析结果对象
     * @param baseDirPath 文件保存的基础目录路径
     */
    @Override
    protected void saveFiles(HtmlCodeResult result, String baseDirPath) {
        // 保存 HTML 文件到基础目录下，文件名固定为 index.html
        writeToFile(baseDirPath, "index.html", result.getHtmlCode());
    }

    /**
     * 校验输入参数
     *
     * 该方法重写了父类的默认校验逻辑，
     * 在父类“结果对象不能为空”的基础上，
     * 进一步校验 HTML 代码内容不能为空。
     *
     * 这样可以保证保存文件前，HTML 内容是合法的。
     *
     * @param result HTML 代码结果对象
     */
    @Override
    protected void validateInput(HtmlCodeResult result) {
        // 先执行父类的公共校验逻辑，确保结果对象本身不为空
        super.validateInput(result);

        // HTML 代码内容不能为空【即子类添加的进一步校验逻辑】，否则无法生成有效的 HTML 文件
        if (StrUtil.isBlank(result.getHtmlCode())) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "HTML代码内容不能为空");
        }
    }
}
```

------



3. 文⁠⁠⁠件保存执行器，根据代码生成类﻿型执﻿行相﻿应的保⁢存逻辑⁢【**集中管理保存文件到本地的逻辑**】

```Java
/**
 * 代码文件保存执行器
 * 根据代码生成类型执行相应的保存逻辑
 *
 * @author yupi
 */
public class CodeFileSaverExecutor {

    private static final HtmlCodeFileSaverTemplate htmlCodeFileSaver = new HtmlCodeFileSaverTemplate();

    private static final MultiFileCodeFileSaverTemplate multiFileCodeFileSaver = new MultiFileCodeFileSaverTemplate();

    /**
     * 执行代码保存
     *
     * @param codeResult  代码结果对象
     * @param codeGenType 代码生成类型
     * @return 保存的目录
     */
    public static File executeSaver(Object codeResult, CodeGenTypeEnum codeGenType) {
        return switch (codeGenType) {
            case HTML -> htmlCodeFileSaver.saveCode((HtmlCodeResult) codeResult);
            case MULTI_FILE -> multiFileCodeFileSaver.saveCode((MultiFileCodeResult) codeResult);
            default -> throw new BusinessException(ErrorCode.SYSTEM_ERROR, "不支持的代码生成类型: " + codeGenType);
        };
    }
}
```

------







## 执行器模式

正常情况下，可以通过⁠⁠⁠⁠⁠工厂模式来创建不同的策略或模板方法，但**由于每种生成模式的参数和返回值不同（`Htm﻿﻿﻿﻿﻿lCodeResult` 和 `MultiF⁢⁢⁢⁢⁢ileCodeResult`）**，很难对通过‍‍‍‍‍工厂模式创建出来的对象进行统一的调用。

```java
public HtmlCodeResult parseCode(String codeContent) {}

public MultiFileCodeResult parseCode(String codeContent) {}

void saveFiles(HtmlCodeResult result, String baseDirPath) {}

void saveFiles(MultiFileCodeResult result, String baseDirPath) {}
```

- **对于方法参⁠⁠⁠⁠⁠数不同的策略模式和模板方法模﻿﻿﻿式，﻿建议﻿使用执⁢⁢⁢行器模⁢式（E⁢x‍‍‍ecu‍tor）。**
- 执行器模式 **提供⁠⁠⁠⁠⁠统一的执行入口来协调不同策略和模板的调用**，特别适合处﻿﻿﻿﻿﻿理参数类型不同但业务逻辑相⁢⁢⁢⁢⁢似的场景

<img src="./AI零代码生成平台.assets/9gewAtIw0yFwhslN.webp" alt="img" style="zoom:50%;" />

------







## 最终方案

- **执行器模式**：提供统一的执行入口，根据生成类型执行不同的操作【**紧接着门面模式**】
- **策略模式**：每种模式对应的解析方法单独作为一个类来维护【***左分支***】
- **模板方法模式**：抽象模板类定义了通用的文件保存流程，子类可以有自己的实现（比如多文件生成模式需要保存 3 个文件，而原生 HTML 模式只需要保存 1 个文件）【***右分支***】

<img src="./AI零代码生成平台.assets/S2CK2YavtvFzhDRH.webp" alt="img" style="zoom:50%;" />

------







### 门面类优化

- 整个流程是由**门面类 `AiCodeGeneratorFacade` 作为统一入口**，先根据用户传入的提示词和代码生成类型，调用 AI 生成对应代码；如果是非流式场景，就直接拿到结果对象后进入保存流程；如果是流式场景，就先把分段返回的代码片段拼接成完整代码，再继续做解析和保存。这个整体入口把“生成、解析、保存”几步串了起来，对外屏蔽了内部细节。
- 其中，**策略模式**体现在“**不同类型使用不同解析器 / 保存器**”上。也就是说，系统会根据 `CodeGenTypeEnum` 选择对应的处理策略：比如 **HTML 类型走 HTML 的解析逻辑和保存逻辑，多文件类型走多文件的解析逻辑和保存逻辑**。这样做的好处是，不同代码类型的处理规则被拆分开了，后续如果新增新的代码类型，只需要新增对应策略实现即可。
- **执行器模式**体现在 `CodeParserExecutor` 和 `CodeFileSaverExecutor` 上。它们不负责具体业务细节，而是负责“调度”和“分发”——也就是**根据代码类型，把请求转交给合适的解析器或保存器执行**。这样调用方不需要直接依赖具体的实现类，只要把结果对象和类型交给执行器即可，系统就能自动路由到正确的处理逻辑。
- **模板方法模式**体现在文件保存这一层。**父类先定义好保存代码文件的标准流程**，例如“校验输入 → 创建唯一目录 → 保存文件 → 返回目录”，这个流程是固定的；而**具体保存哪些文件、如何校验内容，则由不同子类去实现**。比如 HTML 保存器只保存 `index.html`，多文件保存器则分别保存 `index.html`、`style.css` 和 `script.js`。这样既保证了保存流程统一，又保留了不同类型文件保存方式的灵活性。

```Java
/**
 * AI 代码生成门面类
 *
 * 在当前设计中，该类主要整合了以下几个模块：
 * 1. AiCodeGeneratorService：负责调用大模型生成代码
 * 2. CodeParserExecutor：负责根据生成类型解析代码内容
 * 3. CodeFileSaverExecutor：负责根据生成类型保存代码文件
 */
@Slf4j
@Service
public class AiCodeGeneratorFacade {

    /**
     * AI 代码生成服务
     */
    @Resource
    private AiCodeGeneratorService aiCodeGeneratorService;

    /**
     * 统一入口：根据代码生成类型生成并保存代码（非流式）
     *
     * 处理流程如下：
     * 1. 校验生成类型是否为空
     * 2. 根据不同代码类型调用对应的大模型生成方法
     * 3. 获取生成结果对象
     * 4. 调用文件保存执行器，将结果保存到本地目录
     * 5. 返回保存目录
     *
     * 这里通过 switch 对不同代码类型进行分发，
     * 使调用方无需直接依赖具体生成实现和具体保存实现。
     *
     * @param userMessage 用户提示词
     * @param codeGenTypeEnum 代码生成类型
     * @return 代码保存后的目录文件对象
     */
    public File generateAndSaveCode(String userMessage, CodeGenTypeEnum codeGenTypeEnum) {
        // 校验生成类型不能为空，否则无法确定使用哪种生成和保存逻辑
        if (codeGenTypeEnum == null) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "生成类型为空");
        }

        // 根据不同生成类型，调用对应的代码生成逻辑和文件保存逻辑
        return switch (codeGenTypeEnum) {
            case HTML -> {
                // 调用 AI 服务生成 HTML 单文件代码结果
                HtmlCodeResult result = aiCodeGeneratorService.generateHtmlCode(userMessage);

                // 使用 文件保存执行器 统一保存 HTML 代码【根据传入不同的代码生成类型，自动跳转到对应的保存方法】
                // 区别于 return 直接返回一个方法的最终结果，yield 是当前 switch 分支的返回方式
                yield CodeFileSaverExecutor.executeSaver(result, CodeGenTypeEnum.HTML);
            }
            case MULTI_FILE -> {
                // 调用 AI 服务生成多文件代码结果
                MultiFileCodeResult result = aiCodeGeneratorService.generateMultiFileCode(userMessage);

                // 使用 文件保存执行器 统一保存多文件代码【根据传入不同的代码生成类型，自动跳转到对应的保存方法】
                yield CodeFileSaverExecutor.executeSaver(result, CodeGenTypeEnum.MULTI_FILE);
            }
            default -> {
                // 如果传入的生成类型系统暂不支持，则抛出业务异常
                String errorMessage = "不支持的生成类型：" + codeGenTypeEnum.getValue();
                throw new BusinessException(ErrorCode.SYSTEM_ERROR, errorMessage);
            }
        };
    }

    /**
     * 统一入口：根据代码生成类型生成并保存代码（流式）
     *
     * 该方法用于处理流式代码生成场景，
     * 例如前端需要边生成边展示代码内容时使用。
     *
     * 处理流程如下：
     * 1. 校验生成类型是否为空
     * 2. 根据不同代码类型调用对应的大模型流式生成方法
     * 3. 将生成得到的代码流交给 processCodeStream 统一处理
     * 4. 在流式返回结束后自动完成 代码解析 与 文件保存
     *
     * 与非流式方法的区别：
     * - 非流式：一次性拿到结果对象后直接保存
     * - 流式：先逐步返回代码片段，结束后再统一解析和保存
     *
     * @param userMessage 用户提示词
     * @param codeGenTypeEnum 代码生成类型
     * @return 流式代码内容，供前端实时消费
     */
    public Flux<String> generateAndSaveCodeStream(String userMessage, CodeGenTypeEnum codeGenTypeEnum) {
        // 校验生成类型不能为空
        if (codeGenTypeEnum == null) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "生成类型为空");
        }

        // 根据不同生成类型，调用对应的流式代码生成逻辑
        return switch (codeGenTypeEnum) {
            case HTML -> {
                // 调用 AI 服务流式生成 HTML 代码
                Flux<String> codeStream = aiCodeGeneratorService.generateHtmlCodeStream(userMessage);

                // 对流式代码进行统一处理：收集、解析、保存
                yield processCodeStream(codeStream, CodeGenTypeEnum.HTML);
            }
            case MULTI_FILE -> {
                // 调用 AI 服务流式生成多文件代码
                Flux<String> codeStream = aiCodeGeneratorService.generateMultiFileCodeStream(userMessage);

                // 对流式代码进行统一处理：收集、解析、保存
                yield processCodeStream(codeStream, CodeGenTypeEnum.MULTI_FILE);
            }
            default -> {
                // 如果传入的生成类型系统暂不支持，则抛出业务异常
                String errorMessage = "不支持的生成类型：" + codeGenTypeEnum.getValue();
                throw new BusinessException(ErrorCode.SYSTEM_ERROR, errorMessage);
            }
        };
    }
    
    
    /**
     * 通用流式代码处理方法
     *
     * 该方法用于统一处理流式代码生成后的后置逻辑，
     * 属于门面类内部的公共处理方法。
     *
     * 主要职责：
     * 1. 在流式返回过程中实时拼接每一段代码片段
     * 2. 在流式生成完成后，将完整代码内容进行统一解析
     * 3. 根据代码生成类型调用对应的保存器完成文件落盘
     * 4. 记录保存成功或失败日志
     *
     * 说明：
     * 流式场景下，大模型会持续返回代码片段，
     * 因此不能像普通生成那样一次性拿到完整结果对象。
     * 所以需要先用 StringBuilder 将所有片段拼接成完整代码，
     * 再在流结束时进行解析和保存。
     *
     * @param codeStream 代码流，每个元素表示一段代码片段
     * @param codeGenType 代码生成类型，用于决定后续解析器和保存器的选择
     * @return 原始流式响应，供前端或调用方继续消费
     */
    private Flux<String> processCodeStream(Flux<String> codeStream, CodeGenTypeEnum codeGenType) {
        // 用于缓存流式返回的完整代码内容
        StringBuilder codeBuilder = new StringBuilder();

        return codeStream.doOnNext(chunk -> {
            // 在流式返回过程中，持续收集每一段代码片段
            codeBuilder.append(chunk);
        }).doOnComplete(() -> {
            // 当流式响应结束后，说明完整代码已经生成完毕
            try {
                // 获取拼接后的完整代码内容
                String completeCode = codeBuilder.toString();

                // 使用 代码解析执行器，根据 代码类型 选择对应 解析器，
                // 将原始代码文本解析为结构化结果对象
                Object parsedResult = CodeParserExecutor.executeParser(completeCode, codeGenType);

                // 使用 文件保存执行器，根据 代码类型 选择对应 保存器，
                // 将解析后的结果对象保存为本地文件
                File savedDir = CodeFileSaverExecutor.executeSaver(parsedResult, codeGenType);

                // 记录保存成功日志，方便后续排查和追踪
                log.info("保存成功，路径为：" + savedDir.getAbsolutePath());
            } catch (Exception e) {
                // 流式返回不应因保存失败而中断前端展示，因此这里只记录异常日志
                log.error("保存失败: {}", e.getMessage());
            }
        });
    }
}
```

------









# 应用模块

1. 流程解析

用户在主页输入提⁠⁠⁠示词后，**系统会创建一个应用记录**，然后 **跳转到对话页面与 AI ﻿﻿﻿交互生成网站**。生成完成后，用户⁢⁢⁢可以预览效果，满意后进行部署，‍‍‍让网站真正对外提供服务。【涉及到**数据存储、权限控制、文件管理、﻿﻿﻿网站部署**等多个技术⁢⁢环节⁢】

------



2. **应用表的设计**

其中最关键的是 `deployKey` 字段。由于**每个网站应用文件的部署都是隔离的**（想象成沙箱），需要用唯一字段来区分，可以作为应用的存储和访问路径；而且为了便于访问，每个应用的访问路径不能太长。

- `priority` 优先级字段：我们**约定 99 表示精选应用，这样可以在主页展示高质量的应用**，避免用户看到大量测试内容。

​	***为什么用数字⁠⁠⁠而不用枚举类型呢？原因是这样更利于扩展***，比如﻿﻿﻿约定 999 表示置顶⁢⁢⁢；还可以根据数字灵活调‍‍‍整各个应用的具体展示顺序。

- 添加索引⁠⁠⁠：给 `deployKey`、`appName`、`u﻿﻿﻿serId` **三个经常用⁢⁢⁢于作为查询条件的字段增‍‍‍加索引，提高查询性能。**

​	注意，我们 **暂时⁠⁠⁠不考虑将应用代码直接保存到数据库字段中**，而是保存在文件系﻿﻿﻿统里。这样可以避免数据库和文⁢⁢⁢件存储不一致的问题，也便于后‍‍‍续扩展到对象存储等方案。

```sql
-- 应用表
create table app
(
    id           bigint auto_increment comment 'id' primary key,
    appName      varchar(256)                       null comment '应用名称',
    cover        varchar(512)                       null comment '应用封面',
    initPrompt   text                               null comment '应用初始化的 prompt',
    codeGenType  varchar(64)                        null comment '代码生成类型（枚举）',
    deployKey    varchar(64)                        null comment '部署标识',
    deployedTime datetime                           null comment '部署时间',
    priority     int      default 0                 not null comment '优先级',
    userId       bigint                             not null comment '创建用户id',
    editTime     datetime default CURRENT_TIMESTAMP not null comment '编辑时间',
    createTime   datetime default CURRENT_TIMESTAMP not null comment '创建时间',
    updateTime   datetime default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    isDelete     tinyint  default 0                 not null comment '是否删除',
    UNIQUE KEY uk_deployKey (deployKey), -- 确保部署标识唯一
    INDEX idx_appName (appName),         -- 提升基于应用名称的查询性能
    INDEX idx_userId (userId)            -- 提升基于用户 ID 的查询性能
) comment '应用' collate = utf8mb4_unicode_ci;
```

------



3. **CRUD部分**

<img src="./AI零代码生成平台.assets/Screenshot 2026-03-30 122626.png" style="zoom:67%;" />

------

- **以创建应用为例：**

```Java
/**
 * 创建应用
 *
 * @param appAddRequest 创建应用请求
 * @param request       请求
 * @return 应用 id
 */
@PostMapping("/add")
public BaseResponse<Long> addApp(@RequestBody AppAddRequest appAddRequest, HttpServletRequest request) {
    ThrowUtils.throwIf(appAddRequest == null, ErrorCode.PARAMS_ERROR);

    // 参数校验
    String initPrompt = appAddRequest.getInitPrompt();
    ThrowUtils.throwIf(StrUtil.isBlank(initPrompt), ErrorCode.PARAMS_ERROR, "初始化 prompt 不能为空");

    // 获取当前登录用户
    User loginUser = userService.getLoginUser(request);

    // 构造入库对象
    App app = new App();
    BeanUtil.copyProperties(appAddRequest, app);
    app.setUserId(loginUser.getId());
    // 应用名称暂时为 initPrompt 前 12 位
    app.setAppName(initPrompt.substring(0, Math.min(initPrompt.length(), 12)));
    // 暂时设置为多文件生成
    app.setCodeGenType(CodeGenTypeEnum.MULTI_FILE.getValue());

    // 插入数据库
    boolean result = appService.save(app);
    ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
    
    return ResultUtils.success(app.getId());
}
```

------







## 应用生成

1. 用户在主页 **输入提示词创建应用（入库）**

2. 获得应用 ID 后跳转到对话页面

3. 系统 **自动使用初始提示词与 AI 对话** 生成网站代码

   - 由于应用的生成过程和 AI 对话是绑定的，我们可以 **提供一个名为 `chatToGenCode` 的应用生成接口，调用之前开发的 AI 代码生成门面完成任务，并且流式返回给前端**。前端不需要区分用户是否是第一次和该应用对话，始终调用这个接口即可，需要怎么做都交给后端来判断。

   - 一定要 **确保生成的文件能够与应用正确关联**，因此这次生成的网站目录名称不再是之前的 `codeType_雪花算法`，而是 `codeGenType_appId`，这样就**能通过 `appId` 查数据库获取应用信息**、再根据应用信息找到对应的网站目录了。

------



- **开发流程：**

1. 首先需要修改 `CodeFileSaverTemplate` 的 `saveCode` 和 `buildUniqueDir` 方法，***使其支持基于 `appId` 的目录命名：***

```java
/**
 * 模板方法：保存代码的标准流程（使用 appId）
 *
 * @param result 代码结果对象
 * @param appId  应用 ID
 * @return 保存的目录
 */
public final File saveCode(T result, Long appId) {
    // 1. 验证输入
    validateInput(result);
    // 2. 构建基于 appId 的目录
    String baseDirPath = buildUniqueDir(appId);
    // 3. 保存文件（具体实现由子类提供）
    saveFiles(result, baseDirPath);
    // 4. 返回目录文件对象
    return new File(baseDirPath);
}

/**
 * 构建基于 appId 的目录路径
 *
 * @param appId 应用 ID
 * @return 目录路径
 */
protected final String buildUniqueDir(Long appId) {
    if (appId == null) {
        throw new BusinessException(ErrorCode.PARAMS_ERROR, "应用 ID 不能为空");
    }
    String codeType = getCodeType().getValue();
    String uniqueDirName = StrUtil.format("{}_{}", codeType, appId);
    String dirPath = FILE_SAVE_ROOT_DIR + File.separator + uniqueDirName;
    FileUtil.mkdir(dirPath);
    return dirPath;
}
```

------



2. 到外层执行器类补充 `appId` 参数

```java
/**
 * 执行代码保存（使用 appId）
 *
 * @param codeResult  代码结果对象
 * @param codeGenType 代码生成类型
 * @param appId       应用 ID
 * @return 保存的目录
 */
public static File executeSaver(Object codeResult, CodeGenTypeEnum codeGenType, Long appId) {
    return switch (codeGenType) {
        case HTML -> htmlCodeFileSaver.saveCode((HtmlCodeResult) codeResult, appId);
        case MULTI_FILE -> multiFileCodeFileSaver.saveCode((MultiFileCodeResult) codeResult, appId);
        default -> throw new BusinessException(ErrorCode.SYSTEM_ERROR, "不支持的代码生成类型: " + codeGenType);
    };
}
```

------



3. 在 App⁠⁠⁠Service 中编写 `ch﻿at﻿To﻿Gen⁢Cod⁢e` 方⁢法‍，**调用门‍面生成代‍码【即最外层代码】**：

```Java
@Override
public Flux<String> chatToGenCode(Long appId, String message, User loginUser) {
    // 1. 参数校验
    ThrowUtils.throwIf(appId == null, ErrorCode.PARAMS_ERROR, "应用ID不能为空");
    ThrowUtils.throwIf(StrUtil.isBlank(message), ErrorCode.PARAMS_ERROR, "提示词不能为空");

    // 2. 查询应用消息
    App app = this.getById(appId);
    ThrowUtils.throwIf(app == null, ErrorCode.NOT_FOUND_ERROR, "应用不存在");

    // 3. 验证用户是否有权限访问该应用，仅本人可以生成代码
    if(!app.getUserId().equals(loginUser.getId())) {
        throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "无权限访问该应用");
    }

    // 4. 获取应用的代码生成类型【是单个 html 类型还是多文件类型】
    String codeGenTypeStr = app.getCodeGenType();
    CodeGenTypeEnum codeGenTypeEnum = CodeGenTypeEnum.getEnumByValue(codeGenTypeStr);
    if (codeGenTypeEnum == null) {
        throw new BusinessException(ErrorCode.SYSTEM_ERROR, "不支持的代码生成类型");
    }

    // 5. 调用 AI 生成代码
    return aiCodeGeneratorFacade.generateAndSaveCodeStream(message, codeGenTypeEnum, appId);
}
```

------



4. SSE 流式开发接口

`AppCon⁠⁠⁠troller` 新增接口，注意要声明为 SSE ﻿﻿﻿流式返回，使用 get ⁢⁢⁢请求便于前端使用 `Eve‍‍‍ntSource` 对接：

```java
/**
 * 应用聊天生成代码（流式 SSE）
 *
 * @param appId   应用 ID
 * @param message 用户消息
 * @param request 请求对象
 * @return 生成结果流
 */
@GetMapping(value = "/chat/gen/code", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
public Flux<String> chatToGenCode(@RequestParam Long appId,
                                  @RequestParam String message,
                                  HttpServletRequest request) {
    // 参数校验
    ThrowUtils.throwIf(appId == null || appId <= 0, ErrorCode.PARAMS_ERROR, "应用ID无效");
    ThrowUtils.throwIf(StrUtil.isBlank(message), ErrorCode.PARAMS_ERROR, "用户消息不能为空");

    // 获取当前登录用户
    User loginUser = userService.getLoginUser(request);
    // 调用服务流式生成代码
    return appService.chatToGenCode(appId, message, loginUser);
}
```

------







### 空格丢失问题

流式返回的一段段文本，前端可能会无法识别出空格和回车的内容。

- ***包装思路解决：将返回值封装到 JSON 中，通过封装一层 Map，从而识别到每段生成的文本内容：***

<img src="./AI零代码生成平台.assets/cgepPbdCXe1ir2XL.webp" alt="img" style="zoom: 50%;" />

------



- **在项目中实现这种 Map 的包装**

```Java
@GetMapping(value = "/chat/gen/code", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
public Flux<ServerSentEvent<String>> chatToGenCode(@RequestParam Long appId,
                                  @RequestParam String message,
                                  HttpServletRequest request) {
    // 参数校验
    ThrowUtils.throwIf(appId == null || appId <= 0, ErrorCode.PARAMS_ERROR, "应用ID无效");
    ThrowUtils.throwIf(StrUtil.isBlank(message), ErrorCode.PARAMS_ERROR, "用户消息不能为空");

    // 获取当前登录用户
    User loginUser = userService.getLoginUser(request);
    // 调用服务流式生成代码
    Flux<String> contentFlux = appService.chatToGenCode(appId, message, loginUser);

    
    // =============================================================================================
    // 转换为 ServerSentEvent 格式
    return contentFlux
            .map(chunk -> {
                // 将内容包装成JSON对象
                Map<String, String> wrapper = Map.of("d", chunk);
                String jsonData = JSONUtil.toJsonStr(wrapper);
                return ServerSentEvent.<String>builder()
                        .data(jsonData)
                        .build();
            });
}
```

------

- **注意**：在 SSE 中，***当服务器关闭连接时，会触发客户端的 `onclose` 事件***，这是前端判断流结束的标准方式。但是，`onclose`事件会在连接正常结束（服务器主动关闭）和异常中断（如网络问题）时都触发，前端就很难区分到底后端是正常响应了所有数据、还是异常中断了。
- 因此，我们***最好在后端添加一个明确的 `done` 事件，这样可以更清晰地区分流的正常结束和异常中断***。

***在上面代码的基础上，在最后加上：***

```Java
.concatWith(Mono.just(
                    // 发送结束事件
                    ServerSentEvent.<String>builder()
                            .event("done")
                            .data("")
                            .build()
            ));
```

------



最终效果：

<img src="./AI零代码生成平台.assets/HoeichuwTCk28DJT.webp" alt="img" style="zoom:50%;" />

------







## 应用部署

### Nginx

部署的整体思路是：把本地生成的文件同步到一个 **Web 服务器** 上。可以是同一个服务器的不同目录，也可以是不同服务器，但显然前者成本更低。

------



1. **nginx 的配置，使其能够根据路径匹配不同的网站**

进入 nginx 的 conf 目录下的 `nginx.conf` 文件，在 server 块下添加配置：

- ***注意，Windows 系统的路径斜杠要相反【即将 \ 变成 /】***

上述配置中使用了 `try_files` 指令，能够按顺序尝试多个文件路径，从而更灵活地处理文件访问。举个例子，**当访问 `/app/style.css` 时，会先尝试找到 `/app/style.css` 文件，如果不存在则返回 `/app/index.html`，最后才返回 404 错误**，这样能够适配后续我们要生成的 Vue 单页面应用。

💡 try_files 指令的具体解释：

- `/$1/$2`：第一个尝试的路径
- `/$1/index.html`：第二个尝试的路径
- `=404`：如果都找不到，返回 404 错误

------

- **注意：`D:/IDEA Projects/yu-ai-agent/ai-code-creater/tmp/code_output;` 是本地存放静态代码的位置**

<img src="./AI零代码生成平台.assets/image-20260331165448428.png" alt="image-20260331165448428" style="zoom: 67%;" />

------

- **命令：**

		charset      utf-8;
		charset_types text/css application/javascript text/plain text/xml application/json;
		# 项目部署根目录
		root         D:/IDEA Projects/yu-ai-agent/ai-code-creater/tmp/code_output;
		
		# 处理所有请求
		location ~ ^/([^/]+)/(.*)$ {
			try_files /$1/$2 /$1/index.html =404;
		}

------



2. **启动 Nginx，或者输入命令来重载配置：**

```shell
nginx -s reload
```

------



3. 在网站上访问：`localhost/multi_file_2038144454829350912/`，即可访问到对应的前端页面

------







### 项目中部署

***作用：把某个应用已经生成好的前端代码，复制到可对外访问的部署目录下，并返回一个访问链接。***

部署接口接受 `appId` 作为请求参数，返回可访问的 URL 地址 `${部署域名}/{deployKey}`。

**部署流程如下：**

1. **参数校验**：比如是否存在 App、用户是否有权限部署该应用（仅本人可以部署）
2. **生成 deployKey**：之前设计库表时已经提到了 deployKey 的生成逻辑（6 位大小写字母 + 数字），还要注意不能跟已有的 key 重复；此外，每个 app 只生成一次 deployKey，已有则不生成。
3. **部署操作**：本质是***将 `code_output` 目录下的临时文件复制到 `code_deploy` 目录下***，为了简化访问地址，直接将 `deployKey` 作为文件名。

------



1. 首先在 `AppConstant` 中定义常量：

```java
/**
 * 应用生成目录
 */
String CODE_OUTPUT_ROOT_DIR = System.getProperty("user.dir") + "/tmp/code_output";

/**
 * 应用部署目录
 */
String CODE_DEPLOY_ROOT_DIR = System.getProperty("user.dir") + "/tmp/code_deploy";

/**
 * 应用部署域名
 */
String CODE_DEPLOY_HOST = "http://localhost";
```

`CodeF⁠⁠⁠ileSaverTemplate`﻿﻿ 中﻿使用文件保存⁢⁢根目录⁢常量（**用于‍‍保存生成‍的文件**）：

```java
// 文件保存根目录
protected static final String FILE_SAVE_ROOT_DIR = AppConstant.CODE_OUTPUT_ROOT_DIR;
```

`Stati⁠⁠⁠cResourceControlle﻿﻿﻿r` 中使用文件保存⁢⁢⁢根目录常量，因为要‍‍‍在生成时就预览效果：

```java
// 应用生成根目录（用于浏览）
private static final String PREVIEW_ROOT_DIR = AppConstant.CODE_OUTPUT_ROOT_DIR;
```

------



2. **编写部署请求类**

```Java
@Data
public class AppDeployRequest implements Serializable {

    /**
     * 应用 id
     */
    private Long appId;

    private static final long serialVersionUID = 1L;
}
```

------



3. **部署业务**

```Java
@Override
public String deployApp(Long appId, User loginUser) {
    // 1. 参数校验
    ThrowUtils.throwIf(appId == null || appId <= 0, ErrorCode.PARAMS_ERROR, "应用 ID 不能为空");
    ThrowUtils.throwIf(loginUser == null, ErrorCode.NOT_LOGIN_ERROR, "用户未登录");

    // 2. 查询应用信息
    App app = this.getById(appId);
    ThrowUtils.throwIf(app == null, ErrorCode.NOT_FOUND_ERROR, "应用不存在");

    // 3. 验证用户是否有权限部署该应用，仅本人可以部署
    if (!app.getUserId().equals(loginUser.getId())) {
        throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "无权限部署该应用");
    }

    // 4. 检查是否已经有 deployKey【给每个应用生成一个 "对外访问" 时使用的唯一部署标识。】
    // 第一次部署时生成 deployKey，以后再次部署时继续复用原来的 deployKey。
    String deployKey = app.getDeployKey();
    // 没有则生成 6 位 deployKey（大小写字母 + 数字）
    if (StrUtil.isBlank(deployKey)) {
        deployKey = RandomUtil.randomString(6);
    }

    // 5. 获取代码生成类型，构建源目录路径
    String codeGenType = app.getCodeGenType();
    String sourceDirName = codeGenType + "_" + appId;
    // 生成类似：/tmp/code_output/multi_file_2038144454829350912
    String sourceDirPath = AppConstant.CODE_OUTPUT_ROOT_DIR + File.separator + sourceDirName;

    // 6. 检查源目录是否存在
    File sourceDir = new File(sourceDirPath);
    if (!sourceDir.exists() || !sourceDir.isDirectory()) {
        throw new BusinessException(ErrorCode.SYSTEM_ERROR, "应用代码不存在，请先生成代码");
    }

    // 7. 复制文件到部署目录
    String deployDirPath = AppConstant.CODE_DEPLOY_ROOT_DIR + File.separator + deployKey;
    try {
        FileUtil.copyContent(sourceDir, new File(deployDirPath), true);
    } catch (Exception e) {
        throw new BusinessException(ErrorCode.SYSTEM_ERROR, "部署失败：" + e.getMessage());
    }

    // 8. 更新应用的 deployKey 和部署时间
    App updateApp = new App();
    updateApp.setId(appId);
    updateApp.setDeployKey(deployKey);
    updateApp.setDeployedTime(LocalDateTime.now());
    boolean updateResult = this.updateById(updateApp);
    ThrowUtils.throwIf(!updateResult, ErrorCode.OPERATION_ERROR, "更新应用部署信息失败");

    // 9. 返回可访问的 URL
    return String.format("%s/%s/", AppConstant.CODE_DEPLOY_HOST, deployKey);
}
```

------



4. Controller 接口【**用户部署应用供外界访问的入口**】

```Java
    /**
     * 应用部署
     *
     * @param appDeployRequest 部署请求
     * @param request          请求
     * @return 部署 URL
     */
    @PostMapping("/deploy")
    public BaseResponse<String> deployApp(@RequestBody AppDeployRequest appDeployRequest, HttpServletRequest request) {
        ThrowUtils.throwIf(appDeployRequest == null, ErrorCode.PARAMS_ERROR);
        Long appId = appDeployRequest.getAppId();
        ThrowUtils.throwIf(appId == null || appId <= 0, ErrorCode.PARAMS_ERROR, "应用 ID 不能为空");
        // 获取当前登录用户
        User loginUser = userService.getLoginUser(request);
        // 调用服务部署应用
        String deployUrl = appService.deployApp(appId, loginUser);
        return ResultUtils.success(deployUrl);
    }
```

------







# （扩展）应用版本管理化

在实际使用中，用户往往需要对生成的应用进行多次迭代和修改。可以引入版本化管理机制：

- 为每个应用**增加版本号字段**，每次 AI 生成新的代码时**自动递增版本号**
- 保存每个版本对应的代码文件，用户可以**随时回退到历史版本**
- **提供版本对比功能**，让用户清楚地看到不同版本之间的差异（类似下图的效果）

<img src="./AI零代码生成平台.assets/EvHoG8sdxCggHv3Q.webp" alt="img" style="zoom:33%;" />

------





## 1.**库表设计**

给应用主表 `app` 新增一个字段 `currentVersion`，用来表示 **当前这个应用正在使用哪个版本**。

```sql
ALTER TABLE app ADD COLUMN currentVersion INT DEFAULT 1 NOT NULL COMMENT '当前使用的版本号';
```

- **创建 app_version 表**

专门用来保存 **每个应用的每个历史版本信息**。

- 保留历史版本
- 支持回退
- 支持对比

```sql
CREATE TABLE app_version
(
    id            bigint auto_increment comment 'id' primary key,
    appId         bigint                             not null comment '应用ID',
    version       int                                not null comment '版本号',
    prompt        text                               null comment '该版本生成的初始 prompt',
    sourceDirPath varchar(512)                       not null comment '该版本的代码存储源路径',
    createTime    datetime default CURRENT_TIMESTAMP not null comment '创建时间',
    updateTime    datetime default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    isDelete      tinyint  default 0                 not null comment '是否删除',
    INDEX idx_appId_version (appId, version)
) comment '应用版本记录';
```

------





## 2.**DTO 类的对应**

```Java
// 这是“版本回退”接口的请求参数对象。
@Data
public class AppRollbackRequest implements Serializable {
    private Long appId;
    /** 目标回退版本号 */
    private Integer targetVersion;
    private static final long serialVersionUID = 1L;
}


  /**
  * appId = 1001
  * version = 2
  * relativeFilePath = src/App.vue
  * 后端收到后，就能去对应版本目录下找到 src/App.vue 读出来返回。
  */
// 这是 “读取指定版本文件内容” 接口的请求参数对象
@Data
public class AppFileContentRequest implements Serializable {
    private Long appId;
    private Integer version;
    /** 相对路径，例如 src/main/java/Main.java */
    private String relativeFilePath;
    private static final long serialVersionUID = 1L;
}
```

------





## 3.**核心业务类开发【修改 `chatToGenCode` 和 AI 对话核心方法】**

原本是将文件名的拼接方法直接放在底层的文件保存方法中的，没有版本号直接写死。现在**需要携带版本号，因此需要将拼接逻辑提取出来到 `chatToGenCode` 方法中**，核心代码：

```Java
		// 6. 计算新版本号
        //    例如当前是 v2，则本次生成的是 v3
        int nextVersion = (app.getCurrentVersion() == null ? 0 : app.getCurrentVersion()) + 1;

        // 7. 构建本次生成对应的“版本目录”
        //    示例：/tmp/code_output/multi_file_2038144454829350912_v3
        String sourceDirName = app.getCodeGenType() + "_" + appId + "_v" + nextVersion;
        String sourceDirPath = AppConstant.CODE_OUTPUT_ROOT_DIR + File.separator + sourceDirName;
```

------



- **注意：在此处制定好了版本目录，需要将 `sourceDirPath` 传下去给文件保存方法，因此需要修改如下：**

**1）`AiCodeGeneratorFacade.generateAndSaveCodeStream(...)`**

原来：

```java
public Flux<String> generateAndSaveCodeStream(String userMessage, CodeGenTypeEnum codeGenTypeEnum, Long appId)
```

改成：

```java
public Flux<String> generateAndSaveCodeStream(String userMessage,
                                              CodeGenTypeEnum codeGenTypeEnum,
                                              Long appId,
                                              String outputDirPath)
```

并在内部把：

```java
yield processCodeStream(codeStream, CodeGenTypeEnum.HTML, appId);
```

改成：

```java
yield processCodeStream(codeStream, CodeGenTypeEnum.HTML, appId, outputDirPath);
```

多文件同理。

------

**2）`processCodeStream(...)`**

原来：

```java
private Flux<String> processCodeStream(Flux<String> codeStream, CodeGenTypeEnum codeGenType, Long appId)
```

改成：

```java
private Flux<String> processCodeStream(Flux<String> codeStream,
                                       CodeGenTypeEnum codeGenType,
                                       Long appId,
                                       String outputDirPath)
```

然后把：

```java
File savedDir = CodeFileSaverExecutor.executeSaver(parsedResult, codeGenType, appId);
```

改成：

```java
File savedDir = CodeFileSaverExecutor.executeSaver(parsedResult, codeGenType, appId, outputDirPath);
```

------

**3）`CodeFileSaverExecutor.executeSaver(...)`**

原来：

```java
public static File executeSaver(Object codeResult, CodeGenTypeEnum codeGenType, Long appId)
```

改成：

```java
public static File executeSaver(Object codeResult,
                                CodeGenTypeEnum codeGenType,
                                Long appId,
                                String outputDirPath)
```

并把：

```java
case HTML -> htmlCodeFileSaver.saveCode((HtmlCodeResult) codeResult, appId);
case MULTI_FILE -> multiFileCodeFileSaver.saveCode((MultiFileCodeResult) codeResult, appId);
```

改成：

```java
case HTML -> htmlCodeFileSaver.saveCode((HtmlCodeResult) codeResult, appId, outputDirPath);
case MULTI_FILE -> multiFileCodeFileSaver.saveCode((MultiFileCodeResult) codeResult, appId, outputDirPath);
```

------

4）`CodeFileSaverTemplate`

你贴出来的底层模板里，现在目录仍然是：

```java
String uniqueDirName = StrUtil.format("{}_{}", codeType, appId);
```

- ***所以这里必须改成优先使用传入的 `outputDirPath`。***

示例：

```java
public final File saveCode(T result, Long appId, String outputDirPath) {
    validateInput(result);
    String baseDirPath = buildOutputDir(appId, outputDirPath);
    saveFiles(result, baseDirPath);
    return new File(baseDirPath);
}

protected final String buildOutputDir(Long appId, String outputDirPath) {
    if (appId == null) {
        throw new BusinessException(ErrorCode.PARAMS_ERROR, "应用 ID 不能为空");
    }
    if (StrUtil.isBlank(outputDirPath)) {
        throw new BusinessException(ErrorCode.PARAMS_ERROR, "输出目录不能为空");
    }
    FileUtil.mkdir(outputDirPath);
    return outputDirPath;
}
```

------



- **完整代码：**

```Java
@Override
public Flux<String> chatToGenCode(Long appId, String message, User loginUser) {
    // 1. 参数校验
    ThrowUtils.throwIf(appId == null, ErrorCode.PARAMS_ERROR, "应用ID不能为空");
    ThrowUtils.throwIf(StrUtil.isBlank(message), ErrorCode.PARAMS_ERROR, "提示词不能为空");
    ThrowUtils.throwIf(loginUser == null, ErrorCode.NOT_LOGIN_ERROR, "用户未登录");

    // 2. 查询应用
    App app = this.getById(appId);
    ThrowUtils.throwIf(app == null, ErrorCode.NOT_FOUND_ERROR, "应用不存在");

    // 3. 校验权限：只有应用创建者才可以生成代码
    if (!app.getUserId().equals(loginUser.getId())) {
        throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "无权限访问该应用");
    }

    // 4. 获取代码生成类型
    String codeGenTypeStr = app.getCodeGenType();
    CodeGenTypeEnum codeGenTypeEnum = CodeGenTypeEnum.getEnumByValue(codeGenTypeStr);
    if (codeGenTypeEnum == null) {
        throw new BusinessException(ErrorCode.SYSTEM_ERROR, "不支持的代码生成类型");
    }

    // 5. 先记录用户消息
    chatHistoryService.addChatMessage(
            appId,
            message,
            ChatHistoryMessageTypeEnum.USER.getValue(),
            loginUser.getId()
    );

    // 6. 计算新版本号
    //    例如当前是 v2，则本次生成的是 v3
    int nextVersion = (app.getCurrentVersion() == null ? 0 : app.getCurrentVersion()) + 1;

    // 7. 构建本次生成对应的“版本目录”
    //    示例：/tmp/code_output/multi_file_2038144454829350912_v3
    String sourceDirName = app.getCodeGenType() + "_" + appId + "_v" + nextVersion;
    String sourceDirPath = AppConstant.CODE_OUTPUT_ROOT_DIR + File.separator + sourceDirName;

    // 8. 如果目录已存在，先删除，避免脏数据
    File versionDir = new File(sourceDirPath);
    if (versionDir.exists()) {
        FileUtil.del(versionDir);
    }

    // 9. 调用 AI 核心能力：
    //    与老逻辑不同，这里必须把输出目录传下去，
    //    让底层把代码直接保存到“版本目录”，而不是固定目录
    Flux<String> contentFlux = aiCodeGeneratorFacade.generateAndSaveCodeStream(
            message,
            codeGenTypeEnum,
            appId,
            sourceDirPath
    );

    // 10. 收集 AI 的完整回复内容，流结束后写入聊天记录
    StringBuilder aiResponseBuilder = new StringBuilder();

    return contentFlux
            .map(chunk -> {
                // 10.1 实时收集 AI 返回内容
                aiResponseBuilder.append(chunk);
                // 10.2 同时继续把 chunk 返回前端，实现流式展示
                return chunk;
            })
            // 11. 只有当前面的 AI 流完整成功结束，才会执行这里
            .doOnComplete(() -> {
                String aiResponse = aiResponseBuilder.toString();

                // 11.1 保存 AI 回复到聊天记录
                if (StrUtil.isNotBlank(aiResponse)) {
                    chatHistoryService.addChatMessage(
                            appId,
                            aiResponse,
                            ChatHistoryMessageTypeEnum.AI.getValue(),
                            loginUser.getId()
                    );
                }

                // 11.2 再次确认版本目录确实生成成功
                File generatedDir = new File(sourceDirPath);
                ThrowUtils.throwIf(!generatedDir.exists() || !generatedDir.isDirectory(),
                        ErrorCode.SYSTEM_ERROR, "代码生成失败，版本目录不存在");

                // 11.3 保存版本记录，并更新 app.currentVersion
                saveVersionRecordAfterGenerate(appId, nextVersion, message, sourceDirPath);
            })
            .doOnError(error -> {
                // 12. 如果 AI 生成失败，记录错误消息
                String errorMessage = "AI回复失败: " + error.getMessage();
                chatHistoryService.addChatMessage(
                        appId,
                        errorMessage,
                        ChatHistoryMessageTypeEnum.AI.getValue(),
                        loginUser.getId()
                );

                // 13. 清理本次失败生成的目录，避免留下半成品
                File failedDir = new File(sourceDirPath);
                if (failedDir.exists()) {
                    FileUtil.del(failedDir);
                }
            });
}
```

------





## 4.AI 代码生成成功后，保存版本记录并更新 app 主表当前版本号

```Java
/**
 * AI 代码生成成功后，保存版本记录并更新 app 主表当前版本号
 * <p>
 * 注意：
 * 1. 这里只做“版本落库”
 * 2. 不再负责调用 AI
 * 3. 该方法由 chatToGenCode() 在流式生成成功后调用
 */
private void saveVersionRecordAfterGenerate(Long appId, Integer nextVersion, String prompt, String sourceDirPath) {
    transactionTemplate.executeWithoutResult(status -> {
        // 1. 保存一条新版本记录到 app_version 表
        AppVersion appVersion = new AppVersion();
        appVersion.setAppId(appId);
        appVersion.setVersion(nextVersion);
        appVersion.setPrompt(prompt);
        appVersion.setSourceDirPath(sourceDirPath);

        boolean saveResult = appVersionService.save(appVersion);
        ThrowUtils.throwIf(!saveResult, ErrorCode.OPERATION_ERROR, "保存应用版本记录失败");

        // 2. 更新 app 表当前版本号
        App updateApp = new App();
        updateApp.setId(appId);
        updateApp.setCurrentVersion(nextVersion);

        boolean updateResult = this.updateById(updateApp);
        ThrowUtils.throwIf(!updateResult, ErrorCode.OPERATION_ERROR, "更新应用当前版本号失败");
    });
}
```

------





## 5.把当前应用的 currentVersion 切换到某个历史版本，实现“版本回退”

```Java
/**
 * 把当前应用的 currentVersion 切换到某个历史版本，实现“版本回退”
 * <p>
 * 注意：
 * 这里只是改“当前指向的版本号”
 * 真正让线上生效，仍然需要重新调用 deployApp()
 */
@Override
public Boolean rollbackVersion(Long appId, Integer targetVersion, User loginUser) {
    App app = this.getById(appId);
    ThrowUtils.throwIf(app == null, ErrorCode.NOT_FOUND_ERROR, "应用不存在");
    if (!app.getUserId().equals(loginUser.getId())) {
        throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
    }

    QueryWrapper queryWrapper = QueryWrapper.create()
            .eq("appId", appId)
            .eq("version", targetVersion)
            .eq("isDelete", 0);

    long count = appVersionService.count(queryWrapper);
    if (count <= 0) {
        throw new BusinessException(ErrorCode.PARAMS_ERROR, "目标回退版本不存在");
    }

    // 【关键注释 7：轻量级回退机制】
    // 回退操作不需要移动任何物理文件，只需要把 app 主表的 currentVersion 字段修改为目标版本号即可。
    // 等用户下次点击“部署”时，deployApp 方法会自动读取这个旧版本目录里的代码覆盖到线上。
    App updateApp = new App();
    updateApp.setId(appId);
    updateApp.setCurrentVersion(targetVersion);
    return this.updateById(updateApp);
}
```

------





## 6.读取某个应用某个版本下某个文件的代码内容

relativeFilePath 代表文件内容：

```
/tmp/code_output/multi_file_123_v2/src/App.vue
/tmp/code_output/multi_file_123_v2/src/main.js
/tmp/code_output/multi_file_123_v2/package.json
```

- **那么这几个文件对应的 `relativeFilePath` 分别就是：**

```
src/App.vue
src/main.js
package.json
```

------

**代码：**

```Java
/**
 * 读取某个应用某个版本下某个文件的代码内容，供前端做 Diff 对比
 */
@Override
public String getVersionFileContent(Long appId, Integer version, String relativeFilePath, User loginUser) {
    // 1. 防止路径穿越攻击，例如 ../../../../etc/passwd
    if (StrUtil.isBlank(relativeFilePath) || relativeFilePath.contains("..")) {
        throw new BusinessException(ErrorCode.PARAMS_ERROR, "非法的文件路径");
    }

    // 2. 应用必须存在
    App app = this.getById(appId);
    ThrowUtils.throwIf(app == null, ErrorCode.NOT_FOUND_ERROR, "应用不存在");

    // 3. 当前用户必须有权查看代码
    if (!app.getUserId().equals(loginUser.getId())) {
        throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
    }

    // 4. 查询指定版本记录
    QueryWrapper queryWrapper = QueryWrapper.create()
            .eq("appId", appId)
            .eq("version", version)
            .eq("isDelete", 0);

    AppVersion appVersion = appVersionService.getOne(queryWrapper);
    ThrowUtils.throwIf(appVersion == null, ErrorCode.NOT_FOUND_ERROR, "指定版本的代码记录不存在");

    // 5. 拼接绝对路径
    String absolutePath = appVersion.getSourceDirPath() + File.separator + relativeFilePath;
    File file = new File(absolutePath);

    // 6. 文件必须存在，且不能是目录
    if (!file.exists() || file.isDirectory()) {
        throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "文件不存在: " + relativeFilePath);
    }

    // 7. 返回文件内容给前端
    return FileUtil.readUtf8String(file);
}
```

------





## 7.应用部署deployApp

将应用生成目录 code_output 中的文件放到应用部署目录 code_deploy 中

- ***修改点：原本写死的 `code_output 中的文件` 的路径，现在需要从 `应用版本历史表` 中 查询 `sourceDirPath` 字段得到带有版本的路径，然后再进行复制。***

```Java
@Override
public String deployApp(Long appId, User loginUser) {
    // 1. 参数校验
    ThrowUtils.throwIf(appId == null || appId <= 0, ErrorCode.PARAMS_ERROR, "应用 ID 不能为空");
    ThrowUtils.throwIf(loginUser == null, ErrorCode.NOT_LOGIN_ERROR, "用户未登录");

    // 2. 查询应用
    App app = this.getById(appId);
    ThrowUtils.throwIf(app == null, ErrorCode.NOT_FOUND_ERROR, "应用不存在");

    // 3. 只有应用创建者可以部署
    if (!app.getUserId().equals(loginUser.getId())) {
        throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "无权限部署该应用");
    }

    // 4. 应用必须已经存在可部署版本
    ThrowUtils.throwIf(app.getCurrentVersion() == null || app.getCurrentVersion() <= 0,
            ErrorCode.SYSTEM_ERROR, "当前应用还没有可部署的版本");

    // 5. 检查 deployKey，若为空则生成
    String deployKey = app.getDeployKey();
    if (StrUtil.isBlank(deployKey)) {
        deployKey = RandomUtil.randomString(6);
    }

    // 6. 根据 currentVersion 动态查询当前版本记录
    QueryWrapper queryWrapper = QueryWrapper.create()
            .eq("appId", appId)
            .eq("version", app.getCurrentVersion())
            .eq("isDelete", 0);

    AppVersion currentVersionInfo = appVersionService.getOne(queryWrapper);
    ThrowUtils.throwIf(currentVersionInfo == null, ErrorCode.SYSTEM_ERROR, "找不到当前版本的代码记录");

    // 7. 检查源目录是否存在
    String sourceDirPath = currentVersionInfo.getSourceDirPath();
    File sourceDir = new File(sourceDirPath);
    if (!sourceDir.exists() || !sourceDir.isDirectory()) {
        throw new BusinessException(ErrorCode.SYSTEM_ERROR, "应用代码不存在，请先生成代码");
    }

    // 8. 部署目录：先清空旧目录，再复制当前版本代码
    String deployDirPath = AppConstant.CODE_DEPLOY_ROOT_DIR + File.separator + deployKey;
    try {
        File deployDir = new File(deployDirPath);
        // 防止老目录中还残留有旧文件，影响最终项目上线
        if (deployDir.exists()) {
            FileUtil.clean(deployDir);
        } else {
            FileUtil.mkdir(deployDir);
        }
        FileUtil.copyContent(sourceDir, deployDir, true);
    } catch (Exception e) {
        throw new BusinessException(ErrorCode.SYSTEM_ERROR, "部署失败：" + e.getMessage());
    }

    // 9. 更新部署信息
    App updateApp = new App();
    updateApp.setId(appId);
    updateApp.setDeployKey(deployKey);
    updateApp.setDeployedTime(LocalDateTime.now());

    boolean updateResult = this.updateById(updateApp);
    ThrowUtils.throwIf(!updateResult, ErrorCode.OPERATION_ERROR, "更新应用部署信息失败");

    // 10. 返回访问地址
    return String.format("%s/%s/", AppConstant.CODE_DEPLOY_HOST, deployKey);
}
```

------





## 8.在 APPController 层加上接口

添加上 `版本回退` 和 `读取指定版本的文件内容` 两个接口。

```Java
    /**
     * 应用版本回退
     */
    @PostMapping("/rollback")
    public BaseResponse<Boolean> rollbackAppVersion(@RequestBody AppRollbackRequest request, HttpServletRequest httpServletRequest) {
        ThrowUtils.throwIf(request == null || request.getAppId() == null || request.getTargetVersion() == null, ErrorCode.PARAMS_ERROR);
        User loginUser = userService.getLoginUser(httpServletRequest);

        Boolean result = appService.rollbackVersion(request.getAppId(), request.getTargetVersion(), loginUser);
        return ResultUtils.success(result);
    }


    /**
     * 获取指定版本的文件内容（用于前端 Diff 对比）
     */
    @PostMapping("/file/content")
    public BaseResponse<String> getFileContent(@RequestBody AppFileContentRequest request, HttpServletRequest httpServletRequest) {
        ThrowUtils.throwIf(request == null || request.getAppId() == null || request.getVersion() == null, ErrorCode.PARAMS_ERROR);
        User loginUser = userService.getLoginUser(httpServletRequest);

        String content = appService.getVersionFileContent(request.getAppId(), request.getVersion(), request.getRelativeFilePath(), loginUser);
        return ResultUtils.success(content);
    }
```

------











# 对话历史模块

**每次对话都是独立的，AI 无法记住之前的交互内容。**这导致用户无法基于已生成的网站进行迭代改进，极大地限制了平台的实用性。

***需要的实现：***

1. **对话历史的⁠⁠⁠持久化存储**：用户发送消息时，需要保存用户消息；AI 成功﻿﻿﻿回复后，需要保存 AI 消⁢息⁢⁢。即使 AI 回复失败，也要‍‍‍记录错误信息，确保对话的完整性。

2. **应用级⁠⁠⁠别的数据隔离**：每个应用的对话历史都是独﻿﻿﻿立的。删除应用时，需要⁢⁢⁢关联删除该应用的所有‍‍‍对话历史，避免数据冗余。

3. **对话历史查询**：支持分页查看某个应用的对话历史，需要区分用户和 AI 消息。类似聊天软件的消息加载机制，每次加载最新 10 条消息，支持 **向前加载** 更多历史记录。（仅应用创建者和管理员可见）
   - 详细来说，进入应用页面⁠⁠时，**⁠前端根据应用 id 先加载一次对话历史消息，关联查询最新 10 条消息**。如果存在﻿﻿历史对话，直接展示；如果没有﻿历史记录，才自⁢⁢动发送初始化提示词。这样就解决了之前浏览⁢别‍‍人的应用时意外触发对话的问题

4. **管理对⁠⁠⁠话历史**：***管理员可以查看所有应﻿用的﻿对话﻿历史***，⁢按照时⁢间降序⁢排‍序，便于内‍容监管‍。

------







## 游标分页查询

***传统分页查询的问题：***

假设用户会持⁠⁠⁠续收到新的消息。如果按照传统分页基于偏移量加载，第一页已经加载了第 1 - 5 行的﻿﻿﻿数据，本来要查询的第二页数据是第 6 - ⁢⁢⁢10 行（对应的 SQL 语句为 `limi‍‍‍t 5, 5`），数据库记录如下：

<img src="./AI零代码生成平台.assets/zN3TYdqHWgrbdRz8.webp" alt="img" style="zoom: 33%;" />

结果在查询⁠⁠⁠第二页前，突然用户又收到了 5 条新消息﻿﻿﻿，数据库记录就变成了下⁢⁢⁢面这样。**原本的第一‍‍‍页，变成了当前的第二页！**

<img src="./AI零代码生成平台.assets/rrqWGqDgVtWSnq98.webp" alt="img" style="zoom:33%;" />

------





***游标分页：***

使用一个游标来跟踪分页位置，而不是基于页码，**每次请求从上一次请求的游标开始加载数据。**

<img src="./AI零代码生成平台.assets/3KvKIudvl54FFHAz.webp" alt="img" style="zoom:33%;" />

当要加载下一⁠⁠⁠页时，**前端携带游标值发起查询，后端操作数据库从 ﻿﻿﻿id 小于当前游标值的数⁢⁢⁢据开始查询**，这样查询结果‍‍‍就不会受到新增数据的影响。

<img src="./AI零代码生成平台.assets/fuBKjDoKJm3BO4K2.webp" alt="img" style="zoom:33%;" />

------



标准实践建议优先使用 id 作为游⁠⁠⁠标，因为主键性能最优且不重复。但针对我们的场景，**按时间排序是核心需求**，而且**同一个 `appId` 下时间重复的可能性极低，所以直接使用对话历﻿﻿﻿史的创建时间 `createTime` 作为游标**是完全可行的。不需要额外⁢⁢⁢带上对话历史的 id 作为复合游标，简化了游标查询的逻辑

- **示例 SQL 如下：**

```sql
SELECT * FROM chat_history 
WHERE appId = 123 AND createTime < '2025-07-29 10:30:00'
ORDER BY createTime DESC 
LIMIT 10;
```

------







## 对话历史表

```sql
-- 对话历史表
create table chat_history
(
    id          bigint auto_increment comment 'id' primary key,
    message     text                               not null comment '消息',
    messageType varchar(32)                        not null comment 'user/ai',
    appId       bigint                             not null comment '应用id',
    userId      bigint                             not null comment '创建用户id',
    createTime  datetime default CURRENT_TIMESTAMP not null comment '创建时间',
    updateTime  datetime default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    isDelete    tinyint  default 0                 not null comment '是否删除',
    INDEX idx_appId (appId),                       -- 提升基于应用的查询性能
    INDEX idx_createTime (createTime),             -- 提升基于时间的查询性能
    INDEX idx_appId_createTime (appId, createTime) -- 游标查询核心索引
) comment '对话历史' collate = utf8mb4_unicode_ci;
```

------







## 游标查询实现

1. **创建包含游标字段的请求对象**

```Java
@EqualsAndHashCode(callSuper = true)
@Data
public class ChatHistoryQueryRequest extends PageRequest implements Serializable {

    /**
     * id
     */
    private Long id;

    /**
     * 消息内容
     */
    private String message;

    /**
     * 消息类型（user/ai）
     */
    private String messageType;

    /**
     * 应用id
     */
    private Long appId;

    /**
     * 创建用户id
     */
    private Long userId;

    /**
     * 游标查询 - 最后一条记录的创建时间
     * 用于分页查询，获取早于此时间的记录
     */
    private LocalDateTime lastCreateTime;

    private static final long serialVersionUID = 1L;
}
```

------



2. **构建 `queryWrapper` 查询条件**

```java 
// 游标查询的特殊逻辑

// 游标查询逻辑 - 只使用 createTime 作为游标
if (lastCreateTime != null) {
        queryWrapper.lt("createTime", lastCreateTime);
    }
    // 排序
    if (StrUtil.isNotBlank(sortField)) {
        queryWrapper.orderBy(sortField, "ascend".equals(sortOrder));
    } else {
        // 默认按创建时间降序排列
        queryWrapper.orderBy("createTime", false);
    }
```

------



3. 游标分页查询的核心逻辑

```Java
@Override
public Page<ChatHistory> listAppChatHistoryByPage(Long appId, int pageSize,
                                                  LocalDateTime lastCreateTime,
                                                  User loginUser) {

    ThrowUtils.throwIf(appId == null || appId <= 0, ErrorCode.PARAMS_ERROR, "应用ID不能为空");
    ThrowUtils.throwIf(pageSize <= 0 || pageSize > 50, ErrorCode.PARAMS_ERROR, "页面大小必须在1-50之间");
    ThrowUtils.throwIf(loginUser == null, ErrorCode.NOT_LOGIN_ERROR);

    // 验证权限：只有应用创建者和管理员可以查看
    App app = appService.getById(appId);
    ThrowUtils.throwIf(app == null, ErrorCode.NOT_FOUND_ERROR, "应用不存在");
    boolean isAdmin = UserConstant.ADMIN_ROLE.equals(loginUser.getUserRole());
    boolean isCreator = app.getUserId().equals(loginUser.getId());
    ThrowUtils.throwIf(!isAdmin && !isCreator, ErrorCode.NO_AUTH_ERROR, "无权查看该应用的对话历史");

    // 构建查询条件
    ChatHistoryQueryRequest queryRequest = new ChatHistoryQueryRequest();
    queryRequest.setAppId(appId);
    queryRequest.setLastCreateTime(lastCreateTime);
    QueryWrapper queryWrapper = this.getQueryWrapper(queryRequest);

    // 查询数据
    return this.page(Page.of(1, pageSize), queryWrapper);
}
```

------



- **最后的 `this.page(Page.of(1, pageSize), queryWrapper);` 解释：**

传统分页通常是这样：

```Java
// 查第 3 页
// 每页 10 条
// SQL 会带上 offset
this.page(Page.of(3, 10), queryWrapper);
```

现在的写法：

- **永远不使用 offset 去翻到第 2 页、第 3 页**
- 每次都只取“当前条件下的前 `pageSize` 条”

**而当前条件是什么？**

就是 `queryWrapper` 里的条件：

- `appId = appId`
- 可能还有 `createTime < lastCreateTime`

------







## *对话记忆！！

目前我们⁠⁠⁠的 AI 对话会断片儿，无法记住之前的对话内﻿﻿﻿容，**每次修改实际上都是⁢⁢⁢重新生成完整的网站，而‍‍‍不是在原有基础上进行修改**。

- ***`LangC⁠⁠⁠hain4j` 不仅提供了对话记忆﻿﻿能力﻿，而且还能结⁢⁢合 `R⁢edis` ‍‍持久化对‍话记忆***

------

1. **为什么不直接用内存来存储会话记忆？**

首先是重启⁠⁠⁠后会丢失记忆；其次如果每个应用都﻿﻿在内﻿存中维护对话⁢⁢历史，⁢很***容易出现‍‍ OO‍M*** 问题.

2. **为什么不用 MySQL 来存储会话记忆？**

一方面是因为 ***`R⁠⁠⁠edis` 作为内存数据库，在读写对话记忆时性能更高***；另一方面是数﻿﻿﻿据库中的对话历史表包含其他业务字⁢⁢⁢段，不适合直接交给 `LangCh‍‍‍ain4j` 的对话记忆组件管理。

------

- **方案流程：**

方案很简单，之⁠⁠⁠前我们已经在数据库中保存了用户和 AI 的消息，只需要 **在﻿﻿﻿初始化会话记忆时，加载最新的⁢⁢⁢对话记录到 Redis 中**，‍‍‍就能确保 AI 了解交互历史。

流程：**AI⁠⁠⁠ 对话 => 从数据库中加载对话历史﻿﻿﻿到 Redis =⁢⁢⁢> Redis 为‍‍‍ AI 提供对话记忆**

------







### 1.**引入依赖**

【这个依赖会⁠⁠⁠引入 Redis 的 Jedis﻿﻿ 客﻿户端，以及与⁢⁢ La⁢ngCha‍‍in4j 的‍整合组件。】

```xml
<dependency>
  <groupId>dev.langchain4j</groupId>
  <artifactId>langchain4j-community-redis-spring-boot-starter</artifactId>
  <version>1.1.0-beta7</version>
</dependency>
```

------







### 2.**配置 Redis 连接信息**

```yml
spring:
  # redis
  data:
    redis:
      host: 192.168.44.128
      port: 6379
      password:
      # 这里的 ttl 是指定设置到 Redis 中的 key 的过期时间【指定为一小时】
      ttl: 3600
```

------







### 3.**配置 Redis 为 LangChain4j 提供存储能力的配置类**

```java 
@Configuration
@ConfigurationProperties(prefix = "spring.data.redis")
@Data
public class RedisChatMemoryStoreConfig {

    private String host;

    private int port;

    private String password;

    private long ttl;

    @Bean
    public RedisChatMemoryStore redisChatMemoryStore() {
        return RedisChatMemoryStore.builder()
                .host(host)
                .port(port)
                .password(password)
                .ttl(ttl)
                .build();
    }
}
```

- ***注意：需要排除掉 Redis 的向量存储功能，在启动⁠⁠⁠类中排除 embedding﻿ 的﻿自动﻿装配，不然会报错***

```java
@SpringBootApplication(exclude = {RedisEmbeddingStoreAutoConfiguration.class})
```

------







### 4.**使用 LangChain4j 内置的对话记忆功能**

不同 `ap⁠⁠⁠pId` 的对话记忆是独立隔离的，可以**给 AI 服务方法增加 `memoryId` 注解和参数，然后通过 `chatMemoryProvider` 为每个 `appId` 分配对话记忆。**

- ***在之前的调用 AI 接口中，新增一个 `memoryId` 参数：***

```Java
interface AiCodeGeneratorService  {
    HtmlCodeResult generateHtmlCode(@MemoryId int memoryId, @UserMessage String userMessage);
}
```

- ***在 AiCodeGeneratorServiceFactory 类中添加对话记忆功能配置：***

```Java
private final RedisChatMemoryStore redisChatMemoryStore;

@Bean
public AiCodeGeneratorService aiCodeGeneratorService() {
    return AiServices.builder(AiCodeGeneratorService.class)
            .chatModel(chatModel)
            .streamingChatModel(streamingChatModel)
            // 根据 id 构建独立的对话记忆
            .chatMemoryProvider(memoryId -> MessageWindowChatMemory
                    .builder()
                    .id(memoryId)
                    .chatMemoryStore(redisChatMemoryStore)
                    .maxMessages(20)
                    .build())
            .build();
}
```

------



- ***方案2：将每个 AI Service 隔离***

给每﻿﻿﻿个应用分配一个专属的 AI Se⁢⁢⁢rvice，每个 AI Serv‍‍‍ice 绑定独立的对话记忆。

```Java
@Configuration
public class AiCodeGeneratorServiceFactory {

    @Resource
    private ChatModel chatModel;

    @Resource
    private StreamingChatModel streamingChatModel;

    @Resource
    private RedisChatMemoryStore redisChatMemoryStore;

    /**
     * 根据 appId 获取服务
     */
    public AiCodeGeneratorService getAiCodeGeneratorService(long appId) {
        // 根据 appId 构建独立的对话记忆
        MessageWindowChatMemory chatMemory = MessageWindowChatMemory
                .builder()
                .id(appId)
                .chatMemoryStore(redisChatMemoryStore)
                .maxMessages(20)
                .build();
        return AiServices.builder(AiCodeGeneratorService.class)
                .chatModel(chatModel)
                .streamingChatModel(streamingChatModel)
                .chatMemory(chatMemory)
                .build();
    }
}
```

------







### 5.本地缓存优化

每次构造完 appId⁠⁠⁠ 对应的 AI 服务实例后，**利用 Caffeine 缓存来存储，之后相同 appId﻿﻿﻿ 就能直接获取到 AI 服务实例**，避免重⁢⁢⁢复构造。

注意：本地缓存占用的是内存，所以必须‍‍‍**设置合理的过期策略**防止内存泄漏。

------

- **引入 Caffieine 依赖：**

```xml
<dependency>
    <groupId>com.github.ben-manes.caffeine</groupId>
    <artifactId>caffeine</artifactId>
</dependency>
```



- **修改 AI 配置工厂，增加本地缓存逻辑**

```Java
/**
 * AI 服务实例缓存
 * 缓存策略：
 * - 最大缓存 1000 个实例
 * - 写入后 30 分钟过期
 * - 访问后 10 分钟过期
 */
private final Cache<Long, AiCodeGeneratorService> serviceCache = Caffeine.newBuilder()
        .maximumSize(1000)
        .expireAfterWrite(Duration.ofMinutes(30))
        .expireAfterAccess(Duration.ofMinutes(10))
        .removalListener((key, value, cause) -> {
            log.debug("AI 服务实例被移除，appId: {}, 原因: {}", key, cause);
        })
        .build();


/**
 * 根据 appId 获取 AIService 服务（带缓存）
 */
public AiCodeGeneratorService getAiCodeGeneratorService(long appId) {
    // 如果无法根据 appId 获取到 AIService，就调用 create 方法创建一个
    return serviceCache.get(appId, this::createAiCodeGeneratorService);
}


/**
 * 创建新的 AI 服务实例
 */
private AiCodeGeneratorService createAiCodeGeneratorService(long appId) {
    log.info("为 appId: {} 创建新的 AI 服务实例", appId);
    // 根据 appId 构建独立的对话记忆
    MessageWindowChatMemory chatMemory = MessageWindowChatMemory
            .builder()
            .id(appId)
            .chatMemoryStore(redisChatMemoryStore)
            .maxMessages(20)
            .build();
    return AiServices.builder(AiCodeGeneratorService.class)
            .chatModel(chatModel)
            .streamingChatModel(streamingChatModel)
            .chatMemory(chatMemory)
            .build();
}
```

------



- **最后修改 `AiCodeGeneratorFacade` 门面类，所有方法使用的 AI Service 改为通过工厂根据 `appId` 获取 AI Service：**

```java 
@Resource
private AiCodeGeneratorServiceFactory aiCodeGeneratorServiceFactory;

// 根据 appId 获取对应的 AI 服务实例
AiCodeGeneratorService aiCodeGeneratorService = aiCodeGeneratorServiceFactory.getAiCodeGeneratorService(appId);
```

------



***按照方案2的设计之后，用户就无需再单独提供 appId 了。***

------







### 6.历史对话加载

- 首先，根据 `appId` 查询该应用的历史对话记录。查询时按创建时间倒序排列，也就是 **最新的消息排在最前面**。同时通过 ***`limit(1, maxCount)` 跳过第一条最新消息***，再取后面的若干条记录。这样做通常是为了 **排除当前这次刚发送的用户消息**，避免这条消息既作为当前输入传给模型，又在历史记录里再加载一次，造成重复。
- 查出数据后，先判断结果是否为空。如果没有历史记录，就直接返回 `0`，表示没有加载任何上下文。
- 如果查到了历史记录，由于数据库中取出来的顺序是 **“新的在前、旧的在后”**，而 AI 记忆需要按正常对话顺序加载，也就是“旧的在前、新的在后”，所以这里会先**把列表反转**。
- 接着，为了防止之前的上下文残留导致重复，会先调用 `chatMemory.clear()`，把当前记忆窗口清空。
- 然后开始遍历历史消息。对于每一条记录，先判断它的消息类型：
  - **如果是用户消息，就转换成 `UserMessage` 放入 `chatMemory`；**
  - **如果是 AI 消息，就转换成 `AiMessage` 放入 `chatMemory`。**

​		这样就能把数据库中的聊天记录恢复成大模型可以识别的上下文消息对象。

- 在加载过程中，还会**统计成功加入记忆的消息数量**。
- 全部加载完成后，打印日志，说明当前应用成功加载了多少条历史对话，并把这个数量返回。

------

**代码：**

```Java
/**
 * 在用户发起一次新的 AI 对话时，把数据库里的历史消息取出来，装进大模型的记忆窗口 chatMemory 中
 * @param appId       加载哪个 appId 的应用
 * @param chatMemory  往哪个会话记忆中去保存
 * @param maxCount
 * @return
 */
@Override
public int loadChatHistoryToMemory(Long appId, MessageWindowChatMemory chatMemory, int maxCount) {
    try {
        // 1. 查询指定应用的历史对话记录
        // - 按 createTime 倒序查询：最新的数据排在最前面
        // - limit(1, maxCount)：
        //   表示跳过第 1 条记录，再取 maxCount 条
        //   这里这样做的目的，是排除 “当前最新的一条用户消息”，避免它被重复加入上下文
        QueryWrapper queryWrapper = QueryWrapper.create()
                .eq(ChatHistory::getAppId, appId)
                .orderBy(ChatHistory::getCreateTime, false)
                .limit(1, maxCount);

        // 执行查询，获取历史记录列表
        List<ChatHistory> historyList = this.list(queryWrapper);

        // 2. 如果没有历史记录，直接返回 0
        if (CollUtil.isEmpty(historyList)) {
            return 0;
        }

        // 3. 由于前面查询时是按时间倒序取出的（新的在前，老的在后），
        //    这里需要反转列表，变成时间正序（老的在前，新的在后），
        //    这样才能按照正常对话顺序加载到 chatMemory 中
        historyList = historyList.reversed();

        // 记录成功加载到记忆中的消息条数
        int loadedCount = 0;

        // 4. 先清空当前记忆窗口中的历史内容，防止重复加载，避免上下文里出现重复消息
        chatMemory.clear();

        // 5. 按顺序遍历历史记录，并 根据消息类型 恢复成 对应的大模型消息 对象
        // 分别加载 用户消息类型 和 大模型回复消息类型
        for (ChatHistory history : historyList) {
            // 如果是用户消息，则转换成 UserMessage 加入记忆
            if (ChatHistoryMessageTypeEnum.USER.getValue().equals(history.getMessageType())) {
                chatMemory.add(UserMessage.from(history.getMessage()));
                loadedCount++;
            }
            // 如果是 AI 回复，则转换成 AiMessage 加入记忆
            else if (ChatHistoryMessageTypeEnum.AI.getValue().equals(history.getMessageType())) {
                chatMemory.add(AiMessage.from(history.getMessage()));
                loadedCount++;
            }
        }

        // 6. 记录日志，说明本次为当前 app 加载了多少条历史消息
        log.info("成功为 appId: {} 加载了 {} 条历史对话", appId, loadedCount);

        // 7. 返回实际加载的消息数量
        return loadedCount;
    } catch (Exception e) {
        log.error("加载历史对话失败，appId: {}, error: {}", appId, e.getMessage(), e);
        return 0;
    }
}
```

------



- **然后就可以⁠⁠⁠在初始化 AI Service 的对话记﻿﻿﻿忆时调用了，这相当于⁢⁢⁢是懒加载，对话时才会‍‍‍加载记忆，节约内存**

```Java
private AiCodeGeneratorService createAiCodeGeneratorService(long appId) {
    log.info("为 appId: {} 创建新的 AI 服务实例", appId);
    // 根据 appId 构建独立的对话记忆
    MessageWindowChatMemory chatMemory = MessageWindowChatMemory
            .builder()
            .id(appId)
            .chatMemoryStore(redisChatMemoryStore)
            .maxMessages(20)
            .build();
    
    //===============================================================================
    // 从数据库加载历史对话到记忆中
    chatHistoryService.loadChatHistoryToMemory(appId, chatMemory, 20);
    
    return AiServices.builder(AiCodeGeneratorService.class)
            .chatModel(chatModel)
            .streamingChatModel(streamingChatModel)
            .chatMemory(chatMemory)
            .build();
}
```

------







### 7.Redis 分布式 Session

既然已经整合了 ⁠⁠⁠Redis，我们可以顺便优化一下用户登录态的管理。之前每次重启服﻿﻿﻿务器都需要重新登录，现在可以**使用⁢⁢⁢ Redis 管理 `Sessio‍‍‍n` 登录态，实现分布式会话管理**。

------

- **引入依赖：**

```xml
<!-- Spring Session + Redis -->
<dependency>
    <groupId>org.springframework.session</groupId>
    <artifactId>spring-session-data-redis</artifactId>
</dependency>
```

------

- **修改 `application.yml` 配置文件，更改 Session 的存储方式和过期时间：**

```yml
spring: 
  # session 配置
  session:
    store-type: redis
    # session 30 天过期
    timeout: 2592000
server:
  port: 8123
  servlet:
    context-path: /api
    # cookie 30 天过期
    session:
      cookie:
        max-age: 2592000
```

------









# 生成前端工程化项目

为了生成不单单只有 `html, css, js` 文件的项目，而是类似有 vue 框架的完整工程化项目，就需要让 AI 有自主调用工具的能力

<img src="./AI零代码生成平台.assets/tKBGxmFh6btl9Ely.webp" alt="img" style="zoom:50%;" />

------







## 系统提示词设计

1. 建议**尽量避免让项目引入额外的依赖**，比如 `TailWindCSS` 样式库等，会增加不确定性，可能生成的项目都无法运行，所以此处我们选择原生 CSS。
2. **限制输出长度和文件数**很关键，这是为了防止 AI 理想太丰满导致输出的内容不完整，可以根据需要自己调整。
3. 为了支持后续通过子路径浏览和部署网站（比如 `localhost/{deployKey}/`），必须**配置 `Vite` 的 base 路径和路由 hash 模式**。

```
你是一位资深的 Vue3 前端架构师，精通现代前端工程化开发、组合式 API、组件化设计和企业级应用架构。

你的任务是根据用户提供的项目描述，创建一个完整的、可运行的 Vue3 工程项目

## 核心技术栈

- Vue 3.x（组合式 API）
- Vite
- Vue Router 4.x
- Node.js 18+ 兼容

## 项目结构

项目根目录/
├── index.html                 # 入口 HTML 文件
├── package.json              # 项目依赖和脚本
├── vite.config.js           # Vite 配置文件
├── src/
│   ├── main.js             # 应用入口文件
│   ├── App.vue             # 根组件
│   ├── router/
│   │   └── index.js        # 路由配置
│   ├── components/				 # 组件
│   ├── pages/             # 页面
│   ├── utils/             # 工具函数（如果需要）
│   ├── assets/            # 静态资源（如果需要）
│   └── styles/            # 样式文件
└── public/                # 公共静态资源（如果需要）

## 开发约束

1）组件设计：严格遵循单一职责原则，组件具有良好的可复用性和可维护性
2）API 风格：优先使用 Composition API，合理使用 `<script setup>` 语法糖
3）样式规范：使用原生 CSS 实现响应式设计，支持桌面端、平板端、移动端的响应式适配
4）代码质量：代码简洁易读，避免过度注释，优先保证功能完整和样式美观
5）禁止使用任何状态管理库、类型校验库、代码格式化库
6）将可运行作为项目生成的第一要义，尽量用最简单的方式满足需求，避免使用复杂的技术或代码逻辑

## 参考配置

1）vite.config.js 必须配置 base 路径以支持子路径部署、需要支持通过 @ 引入文件、不要配置端口号


import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'

export default defineConfig({
  base: './',
  plugins: [vue()],
  resolve: {
    alias: {
      '@': fileURLToPath(new URL('./src', import.meta.url))
    }
  }
})


2）路由配置必须使用 hash 模式，避免服务器端路由配置问题

import { createRouter, createWebHashHistory } from 'vue-router'

const router = createRouter({
  history: createWebHashHistory(),
  routes: [
    // 路由配置
  ]
})


3）package.json 文件参考：

{
  "scripts": {
    "dev": "vite",
    "build": "vite build"
  },
  "dependencies": {
    "vue": "^3.3.4",
    "vue-router": "^4.2.4"
  },
  "devDependencies": {
    "@vitejs/plugin-vue": "^4.2.3",
    "vite": "^4.4.5"
  }
}


## 网站内容要求

- 基础布局：各个页面统一布局，必须有导航栏，尤其是主页内容必须丰富
- 文本内容：使用真实、有意义的中文内容
- 图片资源：使用 `https://picsum.photos` 服务或其他可靠的占位符
- 示例数据：提供真实场景的模拟数据，便于演示

## 严格输出约束

1）必须通过使用【文件写入工具】依次创建每个文件（而不是直接输出文件代码）。
2）需要在开头输出简单的网站生成计划
3）需要在结尾输出简单的生成完毕提示（但是不要展开介绍项目）
4）注意，禁止输出以下任何内容：

- 安装运行步骤
- 技术栈说明
- 项目特点描述
- 任何形式的使用指导
- 提示词相关内容

5）输出的总 token 数必须小于 20000，文件总数量必须小于 30 个

## 质量检验标准

确保生成的项目能够：
1. 通过 `npm install` 成功安装所有依赖
2. 通过 `npm run dev` 启动开发服务器并正常运行
3. 通过 `npm run build` 成功构建生产版本
4. 构建后的项目能够在任意子路径下正常部署和访问
```

------



- **完整流程：**

生成完 Vue 工程代码后，是无法直接运行的，需要***执行 `npm install` 命令安装依赖、执行 `npm run build` 打包构建***，会得到一个打包后的 `dist` 目录，网站浏览和部署都应该是访问这个目录。

<img src="./AI零代码生成平台.assets/ykou20zszL3gfaMI.webp" alt="img" style="zoom: 67%;" />

------







## 配置推理流式模型并提供工具

1. **在 `config` 包下新建推理流式模型配置类：**

```Java
@Configuration
@ConfigurationProperties(prefix = "langchain4j.open-ai.chat-model")
@Data
public class ReasoningStreamingChatModelConfig {

    private String baseUrl;

    private String apiKey;

    /**
     * 推理流式模型（用于 Vue 项目生成，带工具调用）
     */
    @Bean
    public StreamingChatModel reasoningStreamingChatModel() {
        // 为了测试方便临时修改
        final String modelName = "deepseek-chat";
        final int maxTokens = 8192;

        // 生产环境使用：
        // final String modelName = "deepseek-reasoner";
        // final int maxTokens = 32768;

        return OpenAiStreamingChatModel.builder()
                .apiKey(apiKey)
                .baseUrl(baseUrl)
                .modelName(modelName)
                .maxTokens(maxTokens)
                .logRequests(true)
                .logResponses(true)
                .build();
    }
}
```

------



2. 提供给 AI **保存文件/读写文件** 的工具

由于每个 appId 对应一个生成的网站，因此 **需要根据 `appId` 构造文件保存路径**，可以利用 `LangChain4j` 工具调用提供的 **上下文传参** 能力。***在 AI Service 对话方法中加上 `memoryId` 参数***，然后就能在工具中获取到 `memoryId` 了。

```java 
/**
 * 文件写入工具
 * 支持 AI 通过工具调用的方式写入文件
 */
@Slf4j
public class FileWriteTool {

    @Tool("写入文件到指定路径")
    public String writeFile(@P("文件的相对路径") String relativeFilePath, @P("要写入文件的内容") String content, 
                            @ToolMemoryId Long appId) {
        try {
            Path path = Paths.get(relativeFilePath);
            if (!path.isAbsolute()) {
                // 相对路径处理，创建基于 appId 的项目目录
                String projectDirName = "vue_project_" + appId;
                Path projectRoot = Paths.get(AppConstant.CODE_OUTPUT_ROOT_DIR, projectDirName);
                path = projectRoot.resolve(relativeFilePath);
            }
            // 创建父目录（如果不存在）
            Path parentDir = path.getParent();
            if (parentDir != null) {
                Files.createDirectories(parentDir);
            }
            // 写入文件内容
            Files.write(path, content.getBytes(),
                    StandardOpenOption.CREATE,
                    StandardOpenOption.TRUNCATE_EXISTING);
            log.info("成功写入文件: {}", path.toAbsolutePath());
            // 注意要返回相对路径，不能让 AI 把文件绝对路径返回给用户
            return "文件写入成功: " + relativeFilePath;
        } catch (IOException e) {
            String errorMessage = "文件写入失败: " + relativeFilePath + ", 错误: " + e.getMessage();
            log.error(errorMessage, e);
            return errorMessage;
        }
    }
}
```

------







## 支持VUE项目的生成

1. 生成 VUE 项目的接口入口

**注意**：在调用 AI 的时候就需要告诉框架 `appId` 是什么，方便后面传参

```Java
/**
 * 生成 Vue 项目代码（流式）
 *
 * @param userMessage 用户消息
 * @return 生成过程的流式响应
 */
@SystemMessage(fromResource = "prompt/codegen-vue-project-system-prompt.txt")
Flux<String> generateVueProjectCodeStream(@MemoryId long appId, @UserMessage String userMessage);
```

------



2. 修改 `A⁠⁠⁠iCodeGeneratorServiceF﻿﻿﻿actory` 服务构造⁢⁢⁢工厂，**根据需要生成的代码类型‍‍‍【Vue 还是 原生html】选择不同的模型配置**。

`hallucinatedToolNameStrategy` 的解释：【**处理模型 “幻觉出来的工具名“**】

- 你明明只注册了某些工具
- 但大模型在调用工具时，可能“想象”出一个根本不存在的工具
- 这时框架就会走这个策略，告诉模型：这个工具不存在

```Java
/**
 * 创建新的 AI 服务实例
 */
private AiCodeGeneratorService createAiCodeGeneratorService(long appId, CodeGenTypeEnum codeGenType) {
    // 根据 appId 构建独立的对话记忆
    MessageWindowChatMemory chatMemory = MessageWindowChatMemory
            .builder()
            .id(appId)
            .chatMemoryStore(redisChatMemoryStore)
            .maxMessages(20)
            .build();
    // 从数据库加载历史对话到记忆中
    chatHistoryService.loadChatHistoryToMemory(appId, chatMemory, 20);
    // 根据代码生成类型选择不同的模型配置
    return switch (codeGenType) {
        // Vue 项目生成使用推理模型
        case VUE_PROJECT -> AiServices.builder(AiCodeGeneratorService.class)
                // 指定为设定的推理流式模型
                .streamingChatModel(reasoningStreamingChatModel)
                // 给 AI 服务提供 “对话记忆对象” 的获取方式。
                .chatMemoryProvider(memoryId -> chatMemory)
                .tools(new FileWriteTool())

                // 你明明只注册了某些工具
                // 但大模型在调用工具时，可能“想象”出一个根本不存在的工具
                // 这时框架就会走这个策略，告诉模型：这个工具不存在
                .hallucinatedToolNameStrategy(toolExecutionRequest -> ToolExecutionResultMessage.from(
                        toolExecutionRequest, "Error: there is no tool called " + toolExecutionRequest.name()
                ))
                .build();
        // HTML 和多文件生成使用默认模型
        case HTML, MULTI_FILE -> AiServices.builder(AiCodeGeneratorService.class)
                .chatModel(chatModel)
                .streamingChatModel(openAiStreamingChatModel)
                .chatMemory(chatMemory)
                .build();
        default -> throw new BusinessException(ErrorCode.SYSTEM_ERROR,
                "不支持的代码生成类型: " + codeGenType.getValue());
    };
}
```

------



3. **调整获取 A⁠⁠⁠I Service 缓存的逻辑**

因为现在不同生成模式获取到的 A﻿﻿﻿I Service 不同，所以需⁢⁢⁢要**额外将 `codeGenType‍‍‍` 作为缓存 key 的构造条件**，同时将本地缓存中的 key 类型写成 String 类型。

```Java
/**
 * AI 服务实例缓存
 * 缓存策略：
 * - 最大缓存 1000 个实例
 * - 写入后 30 分钟过期
 * - 访问后 10 分钟过期
 */
// TODO: Cache 的 key 类型需要修改为 String
private final Cache<String, AiCodeGeneratorService> serviceCache = Caffeine.newBuilder()
        .maximumSize(1000)
        .expireAfterWrite(Duration.ofMinutes(30))
        .expireAfterAccess(Duration.ofMinutes(10))
        .removalListener((key, value, cause) -> {
            log.debug("AI 服务实例被移除，缓存键: {}, 原因: {}", key, cause);
        })
        .build();


/**
 * 根据 appId 获取服务（带缓存）这个方法是为了兼容历史逻辑，即使只传入 appId，也能正常运行，默认将生成的代码类型指定为HTML
 */
public AiCodeGeneratorService getAiCodeGeneratorService(long appId) {
    return getAiCodeGeneratorService(appId, CodeGenTypeEnum.HTML);
}


/**
 * 根据 appId 和代码生成类型获取服务（带缓存）
 */
public AiCodeGeneratorService getAiCodeGeneratorService(long appId, CodeGenTypeEnum codeGenType) {
    // 根据 appId 和传入的 代码生成类型 构建缓存键
    String cacheKey = buildCacheKey(appId, codeGenType);
    return serviceCache.get(cacheKey, key -> createAiCodeGeneratorService(appId, codeGenType));
}


 /**
  * 构建缓存键
  */
private String buildCacheKey(long appId, CodeGenTypeEnum codeGenType) {
    return appId + "_" + codeGenType.getValue();
}
```

------



4. 修改门面类的流式调用方法，**【新增 Vue ‍‍‍工程生成的 AI 调用】**

```Java
/**
 * 统一入口：根据代码生成类型生成并保存代码（流式）
 *
 * 该方法用于处理流式代码生成场景，
 * 例如前端需要边生成边展示代码内容时使用。
 *
 * 处理流程如下：
 * 1. 校验生成类型是否为空
 * 2. 根据不同代码类型调用对应的大模型流式生成方法
 * 3. 将生成得到的代码流交给 processCodeStream 统一处理
 * 4. 在流式返回结束后自动完成 代码解析 与 文件保存
 *
 * 与非流式方法的区别：
 * - 非流式：一次性拿到结果对象后直接保存
 * - 流式：先逐步返回代码片段，结束后再统一解析和保存
 *
 * @param userMessage 用户提示词
 * @param codeGenTypeEnum 代码生成类型
 * @return 流式代码内容，供前端实时消费
 */
public Flux<String> generateAndSaveCodeStream(String userMessage, CodeGenTypeEnum codeGenTypeEnum, Long appId, String outputDirPath) {
    // 校验生成类型不能为空
    if (codeGenTypeEnum == null) {
        throw new BusinessException(ErrorCode.SYSTEM_ERROR, "生成类型为空");
    }

    // 根据 appId 和 codeGenTypeEnum 获取对应的 AI 服务实例
    AiCodeGeneratorService aiCodeGeneratorService = aiCodeGeneratorServiceFactory.getAiCodeGeneratorService(appId, codeGenTypeEnum);
    // 根据不同生成类型，调用对应的流式代码生成逻辑
    return switch (codeGenTypeEnum) {
        case HTML -> {
            // 调用 AI 服务流式生成 HTML 代码
            Flux<String> codeStream = aiCodeGeneratorService.generateHtmlCodeStream(userMessage);

            // 对流式代码进行统一处理：收集、解析、保存
            yield processCodeStream(codeStream, CodeGenTypeEnum.HTML, appId, outputDirPath);
        }
        case MULTI_FILE -> {
            // 调用 AI 服务流式生成多文件代码
            Flux<String> codeStream = aiCodeGeneratorService.generateMultiFileCodeStream(userMessage);

            // 对流式代码进行统一处理：收集、解析、保存
            yield processCodeStream(codeStream, CodeGenTypeEnum.MULTI_FILE, appId, outputDirPath);
        }
            
        // ========================================修改点=============================================    
        // 新增生成 VUE 工程项目
        case VUE_PROJECT -> {
            Flux<String> codeStream = aiCodeGeneratorService.generateVueProjectCodeStream(appId, userMessage);
            yield processTokenStream(codeStream, CodeGenTypeEnum.MULTI_FILE, appId, outputDirPath);
        }
            
        default -> {
            // 如果传入的生成类型系统暂不支持，则抛出业务异常
            String errorMessage = "不支持的生成类型：" + codeGenTypeEnum.getValue();
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, errorMessage);
        }
    };
}
```

------







## 统一消息格式

之前我们只需要⁠⁠⁠⁠⁠给前端返回 **AI 的响应信息**，但**现在还需要返回工具调用信﻿﻿﻿﻿﻿息**（后续还有可能需要返回深度⁢⁢⁢⁢⁢思考信息），因此需要约定一种‍‍‍‍‍消息格式，来区分不同的信息类型。

包括：

- **AI 响应消息**
- **工具调用消息**
- **工具调用完成消息**

在 `ai.model.message` 包下新建 `StreamMessage` 流式消息基类，并提供几种类型的消息实现子类、以及消息类型枚举类。

<img src="./AI零代码生成平台.assets/g9Wwyuwe23V5cYi7.webp" alt="img" style="zoom:50%;" />

------



- **对 `TokenStream` 的处理：**

例如 AI 原始的返回内容：

```
AI 响应 {"为你生成代码"}

工具调用请求 {index=0, id="call_0", name="writeFile", arguments="流式参数"}
工具调用完成 {index=0, id="call_0", name="writeFile", arguments="完整参数"}

工具调用请求 {index=1, id="call_1", name="writeFile", arguments="流式参数"}
工具调用完成 {index=1, id="call_1", name="writeFile", arguments="完整参数"}

AI 响应 {"生成代码结束"}
```

封装便于下游处理：

```
{type="ai_response", data="为你生成代码"}

{type="tool_request", index=0, id="call_0", name="writeFile", arguments="流式参数"}
{type="tool_executed", index=0, id="call_0", name="writeFile", arguments="完整参数"}

{type="tool_request", index=1, id="call_1", name="writeFile", arguments="流式参数"}
{type="tool_executed", index=1, id="call_1", name="writeFile", arguments="完整参数"}

{type="ai_response", data="生成代码结束"}
```

写入数据库的格式：

```
为你生成代码：

[工具调用] 写入文件 src/index.html
```html
写入的代码

[工具调用] 写入文件 src/about.html
```html
写入的代码

生成代码结束！
```

------

- ***最终流程：***

![img](./AI零代码生成平台.assets/YYvsOP9syYHjk4YY.webp)

------



- 在 `AiCodeGeneratorFacade` 门面类中编写一个 **适配方法**，将 `TokenStream` 转换为 Flux 对象，之后下游可以愉快地对 Flux 进行处理了，也便于返回给前端。

***作用：***

1. 监听 `LangChain4j` 的 `TokenStream` 各类事件【**普通响应，监听工具调用请求，监听工具执行完成事件，监听整个AI响应流结束**，...】
2. 把 AI 普通文本响应、工具调用请求、工具执行结果统一封装成 JSON 字符串
3. 再通过 Reactor 的 Flux 流向下游传递，便于前端实时消费

```java
/**
 * 将 TokenStream 适配为 Flux<String> 流
 *
 * 作用：
 * 1. 监听 LangChain4j 的 TokenStream 各类事件
 * 2. 把 AI 普通文本响应、工具调用请求、工具执行结果统一封装成 JSON 字符串
 * 3. 再通过 Reactor 的 Flux 流向下游传递，便于前端实时消费
 *
 * 为什么要这样做：
 * 因为上层统一是基于 Flux<String> 来处理流式输出，
 * 而 Vue 工程生成场景返回的是 TokenStream，所以这里需要做一层“适配”。
 *
 * @param tokenStream LangChain4j 返回的流式 TokenStream 对象
 * @return Flux<String> 统一后的流式响应，每个元素都是一个 JSON 字符串
 */
private Flux<String> processTokenStream(TokenStream tokenStream) {
    return Flux.create(sink -> {
        // 监听 AI 普通文本的流式响应
        // 比如模型先返回“正在为你生成代码...”之类的内容
        tokenStream.onPartialResponse((String partialResponse) -> {
                    // 将普通 AI 文本封装成统一消息对象
                    AiResponseMessage aiResponseMessage = new AiResponseMessage(partialResponse);

                    // 转成 JSON 字符串后推送给 Flux 下游
                    sink.next(JSONUtil.toJsonStr(aiResponseMessage));
                })

                // 监听工具调用请求的流式事件
                .onPartialToolExecutionRequest((index, toolExecutionRequest) -> {
                    // 将工具请求封装成统一消息对象
                    ToolRequestMessage toolRequestMessage = new ToolRequestMessage(toolExecutionRequest);

                    // 转成 JSON 字符串后推送给 Flux 下游
                    // 前端可据此实时展示“正在选择工具”或“正在构造工具参数”
                    sink.next(JSONUtil.toJsonStr(toolRequestMessage));
                })

                // 监听工具执行完成事件
                .onToolExecuted((ToolExecution toolExecution) -> {
                    // 将工具执行结果封装成统一消息对象
                    ToolExecutedMessage toolExecutedMessage = new ToolExecutedMessage(toolExecution);

                    // 转成 JSON 字符串后推送给 Flux 下游
                    // 前端可据此展示“某文件已写入完成”等信息
                    sink.next(JSONUtil.toJsonStr(toolExecutedMessage));
                })

                // 监听整个 AI 响应流结束事件
                .onCompleteResponse((ChatResponse response) -> {
                    // 通知 Flux 下游：流已经结束
                    sink.complete();
                })

                // 监听异常事件
                .onError((Throwable error) -> {
                    // 打印异常日志，方便排查问题
                    error.printStackTrace();

                    // 将异常继续传递给 Flux 下游
                    sink.error(error);
                })

                // 启动流式处理
                .start();
    });
}
```

------







### Flux流处理器

接下来，我们还要编写下游的 ⁠⁠⁠⁠⁠Flux 流处理器，之前我们是在 `AppService` 的 `chatToGenCode` 生成方法内处理了原生﻿﻿﻿﻿﻿模式生成的流。现在**由于 Vue 生成模式的消息被封装为⁢⁢⁢⁢⁢了 JSON 格式消息，所以我们最好针对每类生成模式单‍‍‍‍‍独定义一个流处理器**，防止逻辑互相影响。

- 原生文本流处理器（原生模式使用）
- JSON 消息流处理器（Vue 工程使用）

然后再定义⁠⁠⁠⁠⁠一个执行器，根据生成类型调用不同的流处﻿﻿﻿﻿﻿理器。（这个操作⁢⁢⁢⁢⁢在之前的策略模式、‍‍‍‍‍模板方法模式中都用过）

<img src="./AI零代码生成平台.assets/TuZy400QCkS3ly0q.webp" alt="img" style="zoom: 80%;" />

------

- **代码结构：**

<img src="./AI零代码生成平台.assets/Moja4Q2Bzgd3OJSd.webp" alt="img" style="zoom:50%;" />

------





1. **简单文本流处理器**

```Java
/**
 * 简单文本流处理器
 * 处理 HTML 和 MULTI_FILE 类型的流式响应
 */
@Slf4j
public class SimpleTextStreamHandler {

    /**
     * 处理传统流（HTML, MULTI_FILE）
     * 直接收集完整的文本响应
     *
     * @param originFlux         原始流
     * @param chatHistoryService 聊天历史服务
     * @param appId              应用ID
     * @param loginUser          登录用户
     * @return 处理后的流
     */
    public Flux<String> handle(Flux<String> originFlux,
                               ChatHistoryService chatHistoryService,
                               long appId, User loginUser) {
        StringBuilder aiResponseBuilder = new StringBuilder();
        return originFlux
                .map(chunk -> {
                    // 收集AI响应内容
                    aiResponseBuilder.append(chunk);
                    return chunk;
                })
                .doOnComplete(() -> {
                    // 流式响应完成后，添加AI消息到对话历史
                    String aiResponse = aiResponseBuilder.toString();
                    chatHistoryService.addChatMessage(appId, aiResponse, ChatHistoryMessageTypeEnum.AI.getValue(), loginUser.getId());
                })
                .doOnError(error -> {
                    // 如果AI回复失败，也要记录错误消息
                    String errorMessage = "AI回复失败: " + error.getMessage();
                    chatHistoryService.addChatMessage(appId, errorMessage, ChatHistoryMessageTypeEnum.AI.getValue(), loginUser.getId());
                });
    }
}
```

------



2. **JS⁠⁠⁠⁠⁠ON 消息流处理器【在原生流处理器的基础上增加了 2﻿﻿﻿﻿﻿ 个逻辑】**

1. 消息解析：需要根据消息类型，将 JSON 字符串转换为对应的消息对象，然后提取属性进行其他操作（比如返回给前端、或者拼接起来保存到数据库中）
2. 输出选择工具消息：虽然我们后端实现了工具调用的流式输出，但是考虑到前端不好对这些消息进行解析和处理，因此我们只在 **同一个工具第一次输出时【后面的工具调用细则无需返回给前端】**，输出给前端 “选择工具” 的消息。可以**利用一个集合来判断某个 id 的工具是否为首次输出**。

~~~Java
/**
 * JSON 消息流处理器
 * 处理 VUE_PROJECT 类型的复杂流式响应，包含工具调用信息
 */
@Slf4j
@Component
public class JsonMessageStreamHandler {

    /**
     * 处理 TokenStream（VUE_PROJECT）
     * 解析 JSON 消息并重组为完整的响应格式
     *
     * @param originFlux         原始流
     * @param chatHistoryService 聊天历史服务
     * @param appId              应用ID
     * @param loginUser          登录用户
     * @return 处理后的流
     */
    public Flux<String> handle(Flux<String> originFlux,
                               ChatHistoryService chatHistoryService,
                               long appId, User loginUser) {
        // 收集数据用于生成后端记忆格式
        StringBuilder chatHistoryStringBuilder = new StringBuilder();
        // 用于跟踪已经见过的工具ID，判断是否是第一次调用
        Set<String> seenToolIds = new HashSet<>();
        return originFlux
                .map(chunk -> {
                    // 解析每个 JSON 消息块
                    return handleJsonMessageChunk(chunk, chatHistoryStringBuilder, seenToolIds);
                })
                .filter(StrUtil::isNotEmpty) // 过滤空字串
            
                .doOnComplete(() -> {
                    // 流式响应完成后，添加 AI 消息到对话历史
                    String aiResponse = chatHistoryStringBuilder.toString();
                    chatHistoryService.addChatMessage(appId, aiResponse, ChatHistoryMessageTypeEnum.AI.getValue(), loginUser.getId());
                })
            
                .doOnError(error -> {
                    // 如果AI回复失败，也要记录错误消息
                    String errorMessage = "AI回复失败: " + error.getMessage();
                    chatHistoryService.addChatMessage(appId, errorMessage, ChatHistoryMessageTypeEnum.AI.getValue(), loginUser.getId());
                });
    }

    
    /**
     * 解析并收集 TokenStream 数据
     */
    private String handleJsonMessageChunk(String chunk, StringBuilder chatHistoryStringBuilder, Set<String> seenToolIds) {
        // 解析 JSON
        StreamMessage streamMessage = JSONUtil.toBean(chunk, StreamMessage.class);
        // 获取到具体的消息类型
        StreamMessageTypeEnum typeEnum = StreamMessageTypeEnum.getEnumByValue(streamMessage.getType());
        switch (typeEnum) {
            case AI_RESPONSE -> {
                AiResponseMessage aiMessage = JSONUtil.toBean(chunk, AiResponseMessage.class);
                String data = aiMessage.getData();
                // 直接拼接响应
                chatHistoryStringBuilder.append(data);
                return data;
            }
            case TOOL_REQUEST -> {
                ToolRequestMessage toolRequestMessage = JSONUtil.toBean(chunk, ToolRequestMessage.class);
                String toolId = toolRequestMessage.getId();
                // 检查是否是第一次看到这个工具 ID
                if (toolId != null && !seenToolIds.contains(toolId)) {
                    // 第一次调用这个工具，记录 ID 并完整返回工具信息
                    seenToolIds.add(toolId);
                    return "\n\n[选择工具] 写入文件\n\n";
                } else {
                    // 不是第一次调用这个工具，直接返回空
                    return "";
                }
            }
            case TOOL_EXECUTED -> {
                ToolExecutedMessage toolExecutedMessage = JSONUtil.toBean(chunk, ToolExecutedMessage.class);
                JSONObject jsonObject = JSONUtil.parseObj(toolExecutedMessage.getArguments());
                String relativeFilePath = jsonObject.getStr("relativeFilePath");
                String suffix = FileUtil.getSuffix(relativeFilePath);
                String content = jsonObject.getStr("content");
                String result = String.format("""
                        [工具调用] 写入文件 %s
                        ```%s
                        %s
                        ```
                        """, relativeFilePath, suffix, content);
                // 输出前端和要持久化的内容
                String output = String.format("\n\n%s\n\n", result);
                chatHistoryStringBuilder.append(output);
                return output;
            }
            default -> {
                log.error("不支持的消息类型: {}", typeEnum);
                return "";
            }
        }
    }
}
~~~

------



3. 最外层的执行器

```Java
/**
 * 流处理器执行器
 * 根据代码生成类型创建合适的流处理器：
 * 1. 传统的 Flux<String> 流（HTML、MULTI_FILE） -> SimpleTextStreamHandler
 * 2. TokenStream 格式的复杂流（VUE_PROJECT） -> JsonMessageStreamHandler
 */
@Slf4j
@Component
public class StreamHandlerExecutor {

    @Resource
    private JsonMessageStreamHandler jsonMessageStreamHandler;

    /**
     * 创建流处理器并处理聊天历史记录
     *
     * @param originFlux         原始流
     * @param chatHistoryService 聊天历史服务
     * @param appId              应用ID
     * @param loginUser          登录用户
     * @param codeGenType        代码生成类型
     * @return 处理后的流
     */
    public Flux<String> doExecute(Flux<String> originFlux,
                                  ChatHistoryService chatHistoryService,
                                  long appId, User loginUser, CodeGenTypeEnum codeGenType) {
        return switch (codeGenType) {
            case VUE_PROJECT -> // 使用注入的组件实例
                    jsonMessageStreamHandler.handle(originFlux, chatHistoryService, appId, loginUser);
            case HTML, MULTI_FILE -> // 简单文本处理器不需要依赖注入
                    new SimpleTextStreamHandler().handle(originFlux, chatHistoryService, appId, loginUser);
        };
    }
}
```

------



4. 修改调用 AI 的核心方法：`chatToGenCode`【AppServiceImpl2 类中】

将原本的 `.doComplete()`,  `.map()`,  `.doError()` 等流式调用方法改为一句话：【调用执行器类】

```Java
return streamHandlerExecutor.doExecute(codeStream, chatHistoryService, appId, loginUser, codeGenTypeEnum);
```

------







## 工程项目构建和浏览

代码生成后⁠⁠⁠，**需要安装依赖和打包构建，才﻿能浏﻿览 ﻿Vue ⁢项目⁢**。我们在 `core.builder` 目录下新建一个 `VueProjectBuilder`，专门用来编写 Vue 项目的构建过程。

- ***编写一系列类似执行 `npm install`，`npm run build` 等命令***

```java
@Slf4j
public class VueProjectBuilder {

    /**
     * 执行命令
     *
     * @param workingDir     工作目录
     * @param command        命令字符串
     * @param timeoutSeconds 超时时间（秒）
     * @return 是否执行成功
     */
    private boolean executeCommand(File workingDir, String command, int timeoutSeconds) {
        try {
            log.info("在目录 {} 中执行命令: {}", workingDir.getAbsolutePath(), command);
            Process process = RuntimeUtil.exec(
                    null,
                    workingDir,
                    command.split("\\s+") // 命令分割为数组
            );
            // 等待进程完成，设置超时
            boolean finished = process.waitFor(timeoutSeconds, TimeUnit.SECONDS);
            if (!finished) {
                log.error("命令执行超时（{}秒），强制终止进程", timeoutSeconds);
                process.destroyForcibly();
                return false;
            }
            int exitCode = process.exitValue();
            if (exitCode == 0) {
                log.info("命令执行成功: {}", command);
                return true;
            } else {
                log.error("命令执行失败，退出码: {}", exitCode);
                return false;
            }
        } catch (Exception e) {
            log.error("执行命令失败: {}, 错误信息: {}", command, e.getMessage());
            return false;
        }
    }


    /**
     * 编写构建项目的方法，组合执行上述命令，并且校验是否构建成功
     *
     * @param projectPath 项目根目录路径
     * @return 是否构建成功
     */
    public boolean buildProject(String projectPath) {
        File projectDir = new File(projectPath);
        if (!projectDir.exists() || !projectDir.isDirectory()) {
            log.error("项目目录不存在: {}", projectPath);
            return false;
        }
        
        // 检查 package.json 是否存在
        File packageJson = new File(projectDir, "package.json");
        if (!packageJson.exists()) {
            log.error("package.json 文件不存在: {}", packageJson.getAbsolutePath());
            return false;
        }
        log.info("开始构建 Vue 项目: {}", projectPath);
        
        // 执行 npm install
        if (!executeNpmInstall(projectDir)) {
            log.error("npm install 执行失败");
            return false;
        }
        
        // 执行 npm run build
        if (!executeNpmBuild(projectDir)) {
            log.error("npm run build 执行失败");
            return false;
        }
        
        // 验证 dist 目录是否生成
        File distDir = new File(projectDir, "dist");
        if (!distDir.exists()) {
            log.error("构建完成但 dist 目录未生成: {}", distDir.getAbsolutePath());
            return false;
        }
        log.info("Vue 项目构建成功，dist 目录: {}", distDir.getAbsolutePath());
        return true;
    }


    /**
     * 执行 npm install 命令
     */
    private boolean executeNpmInstall(File projectDir) {
        log.info("执行 npm install...");
        String command = String.format("%s install", buildCommand("npm"));
        return executeCommand(projectDir, command, 300); // 5分钟超时
    }


    /**
     * 执行 npm run build 命令
     */
    private boolean executeNpmBuild(File projectDir) {
        log.info("执行 npm run build...");
        String command = String.format("%s run build", buildCommand("npm"));
        return executeCommand(projectDir, command, 180); // 3分钟超时
    }


    /**
     * 检测是否是 windows 操作系统
     * @return
     */
    private boolean isWindows() {
        return System.getProperty("os.name").toLowerCase().contains("windows");
    }


    /**
     * 如果是 windows 需要加上 .cmd 命令
     * @param baseCommand
     * @return
     */
    private String buildCommand(String baseCommand) {
        if (isWindows()) {
            return baseCommand + ".cmd";
        }
        return baseCommand;
    }
}
```

------





### 虚拟线程

由于打⁠⁠⁠包构建是个耗时操作，为了**不阻塞主流程，可﻿﻿﻿以使用 Java 2⁢⁢⁢1 的虚拟线程特性**，‍‍‍在单独的线程中执行构建。

- **在 `VueProjectBuilder` 类下编写异步处理方法：**

```Java
/**
 * 异步构建项目（不阻塞主流程）
 *
 * @param projectPath 项目路径
 */
public void buildProjectAsync(String projectPath) {
    // 在单独的线程中执行构建，避免阻塞主流程
    Thread.ofVirtual().name("vue-builder-" + System.currentTimeMillis()).start(() -> {
        try {
            buildProject(projectPath);
        } catch (Exception e) {
            log.error("异步构建 Vue 项目时发生异常: {}", e.getMessage(), e);
        }
    });
}
```

------



JSON⁠⁠⁠ 消息流式处理器 `JsonMessageSt﻿﻿﻿reamHandler`⁢⁢⁢ 中，当流式输出完成后‍‍‍，**执行 Vue 项目的构建**：

```java 
@Resource
private VueProjectBuilder vueProjectBuilder;

originFlux
.doOnComplete(() -> {
    // 流式响应完成后，添加 AI 消息到对话历史
    String aiResponse = chatHistoryStringBuilder.toString();
    chatHistoryService.addChatMessage(appId, aiResponse, ChatHistoryMessageTypeEnum.AI.getValue(), loginUser.getId());
    
    // ===========================================修改点=================================================
    // 异步构造 Vue 项目
    String projectPath = AppConstant.CODE_OUTPUT_ROOT_DIR + "/vue_project_" + appId;
    vueProjectBuilder.buildProjectAsync(projectPath);
})
```

------





### 项目部署

在 `deployApp` 项目部署方法中新增如下内容：【**根据 `codeGenType` 对 Vue 工程项目进行特殊判断及构建**】

```Java
        // 补充：Vue 项目特殊处理：执行构建
        String codeGenType = app.getCodeGenType();
        CodeGenTypeEnum codeGenTypeEnum = CodeGenTypeEnum.getEnumByValue(codeGenType);
        if (codeGenTypeEnum == CodeGenTypeEnum.VUE_PROJECT) {
            
            // Vue 项目需要构建
            boolean buildSuccess = vueProjectBuilder.buildProject(sourceDirPath);
            ThrowUtils.throwIf(!buildSuccess, ErrorCode.SYSTEM_ERROR, "Vue 项目构建失败，请检查代码和依赖");
            
            // 检查 dist 目录是否存在
            File distDir = new File(sourceDirPath, "dist");
            ThrowUtils.throwIf(!distDir.exists(), ErrorCode.SYSTEM_ERROR, "Vue 项目构建完成但未生成 dist 目录");
            
            // 将 dist 目录作为部署源
            sourceDir = distDir;
            log.info("Vue 项目构建成功，将部署 dist 目录: {}", distDir.getAbsolutePath());
        }
```

------









# *生成应用封面图

参考其他的大⁠⁠⁠厂平台，可以直接 **将网站的实际运行效果作为应用﻿﻿﻿封面图**

- **方案设计：**

1）首先要获取到应用的可访问 URL。由于我们的平台支持多种生成模式（原生 HTML、多文件项目、Vue 工程），其中原生模式和 Vue 工程模式生成可访问浏览 URL 的时机不一样。所以为了统一处理，而且**确保应用已经可以正常访问**，我们选择在 **应用部署完成后再生成封面图**。

2）使用 [Selenium](https://www.selenium.dev/zh-cn/documentation/) 这样的自动化工具打开一个无头浏览器，**访问应用页面并进行截图。**

3）**直接截⁠⁠⁠图得到的图片通常比较大**，不仅占用存储﻿﻿﻿空间，加载速度也会⁢⁢⁢比较慢。因此我们**需‍‍‍要对图片进行压缩处理**。

虽然我们可以通⁠⁠⁠过调整 Selenium 的窗口大小来控制截图尺寸，但这﻿﻿﻿样可能会导致页面显示不全。**更⁢⁢⁢好的方案是先按正常尺寸截图，‍‍‍然后使用工具库对图片进行压缩**。

4）为了确保⁠⁠⁠图片的持久化存储和快速访问，**将压缩后的图片上传到﻿﻿﻿腾讯云 COS 对象存储⁢⁢⁢中，并将访问 URL 保‍‍‍存到数据库的应用表中**。

5）最后，⁠记得清理本地临时文件。

<img src="./AI零代码生成平台.assets/kxFctTy1ASQ3vLA8.webp" alt="img" style="zoom:50%;" />

------







## 1.引入Selenium依赖

```xml
<!-- Selenium 网页截图依赖 -->
<dependency>
    <groupId>org.seleniumhq.selenium</groupId>
    <artifactId>selenium-java</artifactId>
    <version>4.33.0</version>
</dependency>
<dependency>
    <groupId>io.github.bonigarcia</groupId>
    <artifactId>webdrivermanager</artifactId>
    <version>6.1.0</version>
</dependency>
```

------





## 2.本地生成截图

**初始化驱动：**

1. 在静态代码块里初始化驱动，确保整个应用生命周期内只初始化一次
2. 默认使用已经初始化好的驱动实例
3. 在项目停止前正确销毁驱动，释放资源

**以下的关键配置：**

1. ***无头模式运行***：通过 `--headless` 参数，Chrome 浏览器在后台运行，不会弹出窗口。
2. ***Docker 兼容性***：添加了 `--no-sandbox` 和 `--disable-dev-shm-usage` 参数，确保在容器环境中正常运行。

```Java
@Slf4j
public class WebScreenshotUtils {

    private static final WebDriver webDriver;

    static {
        // 指定截图的默认宽高
        final int DEFAULT_WIDTH = 1600;
        final int DEFAULT_HEIGHT = 900;

        // 在类加载时初始化 ChromeDriver，相当于打开了一个 chrome 浏览器
        webDriver = initChromeDriver(DEFAULT_WIDTH, DEFAULT_HEIGHT);
    }


    /**
     * 销毁 WebDriver 资源
     *
     * @PreDestroy 表示当前对象销毁前自动执行该方法，
     * 常用于 Spring 容器管理的 Bean，在项目关闭时释放资源。
     *
     * 作用：
     * 1. 关闭浏览器进程
     * 2. 释放系统资源
     * 3. 避免 ChromeDriver 进程残留
     */
    @PreDestroy
    public void destroy() {
        webDriver.quit();
    }


    /**
     * 初始化 Chrome 浏览器驱动
     */
    private static WebDriver initChromeDriver(int width, int height) {
        try {
            // 自动管理 ChromeDriver
            WebDriverManager.chromedriver().setup();
            // 配置 Chrome 选项
            ChromeOptions options = new ChromeOptions();
            // 无头模式
            options.addArguments("--headless");
            // 禁用GPU（在某些环境下避免问题）
            options.addArguments("--disable-gpu");
            // 禁用沙盒模式（Docker环境需要）
            options.addArguments("--no-sandbox");
            // 禁用开发者shm使用
            options.addArguments("--disable-dev-shm-usage");
            // 设置窗口大小
            options.addArguments(String.format("--window-size=%d,%d", width, height));
            // 禁用扩展
            options.addArguments("--disable-extensions");
            // 设置用户代理
            options.addArguments("--user-agent=Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36");
            // 创建驱动
            WebDriver driver = new ChromeDriver(options);
            // 设置页面加载超时
            driver.manage().timeouts().pageLoadTimeout(Duration.ofSeconds(30));
            // 设置隐式等待
            driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(10));
            return driver;
        } catch (Exception e) {
            log.error("初始化 Chrome 浏览器失败", e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "初始化 Chrome 浏览器失败");
        }
    }
}
```

------





- **该工具类下的一些子方法：**

保存图片到文件：

```Java
/**
 * 保存图片到文件
 */
private static void saveImage(byte[] imageBytes, String imagePath) {
    try {
        FileUtil.writeBytes(imageBytes, imagePath);
    } catch (Exception e) {
        log.error("保存图片失败: {}", imagePath, e);
        throw new BusinessException(ErrorCode.SYSTEM_ERROR, "保存图片失败");
    }
}
```



压缩图片：

```Java
/**
 * 压缩图片
 */
private static void compressImage(String originalImagePath, String compressedImagePath) {
    // 压缩图片质量（0.1 = 10% 质量）
    final float COMPRESSION_QUALITY = 0.3f;
    try {
        ImgUtil.compress(
                FileUtil.file(originalImagePath),
                FileUtil.file(compressedImagePath),
                COMPRESSION_QUALITY
        );
    } catch (Exception e) {
        log.error("压缩图片失败: {} -> {}", originalImagePath, compressedImagePath, e);
        throw new BusinessException(ErrorCode.SYSTEM_ERROR, "压缩图片失败");
    }
}
```



等待页面加载完成：

```Java
/**
 * 等待页面加载完成
 */
private static void waitForPageLoad(WebDriver driver) {
    try {
        // 创建等待页面加载对象
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));

        // 等待 document.readyState 为 complete【表示页面加载完成了】
        wait.until(webDriver ->
                ((JavascriptExecutor) webDriver).executeScript("return document.readyState")
                        .equals("complete")
        );

        // 额外等待一段时间，确保动态内容加载完成
        Thread.sleep(2000);
        log.info("页面加载完成");
    } catch (Exception e) {
        log.error("等待页面加载时出现异常，继续执行截图", e);
    }
}
```

------





### 2.1 完整的生成网页截图

1. 非空校验
2. 指定访问网页后截图需要保存到的路径
3. 访问网页，并调用之前写好的 waitForPageLoad() 方法等待网页加载
4. 截图并保存到之前写好的保存路径
5. 指定压缩图片的保存路径并执行压缩图片的方法
6. 最后删除原先的截图

```Java
/**
 * 生成网页截图
 *
 * @param webUrl 网页URL
 * @return 压缩后的截图文件路径，失败返回null
 */
public static String saveWebPageScreenshot(String webUrl) {
    // 非空校验
    if (StrUtil.isBlank(webUrl)) {
        log.error("网页URL不能为空");
        return null;
    }

    try {
        // 访问网页
        // 指定根目录路径
        String rooPath = System.getProperty("user.dir") + "/tmp/screenshots/" + UUID.randomUUID().toString().substring(0, 8);
        FileUtil.mkdir(rooPath);
        // 指定图片后缀
        final String IMAGE_SUFFIX = ".png";
        // 指定原始图片的保存路径
        String imageSavePath = rooPath + File.separator + RandomUtil.randomNumbers(5) + IMAGE_SUFFIX;

        // 访问网页
        webDriver.get(webUrl);
        // 等待网页加载
        waitForPageLoad(webDriver);

        // 截图【指定文件的输出类型为字节流】
        byte[] screenshotBytes = ((TakesScreenshot) webDriver).getScreenshotAs(OutputType.BYTES);
        // 将截下的原始图片保存【调用图片保存方法】
        saveImage(screenshotBytes, imageSavePath);
        log.info("原始图片保存成功：{}", imageSavePath);

        // 压缩图片
        // 指定压缩后的图片的保存路径
        final String COMPRESS_SUFFIX = "_compressed.jpg";
        String compressedImagePath = rooPath + File.separator + RandomUtil.randomNumbers(5) + COMPRESS_SUFFIX;
        // 调用压缩图片方法
        compressImage(imageSavePath, compressedImagePath);
        log.info("压缩图片保存成功: {}", compressedImagePath);

        // 删除原始图片，只保留压缩图片
        FileUtil.del(imageSavePath);
        return compressedImagePath;
    } catch (Exception e) {
        log.error("网页截图失败: {}", webUrl, e);
        return null;
    }
}
```

------







## 3.保存图片到对象存储

1. 首先在 [腾讯云控制台](https://console.cloud.tencent.com/cos/bucket) 创建一个存储桶：

<img src="./AI零代码生成平台.assets/9GR1W9xXbcFacN3g.webp" alt="img" style="zoom: 33%;" />

- **按需选择参数：**

<img src="./AI零代码生成平台.assets/eYM0iTmvCFv3MQ3f.webp" alt="img" style="zoom: 33%;" />

------



2. 在 `application.yml` 配置文件中添加上如下配置：

```yml
# 添加 COS 对象存储配置（需要从腾讯云获取）
cos:
  client:
  	# 创建的对象存储桶域名
    host: your-custom-domain.com
    secretId: your-secret-id
    secretKey: your-secret-key
    region: ap-shanghai
    bucket: your-bucket-name
```

- **引入腾讯云服务依赖：**

```xml
<dependency>
     <groupId>com.qcloud</groupId>
     <artifactId>cos_api</artifactId>
     <version>5.6.227</version>
</dependency>
```

------



3. 在 config 包下创建 **COS 客户端对应的配置类**

```Java
/**
 * 腾讯云COS配置类
 * 
 * @author yupi
 */
@Configuration
@ConfigurationProperties(prefix = "cos.client")
@Data
public class CosClientConfig {

    /**
     * 域名
     */
    private String host;

    /**
     * secretId
     */
    private String secretId;

    /**
     * 密钥（注意不要泄露）
     */
    private String secretKey;

    /**
     * 区域
     */
    private String region;

    /**
     * 桶名
     */
    private String bucket;

    @Bean
    public COSClient cosClient() {
        // 初始化用户身份信息(secretId, secretKey)
        COSCredentials cred = new BasicCOSCredentials(secretId, secretKey);
        // 设置bucket的区域, COS地域的简称请参照 https://www.qcloud.com/document/product/436/6224
        ClientConfig clientConfig = new ClientConfig(new Region(region));
        // 生成cos客户端
        return new COSClient(cred, clientConfig);
    }
}
```

------



4. 在 `manager` 包下创建可复用的 `CosManager` 类，专门负责和 COS 对象存储进行交互，提供文件上传功能，不包含特殊的业务逻辑

```java
/**
 * COS对象存储管理器
 *
 * @author yupi
 */
@Component
@Slf4j
public class CosManager {

    @Resource
    private CosClientConfig cosClientConfig;
    @Resource
    private COSClient cosClient;


    /**
     * 上传对象
     *
     * @param key  唯一键【key = "user/avatar/1.png"】
     * @param file 文件
     * @return 上传结果
     */
    public PutObjectResult putObject(String key, File file) {
        PutObjectRequest putObjectRequest = new PutObjectRequest(cosClientConfig.getBucket(), key, file);
        return cosClient.putObject(putObjectRequest);
    }


    /**
     * 上传文件到 COS 并返回访问 URL
     *
     * @param key  COS对象键（完整路径）
     * @param file 要上传的文件
     * @return 文件的访问URL，失败返回null
     */
    public String uploadFile(String key, File file) {
        // 上传文件
        PutObjectResult result = putObject(key, file);
        if (result != null) {
            // 构建访问URL
            String url = String.format("%s%s", cosClientConfig.getHost(), key);
            log.info("文件上传COS成功: {} -> {}", file.getName(), url);
            return url;
        } else {
            log.error("文件上传COS失败，返回结果为空");
            return null;
        }
    }
}
```

Manager 层是指 **通用业务处理层**，它有如下特征：

1. **对第三方平台封装的层**，预处理返回结果及转化异常信息（适配上层接口）
2. **对 Service 层通用能力的下沉**，如缓存方案、中间件通用处理
3. 与 DAO 层交互，对多个 DAO 的组合复用

<img src="./AI零代码生成平台.assets/Z0bskWiRi6xT40bK.webp" alt="img" style="zoom: 50%;" />

------







## 4.截图服务

考虑到后续项目要改造为微服务，最好将截图功能单独封装为一个 **通用服务**，将本地生成截图和文件上传整合在一起。不包含 appId 等具体的业务参数，作用就是 **根据要截图的网址返回截图后的图片地址**。

- ***主要业务逻辑：将之前在本地生成的压缩后的截图文件【`saveWebPageScreenshot` 逻辑】上传到 COS 对象存储服务中【`CosManager` 的 `uploadFile` 方法】，注意唯一键格式：`/screenshots/2025/07/31/8位随机数 + compressed.jpg`***

```Java
@Service
@Slf4j
public class ScreenshotServiceImpl implements ScreenshotService {

    @Resource
    private CosManager cosManager;

    @Override
    public String generateAndUploadScreenshot(String webUrl) {
        // 参数校验
        ThrowUtils.throwIf(StrUtil.isBlank(webUrl), ErrorCode.PARAMS_ERROR, "截图网址不能为空");
        log.info("开始生成并上传网页截图，网址: {}", webUrl);

        // 生成本地截图【调用之前写的 saveWebPageScreenshot 生成压缩后的图片】
        String localScreenshotPath = WebScreenshotUtils.saveWebPageScreenshot(webUrl);
        ThrowUtils.throwIf(StrUtil.isBlank(localScreenshotPath), ErrorCode.OPERATION_ERROR, "截图生成失败");

        try {
            // 上传到 COS
            String cosUrl = uploadScreenshotToCos(localScreenshotPath);
            ThrowUtils.throwIf(StrUtil.isBlank(cosUrl), ErrorCode.OPERATION_ERROR, "截图上传对象存储失败");
            log.info("网页截图生成并上传成功: {} -> {}", webUrl, cosUrl);
            return cosUrl;

        } finally {
            // 清理本地文件
            cleanupLocalFile(localScreenshotPath);
        }
    }


    /**
     * 上传截图到对象存储
     *
     * @param localScreenshotPath 本地截图路径
     * @return 对象存储访问URL，失败返回null
     */
    private String uploadScreenshotToCos(String localScreenshotPath) {
        if (StrUtil.isBlank(localScreenshotPath)) {
            return null;
        }

        // 将本地文件路径转换为文件类型
        File screenshotFile = new File(localScreenshotPath);
        if (!screenshotFile.exists()) {
            log.error("截图文件不存在: {}", localScreenshotPath);
            return null;
        }
        
        // 生成 COS 对象键
        String fileName = UUID.randomUUID().toString().substring(0, 8) + "_compressed.jpg";
        String cosKey = generateScreenshotKey(fileName);
        return cosManager.uploadFile(cosKey, screenshotFile);
    }


    /**
     * 生成截图的对象存储键
     * 格式：/screenshots/2025/07/31/8位随机数 + compressed.jpg
     */
    private String generateScreenshotKey(String fileName) {
        // 根据时间来区分每个图片保存到 COS 的唯一键
        String datePath = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
        return String.format("/screenshots/%s/%s", datePath, fileName);
    }


    /**
     * 清理本地文件
     *
     * @param localFilePath 本地文件路径
     */
    private void cleanupLocalFile(String localFilePath) {
        File localFile = new File(localFilePath);
        if (localFile.exists()) {
            File parentDir = localFile.getParentFile();
            FileUtil.del(parentDir);
            log.info("本地截图文件已清理: {}", localFilePath);
        }
    }
}
```

------







## 5.触发截图生成

在原本的 `AppServiceImpl` 中的 `deployApp` 方法中，**在最后得到网站 Url 之后，新增异步生成截图并更新应用封面的方法**

```Java
// 10. 构建应用访问 URL
String appDeployUrl = String.format("%s/%s/", AppConstant.CODE_DEPLOY_HOST, deployKey);

// 11. 异步生成截图并更新应用封面
generateAppScreenshotAsync(appId, appDeployUrl);
return appDeployUrl;
```

------



- **异步生成应用截图并更新封面【保存到数据库】**

这里我们使用了 Java 21 的**虚⁠拟线程⁠⁠（Virtual Thread）特性，这是由 JVM 管理的轻量级线程。它的创建成本极低**（几乎无内存开销），且在执行 I/O 操作时会自动﻿让出 CPU 给其他虚拟线程，﻿﻿从而在同样的系统资源下支持百万级并发而不是⁢传统平台线程的几千级并发。

```Java
/**
 * 异步生成应用截图并更新封面
 *
 * @param appId  应用ID
 * @param appUrl 应用访问URL
 */
@Override
public void generateAppScreenshotAsync(Long appId, String appUrl) {
    // 使用虚拟线程异步执行
    Thread.startVirtualThread(() -> {
        // 调用截图服务生成截图并上传
        String screenshotUrl = screenshotService.generateAndUploadScreenshot(appUrl);

        // 更新应用封面字段
        App updateApp = new App();
        updateApp.setId(appId);
        updateApp.setCover(screenshotUrl);
        boolean updated = this.updateById(updateApp);
        ThrowUtils.throwIf(!updated, ErrorCode.OPERATION_ERROR, "更新应用封面字段失败");
    });
}
```

------









# *AI智能路由

目前我们**平台提⁠⁠⁠供了 3 套不同的代码生成方案：原生 HTML、原生﻿﻿﻿多文件、Vue 工程**。分别⁢⁢⁢适合不同复杂度的项目需求，‍‍‍也使用了成本不同的大模型。

那么问题来⁠⁠⁠了，当用户提出需求时，如何判﻿断应﻿该使﻿用哪套⁢方案呢⁢？

让用户自己选择的话，会增加用户的使用门槛。更好的方案是让 AI 来自动判断，这就是所谓的 **智能路由**。

<img src="./AI零代码生成平台.assets/5862Jm82YJYke6Qd.webp" alt="image.png" style="zoom:50%;" />

------



1. 首先将提示词保存到 `resources/prompt/codegen-routing-system-prompt.txt` 文件中

```
你是一个专业的代码生成方案路由器，需要根据用户需求返回最合适的代码生成类型。

可选的代码生成类型：
1. HTML - 适合简单的静态页面，单个 HTML 文件，包含内联 CSS 和 JS
2. MULTI_FILE - 适合简单的多文件静态页面，分离 HTML、CSS、JS 代码
3. VUE_PROJECT - 适合复杂的现代化前端项目

判断规则：
- 如果用户需求简单，只需要一个展示页面，选择 HTML
- 如果用户需要多个页面但不涉及复杂交互，选择 MULTI_FILE
- 如果用户需求复杂，涉及多页面、复杂交互、数据管理等，选择 VUE_PROJECT
```

------



2. **在 `ai` 包下新建 AI 智能路由服务，也是一个 AI Service：**

```Java
/**
 * AI代码生成类型路由服务工厂
 *
 * @author yupi
 */
@Slf4j
@Configuration
public class AiCodeGenTypeRoutingServiceFactory {

    // 这里使用的 chatModel 模型是最开始生成 html 的模型，不是后面定义的生成多文件的流式模型 openAiStreamingChatModel 和生成 Vue 工程的流式模型 reasoningStreamingChatModel
    @Resource
    private ChatModel chatModel;

    /**
     * 创建AI代码生成类型路由服务实例
     */
    @Bean
    public AiCodeGenTypeRoutingService aiCodeGenTypeRoutingService() {
        return AiServices.builder(AiCodeGenTypeRoutingService.class)
                .chatModel(chatModel)
                .build();
    }
}
```

------



3. **在创建 App 的时候添加上 AI 智能路由的方法：**

```Java
@Override
public Long createApp(AppAddRequest appAddRequest, User loginUser) {
    // 参数校验
    String initPrompt = appAddRequest.getInitPrompt();
    ThrowUtils.throwIf(StrUtil.isBlank(initPrompt), ErrorCode.PARAMS_ERROR, "初始化 prompt 不能为空");
    
    // 构造入库对象
    App app = new App();
    BeanUtil.copyProperties(appAddRequest, app);
    app.setUserId(loginUser.getId());
    
    // 应用名称暂时为 initPrompt 前 12 位
    app.setAppName(initPrompt.substring(0, Math.min(initPrompt.length(), 12)));
    
    // ============================================新增部分==============================================
    // 使用 AI 智能选择代码生成类型
    CodeGenTypeEnum selectedCodeGenType = aiCodeGenTypeRoutingService.routeCodeGenType(initPrompt);
    app.setCodeGenType(selectedCodeGenType.getValue());
    // =================================================================================================
    
    // 插入数据库
    boolean result = this.save(app);
    ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
    log.info("应用创建成功，ID: {}, 类型: {}", app.getId(), selectedCodeGenType.getValue());
    return app.getId();
}
```

------









# 可视化修改

让用户能够﻿﻿﻿﻿﻿﻿﻿﻿﻿﻿﻿﻿﻿通过点击选择网页元素，**结合 AI ⁢⁢⁢⁢⁢⁢⁢⁢⁢⁢⁢⁢⁢提示词来精确修改生成的网站应用**，更‍‍‍‍‍‍‍‍‍‍‍‍‍直观地对生成的网站进行个性化定制。

------

开发具体流程是：

1. 用户开启编辑模式，**选中网页元素**
2. 前端获取用户**选中的元素信息，将其关联到提示词中**，并发送给后端
3. 后端调用 AI 进行修改，**让 AI 自己判断如何修改**并返回结果。

------



原本在后端的逻辑是根据正则表达式去识别对应的 <html> 代码块然后进行修改，因此会出现以下问题：

<img src="./AI零代码生成平台.assets/YviC1BquIomVGx6R.webp" alt="img" style="zoom: 25%;" />

- **后端错误地解析了第一个﻿﻿﻿﻿﻿﻿﻿﻿﻿﻿代码﻿块，﻿直接﻿⁢⁢⁢⁢⁢⁢⁢⁢⁢⁢用要修⁢改的内⁢容‍‍‍‍‍‍‍‍‍‍替换⁢了‍整个网站‍！**

<img src="./AI零代码生成平台.assets/bbrbJcoz32R4GVbd.webp" alt="img" style="zoom:33%;" />

------







## 全量修改---提示词优化

AI 会重新生成完整的‍‍‍‍‍‍‍‍‍‍‍‍‍文件内容，不需要使用额外的工具。



- **在原本的 html 系统提示词中新增如下内容：**

```
特别注意：在生成代码后，用户可能会提出修改要求并给出要修改的元素信息。
1. 你必须严格按照要求修改，不要额外修改用户要求之外的元素和内容
2. 确保始终最多输出 1 个 HTML 代码块，里面包含了完整的页面代码（而不是要修改的部分代码）。
3. 一定不能输出超过 1 个代码块，否则会导致保存错误！
```

- **多文件提示词修改：**

```
特别注意：在生成代码后，用户可能会提出修改要求并给出要修改的元素信息。
1. 你必须严格按照要求修改，不要额外修改用户要求之外的元素和内容
2. 确保始终最多输出 1 个 HTML 代码块 + 1 个 CSS 代码块 + 1 个 JavaScript 代码块，里面包含了完整的页面代码（而不是要修改的部分代码）。
3. 每种语言的代码块一定不能输出超过 1 个，否则会导致保存错误！
```

------







## 增量修改

- **方案设计：**

对于 Vue 工程⁠⁠⁠⁠⁠⁠⁠⁠⁠⁠⁠⁠⁠项目生成，代码量往往很大，每次修改都从零开始完整返回所有文件的内容是﻿﻿﻿﻿﻿﻿﻿﻿﻿﻿﻿﻿不现﻿实的。我们可以**利用 AI 的工⁢⁢⁢⁢⁢⁢⁢⁢⁢⁢⁢⁢⁢具调用能力，提供给 AI 一系列工‍‍‍‍‍‍‍‍‍‍‍‍‍具，让它能够进行精确地增量修改。**

我们需要为⁠⁠⁠⁠⁠⁠⁠⁠⁠ AI 提供以下工具，每个工具单独一个类﻿﻿﻿﻿﻿﻿﻿﻿﻿：         ⁢⁢⁢⁢⁢⁢⁢⁢⁢          ‍‍‍‍‍‍‍‍‍             

1. 读取单个文件，让 AI 能够查看现有代码的内容
2. 递归获取某个目录下所有文件结构，帮助 AI 了解项目组织
3. 删除单个文件，用于清理不需要的文件
4. 修改单个文件，支持用指定的新内容替换指定的旧内容
5. 创建单个文件（之前已经实现）

------



- **提示词修改：**

```
## 特别注意

在生成代码后，用户可能会提出修改要求并给出要修改的元素信息。
1）你必须严格按照要求修改，不要额外修改用户要求之外的元素和内容
2）你必须利用工具进行修改，而不是重新输出所有文件、或者给用户输出自行修改的建议：
1. 首先使用【目录读取工具】了解当前项目结构
2. 使用【文件读取工具】查看需要修改的文件内容
3. 根据用户需求，使用对应的工具进行修改：
- 【文件修改工具】：修改现有文件的部分内容
- 【文件写入工具】：创建新文件或完全重写文件
- 【文件删除工具】：删除不需要的文件
```

------







### 工具开发（举例）

- **文件修改工具：**

```Java
/**
 * 文件修改工具
 * 支持 AI 通过工具调用的方式修改文件内容
 *
 * 作用：
 * 1. 根据传入的文件相对路径或绝对路径定位目标文件
 * 2. 在文件中查找指定的旧内容
 * 3. 使用新内容替换旧内容
 * 4. 将修改后的内容重新写回文件
 */
@Slf4j
public class FileModifyTool {

    /**
     * 修改文件内容，用新内容替换指定的旧内容
     *
     * @param relativeFilePath 文件的相对路径
     *                         - 如果传入的是绝对路径，则直接使用
     *                         - 如果传入的是相对路径，则会自动拼接到当前应用对应的项目根目录下
     * @param oldContent       要被替换的旧内容
     * @param newContent       替换后的新内容
     * @param appId            当前应用 ID，用于定位该应用生成代码所在的项目目录
     * @return 返回处理结果信息：
     *         - 文件修改成功
     *         - 文件不存在
     *         - 未找到要替换的内容
     *         - 替换后内容未变化
     *         - 或具体异常信息
     */
    @Tool("修改文件内容，用新内容替换指定的旧内容")
    public String modifyFile(
            @P("文件的相对路径")
            String relativeFilePath,
            @P("要替换的旧内容")
            String oldContent,
            @P("替换后的新内容")
            String newContent,
            @ToolMemoryId Long appId
    ) {
        try {
            // 1. 先根据传入路径创建 Path 对象
            Path path = Paths.get(relativeFilePath);

            // 2. 如果传入的不是绝对路径，则说明是相对路径
            //    这里会根据 appId 拼接出当前应用对应的项目根目录
            if (!path.isAbsolute()) {
                // 约定项目目录名称，例如：vue_project_1001
                String projectDirName = "vue_project_" + appId;

                // 拼接项目根目录，例如：代码输出根目录/tmp/code_output/vue_project_1001
                Path projectRoot = Paths.get(AppConstant.CODE_OUTPUT_ROOT_DIR, projectDirName);

                // 将相对路径补全为绝对路径
                path = projectRoot.resolve(relativeFilePath);
            }

            // 3. 校验目标路径是否存在，并且必须是普通文件
            //    如果文件不存在，或者路径不是文件（例如目录），则直接返回错误信息
            if (!Files.exists(path) || !Files.isRegularFile(path)) {
                return "错误：文件不存在或不是文件 - " + relativeFilePath;
            }

            // 4. 读取文件原始内容
            String originalContent = Files.readString(path);

            // 5. 检查文件中是否包含要替换的旧内容
            //    如果没找到，说明无法进行替换，直接返回警告信息
            if (!originalContent.contains(oldContent)) {
                return "警告：文件中未找到要替换的内容，文件未修改 - " + relativeFilePath;
            }

            // 6. 执行字符串替换操作
            //    会将文件中所有匹配到的 oldContent 替换为 newContent
            String modifiedContent = originalContent.replace(oldContent, newContent);

            // 7. 如果替换前后内容相同，说明虽然执行了 replace，
            //    但文件实际内容没有发生变化，此时返回提示信息
            if (originalContent.equals(modifiedContent)) {
                return "信息：替换后文件内容未发生变化 - " + relativeFilePath;
            }

            // 8. 将替换后的内容重新写回文件
            //    CREATE：如果文件不存在则创建
            //    TRUNCATE_EXISTING：如果文件已存在，则先清空原内容再写入新内容
            Files.writeString(path, modifiedContent, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);

            // 9. 记录成功日志，便于后续排查问题
            log.info("成功修改文件: {}", path.toAbsolutePath());

            // 10. 返回修改成功信息
            return "文件修改成功: " + relativeFilePath;
        } catch (IOException e) {
            // 11. 如果读写文件过程中发生异常，则记录错误日志并返回失败信息
            String errorMessage = "修改文件失败: " + relativeFilePath + ", 错误: " + e.getMessage();
            log.error(errorMessage, e);
            return errorMessage;
        }
    }
}
```

------



- **使用工具：**

修改创建 AI Service 的工厂类 `AiCodeGeneratorServiceFactory`，为 Vue 项目模式补充更多工具：

```Java
// Vue 项目生成使用推理模型
case VUE_PROJECT -> AiServices.builder(AiCodeGeneratorService.class)
        // 指定为设定的推理流式模型
        .streamingChatModel(reasoningStreamingChatModel)
        // 给 AI 服务提供 “对话记忆对象” 的获取方式。
        .chatMemoryProvider(memoryId -> chatMemory)
    
    	// ================================修改点=======================================
        .tools(new FileWriteTool(),
                new FileReadTool(),
                new FileModifyTool(),
                new FileDirReadTool(),
                new FileDeleteTool())
    	// ============================================================================

        // 你明明只注册了某些工具
        // 但大模型在调用工具时，可能“想象”出一个根本不存在的工具
        // 这时框架就会走这个策略 “.hallucinatedToolNameStrategy()”，告诉模型：这个工具不存在
        .hallucinatedToolNameStrategy(toolExecutionRequest -> ToolExecutionResultMessage.from(
                toolExecutionRequest, "Error: there is no tool called " + toolExecutionRequest.name()
        ))
        .build();
```

------







### 工具信息优化

现在调用工具的时候并不会输出工具调用的具体信息，为了提供更好的⁠⁠⁠⁠⁠⁠⁠⁠⁠⁠⁠⁠⁠用户体验，**每个工具的参数和输出信息都应该有所区别**。比﻿﻿﻿﻿﻿﻿﻿如**修改文件工具，应该同﻿﻿﻿﻿﻿﻿时展⁢⁢⁢⁢⁢⁢⁢示修改的文件相对路径、被替‍‍‍‍⁢⁢⁢⁢⁢⁢‍‍‍换的旧内容、替换后的新内容**

------

- 设计模式优化：

如果在处理 AI 流的⁠代码中⁠⁠⁠⁠⁠⁠⁠⁠⁠⁠⁠⁠，通过写 if else 来区分这些输出信息，代码可能会比较复杂。因此，我们可以结﻿合**策略模式**和**工厂模式**的思路，**每﻿﻿﻿﻿﻿﻿﻿﻿﻿﻿﻿﻿个工具类就像一⁢个策略，提供了输出不同工具调用信息的方法**；⁢⁢⁢⁢⁢⁢⁢⁢⁢⁢⁢⁢还‍需要一个工厂来创建和管理这些工具

<img src="https://pic.code-nav.cn/course_picture/1608440217629360130/WSQYsDrrrUTYt1Ta.svg" alt="img" style="zoom: 67%;" />

------



1. **创建工具基类【定义所有工具必须实现的方法】**

```Java
/**
 * 工具基类
 * 定义所有工具的通用接口
 */
public abstract class BaseTool {

    
    /**
     * 获取工具的英文名称（对应方法名）
     *
     * @return 工具英文名称
     */
    public abstract String getToolName();

    
    /**
     * 获取工具的中文显示名称
     *
     * @return 工具中文名称
     */
    public abstract String getDisplayName();

    
    /**
     * 生成工具请求时的返回值（显示给用户）
     *
     * @return 工具请求显示内容
     */
    public String generateToolRequestResponse() {
        return String.format("\n\n[选择工具] %s\n\n", getDisplayName());
    }

    
    /**
     * 生成工具执行结果格式（用途是给数据库存储用的）
     *
     * @param arguments 工具执行参数
     * @return 格式化的工具执行结果
     */
    public abstract String generateToolExecutedResult(JSONObject arguments);
}
```

------



2. **具体工具优化【以文件删除工具为例】**

每个工具类都要⁠⁠⁠⁠⁠⁠⁠⁠⁠⁠⁠⁠⁠继承基类，并实现自定义处理逻辑。可以把每个工具都定义为 ﻿﻿﻿﻿﻿﻿﻿﻿﻿﻿﻿﻿﻿Spring Boot 的 ⁢⁢⁢⁢⁢⁢⁢⁢⁢⁢⁢⁢⁢Bean，采用自动注入的方式‍‍‍‍‍‍‍‍‍‍‍‍‍，便于项目启动时统一注册。

```Java
/**
 * 文件删除工具
 * 支持 AI 通过工具调用的方式删除文件
 */
@Slf4j
@Component
public class FileDeleteTool extends BaseTool {

    // 核心方法不变，此处省略

    @Override
    public String getToolName() {
        return "deleteFile";
    }

    @Override
    public String getDisplayName() {
        return "删除文件";
    }

    // 拼接工具调用文本
    @Override
    public String generateToolExecutedResult(JSONObject arguments) {
        String relativeFilePath = arguments.getStr("relativeFilePath");
        return String.format("[工具调用] %s %s", getDisplayName(), relativeFilePath);
    }
}
```

------



3. **工具管理类【自动注册所有的工具 Bean】**

```Java
/**
 * 工具管理器
 * 统一管理所有工具，提供根据名称获取工具的功能
 */
@Slf4j
@Component
public class ToolManager {

    /**
     * 工具名称到工具实例的映射
     */
    private final Map<String, BaseTool> toolMap = new HashMap<>();

    /**
     * 自动注入所有工具
     */
    @Resource
    private BaseTool[] tools;

    /**
     * 初始化工具映射
     */
    @PostConstruct
    public void initTools() {
        for (BaseTool tool : tools) {
            toolMap.put(tool.getToolName(), tool);
            log.info("注册工具: {} -> {}", tool.getToolName(), tool.getDisplayName());
        }
        log.info("工具管理器初始化完成，共注册 {} 个工具", toolMap.size());
    }

    /**
     * 根据工具名称获取工具实例
     *
     * @param toolName 工具英文名称
     * @return 工具实例
     */
    public BaseTool getTool(String toolName) {
        return toolMap.get(toolName);
    }

    /**
     * 获取已注册的工具集合
     *
     * @return 工具实例集合
     */
    public BaseTool[] getAllTools() {
        return tools;
    }
}
```

------



4. **修改 `Ai⁠⁠⁠⁠⁠⁠⁠⁠⁠⁠⁠⁠⁠CodeGeneratorService﻿﻿﻿﻿﻿﻿﻿Factory`，通过⁢⁢﻿﻿﻿﻿﻿﻿⁢⁢⁢⁢⁢ `toolManag‍‍‍‍‍‍‍e⁢⁢⁢⁢⁢⁢r` 注入所有工具：**

```java
@Resource
private ToolManager toolManager;

// Vue 项目生成使用推理模型
case VUE_PROJECT -> AiServices.builder(AiCodeGeneratorService.class)
        .streamingChatModel(reasoningStreamingChatModel)
        .chatMemoryProvider(memoryId -> chatMemory)
    
    	// =========================================修改点======================================
        .tools(toolManager.getAllTools())
    	// ====================================================================================
    
        .hallucinatedToolNameStrategy(toolExecutionRequest -> ToolExecutionResultMessage.from(
                toolExecutionRequest, "Error: there is no tool called " + toolExecutionRequest.name()
        ))
        .build();
```

------



5. **修改 `JsonMessageStreamHandler` 消息流处理器**

最后修改流处理⁠⁠⁠⁠⁠⁠⁠⁠⁠⁠⁠⁠⁠逻辑，**从 AI 响应中获取到执行的工具名称，然后通过 `T﻿﻿﻿﻿﻿﻿﻿﻿﻿﻿﻿﻿oolManage﻿r` 获取到⁢⁢⁢⁢⁢⁢⁢⁢⁢⁢⁢⁢对应的工具实例**，并通过调用方法⁢‍‍‍‍‍‍‍‍‍‍来‍‍输出信息。

```java
@Resource
private ToolManager toolManager;

case TOOL_REQUEST -> {
    ToolRequestMessage toolRequestMessage = JSONUtil.toBean(chunk, ToolRequestMessage.class);
    String toolId = toolRequestMessage.getId();
    String toolName = toolRequestMessage.getName();
    // 检查是否是第一次看到这个工具 ID
    if (toolId != null && !seenToolIds.contains(toolId)) {
        // 第一次调用这个工具，记录 ID 并返回工具信息
        seenToolIds.add(toolId);
        
        // =======================================修改点===========================================
        // 根据工具名称获取工具实例
        BaseTool tool = toolManager.getTool(toolName);
        // 返回格式化的工具调用信息
        return tool.generateToolRequestResponse();
        // ========================================================================================
        
    } else {
        // 不是第一次调用这个工具，直接返回空
        return "";
    }
}
case TOOL_EXECUTED -> {
    ToolExecutedMessage toolExecutedMessage = JSONUtil.toBean(chunk, ToolExecutedMessage.class);
    String toolName = toolExecutedMessage.getName();
    JSONObject jsonObject = JSONUtil.parseObj(toolExecutedMessage.getArguments());
    
    // =======================================修改点===========================================
    // 根据工具名称获取工具实例并生成相应的结果格式
    BaseTool tool = toolManager.getTool(toolName);
    String result = tool.generateToolExecutedResult(jsonObject);
    // ========================================================================================
    
    // 输出前端和要持久化的内容
    String output = String.format("\n\n%s\n\n", result);
    chatHistoryStringBuilder.append(output);
    return output;
}
```

------









# LangGraph4j

## StateGraph 工作流图

`State﻿Gr﻿aph` 是主⁢要使用⁢的核心﻿﻿﻿﻿﻿类，‍用于定义‍应用程序的结构。

它将复杂的 AI 工作流抽象为一个 **有向图** 的概念。每个 **节点** 代表 **一个具体的操作单元**，比如调用 LLM 生成文本、搜索外部数据、处理用户输入等。节点之间通过 **边** 来连接，多个节点之间通过 **状态** 来共享数据，形成了一个完整的处理流程。

和传统的有向无环⁠⁠⁠⁠⁠⁠⁠图（DAG）不同，`LangGraph4j` 支持循环，比如**一个智能﻿﻿﻿﻿﻿﻿﻿体可能需要根据结果回到之前的步骤⁢⁢⁢⁢⁢⁢⁢进行重试，或者需要在某个条件满足‍‍‍‍‍‍‍之前持续循环执行某个逻辑。**

------





## AgentState 状态【共享数据】

AgentState 是整个工作流的状态载体，它本质上是一个 `Map`，在不同节点之间传递。**每个节点都可以从这个状态中读取数据，并返回对状态的更新。**

- `Schema`：**状态的结构通过 `Schema` 来定义，这是一个 `Map<String, Channel.Reducer>` 的映射**。Schema 中的每个键对应状态中的一个属性，而值则是一个 `Channel.Reducer`，用于定义如何处理对该属性的更新。（理解成数据库的表结构就好）

- 注意：**不如自己定义一﻿﻿﻿﻿个 Context ⁢⁢⁢上下文类来管理状态**，想怎么操作状态就怎么操作，⁢⁢⁢⁢不用理‍‍‍解 LangGraph4j 自己的一套语法

------





## Nodes 工作节点

节点是图的构建块，**负责执行具体的操作**。

一个节点通常是一个函数或一个 **实现了 `NodeAction<S>` （同步）或 `AsyncNodeAction<S>`（异步） 接口的类**，可以在其中编写具体的操作代码。

**节点的工作流程**：首先接收当前的 `AgentState` 状态作为输入，然后执行某些计算（**比如调用 LLM、执行工具、运行自定义业务逻辑**），最后**返回一个 `Map<String, Object>`，表示对状态的更新**。这些更新会根据 Schema 中定义的 Reducer 应用到 `AgentState` 中。

------





## Edges 边

- **普通边**：最简单的边类型，***提供从一个节点到另一个节点的无条件转换***。可以通过 `addEdge(sourceNodeName, destinationNodeName)` 来定义普通边。
- **条件边**：下一个节点是根据当前 `AgentState` 动态确定的，更加灵活。***当源节点完成后，会执行一个判断函数，这个函数接收当前状态并返回下一个要执行的节点名称***。相当于实现了 if else 分支逻辑。比如智能体决定使用工具，就跳转到 “执行工具” 节点，否则跳转到 “回复用户” 节点。
- **入口点**：可以**定义当用户输入到达时首先调用哪个节点**，同样支持条件入口点。

<img src="./AI零代码生成平台.assets/Screenshot 2026-04-07 163007.png" style="zoom: 50%;" />

------







## LangGraph4j 并发特性

- **能够同时执行多个工作节点或分支，从而提升整体性能。**

<img src="./AI零代码生成平台.assets/8r27gkXstNw3nluZ.png" alt="img" style="zoom:50%;" />

------



- ***Subgraphs 子图***

如果一个工作流图特别复杂，或者其中的一些流程需要复用，就可以考虑 [子图功能](https://langgraph4j.github.io/langgraph4j/core/subgraph/)。相当于**把一个工作流拆分成多个子模块，而且多个子图可以同时并发执行**。

<img src="./AI零代码生成平台.assets/0Yw25cCuH3aEmiML.webp" alt="img" style="zoom: 67%;" />

------









## 核心工作流开发

- 工作流开发的核心是：**节点 + 边 + 状态 + 其他特性**

------

**具体步骤：**

1. 定义工作流结构（所有工作节点先只是临时输出、也无需记录状态）
2. 定义状态
3. 定义工作节点，先通过假数据模拟状态流转
4. 开发真实的工作节点
5. 工作流中使用节点，提供完整的工作流服务

------

**项目中的工作流程：**

1. 输入原始 Prompt

2. 获取图片素材 Agent：**通过工具调用从不同的渠道获取图片**

   - 内容图片：`pexels` 网页搜索

   - 插画图片：undraw 抓取

   - 画架构图：文本绘图 + 上传到 COS

   - Logo 等设计图片：AI 生成或者 MCP

3. 提示词增强：**关联图片内容到原始提示词**

4. **智能路由 Agent**：选用哪种模式生成网站？

   - 原生 HTML

   - 原生多文件

   - Vue 工程

5. 网站生成 Agent：利用搜集到的图片，根据上一步确认的生成模式来生成网站

6. 项目构建器：文件保存 / 打包构建

<img src="./AI零代码生成平台.assets/TXRVc36FSrQ6wZ4K.webp" alt="img" style="zoom: 50%;" />

------



- **简化版的工作流结构代码**

关键点：

1. 基于 `MessagesStateGraph` 的工作流，**`.addNode()` 添加工作流节点，`.addEdge()` 添加将每个工作流连接起来的边**
2. `workflow.getGraph(GraphRepresentation.Type.MERMAID)` 指定当前工作流的**图结构表示为 `MERMAID`**
3. **`return Map.of("messages", message)` 表示返回节点执行后的结果**，这里相当于把当前节点的输出写入状态中

```Java
/**
 * 简化版网站生成工作流应用
 *
 * 这段代码演示了如何使用 LangGraph4j 构建一个最基础的线性工作流。
 *
 * 整个工作流的执行顺序为：
 * 开始 -> 获取图片素材 -> 增强提示词 -> 智能路由选择 -> 网站代码生成 -> 项目构建 -> 结束
 */
@Slf4j
public class SimpleWorkflowApp {

    /**
     * 创建一个通用的工作节点
     *
     * 作用：
     * 1. 打印当前节点执行日志
     * 2. 返回当前节点产生的消息内容
     *
     * 这里为了简化代码，把多个节点的公共逻辑提取成了一个方法。
     * 只需要传入不同的 message，就能快速创建不同的节点。
     *
     * @param message 当前节点要输出的消息
     * @return 异步节点动作
     */
    static AsyncNodeAction<MessagesState<String>> makeNode(String message) {
        return node_async(state -> {
            // 打印当前执行到哪个节点
            log.info("执行节点: {}", message);

            // 返回节点执行后的结果
            // "messages" 是 MessagesState 中的核心字段
            // 这里相当于把当前节点的输出写入状态中
            return Map.of("messages", message);
        });
    }

    /**
     * 程序入口
     *
     * 主要流程：
     * 1. 创建工作流图
     * 2. 添加各个节点
     * 3. 定义节点之间的执行顺序
     * 4. 编译为可执行工作流
     * 5. 输出流程图结构
     * 6. 按步骤执行整个工作流
     *
     * @param args 启动参数
     * @throws GraphStateException 图状态异常
     */
    public static void main(String[] args) throws GraphStateException {

        // 创建并编译一个基于 MessagesState 的工作流
        CompiledGraph<MessagesState<String>> workflow = new MessagesStateGraph<String>()

                // 添加节点：图片收集
                .addNode("image_collector", makeNode("获取图片素材"))
                // 添加节点：提示词增强
                .addNode("prompt_enhancer", makeNode("增强提示词"))
                // 添加节点：智能路由
                .addNode("router", makeNode("智能路由选择"))
                // 添加节点：代码生成
                .addNode("code_generator", makeNode("网站代码生成"))
                // 添加节点：项目构建
                .addNode("project_builder", makeNode("项目构建"))

                // 添加边：开始 -> 图片收集
                .addEdge(START, "image_collector")
                // 添加边：图片收集 -> 提示词增强
                .addEdge("image_collector", "prompt_enhancer")
                // 添加边：提示词增强 -> 智能路由
                .addEdge("prompt_enhancer", "router")
                // 添加边：智能路由 -> 代码生成
                .addEdge("router", "code_generator")
                // 添加边：代码生成 -> 项目构建
                .addEdge("code_generator", "project_builder")
                // 添加边：项目构建 -> 结束
                .addEdge("project_builder", END)

                // 编译工作流
                // compile() 的作用是把前面定义好的节点和边转换成真正可执行的图对象
                .compile();

        log.info("开始执行工作流");

        // 获取当前工作流的图结构表示
        // 这里使用 Mermaid 格式，方便后续可视化展示
        GraphRepresentation graph = workflow.getGraph(GraphRepresentation.Type.MERMAID);

        // 打印 Mermaid 格式的流程图文本
        log.info("工作流图: \n{}", graph.content());

        // 记录当前执行到第几步
        int stepCounter = 1;
        // 开始执行工作流
        // workflow.stream(Map.of()) 表示以一个空状态作为初始输入，逐步执行整个流程
        for (NodeOutput<MessagesState<String>> step : workflow.stream(Map.of())) {
            log.info("--- 第 {} 步完成 ---", stepCounter);
            // 打印当前步骤的输出结果
            // step 中包含当前节点执行后的状态信息
            log.info("步骤输出: {}", step);
            stepCounter++;
        }

        log.info("工作流执行完成！");
    }
}
```

------

<img src="https://pic.code-nav.cn/course_picture/1608440217629360130/UOkQekjuiEaQnxCP.svg" alt="img" style="zoom: 80%;" />

------







### 1.定义状态

需要 **将各个工作节点中共享的数据定义为状态**。

| 工作步骤   | 输入状态                                         | 输出状态                                                     |
| ---------- | ------------------------------------------------ | ------------------------------------------------------------ |
| 图片收集   | `originalPrompt` 原始提示词                      | images 图片资源列表每一个图片都应该是对象结构（图片类别、描述、地址）图片类别：content 内容图片`URLsillustration` 插画图片`URLsarchitecture` 架构图 `URLlogo` Logo图片 |
| 提示词增强 | `originalPrompt` 原始提示⁠⁠⁠词 images 图片资源      | `enhancedPrompt` 增强后的提示词，包含图片描述和引用          |
| 智能路由   | `originalPromptenhancedPrompt` 增强后的提示词    | `generationT﻿﻿﻿ype` 生成类型                                    |
| 代码生成   | `enhancedPromptgenerationType` 生成类型 `imag⁢⁢⁢es` | `generatedCodeDir` 生成的代码目录                            |
| 项目构建   | `generatedCodeDir‍‍‍` 生成的代码目录                | `buildResultDir` 构建成功的目录                              |

------



为了和 LangGraph4j 工作流图需要的 `AgentState` 兼容，我们可以**将 `WorkflowContext` 对象作为一个 key / value 存放在 `MessageState` 中，需要使用时通过 `state.data().getKey` 获取即可**。

<img src="./AI零代码生成平台.assets/JiS9YFUpQNZrSeHQ.webp" alt="img" style="zoom:50%;" />

------

 

- **工具流上下文**

```Java
/**
 * 工作流上下文 - 存储所有状态信息
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WorkflowContext implements Serializable {

    /**
     * WorkflowContext 在 MessagesState 中的存储key
     */
    public static final String WORKFLOW_CONTEXT_KEY = "workflowContext";

    /**
     * 当前执行步骤
     */
    private String currentStep;

    /**
     * 用户原始输入的提示词
     */
    private String originalPrompt;

    /**
     * 图片资源字符串
     */
    private String imageListStr;

    /**
     * 图片资源列表
     */
    private List<ImageResource> imageList;

    /**
     * 增强后的提示词
     */
    private String enhancedPrompt;

    /**
     * 代码生成类型
     */
    private CodeGenTypeEnum generationType;

    /**
     * 生成的代码目录
     */
    private String generatedCodeDir;

    /**
     * 构建成功的目录
     */
    private String buildResultDir;

    /**
     * 错误信息
     */
    private String errorMessage;

    @Serial
    private static final long serialVersionUID = 1L;

    // ========== 上下文操作方法 ==========

    /**
     * 从 MessagesState 中获取 WorkflowContext
     */
    public static WorkflowContext getContext(MessagesState<String> state) {
        return (WorkflowContext) state.data().get(WORKFLOW_CONTEXT_KEY);
    }

    /**
     * 将 WorkflowContext 保存到 MessagesState 中
     */
    public static Map<String, Object> saveContext(WorkflowContext context) {
        return Map.of(WORKFLOW_CONTEXT_KEY, context);
    }
}
```

------







### 2.修改工作流图 --- 引入状态

注意：

1. `WorkflowContext.getContext(state)`：从当前工作流状态 state 中，取出自己定义的业务上下文【即："workflowContext"】
2. `context.setCurrentStep(nodeName)`：**把当前执行到的节点名称，记录到工作流上下文里**

​	比如当前执行：`makeStatefulNode("router", "智能路由选择")`

​	执行到这一步就会变成：`context.currentStep = "router"` 【记录了节点名称】

3. `WorkflowContext.saveContext(context)`：**把更新后的 WorkflowContext 放回状态中**

```Java
/**
 * 简化版带状态定义的工作流 - 只定义状态结构，不实现具体流转
 */
@Slf4j
public class SimpleStatefulWorkflowApp {

    // ==========================================主要修改点===========================================
    /**
     * 创建带状态感知的工作节点
     */
    static AsyncNodeAction<MessagesState<String>> makeStatefulNode(String nodeName, String message) {
        return node_async(state -> {
            // 从当前工作流状态 state 中，取出自己定义的业务上下文【即："workflowContext"】
            WorkflowContext context = WorkflowContext.getContext(state);
            log.info("执行节点: {} - {}", nodeName, message);
            // 只记录当前步骤，不做具体的状态流转
            if (context != null) {
                // 把当前执行到的节点名称，记录到工作流上下文里。
                context.setCurrentStep(nodeName);
            }
            // 把更新后的 WorkflowContext 放回状态中
            return WorkflowContext.saveContext(context);
        });
    }

    public static void main(String[] args) throws GraphStateException {
        // 创建工作流图
        CompiledGraph<MessagesState<String>> workflow = new MessagesStateGraph<String>()
                // 添加节点 - 使用带状态感知的节点
                .addNode("image_collector", makeStatefulNode("image_collector", "获取图片素材"))
                .addNode("prompt_enhancer", makeStatefulNode("prompt_enhancer", "增强提示词"))
                .addNode("router", makeStatefulNode("router", "智能路由选择"))
                .addNode("code_generator", makeStatefulNode("code_generator", "网站代码生成"))
                .addNode("project_builder", makeStatefulNode("project_builder", "项目构建"))

                // 添加边
                .addEdge(START, "image_collector")
                .addEdge("image_collector", "prompt_enhancer")
                .addEdge("prompt_enhancer", "router")
                .addEdge("router", "code_generator")
                .addEdge("code_generator", "project_builder")
                .addEdge("project_builder", END)

                // 编译工作流
                .compile();

        // 初始化 WorkflowContext - 只设置基本信息
        WorkflowContext initialContext = WorkflowContext.builder()
                .originalPrompt("创建一个鱼皮的个人博客网站")
                .currentStep("初始化")
                .build();

        log.info("初始输入: {}", initialContext.getOriginalPrompt());
        log.info("开始执行工作流");

        // 显示工作流图
        GraphRepresentation graph = workflow.getGraph(GraphRepresentation.Type.MERMAID);
        log.info("工作流图:\n{}", graph.content());

        // 执行工作流
        int stepCounter = 1;
        for (NodeOutput<MessagesState<String>> step : workflow.stream(Map.of(WorkflowContext.WORKFLOW_CONTEXT_KEY, initialContext))) {
            log.info("--- 第 {} 步完成 ---", stepCounter);
            // 显示当前状态
            WorkflowContext currentContext = WorkflowContext.getContext(step.state());
            if (currentContext != null) {
                log.info("当前步骤上下文: {}", currentContext);
            }
            stepCounter++;
        }
        log.info("工作流执行完成！");
    }
}
```

------







### 3.定义工作节点

在 `langgraph4j.node` 包下新建每一个步骤对应的工作节点，**每个节点中只需要 Mock 一些假数据来模拟状态流转，不用真正实现业务逻辑。**

- **以提示词增强器节点为例：**

```Java
@Slf4j
public class PromptEnhancerNode {
    public static AsyncNodeAction<MessagesState<String>> create() {
        return node_async(state -> {
            WorkflowContext context = WorkflowContext.getContext(state);
            log.info("执行节点: 提示词增强");
            
            // TODO: 实际执行提示词增强逻辑
            
            // 简单的假数据
            String enhancedPrompt = "这是增强后的假数据提示词";
            
            // 更新状态
            context.setCurrentStep("提示词增强");
            context.setEnhancedPrompt(enhancedPrompt);
            log.info("提示词增强完成");
            return WorkflowContext.saveContext(context);
        });
    }
}
```

------



- **修改原本的工作流图代码，将那些假节点修改为真正定义的工作节点**

原本的节点是通过 `makeStatefulNode(...)` 统一生成的，现在每个节点都已经有自己独立的真实实现了

```Java
@Slf4j
public class WorkflowApp {

    public static void main(String[] args) throws GraphStateException {
        // 创建工作流图
        CompiledGraph<MessagesState<String>> workflow = new MessagesStateGraph<String>()
            
                // 添加节点 - 使用真实的工作节点
                .addNode("image_collector", ImageCollectorNode.create())
                .addNode("prompt_enhancer", PromptEnhancerNode.create())
                .addNode("router", RouterNode.create())
                .addNode("code_generator", CodeGeneratorNode.create())
                .addNode("project_builder", ProjectBuilderNode.create())
            
                // 添加边
                .addEdge(START, "image_collector")
                .addEdge("image_collector", "prompt_enhancer")
                .addEdge("prompt_enhancer", "router")
                .addEdge("router", "code_generator")
                .addEdge("code_generator", "project_builder")
                .addEdge("project_builder", END)
            
                // 编译工作流
                .compile();

        // 初始化 WorkflowContext - 只设置基本信息
        WorkflowContext initialContext = WorkflowContext.builder()
                .originalPrompt("创建一个鱼皮的个人博客网站")
                .currentStep("初始化")
                .build();
        log.info("初始输入: {}", initialContext.getOriginalPrompt());
        log.info("开始执行工作流");

        // 显示工作流图
        GraphRepresentation graph = workflow.getGraph(GraphRepresentation.Type.MERMAID);
        log.info("工作流图:\n{}", graph.content());

        // 执行工作流
        int stepCounter = 1;
        for (NodeOutput<MessagesState<String>> step : workflow.stream(Map.of(WorkflowContext.WORKFLOW_CONTEXT_KEY, initialContext))) {
            log.info("--- 第 {} 步完成 ---", stepCounter);
            // 显示当前状态
            WorkflowContext currentContext = WorkflowContext.getContext(step.state());
            if (currentContext != null) {
                log.info("当前步骤上下文: {}", currentContext);
            }
            stepCounter++;
        }
        log.info("工作流执行完成！");
    }
}
```

------







### 4.节点开发细则

#### 4.1 架构图绘制工具

由于架构图是需⁠⁠⁠要根据特定的描述来定制的，不能直接上网搜索，因此我们﻿﻿﻿的思路是**将 AI 调用工具⁢⁢⁢时传入的 Mermaid ‍‍‍文本绘图代码转换成图片**。

- `mermaid-cli + COS`：**先利用 Mermaid CLI 工具将文本绘图代码转换为图片，之后上传到 COS 对象存储拿到对应的 URL 地址方便后续使用。**

***安装 `mermaid-cli` 工具：***

```shell
npm install -g @mermaid-js/mermaid-cli
```

------

- **工具类写法：**

```Java
@Slf4j
@Component
public class MermaidDiagramTool {

    @Resource
    private CosManager cosManager;

    /**
     * 将 Mermaid 代码转换为架构图图片，用于展示系统结构和技术关系
     * @param mermaidCode AI 生成的图表代码
     * @param description 架构图的解释
     * @return 虽然架构图只生成一张，但是这里使用 List 是为了图片的统一返回，哪怕这里图片保存失败了，返回空列表，不会影响收集其它类型的图片
     */
    @Tool("将 Mermaid 代码转换为架构图图片，用于展示系统结构和技术关系")
    public List<ImageResource> generateMermaidDiagram(@P("Mermaid 图表代码") String mermaidCode,
                                                      @P("架构图描述") String description) {
        if (StrUtil.isBlank(mermaidCode)) {
            return new ArrayList<>();
        }
        try {
            // 转换为SVG图片
            File diagramFile = convertMermaidToSvg(mermaidCode);
            // 上传到COS
            String keyName = String.format("/mermaid/%s/%s",
                    RandomUtil.randomString(5), diagramFile.getName());
            String cosUrl = cosManager.uploadFile(keyName, diagramFile);
            // 清理临时文件
            FileUtil.del(diagramFile);
            if (StrUtil.isNotBlank(cosUrl)) {
                return Collections.singletonList(ImageResource.builder()
                        .category(ImageCategoryEnum.ARCHITECTURE)
                        .description(description)
                        .url(cosUrl)
                        .build());
            }
        } catch (Exception e) {
            log.error("生成架构图失败: {}", e.getMessage(), e);
        }
        return new ArrayList<>();
    }

    /**
     * 将Mermaid代码转换为SVG图片
     */
    private File convertMermaidToSvg(String mermaidCode) {
        // 创建临时输入文件
        File tempInputFile = FileUtil.createTempFile("mermaid_input_", ".mmd", true);
        FileUtil.writeUtf8String(mermaidCode, tempInputFile);
        // 创建临时输出文件
        File tempOutputFile = FileUtil.createTempFile("mermaid_output_", ".svg", true);
        // 根据操作系统选择命令
        String command = SystemUtil.getOsInfo().isWindows() ? "mmdc.cmd" : "mmdc";
        // 构建命令
        String cmdLine = String.format("%s -i %s -o %s -b transparent",
                command,
                tempInputFile.getAbsolutePath(),
                tempOutputFile.getAbsolutePath()
        );
        // 执行命令
        RuntimeUtil.execForStr(cmdLine);
        // 检查输出文件
        if (!tempOutputFile.exists() || tempOutputFile.length() == 0) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "Mermaid CLI 执行失败");
        }
        // 清理输入文件，保留输出文件供上传使用
        FileUtil.del(tempInputFile);
        return tempOutputFile;
    }
}
```

------





#### 4.2 **图片收集AI服务**

- **写一段 AI 收集图片的提示词，指定好各个工具类的定义**

```
你是一个专业的图片收集助手。根据用户的网站需求，智能选择并调用相应的工具收集不同类型的图片资源。

你可以根据需要调用下面多个工具，收集全面的图片资源：
1. searchContentImages - 搜索内容相关图片，用于网站内容展示
2. searchIllustrations - 搜索插画图片，用于网站美化和装饰  
3. generateArchitectureDiagram - 根据技术主题生成架构图，用于展示系统结构和技术关系
4. generateLogos - 根据描述生成Logo设计图片，用于网站品牌标识

请根据用户的需求分析，优先选择与用户需求最相关的图片类型：
- 如果涉及技术、系统、架构等内容，调用 generateArchitectureDiagram 生成架构图
- 如果需要品牌标识、Logo设计，调用 generateLogos 生成Logo
- 如果需要内容相关图片，调用 searchContentImages 搜索图片
- 如果需要装饰性插画，调用 searchIllustrations 搜索插画

你必须按照 JSON 格式输出！
```

------



- 然后编写图⁠⁠⁠片收集 AI 服务。理想情况﻿下 ﻿AI﻿ 服务⁢肯定是⁢采用结⁢构‍化输出：

但由于我们返回的是 `List<POJO>` 类型，这里会遇到一些坑！最终的解决方⁠案是⁠⁠使用 DeepSeek 模型，但是**不使用结构化输出能力，方法直接﻿返回 String，在后续的﻿﻿提示词⁢增强节点中直接使用 AI 输出的信‍息即⁢⁢可【因为得到的返回结果是继续传下去给 AI 来执行，让AI来拼接原始文本和新的工具调用后的文本】**，干脆就不结构化了。

```Java
public interface ImageCollectionService {

    /**
     * 根据用户提示词收集所需的图片资源
     * AI 会根据需求自主选择调用相应的工具
     */
    @SystemMessage(fromResource = "prompt/image-collection-system-prompt.txt")
    String collectImages(@UserMessage String userPrompt);
}
```

------



- **创建 ⁠⁠⁠AI 服务创建工厂，注入指定﻿的 ﻿ch﻿atM⁢ode⁢l 和⁢各‍种图片收‍集工具：**

```Java
@Slf4j
@Configuration
public class ImageCollectionServiceFactory {

    @Resource
    private ChatModel chatModel;

    @Resource
    private ImageSearchTool imageSearchTool;

    @Resource
    private UndrawIllustrationTool undrawIllustrationTool;

    @Resource
    private MermaidDiagramTool mermaidDiagramTool;

    @Resource
    private LogoGeneratorTool logoGeneratorTool;

    /**
     * 创建图片收集 AI 服务
     */
    @Bean
    public ImageCollectionService createImageCollectionService() {
        return AiServices.builder(ImageCollectionService.class)
            	// 指定大模型
                .chatModel(chatModel)
            	// 指定要调用的图片收集的各种工具类
                .tools(
                        imageSearchTool,
                        undrawIllustrationTool,
                        mermaidDiagramTool,
                        logoGeneratorTool
                )
                .build();
    }
}
```

------



**修改工作流**

- **工作节点是通过静态方法 `create()` 来创建的，而静态方法本身不属于某个 Spring 管理的对象实例，所以没法直接使用 `@Resource`、`@Autowired` 注入 `ImageCollectionService`。**
- 所以才要通过一个 `SpringContextUtil`，在静态方法里“主动去 Spring 容器里拿 Bean”。

```Java
/**
 * Spring上下文工具类
 * 用于在静态方法中获取Spring Bean
 */
@Component
public class SpringContextUtil implements ApplicationContextAware {

    private static ApplicationContext applicationContext;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        SpringContextUtil.applicationContext = applicationContext;
    }

    /**
     * 获取Spring Bean
     */
    public static <T> T getBean(Class<T> clazz) {
        return applicationContext.getBean(clazz);
    }

    /**
     * 获取Spring Bean
     */
    public static Object getBean(String name) {
        return applicationContext.getBean(name);
    }

    /**
     * 根据名称和类型获取Spring Bean
     */
    public static <T> T getBean(String name, Class<T> clazz) {
        return applicationContext.getBean(name, clazz);
    }
}
```

------

- **工作节点中的实际执行图片收集逻辑：**

```Java
@Slf4j
public class ImageCollectorNode {
    public static AsyncNodeAction<MessagesState<String>> create() {
        return node_async(state -> {
            WorkflowContext context = WorkflowContext.getContext(state);
            log.info("执行节点: 图片收集");
            
            // ===============================================================================
            // TODO: 实际执行图片收集逻辑
            // 获取到原始 prompt
            String originalPrompt = context.getOriginalPrompt();
            String imageListStr = "";
            try {
                // 获取 图片收集服务 AI Service
                ImageCollectionService imageCollectionService = SpringContextUtil.getBean(ImageCollectionService.class);
                // 使用 AI 服务进行智能图片收集
                imageListStr = imageCollectionService.collectImages(originalPrompt);
            } catch (Exception e) {
                log.error("图片收集失败: {}", e.getMessage(), e);
            }
            // ===============================================================================
            
            // 更新状态
            context.setCurrentStep("图片收集");
            context.setImageListStr(imageListStr);
            return WorkflowContext.saveContext(context);
        });
    }
}
```

------







### 5.完整工作流

```Java
/**
 * 代码生成工作流
 *
 * 作用：
 * 1. 负责构建完整的代码生成流程图
 * 2. 负责执行工作流
 * 3. 将执行过程中的上下文状态逐步传递给各个节点
 * 4. 最终返回整个流程执行完成后的 WorkflowContext
 */
@Slf4j
public class CodeGenWorkflow {

    /**
     * 创建完整的工作流
     *
     * 工作流执行顺序：
     * START
     * -> image_collector（图片收集）
     * -> prompt_enhancer（提示词增强）
     * -> router（路由选择）
     * -> code_generator（代码生成）
     * -> project_builder（项目构建）
     * -> END
     *
     * @return 编译完成后的工作流图对象
     */
    public CompiledGraph<MessagesState<String>> createWorkflow() {
        try {
            return new MessagesStateGraph<String>()
                    // 添加各个业务节点
                    // 每个节点都负责处理 WorkflowContext 中的一部分数据
                    .addNode("image_collector", ImageCollectorNode.create())
                    .addNode("prompt_enhancer", PromptEnhancerNode.create())
                    .addNode("router", RouterNode.create())
                    .addNode("code_generator", CodeGeneratorNode.create())
                    .addNode("project_builder", ProjectBuilderNode.create())

                    // 定义节点之间的执行顺序
                    .addEdge(START, "image_collector")
                    .addEdge("image_collector", "prompt_enhancer")
                    .addEdge("prompt_enhancer", "router")
                    .addEdge("router", "code_generator")
                    .addEdge("code_generator", "project_builder")
                    .addEdge("project_builder", END)

                    // 编译工作流图
                    .compile();
        } catch (GraphStateException e) {
            // 如果工作流图构建失败，则包装成系统自己的业务异常抛出
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "工作流创建失败");
        }
    }

    /**
     * 执行工作流
     *
     * 执行流程：
     * 1. 创建完整工作流
     * 2. 初始化上下文 WorkflowContext
     * 3. 将初始上下文传入工作流
     * 4. 按节点顺序逐步执行
     * 5. 每执行完一个节点，就从当前状态中取出最新的上下文
     * 6. 最终返回最后一个节点执行完成后的上下文结果
     *
     * @param originalPrompt 用户输入的原始提示词
     * @return 工作流执行完成后的最终上下文
     */
    public WorkflowContext executeWorkflow(String originalPrompt) {
        // 先创建工作流图
        CompiledGraph<MessagesState<String>> workflow = createWorkflow();

        // 初始化工作流上下文
        // 这里只放入最基础的数据：用户原始提示词 + 当前步骤
        WorkflowContext initialContext = WorkflowContext.builder()
                .originalPrompt(originalPrompt)
                .currentStep("初始化")
                .build();

        // 获取工作流图的 Mermaid 表示，方便打印查看整体结构
        GraphRepresentation graph = workflow.getGraph(GraphRepresentation.Type.MERMAID);
        log.info("工作流图:\n{}", graph.content());
        log.info("开始执行代码生成工作流");

        // finalContext 用于保存流程执行过程中的最新上下文
        // 最终返回的就是最后一步执行完成后的上下文
        WorkflowContext finalContext = null;

        // 记录当前执行到第几步，方便打印日志观察执行过程
        int stepCounter = 1;

        // workflow.stream(...) 会按节点顺序依次执行工作流
        // 这里将初始的 WorkflowContext 放入状态中，作为整个流程的起点
        for (NodeOutput<MessagesState<String>> step : workflow.stream(
                Map.of(WorkflowContext.WORKFLOW_CONTEXT_KEY, initialContext))) {

            log.info("--- 第 {} 步完成 ---", stepCounter);

            // 从当前节点执行完成后的状态中，取出最新的 WorkflowContext
            WorkflowContext currentContext = WorkflowContext.getContext(step.state());

            if (currentContext != null) {
                // 持续更新 finalContext，确保最后拿到的是最终执行结果
                finalContext = currentContext;
                log.info("当前步骤上下文: {}", currentContext);
            }

            stepCounter++;
        }

        log.info("代码生成工作流执行完成！");

        // 返回最终执行结果
        return finalContext;
    }
}
```

------

- **执行工作流的模拟**

1. 用户传入：`originalPrompt = "创建一个鱼皮的个人博客网站"`

2. 初始化工作流图之后，初始化上下文：

```Java
originalPrompt = "创建一个鱼皮的个人博客网站"
currentStep = "初始化"
```

3. 开始执行工作流

```
for (NodeOutput<MessagesState<String>> step : workflow.stream(
        Map.of(WorkflowContext.WORKFLOW_CONTEXT_KEY, initialContext))) {
```

- **把初始上下文：作为整个工作流的起点传进去，然后工作流会按顺序一个节点一个节点执行。**

```
{
    "workflowContext": initialContext
}
```



- **假设第一个节点 `image_collector` 执行完了。**

```
log.info("--- 第 {} 步完成 ---", stepCounter);
```

输出：

```
--- 第 1 步完成 ---
```

然后执行：

```Java
WorkflowContext currentContext = WorkflowContext.getContext(step.state());
```

就是从 “第一个节点执行后的状态” 里，拿出最新的 `WorkflowContext`。

假设此时拿到的是：

```Java
currentStep = "图片收集"
originalPrompt = "创建一个鱼皮的个人博客网站"
imageListStr = "收集到的图片信息"
```

然后进入：

```Java
if (currentContext != null) {
    finalContext = currentContext;
    log.info("当前步骤上下文: {}", currentContext);
}
```

此时：

- `finalContext` 被更新成“第 1 步执行后的结果”
- 日志打印当前上下文

然后：变成 2。

```java 
stepCounter++;
```

------









## LangGraph4j高级特性

### 1.条件边

对于 HTM⁠⁠L ⁠和 MULTI_FILE 网站生成类型，﻿﻿原本是使用 if-else 判断是否是 VUE 项目然后进行构建，现在只需要在条件边判断即可。

<img src="./AI零代码生成平台.assets/OMRGwjtGaFyIQNtT.webp" alt="img" style="zoom: 50%;" />

------



1）工作流新增路由函数和条件边配置：

```java
.addEdge("router", "code_generator")
    
// =================================================================================
// 使用条件边：根据代码生成类型决定是否需要构建
.addConditionalEdges("code_generator",
        edge_async(this::routeBuildOrSkip),
        Map.of(
                "build", "project_builder",  // 需要构建的情况
                "skip_build", END             // 跳过构建直接结束
        ))
// =================================================================================
    
.addEdge("project_builder", END)
```

2）路由函数决定代码生成后是否需要项目构建：

```java
private String routeBuildOrSkip(MessagesState<String> state) {
    // 通过状态拿到工具流上下文
    WorkflowContext context = WorkflowContext.getContext(state);
    // 根据 context 获取代码生成类型
    CodeGenTypeEnum generationType = context.getGenerationType();
    // HTML 和 MULTI_FILE 类型不需要构建，直接结束
    if (generationType == CodeGenTypeEnum.HTML || generationType == CodeGenTypeEnum.MULTI_FILE) {
        return "skip_build";
    }
    // VUE_PROJECT 需要构建
    return "build";
}
```

3）项目构建工作节点中移除 if-else 逻辑：NTEopWGcs5rjLSK09K9vexnEO/0dhY/ME1oYwCPOYIc=

```java
String buildResultDir;
// 一定是 Vue 项目类型：使用 VueProjectBuilder 进行构建
// 这里去掉了原先的 if-else 判断是否是 VUE 项目的逻辑
try {
    VueProjectBuilder vueBuilder = SpringContextUtil.getBean(VueProjectBuilder.class);
    // 执行 Vue 项目构建（npm install + npm run build）
    boolean buildSuccess = vueBuilder.buildProject(generatedCodeDir);
    if (buildSuccess) {
        // 构建成功，返回 dist 目录路径
        buildResultDir = generatedCodeDir + File.separator + "dist";
        log.info("Vue 项目构建成功，dist 目录: {}", buildResultDir);
    } else {
        throw new BusinessException(ErrorCode.SYSTEM_ERROR, "Vue 项目构建失败");
    }
} catch (Exception e) {
    log.error("Vue 项目构建异常: {}", e.getMessage(), e);
    buildResultDir = generatedCodeDir; // 异常时返回原路径
}
```

这种设计比在⁠⁠⁠ `ProjectBuilderNode` 中写 i﻿﻿﻿f-else 更加优雅，⁢⁢⁢符合 `LangGraph‍‍‍4j` 和工作流的设计理念。

------







### 2.并发和子图

1. 工作节点内部实现并发（推荐）

在图片收集⁠⁠⁠节点内部通过 `Completab﻿﻿le﻿Future`⁢⁢ 并发⁢调用工具进‍‍行收集，并‍更新结果。

2. 利用 LangGraph4j 的并发能力

把每个图片⁠⁠⁠收集工具定义成工作节点，并发﻿执行﻿这些﻿工具，⁢最后再⁢统一汇‍⁢总结果。

3. 利用 LangGraph4j 的子图能力

把每个图片⁠⁠⁠收集工具定义成子图，并发执行﻿这些﻿工具﻿，最后⁢再统一汇⁢⁢总结果‍。

------



**定义保存图片收集任务类**

- 内容图任务：搜“博客首页横幅”
- 插画任务：搜“程序员办公插画”
- 架构图任务：生成一张 Mermaid 架构图
- Logo 任务：生成一个技术博客 Logo

这些都只是“任务描述项”，不需要复杂行为，所以用 `record` 很合适。

```Java
@Data
public class ImageCollectionPlan implements Serializable {
    
    /**
     * 内容图片搜索任务列表
     */
    private List<ImageSearchTask> contentImageTasks;
    
    /**
     * 插画图片搜索任务列表
     */
    private List<IllustrationTask> illustrationTasks;
    
    /**
     * 架构图生成任务列表
     */
    private List<DiagramTask> diagramTasks;
    
    /**
     * Logo生成任务列表
     */
    private List<LogoTask> logoTasks;
    
    /**
     * 内容图片搜索任务
     * 对应 ImageSearchTool.searchContentImages(String query)
     */
    public record ImageSearchTask(String query) implements Serializable {}
    
    /**
     * 插画图片搜索任务
     * 对应 UndrawIllustrationTool.searchIllustrations(String query)
     */
    public record IllustrationTask(String query) implements Serializable {}
    
    /**
     * 架构图生成任务
     * 对应 MermaidDiagramTool.generateMermaidDiagram(String mermaidCode, String description)
     */
    public record DiagramTask(String mermaidCode, String description) implements Serializable {}
    
    /**
     * Logo生成任务
     * 对应 LogoGeneratorTool.generateLogos(String description)
     */
    public record LogoTask(String description) implements Serializable {}
}
```

------





#### 2.1 CompletableFuture 并发实现

直接修改图⁠⁠⁠片收集工作节点。先调用 AI 进行规划，﻿﻿﻿然后并发收集图片并汇⁢⁢⁢总，最后设置 ima‍‍‍geList 状态。

```Java
@Slf4j
public class ImageCollectorNode {

    /**
     * 创建图片收集节点
     *
     * 节点职责：
     * 1. 从工作流状态中获取用户原始提示词
     * 2. 调用图片收集计划服务，先生成一份“图片收集计划”
     * 3. 根据计划并发执行不同类型的图片收集任务
     * 4. 汇总所有收集到的图片资源
     * 5. 将结果更新回 WorkflowContext，供后续节点继续使用
     *
     * @return 异步工作节点动作
     */
    public static AsyncNodeAction<MessagesState<String>> create() {
        return node_async(state -> {
            // 从当前工作流状态中取出上下文对象
            WorkflowContext context = WorkflowContext.getContext(state);

            // 获取用户最初输入的提示词，后续会根据它来规划图片收集任务
            String originalPrompt = context.getOriginalPrompt();

            // 用于保存最终收集到的所有图片资源
            List<ImageResource> collectedImages = new ArrayList<>();

            try {
                // 第一步：先获取图片收集计划服务
                // 由于当前节点是通过静态方法创建的，不能直接注入 Bean，所以通过 SpringContextUtil 手动从 Spring 容器中获取
                ImageCollectionPlanService planService = SpringContextUtil.getBean(ImageCollectionPlanService.class);

                // 根据用户原始提示词，调用图片收集 AIService，生成图片收集计划
                // 这个计划中会包含多种任务，例如：
                // 1. 内容图片搜索任务
                // 2. 插画搜索任务
                // 3. 架构图生成任务
                // 4. Logo 生成任务
                ImageCollectionPlan plan = planService.planImageCollection(originalPrompt);
                log.info("获取到图片收集计划，开始并发执行");

                // 第二步：并发执行各种图片收集任务
                // futures 用来保存每一个异步任务，每个异步任务最终都会返回一个 List<ImageResource>
                List<CompletableFuture<List<ImageResource>>> futures = new ArrayList<>();

                // -------------------- 并发执行内容图片搜索 --------------------
                // 如果计划中存在内容图片搜索任务，则逐个创建异步任务
                if (plan.getContentImageTasks() != null) {
                    // 获取内容图片搜索工具
                    ImageSearchTool imageSearchTool = SpringContextUtil.getBean(ImageSearchTool.class);
                    // 遍历所有内容图片搜索任务
                    for (ImageCollectionPlan.ImageSearchTask task : plan.getContentImageTasks()) {
                        // 为每个任务创建一个异步执行单元
                        futures.add(CompletableFuture.supplyAsync(() ->
                                imageSearchTool.searchContentImages(task.query())));
                    }
                }

                // -------------------- 并发执行插画图片搜索 --------------------
                // 如果计划中存在插画任务，则逐个异步执行
                if (plan.getIllustrationTasks() != null) {
                    // 获取插画搜索工具
                    UndrawIllustrationTool illustrationTool = SpringContextUtil.getBean(UndrawIllustrationTool.class);
                    // 遍历所有插画任务
                    for (ImageCollectionPlan.IllustrationTask task : plan.getIllustrationTasks()) {
                        // 为每个任务创建一个异步执行单元
                        futures.add(CompletableFuture.supplyAsync(() ->
                                illustrationTool.searchIllustrations(task.query())));
                    }
                }

                // -------------------- 并发执行架构图生成 --------------------
                // 如果计划中存在 Mermaid 架构图生成任务，则逐个异步执行
                if (plan.getDiagramTasks() != null) {
                    // 获取架构图生成工具
                    MermaidDiagramTool diagramTool = SpringContextUtil.getBean(MermaidDiagramTool.class);
                    // 遍历所有架构图任务
                    for (ImageCollectionPlan.DiagramTask task : plan.getDiagramTasks()) {
                        // 每个任务都会根据 Mermaid 代码和描述生成架构图图片
                        futures.add(CompletableFuture.supplyAsync(() ->
                                diagramTool.generateMermaidDiagram(task.mermaidCode(), task.description())));
                    }
                }

                // -------------------- 并发执行 Logo 生成 --------------------
                // 如果计划中存在 Logo 生成任务，则逐个异步执行
                if (plan.getLogoTasks() != null) {
                    // 获取 Logo 生成工具
                    LogoGeneratorTool logoTool = SpringContextUtil.getBean(LogoGeneratorTool.class);
                    // 遍历所有 Logo 任务
                    for (ImageCollectionPlan.LogoTask task : plan.getLogoTasks()) {
                        // 每个任务都会根据描述生成 Logo 图片
                        futures.add(CompletableFuture.supplyAsync(() ->
                                logoTool.generateLogos(task.description())));
                    }
                }

                // 第三步：等待所有异步任务执行完成
                // CompletableFuture.allOf(...) 会把所有 future 组合成一个总任务
                // join() 表示阻塞等待，直到所有任务都执行结束
                CompletableFuture<Void> allTasks = CompletableFuture.allOf(
                        futures.toArray(new CompletableFuture[0]));
                allTasks.join();

                // 第四步：收集所有异步任务的执行结果
                // 遍历每个 future，取出其中返回的图片列表
                for (CompletableFuture<List<ImageResource>> future : futures) {
                    List<ImageResource> images = future.get();

                    // 如果当前任务返回的图片列表不为空，则追加到最终结果集中
                    if (images != null) {
                        collectedImages.addAll(images);
                    }
                }

                log.info("并发图片收集完成，共收集到 {} 张图片", collectedImages.size());
            } catch (Exception e) {
                // 这里没有直接抛异常，是为了避免整个工作流因为图片收集失败而中断
                log.error("图片收集失败: {}", e.getMessage(), e);
            }

            // 第五步：更新工作流上下文
            // 记录当前节点名称，表示当前步骤已经执行到“图片收集”
            context.setCurrentStep("图片收集");

            // 将收集到的所有图片资源保存到上下文中
            // 供后续节点（如提示词增强、代码生成等）继续使用
            context.setImageList(collectedImages);

            // 将更新后的上下文重新保存回工作流状态中
            return WorkflowContext.saveContext(context);
        });
    }
}
```

------



**流程如下：**

1. 假设用户输入

```
帮我生成一个宠物医院官网，网站里需要：
1. 医生和宠物看诊场景图片
2. 温馨的插画
3. 一个预约流程图
4. 一个宠物医院 Logo
```

------

2. 节点执行前，state 里大概长什么样

在进入 `ImageCollectorNode` 之前，里面可能已经有一个上下文对象：

```Java
state = {
    "workflowContext": {
        originalPrompt: "帮我生成一个宠物医院官网，网站里需要：医生和宠物看诊场景图片、温馨插画、预约流程图、Logo",
        currentStep: "开始",
        imageList: []
    },
    "messages": [...]
}
```

------

3. 根据原始提示词，**先生成 “图片收集计划”**

```Java
ImageCollectionPlanService planService = SpringContextUtil.getBean(ImageCollectionPlanService.class);
ImageCollectionPlan plan = planService.planImageCollection(originalPrompt);
```

- **这一步不是直接搜图，而是先让 AI 或规则系统做一个“规划”**。比如生成出来的 `plan` 可能是这样的：

```Java
// 根据自然语言需求，拆成四类任务
plan = {
    contentImageTasks = [
        new ImageSearchTask("pet doctor examining dog in clinic"),
        new ImageSearchTask("veterinary hospital reception desk")
    ],
    
    illustrationTasks = [
        new IllustrationTask("cute pet care illustration"),
        new IllustrationTask("friendly veterinary service illustration")
    ],
    
    diagramTasks = [
        new DiagramTask(
            "graph TD; 用户预约-->选择医生; 选择医生-->确认时间; 确认时间-->到店看诊;",
            "宠物医院预约流程图"
        )
    ],
    
    logoTasks = [
        new LogoTask("宠物医院 logo，风格温馨、专业、带爪印元素")
    ]
}
```

------

4. **根据 plan，把不同任务都变成异步任务**

```Java
List<CompletableFuture<List<ImageResource>>> futures = new ArrayList<>();
```

------

5. **并发执行任务**

```Java
				// -------------------- 并发执行内容图片搜索 --------------------
                // 如果计划中存在内容图片搜索任务，则逐个创建异步任务
                if (plan.getContentImageTasks() != null) {
                    // 获取内容图片搜索工具
                    ImageSearchTool imageSearchTool = SpringContextUtil.getBean(ImageSearchTool.class);
                    // 遍历所有内容图片搜索任务
                    for (ImageCollectionPlan.ImageSearchTask task : plan.getContentImageTasks()) {
                        // 为每个任务创建一个异步执行单元
                        futures.add(CompletableFuture.supplyAsync(() ->
                                imageSearchTool.searchContentImages(task.query())));
                    }
                }
```

- **假设 `contentImageTasks` 有两个任务：**

```Java
[
    "pet doctor examining dog in clinic",
    "veterinary hospital reception desk"
]
```

- **那么它会创建两个异步任务：**

任务 A

```Java
imageSearchTool.searchContentImages("pet doctor examining dog in clinic")
```

可能返回：

```java
[
    ImageResource(url="https://img.xxx/1.jpg", category=CONTENT, description="医生给狗狗检查"),
    ImageResource(url="https://img.xxx/2.jpg", category=CONTENT, description="宠物诊室场景")
]
```

任务 B

```Java
imageSearchTool.searchContentImages("veterinary hospital reception desk")
```

可能返回：

```Java
[
    ImageResource(url="https://img.xxx/3.jpg", category=CONTENT, description="宠物医院前台"),
    ImageResource(url="https://img.xxx/4.jpg", category=CONTENT, description="接待区场景")
]
```

------







#### 2.2 LangGraph4j 并发实现

利用 LangGraph4j 的 [Parallel Branch](https://langgraph4j.github.io/langgraph4j/core/parallel-branch/) 特性，将每个图片收集工具都定义为一个工作节点，这些工作节点可并发执行。

- **工作流程变为**：图片规划 => 并发收集 => 图片聚合

<img src="./AI零代码生成平台.assets/tCcpLBXnnmVDzqNP.webp" alt="img" style="zoom:50%;" />

------

在 `node.concurrent` 包下开发并发相关的新工作节点，包括：

- 1 个规划节点
- 4 个收集节点
- 1 个汇总节点

<img src="./AI零代码生成平台.assets/C77kjGvYlwm5XTKZ.webp" alt="img" style="zoom: 50%;" />

------



- **以图片收集节点为例：**

```Java
@Slf4j
public class ContentImageCollectorNode {

    /**
     * 创建“内容图片收集”节点
     *
     * 节点职责：
     * 1. 从工作流状态 state 中取出 WorkflowContext
     * 2. 从上下文中获取前面步骤已经生成好的图片收集计划 ImageCollectionPlan
     * 3. 遍历计划中的内容图片搜索任务
     * 4. 调用 ImageSearchTool 搜索内容图片
     * 5. 将搜索到的图片结果暂存到 context 的中间字段 contentImages 中
     * 6. 更新当前工作流步骤，并将 context 重新保存回 state
     *
     * @return 异步节点动作
     */
    public static AsyncNodeAction<MessagesState<String>> create() {
        return node_async(state -> {

            // 1. 从当前工作流状态中获取上下文对象
            WorkflowContext context = WorkflowContext.getContext(state);

            // 2. 用于存放当前节点收集到的“内容图片”
            // 这里先创建一个空列表，后面把每个任务搜索出来的图片统一汇总到这里
            List<ImageResource> contentImages = new ArrayList<>();

            try {
                // 3. 从上下文中获取图片收集计划
                // 这个 plan 一般是在前面的“图片计划生成节点”中提前生成好的
                ImageCollectionPlan plan = context.getImageCollectionPlan();

                // 4. 做空值判断：
                if (plan != null && plan.getContentImageTasks() != null) {

                    // 5. 从 Spring 容器中获取图片搜索工具
                    // 因为当前类通常不是普通的 Spring Bean 注入场景，所以通过工具类手动获取
                    ImageSearchTool imageSearchTool = SpringContextUtil.getBean(ImageSearchTool.class);

                    // 6. 记录日志：准备开始执行内容图片收集任务
                    log.info("开始并发收集内容图片，任务数: {}", plan.getContentImageTasks().size());

                    // 7. 遍历所有内容图片搜索任务
                    // 每一个 task 都对应一条图片搜索指令，比如：
                    // “医院场景图片”、“宠物医生看诊图片”、“首页 banner 图片”等
                    for (ImageCollectionPlan.ImageSearchTask task : plan.getContentImageTasks()) {

                        // 8. 调用图片搜索工具，根据 task 中的 query 搜索内容图片
                        // 例如 task.query() 可能是："pet doctor examining dog in clinic"
                        List<ImageResource> images = imageSearchTool.searchContentImages(task.query());

                        // 9. 如果本次搜索结果不为空，则将结果追加到总列表中
                        if (images != null) {
                            contentImages.addAll(images);
                        }
                    }

                    // 10. 所有内容图片任务执行完成后，记录最终收集到的图片数量
                    log.info("内容图片收集完成，共收集到 {} 张图片", contentImages.size());
                }
            } catch (Exception e) {
                log.error("内容图片收集失败: {}", e.getMessage(), e);
            }

            // 12. 将当前节点收集到的内容图片保存到上下文中
            // 注意：这里保存的是“中间结果字段”，供后续节点继续处理
            context.setContentImages(contentImages);
            // 13. 更新当前工作流步骤，表示当前已经执行到“内容图片收集”阶段
            context.setCurrentStep("内容图片收集");

            // 14. 将更新后的上下文重新保存回工作流状态，并返回给下一个节点
            return WorkflowContext.saveContext(context);
        });
    }
}
```

------



- 在工作流类中新增了 **4个节点和8条边**

```Java
.addEdge("image_plan", "content_image_collector")
.addEdge("image_plan", "illustration_collector")
.addEdge("image_plan", "diagram_collector")
.addEdge("image_plan", "logo_collector")

.addEdge("content_image_collector", "image_aggregator")
.addEdge("illustration_collector", "image_aggregator")
.addEdge("diagram_collector", "image_aggregator")
.addEdge("logo_collector", "image_aggregator")
```

------



- **注意：这时由于之前 LangGraph4j 的旧版本是不支持并发的，即使按照要求写代码，并发分支仍然是串行执行；从 `1.6.0-rc2` 版本后，就支持配置线程池了。**

在工作流代码中的 “执行并发工作流” 方法中**添加上配置线程池和运行时配置：**

```Java
// 配置并发执行
ExecutorService pool = ExecutorBuilder.create()
        .setCorePoolSize(10)
        .setMaxPoolSize(20)
        .setWorkQueue(new LinkedBlockingQueue<>(100))
        .setThreadFactory(ThreadFactoryBuilder.create().setNamePrefix("Parallel-Image-Collect").build())
        .build();
RunnableConfig runnableConfig = RunnableConfig.builder()
        .addParallelNodeExecutor("image_plan", pool)
        .build();

// 把线程池配置 RunnableConfig 设置到 workflow 的第二个参数中支持并发
for (NodeOutput<MessagesState<String>> step : workflow.stream(
        Map.of(WorkflowContext.WORKFLOW_CONTEXT_KEY, initialContext),
        runnableConfig)) {}
```

------









# 性能优化

## 1.分配不同的ChatModel

- 问题：当多个用户同时使用平台时，只有第一个用﻿﻿﻿户的 AI 请求能够正常处理，后续⁢⁢⁢的请求都会被阻塞，需要等待前面的请‍‍‍求完全处理完毕后才能开始执行。
- 经过分析，发现问题出在 AI 大模型的 `ChatModel` 采用了单例模式。虽然 `StreamingChatModel` 返回的是 Flux 响应式流，表面上看起来是异步的，但其**底层的 `SpringRestClient.execute()` 方法内部实际上是同步解析数据流的，导致了串行执行问题。**

------



- **使用多例模式解决**

1. **工厂模式**：编写一个专门的工厂类，提供创建新 `ChatModel` 实例的方法
2. **Spring 多例模式**：利用 Spring 的 Bean 作用域机制，从 Spring 容器中获取新的 `ChatModel` 实例

------



1. 针对不同的任务定制不同的大模型配置

```yml
# AI 模型配置
langchain4j:
  open-ai:
    # 推理 AI 模型配置（用于复杂的推理任务）
    reasoning-streaming-chat-model:
      base-url: https://api.deepseek.com
      api-key: <Your API Key>
      model-name: deepseek-reasoner
      max-tokens: 32768
      temperature: 0.1
      log-requests: true
      log-responses: true
    # 智能路由 AI 模型配置（用于简单的分类任务）
    routing-chat-model:
      base-url: https://api.deepseek.com
      api-key: <Your API Key>
      model-name: deepseek-chat
      log-requests: true
      log-responses: true
```

------



2. 在 config 包下编写以上两个模型对应的配置类【**以智能路由配置为例**】

```Java
@Configuration
@ConfigurationProperties(prefix = "langchain4j.open-ai.routing-chat-model")
@Data
public class RoutingAiModelConfig {

    private String baseUrl;

    private String apiKey;

    private String modelName;

    private Integer maxTokens;

    private Double temperature;

    private Boolean logRequests = false;

    private Boolean logResponses = false;

    /**
     * 创建用于路由判断的ChatModel
     */
    @Bean
    @Scope("prototype")
    public ChatModel routingChatModelPrototype() {
        return OpenAiChatModel.builder()
                .apiKey(apiKey)
                .modelName(modelName)
                .baseUrl(baseUrl)
                .maxTokens(maxTokens)
                .temperature(temperature)
                .logRequests(logRequests)
                .logResponses(logResponses)
                .build();
    }
}
```

------



3. **更新 `AiCodeGeneratorServiceFactory` 类，让它根据代码生成类型选择不同的模型配置**

这样每次创建一个新的应用就会创建出一个新的AI服务，一个新的AI服务就会被分配到一个新的 `ChatModel` 对象

```Java
/**
 * 创建新的 AI 服务实例
 */
private AiCodeGeneratorService createAiCodeGeneratorService(long appId, CodeGenTypeEnum codeGenType) {
    // 根据 appId 构建独立的对话记忆
    MessageWindowChatMemory chatMemory = MessageWindowChatMemory
            .builder()
            .id(appId)
            .chatMemoryStore(redisChatMemoryStore)
            .maxMessages(20)
            .build();
    // 从数据库加载历史对话到记忆中
    chatHistoryService.loadChatHistoryToMemory(appId, chatMemory, 20);
    // 根据代码生成类型选择不同的模型配置
    return switch (codeGenType) {
        // Vue 项目生成使用推理模型
        case VUE_PROJECT -> {
            
            // ========================================修改点======================================
            // 使用多例模式的 StreamingChatModel 解决并发问题【根据名称获取 Bean】
            StreamingChatModel reasoningStreamingChatModel =
                    SpringContextUtil.getBean("reasoningStreamingChatModelPrototype", StreamingChatModel.class);
            // ========================================修改点======================================

            yield AiServices.builder(AiCodeGeneratorService.class)
                    // 指定为设定的推理流式模型
                    .streamingChatModel(reasoningStreamingChatModel)
                    // 给 AI 服务提供 “对话记忆对象” 的获取方式。
                    .chatMemoryProvider(memoryId -> chatMemory)
                    .tools(toolManager.getAllTools())

                    // 你明明只注册了某些工具
                    // 但大模型在调用工具时，可能“想象”出一个根本不存在的工具
                    // 这时框架就会走这个策略，告诉模型：这个工具不存在
                    .hallucinatedToolNameStrategy(toolExecutionRequest -> ToolExecutionResultMessage.from(
                            toolExecutionRequest, "Error: there is no tool called " + toolExecutionRequest.name()
                    ))
                    .build();
        }

        // HTML 和多文件生成使用默认模型
        case HTML, MULTI_FILE -> {
            
            // ========================================修改点======================================
            // 使用多例模式的 StreamingChatModel 解决并发问题【根据名称获取 Bean】
            StreamingChatModel openAiStreamingChatModel =
                    SpringContextUtil.getBean("streamingChatModelPrototype", StreamingChatModel.class);
			// ========================================修改点======================================
            
            yield AiServices.builder(AiCodeGeneratorService.class)
                    .chatModel(chatModel)
                    .streamingChatModel(openAiStreamingChatModel)
                    .chatMemory(chatMemory)
                    .build();
        }
        default -> throw new BusinessException(ErrorCode.SYSTEM_ERROR,
                "不支持的代码生成类型: " + codeGenType.getValue());
    };
}
```

------



4. **更新智能路由 AI 服务工厂类**

和上面的创建一个应用就对应生成一个 AI 服务一样，AI路由也需要根据每个路由服务生成一个对应的 `ChatModel`

```java
/**
 * AI代码生成类型路由服务工厂
 *
 * @author yupi
 */
@Slf4j
@Configuration
public class AiCodeGenTypeRoutingServiceFactory {

    /**
     * 创建AI代码生成类型路由服务实例
     */
    public AiCodeGenTypeRoutingService createAiCodeGenTypeRoutingService() {
        // 动态获取多例的路由 ChatModel，支持并发
        ChatModel chatModel = SpringContextUtil.getBean("routingChatModelPrototype", ChatModel.class);
        return AiServices.builder(AiCodeGenTypeRoutingService.class)
                .chatModel(chatModel)
                .build();
    }

    /**
     * 默认提供一个 Bean
     */
    @Bean
    public AiCodeGenTypeRoutingService aiCodeGenTypeRoutingService() {
        return createAiCodeGenTypeRoutingService();
    }
}
```

------



5. **调⁠⁠⁠用智能路由服务的地方也需要调整获﻿取﻿﻿逻辑**

```java
@Resource
private AiCodeGenTypeRoutingServiceFactory aiCodeGenTypeRoutingServiceFactory;

@Override
public Long createApp(AppAddRequest appAddRequest, User loginUser) {
    // 参数校验
    String initPrompt = appAddRequest.getInitPrompt();
    ThrowUtils.throwIf(StrUtil.isBlank(initPrompt), ErrorCode.PARAMS_ERROR, "初始化 prompt 不能为空");
    
    // ===========================================修改点============================================
    // 使用 AI 智能选择代码生成类型（多例模式，需要新建一个AI智能路由服务，然后根据这个服务创建对应的ChatModel）
    AiCodeGenTypeRoutingService routingService = aiCodeGenTypeRoutingServiceFactory.createAiCodeGenTypeRoutingService();
    CodeGenTypeEnum selectedCodeGenType = routingService.routeCodeGenType(initPrompt);
    // ===========================================修改点============================================
    
    // ... 其他业务逻辑
}
```

------







## 2.实时性优化

之前有提到，如果是 V⁠⁠⁠ue 工程模式生成，用户在 AI 生成完代码后无法实时浏览到网站效果，或者看到的还是旧版本﻿的﻿﻿页面。这是因为我们 **之前采用的是异步打包策⁢⁢⁢略，当用户看到 AI 回复完成时，Vue 项‍‍‍目可能还在后台构建中，存在时间差**。

------

- **解决方案：将异步打包修改为同步打包**

1. 首先从 `JsonMessageStreamHandler` 的 `doOnComplete` 方法中移除异步构建逻辑

```Java
                .doOnComplete(() -> {
                    // 流式响应完成后，添加 AI 消息到对话历史
                    String aiResponse = chatHistoryStringBuilder.toString();
                    chatHistoryService.addChatMessage(appId, aiResponse, ChatHistoryMessageTypeEnum.AI.getValue(), loginUser.getId());

//                    // 异步构造 Vue 项目
//                    String projectPath = AppConstant.CODE_OUTPUT_ROOT_DIR + "/vue_project_" + appId;
//                    vueProjectBuilder.buildProjectAsync(projectPath);
                })
```

------



2. 在 `AiCodeGeneratorFacade` 的 `processTokenStream` 方法中添加 **同步打包** 构建逻辑

```Java
// 监听整个 AI 响应流结束事件
.onCompleteResponse((ChatResponse response) -> {
    // 执行 Vue 项目构建（同步执行，确保预览时项目已就绪）
    String projectPath = AppConstant.CODE_OUTPUT_ROOT_DIR + File.separator + "vue_project_" + appId;
    vueProjectBuilder.buildProject(projectPath);
    // 通知 Flux 下游：流已经结束
    sink.complete();
})
```

------







### （扩展）2.1 基于 SSE 实现构建状态的实时推送

**阶段 A：AI 代码生成阶段，负责把：**

- AI 普通回复
- 工具调用请求
- 工具执行完成

实时推给前端。

------

**阶段 B：Vue 构建阶段**

当 AI 代码生成完成后，不立即结束 SSE，而是继续：

- 推送“开始构建”
- 推送“依赖安装中”
- 推送“项目构建中”
- 推送“构建成功 / 失败”
- 构建成功后推送预览地址
- 最后才结束 SSE

------





#### 修改点1：修改门面类：

`AiCodeGeneratorFacade.processTokenStream()`

- 之前：`onCompleteResponse()` 里要么直接 `sink.complete()`，要么异步构建后不管前端
- 现在：`onCompleteResponse()` 里**不能立刻结束流**，而是要在这里触发构建，并**把构建状态继续通过 `sink.next(...)` 推给前端，最后才 `sink.complete()`**
  - **关键修改代码:**

```Java
.onCompleteResponse((ChatResponse response) -> {
    String projectPath = AppConstant.CODE_OUTPUT_ROOT_DIR + File.separator + "vue_project_" + appId;

    // 每当 VueProjectBuilder 产生一条构建状态消息，我就把它转成 JSON，然后通过 sink.next(...) 推送给前端
    boolean success = vueProjectBuilder.buildProject(projectPath, buildMessage -> {
        sink.next(JSONUtil.toJsonStr(buildMessage));
    });

    sink.complete();
})
```

------

把：

```
AI结束 -> 直接结束SSE
```

改成：

```Java
AI结束 -> 开始构建 -> 持续推送构建状态 -> 构建完成后结束SSE
```

------

- **完整的修改后的代码：**

```Java
// 监听整个 AI 响应流结束事件
.onCompleteResponse((ChatResponse response) -> {
    // 执行 Vue 项目构建（同步执行，确保预览时项目已就绪）
    String projectPath = AppConstant.CODE_OUTPUT_ROOT_DIR + File.separator + "vue_project_" + appId;

    try {

        // 每当 VueProjectBuilder 产生一条构建状态消息，我就把它转成 JSON，然后通过 sink.next(...) 推送给前端
        boolean success = vueProjectBuilder.buildProject(projectPath, buildMessage -> {
            sink.next(JSONUtil.toJsonStr(buildMessage));
        });

        if (!success) {
            log.error("Vue 项目构建失败, appId={}", appId);
        }
        sink.complete();
    } catch (Exception e) {
        log.error("Vue 项目构建异常", e);
        BuildStatusMessage failMessage = new BuildStatusMessage("BUILD_FAIL", "项目构建异常");
        failMessage.setErrorMessage(e.getMessage());
        sink.next(JSONUtil.toJsonStr(failMessage));
        sink.error(e);
    }
})
```

------





#### 修改点2：改造 `VueProjectBuilder`

***将构建 Vue 项目的每一步状态都推送出去到门面类，然后统一返回给前端***

把

```
public boolean buildProject(String projectPath)
```

改成类似：

```
public boolean buildProject(String projectPath, Consumer<BuildStatusMessage> statusConsumer)
```

**然后在里面每一步都推状态：**

- 开始构建
- 开始 `npm install`
- `npm install` 成功 / 失败
- 开始 `npm run build`
- `npm run build` 成功 / 失败
- `dist` 校验成功 / 失败
- 最终构建成功 / 失败

------

例如：

```Java
statusConsumer.accept(new BuildStatusMessage("BUILD_START", "代码生成完成，开始构建 Vue 项目"));
statusConsumer.accept(new BuildStatusMessage("INSTALL_START", "正在安装依赖..."));
statusConsumer.accept(new BuildStatusMessage("INSTALL_SUCCESS", "依赖安装完成"));
statusConsumer.accept(new BuildStatusMessage("BUILD_STEP_START", "正在执行 npm run build ..."));
statusConsumer.accept(new BuildStatusMessage("BUILD_SUCCESS", "项目构建成功"));
```

------



- **问题：`Consumer<T>` 是什么？**

解答：接收一个参数，**执行某个动作，但不返回结果**

比如你把构建方法写成：

```Java
public boolean buildProject(String projectPath, Consumer<BuildStatusMessage> statusConsumer)
```

意思不是“传一个普通参数进来”，而是：

> 在构建过程中，如果有新的状态，就调用 `statusConsumer.accept(...)` 把状态交出去

- **例如：**

```Java
statusConsumer.accept(new BuildStatusMessage("INSTALL_START", "正在安装依赖..."));
```

> 把“正在安装依赖”这条状态消息，交给外部去处理

------

在你的 `processTokenStream()` 里：

```Java
boolean success = vueProjectBuilder.buildProject(projectPath, buildMessage -> {
    sink.next(JSONUtil.toJsonStr(buildMessage));
});
```

- **每当 `VueProjectBuilder` 产生一条构建状态消息，我就把它转成 JSON，然后通过 `sink.next(...)` 推送给前端**

------



- 具体代码：

```Java
public boolean buildProject(String projectPath, Consumer<BuildStatusMessage> statusConsumer) {
    File projectDir = new File(projectPath);
    if (!projectDir.exists() || !projectDir.isDirectory()) {
        statusConsumer.accept(buildFail("项目目录不存在: " + projectPath));
        return false;
    }

    File packageJson = new File(projectDir, "package.json");
    if (!packageJson.exists()) {
        statusConsumer.accept(buildFail("package.json 文件不存在: " + packageJson.getAbsolutePath()));
        return false;
    }

    statusConsumer.accept(new BuildStatusMessage("BUILD_START", "代码生成完成，开始构建 Vue 项目"));

    // 1. npm install
    statusConsumer.accept(new BuildStatusMessage("INSTALL_START", "正在安装项目依赖..."));
    if (!executeNpmInstall(projectDir)) {
        statusConsumer.accept(buildFail("npm install 执行失败"));
        return false;
    }
    statusConsumer.accept(new BuildStatusMessage("INSTALL_SUCCESS", "项目依赖安装完成"));

    // 2. npm run build
    statusConsumer.accept(new BuildStatusMessage("BUILD_STEP_START", "正在执行 npm run build ..."));
    if (!executeNpmBuild(projectDir)) {
        statusConsumer.accept(buildFail("npm run build 执行失败"));
        return false;
    }
    statusConsumer.accept(new BuildStatusMessage("BUILD_STEP_SUCCESS", "项目构建命令执行完成"));

    // 3. 检查 dist
    File distDir = new File(projectDir, "dist");
    if (!distDir.exists()) {
        statusConsumer.accept(buildFail("构建完成但 dist 目录未生成"));
        return false;
    }

    BuildStatusMessage successMessage = new BuildStatusMessage("BUILD_SUCCESS", "Vue 项目构建成功");
    successMessage.setPreviewUrl("/preview/" + projectDir.getName() + "/");
    statusConsumer.accept(successMessage);

    return true;
}
```

------





#### 修改点3：创建一个BuildStatusMessage对象

需要这个 `BuildStatusMessage`，本质上是因为现在的流里，已经不只是传“AI 文本”了，而是要传 “**构建阶段的状态信息**”。

- ***即：“Vue 构建过程中的专用流式消息载体”***

```Java
@Data
@EqualsAndHashCode(callSuper = true)
public class BuildStatusMessage extends StreamMessage {

    /**
     * 构建阶段：
     * BUILD_START / INSTALL_START / INSTALL_SUCCESS / INSTALL_FAIL
     * BUILD_STEP_START / BUILD_STEP_SUCCESS / BUILD_STEP_FAIL
     * BUILD_SUCCESS / BUILD_FAIL
     */
    private String stage;

    /**
     * 展示消息
     */
    private String message;

    /**
     * 预览地址
     */
    private String previewUrl;

    /**
     * 错误信息
     */
    private String errorMessage;

    public BuildStatusMessage() {
        super(StreamMessageTypeEnum.BUILD_STATUS.getValue());
    }

    public BuildStatusMessage(String stage, String message) {
        super(StreamMessageTypeEnum.BUILD_STATUS.getValue());
        this.stage = stage;
        this.message = message;
    }
}
```

------







## 3.Prompt 安全审查 - 护轨机制

护轨是 AI⁠⁠⁠ 应用中的安全机制，类似于道路上的护栏，用于﻿﻿﻿防止恶意的 Promp⁢⁢⁢t 输入、防止 AI ‍‍‍模型产生不当或有害的内容。

其实我们把它理解为拦截器就好了，护轨分为两种：

- **输入护轨（Input Guardrails）：在用户输入传递给 AI 模型之前进行检查和过滤**
- **输出护轨（Output Guardrails）：在 AI 模型生成内容后进行检查和过滤**

------

除了输入 ⁠⁠⁠`Prompt` 和 AI 输出结果﻿﻿的安﻿全校验外，你⁢⁢还**可以⁢利用护轨进‍‍行权限校‍验、日志记录等**。

下面来⁠⁠⁠利用输入护轨实现 `Promp﻿t` ﻿安全﻿审核，⁢防止一⁢些非法⁢ ‍`Prom‍pt`，比‍如：

- 拒绝过长的 Prompt
- 拒绝包含敏感词的 Prompt
- 拒绝包含注入攻击的 Prompt

------



- ***开发实现：***

***实现 `InputGuardrail` 接口来实现护轨功能。***

```java 
/**
 * Prompt 安全审查
 */
public class PromptSafetyInputGuardrail implements InputGuardrail {

    // 敏感词列表
    private static final List<String> SENSITIVE_WORDS = Arrays.asList(
            "忽略之前的指令", "ignore previous instructions", "ignore above",
            "破解", "hack", "绕过", "bypass", "越狱", "jailbreak"
    );

    // 注入攻击模式
    private static final List<Pattern> INJECTION_PATTERNS = Arrays.asList(
            Pattern.compile("(?i)ignore\\s+(?:previous|above|all)\\s+(?:instructions?|commands?|prompts?)"),
            Pattern.compile("(?i)(?:forget|disregard)\\s+(?:everything|all)\\s+(?:above|before)"),
            Pattern.compile("(?i)(?:pretend|act|behave)\\s+(?:as|like)\\s+(?:if|you\\s+are)"),
            Pattern.compile("(?i)system\\s*:\\s*you\\s+are"),
            Pattern.compile("(?i)new\\s+(?:instructions?|commands?|prompts?)\\s*:")
    );

    @Override
    public InputGuardrailResult validate(UserMessage userMessage) {
        String input = userMessage.singleText();
        // 检查输入长度
        if (input.length() > 1000) {
            return fatal("输入内容过长，不要超过 1000 字");
        }
        // 检查是否为空
        if (input.trim().isEmpty()) {
            return fatal("输入内容不能为空");
        }
        // 检查敏感词
        String lowerInput = input.toLowerCase();
        for (String sensitiveWord : SENSITIVE_WORDS) {
            if (lowerInput.contains(sensitiveWord.toLowerCase())) {
                return fatal("输入包含不当内容，请修改后重试");
            }
        }
        // 检查注入攻击模式
        for (Pattern pattern : INJECTION_PATTERNS) {
            if (pattern.matcher(input).find()) {
                return fatal("检测到恶意输入，请求被拒绝");
            }
        }
        return success();
    }
}
```

------



- **在 AI 服务工厂中集成输入护轨：**

```java 
yield AiServices.builder(AiCodeGeneratorService.class)
        // 指定为设定的推理流式模型
        .streamingChatModel(reasoningStreamingChatModel)
        // 给 AI 服务提供 “对话记忆对象” 的获取方式。
        .chatMemoryProvider(memoryId -> chatMemory)
        .tools(toolManager.getAllTools())

        // 你明明只注册了某些工具
        // 但大模型在调用工具时，可能“想象”出一个根本不存在的工具
        // 这时框架就会走这个策略，告诉模型：这个工具不存在
        .hallucinatedToolNameStrategy(toolExecutionRequest -> ToolExecutionResultMessage.from(
                toolExecutionRequest, "Error: there is no tool called " + toolExecutionRequest.name()
        ))

    	// =====================================修改点==========================================
        // 添加输入护轨
        .inputGuardrails(new PromptSafetyInputGuardrail())
    	// =====================================修改点==========================================

        .build();
```

------







### （扩展）3.1 Nacos实现敏感词库的动态维护

1. 添加 Nacos 依赖

```xml
<dependency>
    <groupId>com.alibaba.cloud</groupId>
    <artifactId>spring-cloud-starter-alibaba-nacos-config</artifactId>
    <version>2025.1.0.0</version>
</dependency>
```

------



2. **新增和远程Nacos配置文件对应的配置类**

- `@RefreshScope` 实现动态刷新
- `@ConfigurationProperties` ：配置文件中以 `ai.guardrail.prompt-safety` 开头的配置，自动映射到这个 Java 类的字段上

```Java
/**
 * Prompt 安全护轨配置
 *
 * 配置来源：
 * 1. 本地 application.yml
 * 2. Nacos 配置中心
 *
 * 作用：
 * - 将敏感词、注入检测正则、最大长度等规则配置化
 * - 配合 @RefreshScope 实现动态刷新
 */
@Data
@Component
@RefreshScope
@ConfigurationProperties(prefix = "ai.guardrail.prompt-safety")
public class PromptSafetyProperties {

    /**
     * 是否开启护轨
     */
    private boolean enabled = true;

    /**
     * 输入最大长度
     */
    private int maxLength = 1000;

    /**
     * 敏感词列表
     */
    private List<String> sensitiveWords = new ArrayList<>();

    /**
     * 注入攻击检测正则
     */
    private List<String> injectionPatterns = new ArrayList<>();
}
```

------



3. **修改护轨类**

将原本写死的 `SENSITIVE_WORDS` 和 `INJECTION_PATTERNS` 正则表达式和敏感词修改为从配置文件中读取【即 `PromptSafetyProperties` 配置类】

```java 
/**
 * Prompt 安全审查
 *
 * 改造点：
 * 1. 原来的敏感词和注入正则不再写死在代码中
 * 2. 改为从 PromptSafetyProperties 动态读取
 * 3. 配合 Nacos + @RefreshScope，实现规则热更新
 */
@Component
@RequiredArgsConstructor
public class PromptSafetyInputGuardrail implements InputGuardrail {

    /**
     * 护轨配置对象
     * 配置可来自 Nacos，并支持动态刷新
     */
    private final PromptSafetyProperties promptSafetyProperties;

    @Override
    public InputGuardrailResult validate(UserMessage userMessage) {
        String input = userMessage.singleText();

        // 1. 开关关闭时，直接放行
        if (!promptSafetyProperties.isEnabled()) {
            return success();
        }

        // 2. 检查是否为空
        if (input == null || input.trim().isEmpty()) {
            return fatal("输入内容不能为空");
        }

        // 3. 检查输入长度
        if (input.length() > promptSafetyProperties.getMaxLength()) {
            return fatal("输入内容过长，不要超过 " + promptSafetyProperties.getMaxLength() + " 字");
        }

        // 4. 检查敏感词
        String lowerInput = input.toLowerCase();
        List<String> sensitiveWords = promptSafetyProperties.getSensitiveWords();
        if (sensitiveWords != null) {
            for (String sensitiveWord : sensitiveWords) {
                if (sensitiveWord != null && !sensitiveWord.isBlank()
                        && lowerInput.contains(sensitiveWord.toLowerCase())) {
                    return fatal("输入包含不当内容，请修改后重试");
                }
            }
        }

        // 5. 检查注入攻击模式
        List<String> injectionPatterns = promptSafetyProperties.getInjectionPatterns();
        if (injectionPatterns != null) {
            for (String regex : injectionPatterns) {
                if (regex == null || regex.isBlank()) {
                    continue;
                }
                Pattern pattern = Pattern.compile(regex);
                if (pattern.matcher(input).find()) {
                    return fatal("检测到恶意输入，请求被拒绝");
                }
            }
        }

        return success();
    }
}
```

------



4. **修改工厂类**

将原本的手动 `new PromptSafetyInputGuardrail()` 改为 Spring 容器注入形式

- **在工厂类注入：**

```Java
@Resource
private PromptSafetyInputGuardrail promptSafetyInputGuardrail;
```

- **原本的：**

```Java
.inputGuardrails(new PromptSafetyInputGuardrail())
```

- **改为**

```Java
.inputGuardrails(promptSafetyInputGuardrail)
```

------



5. **修改 `application.yml` 配置**

```yml
spring:
  application:
    name: ai-code-creater

  # 让 Spring Boot 在启动时，额外去 Nacos 配置中心加载一个名为 aicodecreater-prompt-safety.yml 的远程配置文件。
  config:
    import:
      - optional:nacos:aicodecreater-prompt-safety.yml?group=DEFAULT_GROUP

  cloud:
    nacos:
      config:
        server-addr: 127.0.0.1:8848
        group: DEFAULT_GROUP
        file-extension: yml
        refresh-enabled: true
```

------



6. **在远程 Nacos 配置文件 `aicodecreater-prompt-safety.yml` 的配置：**

```yml
ai:
  guardrail:
    prompt-safety:
      enabled: true
      max-length: 1000
      sensitive-words:
        - 忽略之前的指令
        - ignore previous instructions
        - ignore above
        - 破解
        - hack
        - 绕过
        - bypass
        - 越狱
        - jailbreak
      injection-patterns:
        - "(?i)ignore\\s+(?:previous|above|all)\\s+(?:instructions?|commands?|prompts?)"
        - "(?i)(?:forget|disregard)\\s+(?:everything|all)\\s+(?:above|before)"
        - "(?i)(?:pretend|act|behave)\\s+(?:as|like)\\s+(?:if|you\\s+are)"
        - "(?i)system\\s*:\\s*you\\s+are"
        - "(?i)new\\s+(?:instructions?|commands?|prompts?)\\s*:"
```

------







## 4.稳定性优化

### 4.1 重试策略，输出护轨

由于大模型调用存在一⁠⁠⁠定不确定性，有时候可能返回不符合预期的内容、或者回复中断。所以**为了提升系统的稳定﻿﻿﻿性，我们需要让大模型调用失败时能够自动重⁢⁢⁢试，并且还可以实现自定义的重试策略**，在 ‍‍‍AI 响应内容不符合要求时也自动重试。



1. **LangChain4j 重试机制**

其实 `LangChain4j` 的 `ChatModel` 对象本身就支持重试，可以**通过配置 `max-retries` 参数修改重试次数（默认值是 2）**：

```yaml
langchain4j:
  open-ai:
    chat-model:
      max-retries: 3
```

构造 `Ch⁠⁠atModel` 时也可以设置重试次数：

```java
OpenAiChatModel.builder()
    .maxRetries(3)
    .build();
```

------



2. 如果想自己决定重试时机和策略，可以利用 [LangChain4j 的输出护轨](https://docs.langchain4j.dev/tutorials/guardrails#output-guardrail-outcomes)，可以对 AI 的响应结果进行检测和处理，并且提供了多种结果类型。
   - **`success()`：允许响应通过**
   - **`retry()`：使用相同的输入重新调用 AI**
   - **`reprompt()`：添加额外的提示信息后重新调用 AI**
   - **`fatal()`：中断 AI 响应，抛出异常**

```java
public class RetryOutputGuardrail implements OutputGuardrail {

    @Override
    public OutputGuardrailResult validate(AiMessage responseFromLLM) {
        String response = responseFromLLM.text();
        // 检查响应是否为空或过短
        if (response == null || response.trim().isEmpty()) {
            return reprompt("响应内容为空", "请重新生成完整的内容");
        }
        if (response.trim().length() < 10) {
            return reprompt("响应内容过短", "请提供更详细的内容");
        }
        // 检查是否包含敏感信息或不当内容
        if (containsSensitiveContent(response)) {
            return reprompt("包含敏感信息", "请重新生成内容，避免包含敏感信息");
        }
        return success();
    }
    
    /**
     * 检查是否包含敏感内容
     */
    private boolean containsSensitiveContent(String response) {
        String lowerResponse = response.toLowerCase();
        String[] sensitiveWords = {
            "密码", "password", "secret", "token", 
            "api key", "私钥", "证书", "credential"
        };
        for (String word : sensitiveWords) {
            if (lowerResponse.contains(word)) {
                return true;
            }
        }
        return false;
    }
}
```

------



3. 在 AI 服务工厂 `AiCodeGeneratorServiceFactory` 中集成输出护轨：

```java
yield AiServices.builder(AiCodeGeneratorService.class)
        .chatModel(chatModel)
        .streamingChatModel(openAiStreamingChatModel)
        .chatMemory(chatMemory)
        
        .inputGuardrails(new PromptSafetyInputGuardrail())
        .outputGuardrails(new RetryOutputGuardrail())
        
        .build();
```

------



***！！！注意：如果⁠⁠⁠用了输出护轨，可能会导致流式输出的响应不及时，等到 AI﻿﻿﻿ 输出结束才一起返回，所以如⁢⁢⁢果为了追求流式输出效果，建议‍‍‍不要通过护轨的方式进行重试。***

------







### 4.2 工具调用优化

**为 AI 提供一个专门的退出工具，让它能够主动结束工具调用循环**。在 `ai.tools` 包下新建一个继承了 `BaseTool` 的工具类，这样可以被 `ToolManager` 自动注册：

```Java
@Slf4j
@Component
public class ExitTool extends BaseTool {

    @Override
    public String getToolName() {
        return "exit";
    }

    @Override
    public String getDisplayName() {
        return "退出工具调用";
    }

    /**
     * 退出工具调用
     * 当任务完成或无需继续使用工具时调用此方法
     *
     * @return 退出确认信息
     */
    @Tool("当任务已完成或无需继续调用工具时，使用此工具退出操作，防止循环")
    public String exit() {
        log.info("AI 请求退出工具调用");
        return "不要继续调用工具，可以输出最终结果了";
    }

    @Override
    public String generateToolExecutedResult(JSONObject arguments) {
        return "\n\n[执行结束]\n\n";
    }
}
```

------









# 可观测性

## 监控的数据分类

在实现可观测性时，我们需要关注多种不同类型的数据：

1. **系统指⁠⁠⁠⁠⁠⁠⁠标**：包括 **CPU 使用率、内存占﻿﻿﻿﻿﻿﻿用、﻿磁盘 I/O⁢⁢⁢⁢⁢⁢、网络⁢流量**等基础‍‍‍‍‍‍设施层面的‍监控数据。

2. **应用指⁠⁠⁠⁠⁠⁠⁠标**：涵盖接口**响应时间、QPS（每﻿﻿﻿﻿﻿﻿秒查﻿询率）、错误⁢⁢⁢⁢⁢⁢率、J⁢VM 状态‍‍‍‍‍‍**等应用层面的‍性能数据。

3. **业务指⁠⁠⁠⁠⁠⁠⁠标**：针对我们平台的特定业务逻辑，比如﻿﻿﻿﻿﻿﻿﻿ **AI 模型调用次⁢⁢⁢⁢⁢⁢⁢数、Token 消‍‍‍‍‍‍‍耗量、用户活跃度**等。

4. **调用链**：在分布式系统中，一个请求可能经过多个服务组件。**Trace** 表示**一个完整请求的调用链路**，而 **Span** 则代表**调用链中的一个操作单元**。通过分析 Trace 和 Span，我们可以清晰地看到请求在系统中的流转过程，快速定位性能瓶颈。

![img](./AI零代码生成平台.assets/Ff74D8svT0dnLCTI.webp)

------







## Prometheus + Grafana 业务监控

1. 统计什么？监控 AI 模型调用相关的业务指标
2. 如何收集？通过在代码中**埋点的方式主动收集数据**。
3. 如何存储？使用 **Prometheus 时序数据库**存储指标数据。
4. 如何展示？通过 **Grafana 构建可视化监控仪表**板。

------







### Prometheus

- **注意：下载服务后会在 9090 端口提供服务**

[Prometheus](https://prometheus.io/docs/introduction/overview/) 是一个开源的监控系统，专门为时序数据的收集、存储和查询而设计。

Prometheus 的核心理念是将所有监控数据以 **时间序列** 的形式存储。根据它的 [数据模型](https://prometheus.io/docs/concepts/data_model/)，每个时间序列都由指标名称和一组标签唯一标识。比如 **`http_requests_total{method="POST", handler="/api/yupi"}` 就表示一个记录 POST 请求接口总数的时间序列。**

------



- **数据收集原理**

Prometheus 采用 **拉取模式** 来收集指标数据，而不是由项目主动推送数据，这是它的核心特征。

它会**定期向配置的目标发起 HTTP 请求**，从 `/metrics` 端点获取指标数据。

拉模式的好⁠处是：               ﻿         ⁢        

- 简单可靠：基于标准 HTTP 协议，无需复杂的消息队列或特殊的网络配置
- 监控目标的发现和管理更加灵活：**Prometheus 可以通过服务发现机制自动发现新的监控目标**。而且将监控的控制权交给 Prometheus，可以避免目标服务的监控数据推送失败影响业务逻辑。

我们可以通过 [Jobs 和 Instances](https://prometheus.io/docs/concepts/jobs_instances/) 配置需要拉取的数据任务和服务实例，当 Prometheus 抓取目标时，会**自动为每个时间序列添加 `job` 和 `instance` 标签来标识数据来源**。同时还会生成一些元指标，比如 `up` 指标表示目标是否可达，`scrape_duration_seconds` 记录抓取耗时，这些信息对于监控系统自身的健康检查非常有用。

------



- **Prometheus存储机制**

新写入的 **数据首先存储⁠⁠⁠⁠⁠⁠⁠⁠⁠⁠⁠在内存中**，达到时间阈值（每 2 小时一个数据块）**后批量写入磁盘**，这种设计在保﻿﻿﻿﻿﻿﻿﻿﻿﻿﻿﻿证查询性能的同时也提供了良好的写入⁢⁢⁢⁢⁢⁢⁢⁢⁢⁢⁢吞吐量。**预写日志 WAL 机制确保了数据的‍‍‍‍‍‍‍‍‍‍‍可靠性**，即使系统崩溃也不会丢失数据。

<img src="./AI零代码生成平台.assets/NuWNujC70caqIZJ3.webp" alt="img" style="zoom:33%;" />

------







### Grafana

[Grafana](https://grafana.com/) 是一个开源的数据可视化平台，专门用于创建监控看板。它可以连接多种数据源（包括 Prometheus、MySQL、PostgreSQL、Elasticsearch 等），并提供丰富的图表类型和可视化选项。

------



-  ***Windows 系统直接执行 `grafana-server.exe` 启动，访问 [http://localhost:3000](http://localhost:3000/) 查看看板，默认登录账号密码都是 `admin`***


------



- **Grafana 整合 Prometheus**

1. 登录 Grafana 后，需要先添加 Prometheus 作为数据源：在 `Connections` 选项下的 `Data sources` ，选择普罗米修斯连接，注意 Connection 这里配置好 `prometheus` 的链接地址：`localhost:9090`

<img src="./AI零代码生成平台.assets/BiYY7edVGKYYrFJz.webp" alt="img" style="zoom:50%;" />

------



2. 然后在快速导入现成的仪表板模板：

<img src="./AI零代码生成平台.assets/FazRrk2GQ1psadE8.webp" alt="img" style="zoom:50%;" />

------







## 项目中开发实现

### 1.**引入依赖**

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-actuator</artifactId>
</dependency>
<dependency>
    <groupId>io.micrometer</groupId>
    <artifactId>micrometer-registry-prometheus</artifactId>
</dependency>
```

------

1）`spring-boot-starter-actuator`：**Actuator** 提供生产就绪的监控基础设施，**暴露各种管理和监控端点**。它是应用与外部监控系统交互的窗口，但本身不负责指标数据的收集。

2）`micromete⁠⁠⁠⁠⁠⁠⁠⁠⁠⁠⁠r-core`：**Micrometer 是真正的指标收集引擎，负责收集 JVM、HTTP﻿﻿﻿﻿﻿﻿﻿﻿﻿﻿﻿、数据库等各种指标数据**。它提供统一的 AP⁢⁢⁢⁢⁢⁢⁢⁢⁢⁢⁢I 让开发者可以创建自定义指标（类似于一个‍‍‍‍‍‍‍‍‍‍‍门面），是整个监控体系的**数据生产者**。

3）`micrometer-registry-prometheus`：**Prometheus Registry 专门负责将 Micrometer 收集的指标数据转换为 Prometheus 格式**。它创建 `/actuator/prometheus` 端点，让 Prometheus 服务器可以直接拉取标准格式的监控数据。

**Prometheus** 可以定期访问 `/actuator/prometheus` 端点拉取指标数据，**实现对 Spring Boot 应用的持续监控和告警**。

总结一下作用：

- `spring-boot-starter-actuator` = 端点提供者（开门的）
- `micrometer-core` = 数据收集者（干活的）
- `micrometer-registry-prometheus` = 格式转换者（翻译的）
- `Prometheus` = 数据拉取者（消费的）

------





### 2.编写配置文件

**在 `application.yml` 中添加 Actuator 配置，暴露监控端点：**

```yml
management:
  endpoints:
    web:
      exposure:
        include: health,info,prometheus
  endpoint:
    health:
      show-details: always
```

------





### 3.**监控上下文**

由于**需要在监⁠⁠⁠⁠⁠⁠⁠⁠⁠⁠⁠听器中获取业务维度信息（比如 `appId、u﻿﻿﻿﻿﻿﻿﻿﻿﻿﻿﻿serId`），我们可以⁢⁢⁢⁢⁢⁢⁢⁢⁢⁢⁢通过 `ThreadLo‍‍‍‍‍‍‍‍‍‍‍cal` 来传递这些参数**。

1. 首先在 monitor 包下定义上下文类 `MonitorContext`：

- ***还可以按需添加 `requestId`、`chatHistoryId` 等字段***

```java
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MonitorContext implements Serializable {

    private String userId;

    private String appId;

    @Serial
    private static final long serialVersionUID = 1L;
}
```

------



2. 定义上⁠⁠⁠⁠⁠⁠⁠⁠⁠⁠⁠下文持有者 `MonitorContext﻿﻿﻿﻿﻿﻿﻿﻿﻿﻿﻿Holder`，**提供 ⁢⁢⁢⁢⁢⁢⁢⁢⁢⁢⁢`ThreadLoca‍‍‍‍‍‍‍‍‍‍‍l` 的读、写、清除方法**：

- ***设置监控的上下文：`MonitorContext`***

```java
@Slf4j
public class MonitorContextHolder {

    private static final ThreadLocal<MonitorContext> CONTEXT_HOLDER = new ThreadLocal<>();

    /**
     * 设置监控上下文
     */
    public static void setContext(MonitorContext context) {
        CONTEXT_HOLDER.set(context);
    }

    /**
     * 获取当前监控上下文
     */
    public static MonitorContext getContext() {
        return CONTEXT_HOLDER.get();
    }

    /**
     * 清除监控上下文
     */
    public static void clearContext() {
        CONTEXT_HOLDER.remove();
    }
}
```

------



3. 在 `AppServiceImpl` 的 `chatToGenCode` 方法中【**核心和AI对话的方法，在这里可以获取到 `appId` 和 `userId`**】设置上下文，并在 AI 调用流结束时清理【**最后清理的时候由于是异步传递的，因此要用 `.doFinally()` 流式等待AI生成了所有结果了再返回最终结果**】：

```java
// 5. 先记录用户消息
chatHistoryService.addChatMessage(
        appId,
        message,
        ChatHistoryMessageTypeEnum.USER.getValue(),
        loginUser.getId()
);

// ============================================修改点============================================
// 6. 设置监控上下文
MonitorContextHolder.setContext(
        MonitorContext.builder()
                .userId(loginUser.getId().toString())
                .appId(appId.toString())
                .build()
);
// ============================================修改点============================================

// 7. 计算新版本号
//    例如当前是 v2，则本次生成的是 v3
int nextVersion = (app.getCurrentVersion() == null ? 0 : app.getCurrentVersion()) + 1;

// 8. 构建本次生成对应的“版本目录”
//    示例：/tmp/code_output/multi_file_2038144454829350912_v3
String sourceDirName = app.getCodeGenType() + "_" + appId + "_v" + nextVersion;
String sourceDirPath = AppConstant.CODE_OUTPUT_ROOT_DIR + File.separator + sourceDirName;

// 9. 如果目录已存在，先删除，避免脏数据
File versionDir = new File(sourceDirPath);
if (versionDir.exists()) {
    FileUtil.del(versionDir);
}

// 10. 调用 AI 核心能力：
//    与老逻辑不同，这里必须把输出目录传下去，
//    让底层把代码直接保存到“版本目录”，而不是固定目录
Flux<String> codeStream = aiCodeGeneratorFacade.generateAndSaveCodeStream(
        message,
        codeGenTypeEnum,
        appId,
        sourceDirPath
);

return streamHandlerExecutor.doExecute(codeStream, chatHistoryService, appId, loginUser, codeGenTypeEnum)
    
    // ============================================修改点============================================
    .doFinally(signalType -> {
    // 流结束时清理（无论成功/失败/取消）
    MonitorContextHolder.clearContext();
});
// ============================================修改点============================================
```

------





### 4.指标收集器

- 所有 `Counter、Timer` 等指标最终都会注册到 `Micrometer` 的指标注册中心，之后可以被 `Prometheus、Grafana` 等监控系统采集。

```Java
@Component
@Slf4j
public class AiModelMetricsCollector {

    /**
     * Micrometer 的指标注册中心
     *
     * 所有 Counter、Timer 等指标最终都会注册到这里，
     * 之后可以被 Prometheus、Grafana 等监控系统采集。
     */
    @Resource
    private MeterRegistry meterRegistry;

    /**
     * 请求次数指标缓存
     *
     * key 由 userId、appId、modelName、status 组成，
     * value 是对应的 Counter。
     *
     * 作用：
     * 避免每次记录指标时都重新创建 Counter，
     * 提高性能，并保证同一组标签对应同一个指标对象。
     */
    private final ConcurrentMap<String, Counter> requestCountersCache = new ConcurrentHashMap<>();

    /**
     * 错误次数指标缓存
     *
     * key 由 userId、appId、modelName、errorMessage 组成，
     * value 是对应的 Counter。
     *
     * 作用：
     * 针对不同错误信息分别统计出现次数。
     */
    private final ConcurrentMap<String, Counter> errorCountersCache = new ConcurrentHashMap<>();

    /**
     * Token 消耗指标缓存
     *
     * key 由 userId、appId、modelName、tokenType 组成，
     * value 是对应的 Counter。
     *
     * 作用：
     * 用于分别统计不同类型 Token 的累计消耗量，
     * 例如 input_token、output_token 等。
     */
    private final ConcurrentMap<String, Counter> tokenCountersCache = new ConcurrentHashMap<>();

    /**
     * 响应时间指标缓存
     *
     * key 由 userId、appId、modelName 组成，
     * value 是对应的 Timer。
     *
     * 作用：
     * 用于统计某个用户、某个应用、某个模型的响应耗时情况。
     */
    private final ConcurrentMap<String, Timer> responseTimersCache = new ConcurrentHashMap<>();


    /**
     * 记录请求次数
     *
     * @param userId    用户 ID
     * @param appId     应用 ID
     * @param modelName 模型名称
     * @param status    请求状态，例如 success / fail
     */
    public void recordRequest(String userId, String appId, String modelName, String status) {
        // 使用标签拼接成唯一 key，用于从缓存中获取或创建 Counter
        String key = String.format("%s_%s_%s_%s", userId, appId, modelName, status);

        // 如果缓存中不存在，则创建一个新的 Counter 并注册到 meterRegistry
        Counter counter = requestCountersCache.computeIfAbsent(key, k ->
                Counter.builder("ai_model_requests_total")
                        .description("AI模型总请求次数")
                        .tag("user_id", userId)
                        .tag("app_id", appId)
                        .tag("model_name", modelName)
                        .tag("status", status)
                        .register(meterRegistry)
        );

        // 请求次数 +1
        counter.increment();
    }


    /**
     * 记录错误次数
     *
     * @param userId       用户 ID
     * @param appId        应用 ID
     * @param modelName    模型名称
     * @param errorMessage 错误信息
     */
    public void recordError(String userId, String appId, String modelName, String errorMessage) {
        // 用用户、应用、模型、错误信息生成唯一 key
        String key = String.format("%s_%s_%s_%s", userId, appId, modelName, errorMessage);

        // 如果缓存中没有对应 Counter，就创建并注册
        Counter counter = errorCountersCache.computeIfAbsent(key, k ->
                Counter.builder("ai_model_errors_total")
                        .description("AI模型错误次数")
                        .tag("user_id", userId)
                        .tag("app_id", appId)
                        .tag("model_name", modelName)
                        .tag("error_message", errorMessage)
                        .register(meterRegistry)
        );

        // 错误次数 +1
        counter.increment();
    }


    /**
     * 记录 Token 消耗总量
     *
     * @param userId     用户 ID
     * @param appId      应用 ID
     * @param modelName  模型名称
     * @param tokenType  Token 类型，例如 input / output / total
     * @param tokenCount 本次消耗的 Token 数量
     */
    public void recordTokenUsage(String userId, String appId, String modelName,
                                 String tokenType, long tokenCount) {
        // 使用用户、应用、模型、Token 类型构造唯一 key
        String key = String.format("%s_%s_%s_%s", userId, appId, modelName, tokenType);

        // 如果缓存中没有，则创建并注册 Counter
        Counter counter = tokenCountersCache.computeIfAbsent(key, k ->
                Counter.builder("ai_model_tokens_total")
                        .description("AI模型Token消耗总数")
                        .tag("user_id", userId)
                        .tag("app_id", appId)
                        .tag("model_name", modelName)
                        .tag("token_type", tokenType)
                        .register(meterRegistry)
        );

        // 将本次 token 消耗量累加到 Counter 中
        counter.increment(tokenCount);
    }


    /**
     * 记录模型响应时间
     *
     * @param userId    用户 ID
     * @param appId     应用 ID
     * @param modelName 模型名称
     * @param duration  本次请求耗时
     */
    public void recordResponseTime(String userId, String appId, String modelName, Duration duration) {
        // 以用户、应用、模型维度构造唯一 key
        String key = String.format("%s_%s_%s", userId, appId, modelName);

        // 如果缓存中没有对应 Timer，则创建并注册
        Timer timer = responseTimersCache.computeIfAbsent(key, k ->
                Timer.builder("ai_model_response_duration_seconds")
                        .description("AI模型响应时间")
                        .tag("user_id", userId)
                        .tag("app_id", appId)
                        .tag("model_name", modelName)
                        .register(meterRegistry)
        );

        // 记录本次请求耗时
        timer.record(duration);
    }
}
```

------





### 5.AI调用监听器

它实现了 `ChatModelListener` 接口，说明它会**监听模型调用生命周期中的事件**。

你可以把它理解成：

- `onRequest()`：**模型请求刚发出去时触发**
- `onResponse()`：**模型正常返回结果时触发**
- `onError()`：模型调用发生异常时触发

而这个监听器的职责，不是处理业务结果，而是**专门负责监控统计**。

**比如最终会统计出：**

- 某个用户调用了多少次模型
- 某个应用调用成功多少次、失败多少次
- 某个模型平均响应多长时间
- 某个模型消耗了多少 input/output token
- 某个错误出现了多少次

```Java
/**
 * AI 模型调用监控埋点监听器
 */
@Component
@Slf4j
public class AiModelMonitorListener implements ChatModelListener {

    // 用于存储请求开始时间的键
    private static final String REQUEST_START_TIME_KEY = "request_start_time";
    // 用于监控上下文传递（因为请求和响应事件的触发不是同一个线程）
    private static final String MONITOR_CONTEXT_KEY = "monitor_context";

    @Resource
    private AiModelMetricsCollector aiModelMetricsCollector;


    /**
     * 请求开始时：记录开始时间、记录一次“请求已发起”
     * @param requestContext
     */
    @Override
    public void onRequest(ChatModelRequestContext requestContext) {
        // 获取当前时间戳，但未作任何处理
        requestContext.attributes().put(REQUEST_START_TIME_KEY, Instant.now());
        // 从监控上下文中获取信息
        MonitorContext monitorContext = MonitorContextHolder.getContext();
        String userId = monitorContext.getUserId();
        String appId = monitorContext.getAppId();
        requestContext.attributes().put(MONITOR_CONTEXT_KEY, monitorContext);
        // 获取模型名称
        String modelName = requestContext.chatRequest().modelName();
        // 记录请求指标
        aiModelMetricsCollector.recordRequest(userId, appId, modelName, "started");
    }


    /**
     * 响应成功时：记录一次“请求成功”、统计耗时、统计 token 使用量
     * @param responseContext
     */
    @Override
    public void onResponse(ChatModelResponseContext responseContext) {
        // 从属性中获取监控信息（由 onRequest 方法存储）
        Map<Object, Object> attributes = responseContext.attributes();
        // 从监控上下文中获取信息
        MonitorContext context = (MonitorContext) attributes.get(MONITOR_CONTEXT_KEY);
        String userId = context.getUserId();
        String appId = context.getAppId();
        // 获取模型名称
        String modelName = responseContext.chatResponse().modelName();
        // 记录成功请求
        aiModelMetricsCollector.recordRequest(userId, appId, modelName, "success");
        // 记录响应时间
        recordResponseTime(attributes, userId, appId, modelName);
        // 记录 Token 使用情况
        recordTokenUsage(responseContext, userId, appId, modelName);
    }


    /**
     * 响应失败时：记录一次“请求失败”、记录错误信息、统计失败耗时
     * @param errorContext
     */
    @Override
    public void onError(ChatModelErrorContext errorContext) {
        // 从监控上下文中获取信息
        MonitorContext context = MonitorContextHolder.getContext();
        String userId = context.getUserId();
        String appId = context.getAppId();
        // 获取模型名称和错误类型
        String modelName = errorContext.chatRequest().modelName();
        String errorMessage = errorContext.error().getMessage();
        // 记录失败请求
        aiModelMetricsCollector.recordRequest(userId, appId, modelName, "error");
        aiModelMetricsCollector.recordError(userId, appId, modelName, errorMessage);
        // 记录响应时间（即使是错误响应）
        Map<Object, Object> attributes = errorContext.attributes();
        recordResponseTime(attributes, userId, appId, modelName);
    }


    /**
     * 记录模型响应时间
     *
     * 处理流程：
     * 1. 从 attributes 中取出请求开始时间
     * 2. 计算从开始到当前的时间差
     * 3. 上报到指标采集器
     */
    private void recordResponseTime(Map<Object, Object> attributes, String userId, String appId, String modelName) {
        // 获取请求开始时间
        Instant startTime = (Instant) attributes.get(REQUEST_START_TIME_KEY);
        // 计算本次请求耗时
        Duration responseTime = Duration.between(startTime, Instant.now());
        // 上报响应时间指标
        aiModelMetricsCollector.recordResponseTime(userId, appId, modelName, responseTime);
    }


    /**
     * 记录 Token 使用情况
     *
     * 处理流程：
     * 1. 从响应元数据中获取 TokenUsage
     * 2. 分别记录 input / output / total token 数量
     */
    private void recordTokenUsage(ChatModelResponseContext responseContext, String userId, String appId, String modelName) {
        // 从响应元数据中获取 Token 使用情况
        TokenUsage tokenUsage = responseContext.chatResponse().metadata().tokenUsage();

        // 如果模型返回了 token 统计信息，则分别记录
        if (tokenUsage != null) {
            aiModelMetricsCollector.recordTokenUsage(userId, appId, modelName, "input", tokenUsage.inputTokenCount());
            aiModelMetricsCollector.recordTokenUsage(userId, appId, modelName, "output", tokenUsage.outputTokenCount());
            aiModelMetricsCollector.recordTokenUsage(userId, appId, modelName, "total", tokenUsage.totalTokenCount());
        }
    }
}
```

------



- **然后需要将监听器注册到 AI 模型配置中。修改 `ReasoningStreamingChatModelConfig` 和 `StreamingChatModelConfig`：**

```Java
@Resource
private AiModelMonitorListener aiModelMonitorListener;

@Bean
@Scope("prototype")
public StreamingChatModel streamingChatModelPrototype() {
    return OpenAiStreamingChatModel.builder()
            .apiKey(apiKey)
            .baseUrl(baseUrl)
            .modelName(modelName)
            .maxTokens(maxTokens)
            .temperature(temperature)
            .logRequests(logRequests)
            .logResponses(logResponses)
            .listeners(List.of(aiModelMonitorListener))
            .build();
}
```

------





### 6.Prometheus 配置

- **需要配⁠⁠⁠⁠⁠⁠⁠⁠⁠⁠⁠置 Prometheus 定期从我们﻿﻿﻿﻿﻿﻿﻿﻿﻿﻿﻿的应用拉取监控⁢⁢⁢⁢⁢⁢⁢⁢⁢⁢数据⁢**

在当前项目下创建一个 `Prometheus.yml` 配置文件，制定好应用服务器地址，并指定好每 10s 抓取一次数据

```yml
# Prometheus 配置文件
global:
  scrape_interval: 15s      # 全局抓取间隔
  evaluation_interval: 15s  # 规则评估间隔

# 告警管理器配置 (可选)
alerting:
  alertmanagers:
    - static_configs:
        - targets:
          # - alertmanager:9093

# 规则文件配置
rule_files:
# - "alert_rules.yml"  # 可以添加告警规则

# 抓取配置
scrape_configs:
  # Prometheus 自身监控
  - job_name: 'prometheus'
    static_configs:
      - targets: ['localhost:9090']

  # Spring Boot 应用监控
  - job_name: 'ai-code-creater'
    metrics_path: '/api/actuator/prometheus'  # Spring Boot Actuator 端点
    static_configs:
      - targets: ['localhost:8123']  # 应用服务器地址
    scrape_interval: 10s  # 每 10 秒抓取一次
    scrape_timeout: 10s   # 抓取超时时间
```

------

- **然后重新启动 Prometheus ：**

```
prometheus.exe --config.file=<配置文件路径>D:\IDEA_Projects\yu-ai-agent\ai-code-creater\Prometheus.yml
```

------









# 微服务改造

1. **设计上：怎么划分服务？** 按照什么原则将单体应用拆分为多个微服务？
2. **实现上：用什么技术？** 选择什么技术栈来支撑微服务架构？
3. **治理上：如何管理服务？** 如何实现服务注册发现、配置管理、监控等？

------





## **通用模块**

这些模块不是独立的服务，而是被其他服务依赖的公共组件：

- `yu-ai-code-common`：包含所有服务公用的代码，如**异常处理、工具类、常量定义等**，为其他服务提供基础设施支持。
- `yu-ai-code-model`：统一的数据模型定义，包含**实体类、DTO、VO、枚举类**等，确保各服务间数据格式的一致性。
- `yu-ai-code-client`：定义需要**内部调用的服务接口**，作为**服务间通信的契约**，实现服务间的松耦合。

------





## **业务服务**

这些是真正⁠⁠⁠的微服务，（原则上来说）每个﻿服务﻿都有﻿独立的⁢进程和⁢端口：

- **用户服务**：作为整个系统的基础，统一管理用户状态和权限。负责用户注册、登录、注销、权限验证等核心功能。**由于几乎所有业务都需要用户信息，所以用户服务是其他服务的基础依赖。**
- **应用服务**：业务核心服务，负责应用的完整生命周期管理。包括**应用创建、编辑、删除、对话历史、代码保存、项目下载等功能**，集成了文件操作、缓存管理、限流控制等能力。
- **AI 服务**：专门处理代码生成相关功能，**集成 LangChain4j 支持流式响应。包含 AI 模型调用、代码生成功能**。
- **截图服务**：独立的 IO 密集型服务，**使用 `Selenium WebDriver` 进行网页截图**。由于截图操作比较消耗 CPU 和内存资源，独立部署便于单独优化和扩展。

------





## **服务间的依赖关系**

在微服务架⁠⁠⁠构中，服务间不能像之前那样通过本地方﻿﻿﻿法调用，**需要通过网⁢⁢⁢络进行远程调用**。我‍‍‍们的服务间依赖关系如下：

- 应用服务 → 用户服务：应用服务需要调用用户服务获取用户信息、进行权限验证。比如创建应用时需要验证用户身份，查询应用列表时需要获取创建者信息。
- 应用服务 → 截图服务：应用服务在生成代码后，需要调用截图服务生成应用的预览图，方便用户查看效果。
- ***应用服务 ←→ AI 服务：这是一个双向依赖关系。应用服务需要调用 AI 服务进行代码生成，而 AI 服务在生成完成后需要调用应用服务保存聊天历史。***

------





## 服务划分结果

通过梳理服务间依赖关系，我们发现应用服务和 AI 服务是强绑定的关系，**因此 AI 服务不能作为完全独立的服务占用独立端口，而是作为 SDK 模块引入到应用服务中**。

**最后，我们需要整理出一个微服务划分表。**包括各个服务的职责、端口分配、路由规划和依赖关系，便于后续的开发实现。

| 服务名称                | 端口和路由前缀                              | 主要功能                                   | 依赖服务                         |
| ----------------------- | ------------------------------------------- | ------------------------------------------ | -------------------------------- |
| ***通用模块***          |                                             |                                            |                                  |
| `yu-ai-code-commo⁠⁠⁠n`     | -                                           | 注解、异常处理、工具类、常量、公共响应类   | -                                |
| `yu-ai-code-model`      | -                                           | 实体类、DTO、﻿﻿﻿VO、枚举类、AI 模型类         | common                           |
| `yu-ai-cod⁢⁢⁢e-client`     | -                                           | 服务接口定义、内部调用契约                 | c‍‍‍ommon、model                    |
| ***业务服务***          |                                             |                                            |                                  |
| `yu-ai-code-user`       | 8124 端口，`/api/user/**`                   | 用户管理、权限认证、用户信息维护           | Redis、MySQL                     |
| `yu-ai-code-app`        | 8125 端口，`/api/app** /api/chatHistory/**` | 应用管理、聊天历史、项目下载、代码解析保存 | Redis、MySQL、用户服务、截图服务 |
| `yu-ai⁠⁠⁠-code-ai `        | -                                           | A﻿I代﻿码生成⁢、模型⁢管理 ⁢                      | ‍ AI ‍大模型                       |
| `yu-ai-code-screenshot` | 8127 端口，`/api/screenshot/**`             | 网页截图、图片处理、对象存储               | 腾讯云 COS                       |

------



- **微服务部署架构图**

<img src="./AI零代码生成平台.assets/XMNglFDkM6kaTZjV.webp" alt="img" style="zoom: 33%;" />

------



- **项目模块：**

<img src="./AI零代码生成平台.assets/Screenshot 2026-04-14 202244.png" style="zoom: 67%;" />

------







## ai-code-common模块

- common/ 公共请求响应类（BaseResponse、ResultUtils等）
- constant/ 常量
- exception/ 异常处理
- generator/ 代码生成器
- utils/ 工具类（除 WebScreenshotUtils 外）
- config/ 配置类（JsonConfig、CorsConfig、CosClientConfig）
- manager/ 通用能力（CosManager）
- annotation/ 注解（AuthCheck）

------



- 注意：引入 `CosManager` 后，必须要填写 COS 配置【对象存储的用户名密码等参数】才能启﻿﻿﻿动项目，但有些模块是不需要 `Co⁢⁢⁢sManager` 的。因此我们**添‍‍‍加条件注解，没配置就不加载：**

```Java
@Configuration
@ConfigurationProperties(prefix = "cos.client")
// 确保了只有配置了这些属性才会初始化这个 Config Bean
@ConditionalOnProperty(
        prefix = "cos.client",
        name = {"host", "secretId", "secretKey", "region", "bucket"}
)
@Data
public class CosClientConfig {
}
```

------

在 `CosManager` 类中确保了⁠⁠⁠只有在配置了 COS 相关参数时﻿﻿才会﻿加载相关的 ⁢⁢Bea⁢n

```java
@Component
// 如果 COSClient 这个 Bean 没有加载，那么这个 CosManager 也不会加载
@ConditionalOnBean(COSClient.class)
@Slf4j
public class CosManager {
}
```

------







## ai-code-model实体类模块

- **需要依赖刚刚的公共模块类：**

```xml
<dependencies>
    <dependency>
        <groupId>com.ryy</groupId>
        <artifactId>ai-code-common</artifactId>
        <version>1.0-SNAPSHOT</version>
    </dependency>
</dependencies>
```

------



然后迁移整个 model 包：

- `model/entity/` 实体类（`User、App、ChatHistory`）
- `model/dto/` 数据传输对象
- `model/vo/` 视图对象
- `model/enums/` 枚举类

------







## ai-code-client 服务接口模块

这个模块**定义了需要被其他服务内部调用的接口**。

当作是一个客户端，作用是**为其他模块提供对应的需要服务**。即可以理解为各个模块间内部的互相远程调用需要用到的方法。

------

- **比如内部的用户调用接口：**

其中， `getLoginUser` 方法比较特殊，**由于 `HttpServletRequest` 对象不好在网络中传递，因此采用静态方法，避免跨服务调用**：

```Java
public interface InnerUserService {

    List<User> listByIds(Collection<? extends Serializable> ids);

    User getById(Serializable id);

    UserVO getUserVO(User user);

    // 静态方法，避免跨服务调用
    static User getLoginUser(HttpServletRequest request) {
        Object userObj = request.getSession().getAttribute(USER_LOGIN_STATE);
        User currentUser = (User) userObj;
        if (currentUser == null || currentUser.getId() == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }
        return currentUser;
    }
}
```

------







## Dubbo 远程调用

- **引入依赖：**

```xml
<!-- Dubbo -->
<dependency>
    <groupId>org.apache.dubbo</groupId>
    <artifactId>dubbo-spring-boot-starter</artifactId>
</dependency>
<dependency>
    <groupId>org.apache.dubbo</groupId>
    <artifactId>dubbo-nacos-spring-boot-starter</artifactId>
</dependency>
```

------



- **配置文件：**

```yml
# Dubbo
dubbo:
  registry:
    address: nacos://127.0.0.1:8848?username=nacos&password=nacos
    register-mode: instance
  protocol:
    name: tri
    port: 50053
  consumer:
    timeout: 120000
  provider:
    timeout: 120000
```

------



**`@DubboReference` 是 Dubbo 框架提供的服务消费者注解，用于自动注入远程服务的代理对象**。当 Spring 容器启动时，Dubbo 会根据注解信息从 `Nacos` 注册中心查找对应的服务提供者地址，然后创建代理对象注入到当前服务中。通过这个代理对象调用方法时，Dubbo 会自动将调用请求通过网络发送到远程服务，并将结果返回给调用方，整个过程对开发者来说 **就像调用本地方法一样简单**。

------

- **比如应用服务需要远程调用到用户服务和截图服务，就可以使用这个注解标注**

```java
@DubboReference
private InnerUserService userService;

@DubboReference
private InnerScreenshotService screenshotService;
```

------

- **调用时就和内部调用一样**

```Java
			// 调用截图服务生成截图并上传
            String screenshotUrl = screenshotService.generateAndUploadScreenshot(appUrl);
```

------



- **启动类上需要加上的注解：**

```Java
@EnableDubbo
```

------













































