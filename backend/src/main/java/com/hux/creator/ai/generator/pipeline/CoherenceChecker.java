package com.hux.creator.ai.generator.pipeline;

import com.hux.creator.ai.generator.BaseGenerator;
import com.hux.creator.ai.prompt.PromptTemplate;
import org.springframework.stereotype.Component;

@Component
public class CoherenceChecker extends BaseGenerator {

    private static final String SYSTEM_PROMPT = "你是一个专业的视频脚本审稿编辑，擅长检查脚本的逻辑连贯性、视觉适配度和时长控制。你会仔细分析每个场景和镜头之间的衔接，确保整个视频流畅自然。";

    private static final String TEMPLATE = "请检查以下视频脚本的连贯性，并提供优化后的版本：\n\n【原始脚本】\n{{script}}\n\n【场景和镜头结构】\n{{scenes}}\n\n请检查以下方面：\n\n1. **逻辑连贯性**：\n   - 场景之间的过渡是否自然\n   - 故事线是否清晰\n   - 信息传递是否有遗漏或重复\n\n2. **视觉适配度**：\n   - 标记适合用动画展示的内容（ANIMATION）\n   - 标记适合实拍的内容（LIVE）\n   - 标记适合用图表展示的内容（GRAPHIC）\n\n3. **时长控制**：\n   - 按150字/分钟估算\n   - 检查总时长是否合理\n   - 过长的段落是否需要拆分\n\n请返回优化后的完整JSON结构，格式与输入相同。确保：\n- 每个场景都有明确的标题和描述\n- 每个镜头都有完整的元数据\n- visualType字段已正确标记\n- 时长估算合理\n\n```json\n{\"scenes\": [...], \"improvements\": [\"改进点1\", \"改进点2\"]}\n```";

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
        return "coherenceCheck";
    }
}
