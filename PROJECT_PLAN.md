# HuX Creator V2

> AI 视频创作策划助手 — 多模型迭代流水线架构

---

# 1. 核心升级

## 从 V1 到 V2 的变化

| 维度 | V1 | V2 |
|------|----|----|
| 生成方式 | 一次性生成所有内容 | 多模型流水线迭代优化 |
| 数据存储 | 所有内容塞在一个 TEXT 字段 | 结构化存储（脚本/场景/镜头/素材独立表） |
| 视频类型 | 统一风格 | 营销类/知识科普类/故事类 三种模板 |
| 质量控制 | 无 | 模型评分 + 自动迭代优化 |
| 生成模式 | 单一模式 | 快速模式(3轮) / 专业模式(5+轮) |
| 用户控制 | 仅选择模型 | 叙事节奏/视觉风格/生成模式可调 |

---

# 2. 视频类型体系

## 三种视频类型

### 营销类 (MARKETING)

- 结构化脚本（分镜、时长、转场等元数据）
- 明确产品展示顺序、镜头切换逻辑
- 适合：产品推广、品牌宣传、带货视频

### 知识科普类 (KNOWLEDGE)

- 非结构化文本（纯文字描述）
- 重点描述核心知识点和讲解逻辑
- 适合：教程、科普、分享类视频

### 故事类 (STORY)

- 分镜脚本（标注人物动作、场景转换）
- 详细的视觉叙事设计
- 适合：短剧、故事、情感类视频

---

# 3. 生成参数

## 用户可调参数

| 参数 | 类型 | 范围 | 默认值 | 说明 |
|------|------|------|--------|------|
| 叙事节奏 | Float | 0.6 - 0.8 | 0.7 | 值越大节奏越紧凑 |
| 视觉风格 | Enum | 自动/简约/科技/文艺/商务 | 自动 | 影响画面描述和提示词风格 |
| 生成模式 | Enum | QUICK/PROFESSIONAL | QUICK | 快速3轮 vs 专业5+轮 |
| 视频类型 | Enum | MARKETING/KNOWLEDGE/STORY | 由AI判断 | 可手动覆盖 |

---

# 4. 多模型流水线架构

## 5个模型的职责

```
┌─────────────────────────────────────────────────────────────┐
│                    用户创建项目                                │
│  (标题/描述/平台/时长/类型/节奏/风格/模式)                      │
└──────────────────────────┬──────────────────────────────────┘
                           │
                           ▼
┌─────────────────────────────────────────────────────────────┐
│  模型1: 类型分类器 (TypeClassifier)                           │
│  输入: 项目描述                                               │
│  输出: 视频类型 (MARKETING/KNOWLEDGE/STORY)                   │
│  存储: t_project.video_type                                   │
└──────────────────────────┬──────────────────────────────────┘
                           │
                           ▼
┌─────────────────────────────────────────────────────────────┐
│  模型2: 脚本生成器 (ScriptGenerator)                          │
│  输入: 项目信息 + 视频类型模板 + 节奏参数                       │
│  输出: 完整脚本，拆分为场景和镜头                               │
│  存储: t_script + t_scene + t_shot                            │
└──────────────────────────┬──────────────────────────────────┘
                           │
                           ▼
┌─────────────────────────────────────────────────────────────┐
│  模型3: 连贯性检查器 (CoherenceChecker)                       │
│  输入: 脚本内容                                               │
│  输出: 优化后的脚本（检查逻辑衔接、视觉适配、时长控制）          │
│  存储: 更新 t_script + t_scene + t_shot                       │
└──────────────────────────┬──────────────────────────────────┘
                           │
                           ▼
┌─────────────────────────────────────────────────────────────┐
│  模型4: 素材标记器 (MaterialMarker)                           │
│  输入: 脚本 + 场景 + 镜头                                     │
│  输出: 每个场景的素材位置（画面/人物/道具）                     │
│  存储: t_material                                             │
└──────────────────────────┬──────────────────────────────────┘
                           │
                           ▼
┌─────────────────────────────────────────────────────────────┐
│  模型5: 评估器 (Evaluator)                                    │
│  输入: 所有生成内容                                           │
│  输出: 评分 + 优化建议                                        │
│  存储: t_evaluation                                           │
│                                                              │
│  逻辑:                                                       │
│  - 评分 >= 90 → 推送前端展示                                  │
│  - 评分 < 90 → 生成优化建议，进入下一轮迭代                    │
└──────────────────────────┬──────────────────────────────────┘
                           │
                           ▼
┌─────────────────────────────────────────────────────────────┐
│  迭代循环                                                    │
│  快速模式: 最多3轮                                           │
│  专业模式: 最多5+轮                                          │
│  每轮使用优化建议改进脚本，重新执行模型2-5                     │
└──────────────────────────┬──────────────────────────────────┘
                           │
                           ▼
┌─────────────────────────────────────────────────────────────┐
│  最终输出: 完整创作包                                         │
│  脚本 + 场景 + 镜头 + 素材 + 评估报告                         │
└─────────────────────────────────────────────────────────────┘
```

