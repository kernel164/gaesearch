package org.gaesearch.dao.impl;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import javax.jdo.Extent;
import javax.jdo.JDOObjectNotFoundException;
import javax.jdo.PersistenceManager;
import javax.jdo.PersistenceManagerFactory;

import org.gaesearch.dao.__Generic__Dao;

public abstract class __Generic__DaoImpl<K extends Serializable, T> implements __Generic__Dao<K, T> {
	protected PersistenceManagerFactory pmf;

	protected final Class<T> clazz;

	protected __Generic__DaoImpl(Class<T> clazz) {
		this.clazz = clazz;
	}

	@Override
	public void setPersistenceManagerFactory(PersistenceManagerFactory pmf) {
		this.pmf = pmf;
	}

	@Override
	public boolean exists(K id) {
		return get(id) != null;
	}

	public T get(K id) {
		PersistenceManager pm = pmf.getPersistenceManager();
		T obj = null;
		//T detached = null;

		try {
			obj = pm.getObjectById(clazz, id);
			System.out.println("__Generic__DaoImpl:get(obj) - " + obj);
			// If you're using transactions, you can call
			// pm.setDetachAllOnCommit(true) before committing to automatically
			// detach all objects without calls to detachCopy or detachCopyAll.
			//detached = pm.detachCopy(obj);
			return obj;
			//System.out.println("__Generic__DaoImpl:get(detached) - " + detached);
		} catch (JDOObjectNotFoundException e) {
			// e.printStackTrace();
			System.err.println(e.getMessage());
			// ignore
			// could not retrieve entity of kind __Index__ with key __Index__("xyz")
			return null;
		} finally {
			pm.close();
		}
		//return detached;
	}

	@Override
	public T save(T obj) {
		PersistenceManager pm = pmf.getPersistenceManager();
		try {
			pm.makePersistent(obj);
		} finally {
			pm.close();
		}
		return obj;
	}

	public Collection<T> saveAll(Collection<T> list) {
		PersistenceManager pm = pmf.getPersistenceManager();
		try {
			Iterator<T> it = list.iterator();
			for (; it.hasNext();) {
				pm.makePersistent(it.next());
			}
		} finally {
			pm.close();
		}
		return list;
	}

	@Override
	public T[] saveAll(T... list) {
		saveAll(Arrays.asList(list));
		return list;

	}

	@Override
	public List<T> saveAll(List<T> list) {
		PersistenceManager pm = pmf.getPersistenceManager();
		try {
			for (int i = 0; i < list.size(); i++) {
				pm.makePersistent(list.get(i));
			}
		} finally {
			pm.close();
		}
		return list;
	}


	@Override
	public List<T> getAll() {
		List<T> rtn = new ArrayList<T>();
		PersistenceManager pm = pmf.getPersistenceManager();
		try {
			Extent<T> extent = pm.getExtent(clazz, false);
			for (T e : extent) {
				rtn.add(e);
			}
			extent.closeAll();
		} finally {
			pm.close();
		}
		return rtn;
	}

	@Override
	public List<T> getAll(K... ids) {
		return getAll(Arrays.asList(ids));
	}

	@Override
	public List<T> getAll(List<K> ids) {
		throw new UnsupportedOperationException("Not Yet Implemented!");
	}

	@Override
	public void remove(T obj) {
		PersistenceManager pm = pmf.getPersistenceManager();
		try {
			pm.deletePersistent(obj);
		} finally {
			pm.close();
		}
	}

	@Override
	public void removeAll(T... objs) {
		removeAll(Arrays.asList(objs));
	}

	@Override
	public void removeAll(Collection<T> objs) {
		PersistenceManager pm = pmf.getPersistenceManager();
		try {
			pm.deletePersistentAll(objs);
		} finally {
			pm.close();
		}
	}

	protected String getStartsWith(String fieldName, String... prefixes) {
		String ff = fieldName + ".startsWith(\"";
		StringBuilder sb = new StringBuilder();

		sb.append("(");
		for (int i = 0; i < prefixes.length; i++) {
			sb.append(ff);
			sb.append(prefixes[i]);
			sb.append("\")");
			if (i != prefixes.length - 1) {
				sb.append(" or ");
			}
		}
		sb.append(")");

		return sb.toString();
	}
}

/*
 * DatastoreService service =
 * DatastoreServiceFactory.getDatastoreService();
 *
 * List<Key> keys = new ArrayList<Key>(); //...
 *
 * Map<Key,Entity> entities = service.get(keys);
 *
 * for (Map.Entry<Key,Entity> e : entities) { //... }
 */
