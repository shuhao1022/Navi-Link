# Navi-Link 项目说明文档

## 项目概述

**Navi-Link**（内部代号 ShadowMap）是一款 Android 悬浮窗导航应用。它通过监听高德地图（Amap/AutoNavi）的标准广播，将导航信息以悬浮窗形式实时叠加显示在其他应用之上，让用户在使用其他应用时也能看到导航指引。

| 项目属性 | 值 |
|---------|---|
| 包名 | `com.navi.link` |
| 最低 SDK | Android 5.0 (API 21) |
| 目标 SDK | Android 14 (API 34) |
| 编译 SDK | 34 |
| 版本 | 1.8 (versionCode 18) |
| 开发语言 | Java |
| 构建工具 | Gradle + AGP 8.5.0 |

---

## 项目结构

```
Navi-Link/
├── app/
│   ├── src/main/
│   │   ├── java/com/navi/link/
│   │   │   ├── RouterActivity.java          # 透明路由入口（应用启动分发器）
│   │   │   ├── MainActivity.java            # 主界面（配置页面）
│   │   │   ├── AutoMapService.java          # 前台服务（维持悬浮窗生命周期）
│   │   │   ├── AmapNaviReceiver.java        # 广播接收器（解析高德导航数据）
│   │   │   ├── FloatingWindowManager.java   # 悬浮窗管理器（核心逻辑，1689行）
│   │   │   ├── LaneLineView.java            # 车道线自定义组件
│   │   │   ├── TmcProgressBar.java          # TMC路况进度条自定义组件
│   │   │   └── CrashHandler.java            # 全局异常捕获处理器
│   │   ├── res/
│   │   │   ├── layout/
│   │   │   │   ├── activity_main.xml                    # 主界面配置页
│   │   │   │   ├── layout_floating_cruise.xml           # 巡航模式悬浮窗（灵动岛式）
│   │   │   │   ├── layout_floating_cruise_normal.xml    # 巡航模式悬浮窗（常规式）
│   │   │   │   ├── layout_floating_navi.xml             # 常规导航悬浮窗
│   │   │   │   ├── layout_floating_navi_full.xml        # 全数据导航悬浮窗
│   │   │   │   ├── layout_floating_navi_minimal.xml     # 灵动岛导航悬浮窗
│   │   │   │   ├── layout_floating_traffic_light_group.xml  # 红绿灯胶囊组件
│   │   │   │   └── item_cruise_traffic_light.xml        # 巡航红绿灯单项
│   │   │   ├── drawable/
│   │   │   │   ├── ic_notification.xml                 # 通知栏图标
│   │   │   │   ├── ic_speed.xml                        # 速度图标
│   │   │   │   ├── bg_traffic_light_capsule.xml        # 红绿灯胶囊背景
│   │   │   │   ├── bg_main_navigation_box.xml          # 导航/巡航主背景
│   │   │   │   ├── bg_floating_dark.xml                # 深色背景
│   │   │   │   ├── bg_floating_dark_info.xml           # 信息栏背景
│   │   │   │   ├── bg_lane_line.xml                    # 车道线背景
│   │   │   │   ├── camera_shape.xml                    # 摄像头形状
│   │   │   │   └── lane_pdf_*.png / lane_special_unknown.png  # 车道线图标集合(49个)
│   │   │   └── mipmap-*/              # 图标资源（转向箭头、红绿灯、方向指示）
│   │   └── AndroidManifest.xml
│   └── build.gradle
├── gradle/
│   └── libs.versions.toml              # 版本目录（统一依赖管理）
├── build.gradle                        # 顶级构建脚本
├── settings.gradle
└── navi.jks                            # 签名密钥文件
```

---

## 核心功能

### 1. 透明路由入口