---

# 5. 数据库设计 (V2)

## 5.1 t_project (项目表 - 扩展)

```sql
ALTER TABLE t_project ADD COLUMN video_type VARCHAR(20);          -- MARKETING/KNOWLEDGE/STORY
ALTER TABLE t_project ADD COLUMN narrative_rhythm FLOAT DEFAULT 0.7;  -- 0.6-0.8
ALTER TABLE t_project ADD COLUMN visual_style VARCHAR(20) DEFAULT 'AUTO'; -- AUTO/SIMPLE/TECH/ARTISTIC/BUSINESS
ALTER TABLE t_project ADD COLUMN generation_mode VARCHAR(20) DEFAULT 'QUICK'; -- QUICK/PROFESSIONAL
ALTER TABLE t_project ADD COLUMN current_round INT DEFAULT 0;     -- 当前轮次
ALTER TABLE t_project ADD COLUMN max_rounds INT DEFAULT 3;        -- 最大轮次
ALTER TABLE t_project ADD COLUMN final_score FLOAT;               -- 最终评分
```

## 5.2 t_script (脚本表)

```sql
CREATE TABLE t_script (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    project_id BIGINT NOT NULL,
    round INT NOT NULL,                    -- 第几轮生成
    version INT DEFAULT 1,                 -- 版本号
    content TEXT,                          -- 完整脚本文本
    video_type VARCHAR(20),                -- 脚本类型
    narrative_rhythm FLOAT,                -- 叙事节奏
    word_count INT,                        -- 字数
    estimated_duration INT,                -- 预估时长(秒)
    status INT DEFAULT 0,                  -- 0=草稿 1=已优化 2=最终版
    score FLOAT,                           -- 评分
    remark VARCHAR(500),
    deleted INT DEFAULT 0,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);
```

## 5.3 t_scene (场景表)

```sql
CREATE TABLE t_scene (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    script_id BIGINT NOT NULL,
    scene_number INT NOT NULL,             -- 场景序号
    title VARCHAR(200),                    -- 场景标题
    description TEXT,                      -- 场景描述
    location VARCHAR(200),                 -- 场景地点
    mood VARCHAR(100),                     -- 氛围/情绪
    estimated_duration INT,                -- 预估时长(秒)
    visual_type VARCHAR(50),               -- ANIMATION/LIVE/GRAPHIC/MIXED
    transition VARCHAR(100),               -- 转场方式
    remark VARCHAR(500),
    deleted INT DEFAULT 0,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);
```

## 5.4 t_shot (镜头表)

```sql
CREATE TABLE t_shot (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    scene_id BIGINT NOT NULL,
    shot_number INT NOT NULL,              -- 镜头序号
    description TEXT,                      -- 镜头描述
    camera_angle VARCHAR(100),             -- 摄影角度
    camera_movement VARCHAR(100),          -- 运镜方式
    duration INT,                          -- 时长(秒)
    dialogue TEXT,                         -- 台词/旁白
    visual_type VARCHAR(50),               -- ANIMATION/LIVE/GRAPHIC
    transition VARCHAR(100),               -- 转场
    remark VARCHAR(500),
    deleted INT DEFAULT 0,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);
```

## 5.5 t_material (素材表)

```sql
CREATE TABLE t_material (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    scene_id BIGINT NOT NULL,
    shot_id BIGINT,                        -- 可选，关联到具体镜头
    material_type VARCHAR(50) NOT NULL,    -- IMAGE/PERSON/PROP/BACKGROUND/EFFECT/AUDIO
    name VARCHAR(200),                     -- 素材名称
    description TEXT,                      -- 素材描述
    position VARCHAR(200),                 -- 画面中的位置
    source_suggestion VARCHAR(500),        -- 来源建议（实拍/素材库/AI生成）
    prompt TEXT,                           -- AI生成提示词（如适用）
    remark VARCHAR(500),
    deleted INT DEFAULT 0,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);
```

## 5.6 t_generation_round (生成轮次表)

```sql
CREATE TABLE t_generation_round (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    project_id BIGINT NOT NULL,
    round_number INT NOT NULL,             -- 轮次编号
    mode VARCHAR(20),                      -- QUICK/PROFESSIONAL
    script_id BIGINT,                      -- 本轮生成的脚本ID
    model1_result TEXT,                    -- 类型分类结果
    model2_result TEXT,                    -- 脚本生成结果摘要
    model3_result TEXT,                    -- 连贯性检查结果摘要
    model4_result TEXT,                    -- 素材标记结果摘要
    model5_score FLOAT,                    -- 评估评分
    model5_suggestions TEXT,               -- 评估优化建议
    status INT DEFAULT 0,                  -- 0=进行中 1=完成 2=失败
    remark VARCHAR(500),
    deleted INT DEFAULT 0,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);
```

