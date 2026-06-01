# Navi-Link 项目说明文档

## 项目概述

**Navi-Link**（内部代号 ShadowMap）是一款 Android 悬浮窗导航应用。它通过监听高德地图（Amap/AutoNavi）的标准广播，将导航信息以悬浮窗形式实时叠加显示在其他应用之上，让用户在使用其他应用时也能看到导航指引。

| 项目属性 | 值 |
|---------|---|
| 包名 | `com.navi.link` |
| 最低 SDK | Android 7.0 (API 24) |
| 目标 SDK | Android 14 (API 34) |
| 编译 SDK | 34 |
| 版本 | 1.0 (versionCode 1) |
| 开发语言 | Java |
| 构建工具 | Gradle + AGP 8.5.0 |

---

## 项目结构

```
Navi-Link/
├── app/
│   ├── src/main/
│   │   ├── java/com/navi/link/
│   │   │   ├── MainActivity.java          # 主界面（配置页面）
│   │   │   ├── AutoMapService.java        # 前台服务（维持悬浮窗生命周期）
│   │   │   ├── AmapNaviReceiver.java      # 广播接收器（解析高德导航数据）
│   │   │   └── FloatingWindowManager.java # 悬浮窗管理器（核心逻辑）
│   │   ├── res/
│   │   │   ├── layout/
│   │   │   │   ├── activity_main.xml                 # 主界面布局
│   │   │   │   ├── layout_floating_cruise.xml        # 巡航模式悬浮窗
│   │   │   │   ├── layout_floating_navi.xml          # 常规导航悬浮窗
│   │   │   │   ├── layout_floating_navi_minimal.xml  # 灵动岛导航悬浮窗
│   │   │   │   ├── layout_floating_traffic_light_group.xml  # 红绿灯胶囊组件
│   │   │   │   └── item_cruise_traffic_light.xml     # 巡航红绿灯单项
│   │   │   ├── drawable/             # 背景图形资源
│   │   │   └── mipmap-*/             # 图标资源（转向箭头、红绿灯、方向指示）
│   │   └── AndroidManifest.xml
│   └── build.gradle
├── gradle/
│   └── libs.versions.toml           # 版本目录（统一依赖管理）
├── build.gradle                     # 顶级构建脚本
├── settings.gradle
└── navi.jks                         # 签名密钥文件
```

---

## 核心功能

### 1. 双模式悬浮窗

| 模式 | 说明 | 触发条件 |
|------|------|---------|
| **巡航模式** (MODE_CRUISE) | 显示当前速度和道路名称，可附带红绿灯倒计时 | 无转向图标时默认进入 |
| **导航模式** (MODE_NAVI) | 显示转弯指示、剩余距离、道路名、进度条、ETA、红绿灯 | 接收到转向图标(ICON≠0)时自动切换 |

### 2. 双样式导航悬浮窗

| 样式 | 说明 |
|------|------|
| **常规样式** | 完整导航信息：大转向图标 + 距离数字 + 动作文字 + 道路名 + 进度条 + 底部摘要(剩余距离·时间) + ETA预计到达时间 |
| **灵动岛样式**（精简） | 精简布局：当前速度 + 转向图标 + 剩余距离 + 道路名 + 红绿灯 |

### 3. 红绿灯实时显示

- **导航模式红绿灯**：显示单一路口红绿灯状态（红/黄/绿）+ 方向箭头 + 倒计时秒数，5秒无更新自动隐藏
- **巡航模式红绿灯**：支持同时显示多个方向的红绿灯倒计时（JSONArray 批量数据），所有灯倒计时归零后自动隐藏容器

### 4. 个性化配置

- **主题色**：8种预设颜色可选（黑/蓝/浅蓝/橙/粉红/紫/深橙/青绿），自动计算文字对比度
- **缩放**：0.5x ~ 2.0x 无极滑块调节
- **拖拽定位**：悬浮窗可自由拖拽，长按500ms锁定/解锁位置
- **配置持久化**：所有设置通过 `SharedPreferences` 保存

### 5. 超时与看门狗机制

| 机制 | 超时时间 | 效果 |
|------|---------|------|
| 导航超时 | 6秒 | 6秒内无导航数据则自动切回巡航模式 |
| 巡航宽容 | 3秒 | 导航模式下短暂无转向图标时，给3秒宽容期 |
| 看门狗 | 5秒 | 5秒内无任何数据则隐藏悬浮窗 |

