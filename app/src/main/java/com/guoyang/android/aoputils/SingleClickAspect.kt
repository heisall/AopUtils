package com.guoyang.android.aoputils

import android.util.Log
import org.aspectj.lang.ProceedingJoinPoint
import org.aspectj.lang.annotation.Around
import org.aspectj.lang.annotation.Aspect
import org.aspectj.lang.annotation.Pointcut

import java.util.Calendar

/**
 * Created by guoyang on 2018/4/15.
 * github https://github.com/GuoYangGit
 * QQ:352391291
 */

@Aspect
class SingleClickAspect {

    @Pointcut("execution(@com.guoyang.android.aoputils.SingleClick * *(..))")//方法切入点
    fun methodAnnotated() {

    }

    /**
     * joinPoint.proceed() 执行注解所标识的代码
     * @After 可以在方法前插入代码
     * @Before 可以在方法后插入代码
     * @Around 可以在方法前后各插入代码
     */
    @Around("methodAnnotated()")
    @Throws(Throwable::class)
    fun aroundJoinPoint(joinPoint: ProceedingJoinPoint) {
        val currentTime = Calendar.getInstance().timeInMillis
        if (currentTime - lastClickTime > MIN_CLICK_DELAY_TIME) {//过滤掉600毫秒内的连续点击
            if (BuildConfig.DEBUG) {
                Log.d(TAG, "currentTime:" + currentTime)
            }
            lastClickTime = currentTime
            //执行原方法
            joinPoint.proceed()
        }
    }

    companion object {
        const val TAG = "SingleClickAspect"
        const val MIN_CLICK_DELAY_TIME = 600
        var lastClickTime = 0L
    }
}