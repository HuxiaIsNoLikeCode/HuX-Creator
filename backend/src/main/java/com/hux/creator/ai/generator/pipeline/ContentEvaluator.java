package com.hux.creator.ai.generator.pipeline;

import com.hux.creator.ai.generator.BaseGenerator;
import com.hux.creator.ai.prompt.PromptTemplate;
import org.springframework.stereotype.Component;

@Component
public class ContentEvaluator extends BaseGenerator {

    private static final String SYSTEM_PROMPT = "你是一个专业的视频内容质量评估专家，擅长从多个维度评估视频脚本的质量。你会给出客观、详细的评分和具体的优化建议。";

    private static final String TEMPLATE = "请评估以下视频脚本的质量：\n\n【视频信息】\n标题：{{title}}\n类型：{{videoType}}\n平台：{{platform}}\n时长：{{duration}}秒\n\n【脚本内容】\n{{script}}\n\n【场景结构】\n{{scenes}}\n\n【素材清单】\n{{materials}}\n\n请从以下维度进行评分（0-100分）：\n\n1. **完整性 (completeness)**：脚本是否涵盖了所有必要的内容元素\n2. **连贯性 (coherence)**：场景之间的逻辑是否通顺，过渡是否自然\n3. **视觉适配度 (visualFit)**：视觉类型标记是否合理，画面描述是否清晰\n4. **创意性 (creativity)**：内容是否有新意，能否吸引目标用户\n\n请严格按照以下JSON格式返回：\n\n```json\n{\n  \"overallScore\": 85,\n  \"completenessScore\": 90,\n  \"coherenceScore\": 85,\n  \"visualFitScore\": 80,\n  \"creativityScore\": 85,\n  \"passed\": true,\n  \"suggestions\": [\n    {\n      \"target\": \"script\",\n      \"issue\": \"问题描述\",\n      \"suggestion\": \"具体优化建议\"\n    }\n  ]\n}\n```\n\n评分标准：\n- 90分以上：优秀，可以直接使用\n- 80-89分：良好，有少量改进空间\n- 70-79分：一般，需要优化\n- 70分以下：较差，需要大幅修改\n\n注意：如果 overallScore >= 90，将 passed 设为 true，否则设为 false。";

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
        return "contentEvaluation";
    }
}
