package com.gochinatv.ad.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.gochinatv.ad.R;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;
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

        imageLoader = ImageLoader.getInstance();
        options = new DisplayImageOptions.Builder().cacheInMemory(false).cacheOnDisc(true)
                .imageScaleType(ImageScaleType.IN_SAMPLE_POWER_OF_2)
                .showImageOnLoading(R.drawable.ad_three_loading1)
                .showImageOnFail(R.drawable.ad_three_loading1).bitmapConfig(Bitmap.Config.RGB_565)
                .displayer(new FadeInBitmapDisplayer(1000, true, false, false)).build();


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
        if(imgResponses != null && imgResponses.size()>0 ){
            int curPosition = position % imgResponses.size();
            AdImgResponse episodeBean = imgResponses.get(curPosition);
            holder.tvTitle.setText(episodeBean.adImgName);

            if(!TextUtils.isEmpty(episodeBean.adImgPrice)){
                holder.tvPrice.setText(episodeBean.adImgPrice + "元");
            }else{
                holder.tvPrice.setText(episodeBean.adImgPrice);
            }

            if("localPicture".equals(episodeBean.adImgUrl)){
                imageLoader.displayImage("drawable://" + R.drawable.ad_three_loading1,holder.iv,options);
            }else {
                imageLoader.displayImage(episodeBean.adImgUrl,holder.iv,options);
            }

            for(AdImgResponse adImgResponse :imgResponses){
//                LogCat.e("%%%%%%%%%%%  EpisodeAdapter %%%%%%%%%%轮循请求图片    " + adImgResponse.adImgName);
            }
        }

//        //Uri uri = Uri.parse("res://com.gochinatv.ad/" + R.drawable.news3);
//        Uri uri = Uri.parse(episodeBean.adImgUrl);
//        //holder.iv.setBackgroundResource(R.drawable.hannibal);
//
//        //创建DraweeController
//        DraweeController controller = Fresco.newDraweeControllerBuilder()
//                //加载的图片URI地址
//                .setUri(uri)
//                        //设置点击重试是否开启
//                .setTapToRetryEnabled(true)
//                        //设置旧的Controller
//                .setOldController(holder.iv.getController())
//                        //构建
//                .build();
//
//        holder.iv.setController(controller);
    }



    @Override
    public int getItemCount() {
        return Integer.MAX_VALUE;
        //return imgResponses.size();
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


    /**
     * 刷新数据
     */

    public void referenceData(ArrayList<AdImgResponse> imgResponses){
        this.imgResponses = imgResponses;
        notifyDataSetChanged();
    }





}