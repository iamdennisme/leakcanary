# leakcanary分析

# 依赖关系

## leakcanary-android-sample
- leakcanary-android
- leakcanary-android-instrumentation
## leakcanary-android
- leakcanary-android-core
## leakcanary-android-core
- shark-android
- leakcanary-object-watcher-android
- leakcanary-object-watcher-android-androidx
## leakcanary-android-instrumentation
- leakcanary-android-core
## leakcanary-android-process
- leakcanary-android-core
##  leakcanary-object-watcher-android-androidx
- leakcanary-object-watcher-android
## leakcanary-object-watcher-android
- leakcanary-object-watcher

![libtree](/Users/daicheng.huang/source/apm/leakcanary/wiki/image/leakcanary-android-sample.png)



# 源码分析

1.根据模块依赖找到初始化的代码

```java
AppWatcherInstaller
```

```
InternalAppWatcher.install(application)//初始化逻辑
```

# 
