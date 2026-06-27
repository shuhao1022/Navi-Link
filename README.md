# Navi-Link 项目说明文档

## 项目概述

**Navi-Link**（内部代号 ShadowMap）是一款 Android 悬浮窗导航应用。它通过监听高德地图（Amap/AutoNavi）的标准广播，将导航信息以悬浮窗形式实时叠加显示在其他应用之上，让用户在使用其他应用时也能看到导航指引。

| 项目属性 | 值 |
|---------|---|
| 包名 | `com.navi.link` |
| 最低 SDK | Android 4.2 (API 17) |
| 目标 SDK | Android 14 (API 34) |
| 编译 SDK | 34 |
| 版本 | 2.5.7 (versionCode 257) |
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
│   │   │   ├── FloatingWindowManager.java   # 悬浮窗管理器（窗口调度 + 生命周期）
│   │   │   ├── FloatingWindowFactory.java   # 窗口工厂（按模式+样式创建具体窗口）
│   │   │   ├── BaseFloatingWindow.java      # 窗口抽象基类（公共方法 + 接口定义）
│   │   │   ├── MinimalCruiseWindow.java     # 灵动岛巡航窗口实现
│   │   │   ├── NormalCruiseWindow.java      # 常规巡航窗口实现
│   │   │   ├── NormalNaviWindow.java        # 常规导航窗口实现
│   │   │   ├── MinimalNaviWindow.java       # 灵动岛导航窗口实现
│   │   │   ├── FullNaviWindow.java          # 全数据导航窗口实现
│   │   │   ├── LaneLineView.java            # 车道线自定义组件
│   │   │   ├── TmcProgressBar.java          # TMC路况进度条自定义组件
│   │   │   └── CrashHandler.java            # 全局异常捕获处理器
│   │   ├── res/
│   │   │   ├── layout/
│   │   │   │   ├── activity_main.xml                    # 主界面配置页
│   │   │   │   ├── layout_floating_cruise_minimal.xml           # 巡航模式悬浮窗（灵动岛式）
│   │   │   │   ├── layout_floating_cruise_normal.xml    # 巡航模式悬浮窗（常规式）
│   │   │   │   ├── layout_floating_navi_normal.xml             # 常规导航悬浮窗
│   │   │   │   ├── layout_floating_navi_full.xml        # 全数据导航悬浮窗
│   │   │   │   ├── layout_floating_navi_minimal.xml     # 灵动岛导航悬浮窗
│   │   │   │   ├── layout_floating_traffic_light_group.xml  # 红绿灯胶囊组件
│   │   │   │   ├── item_cruise_traffic_light.xml        # 巡航红绿灯单项
│   │   │   │   ├── item_cruise_traffic_light_small.xml  # 巡航红绿灯小尺寸项
│   │   │   │   └── item_app_list.xml                    # 应用列表项
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

### 2. 双模式五种窗口（工厂模式）

应用通过 `FloatingWindowFactory` 工厂模式创建具体窗口实现，`FloatingWindowManager` 仅负责调度和生命周期管理：

| 模式 | 窗口类型 | 说明 |
|------|---------|------|
| **巡航模式** (MODE_CRUISE) | MinimalCruiseWindow | 灵动岛巡航：紧凑胶囊，速度 + 道路名 + 红绿灯 |
| **巡航模式** (MODE_CRUISE) | NormalCruiseWindow | 常规巡航：矩形卡片，速度 + 限速 + 电子眼距离 + 道路名 + 车道线 + 红绿灯 |
| **导航模式** (MODE_NAVI) | NormalNaviWindow | 常规导航：转向图标 + 距离 + 道路名 + TMC进度条 + ETA + 出口信息 + 红绿灯 |
| **导航模式** (MODE_NAVI) | MinimalNaviWindow | 灵动岛导航：紧凑布局，速度 + 转向图标 + 距离 + 道路名 + 红绿灯 |
| **导航模式** (MODE_NAVI) | FullNaviWindow | 全数据导航：完整驾驶信息 + 车道线 + TMC进度条 + 出口信息 + 红绿灯 |

- **巡航模式**：无转向图标时默认进入，可在配置页选择灵动岛或常规样式
- **导航模式**：接收到转向图标(ICON≠0)时自动切换，支持三种样式选择

### 3. 红绿灯实时显示

- **导航模式红绿灯**：显示单一路口红绿灯状态（红/黄/绿）+ 方向箭头 + 倒计时秒数，5秒无更新自动隐藏
- **巡航模式红绿灯**：支持同时显示多个方向的红绿灯倒计时（JSONArray 批量数据），所有灯倒计时归零后自动隐藏容器

### 4. TMC 路况进度条

