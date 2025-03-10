package leakcanary.internal

import android.app.Application
import android.content.pm.ApplicationInfo
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import leakcanary.Clock
import leakcanary.AppWatcher
import leakcanary.ObjectWatcher
import leakcanary.OnObjectRetainedListener
import shark.SharkLog
import java.lang.reflect.InvocationHandler
import java.lang.reflect.Proxy
import java.util.concurrent.Executor

internal object InternalAppWatcher {

  val isInstalled
    get() = ::application.isInitialized

  private val onAppWatcherInstalled: (Application) -> Unit

  val isDebuggableBuild by lazy {
    (application.applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE) != 0
  }

  lateinit var application: Application

  private val clock = object : Clock {
    override fun uptimeMillis(): Long {
      return SystemClock.uptimeMillis()
    }
  }

  private val mainHandler = Handler(Looper.getMainLooper())

  init {//反射创建InternalLeakCanary
    val internalLeakCanary = try {
      val leakCanaryListener = Class.forName("leakcanary.internal.InternalLeakCanary")
      leakCanaryListener.getDeclaredField("INSTANCE")
          .get(null)
    } catch (ignored: Throwable) {
      NoLeakCanary
    }
    @kotlin.Suppress("UNCHECKED_CAST")
    onAppWatcherInstalled = internalLeakCanary as (Application) -> Unit
  }

  private val checkRetainedExecutor = Executor {
    mainHandler.postDelayed(it, AppWatcher.config.watchDurationMillis)
  }
  val objectWatcher = ObjectWatcher(
      clock = clock,//时间
      checkRetainedExecutor = checkRetainedExecutor,
      isEnabled = { AppWatcher.config.enabled }//根据配置是否监听
  )

  fun install(application: Application) {
    SharkLog.logger = DefaultCanaryLog()//初始化日志工具
    SharkLog.d { "Installing AppWatcher" }
    checkMainThread()
    if (this::application.isInitialized) {//已经初始化过
      return
    }
    InternalAppWatcher.application = application

    val configProvider = { AppWatcher.config }//获取config。//根据是否初始化判断是否监听
    ActivityDestroyWatcher.install(application, objectWatcher, configProvider)//注册Activity
    FragmentDestroyWatcher.install(application, objectWatcher, configProvider)//注册fragment
    onAppWatcherInstalled(application)//调用InternalLeakCanary.invoke()
  }

  inline fun <reified T : Any> noOpDelegate(): T {
    val javaClass = T::class.java
    val noOpHandler = InvocationHandler { _, _, _ ->
      // no op
    }
    return Proxy.newProxyInstance(
        javaClass.classLoader, arrayOf(javaClass), noOpHandler
    ) as T
  }

  private fun checkMainThread() {
    if (Looper.getMainLooper().thread !== Thread.currentThread()) {
      throw UnsupportedOperationException(
          "Should be called from the main thread, not ${Thread.currentThread()}"
      )
    }
  }

  object NoLeakCanary : (Application) -> Unit, OnObjectRetainedListener {
    override fun invoke(application: Application) {
    }

    override fun onObjectRetained() {
    }
  }
}