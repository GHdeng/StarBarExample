# StarBarExample
	在开发电商项目中经常都会遇到一些星级评分控件的需求，有需求就必然有开发。废话不多说，没图没真相，上图：
![image](https://raw.githubusercontent.com/GHdeng/StarBarExample/master/resources/StarBarExample.gif)

	先吐槽几句，使用官方提供SatingBar各种蛋疼，图片无法自定义，适配也是麻烦的事情。

###确定需求：
* 可以控制子item之间的边距
* 自定义选中图片和未选中图片
* 摆放纵向或者横向
* 可选择选中数量

###基本绘制流程：

- 1 自定义属性
```html
<!--星星控件属性-->
    <declare-styleable name="StarBarView">
        <!--设置星星间的间隔-->
        <attr name="space_width" format="dimension" />
        <!--星星间宽度-->
        <attr name="star_width" format="dimension" />
        <!--星星间高度-->
        <attr name="star_height" format="dimension" />
        <!--最大数量-->
        <attr name="star_max" format="integer" />
        <!--选中数量-->
        <attr name="star_rating" format="float" />
        <!--未选中图片-->
        <attr name="star_hollow" format="reference" />
        <!--选中图片-->
        <attr name="star_solid" format="reference" />
        <!--是否可以滑动改变选中数量-->
        <attr name="star_isIndicator" format="boolean" />
        <!--排列方向-->
        <attr name="star_orientation" format="enum">
            <enum name="vertical" value="1" />
            <enum name="horizontal" value="0" />
        </attr>
    </declare-styleable>
```
- 2 构造函数中获取自定义属性值
```java
	//星星水平排列
    public static final int HORIZONTAL = 0;
    //星星垂直排列
    public static final int VERTICAL = 1;
    //实心图片
    private Bitmap mSolidBitmap;
    //空心图片
    private Bitmap mHollowBitmap;
    private Context context;
    //最大的数量
    private int starMaxNumber;
    private float starRating;
    private Paint paint;
    private int mSpaceWidth;//星星间隔
    private int mStarWidth;//星星宽度
    private int mStarHeight;//星星高度
    private boolean isIndicator;//是否是一个指示器（用户无法进行更改）
    private int mOrientation;

    public StarBarView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public StarBarView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        paint = new Paint();
        this.context = context;
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.StarBarView, defStyle, 0);
        mSpaceWidth = a.getDimensionPixelSize(R.styleable.StarBarView_space_width, 0);
        mStarWidth = a.getDimensionPixelSize(R.styleable.StarBarView_star_width, 0);
        mStarHeight = a.getDimensionPixelSize(R.styleable.StarBarView_star_height, 0);
        starMaxNumber = a.getInt(R.styleable.StarBarView_star_max, 0);
        starRating = a.getFloat(R.styleable.StarBarView_star_rating, 0);
        mSolidBitmap = getZoomBitmap(BitmapFactory.decodeResource(context.getResources(), a.getResourceId(R.styleable.StarBarView_star_solid, 0)));
        mHollowBitmap = getZoomBitmap(BitmapFactory.decodeResource(context.getResources(), a.getResourceId(R.styleable.StarBarView_star_hollow, 0)));
        mOrientation = a.getInt(R.styleable.StarBarView_star_orientation, HORIZONTAL);
        isIndicator = a.getBoolean(R.styleable.StarBarView_star_isIndicator, false);
        a.recycle();
    }
```
- 3 获取图片bitmap,设置宽高
```java
/**
     * 获取缩放图片
     *
     * @param bitmap
     * @return
     */
    public Bitmap getZoomBitmap(Bitmap bitmap) {
        if (mStarWidth == 0 || mStarHeight == 0) {
            return bitmap;
        }
        // 获得图片的宽高
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();

        // 设置想要的大小
        int newWidth = mStarWidth;
        int newHeight = mStarHeight;
        // 计算缩放比例
        float scaleWidth = ((float) newWidth) / width;
        float scaleHeight = ((float) newHeight) / height;
        // 取得想要缩放的matrix参数
        Matrix matrix = new Matrix();
        matrix.postScale(scaleWidth, scaleHeight);
        // 得到新的图片
        Bitmap newbm = Bitmap.createBitmap(bitmap, 0, 0, width, height, matrix, true);
        return newbm;
    }
```
- 4 onMeasure函数测量子控件大小，然后设置当前控件大小
```java
@Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if (mOrientation == HORIZONTAL) {
            //判断是横向还是纵向，测量长度
            setMeasuredDimension(measureLong(widthMeasureSpec), measureShort(heightMeasureSpec));
        } else {
            setMeasuredDimension(measureShort(widthMeasureSpec), measureLong(heightMeasureSpec));
        }
    }

    private int measureLong(int measureSpec) {
        int result;
        int specMode = MeasureSpec.getMode(measureSpec);
        int specSize = MeasureSpec.getSize(measureSpec);

        if ((specMode == MeasureSpec.EXACTLY)) {
            result = specSize;
        } else {
            result = (int) (getPaddingLeft() + getPaddingRight() + (mSpaceWidth + mStarWidth) * (starMaxNumber));
            if (specMode == MeasureSpec.AT_MOST) {
                result = Math.min(result, specSize);
            }
        }
        return result;
    }

    private int measureShort(int measureSpec) {
        int result;
        int specMode = MeasureSpec.getMode(measureSpec);
        int specSize = MeasureSpec.getSize(measureSpec);

        if (specMode == MeasureSpec.EXACTLY) {
            result = specSize;
        } else {
            result = (int) (mStarHeight + getPaddingTop() + getPaddingBottom());
            if (specMode == MeasureSpec.AT_MOST) {
                result = Math.min(result, specSize);
            }
        }
        return result;
    }
```
- 5 确定好数据后开始描绘bitmap到Canvas上
```java
@Override
    protected void onDraw(Canvas canvas) {
        if (mHollowBitmap == null || mSolidBitmap == null) {
            return;
        }
        //绘制实心进度
        int solidStarNum = (int) starRating;
        //绘制实心的起点位置
        int solidStartPoint = 0;
        if (mOrientation == HORIZONTAL)
            for (int i = 1; i <= solidStarNum; i++) {
                canvas.drawBitmap(mSolidBitmap, solidStartPoint, 0, paint);
                solidStartPoint = solidStartPoint + mSpaceWidth + mSolidBitmap.getWidth();
            }
        else
            for (int i = 1; i <= solidStarNum; i++) {
                canvas.drawBitmap(mSolidBitmap, 0, solidStartPoint, paint);
                solidStartPoint = solidStartPoint + mSpaceWidth + mSolidBitmap.getHeight();
            }
        //虚心开始位置
        int hollowStartPoint = solidStartPoint;
        //多出的实心部分起点
        int extraSolidStarPoint = hollowStartPoint;
        //虚心数量
        int hollowStarNum = starMaxNumber - solidStarNum;
        if (mOrientation == HORIZONTAL)
            for (int j = 1; j <= hollowStarNum; j++) {
                canvas.drawBitmap(mHollowBitmap, hollowStartPoint, 0, paint);
                hollowStartPoint = hollowStartPoint + mSpaceWidth + mHollowBitmap.getWidth();
            }
        else
            for (int j = 1; j <= hollowStarNum; j++) {
                canvas.drawBitmap(mHollowBitmap, 0, hollowStartPoint, paint);
                hollowStartPoint = hollowStartPoint + mSpaceWidth + mHollowBitmap.getWidth();
            }
        //多出的实心长度
        int extraSolidLength = (int) ((starRating - solidStarNum) * mHollowBitmap.getWidth());
        Rect rectSrc = new Rect(0, 0, extraSolidLength, mHollowBitmap.getHeight());
        Rect dstF = new Rect(extraSolidStarPoint, 0, extraSolidStarPoint + extraSolidLength, mHollowBitmap.getHeight());
        canvas.drawBitmap(mSolidBitmap, rectSrc, dstF, paint);
    }
```
- 6 最后通过onTouchEvent方法去监听事件
```java
@Override
    public boolean onTouchEvent(MotionEvent event) {
        if (!isIndicator) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    if (mOrientation == HORIZONTAL) {
                        float TotalWidth = starMaxNumber * (mStarWidth + mSpaceWidth);
                        if (event.getX() <= TotalWidth) {
                            float newStarRating = (int) event.getX() / (mStarWidth + mSpaceWidth) + 1;
                            setStarRating(newStarRating);
                        }
                    }else{
                        float TotalHeight = starMaxNumber * (mStarHeight + mSpaceWidth);
                        if (event.getY() <= TotalHeight) {
                            float newStarRating = (int) event.getY() / (mStarHeight + mSpaceWidth) + 1;
                            setStarRating(newStarRating);
                        }
                    }
                    break;
                case MotionEvent.ACTION_MOVE:
//                    float starTotalWidth = starMaxNumber * (mStarWidth + mSpaceWidth);
//                    if (event.getX() <= starTotalWidth) {
//                        float newStarRating = (int) event.getX() / (mStarWidth + mSpaceWidth) + 1;
//                    setStarRating(newStarRating);
//                    }
                    break;
                case MotionEvent.ACTION_UP:
                    break;
                case MotionEvent.ACTION_CANCEL:
                    break;
            }
        }
        return super.onTouchEvent(event);
    }
```

###如何使用：

- xml布局
```html
<com.caption.starbarexample.widget.StarBarView
        android:id="@+id/sbv_starbar"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:layout_centerInParent="true"
        android:layout_margin="10dp"
        app:space_width="1dp"
        app:star_height="25dp"
        app:star_hollow="@mipmap/ic_star_yellow_normal"
        app:star_isIndicator="false"
        app:star_max="5"
        app:star_orientation="horizontal"
        app:star_rating="2"
        app:star_solid="@mipmap/ic_star_yellow_selected"
        app:star_width="25dp" />
```
- 代码添加
```java
	//拿到当前星星数量
    mStarbar.getStarRating();
```

# License
	Copyright (c) 2016 GHdeng
	Licensed under the Apache License, Version 2.0 (the "License");
	you may not use this file except in compliance with the License.
	You may obtain a copy of the License at
	
	http://www.apache.org/licenses/LICENSE-2.0

	Unless required by applicable law or agreed to in writing, software
	distributed under the License is distributed on an "AS IS" BASIS,
	WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
	See the License for the specific language governing permissions and
	limitations under the License.