通过 [TmcProgressBar](file:///d:/AndroidStudioProjects/Navi-Link/app/src/main/java/com/navi/link/TmcProgressBar.java) 自定义 View 分段展示路况状态：
- 7种状态颜色映射（已驶过灰/畅通绿/缓行黄/拥堵红/严重拥堵深红/蓝色/青蓝）
- 当前位置三角标记图标指示
- 缩放时物理缩放圆角保持视觉一致

### 5. 车道线显示

通过 [LaneLineView](file:///d:/AndroidStudioProjects/Navi-Link/app/src/main/java/com/navi/link/LaneLineView.java) 自定义组件，解析高德 `KEY_TYPE=13012` 广播中的 `EXTRA_DRIVE_WAY` 数据，动态绘制车道线图标：
- 3条及以下：wrap_content 紧凑模式，固定图标尺寸
- 4条及以上：match_parent 均分模式，weight 等比分配宽度
- 图标来自 `drawable/lane_pdf_*.png` 资源集合

### 6. 背景模式与昼夜自适应

| 背景模式 | 说明 |
|---------|------|
| **深色模式**（默认） | 黑色半透明背景，深色底白字 |
| **半透明模式** | 半透明毛玻璃效果 |
| **全透明模式** | 完全透明背景，仅显示文字和图标 |

- 支持跟随高德地图昼夜模式自动切换（通过 `KEY_TYPE=10019` 广播）
- 应用启动时主动向高德查询当前昼夜状态（`KEY_TYPE=13030`）
- 昼夜切换时自动调整文字颜色（深色→白色，浅色→深色）

### 7. 巡航开关控制

- 配置页提供巡航窗独立开关，可控制巡航模式是否显示
- 仅影响巡航模式，导航模式不受影响
- 关闭巡航后即使无转向图标也不显示巡航窗

### 8. 出口信息显示

解析高德广播 `EXIT_NAME_INFO` 和 `EXIT_DIRECTION_INFO` 字段，在导航时显示出口编号和方向指示，便于快速识别高速/快速路出口。

### 9. 个性化配置

- **启动方式**：只启动服务 / 正常打开配置页
- **主题色**：8种预设颜色可选（黑/蓝/浅蓝/橙/粉红/紫/深橙/青绿），自动计算文字对比度，启动方式卡片和样式卡片同步跟随
- **缩放**：0.5x ~ 2.0x 无极滑块调节，采用**物理内容缩放**方案；**三种导航样式独立记忆缩放值**，切换时自动恢复
- **悬浮窗点击**：点击悬浮窗快捷打开设置页
- **拖拽定位**：悬浮窗可自由拖拽，长按 500ms 锁定/解锁位置
- **配置持久化**：所有设置通过 `SharedPreferences` 保存

### 10. 超时与看门狗机制

| 机制 | 超时时间 | 效果 |
|------|---------|------|
| 导航超时 | 6秒 | 6秒内无导航数据则自动切回巡航模式 |
| 巡航宽容 | 3秒 | 导航模式下短暂无转向图标时，给3秒宽容期 |
| 看门狗 | 5秒 | 5秒内无任何数据则隐藏悬浮窗 |

### 11. 缩放刷新数据缓存

缩放调节触发 `recreateWindow()` 重建窗口时，自动读取上次缓存的最新导航数据填充到新布局中，避免窗口重建瞬间显示默认内容的闪烁。

### 12. 全局异常捕获

[CrashHandler](file:///d:/AndroidStudioProjects/Navi-Link/app/src/main/java/com/navi/link/CrashHandler.java) 捕获所有未处理异常，保存设备信息、应用版本、异常堆栈到本地日志文件，自动清理旧日志保留最近10个。

### 13. 关于我们与系统诊断页面

在配置页新增“关于我们”独立页面，并支持全局主题色跟随与自适应：
- **关于软件**：展示软件版本、作者（Shadow），并提供 **QQ交流群（1106923186）** 与 **Git开源地址**，支持一键点击复制至剪贴板，同时向用户声明本软件完全免费与相关免责条款。
- **设备硬件诊断**：
  - **处理器 (CPU)**：读取并解析 `/proc/cpuinfo`，展示 CPU 硬件型号、核心数及架构类型（如 `AArch64 (8核 / aarch64)`）。
  - **运行内存 (RAM)**：调用 `ActivityManager.MemoryInfo` 动态计算并展示实时可用内存与物理内存总量。
  - **存储空间 (ROM)**：使用 `StatFs` 获取设备数据存储的剩余与总容量。
  - **系统版本 (API)**：动态展示 Android 系统版本及对应的 API 等级。
  - **显示信息**：展示屏幕物理分辨率、应用可用分辨率、屏幕像素密度（dpi及缩放因子）以及屏幕刷新率。
- **主题色自适应**：左侧“关于我们”菜单的选中指示条、关于卡片的主标题以及其他设置面板中各个卡片的主标题（如常规布局设置、灵动布局设置、仪表盘副屏设置等）均会自动跟随用户选择的全局主题色。

---

## 架构设计（工厂模式）

```
┌──────────────────────────────────────────────────────────────┐
│                       RouterActivity                         │
│                 (透明分发器：权限/模式判断)                     │
│              ┌──────────┴──────────┐                          │
│              ▼                     ▼                          │
│       启动服务模式            正常打开模式                      │
│        │                     │                               │
│        ▼                     ▼                               │
│    AutoMapService        MainActivity                        │
│    (前台Service)         (配置界面)                            │
│        │                     │ 点击悬浮窗 →                   │
│        ▼                     │                               │
│    FloatingWindowManager ◄────┘                               │
│    (单例管理器：调度/生命周期/缓存/拖拽)                        │
│      │                                                       │
│      ├── FloatingWindowFactory.createWindow(mode, style)     │
│      │         │                                             │
│      │         ├── MinimalCruiseWindow  (灵动岛巡航)           │
│      │         ├── NormalCruiseWindow   (常规巡航)             │
│      │         ├── NormalNaviWindow     (常规导航)             │
│      │         ├── MinimalNaviWindow    (灵动岛导航)           │
│      │         └── FullNaviWindow       (全数据导航)           │
│      │                                                       │
│      └── activeWindow: BaseFloatingWindow                    │
│            │                                                 │
│            ├── updateNaviInfo()   / updateCruiseInfo()       │
│            ├── updateTrafficLight() / updateCruiseTrafficLights() │
│            ├── updateLaneLines()  / updateExitInfo()         │
│            ├── updateTmcData()    / applyThemeColor()        │
│            └── applyDayNightTextColors()                     │
│                                                              │
│    LaneLineView / TmcProgressBar  (窗口内嵌自定义组件)         │
│                                                              │
│    AmapNaviReceiver                                          │
│    (监听高德广播 AUTONAVI_STANDARD_BROADCAST_SEND)            │
│      │                                                       │
│      ├── KEY_TYPE=10001 (导航/巡航)                           │
│      ├── KEY_TYPE=60073 (红绿灯)                              │
│      ├── KEY_TYPE=10019 (昼夜模式)                            │
│      ├── KEY_TYPE=13011 (TMC路况)                             │
│      └── KEY_TYPE=13012 (车道线)                              │
└──────────────────────────────────────────────────────────────┘
```

### 工厂模式说明

```
FloatingWindowManager
  │
  │  recreateWindow() 时调用
  ▼
FloatingWindowFactory.createWindow(currentMode, styleMode, context, inflated)
  │
  ├── MODE_CRUISE + styleMode=1 → new MinimalCruiseWindow()
  ├── MODE_CRUISE + styleMode=0 → new NormalCruiseWindow()
  ├── MODE_NAVI   + styleMode=0 → new NormalNaviWindow()
  ├── MODE_NAVI   + styleMode=1 → new MinimalNaviWindow()
  └── MODE_NAVI   + styleMode=2 → new FullNaviWindow()
```

### 类职责说明

| 类 | 行数 | 职责 |
|----|------|------|
| [RouterActivity](file:///d:/AndroidStudioProjects/Navi-Link/app/src/main/java/com/navi/link/RouterActivity.java) | 102 | 透明路由入口，根据启动方式配置分发到服务或配置页 |
| [MainActivity](file:///d:/AndroidStudioProjects/Navi-Link/app/src/main/java/com/navi/link/MainActivity.java) | 1653 | 主界面，提供启动方式、悬浮窗样式、背景模式、缩放、主题色、关于我们及设备信息系统诊断等全部配置 |
| [AutoMapService](file:///d:/AndroidStudioProjects/Navi-Link/app/src/main/java/com/navi/link/AutoMapService.java) | 117 | 前台服务，创建通知栏常驻通知，初始化和销毁悬浮窗及广播接收器，主动查询昼夜模式 |
| [AmapNaviReceiver](file:///d:/AndroidStudioProjects/Navi-Link/app/src/main/java/com/navi/link/AmapNaviReceiver.java) | 198 | 监听 `AUTONAVI_STANDARD_BROADCAST_SEND` 广播，解析5类KEY_TYPE数据 |
| [FloatingWindowManager](file:///d:/AndroidStudioProjects/Navi-Link/app/src/main/java/com/navi/link/FloatingWindowManager.java) | 1108 | 单例管理器，负责窗口调度、模式切换、物理缩放、数据缓存、拖拽/长按、超时管理等；具体窗口逻辑委托给 BaseFloatingWindow 子类 |
| [FloatingWindowFactory](file:///d:/AndroidStudioProjects/Navi-Link/app/src/main/java/com/navi/link/FloatingWindowFactory.java) | 26 | 工厂类，根据当前模式和样式创建对应的窗口实例 |
| [BaseFloatingWindow](file:///d:/AndroidStudioProjects/Navi-Link/app/src/main/java/com/navi/link/BaseFloatingWindow.java) | 204 | 抽象基类，定义窗口公共接口（数据更新/主题/昼夜），提供工具方法（缩放/图标/颜色/格式化） |
| [MinimalCruiseWindow](file:///d:/AndroidStudioProjects/Navi-Link/app/src/main/java/com/navi/link/MinimalCruiseWindow.java) | 218 | 灵动岛巡航窗口：紧凑胶囊布局，速度 + 道路名 + 多红绿灯 |
| [NormalCruiseWindow](file:///d:/AndroidStudioProjects/Navi-Link/app/src/main/java/com/navi/link/NormalCruiseWindow.java) | 256 | 常规巡航窗口：速度 + 限速 + 电子眼距离 + 道路名 + 车道线 + 多红绿灯 |
| [NormalNaviWindow](file:///d:/AndroidStudioProjects/Navi-Link/app/src/main/java/com/navi/link/NormalNaviWindow.java) | 318 | 常规导航窗口：转向图标 + 距离 + 道路名 + TMC进度条 + ETA + 出口信息 + 红绿灯 |
| [MinimalNaviWindow](file:///d:/AndroidStudioProjects/Navi-Link/app/src/main/java/com/navi/link/MinimalNaviWindow.java) | 211 | 灵动岛导航窗口：紧凑布局，速度 + 转向 + 距离 + 道路名 + 红绿灯 |
| [FullNaviWindow](file:///d:/AndroidStudioProjects/Navi-Link/app/src/main/java/com/navi/link/FullNaviWindow.java) | 341 | 全数据导航窗口：完整驾驶信息 + 车道线 + TMC进度条 + 出口信息 + 红绿灯 |
| [LaneLineView](file:///d:/AndroidStudioProjects/Navi-Link/app/src/main/java/com/navi/link/LaneLineView.java) | 218 | 车道线自定义组件，解析 `EXTRA_DRIVE_WAY` 数据动态渲染车道图标 |
| [TmcProgressBar](file:///d:/AndroidStudioProjects/Navi-Link/app/src/main/java/com/navi/link/TmcProgressBar.java) | 178 | TMC路况进度条自定义组件，分段彩色绘制 + 当前位置标记 |
| [CrashHandler](file:///d:/AndroidStudioProjects/Navi-Link/app/src/main/java/com/navi/link/CrashHandler.java) | 185 | 全局异常捕获，保存崩溃日志到本地，自动清理旧日志 |

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
<uses-permission android:name="android.permission.QUERY_ALL_PACKAGES" />
```

- **悬浮窗权限** (`SYSTEM_ALERT_WINDOW`)：必须手动授权，应用启动时会引导用户开启
- **前台服务权限**：Android 14+ 需要 `FOREGROUND_SERVICE_SPECIAL_USE`
- **通知权限** (`POST_NOTIFICATIONS`)：Android 13+ 前台服务必须显示通知
- **包查询权限** (`QUERY_ALL_PACKAGES`)：Android 11+ 用于从配置页启动高德地图应用

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

例如：`Navi-Link-v2.3-release-202606110830.apk`

### 运行要求

1. Android 4.2 (API 17) 及以上系统
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
  ├─── KEY_TYPE=60073 ──→ FloatingWindowManager
  │                       └── activeWindow.updateTrafficLight()
  │                           或 activeWindow.updateCruiseTrafficLights()
  │
  ├─── KEY_TYPE=13011 ──→ FloatingWindowManager
  │                       └── activeWindow.updateTmcData()
  │                           └── TmcProgressBar.updateTmcData()
  │
  ├─── KEY_TYPE=13012 ──→ FloatingWindowManager
  │                       └── activeWindow.updateLaneLines()
  │                           └── LaneLineView.updateLanes()
  │
  ├─── KEY_TYPE=10019 ──→ FloatingWindowManager.onDayNightChanged()
  │                       └── activeWindow.applyDayNightTextColors()
  │
  └─── KEY_TYPE=10001 ──→ 判断 ICON 字段
                            │
                            ├─ ICON≠0 → switchToNaviMode()
                            │              ├── FloatingWindowFactory → 新窗口
                            │              ├── activeWindow.updateNaviInfo()
                            │              └── activeWindow.updateExitInfo()
                            │
                            └─ ICON=0 → switchToCruiseMode()
                                           ├── FloatingWindowFactory → 新窗口
                                           └── activeWindow.updateCruiseInfo()

用户调节缩放 → recreateWindow()
  ├── 删除旧窗口
  ├── FloatingWindowFactory.createWindow() → 新窗口实例
  ├── 物理缩放（scaleViewRecursive）
  ├── restoreCachedData()  ← 立即恢复最后一次数据，避免闪烁
  └── windowManager.addView() → 显示时已有真实数据
```
