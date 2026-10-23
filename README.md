# 阅微 2.3.2 VIP 解锁模块 · YukiHook 1.5.0

框架：[YukiHook](https://github.com/HighCapable/YukiHook)（1.5.0，原 YukiHookAPI 重写版）

目标包：`app.zhendong.reamicro`（阅微 2.3.2）

## 一、Hook 点位

| 优先级 | 类 | 方法 | 处理 |
|---|---|---|---|
| ① 核心 | `data.res.user.UserModelsKt` | `isVipActive(UserInfo): Boolean` | `intercept(true)` |
| ② 展示 | `data.res.user.UserInfo` | `getUserType(): Int` | `intercept(4)`（SUPER_VIP） |
| ③ 展示 | `data.res.user.UserInfo` | `getVipExpireTime(): String` | `intercept("2099-12-31 23:59:59")` |
| ④ 同步 | `lan.sync.SyncPolicy` | `isVip(): Boolean` | `intercept(true)` |
| ⑤ 可选 | `data.res.user.UserInfo` | `getCoin()/getGem()` | 默认关（服务端对账） |

①是唯一入口，21 处调用点全走它：增强服务、局域网同步策略、阅读器滚动分页、标签浮层、豆瓣评分/单本图表、关联封面下载、动态主题、E-ink 概览、同步页、会员页状态。

## 二、1.5.0 关键变化（相比 1.1.x/1.2.x）

- 项目更名为 **YukiHook**，拆分为多模块：`yukihook-core` + `yukihook-compiler` + 基础模块（`yukihook-runtime-xposed82` / `yukihook-runtime-libxposed`）
- 入口由 `IYukiHookXposedInit` 变为 **`YukiHookXposedModule`**，注解变为 **`@YukiHookXposed82Entry`**
- 配置入口：`YukiHook.configure { logging { ... }; debug; dataChannel }`，Hook 入口：`YukiHook.encase { loadApp(...) { } }`
- 方法查找走 **KavaRef**：`"类名".toClass().resolve().firstMethod { name/emptyParameters/returnType }`
- 结果替换用 **`intercept(value)`**（替代旧的 `result =`）
- `assets/xposed_init`、`xposedmodule` meta-data 由 **KSP 编译器自动生成**，不再手写

## 三、工程结构

```
hook_module_yuki/
├── settings.gradle.kts        # mavenCentral + YukiHook 备用库 + api.xposed.info
├── build.gradle.kts           # AGP 8.5.2 / Kotlin 1.9.24 / KSP 1.9.24-1.0.20
├── gradle.properties
├── .github/workflows/build.yml
└── app/
    ├── build.gradle.kts       # yukihook-bom 1.5.0 + core/runtime-xposed82/extension-kavaref + ksp compiler
    ├── proguard-rules.pro
    └── src/main/
        ├── AndroidManifest.xml    # 仅保留 description / minversion / scope
        ├── res/values/arrays.xml  # 作用域：app.zhendong.reamicro
        └── java/com/op/hook/reamicro/HookEntry.kt
```

## 四、核心代码

```kotlin
@YukiHookXposed82Entry
object HookEntry : YukiHookXposedModule {

    override fun onInit() {
        YukiHook.configure {
            logging { tag = "ReaMicroVIP"; enabled = true; recording = false }
            debug = false
            dataChannel = false
        }
    }

    override fun onHook() {
        YukiHook.encase {
            loadApp("app.zhendong.reamicro") {
                "app.zhendong.reamicro.data.res.user.UserModelsKt".toClass().resolve()
                    .firstMethod {
                        name = "isVipActive"
                        emptyParameters()
                        returnType = Boolean::class
                    }
                    .hook { intercept(true) }
            }
        }
    }
}
```

## 五、依赖

```kotlin
implementation(platform("com.highcapable.yukihookapi:yukihook-bom:1.5.0"))
implementation("com.highcapable.yukihookapi:yukihook-core")
implementation("com.highcapable.yukihookapi:yukihook-runtime-xposed82")
implementation("com.highcapable.yukihookapi:yukihook-extension-kavaref")
implementation(platform("com.highcapable.kavaref:kavaref-bom:1.1.1"))
implementation("com.highcapable.kavaref:kavaref-core")
implementation("com.highcapable.kavaref:kavaref-android")
implementation("com.highcapable.kavaref:kavaref-extension")
ksp("com.highcapable.yukihookapi:yukihook-compiler")
compileOnly("de.robv.android.xposed:api:82")
```

## 六、构建 & 使用

```bash
./gradlew :app:assembleDebug
# 或直接看 GitHub Actions 产物
```

1. LSPosed 安装模块 → 启用 → 作用域勾「阅微」
2. `am force-stop app.zhendong.reamicro` 后重开
3. 日志：`logcat -s ReaMicroVIP`

## 七、边界

- 金币/宝石/墨点、内购开通、VIP 云端接口 → 服务端权威，只能改本地展示
- 版本升级若更换类名/方法名，需重新用 MT 跑一遍 `isVipActive` 的交叉引用定位