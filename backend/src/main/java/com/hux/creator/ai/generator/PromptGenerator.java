package com.hux.creator.ai.generator;

import com.hux.creator.ai.prompt.PromptTemplate;
import org.springframework.stereotype.Component;

@Component
public class PromptGenerator extends BaseGenerator {

    private static final String SYSTEM_PROMPT = "你是一个精通AI图像和视频生成的提示词工程师。你熟悉Midjourney、Stable Diffusion、DALL-E、Flux、可灵、Runway、Pika等主流AI生成工具的提示词语法和最佳实践，能为每个画面创作出精准、高质量的生成提示词。";

    private static final String TEMPLATE = "请根据以下分镜脚本，为每个关键画面生成AI图像和视频生成提示词。\n\n【分镜脚本】\n{{storyboard}}\n\n【风格定位】{{style}}\n【目标平台】{{platform}}\n\n请严格按照以下格式输出，必须使用【图片提示词】和【视频提示词】作为分隔标记：\n\n【图片提示词】\n\n为每个关键画面生成图片提示词：\n\n1. **GPT Image / DALL-E 提示词**（英文）\n   - 自然语言描述，简洁精准\n\n2. **Flux 提示词**（英文）\n   - 包含风格、构图、光影、色彩等细节\n\n3. **Midjourney 提示词**（英文）\n   - 使用Midjourney标准语法\n   - 包含 --ar（宽高比）、--style、--v 等参数\n\n【视频提示词】\n\n为每个关键画面生成视频提示词：\n\n1. **可灵(Kling)视频提示词**（中文）\n   - 描述动态画面的运动方式和变化\n\n2. **Runway视频提示词**（英文）\n   - 描述运动轨迹和时间变化\n\n3. **Pika视频提示词**（英文）\n   - 简洁的动态描述\n\n每个提示词请标注对应的镜号和建议的生成参数（分辨率、时长等）。";

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
        return "imagePrompt";
    }
}
