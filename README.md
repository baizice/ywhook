# 阅微 2.3.2 VIP 解锁模块 · YukiHookAPI 版

框架：<https://github.com/HighCapable/YukiHookAPI>（YukiHookAPI 1.1.11 + Kotlin DSL）

目标包：`app.zhendong.reamicro`（阅微 2.3.2）

## 一、Hook 点位（逆向结论）

| 优先级 | 类 | 方法 | 处理 |
|---|---|---|---|
| ① 核心 | `data.res.user.UserModelsKt` | `isVipActive(UserInfo): Boolean` | `before { result = true }` |
| ② 展示 | `data.res.user.UserInfo` | `getUserType(): Int` | `after { result = 4 }`（SUPER_VIP） |
| ③ 展示 | `data.res.user.UserInfo` | `getVipExpireTime(): String` | `after { result = "2099-12-31 23:59:59" }` |
| ④ 同步 | `lan.sync.SyncPolicy` | `isVip(): Boolean` | `before { result = true }` |
| ⑤ 可选 | `data.res.user.UserInfo` | `getCoin()/getGem()` | 默认关（服务端对账） |

①是唯一入口，21 处调用点全走它：增强服务、局域网同步策略、阅读器滚动分页、标签浮层、豆瓣评分/单本图表、关联封面下载、动态主题、E-ink 概览、同步页、会员页状态。

## 二、工程结构

```
hook_module_yuki/
├── settings.gradle.kts        # 额外加了 maven("https://api.xposed.info/")
├── build.gradle.kts           # AGP 8.5.2 + Kotlin 1.9.24
├── gradle.properties
└── app/
    ├── build.gradle.kts       # yukihookapi:api:1.1.11 + compileOnly xposed api
    ├── proguard-rules.pro
    └── src/main/
        ├── AndroidManifest.xml    # xposedmodule / xposedscope
        ├── assets/xposed_init     # com.op.hook.reamicro.HookEntry
        ├── res/values/arrays.xml
        └── java/com/op/hook/reamicro/HookEntry.kt
```

> 用了 `@InjectYukiHookWithXposed` 注解。**不接 KSP 时**，`assets/xposed_init` 手写即可（已提供）；想要注解自动生成入口，再在 `app/build.gradle.kts` 里加：
> ```kotlin
> plugins { id("com.google.devtools.ksp") version "1.9.24-1.0.20" }
> dependencies { ksp("com.highcapable.yukihookapi:ksp-xposed:1.1.11") }
> ```

## 三、核心代码

```kotlin
@InjectYukiHookWithXposed
class HookEntry : IYukiHookXposedInit {

    override fun onInit() = configs {
        debugTag = "ReaMicroVIP"
        isDebug = true
    }

    override fun onHook() = encase {
        loadApp(name = "app.zhendong.reamicro") {
            // 核心：全局 VIP 判定恒真
            "app.zhendong.reamicro.data.res.user.UserModelsKt".toClass()
                .method {
                    name = "isVipActive"
                    returnType = BooleanType
                }.hook {
                    before { result = true }
                }
        }
    }
}
```

## 四、编译

```bash
cd hook_module_yuki
./gradlew :app:assembleDebug
# 产物 app/build/outputs/apk/debug/app-debug.apk
```

依赖来源：`google()` + `mavenCentral()` + `https://api.xposed.info/`（Xposed API 82）。

## 五、使用

1. LSPosed 安装模块 → 启用 → 作用域勾 **阅微**
2. `am force-stop app.zhendong.reamicro` 后重开
3. 日志验证（YukiHookAPI 自带日志前缀）：
   ```bash
   logcat -s LSPosed-Bridge:* | grep ReaMicroVIP
   ```
   期望：
   ```
   ReaMicroVIP | isVipActive -> true hooked
   ReaMicroVIP | getUserType -> 4 hooked
   ReaMicroVIP | getVipExpireTime -> 2099-12-31 23:59:59 hooked
   ReaMicroVIP | SyncPolicy.isVip -> true hooked
   ReaMicroVIP | all hooks installed
   ```

## 六、YukiHookAPI 相比原生 Xposed 的优势（这个场景）

- `loadApp(name)` 自带包名过滤 + 进程/类加载就绪时机处理，不用手写 `handleLoadPackage`
- `method { name / emptyParameters / returnType }` 是结构化查找，方法签名变更时比 `findAndHookMethod` 字符串硬编码更抗改
- `runCatching` + `YLog` 统一异常隔离，某个点位失败不影响其余 hook 安装
- 内建 `replaceTo` / `result` 语义、`HookParam` 封装，后续要扩点位（比如伪造服务器响应）扩展更快

## 七、边界

- 金币/宝石/墨点、内购开通、VIP 云端接口 → 服务端权威，hook 只能改本地展示
- 若后续版本把判定下放到 native 或换类名，需重新用 mt 跑一遍 `isVipActive` 的 xref 定位