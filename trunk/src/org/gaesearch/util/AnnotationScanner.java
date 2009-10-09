package org.gaesearch.util;

import java.io.File;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class AnnotationScanner {
	public static List<Class<? extends Annotation>> getClassAnnotations(String[] paths, Class<? extends Annotation> aClazz) {
		Map<String, List<Class<?>>> clazzMap = new HashMap<String, List<Class<?>>>();
		for (int i = 0; i < paths.length; i++) {
			clazzMap.putAll(getAllClasses(paths[i]));
		}
		return getClassAnnotations(clazzMap, aClazz);
	}

	@SuppressWarnings("unchecked")
	public static List<Class<? extends Annotation>> getClassAnnotations(Map<String, List<Class<?>>> clazzMap, Class<? extends Annotation> aClazz) {
		List<Class<? extends Annotation>> classList = new ArrayList<Class<? extends Annotation>>();
		Set<Map.Entry<String, List<Class<?>>>> entrySet = clazzMap.entrySet();
		Iterator<Map.Entry<String, List<Class<?>>>> it = entrySet.iterator();

		while (it.hasNext()) {
			Map.Entry<String, List<Class<?>>> entry = it.next();
			// String key = entry.getKey();
			List<Class<?>> clazzList = entry.getValue();
			for (int i = 0; i < clazzList.size(); i++) {
				Class<?> clazz = clazzList.get(i);
				if (clazz.isAnnotationPresent(aClazz)) {
					classList.add((Class<? extends Annotation>) clazz);
				}
			}
		}

		return classList;
	}

	public static Map<String, List<Class<?>>> getAllClasses(String path) {
		Map<String, List<Class<?>>> clazzMap = new HashMap<String, List<Class<?>>>();

		// check for absolute class
		clazzMap.putAll(getClass(path));
		if (clazzMap.size() == 1) {
			return clazzMap;
		}

		// Translate the package name into an absolute path
		String name = new String(path);
		if (!name.startsWith("" + File.separatorChar)) {
			name = File.separatorChar + name;
		}
		name = name.replace('.', File.separatorChar);

		// Get a File object for the package
		URL url = AnnotationScanner.class.getResource(name);
		File directory = new File(url.getFile());
		if (directory.exists()) {
			getAllClasses(directory, clazzMap, path);
		}

		return clazzMap;
	}

	public static Map<String, List<Class<?>>> getClass(String path) {
		Map<String, List<Class<?>>> clazzMap = new HashMap<String, List<Class<?>>>();
		List<Class<?>> clazzes = new ArrayList<Class<?>>();

		try {
			// Try to create an instance of the object
			Class<?> clazz = Class.forName(path);
			clazzes.add(clazz);
			clazzMap.put(clazz.getPackage().getName(), clazzes);
		} catch (ClassNotFoundException cnfex) {
			System.err.println(cnfex);
		}

		return clazzMap;
	}

	public static Map<String, List<Class<?>>> getAllClasses(File directory, Map<String, List<Class<?>>> clazzMap, String rootPackageName) {
		List<Class<?>> clazzes = new ArrayList<Class<?>>();
		List<File> dirs = new ArrayList<File>();

		// Get the list of the files contained in the package
		// String[] files = directory.list();
		File[] files = directory.listFiles();
		String packagePath = getPackageName(rootPackageName, directory.getPath());
		for (int i = 0; i < files.length; i++) {
			if (files[i].isDirectory()) {
				dirs.add(files[i]);
				continue;
			}
			if (files[i].isFile()) {
				String fileName = files[i].getName();
				// we are only interested in .class files
				if (fileName.endsWith(".class")) {
					// removes the .class extension
					String classname = fileName.substring(0, fileName.length() - 6);
					try {
						// Try to create an instance of the object
						clazzes.add(Class.forName(packagePath + "." + classname));
					} catch (ClassNotFoundException cnfex) {
						System.err.println(cnfex);
					}
				}
			}
		}

		clazzMap.put(packagePath, clazzes);

		for (int i = 0; i < dirs.size(); i++) {
			File dir = dirs.get(i);
			getAllClasses(dir, clazzMap, packagePath);
		}

		return clazzMap;
	}

	public static String getPackageName(String rootPackageName, String dirName) {
		String dName = dirName.replace(File.separatorChar, '.');
		int index = dName.indexOf(rootPackageName);
		return rootPackageName + dName.substring(rootPackageName.length() + index);
	}

	public static List<Field> getFieldAnnotations(Class<?> clazz, Class<? extends Annotation> aClazz) {
		List<Field> fieldList = new ArrayList<Field>();
		// System.out.println(Arrays.asList(clazz.getAnnotations()));
		Field[] fields = clazz.getDeclaredFields();
		for (int i = 0; i < fields.length; i++) {
			if (fields[i].isAnnotationPresent(aClazz)) {
				fieldList.add(fields[i]);
			}
		}

		return fieldList;
	}
}
