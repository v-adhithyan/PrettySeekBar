<h1 align="center">PrettySeekBar</h1>
<h4 align="center"r>Android library</h4>

<p align="center">
<a target="_blank" href="https://jitpack.io/#v-adhithyan/PrettySeekBar"><img src="https://jitpack.io/v/v-adhithyan/PrettySeekBar.svg"/></a>
 </p>



Beautiful circular seekbar library for android, inspired by analog clock. It can be used for stopwatches and audio players. Free and Open source.

# Demo

# Usage
This library is currently available only via jitpack. To add this library to your project, do the following changes: <br>

<b>Step 1:</b> Add it in your root build.gradle at the end of repositories.
```
allprojects {
	 repositories {
	   ...
    maven { url "https://jitpack.io" }
  }
}
```
<br>

<b>Step 2:</b> Add the dependency.
```
dependencies{
 ...
 compile 'com.github.v-adhithyan:PrettySeekBar:v1.0'
}
```
<br>

<b>Step 3:</b> Sync project with gradle files. <br>

<b>Step 4:</b> In your layout file,add a xml namespace (<xmlns:pretty>) for the PrettySeekBar within the top parent. <br>
  ```
xmlns:pretty="http://schemas.android.com/apk/res-auto"
  ```
  <br>
  
  Now add the following code:
  ```
<com.avtechlabs.prettyseekbar.PrettySeekBar
     android:layout_width="match_parent"
     android:layout_height="match_parent"
     pretty:outerCircleFillColor="replace with the color you want, default is black"
     pretty:innerCircleFillColor="replace with the color you want, default is white"
/>
  ```
Voila, PrettySeekBar is now added to your project.

##Operations

You can avail the following functions of PrettySeeBar activity java file.

* Set maxProgress in seconds. (Default is 100)
```
prettySeekBar.setMaxProgress(seconds);
```

* The clock starts zooming in and out, once added to layout. To animate clock hand, call the following:
```
prettySeekBar.makeProgress();
```

* To pause moving clock hand, call:
```
prettySeekBar.pauseProgress();
```

* Implement an listener and override onProgressChanged method to get the current progress seeked by user.
```
prettySeekBar.setOnPrettySeekBarChangeListener(new PrettySeekBar.OnPrettySeekBarChangeListener() {
        @Override
        public void onProgressChanged(PrettySeekBar prettySeekBar, int progress, boolean touched) {
            //your logic
        }

        @Override
        public void onStartTrackingTouch(PrettySeekBar seekBar) {
		//currently the logic added here is not supported.
        }

        @Override
        public void onStopTrackingTouch(PrettySeekBar seekBar) {
		//currently the logic added here is not supported.
        }
});
```

# License

This project is released under MIT license. See [license file]() for more info.
