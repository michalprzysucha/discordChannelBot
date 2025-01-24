package com.packt.repositories;

public interface DatabaseRepository<T> {
    void save(T t);
    void delete(T t);
    void update(T t);
}
