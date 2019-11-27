/*
 * Copyright (C) 2015 Square, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package leakcanary.internal

import android.app.Activity
import android.app.Application
import leakcanary.AppWatcher.Config
import leakcanary.ObjectWatcher
import leakcanary.internal.InternalAppWatcher.noOpDelegate

internal class ActivityDestroyWatcher private constructor(
  private val objectWatcher: ObjectWatcher,
  private val configProvider: () -> Config
) {

  private val lifecycleCallbacks =
    object : Application.ActivityLifecycleCallbacks by noOpDelegate() {//委托协议 动态代理注册onDestory
      override fun onActivityDestroyed(activity: Activity) {
        if (configProvider().watchActivities) {//判断activity，则监听activity
          objectWatcher.watch(activity)
        }
      }
    }

  companion object {
    fun install(
      application: Application,
      objectWatcher: ObjectWatcher,
      configProvider: () -> Config
    ) {
      val activityDestroyWatcher =
        ActivityDestroyWatcher(objectWatcher, configProvider)//创建activity监听
      application.registerActivityLifecycleCallbacks(activityDestroyWatcher.lifecycleCallbacks)//注册生命周期
    }
  }
}
