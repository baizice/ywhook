package com.op.hook.reamicro

import android.util.Log

import com.highcapable.kavaref.KavaRef.Companion.resolve
import com.highcapable.yukihookapi.YukiHook
import com.highcapable.yukihookapi.annotation.xposed.YukiHookXposed82Entry
import com.highcapable.yukihookapi.extension.kavaref.hook
import com.highcapable.yukihookapi.extension.kavaref.intercept
import com.highcapable.yukihookapi.extension.kavaref.toClass
import com.highcapable.yukihookapi.hook.xposed.YukiHookXposedModule

/**
 * 阅微 2.3.2（app.zhendong.reamicro）VIP 解锁模块
 *
 * 框架：YukiHook 1.5.0（yukihook-core / runtime-xposed82 / extension-kavaref / compiler）
 *
 * 逆向结论：所有本地会员判定收敛在
 *   data.res.user.UserModelsKt.isVipActive(UserInfo): Boolean
 * 21 处调用点（增强服务 / 局域网同步 / 阅读器分页 / 豆瓣评分 / 图表 / 关联封面 / 动态主题 …）
 */
@YukiHookXposed82Entry
object HookEntry : YukiHookXposedModule {

    override fun onInit() {
        YukiHook.configure {
            logging {
                tag = TAG
                enabled = true
                recording = false
            }
            debug = false
            dataChannel = false
        }
    }

    override fun onHook() {
        YukiHook.encase {
            loadApp(PKG) {

                // ① 核心开关：isVipActive(UserInfo) 恒 true
                runCatching {
                    USER_MODELS.toClass().resolve()
                        .firstMethod {
                            name = "isVipActive"
                            emptyParameters()
                            returnType = Boolean::class
                        }
                        .hook { intercept(true) }
                    Log.i(TAG, "isVipActive -> true hooked")
                }.onFailure { Log.e(TAG, "hook isVipActive failed", it) }

                // ② UI：userType -> 4 (UserType.SUPER_VIP)
                runCatching {
                    USER_INFO.toClass().resolve()
                        .firstMethod {
                            name = "getUserType"
                            emptyParameters()
                            returnType = Int::class
                        }
                        .hook { intercept(USER_TYPE_SUPER_VIP) }
                    Log.i(TAG, "getUserType -> $USER_TYPE_SUPER_VIP hooked")
                }.onFailure { Log.e(TAG, "hook getUserType failed", it) }

                // ③ UI：到期时间 -> 2099
                runCatching {
                    USER_INFO.toClass().resolve()
                        .firstMethod {
                            name = "getVipExpireTime"
                            emptyParameters()
                            returnType = String::class
                        }
                        .hook { intercept(VIP_EXPIRE_FAKE) }
                    Log.i(TAG, "getVipExpireTime -> $VIP_EXPIRE_FAKE hooked")
                }.onFailure { Log.e(TAG, "hook getVipExpireTime failed", it) }

                // ④ 局域网同步策略
                runCatching {
                    SYNC_POLICY.toClass().resolve()
                        .firstMethod {
                            name = "isVip"
                            emptyParameters()
                            returnType = Boolean::class
                        }
                        .hook { intercept(true) }
                    Log.i(TAG, "SyncPolicy.isVip -> true hooked")
                }.onFailure { Log.e(TAG, "hook SyncPolicy.isVip failed", it) }

                // ⑤ 可选：金币/宝石（服务端对账，默认关闭）
                if (HOOK_COIN) {
                    runCatching {
                        USER_INFO.toClass().resolve()
                            .firstMethod {
                                name = "getCoin"
                                emptyParameters()
                                returnType = Int::class
                            }
                            .hook { intercept(999999) }
                        USER_INFO.toClass().resolve()
                            .firstMethod {
                                name = "getGem"
                                emptyParameters()
                                returnType = Int::class
                            }
                            .hook { intercept(999999) }
                        Log.i(TAG, "coin/gem hooked (display only)")
                    }.onFailure { Log.e(TAG, "hook coin/gem failed", it) }
                }

                Log.i(TAG, "all hooks installed")
            }
        }
    }

    private const val TAG = "ReaMicroVIP"

    private const val PKG = "app.zhendong.reamicro"
    private const val USER_INFO = "app.zhendong.reamicro.data.res.user.UserInfo"
    private const val USER_MODELS = "app.zhendong.reamicro.data.res.user.UserModelsKt"
    private const val SYNC_POLICY = "app.zhendong.reamicro.lan.sync.SyncPolicy"

    private const val USER_TYPE_SUPER_VIP = 4
    private const val VIP_EXPIRE_FAKE = "2099-12-31 23:59:59"

    private const val HOOK_COIN = false
}