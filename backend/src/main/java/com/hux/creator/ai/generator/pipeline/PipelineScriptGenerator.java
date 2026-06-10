package com.hux.creator.ai.generator.pipeline;

import com.hux.creator.ai.generator.BaseGenerator;
import com.hux.creator.ai.prompt.PromptTemplate;
import org.springframework.stereotype.Component;

@Component
public class PipelineScriptGenerator extends BaseGenerator {

    private static final String SYSTEM_PROMPT = "你是一个专业的视频脚本编剧，精通各类视频的脚本创作。你能根据视频类型、目标平台和用户需求，创作出结构清晰、节奏紧凑、引人入胜的视频脚本。你特别擅长将脚本拆分为场景和镜头，每个镜头都有明确的画面描述、台词和时长。";

    private static final String TEMPLATE = "请为以下项目生成完整的视频脚本：\n\n【视频标题】{{title}}\n【视频描述】{{description}}\n【目标平台】{{platform}}\n【目标用户】{{targetUser}}\n【视频时长】{{duration}}秒\n【视频类型】{{videoType}}\n【叙事节奏】{{narrativeRhythm}}（0.6-0.8，值越大节奏越紧凑）\n【视觉风格】{{visualStyle}}\n\n{{typeSpecificInstructions}}\n\n请严格按照以下JSON格式返回脚本结构，不要添加任何其他内容：\n\n```json\n{\n  \"content\": \"完整脚本文本（包含所有台词和旁白）\",\n  \"scenes\": [\n    {\n      \"sceneNumber\": 1,\n      \"title\": \"场景标题\",\n      \"description\": \"场景描述\",\n      \"location\": \"场景地点\",\n      \"mood\": \"氛围情绪\",\n      \"estimatedDuration\": 30,\n      \"visualType\": \"LIVE\",\n      \"transition\": \"转场方式\",\n      \"shots\": [\n        {\n          \"shotNumber\": 1,\n          \"description\": \"镜头描述\",\n          \"cameraAngle\": \"摄影角度\",\n          \"cameraMovement\": \"运镜方式\",\n          \"duration\": 10,\n          \"dialogue\": \"台词或旁白\",\n          \"visualType\": \"LIVE\",\n          \"transition\": \"转场\"\n        }\n      ]\n    }\n  ]\n}\n```\n\n字段说明：\n- visualType: ANIMATION(动画)/LIVE(实拍)/GRAPHIC(图表)/MIXED(混合)\n- cameraAngle: 特写/近景/中景/全景/俯拍/仰拍等\n- cameraMovement: 固定/推进/拉远/平移/跟随/环绕等\n- transition: 硬切/淡入淡出/滑动/缩放/闪白等\n- mood: 紧张/轻松/温馨/震撼/幽默/严肃等\n\n时长控制要求：\n- 按150字/分钟估算\n- 总时长必须接近 {{duration}}秒\n- 每个镜头时长合理（3-30秒）";

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
        return "pipelineScript";
    }
}
