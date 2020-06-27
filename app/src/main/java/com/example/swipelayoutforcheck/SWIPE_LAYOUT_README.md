该包存放自定义的view

SwipeLayout(左滑删除控件)用法：

1、只需要在布局文件中引用SwipeLayout
2、SwipeLayout节点下只能有两个子容器，第一个是正常显示的内容，第二个是拖出来的内容。

示例：
    <com.example.huangming.swipelayout.SwipeLayout
        android:id="@+id/swipeLayout"
        android:layout_width="match_parent"
        android:layout_height="wrap_content">

        <!--这是第一个子容器，显示拖动之前的内容，可以是一行文字，或其它更多内容-->
        <LinearLayout
            android:id="@+id/firstChild"
            android:layout_width="match_parent"
            android:layout_height="50dp"
            android:background="#ffffff">

            <TextView
                android:id="@+id/tvContent"
                android:layout_width="wrap_content"
                android:layout_height="wrap_content"
                android:layout_gravity="center_vertical"
                android:paddingLeft="20dp"
                android:text="请把我滑到左边"
                android:textSize="20sp"/>
        </LinearLayout>

        <!--这是第二个子容器，显示要拖出的内容，可以是一个删除按钮，或其它更多内容-->
        <!--注意！第二个子容器的宽度必须显式指定，否则无法拖出-->
        <RelativeLayout
            android:id="@+id/secondChild"
            android:layout_width="@dimen/width_two_child"
            android:layout_height="50dp">

            <TextView
                android:id="@+id/tvDelete"
                android:layout_width="60dp"
                android:layout_height="50dp"
                android:background="#ff0000"
                android:gravity="center"
                android:text="删除"
                android:textColor="#ffffff"/>

            <TextView
                android:id="@+id/tvEdit"
                android:layout_width="60dp"
                android:layout_height="50dp"
                android:layout_toRightOf="@id/tvDelete"
                android:background="#ff9c00"
                android:gravity="center"
                android:text="编辑"
                android:textColor="#ffffff"/>
        </RelativeLayout>
    </com.example.huangming.swipelayout.SwipeLayout>
