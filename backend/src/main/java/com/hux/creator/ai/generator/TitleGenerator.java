package com.hux.creator.ai.generator;

import com.hux.creator.ai.prompt.PromptTemplate;
import org.springframework.stereotype.Component;

@Component
public class TitleGenerator extends BaseGenerator {

    private static final String SYSTEM_PROMPT = "你是一个资深的短视频标题策划师，精通各平台的标题算法和用户心理。你能创作出既吸引眼球又不标题党的优质标题，善于运用数字、悬念、痛点、共鸣等技巧提升点击率。";

    private static final String TEMPLATE = "请为以下视频生成3-5个高质量标题选项：\n\n【选题标题】{{title}}\n【目标平台】{{platform}}\n【目标用户】{{targetUser}}\n【选题分析】{{topicAnalysis}}\n\n标题要求：\n1. 每个标题控制在15-25字之间\n2. 融入平台热门关键词和表达方式\n3. 至少包含1个使用数字的标题\n4. 至少包含1个使用悬念/疑问句式的标题\n5. 符合目标用户的语言习惯和兴趣点\n6. 避免违规词和过度夸张的表述\n\n请为每个标题附上简短的设计思路说明，解释为什么这个标题能吸引目标用户。";

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
        return "titleOptions";
    }
}