---

## 架构设计

```
┌──────────────────────────────────────────────────┐
│                   MainActivity                    │
│            (配置界面：样式/缩放/主题色)              │
│                      │ 启动                       │
│                      ▼                            │
│               AutoMapService                      │
│            (前台Service，维持进程存活)              │
│         ┌──────┼──────────────┐                   │
│         ▼      │              ▼                   │
│  AmapNaviReceiver  │   FloatingWindowManager      │
│  (监听高德广播)     │   (悬浮窗单例管理器)           │
│   │              │     │  │  │                   │
│   │  广播数据     │     │  │  └── 位置拖拽/锁定     │
│   │   ├─ 60073   │     │  └── 缩放变换            │
│   │   │  红绿灯   │     └── 窗口模式切换/重建       │
│   │   └─ 10001   │                               │
│   │     导航/巡航  │    ┌────────┬─────────┐       │
│   └──────────────┘    │        │         │       │
│                       ▼        ▼         ▼       │
│                    巡航窗   常规导航窗  灵动岛窗     │
│                      (悬浮窗浮层)                  │
└──────────────────────────────────────────────────┘
```

### 类职责说明

| 类 | 职责 |
|----|------|
| [MainActivity](file:///d:/AndroidStudioProjects/Navi-Link/app/src/main/java/com/navi/link/MainActivity.java) | 主界面，提供样式切换、缩放调节、主题色选择功能，启动服务前检查悬浮窗权限 |
| [AutoMapService](file:///d:/AndroidStudioProjects/Navi-Link/app/src/main/java/com/navi/link/AutoMapService.java) | 前台服务，创建通知栏常驻通知，初始化和销毁悬浮窗及广播接收器，保证后台存活 |
| [AmapNaviReceiver](file:///d:/AndroidStudioProjects/Navi-Link/app/src/main/java/com/navi/link/AmapNaviReceiver.java) | 监听 `AUTONAVI_STANDARD_BROADCAST_SEND` 广播，解析 `KEY_TYPE=60073`（红绿灯）和 `KEY_TYPE=10001`（导航/巡航）数据 |
| [FloatingWindowManager](file:///d:/AndroidStudioProjects/Navi-Link/app/src/main/java/com/navi/link/FloatingWindowManager.java) | 单例悬浮窗管理器（893行），负责窗口创建/销毁/重建、模式切换、缩放、主题色应用、触摸拖拽、位置保存、超时管理等全部悬浮窗逻辑 |

---

## 技术要点

### 高德广播数据协议

应用监听高德地图发出的标准广播 `AUTONAVI_STANDARD_BROADCAST_SEND`，解析两类关键数据：

| KEY_TYPE | 含义 | 关键字段 |
|----------|------|---------|
| `10001` | 导航/巡航信息 | `ICON`/`NEW_ICON`(转向图标), `CUR_SPEED`(当前速度), `SEG_REMAIN_DIS_AUTO`(段剩余距离), `ROUTE_REMAIN_DIS`/`_AUTO`(全程剩余距离), `ROUTE_REMAIN_TIME_AUTO`(剩余时间), `ETA_TEXT`(预计到达), `NEXT_ROAD_NAME`/`CUR_ROAD_NAME`(道路名), `ROUTE_ALL_DIS`(全程总距离) |
| `60073` | 红绿灯数据 | `trafficLightStatus`(灯状态), `dir`(方向), `redLightCountDownSeconds`(倒计时), `lightsData`(巡航模式JSON数组) |

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

例如：`Navi-Link-v1.0-release-202605290830.apk`

### 运行要求

1. Android 7.0 (API 24) 及以上系统
2. 必须授予「悬浮窗」权限
3. 必须安装高德地图（用于发出导航广播）
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
  │                       或 FloatingWindowManager.updateCruiseTrafficLights()
  │
  └─── KEY_TYPE=10001 ──→ 判断 ICON 字段
                            │
                            ├─ ICON≠0 → switchToNaviMode() → updateNaviInfo()
                            └─ ICON=0 → 巡航模式 updateCruiseInfo()
                                        或导航宽容期
```
