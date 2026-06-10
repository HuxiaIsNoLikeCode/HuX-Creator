package com.hux.creator.ai.generator;

import com.hux.creator.ai.prompt.PromptTemplate;
import org.springframework.stereotype.Component;

@Component
public class CoverGenerator extends BaseGenerator {

    private static final String SYSTEM_PROMPT = "你是一个专业的短视频运营专家，精通各平台的封面设计和发布文案优化。你了解平台的推荐算法和用户浏览习惯，能创作出高点击率的封面文案和引发互动的发布文案。";

    private static final String TEMPLATE = "请为以下视频生成封面文案和发布文案：\n\n【选题标题】{{title}}\n【目标平台】{{platform}}\n【选定标题】{{chosenTitle}}\n【开头Hook】{{hook}}\n【视频脚本】\n{{script}}\n\n请生成以下内容：\n\n## 一、封面文案\n\n提供3个封面文案方案：\n1. **大字标题方案**：6-10个大字，直接点出核心看点\n2. **悬念方案**：抛出问题或反常识观点，激发好奇心\n3. **数据方案**：用具体数字增加可信度和吸引力\n\n每个方案标注：\n- 建议的字体大小和排版位置\n- 建议的配色方案\n- 是否需要配合人脸/产品图\n\n## 二、发布文案\n\n提供2个版本的发布文案：\n1. **简洁版**（50字以内）：适合快速浏览\n2. **详细版**（150-200字）：包含话题引导和互动设计\n\n两个版本都要求：\n- 融入平台热门话题标签\n- 包含互动引导语（提问/投票/挑战等）\n- 自然植入关键词利于搜索推荐\n\n## 三、话题标签\n\n推荐5-8个相关话题标签，按热度和相关性排序。";

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
        return "coverCopy";
    }
}
