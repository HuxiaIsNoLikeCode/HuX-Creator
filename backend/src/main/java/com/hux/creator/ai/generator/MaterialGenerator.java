package com.hux.creator.ai.generator;

import com.hux.creator.ai.prompt.PromptTemplate;
import org.springframework.stereotype.Component;

@Component
public class MaterialGenerator extends BaseGenerator {

    private static final String SYSTEM_PROMPT = "你是一个专业的视频素材规划师，擅长根据分镜脚本精确规划所需的视觉素材、音频素材和辅助资源。你熟悉各素材平台的资源类型和获取方式，能提供高性价比的素材方案。";

    private static final String TEMPLATE = "请根据以下分镜脚本，生成完整的素材清单：\n\n【分镜脚本】\n{{storyboard}}\n\n【风格定位】{{style}}\n\n请按以下分类列出所有所需素材：\n\n1. **实拍素材**：需要自行拍摄的画面，标注拍摄要点\n2. **图片素材**：需要的静态图片，描述具体内容和风格要求\n3. **视频素材**：可从素材库获取的视频片段，描述内容和时长\n4. **音频素材**：背景音乐、音效的具体风格和节奏要求\n5. **图形素材**：需要的图标、贴纸、表情包等\n6. **字幕样式**：字体、颜色、动画效果建议\n7. **特效素材**：转场特效、粒子效果等\n\n每项素材请标注：\n- 素材描述\n- 建议的获取方式（自制/素材库/AI生成）\n- 推荐的素材平台（如适用）\n- 预估成本（免费/付费）\n- 优先级（必需/建议/可选）";

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
        return "materialList";
    }
}
