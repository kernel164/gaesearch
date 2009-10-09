package org.gaesearch.dao;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;

import javax.jdo.PersistenceManagerFactory;

public interface __Generic__Dao<K extends Serializable, T> {
	void setPersistenceManagerFactory(PersistenceManagerFactory pmf);

	List<T> getAll();

	List<T> getAll(K... ids);

	List<T> getAll(List<K> ids);

	T get(K id);

	boolean exists(K id);

	T save(T obj);

	void remove(T obj);

	void removeAll(T... objs);

	void removeAll(Collection<T> objs);

	List<T> saveAll(List<T> list);

	T[] saveAll(T... list);

	Collection<T> saveAll(Collection<T> list);
}