## 5.7 t_evaluation (评估表)

```sql
CREATE TABLE t_evaluation (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    project_id BIGINT NOT NULL,
    script_id BIGINT NOT NULL,
    round_number INT NOT NULL,
    overall_score FLOAT,                   -- 总分 0-100
    completeness_score FLOAT,              -- 完整性评分
    coherence_score FLOAT,                 -- 连贯性评分
    visual_fit_score FLOAT,                -- 视觉适配评分
    creativity_score FLOAT,                -- 创意评分
    suggestions TEXT,                      -- 优化建议 JSON
    passed BOOLEAN DEFAULT FALSE,          -- 是否通过(>=90)
    remark VARCHAR(500),
    deleted INT DEFAULT 0,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);
```

---

# 6. API 设计 (V2)

## 6.1 创建项目 (扩展)

```http
POST /api/project/create
```

```json
{
  "title": "2026年了，还有人把AI只当聊天机器人？",
  "platform": "BILIBILI",
  "targetUser": "学生",
  "duration": 180,
  "description": "介绍AI工具如何提升学习效率...",
  "narrativeRhythm": 0.7,
  "visualStyle": "AUTO",
  "generationMode": "QUICK"
}
```

## 6.2 启动生成 (新)

```http
POST /api/generation/start
```

```json
{
  "projectId": 1,
  "modelProvider": "DEEPSEEK"
}
```

## 6.3 查询生成进度 (新)

```http
GET /api/generation/progress/{projectId}
```

Response:
```json
{
  "code": 200,
  "data": {
    "projectId": 1,
    "currentRound": 1,
    "maxRounds": 3,
    "status": "GENERATING",
    "currentModel": 3,
    "currentModelName": "连贯性检查",
    "score": null,
    "rounds": [...]
  }
}
```

## 6.4 获取脚本详情 (新)

```http
GET /api/script/{projectId}
```

## 6.5 获取场景列表 (新)

```http
GET /api/script/{scriptId}/scenes
```

## 6.6 获取镜头列表 (新)

```http
GET /api/scene/{sceneId}/shots
```

## 6.7 获取素材列表 (新)

```http
GET /api/scene/{sceneId}/materials
```

## 6.8 优化单个场景 (新)

```http
POST /api/scene/{sceneId}/optimize
```

```json
{
  "instruction": "让这个场景更适合学生群体",
  "modelProvider": "DEEPSEEK"
}
```

## 6.9 优化单个镜头 (新)

```http
POST /api/shot/{shotId}/optimize
```

## 6.10 获取评估报告 (新)

```http
GET /api/evaluation/{projectId}
```

---

# 7. 前端页面设计

## 7.1 项目创建页 (改造)

新增字段：
- 视频类型选择（可选或AI自动判断）
- 叙事节奏滑块 (0.6-0.8)
- 视觉风格下拉
- 生成模式选择（快速/专业）

## 7.2 生成进度页 (新)

- 显示当前轮次和模型进度
- 实时更新每个模型的执行状态
- 显示评分和优化建议

## 7.3 脚本详情页 (新)

- 分场景展示脚本
- 每个场景可展开查看镜头
- 每个镜头可查看素材
- 支持单个场景/镜头优化
- 显示评估报告

## 7.4 创作包总览页 (新)

- 项目概览
- 脚本摘要
- 场景列表
- 素材清单
- 评估报告
- 导出功能

---

# 8. 开发计划

## Phase 1: 数据库重构

- 创建新表 (t_script, t_scene, t_shot, t_material, t_generation_round, t_evaluation)
- 修改 t_project 添加新字段
- 创建对应 Entity/Repository

## Phase 2: 多模型流水线

- 创建5个新模型生成器
- 实现 GenerationService（流水线编排）
- 实现快速/专业模式逻辑

## Phase 3: API 层

- 新增 GenerationController
- 新增 ScriptController
- 新增 EvaluationController

## Phase 4: 前端改造

- 改造项目创建页
- 新增生成进度页
- 新增脚本详情页
- 新增创作包总览页

## Phase 5: 导出和优化

- 结构化导出（Markdown/JSON/Word）
- 单场景/镜头优化
- 历史版本对比

---

# 9. 技术要点

## 时长控制

- 按 150字/分钟 估算视频长度
- 自动拆分过长段落
- 每个镜头标注预估时长

## 视觉适配度标记

- ANIMATION: 适合用动画展示的内容
- LIVE: 适合实拍的内容
- GRAPHIC: 适合图表/数据展示的内容
- MIXED: 混合类型

## 逻辑连贯性检查

- 使用 NLP 检测句子间衔接度
- 检查场景间的逻辑过渡
- 验证时间线的合理性

## 迭代优化机制

- 每轮生成后由评估器打分
- 评分 >= 90: 标记为最终版本，推送前端
- 评分 < 90: 生成优化建议，进入下一轮
- 达到最大轮次后使用当前最佳版本
