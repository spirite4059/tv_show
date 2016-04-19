package com.gochinatv.ad.db;

import com.httputils.http.response.AdDetailResponse;

public interface DaoOperationInterface {

	/**
	 * 插入一条视频信息记录
	 * @param adDetailResponse
	 * @return
	 */
	 boolean insert(AdDetailResponse adDetailResponse);

	/**
	 * 根据视频id，删除当前记录
	 * @param id
	 * @return
	 */
	boolean delete(int id);


	/**
	 * 根据视频id，查询是否存在当前记录
	 * @param id
	 * @return 存在：true，反正false
	 */
	 boolean query(int id);

	/**
	 * 根据id，修改对应视频的长度
	 * @param id
	 * @param length
	 * @return
	 */
	boolean update(int id, long length);


}
