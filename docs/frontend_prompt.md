Persona (角色): 你是一名资深的 React (TypeScript) 前端架构师，精通状态管理（尤其是 Zustand）和高性能金融图表（lightweight-charts）。

Primary Goal (首要目标): 为 K 线训练游戏实现前端应用。应用的核心是一个严格的状态机，用于控制 K 线的“播放-暂停-决策-播放”循环。

Backend API (后端接口): 所有后端交互的唯一真实来源 (Single Source of Truth) 是位于 /docs/api-docs.json 的 OpenAPI (Swagger) 规范文档。 你的首要任务是：

（假设）“读取”并解析 /docs/api-docs.json。

基于此 JSON 自动生成所有 TypeScript 的 interface (用于数据模型) 和 api (用于 fetch 调用) 函数。

Technical Requirements (技术实现要求)

1. Technology Stack (技术栈)

Framework: React 18+ (使用 Hooks)

Language: TypeScript

State Management: Zustand

Charting Library: lightweight-charts

Styling: Styled-components (或 Tailwind CSS，请自选其一)

2. Core State Machine (核心状态机)

应用必须由一个在 Zustand store 中定义的 gameState 严格驱动。

TypeScript
type GameState = 
  | 'IDLE'                // 初始状态
  | 'LOADING_GAME'        // 正在调用 GET /api/game/new
  | 'WAITING_FOR_DECISION'  // K线已暂停, 等待用户点击 "做多/做空"
  | 'SUBMITTING_DECISION' // 正在调用 POST /api/game/decision
  | 'ANIMATING_KLINE'     // 正在平滑播放K线动画
  | 'SHOWING_RESULT'      // 动画停止, 显示 "WIN/LOSS" 模态框
  | 'ENDED';              // 游戏会话结束, 显示总结
3. Data Structures (TypeScript)

你将从 /docs/api-docs.json 自动生成。它们应该（大致）符合以下结构：

TypeScript
// (来自 API 文档) 
// 用于 lightweight-charts 的格式
interface KLineBar {
  time: number; // UNIX
  open: number;
  high: number;
  low: number;
  close: number;
}

// (来自 GET /api/game/new 的响应)
interface GameSession {
  sessionId: string;
  symbol: string;
  fullKLineData: KLineBar[]; // 完整K线 (包含所有答案)
  keyNodeIndices: number[];  // 暂停点, e.g., [5, 22, 45, 78]
}

// (来自 POST /api/game/decision 的响应)
interface DecisionResponse {
  result: 'WIN' | 'LOSS';
  pnlPercent: number;
  scoreChange: number;
  nextStopIndex: number; // 动画应停止的下一个K线索引
}
4. File & Component Structure (文件与组件结构)

请按以下结构组织你的代码输出：

src/services/api.ts

包含基于 /docs/api-docs.json 生成的 TypeScript 接口。

包含 fetch 封装函数，例如 startNewGameApi(): Promise<GameSession> 和 makeDecisionApi(payload): Promise<DecisionResponse>。

src/store/gameStore.ts

(关键) Zustand store 的实现。

State:

gameState: GameState

gameSession: GameSession | null

currentKLineIndex: number (当前K线显示到的索引)

currentKeyNodeOrder: number (当前是第几个决策点)

lastDecisionResult: DecisionResponse | null

sessionPnl: number

Actions (必须是异步的):

startNewGame(): Promise<void> (调用 API, 更新 state, 将 gameState 变为 WAITING_FOR_DECISION)

makeDecision(choice: 'LONG' | 'SHORT'): Promise<void> (设置 SUBMITTING_DECISION, 调用 API, 存储结果, 设置 ANIMATING_KLINE)

animationCompleted(): void (由图表组件在动画播完后调用, 设置 SHOWING_RESULT)

nextStep(): void (由结果模态框调用, 判断游戏是否 ENDED 或回到 WAITING_FOR_DECISION)

src/pages/GamePage.tsx

(容器) 布局页面并协调所有子组件。

从 gameStore 中获取所有状态。

条件渲染逻辑:

LOADING_GAME -> 显示 <Spinner />

WAITING_FOR_DECISION -> 激活 <DecisionPanel />

SUBMITTING_DECISION -> 禁用 <DecisionPanel /> (带loading)

ANIMATING_KLINE -> 禁用 <DecisionPanel /> (不带loading)

SHOWING_RESULT -> 显示 <ResultModal />

ENDED -> 显示 <GameSummary />

包含 <GameHUD />, <KLineChartComponent />, <DecisionPanel />。

src/components/KLineChartComponent.tsx

(最核心的组件)

Props: session: GameSession, startAnimIndex: number, endAnimIndex: number (当 gameState 变为 ANIMATING_KLINE 时, start/end 会更新)

内部逻辑 (使用 useRef 和 useEffect):

初始化 (useEffect - 依赖 session):

使用 lightweight-charts 创建图表。

获取 firstStopIndex = session.keyNodeIndices[0]。

使用 candleSeries.setData(session.fullKLineData.slice(0, firstStopIndex + 1)) 一次性渲染初始K线。

执行动画 (useEffect - 依赖 endAnimIndex):

当 endAnimIndex (来自 lastDecisionResult.nextStopIndex) 变化时触发。

必须使用 setInterval (例如 300ms) 来模拟K线生长。

在循环中, 必须使用 candleSeries.update() 动态添加 fullKLineData 中的 K 线 (从 startAnimIndex + 1 到 endAnimIndex)。

严禁在动画期间调用 setData() (会导致图表闪烁)。

动画播放完毕后, 必须调用 gameStore.getState().animationCompleted() 来更新全局状态机。

src/components/DecisionPanel.tsx

包含 "做多 (LONG)" 和 "做空 (SHORT)" 按钮。

onClick 调用 gameStore.getState().makeDecision('LONG')。

从 gameStore 获取 gameState 来控制 disabled 状态 (WAITING_FOR_DECISION 时才可点击)。

src/components/ResultModal.tsx

从 gameStore 获取 lastDecisionResult 来显示 "WIN/LOSS" 和 "PnL %"。

包含 "下一步 (Next)" 按钮, onClick 调用 gameStore.getState().nextStep()。

Task (执行任务): 请基于上述所有要求，以模块化的方式，提供完整的 React (TypeScript) 代码。请确保 KLineChartComponent 中的动画逻辑是平滑且高效的。