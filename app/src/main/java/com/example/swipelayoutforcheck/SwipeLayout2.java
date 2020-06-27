package com.example.swipelayoutforcheck;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;

import androidx.core.view.MotionEventCompat;
import androidx.core.view.ViewCompat;
import androidx.customview.widget.ViewDragHelper;


/**
 * 往右拖动验证滑块的控件，(根据左滑删除控件改写而来。用法见同级目录下的SWIPE_LAYOUT_README.md文件）
 *
 * @author Huangming
 * @date 2020/6/27
 */
public class SwipeLayout2 extends LinearLayout {

    private ViewDragHelper viewDragHelper;
    private View contentView;
    private View actionView;
    private int dragDistance;
    private final double AUTO_OPEN_SPEED_LIMIT = 800.0;
    private int draggedX;

    // private boolean isOpened = false;

    /** 是否已禁用(即不可滑动) */
    private boolean isDisabled = false;

    private int mLastXIntercept;

    private OnSwipeLayoutOpenedListener listener;

    public SwipeLayout2(Context context) {
        this(context, null);
    }

    public SwipeLayout2(Context context, AttributeSet attrs) {
        this(context, attrs, -1);
    }

    public SwipeLayout2(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        viewDragHelper = ViewDragHelper.create(this, new DragHelperCallback());
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        contentView = getChildAt(1);
        actionView = getChildAt(0);
        actionView.setVisibility(VISIBLE);
        dragDistance = actionView.getWidth();
        // XbdLog.d("dragDistance1 = %s", dragDistance);

        actionView.post(() -> {
            dragDistance = actionView.getWidth();
            // XbdLog.d("dragDistance2 = %s", dragDistance);
            LayoutParams lp = (LayoutParams) actionView.getLayoutParams();
            lp.leftMargin = -dragDistance;
        });

        // android 10 初始状态下滑块会停在右边
        postDelayed(()->{
            requestLayout();
        }, 30);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    private class DragHelperCallback extends ViewDragHelper.Callback {

        @Override
        public boolean tryCaptureView(View view, int i) {
            //确定当前子view是否可拖动
            Log.d("SwipeLayout2","tryCaptureView");
            return view == contentView;
        }


        /**
         * 控制被拖动的view在水平方向的移动范围
         * @param child 被拖动的view
         * @param left child的left坐标
         * @param dx
         * @return
         */
        @Override
        public int clampViewPositionHorizontal(View child, int left, int dx) {
            //        代表你将要移动到的位置的坐标,返回值就是最终确定的移动的位置,
            //               判断如果这个坐标在layout之内,那我们就返回这个坐标值，
            //               不能让他超出这个范围, 除此之外就是如果你的layout设置了padding的话，
            //               让子view的活动范围在padding之内的
            Log.d("SwipeLayout2","clampViewPositionHorizontal");

            if (child == contentView) {
                final int leftBound = getPaddingLeft();
                final int maxLeftBound = leftBound + dragDistance;
                final int newLeft = Math.min(Math.max(leftBound, left), maxLeftBound);
                return newLeft;
            } else {
                // 不会来到这个分支，因为child是被拖动的那个view
                return 0;
            }
        }

        /**
         * view被拖动时调用此方法, 调用clampViewPositionHorizontal之后会调用这个方法
         * @param changedView
         * @param left
         * @param top
         * @param dx
         * @param dy
         */
        @Override
        public void onViewPositionChanged(View changedView, int left, int top, int dx, int dy) {
            draggedX = left;
            // XbdLog.d("onViewPositionChanged  left=%s ", left);

            Log.d("SwipeLayout2","onViewPositionChanged");
            if (changedView == contentView) {
                // 这里通过改变被隐藏的view的left/right坐标来实现将被隐藏的view拖出来
                actionView.offsetLeftAndRight(dx);
            } else {
                // XbdLog.d("changedView == 被隐藏的view");
                // 不会进来这个分支
                contentView.offsetLeftAndRight(dx);
            }
            if (actionView.getVisibility() == View.GONE) {
                actionView.setVisibility(View.VISIBLE);
            }
            // 重绘
            invalidate();
        }

        @Override
        public int getViewHorizontalDragRange(View child) {
            // 可拖动的距离
            return dragDistance;
        }

        /**
         * 拖动的view被释放时调用此方法
         * @param releasedChild
         * @param xvel
         * @param yvel
         */
        @Override
        public void onViewReleased(View releasedChild, float xvel, float yvel) {
            super.onViewReleased(releasedChild, xvel, yvel);
            boolean settleToOpen;
            settleToOpen = draggedX > dragDistance * 0.99;
            final int settleDestX = settleToOpen ? dragDistance : 0;
            viewDragHelper.smoothSlideViewTo(contentView, settleDestX, 0);
            ViewCompat.postInvalidateOnAnimation(SwipeLayout2.this);
            if (settleToOpen && listener != null) {
                listener.onSwipeLayoutOpened();
            }
            // 已经打开的情况下不允许滑动
            isDisabled = settleToOpen;
        }
    }


    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {

        viewDragHelper.shouldInterceptTouchEvent(ev);

        boolean intercepted = false;
        int action = MotionEventCompat.getActionMasked(ev);
        int x = (int) ev.getX();

        switch (action) {
            case MotionEvent.ACTION_DOWN:
                mLastXIntercept = x;
                onTouchEvent(ev);
                intercepted = false;
                break;
            case MotionEvent.ACTION_MOVE:
                int deltaX = x - mLastXIntercept;
                intercepted = Math.abs(deltaX) > 10;
                break;
            case MotionEvent.ACTION_UP:
                intercepted = false;
                break;
            default:
                break;
        }
        return intercepted;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        viewDragHelper.processTouchEvent(event);
        return !isDisabled;
    }

    @Override
    public void computeScroll() {
        super.computeScroll();
        if (viewDragHelper.continueSettling(true)) {
            ViewCompat.postInvalidateOnAnimation(this);
        }
    }

    /**
     * 关闭拖出的内容
     *
     * @param
     * @return
     * @author Huangming
     * @date 2016/10/31
     * @modified [describe][editor][date]
     */
    public void close() {
        viewDragHelper.smoothSlideViewTo(contentView, 0, 0);
        ViewCompat.postInvalidateOnAnimation(SwipeLayout2.this);
        isDisabled = false;
    }

    public boolean isDisabled() {
        return isDisabled;
    }

    public void setDisabled(boolean disabled) {
        isDisabled = disabled;
    }

    /**
     * 设置监听器
     *
     * @param
     * @return
     * @author Huangming
     * @date 2016/10/31
     * @modified [describe][editor][date]
     */
    public void setOnSwipelayoutOpenedListener(OnSwipeLayoutOpenedListener listener) {
        this.listener = listener;
    }

    /**
     * 拖出完成监听器
     *
     * @author Huangming
     * @date 2016/10/31
     * @modified [describe][editor][date]
     */
    public interface OnSwipeLayoutOpenedListener {
        void onSwipeLayoutOpened();
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        if (!isDisabled) {
            // 呼出键盘或关闭键盘的时候会导致已滑到右边后又重置到左边
            super.onLayout(changed, l, t, r, b);
        }
    }

}
