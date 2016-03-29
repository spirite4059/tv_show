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

import java.util.ArrayList;

/**
 * Created by fq_mbp on 16/1/25.
 */
public class EpisodeAdapter extends RecyclerView.Adapter<EpisodeAdapter.MyViewHolder> {

    private final LayoutInflater mLayoutInflater;
    private ArrayList<String> datas;
    private DisplayImageOptions options;
    private ImageLoader imageLoader;

    public EpisodeAdapter(Context context, ArrayList<String> mDatas) {
        mLayoutInflater = LayoutInflater.from(context);
        this.datas = mDatas;
//        imageLoader = ImageLoader.getInstance();
//        imageLoader.init(MyApplication.initImageLoader(context).build());
//        options = new DisplayImageOptions.Builder().cacheInMemory(false).cacheOnDisc(true)
//                .imageScaleType(ImageScaleType.IN_SAMPLE_POWER_OF_2)
//                .bitmapConfig(Bitmap.Config.RGB_565)
//                .displayer(new FadeInBitmapDisplayer(800, true, false, false)).build();
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        MyViewHolder holder = new MyViewHolder(mLayoutInflater.inflate(R.layout.item_episode_list, parent, false));
        return holder;
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
//        int curPosition = position % datas.size();
//        EpisodeItemResponse episodeBean = datas.get(curPosition);
//        holder.tvTitle.setText(episodeBean.installment);
//        holder.tvDescription.setText(episodeBean.name);
//
//        if(!TextUtils.isEmpty(DataUtils.getVideoTimeBySecond(episodeBean.duration))){
//            holder.tvDuration.setText("("+ DataUtils.getVideoTimeBySecond(episodeBean.duration)+"分钟)");
//        }
//
//        holder.iv.setBackgroundResource(getResId(position));
    }



    @Override
    public int getItemCount() {
        return Integer.MAX_VALUE;
    }

    class MyViewHolder extends RecyclerView.ViewHolder {

        TextView tvTitle;
        TextView tvDescription;
        TextView tvInstallment;
        TextView tvDuration;
        ImageView iv;

        public MyViewHolder(View itemView) {
            super(itemView);
//            tvTitle = (TextView) itemView.findViewById(R.id.tv_episode_title);
//            tvDescription = (TextView) itemView.findViewById(R.id.tv_episode_description);
//            //tvInstallment = (TextView) itemView.findViewById(R.id.tv_episode_installment);
//            tvDuration = (TextView) itemView.findViewById(R.id.tv_episode_duration);
//            iv = (ImageView) itemView.findViewById(R.id.iv_episode);
        }
    }



}