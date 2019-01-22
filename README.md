# ClipPathLayout
[ ![Download](https://api.bintray.com/packages/dqh147258/ClipPathLayout/ClipPathLayout/images/download.svg?version=1.0.0) ](https://bintray.com/dqh147258/ClipPathLayout/ClipPathLayout/1.0.0/link)
[![Platform](https://img.shields.io/badge/platform-android-blue.svg)]()
[![License](https://img.shields.io/hexpm/l/plug.svg)](https://www.apache.org/licenses/LICENSE-2.0)

Android中实现不规则图形的布局

及由此扩展的转场动画布局


## 效果展示

### 不规则图形

将方形图片裁剪成圆形并且让圆形View的4角不接收触摸事件

![](https://resource-1255703580.cos.ap-shanghai.myqcloud.com/ClipPathLayout/circle.gif)

很多游戏都会有方向键,曾经我也做过一个小游戏,但是在做方向键的时候遇到一个问题,4个方向按钮的位置会有重叠,导致局部地方会发生误差.
当时没有特别好的解决办法,只能做自定义View,而自定义View特别麻烦,需要重写onTouchEvent和onDraw计算落点属于哪个方向,并增加点击效果.
简单的自定义View会丧失很多Android自带的一些特性,要支持这些特性又繁琐而复杂.
下面借助于CLipPathLayout用4个菱形按钮实现的方向控制键很好的解决了这个问题

![](https://resource-1255703580.cos.ap-shanghai.myqcloud.com/ClipPathLayout/control_button.gif)

对于遥控器的按键的模拟同样有上述问题,一般只能采用自定义View实现,较为繁琐.
以下是借助于ClipPathLayout实现的遥控器按钮,由于没有美工切图,比较丑,将就下吧

![](https://resource-1255703580.cos.ap-shanghai.myqcloud.com/ClipPathLayout/remote_controller.gif)

甚至我们可以将不连续的图形变成一个View,比如做一个阴阳鱼的按钮

![](https://resource-1255703580.cos.ap-shanghai.myqcloud.com/ClipPathLayout/yin_yang_fish.gif)


### 转场动画

两个View的场景切换效果,Android原生自带的场景切换效果大部分是由动画实现的平移,缩小,暗淡.
原生比较少带有那种PPT播放的切换效果,一些第三方库实现的效果一般是由在DecorView中添加一层View来实现较为和谐的切换,
沪江开心词场里使用的就是这种动画,这种动画很棒,但是也有一个小缺点,就是在切换的过程中,切换用的View和即将要切换的View没有什么关系.
借助于ClipPathLayout扩展的TransitionFrameLayout也可以实现较为和谐的切换效果,由于是示例,不写太复杂的场景,以下仅用两个TextView作为展示

![](https://resource-1255703580.cos.ap-shanghai.myqcloud.com/ClipPathLayout/view_transition.gif)

在浏览QQ空间和使用QQ浏览器的过程看到腾讯的广告切换效果也是很不错的,这里借助于TransitionFrameLayout也可以实现这种效果

![](https://resource-1255703580.cos.ap-shanghai.myqcloud.com/ClipPathLayout/scroll_transition_2.gif)

其实大部分的场景切换应该是用在Fragment中,这里也用TransitionFragmentContainer实现了Fragment的场景切换效果

![](https://resource-1255703580.cos.ap-shanghai.myqcloud.com/ClipPathLayout/fragment_transition_2.gif)

## 使用

### 添加依赖

库已经上传jcenter,Android Studio自带jcenter依赖,
如果没有添加,请在项目根build.gradle中添加jcenter Maven

```
buildscript {

    repositories {
        google()
        jcenter()
    }
    dependencies {
        classpath 'com.android.tools.build:gradle:3.1.0'

        // NOTE: Do not place your application dependencies here; they belong
        // in the individual module build.gradle files
    }
}

allprojects {
    repositories {
        google()
        jcenter()
    }
}
```

在app module中的build.gradle中添加依赖
```
implementation 'com.yxf:clippathlayout:1.0.0'
```

### 不规则图形布局的使用

当前实现了三个不规则图形的布局

- ClipPathFrameLayout
- ClipPathLinearLayout
- ClipPathRelativeLayout

如果有其他布局要求,请自定义,参见[自定义ClipPathLayout](#自定义clippathlayout)

ClipPathLayout是一个接口,以上布局都实现了ClipPathLayout接口,并且具备父类的功能.

要实现不规则图形,其实要操作的并不是父布局,而是子View.
我们需要给子View添加一些信息,这样父布局才知道应该如何去实现这个不规则图形.

这里以最简单的圆形View为例.

在一个实现了ClipPathLayout接口的ViewGroup(以ClipPathFrameLayout为例)中添加一个子View(ImageView).
```
<com.yxf.clippathlayout.impl.ClipPathFrameLayout xmlns:android="http://schemas.android.com/apk/res/android"
    android:id="@+id/clip_path_frame_layout"
    android:layout_width="match_parent"
    android:layout_height="match_parent">

    <ImageView
        android:id="@+id/image"
        android:layout_width="300dp"
        android:layout_height="300dp"
        android:layout_gravity="center"
        android:src="@mipmap/image" />

</com.yxf.clippathlayout.impl.ClipPathFrameLayout>

```

```
mImageView = mLayout.findViewById(R.id.image);
```

然后构建一个PathInfo对象

```
new PathInfo.Builder(new CirclePathGenerator(), mImageView)
    .setApplyFlag(mApplyFlag)
    .setClipType(mClipType)
    .create()
    .apply();
```

搞定!运行就可以看到一个圆形的View.

对其中使用到的参数和方法做下说明

#### PathInfo.Builder
PathInfo创建器,用于配置和生成PathInfo.

构造方法定义如下
```
        /**
         * @param generator Path生成器
         * @param view 实现了ClipPathLayout接口的ViewGroup的子View
         */
        public Builder(PathGenerator generator, View view) {

        }
```

#### PathGenerator

CirclePathGenerator是一个PathGenerator接口的实现类,用于生成圆形的Path.

PathGenerator定义如下
```
public interface PathGenerator {

    /**
     * @param old 以前使用过的Path,如果以前为null,则可能为null
     * @param view Path关联的子View对象
     * @param width 生成Path所限定的范围宽度,一般是子View宽度
     * @param height 生成Path所限定的范围高度,一般是子View高度
     * @return 返回一个Path对象,必须为闭合的Path,将用于裁剪子View
     *
     * 其中Path的范围即left : 0 , top : 0 , right : width , bottom : height
     */
    Path generatePath(Path old, View view, int width, int height);

}
```
PathGenerator是使用的核心,父布局将根据这个来对子View进行裁剪来实现不规则图形.

此库内置了4种Path生成器
- CirclePathGenerator(圆形Path生成器)
- OvalPathGenerator(椭圆Path生成器)
- RhombusPathGenerator(菱形Path生成器)
- OvalRingPathGenerator(椭圆环Path生成器)

如果有其他复杂的Path,可以自己实现PathGenerator,可以参考示例中的阴阳鱼Path的生成.

#### ApplyFlag

Path的应用标志,有如下几种

- APPLY_FLAG_DRAW_ONLY(只用于绘制)
- APPLY_FLAG_TOUCH_ONLY(只用于触摸事件)
- APPLY_FLAG_DRAW_AND_TOUCH(绘制和触摸事件一起应用)

默认不设置的话是APPLY_FLAG_DRAW_AND_TOUCH.

切换效果如下

![](https://resource-1255703580.cos.ap-shanghai.myqcloud.com/ClipPathLayout/select_apply_flag.gif)

#### ClipType

Path的裁剪模式,有如下两种

- CLIP_TYPE_IN(取Path内范围作为不规则图形子View)
- CLIP_TYPE_OUT(取Path外范围作为不规则图形子View)

默认不设置为CLIP_TYPE_IN.

切换效果如下

![](https://resource-1255703580.cos.ap-shanghai.myqcloud.com/ClipPathLayout/select_clip_mode.gif)


### 自定义ClipPathLayout

自定义一个ClipPathLayout很简单,首先选择一个ViewGroup,然后实现ClipPathLayout接口.

然后再在自定义的ViewGroup中创建一个ClipPathLayoutDelegate对象.

```
ClipPathLayoutDelegate mClipPathLayoutDelegate = new ClipPathLayoutDelegate(this);
```

并将所有ClipPathLayout接口的实现都委派给CLipPathLayoutDelegate去实现.

这里需要注意两点:

- 需要重写ViewGroup的drawChild,按如下实现即可

```
    @Override
    protected boolean drawChild(Canvas canvas, View child, long drawingTime) {
        beforeDrawChild(canvas, child, drawingTime);
        boolean result = super.drawChild(canvas, child, drawingTime);
        afterDrawChild(canvas, child, drawingTime);
        return result;
    }
```

- requestLayout方法也需要重写,这属于ViewGroup和ClipPathLayout共有的方法,这个方法会在父类的ViewGroup的构造方法中调用,在父类构造方法中,mClipPathLayoutDelegate还没有初始化,如果直接调用会报空指针,所以需要添加空判断.

```
    @Override
    public void requestLayout() {
        super.requestLayout();
        // the request layout method would be invoked in the constructor of super class
        if (mClipPathLayoutDelegate == null) {
            return;
        }
        mClipPathLayoutDelegate.requestLayout();
    }
```

这里将整个ClipPathFrameLayout源码贴出作为参考
```
public class ClipPathFrameLayout extends FrameLayout implements ClipPathLayout {

    ClipPathLayoutDelegate mClipPathLayoutDelegate = new ClipPathLayoutDelegate(this);

    public ClipPathFrameLayout(@NonNull Context context) {
        this(context, null);
    }

    public ClipPathFrameLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ClipPathFrameLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public boolean isTransformedTouchPointInView(float x, float y, View child, PointF outLocalPoint) {
        return mClipPathLayoutDelegate.isTransformedTouchPointInView(x, y, child, outLocalPoint);
    }

    @Override
    public void applyPathInfo(PathInfo info) {
        mClipPathLayoutDelegate.applyPathInfo(info);
    }

    @Override
    public void cancelPathInfo(View child) {
        mClipPathLayoutDelegate.cancelPathInfo(child);
    }

    @Override
    public void beforeDrawChild(Canvas canvas, View child, long drawingTime) {
        mClipPathLayoutDelegate.beforeDrawChild(canvas, child, drawingTime);
    }

    @Override
    public void afterDrawChild(Canvas canvas, View child, long drawingTime) {
        mClipPathLayoutDelegate.afterDrawChild(canvas, child, drawingTime);
    }

    //the drawChild method is not belong to ClipPathLayout ,
    //but you should rewrite it without changing the return value of the method
    @Override
    protected boolean drawChild(Canvas canvas, View child, long drawingTime) {
        beforeDrawChild(canvas, child, drawingTime);
        boolean result = super.drawChild(canvas, child, drawingTime);
        afterDrawChild(canvas, child, drawingTime);
        return result;
    }

    //do not forget to rewrite the method
    @Override
    public void requestLayout() {
        super.requestLayout();
        // the request layout method would be invoked in the constructor of super class
        if (mClipPathLayoutDelegate == null) {
            return;
        }
        mClipPathLayoutDelegate.requestLayout();
    }

    @Override
    public void notifyPathChanged(View child) {
        mClipPathLayoutDelegate.notifyPathChanged(child);
    }

    @Override
    public void notifyAllPathChanged() {
        mClipPathLayoutDelegate.notifyAllPathChanged();
    }
}
```


### 转场动画布局的使用

转场动画布局这里做了两个,一个用于普通的View(TransitionFrameLayout),一个是针对Fragment的容器(TransitionFragmentContainer).

#### TransitionFrameLayout

这个布局继承于FrameLayout,用于两个View的场景切换.

要求两个子View大小宽高需要一致,位置也一致.一般不做什么设置的话,FrameLayout就是如此的.

**这个ViewGroup限定只显示一个View**,如果在xml中添加了多个View,**只有最后一个View会显示出来**.

如果需要添加一个View或者将其中隐藏的View显示出来请调用TransitionFrameLayout的switchView方法,**不要直接调用addView或者setVisibility**,可能会造成不太友好的画面.

##### 使用

以两个TextView的切换为例

```
<com.yxf.clippathlayout.transition.TransitionFrameLayout xmlns:android="http://schemas.android.com/apk/res/android"
    android:layout_width="match_parent"
    android:layout_height="match_parent">

    <TextView
        android:id="@+id/blue_view"
        android:layout_width="match_parent"
        android:layout_height="match_parent"
        android:background="#880000ff"
        android:gravity="center"
        android:text="蓝色界面"
        android:textSize="30sp" />

    <TextView
        android:id="@+id/green_view"
        android:layout_width="match_parent"
        android:layout_height="match_parent"
        android:background="#8800ff00"
        android:gravity="center"
        android:text="绿色界面"
        android:textSize="30sp" />

</com.yxf.clippathlayout.transition.TransitionFrameLayout>
```

```
mLayout = (TransitionFrameLayout) inflater.inflate(R.layout.fragment_view_transition, null);
```

现在绿色界面在上面显示,蓝色隐藏.

如果需要将蓝色界面切换出来,可以调用如下代码.

```
TransitionAdapter adapter = mLayout.switchView(mBlueView);
```
switchView有两个方法
```
    @Override
    public TransitionAdapter switchView(View view) {
        return switchView(view, false);
    }

    /**
     * if you want add a view , just invoke switchView directly ,
     * do not invoke addView , it may cause some problem .
     *
     * @param view
     * @return
     */
    @Override
    public TransitionAdapter switchView(final View view, boolean reverse) {
        //.................
    }
```
reverse为false表示动画扩张,为true表示收缩.

在switchView后获得一个adapter对象,此时蓝色界面还没有展示出来.

可以通过adapter获得一个ValueAnimator对象或者一个Controller对象.
可以直接调用
```
adapter.animate();
```
来启动场景切换动画效果.

也可以通过
```
adapter.getAnimator();
```
获得一个属性动画,自己控制动画过程.

还可以获得一个Controller对象
```
mController = adapter.getController();
```
然后通过
```
mController.setProgress
```
来控制动画的实现进度.当到达1时(进度范围0~1),即动画结束时,调用
```
adapter.finish();
```
来通知转场结束了.

具体效果请查看之前的效果展示.

#### TransitionFragmentContainer

这个布局作为Fragment的容器来实现Fragment的场景切换效果.

直接像FrameLayout作为Fragment容器做动态添加删除即可.

具体效果移步效果展示

#### TransitionAdapter

这个类是一个Path适配器,构造方法如下

```
public TransitionAdapter(TransitionPathGenerator generator)
```

其中TransitionPathGenerator是用于转场动画的Path生成器接口,此接口继承于PathGenerator,定义如下

```
public interface TransitionPathGenerator extends PathGenerator {

    /*
     * 返回path在viewRange中包含的最大的和viewRange相似的的矩形区域
     */
    Rect maxContainSimilarRange(Rect viewRange);

}
```
由于当前没有很好的办法来判断Path内所能放下的最大矩形区域,所以这里目前需要生成器自己提供一个区域.

为了避免频繁垃圾回收,实现TransitionPathGenerator时,尽量直接用传进来的viewRange返回.

当前内置了四种转场动画的Path生成器
- CircleTransitionPathGenerator(圆形)
- OvalTransitionPathGenerator(椭圆)
- RhombusTransitionPathGenerator(菱形)
- RandomTransitionPathGenerator(随机)

默认使用圆形.

回到TransitionAdapter上

以上两种转场动画容器都有setAdapter方法,可以替换掉默认的TransitionAdapter.

从TransitionFrameLayout.switchView中获得Adapter后,还可以通过setPathCenter来控制Path的扩张和收缩中心.