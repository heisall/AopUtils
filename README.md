> 这段时间一直在整理项目代码结构，做一些8.0的适配，项目中所用到了button不能多次点击，一直想着不影响代码结构进行hook，这次抽周六日的时间正好好好研究下aop与踩踩坑。

### AspectJ

说到Aop，我们这里可能想到APT,AspectJ,Javassist这三个。这里为了上手与学习成本，这次选择AspectJ，具体的AspectJ介绍这里就不介绍了，本人比较懒。
	
可以通过文章去了解：[安卓AOP三剑客:APT,AspectJ,Javassist](https://www.jianshu.com/p/dca3e2c8608a)

如果使用AspectJ，需要在项目的build里面进行一大丢配置，这里为了方便快捷，推荐使用沪江的[gradle_plugin_android_aspectjx](https://github.com/HujiangTechnology/gradle_plugin_android_aspectjx)。

### 直接入坑

1. 按照github使用指南，添加依赖。
	
	根目录的build
	
	```
	buildscript {
		...
	    dependencies {
	        classpath 'com.android.tools.build:gradle:3.0.1'
	        //添加依赖，如果studio是3.0以上版本，建议使用v1.1.1
	        classpath 'com.hujiang.aspectjx:gradle-android-plugin-aspectjx:1.1.1'
	        //注意不能少了aspectjtools的依赖，否则会报错
	        //同样aspectjtools可以不写在这里，写在app主module的dependencies下面。
	    }
	}
	
	allprojects {
	    ...
	}
	```
	
	app主目录中的build
	
	```
	//或者这样也可以
	apply plugin: 'com.hujiang.android-aspectjx'
	
	android {
	    ...
	}
	
	dependencies {
		implementation fileTree(dir: 'libs', include: ['*.jar'])
	    ...
	    //aspectjrt的依赖
	    implementation 'org.aspectj:aspectjrt:1.8.13'
	}

	```
	
2. 功能实现

	这次我们简单的实现下防止button多次点击，我这里使用kotlin实现，java代码原理相同。
	
	我们先来添加切点(注解)
	
	``` java
	/**
	 * AnnotationRetention.SOURCE：不存储在编译后的 Class 文件。
	 * AnnotationRetention.BINARY：存储在编译后的 Class 文件，但是反射不可见。
	 * AnnotationRetention.RUNTIME：存储在编译后的 Class 文件，反射可见。
	 */
	@Retention(AnnotationRetention.RUNTIME)
	/**
	 * AnnotationTarget.CLASS：类，接口或对象，注解类也包括在内。
	 * AnnotationTarget.ANNOTATION_CLASS：只有注解类。
	 * AnnotationTarget.TYPE_PARAMETER：Generic type parameter (unsupported yet)通用类型参数（还不支持）。
	 * AnnotationTarget.PROPERTY：属性。
	 * AnnotationTarget.FIELD：字段，包括属性的支持字段。
	 * AnnotationTarget.LOCAL_VARIABLE：局部变量。
	 * AnnotationTarget.VALUE_PARAMETER：函数或构造函数的值参数。
	 * AnnotationTarget.CONSTRUCTOR：仅构造函数（主函数或者第二函数）。
	 * AnnotationTarget.FUNCTION：方法（不包括构造函数）。
	 * AnnotationTarget.PROPERTY_GETTER：只有属性的 getter。
	 * AnnotationTarget.PROPERTY_SETTER：只有属性的 setter。
	 * AnnotationTarget.TYPE：类型使用。
	 * AnnotationTarget.EXPRESSION：任何表达式。
	 * AnnotationTarget.FILE：文件。
	 * AnnotationTarget.TYPEALIAS：@SinceKotlin("1.1") 类型别名，Kotlin1.1已可用。
	 */
	@Target(AnnotationTarget.FUNCTION)
	annotation class SingleClick
	```
	
	接下来我们来编写切面(具体的实现)
	
	``` java
	//使用@Aspect注解标示这是一个切面类
	@Aspect
	class SingleClickAspect {
		//@Pointcut来标识所要寻找的切点，就是我们定义的@ SingleClick注解
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
	    //@Throws这个注解不必在意，这个是kotlin的注解，标识该方法可以抛出异常
	    @Throws(Throwable::class)
	    fun aroundJoinPoint(joinPoint: ProceedingJoinPoint) {
	    	//获取系统当前时间
	        val currentTime = Calendar.getInstance().timeInMillis
	        //当前时间-上次记录时间>过滤的时间 过滤掉600毫秒内的连续点击
	        //表示该方法可以执行
	        if (currentTime - lastClickTime > MIN_CLICK_DELAY_TIME) {
	            if (BuildConfig.DEBUG) {
	                Log.d(TAG, "currentTime:" + currentTime)
	            }
	            //将刚进入方法的时间赋值给上次点击时间
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
	```
	
	这样我们的这个防止button多次点击功能就实现了，我们来测试一下
	
	```
	class MainActivity : AppCompatActivity() {
	    var nornalSum = 0
	    var singleSum = 0
	
	    override fun onCreate(savedInstanceState: Bundle?) {
	        super.onCreate(savedInstanceState)
	        setContentView(R.layout.activity_main)
			//每次button点击我们在两个方法中改变两个text的文字
	        button.setOnClickListener {
	            normal()
	            single()
	        }
	    }
		
		//普通的方法
	    fun normal(){
	        normal.text = "点击次数:${nornalSum++}次"
	    }
	
		//使用@SingleClick注解表示该方法防止抖动
	    @SingleClick
	    fun single(){
	        single.text = "防止多次点击:${singleSum++}次"
	    }
	}
	```
	
	运行效果
	
	![效果图.gif](https://upload-images.jianshu.io/upload_images/3347923-c04c555b2591fdc6.gif?imageMogr2/auto-orient/strip)
	
   这样我们的功能就实现了。

### 注意事项

1. 使用studio3.0以上版本。
`classpath 'com.hujiang.aspectjx:gradle-android-plugin-aspectjx:1.1.1'`这里要使用1.1.1最新版本

2. AspectJ使用的话可以开启混淆模式，但是注意被切点注解标识的类与方法一定要keep住，否则会没有效果。

   ```
   class Test{
	    @SingleClick
	    fun single(){
	        ...
	    }
	}
	
	//在proguard-rules.pro混淆配置文件中
	//这里我们需要keep住Test这个类
	-keep class com.guoyang.android.aoputils.Test {*;}
   ```
3. 如果运行项目时报错：
	```
		Error:Execution failed for task :app:transformClassesWithAspectTransformForDebug'.
		> org/aspectj/bridge/MessageHandler
	```
	
	我们需要检查`classpath 'org.aspectj:aspectjtools:1.8.13'`这句代码在build.gradle文件中有没有
	
---
好了，今天的文章就先写到这里。