package com.op.hook.reamicro

import com.highcapable.yukihookapi.annotation.xposed.InjectYukiHookWithXposed
import com.highcapable.yukihookapi.hook.factory.method
import com.highcapable.yukihookapi.hook.factory.toClass
import com.highcapable.yukihookapi.hook.log.YLog
import com.highcapable.yukihookapi.hook.type.java.BooleanType
import com.highcapable.yukihookapi.hook.type.java.IntType
import com.highcapable.yukihookapi.hook.type.java.StringClass
import com.highcapable.yukihookapi.hook.xposed.proxy.IYukiHookXposedInit

/**
 * 阅微 2.3.2（app.zhendong.reamicro）VIP 解锁 · YukiHookAPI 1.1.x
 *
 * 逆向结论：所有本地会员判定收敛在
 *   data.res.user.UserModelsKt.isVipActive(UserInfo): Boolean
 * 共 21 处调用点（增强服务 / 局域网同步 / 阅读器分页 / 豆瓣评分 / 图表 / 关联封面 / 动态主题 …）
 */
@InjectYukiHookWithXposed
class HookEntry : IYukiHookXposedInit {

    override fun onInit() = configs {
        debugTag = TAG
        // 发布时改成 BuildConfig.DEBUG
        isDebug = true
    }

    override fun onHook() = encase {
        loadApp(name = PKG) {

            // ① 核心开关：isVipActive(UserInfo) 恒 true
            runCatching {
                USER_MODELS.toClass()
                    .method {
                        name = "isVipActive"
                        returnType = BooleanType
                    }
                    .hook {
                        before { result = true }
                    }
                YLog.info("$TAG | isVipActive -> true hooked")
            }.onFailure {
                YLog.error("$TAG | hook isVipActive failed: $it")
            }

            // ② UI 展示：userType = 4 (UserType.SUPER_VIP)
            runCatching {
                USER_INFO.toClass()
                    .method {
                        name = "getUserType"
                        emptyParameters()
                        returnType = IntType
                    }
                    .hook {
                        after { result = USER_TYPE_SUPER_VIP }
                    }
                YLog.info("$TAG | getUserType -> $USER_TYPE_SUPER_VIP hooked")
            }.onFailure {
                YLog.error("$TAG | hook getUserType failed: $it")
            }

            // ③ UI 展示：到期时间拉到 2099
            runCatching {
                USER_INFO.toClass()
                    .method {
                        name = "getVipExpireTime"
                        emptyParameters()
                        returnType = StringClass
                    }
                    .hook {
                        after { result = VIP_EXPIRE_FAKE }
                    }
                YLog.info("$TAG | getVipExpireTime -> $VIP_EXPIRE_FAKE hooked")
            }.onFailure {
                YLog.error("$TAG | hook getVipExpireTime failed: $it")
            }

            // ④ 局域网同步策略
            runCatching {
                SYNC_POLICY.toClass()
                    .method {
                        name = "isVip"
                        returnType = BooleanType
                    }
                    .hook {
                        before { result = true }
                    }
                YLog.info("$TAG | SyncPolicy.isVip -> true hooked")
            }.onFailure {
                YLog.error("$TAG | hook SyncPolicy.isVip failed: $it")
            }

            // ⑤ 可选：金币/宝石（服务端对账，默认关闭）
            if (HOOK_COIN) {
                runCatching {
                    USER_INFO.toClass()
                        .method {
                            name = "getCoin"
                            emptyParameters()
                            returnType = IntType
                        }
                        .hook { after { result = 999999 } }
                    USER_INFO.toClass()
                        .method {
                            name = "getGem"
                            emptyParameters()
                            returnType = IntType
                        }
                        .hook { after { result = 999999 } }
                    YLog.info("$TAG | coin/gem hooked (local display only)")
                }.onFailure {
                    YLog.error("$TAG | hook coin/gem failed: $it")
                }
            }

            YLog.info("$TAG | all hooks installed")
        }
    }

    private companion object {
        const val TAG = "ReaMicroVIP"

        const val PKG = "app.zhendong.reamicro"

        const val USER_INFO = "app.zhendong.reamicro.data.res.user.UserInfo"
        const val USER_MODELS = "app.zhendong.reamicro.data.res.user.UserModelsKt"
        const val SYNC_POLICY = "app.zhendong.reamicro.lan.sync.SyncPolicy"

        const val USER_TYPE_SUPER_VIP = 4
        const val VIP_EXPIRE_FAKE = "2099-12-31 23:59:59"

        const val HOOK_COIN = false
    }
}