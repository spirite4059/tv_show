package com.vego.player;

import com.vego.player.R;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class Sample extends Activity {
	// 【转发】【桜の明月】【图片】萌图精选~喜欢就下吧！

	@Override
	protected void onCreate(Bundle pSavedInstanceState) {
		super.onCreate(pSavedInstanceState);

		setContentView(R.layout.sample);

		// final TextView videoIdTextView = (TextView)
		// findViewById(R.id.youtubeIdText);
		final Button viewVideoButton = (Button) findViewById(R.id.viewVideoButton);

		viewVideoButton.setOnClickListener(new View.OnClickListener() {

			public void onClick(View pV) {

				// String videoId = videoIdTextView.getText().toString();
				// tzgFuUu0oxg
				String videoId = "mknj-e9Bj9Q";

				// String videoId = "PLocfYszBfq7mAn-MZ_f1Ya9SeVpm-z4C_";

				if (videoId == null || videoId.trim().equals("")) {
					return;
				}

				Intent lVideoIntent = new Intent(null, Uri.parse("ytv://" + videoId), Sample.this,
						OpenYouTubePlayerActivity.class);
				startActivity(lVideoIntent);

			}
		});

	}

}
