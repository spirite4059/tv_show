package com.gochinatv.db;


import android.content.Context;

import com.okhtttp.response.AdDetailResponse;

import java.util.ArrayList;

public abstract class DaoOperationInterface {

	/**
	 * 插入一条视频信息记录
	 *
	 * @param adDetailResponse
	 * @return
	 */
	public static boolean insert(Context context, boolean isToday, AdDetailResponse adDetailResponse) {
		return false;
	}

	/**
	 * 插入多条视频信息记录
	 * @return
	 */
	public static void insertAll(Context context, boolean isToday, ArrayList<AdDetailResponse> adDetailResponses){};

	/**
	 * 根据视频id，删除当前记录
	 * @param id
	 * @return
	 */
	public static boolean delete(Context context, boolean isToday, int id){return false;};


	/**
	 * 根据视频id，查询是否存在当前记录
	 * @param id
	 * @return 存在：true，反正false
	 */
	public static boolean query(Context context, boolean isToday, int id){
		return false;
	};

	/**
	 * 查询表内所有信息
	 * @return 存在：true，反正false
	 */
	public static ArrayList<AdDetailResponse> queryAll(Context context, boolean isToday){
		return null;
	};

	/**
	 * 根据id，修改对应视频的长度
	 * @param fileName
	 * @return
	 */
	public static void update(Context context, boolean isToday, String fileName, String column, String value){};


}
