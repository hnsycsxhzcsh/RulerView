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



如果有帮助到大家希望点下右上角Star，谢谢！

