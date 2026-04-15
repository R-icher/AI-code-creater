package com.ryy.aicodecreater.core;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import com.ryy.aicodecreater.ai.model.HtmlCodeResult;
import com.ryy.aicodecreater.ai.model.MultiFileCodeResult;
import com.ryy.aicodecreater.model.enums.CodeGenTypeEnum;

import java.io.File;
import java.nio.charset.StandardCharsets;

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
@Deprecated
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