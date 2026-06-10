package com.hux.creator.ai.generator.pipeline;

import com.hux.creator.ai.generator.BaseGenerator;
import com.hux.creator.ai.prompt.PromptTemplate;
import org.springframework.stereotype.Component;

@Component
public class TypeClassifier extends BaseGenerator {

    private static final String SYSTEM_PROMPT = "你是一个专业的视频内容策划师，擅长分析视频选题并判断最适合的视频类型。你需要根据用户提供的项目信息，判断这个视频最适合哪种类型。";

    private static final String TEMPLATE = "请根据以下项目信息，判断这个视频最适合哪种类型：\n\n【视频标题】{{title}}\n【视频描述】{{description}}\n【目标平台】{{platform}}\n【目标用户】{{targetUser}}\n【视频时长】{{duration}}秒\n\n请从以下三种类型中选择最合适的一种：\n\n1. MARKETING（营销类）：适合产品推广、品牌宣传、带货视频。特点是结构化脚本，明确产品展示顺序、镜头切换逻辑。\n\n2. KNOWLEDGE（知识科普类）：适合教程、科普、知识分享。特点是重点描述核心知识点和讲解逻辑，使用非结构化文本。\n\n3. STORY（故事类）：适合短剧、故事、情感类内容。特点是详细的分镜脚本，标注人物动作、场景转换。\n\n请严格按照以下JSON格式返回，不要添加任何其他内容：\n```json\n{\"type\": \"MARKETING\", \"reason\": \"选择原因的简要说明\"}\n```\n\n注意：type字段只能是 MARKETING、KNOWLEDGE 或 STORY 之一。";

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
        return "typeClassification";
    }
}
