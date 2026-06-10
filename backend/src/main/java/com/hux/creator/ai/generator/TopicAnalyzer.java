package com.hux.creator.ai.generator;

import com.hux.creator.ai.prompt.PromptTemplate;
import org.springframework.stereotype.Component;

@Component
public class TopicAnalyzer extends BaseGenerator {

    private static final String SYSTEM_PROMPT = "你是一个专业的视频选题分析师，拥有丰富的短视频行业经验。你能从选题潜力、受众匹配度、平台趋势、竞争环境等多个维度进行深入分析。请用结构化的JSON格式输出分析结果，确保分析具有可操作性。";

    private static final String TEMPLATE = "请对以下视频选题进行全面分析：\n\n【选题标题】{{title}}\n【目标平台】{{platform}}\n【目标用户】{{targetUser}}\n【风格定位】{{style}}\n\n请从以下维度进行分析：\n1. 选题潜力评分（1-10分）及理由\n2. 目标受众画像与需求匹配度\n3. 当前平台趋势契合度\n4. 竞争环境分析（同类内容数量与质量）\n5. 差异化切入点建议\n6. 风险提示与注意事项\n\n请以JSON格式输出，包含以下字段：score, reason, audienceMatch, trendFit, competition, differentiation, risks。";

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
        return "topicAnalysis";
    }
}
