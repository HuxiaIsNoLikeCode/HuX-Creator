package com.hux.creator.ai.generator;

import com.hux.creator.ai.prompt.PromptTemplate;
import org.springframework.stereotype.Component;

@Component
public class ScriptGenerator extends BaseGenerator {

    private static final String SYSTEM_PROMPT = "你是一个专业的短视频脚本编剧，擅长创作节奏紧凑、信息密度高、口语化表达的视频脚本。你深谙各类型视频的叙事结构，能在有限的时长内完成起承转合，让观众从头看到尾。";

    private static final String TEMPLATE = "请为以下视频撰写完整的口播脚本：\n\n【选题标题】{{title}}\n【目标平台】{{platform}}\n【目标用户】{{targetUser}}\n【视频时长】{{duration}}秒\n【风格定位】{{style}}\n【选定标题】{{chosenTitle}}\n【开头Hook】{{hook}}\n\n脚本要求：\n1. 严格按照{{duration}}秒的时长来控制字数（口播语速约每分钟250-300字）\n2. 以选定的Hook作为开场，自然过渡到正文\n3. 正文采用清晰的逻辑结构（总分总/问题-方案/故事线等）\n4. 每30-45秒设置一个信息锚点或情绪转折点，防止观众流失\n5. 结尾包含明确的行动号召（关注/点赞/评论引导）\n6. 语言风格口语化、接地气，符合{{platform}}平台调性\n\n请按以下格式输出：\n- 标注每个段落的时间节点\n- 标注关键的情绪节点\n- 标注建议的画面/素材配合";

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
        return "script";
    }
}
