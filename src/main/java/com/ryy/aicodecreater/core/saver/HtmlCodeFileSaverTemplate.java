package com.ryy.aicodecreater.core.saver;

import cn.hutool.core.util.StrUtil;
import com.ryy.aicodecreater.ai.model.HtmlCodeResult;
import com.ryy.aicodecreater.exception.BusinessException;
import com.ryy.aicodecreater.exception.ErrorCode;
import com.ryy.aicodecreater.model.enums.CodeGenTypeEnum;

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
