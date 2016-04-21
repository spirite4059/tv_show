package com.gochinatv.db;


import com.okhtttp.response.AdDetailResponse;

import java.util.ArrayList;

public interface DaoOperationInterface {

	/**
	 * 插入一条视频信息记录
	 * @param adDetailResponse
	 * @return
	 */
	boolean insert(boolean isToday, AdDetailResponse adDetailResponse);

	/**
	 * 插入多条视频信息记录
	 * @return
	 */
	void insertAll(boolean isToday, ArrayList<AdDetailResponse> adDetailResponses);

	/**
	 * 根据视频id，删除当前记录
	 * @param id
	 * @return
	 */
	boolean delete(boolean isToday, int id);


	/**
	 * 根据视频id，查询是否存在当前记录
	 * @param id
	 * @return 存在：true，反正false
	 */
	 boolean query(boolean isToday, int id);

	/**
	 * 查询表内所有信息
	 * @return 存在：true，反正false
	 */
	ArrayList<AdDetailResponse> queryAll(boolean isToday);

	/**
	 * 根据id，修改对应视频的长度
	 * @param fileName
	 * @return
	 */
	boolean update(boolean isToday, String fileName, String column, String value);


}
