package com.hux.creator.ai.generator;

import com.hux.creator.ai.prompt.PromptTemplate;
import org.springframework.stereotype.Component;

@Component
public class HookGenerator extends BaseGenerator {

    private static final String SYSTEM_PROMPT = "你是一个短视频开头黄金3秒的专家策划师。你深谙各平台的完播率算法和用户注意力机制，能在前3秒内制造强烈的好奇心、共鸣感或视觉冲击，让观众停下滑动的手指。";

    private static final String TEMPLATE = "请为以下视频设计3个不同风格的开头Hook：\n\n【选题标题】{{title}}\n【目标平台】{{platform}}\n【目标用户】{{targetUser}}\n【选定标题】{{chosenTitle}}\n\n请设计以下三种类型的Hook：\n\n1. **悬念型Hook**：抛出一个让人忍不住想知道答案的问题或反常识的观点\n2. **共鸣型Hook**：直接说出目标用户的痛点或共同经历，引发说的就是我的感觉\n3. **视觉冲击型Hook**：描述一个极具画面感的开场场景或动作\n\n每个Hook要求：\n- 控制在30字以内的口播文案\n- 标注建议的画面配合方式\n- 标注预期的情绪引导效果\n- 适合{{platform}}平台的内容风格";

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
        return "hook";
    }
}
