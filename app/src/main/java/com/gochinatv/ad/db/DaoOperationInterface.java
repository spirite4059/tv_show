package com.gochinatv.ad.db;

import java.util.ArrayList;

public interface DaoOperationInterface<T> {
	
	public boolean add(T record);
	
	public boolean query(T record);
	
	public ArrayList<T> queryAll();
	
	public boolean delete(String aid);
	
	public void deleteAll();

	public void close();
	
	public void insert(T record);
}
