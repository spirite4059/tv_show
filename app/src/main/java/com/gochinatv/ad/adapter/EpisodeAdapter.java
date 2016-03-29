package com.gochinatv.ad.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.gochinatv.ad.R;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.okhtttp.response.AdImgResponse;

import java.util.ArrayList;

/**
 * Created by fq_mbp on 16/1/25.
 */
public class EpisodeAdapter extends RecyclerView.Adapter<EpisodeAdapter.MyViewHolder> {

    private final LayoutInflater mLayoutInflater;
    private ArrayList<AdImgResponse> imgResponses;
    private DisplayImageOptions options;
    private ImageLoader imageLoader;

    public EpisodeAdapter(Context context, ArrayList<AdImgResponse> imgResponses) {
        mLayoutInflater = LayoutInflater.from(context);
        this.imgResponses = imgResponses;
//        imageLoader = ImageLoader.getInstance();
//        imageLoader.init(MyApplication.initImageLoader(context).build());
//        options = new DisplayImageOptions.Builder().cacheInMemory(false).cacheOnDisc(true)
//                .imageScaleType(ImageScaleType.IN_SAMPLE_POWER_OF_2)
//                .bitmapConfig(Bitmap.Config.RGB_565)
//                .displayer(new FadeInBitmapDisplayer(800, true, false, false)).build();
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        MyViewHolder holder = new MyViewHolder(mLayoutInflater.inflate(R.layout.itme_ad_three, parent, false));
        return holder;
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        int curPosition = position % imgResponses.size();
        AdImgResponse episodeBean = imgResponses.get(curPosition);
        holder.tvTitle.setText(episodeBean.adImgName);
        holder.tvPrice.setText(episodeBean.adImgPrice);


        holder.iv.setBackgroundResource(R.drawable.hannibal);
    }



    @Override
    public int getItemCount() {
        return Integer.MAX_VALUE;
    }

    class MyViewHolder extends RecyclerView.ViewHolder {

        TextView tvTitle;
        TextView tvPrice;
        ImageView iv;

        public MyViewHolder(View itemView) {
            super(itemView);
            tvTitle = (TextView) itemView.findViewById(R.id.ad_three_text_name);
            tvPrice = (TextView) itemView.findViewById(R.id.ad_three_text_price);
            iv = (ImageView) itemView.findViewById(R.id.ad_three_img);
        }
    }



}