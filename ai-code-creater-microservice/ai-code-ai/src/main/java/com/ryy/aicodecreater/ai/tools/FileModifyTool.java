package com.ryy.aicodecreater.ai.tools;

import cn.hutool.json.JSONObject;
import com.ryy.aicodecreater.constant.AppConstant;
import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;
import dev.langchain4j.agent.tool.ToolMemoryId;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

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
@Component
public class FileModifyTool extends BaseTool{

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

    @Override
    public String getToolName() {
        return "modifyFile";
    }

    @Override
    public String getDisplayName() {
        return "修改文件";
    }

    @Override
    public String generateToolExecutedResult(JSONObject arguments) {
        String relativeFilePath = arguments.getStr("relativeFilePath");
        String oldContent = arguments.getStr("oldContent");
        String newContent = arguments.getStr("newContent");
        // 显示对比内容
        return String.format("""
                [工具调用] %s %s
                
                替换前：
                ```
                %s
                ```
                
                替换后：
                ```
                %s
                ```
                """, getDisplayName(), relativeFilePath, oldContent, newContent);
    }
}
