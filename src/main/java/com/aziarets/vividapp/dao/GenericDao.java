package com.aziarets.vividapp.dao;

public interface GenericDao<T> {
    public int save(T t);
//    public T getById(long id);
}
