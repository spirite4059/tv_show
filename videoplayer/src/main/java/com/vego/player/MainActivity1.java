package com.vego.player;

import com.vego.player.R;

import android.app.Activity;
import android.os.Bundle;

public class MainActivity1 extends Activity {
	MeasureVideoView measureVideoView;

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main1);
		measureVideoView = (MeasureVideoView) findViewById(R.id.video);

//		measureVideoView.setVideoURI(Uri.parse("http://210.14.151.99/movie/congcongnanian.mp4"));
		
//		measureVideoView.setVideoUrl("http://210.14.151.99/movie/congcongnanian.mp4");
//		GridView gridView = (GridView) findViewById(R.id.video);
		
		measureVideoView.setVideoUrl("http://210.14.158.48/hls/testp2p.m3u8");
		
		
	}

}
