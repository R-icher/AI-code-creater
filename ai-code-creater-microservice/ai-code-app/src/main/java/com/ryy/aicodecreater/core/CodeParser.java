package com.ryy.aicodecreater.core;

import com.ryy.aicodecreater.ai.model.HtmlCodeResult;
import com.ryy.aicodecreater.ai.model.MultiFileCodeResult;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
@Deprecated
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