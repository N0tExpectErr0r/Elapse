一个基于 ASM 实现的 Gradle Plugin，可以在编译期对注解所指定的方法进行插桩，在方法调用前后执行指定逻辑。

具体实现思路文章可以见此篇文章： [跟我一起用 ASM 实现编译期字节码插桩](http://blog.n0texpecterr0r.cn/?p=752)

## 使用方式

### 引入

首先需要将 `elapse` 及 `elapse-asm` 导入到项目中。

在项目的 build.gradle 中将其添加到 classpath 中：

```
repositories {
    //...
    maven {
        url uri("repo")
    }
}
 
dependencies {
    // ...
    classpath 'com.n0texpecterr0r.build:elapse-asm:1.0.0'
}
```

在 App module 中将插件 apply：

```
apply plugin: 'com.n0texpecterr0r.elapse-asm'
```

### 使用

在需要进行插桩的方法上加入注解：

```
@TrackMethod(tag = TAG_LOG)
public void test() {
    try {
        Thread.sleep(1200);
    } catch (InterruptedException e) {
        e.printStackTrace();
    }
}
```

在 Application 中注册 `MethodObserver`，并实现对应方法：

```
MethodEventManager.getInstance()
        .registerMethodObserver(TAG_LOG, new MethodObserver() {
            @Override
            public void onMethodEnter(String tag, String methodName) {
                Log.d("MethodEvent", "method "+methodName+" entered at "+System.currentTimeMillis());
            }
            @Override
            public void onMethodExit(String tag, String methodName) {
                Log.d("MethodEvent", "method "+methodName+" exited at "+System.currentTimeMillis());
            }
        });
```

程序运行时，在调用对应方法前后会执行 `MethodObserver` 中的对应方法，从而实现在方法调用前后执行指定逻辑。