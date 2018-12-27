# RulerView
自定义控件，身高、体重等标尺控件

<a href="https://github.com/hnsycsxhzcsh/RulerView/blob/master/myres/rulerview.apk">Download Apk</a>

效果图

<img src="https://github.com/hnsycsxhzcsh/RulerView/blob/master/myres/rulerview.gif" width="300" height="612">


步骤1.将JitPack存储库添加到构建文件中

项目的根build.gradle中添加以下代码：

	allprojects {
		repositories {
			...
			maven { url 'https://jitpack.io' }
		}
	}

步骤2.build.gradle添加依赖项

	dependencies {
	        implementation 'com.github.hnsycsxhzcsh:RulerView:v1.1'
	}

步骤3. 布局中引用控件

  	<com.rulerlibrary.RulerView
        	android:id="@+id/sizeview_kg"
        	android:layout_width="match_parent"
        	android:layout_height="wrap_content"
        	app:currentValue="76"
        	app:maxValue="150"
        	app:minValue="45"
        	app:titleName="体重"
        	app:unitName="kg" />
		
步骤4. activity中添加监听
  
  	mSizeViewKg = findViewById(R.id.sizeview_kg);
	//设置初始化值
        mSizeViewKg.setCurrentValue(89);

	mSizeViewKg.setOnValueChangeListener(new SizeViewValueChangeListener() {
            @Override
            public void onValueChange(int value) {
		//获取现在的值
            }
        });

如果有帮助到大家希望点下右上角Star，谢谢！

