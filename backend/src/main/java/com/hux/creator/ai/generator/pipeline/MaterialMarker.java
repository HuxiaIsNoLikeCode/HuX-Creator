package com.hux.creator.ai.generator.pipeline;

import com.hux.creator.ai.generator.BaseGenerator;
import com.hux.creator.ai.prompt.PromptTemplate;
import org.springframework.stereotype.Component;

@Component
public class MaterialMarker extends BaseGenerator {

    private static final String SYSTEM_PROMPT = "你是一个专业的视频制作素材规划师，擅长为视频脚本中的每个场景和镜头标记所需的素材。你了解各种素材来源，包括实拍、素材库、AI生成等，能为每个画面元素提供精确的素材建议。";

    private static final String TEMPLATE = "请为以下视频脚本的每个场景标记所需素材：\n\n【视频脚本】\n{{script}}\n\n【场景和镜头结构】\n{{scenes}}\n\n请为每个场景标记以下类型的素材：\n\n1. **画面素材 (IMAGE)**：背景画面、场景图、产品图等\n2. **人物素材 (PERSON)**：出镜人物、演员、动画角色等\n3. **道具素材 (PROP)**：产品、工具、装饰物等\n4. **背景素材 (BACKGROUND)**：背景图、背景视频、环境素材\n5. **特效素材 (EFFECT)**：转场特效、动画特效、粒子效果等\n6. **音频素材 (AUDIO)**：背景音乐、音效、配音等\n\n每个素材需要标注：\n- name: 素材名称\n- description: 素材描述\n- position: 在画面中的位置（左上/居中/右下等）\n- sourceSuggestion: 来源建议（实拍/素材库下载/AI生成/自制）\n- prompt: 如需AI生成，提供生成提示词\n\n请严格按照以下JSON格式返回：\n\n```json\n{\n  \"materials\": [\n    {\n      \"sceneNumber\": 1,\n      \"shotNumber\": 1,\n      \"items\": [\n        {\n          \"materialType\": \"IMAGE\",\n          \"name\": \"素材名称\",\n          \"description\": \"素材描述\",\n          \"position\": \"画面位置\",\n          \"sourceSuggestion\": \"来源建议\",\n          \"prompt\": \"AI生成提示词（如适用）\"\n        }\n      ]\n    }\n  ]\n}\n```";

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
        return "materialMarking";
    }
}