应用启动时由 [RouterActivity](file:///d:/AndroidStudioProjects/Navi-Link/app/src/main/java/com/navi/link/RouterActivity.java) 作为透明分发器，无 UI 界面，根据用户配置决定行为：

| 启动方式 | 行为 |
|---------|------|
| **只启动服务** | 有悬浮窗权限时，若服务未运行则静默启动服务 + Toast 提示；若已运行则仅提示。无权限时跳转配置页 |
| **正常打开** | 直接进入 MainActivity 配置页 |

### 2. 双模式悬浮窗

| 模式 | 说明 | 触发条件 |
|------|------|---------|
| **巡航模式** (MODE_CRUISE) | 显示当前速度和道路名称，可附带多个红绿灯倒计时 | 无转向图标时默认进入 |
| **导航模式** (MODE_NAVI) | 显示转弯指示、剩余距离、道路名、进度条、ETA、红绿灯胶囊 | 接收到转向图标(ICON≠0)时自动切换 |

巡航模式支持**两种布局样式**（在配置页选择）：
| 巡航样式 | 说明 |
|---------|------|
| **灵动岛式** (layout_floating_cruise.xml) | 紧凑圆角胶囊，速度 + 道路名 + 红绿灯 |
| **常规式** (layout_floating_cruise_normal.xml) | 矩形卡片，速度 + 限速 + 道路名 + 车道线 + 红绿灯 |

### 3. 三样式导航悬浮窗

| 样式 | 说明 |
|------|------|
| **常规样式** | 大转向图标 + 距离数字 + 动作文字 + 道路名 + TMC路况进度条 + 底部摘要(剩余距离·时间) + ETA + 红绿灯胶囊 + 出口信息 |
| **灵动岛样式**（精简） | 紧凑布局：当前速度 + 转向图标 + 剩余距离 + 道路名 + 红绿灯胶囊 |
| **全数据样式** | 完整驾驶信息：转向图标 + 距离 + 道路名 + 进度条 + ETA + 当前速度 + 限速 + 电子眼距离/限速 + 终点名称 + 红绿灯总数/剩余 + 车道线 + TMC路况进度条 + 出口信息 + 红绿灯胶囊 |

### 4. 红绿灯实时显示

- **导航模式红绿灯**：显示单一路口红绿灯状态（红/黄/绿）+ 方向箭头 + 倒计时秒数，5秒无更新自动隐藏
- **巡航模式红绿灯**：支持同时显示多个方向的红绿灯倒计时（JSONArray 批量数据），所有灯倒计时归零后自动隐藏容器

### 5. TMC 路况进度条

通过 [TmcProgressBar](file:///d:/AndroidStudioProjects/Navi-Link/app/src/main/java/com/navi/link/TmcProgressBar.java) 自定义 View 分段展示路况状态：
- 7种状态颜色映射（已驶过灰/畅通绿/缓行黄/拥堵红/严重拥堵深红/蓝色/青蓝）
- 当前位置三角标记图标指示
- 缩放时物理缩放圆角保持视觉一致

### 6. 车道线显示

通过 [LaneLineView](file:///d:/AndroidStudioProjects/Navi-Link/app/src/main/java/com/navi/link/LaneLineView.java) 自定义组件，解析高德 `KEY_TYPE=13012` 广播中的 `EXTRA_DRIVE_WAY` 数据，动态绘制车道线图标：
- 3条及以下：wrap_content 紧凑模式，固定图标尺寸
- 4条及以上：match_parent 均分模式，weight 等比分配宽度
- 图标来自 `drawable/lane_pdf_*.png` 资源集合

### 7. 背景模式与昼夜自适应

| 背景模式 | 说明 |
|---------|------|
| **深色模式**（默认） | 黑色半透明背景，深色底白字 |
| **半透明模式** | 半透明毛玻璃效果 |
| **全透明模式** | 完全透明背景，仅显示文字和图标 |

- 支持跟随高德地图昼夜模式自动切换（通过 `KEY_TYPE=10019` 广播）
- 应用启动时主动向高德查询当前昼夜状态（`KEY_TYPE=13030`）
- 昼夜切换时自动调整文字颜色（深色→白色，浅色→深色）

### 8. 巡航开关控制

- 配置页提供巡航窗独立开关，可控制巡航模式是否显示
- 仅影响巡航模式，导航模式不受影响
- 关闭巡航后即使无转向图标也不显示巡航窗

### 9. 出口信息显示

解析高德广播 `EXIT_NAME_INFO` 和 `EXIT_DIRECTION_INFO` 字段，在导航时显示出口编号和方向指示，便于快速识别高速/快速路出口。

### 10. 个性化配置

- **启动方式**：只启动服务 / 正常打开配置页
- **主题色**：8种预设颜色可选（黑/蓝/浅蓝/橙/粉红/紫/深橙/青绿），自动计算文字对比度，启动方式卡片和样式卡片同步跟随
- **缩放**：0.5x ~ 2.0x 无极滑块调节，采用**物理内容缩放**方案；**三种导航样式独立记忆缩放值**，切换时自动恢复
- **悬浮窗点击**：点击悬浮窗快捷打开设置页
- **拖拽定位**：悬浮窗可自由拖拽，长按 500ms 锁定/解锁位置
- **配置持久化**：所有设置通过 `SharedPreferences` 保存

### 11. 超时与看门狗机制

| 机制 | 超时时间 | 效果 |
|------|---------|------|
| 导航超时 | 6秒 | 6秒内无导航数据则自动切回巡航模式 |
| 巡航宽容 | 3秒 | 导航模式下短暂无转向图标时，给3秒宽容期 |
| 看门狗 | 5秒 | 5秒内无任何数据则隐藏悬浮窗 |

### 12. 缩放刷新数据缓存

缩放调节触发 `recreateWindow()` 重建窗口时，自动读取上次缓存的最新导航数据填充到新布局中，避免窗口重建瞬间显示默认内容的闪烁。

### 13. 全局异常捕获

[CrashHandler](file:///d:/AndroidStudioProjects/Navi-Link/app/src/main/java/com/navi/link/CrashHandler.java) 捕获所有未处理异常，保存设备信息、应用版本、异常堆栈到本地日志文件，自动清理旧日志保留最近10个。

---

## 架构设计

```
┌──────────────────────────────────────────────────────────┐
│                     RouterActivity                       │
│               (透明分发器：权限/模式判断)                   │
│            ┌──────────┴──────────┐                        │
│            ▼                     ▼                        │
│     启动服务模式            正常打开模式                    │
│      │                     │                             │
│      ▼                     ▼                             │
│  AutoMapService        MainActivity                      │
│  (前台Service)         (配置界面)                          │
│      │                     │ 点击悬浮窗 →                 │
│      ▼                     │                             │
│  FloatingWindowManager ◄────┘                             │
│  (悬浮窗单例管理器)                                        │
│    │  │  │  │  │  │                                     │
│    │  │  │  │  │  └── 昼夜/背景模式切换                    │
│    │  │  │  │  └── 巡航开关控制                            │
│    │  │  │  └── 出口信息显示                              │
│    │  │  └── 物理缩放（文字/间距/圆角）                      │
│    │  └── 数据缓存（重建无闪烁）                             │
│    └── 模式切换/窗口重建                                   │
│    ┌──────┬──────┬──────┬──────┐                         │
│    ▼      ▼      ▼      ▼      ▼                         │
│ 巡航窗  巡航窗  常规   灵动岛  全数据                        │
│ (灵动岛)(常规) 导航窗  导航窗  导航窗                        │
│    │      │      │      │      │                         │
│    └──────┴──┬───┴──┬───┴──────┘                         │
│              ▼      ▼                                    │
│    LaneLineView   TmcProgressBar                         │
│    (车道线)        (路况进度条)                            │
│                                                          │
│  AmapNaviReceiver                                        │
│  (监听高德广播 AUTONAVI_STANDARD_BROADCAST_SEND)          │
│    │                                                     │
│    ├── KEY_TYPE=10001 (导航/巡航)                         │
│    ├── KEY_TYPE=60073 (红绿灯)                            │
│    ├── KEY_TYPE=10019 (昼夜模式)                          │
│    ├── KEY_TYPE=13011 (TMC路况)                           │
│    └── KEY_TYPE=13012 (车道线)                            │
└──────────────────────────────────────────────────────────┘
```

### 类职责说明

| 类 | 行数 | 职责 |
|----|------|------|
| [RouterActivity](file:///d:/AndroidStudioProjects/Navi-Link/app/src/main/java/com/navi/link/RouterActivity.java) | 73 | 透明路由入口，根据启动方式配置分发到服务或配置页 |
| [MainActivity](file:///d:/AndroidStudioProjects/Navi-Link/app/src/main/java/com/navi/link/MainActivity.java) | 466 | 主界面，提供启动方式、悬浮窗样式、背景模式、缩放、主题色、巡航开关等全部配置 |
| [AutoMapService](file:///d:/AndroidStudioProjects/Navi-Link/app/src/main/java/com/navi/link/AutoMapService.java) | 118 | 前台服务，创建通知栏常驻通知，初始化和销毁悬浮窗及广播接收器，主动查询昼夜模式 |
| [AmapNaviReceiver](file:///d:/AndroidStudioProjects/Navi-Link/app/src/main/java/com/navi/link/AmapNaviReceiver.java) | 188 | 监听 `AUTONAVI_STANDARD_BROADCAST_SEND` 广播，解析5类KEY_TYPE数据 |
| [FloatingWindowManager](file:///d:/AndroidStudioProjects/Navi-Link/app/src/main/java/com/navi/link/FloatingWindowManager.java) | 1689 | 单例悬浮窗管理器，负责5种悬浮窗布局的创建/销毁/重建、模式切换、物理缩放、主题色、背景模式、昼夜切换、触摸拖拽、数据缓存恢复等全部悬浮窗逻辑 |
| [LaneLineView](file:///d:/AndroidStudioProjects/Navi-Link/app/src/main/java/com/navi/link/LaneLineView.java) | 219 | 车道线自定义组件，解析 `EXTRA_DRIVE_WAY` 数据动态渲染车道图标 |
| [TmcProgressBar](file:///d:/AndroidStudioProjects/Navi-Link/app/src/main/java/com/navi/link/TmcProgressBar.java) | 179 | TMC路况进度条自定义组件，分段彩色绘制 + 当前位置标记 |
| [CrashHandler](file:///d:/AndroidStudioProjects/Navi-Link/app/src/main/java/com/navi/link/CrashHandler.java) | 186 | 全局异常捕获，保存崩溃日志到本地，自动清理旧日志 |

---

## 技术要点

### 高德广播数据协议

应用监听高德地图发出的标准广播 `AUTONAVI_STANDARD_BROADCAST_SEND`，解析5类关键数据：

| KEY_TYPE | 含义 | 关键字段 |
|----------|------|---------|
| `10001` | 导航/巡航信息 | `ICON`/`NEW_ICON`(转向图标), `CUR_SPEED`(当前速度), `SEG_REMAIN_DIS_AUTO`(段剩余距离), `ROUTE_REMAIN_DIS`/`_AUTO`(全程剩余距离), `ROUTE_REMAIN_TIME_AUTO`(剩余时间), `ETA_TEXT`(预计到达), `NEXT_ROAD_NAME`/`CUR_ROAD_NAME`(道路名), `ROUTE_ALL_DIS`(全程总距离), `LIMITED_SPEED`(限速), `CAMERA_DIST`/`CAMERA_SPEED`(电子眼), `endPOIName`(终点名称), `TRAFFIC_LIGHT_NUM`(红绿灯总数), `EXIT_NAME_INFO`/`EXIT_DIRECTION_INFO`(出口信息), `CAR_DIRECTION`(车头方向) |
| `60073` | 红绿灯数据 | `trafficLightStatus`(灯状态), `dir`(方向), `redLightCountDownSeconds`(倒计时), `lightsData`(巡航模式JSON数组) |
| `10019` | 昼夜模式 | `EXTRA_STATE`(37=白天, 38=夜晚) |
| `13011` | TMC路况 | `EXTRA_TMC_SEGMENT`(JSON路况分段数据) |
| `13012` | 车道线 | `EXTRA_DRIVE_WAY`(车道线JSON数据) |

### 转向图标映射

| ICON值 | 含义 | 图标资源 |
|--------|------|---------|
| 2 | 左转 | `ic_navi_left` |
| 3 | 右转 | `ic_navi_right` |
| 4 | 左前方 | `ic_navi_left_d` |
| 5 | 右前方 | `ic_navi_right_d` |
| 8 | 掉头 | `ic_navi_u_turn` |
| 9 | 直行 | `ic_navi_straight` |
| 10 | 途经点 | `ic_navi_mid` |
| 11 | 进入匝道 | `ic_navi_in_dao` |
| 12 | 驶出匝道 | `ic_navi_en_dao` |
| 15 | 终点 | `ic_navi_end` |

### 权限配置

```xml
<uses-permission android:name="android.permission.SYSTEM_ALERT_WINDOW" />
<uses-permission android:name="android.permission.FOREGROUND_SERVICE" />
<uses-permission android:name="android.permission.FOREGROUND_SERVICE_SPECIAL_USE" />
<uses-permission android:name="android.permission.POST_NOTIFICATIONS" />
```

- **悬浮窗权限** (`SYSTEM_ALERT_WINDOW`)：必须手动授权，应用启动时会引导用户开启
- **前台服务权限**：Android 14+ 需要 `FOREGROUND_SERVICE_SPECIAL_USE`
- **通知权限** (`POST_NOTIFICATIONS`)：Android 13+ 前台服务必须显示通知

### 依赖库

| 库 | 版本 | 用途 |
|----|------|------|
| AndroidX AppCompat | 1.6.1 | 向后兼容支持 |
| Material Components | 1.10.0 | MaterialCardView 等 Material Design 组件 |
| AndroidX Activity | 1.8.0 | EdgeToEdge 等新特性 |
| ConstraintLayout | 2.1.4 | 布局约束 |

### 物理内容缩放方案

传统 `setScaleX/Y` 方案在 scale > 1.0 时会被硬件加速的 RenderNode 裁剪边界裁剪。本项目采用**物理内容缩放**替代方案：

- `scaleViewRecursive()` 递归遍历 View 树，将文字尺寸(`textSize`)、内边距(`padding`)、外边距(`margin`)、固定宽高按缩放因子等比调整
- 同时缩放 `GradientDrawable` 背景的圆角半径（`cornerRadius` / `cornerRadii`），保持视觉比例一致
- 窗口始终 `WRAP_CONTENT`，由缩放后的内容自然撑开，无需手动计算窗口尺寸
- 缩放调节时通过 `recreateWindow()` 重建窗口，配合数据缓存恢复机制实现无闪烁切换
- 三种导航样式和巡航模式**独立记忆缩放值**，切换时自动恢复

---

## 构建与运行

### 构建 APK

```bash
# Debug 版本
./gradlew assembleDebug

# Release 版本
./gradlew assembleRelease
```

APK 输出命名格式：`Navi-Link-v{versionName}-{buildType}-{yyyyMMddHHmm}.apk`

例如：`Navi-Link-v1.8-release-202606110830.apk`

### 运行要求

1. Android 5.0 (API 21) 及以上系统
2. 必须授予「悬浮窗」权限
3. 必须安装高德地图车机版或手机版（用于发出导航广播）
4. Android 13+ 需授予「通知」权限

---

## 数据流

```
高德地图App
  │
  │  AUTONAVI_STANDARD_BROADCAST_SEND 广播
  ▼
AmapNaviReceiver.onReceive()
  │
  ├─── KEY_TYPE=60073 ──→ FloatingWindowManager.updateTrafficLight()
  │                       或 updateCruiseTrafficLights()
  │                       └── 同时写入数据缓存
  │
  ├─── KEY_TYPE=13011 ──→ FloatingWindowManager.updateTmcData()
  │                       └── TmcProgressBar.updateTmcData()
  │
  ├─── KEY_TYPE=13012 ──→ FloatingWindowManager.updateLaneLines()
  │                       └── LaneLineView.updateLanes()
  │
  ├─── KEY_TYPE=10019 ──→ FloatingWindowManager.onDayNightChanged()
  │                       └── 切换文字颜色主题
  │
  └─── KEY_TYPE=10001 ──→ 判断 ICON 字段
                            │
                            ├─ ICON≠0 → switchToNaviMode() → updateNaviInfo()
                            │              ├── updateExitInfo()
                            │              └── 写入数据缓存
                            └─ ICON=0 → switchToCruiseMode() + updateCruiseInfo()
                                           └── 写入数据缓存

用户调节缩放 → recreateWindow()
  ├── 删除旧窗口
  ├── 创建新布局 + 物理缩放
  ├── bindViews()
  ├── restoreCachedData()  ← 立即恢复最后一次数据，避免闪烁
  └── windowManager.addView() → 显示时已有真实数据
```
