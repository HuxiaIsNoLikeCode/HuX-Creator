package com.hux.creator.ai.generator;

import com.hux.creator.ai.prompt.PromptTemplate;
import org.springframework.stereotype.Component;

@Component
public class StoryboardGenerator extends BaseGenerator {

    private static final String SYSTEM_PROMPT = "你是一个专业的视频分镜师，拥有丰富的短视频和广告分镜经验。你能将文字脚本转化为精确的视觉语言，每个镜头都考虑构图、运镜、景别、转场和节奏，确保画面与内容完美配合。";

    private static final String TEMPLATE = "请将以下视频脚本转化为详细的分镜脚本：\n\n【视频脚本】\n{{script}}\n\n【视频时长】{{duration}}秒\n【风格定位】{{style}}\n\n分镜要求：\n1. 每个镜头控制在3-8秒\n2. 每个分镜包含以下信息：\n   - 镜号\n   - 时间段（起止时间）\n   - 景别（特写/近景/中景/全景/远景）\n   - 画面描述（具体的视觉内容）\n   - 运镜方式（推/拉/摇/移/固定/跟拍等）\n   - 口播/字幕文字\n   - 转场方式（切/淡入淡出/滑动/缩放等）\n   - 配乐/音效建议\n3. 整体节奏要有变化，避免单调\n4. 开头镜头要有视觉冲击力\n5. 结尾镜头要适合引导互动\n\n请以表格或结构化格式输出每个分镜。";

    @Override
    protected PromptTemplate getTemplate() {
        return new PromptTemplate(TEMPLATE);
    }

    @Override
    protected String getSystemPrompt() {
        return SYSTEM_PROMPT;
    }

    @Override
    protected String getSectionName() {
        return "storyboard";
    }
}
