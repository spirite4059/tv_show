/*
 * FFmpegMediaMetadataRetriever: A unified interface for retrieving frame 
 * and meta data from an input media file.
 *
 * Copyright 2015 William Seemann
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.gochinatv.ad.screenshot;

import android.content.AsyncTaskLoader;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;

import com.gochinatv.ad.tools.LogCat;
import com.gochinatv.ad.tools.ScreenShotUtils;

import java.io.File;

import wseemann.media.FFmpegMediaMetadataRetriever;

/**
 * A custom Loader that loads all of the installed applications.
 */
public class MetadataLoader extends AsyncTaskLoader<Metadata> {
    // TODO fix this
    //final InterestingConfigChanges mLastConfig = new InterestingConfigChanges();

    private String mUri;
    private Metadata mMetadata;
    private long currentPosition;


    public MetadataLoader(Context context, Bundle args) {
        super(context);
        mUri = args.getString("uri");
        currentPosition = args.getLong("currentPosition");
    }

    /**
     * This is where the bulk of our work is done.  This function is
     * called in a background thread and should generate a new set of
     * data to be published by the loader.
     */
    @Override
    public Metadata loadInBackground() {
        // Retrieve all metadata.
        if (mUri == null) {
            return null;
        }
        LogCat.e("screenShot", "loadInBackground。。。。。。。。。。");
        Bitmap b = null;
        FFmpegMediaMetadataRetriever fmmr = new FFmpegMediaMetadataRetriever();
        try {

            fmmr.setDataSource(mUri);


            b = fmmr.getFrameAtTime(currentPosition * 1000, FFmpegMediaMetadataRetriever.OPTION_CLOSEST_SYNC);
            LogCat.e("screenShot", "1。。。。。。。。。。");
            File file = ScreenShotUtils.initScreenShotFile();
            LogCat.e("screenShot", "2。。。。。。。。。。");
            ScreenShotUtils.createScreenShotFile(b, file);
            LogCat.e("screenShot", "3。。。。。。。。。。");


            if (b != null) {
                LogCat.e("screenShot", "截图成功。。。。。。。。。。");
            } else {
                Log.e(MetadataLoader.class.getName(), "Failed to extract frame");
            }
        } catch (IllegalArgumentException ex) {
            ex.printStackTrace();
        } finally {
            fmmr.release();

            if (b != null) {
                b.recycle();
            }
        }


        // Sort the list.
        //Collections.sort(entries, ALPHA_COMPARATOR);

        // Done!
        return mMetadata;
    }

    /**
     * Called when there is new data to deliver to the client.  The
     * super class will take care of delivering it; the implementation
     * here just adds a little more logic.
     */
    @Override
    public void deliverResult(Metadata metadata) {

        if (isReset()) {
            // An async query came in while the loader is stopped.  We
            // don't need the result.
            if (metadata != null) {
                onReleaseResources(metadata);
            }
        }
        mMetadata = metadata;

        if (isStarted()) {
            // If the Loader is currently started, we can immediately
            // deliver its results.
            super.deliverResult(metadata);
        }

        // At this point we can release the resources associated with
        // 'oldMetadata' if needed; now that the new result is delivered we
        // mMetadata that it is no longer in use.
        if (mMetadata != null) {
            onReleaseResources(mMetadata);
        }
    }

    /**
     * Handles a request to start the Loader.
     */
    @Override
    protected void onStartLoading() {
        if (mMetadata != null) {
            // If we currently have a result available, deliver it
            // immediately.
            deliverResult(mMetadata);
        }

        // TODO fix this
        if (takeContentChanged() || mMetadata == null) { //|| configChange) {
            // If the data has changed since the last time it was loaded
            // or is not currently available, start a load.
            forceLoad();
        }
    }

    /**
     * Handles a request to stop the Loader.
     */
    @Override
    protected void onStopLoading() {
        // Attempt to cancel the current load task if possible.
        cancelLoad();
    }

    /**
     * Handles a request to cancel a load.
     */
    @Override
    public void onCanceled(Metadata metadata) {
        super.onCanceled(metadata);

        // At this point we can release the resources associated with 'metadata'
        // if needed.
        onReleaseResources(metadata);
    }

    /**
     * Handles a request to completely reset the Loader.
     */
    @Override
    protected void onReset() {
        super.onReset();

        // Ensure the loader is stopped
        onStopLoading();

        // At this point we can release the resources associated with 'metadata'
        // if needed.
        if (mMetadata != null) {
            onReleaseResources(mMetadata);
            mMetadata = null;
        }
    }

    /**
     * Helper function to take care of releasing resources associated
     * with an actively loaded data set.
     */
    protected void onReleaseResources(Metadata metadata) {
        // For a simple List<> there is nothing to do.  For something
        // like a Cursor, we would close it here.
    }
}