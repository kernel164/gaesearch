package org.gaesearch.util;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.gaesearch.annotation.SearchCapable;
import org.gaesearch.annotation.SearchField;
import org.gaesearch.annotation.SearchKey;
import org.gaesearch.model.SearchCapableMetaData;

// split("[^\\w]+")
// http://searchguestbook.appspot.com/searchguestbook.tar.gz
// http://www.java2s.com/Code/Java/Regular-Expressions/CalculatingWordFrequencieswithRegularExpressions.htm
// http://www.devx.com/Java/Article/42216/1954?pf=true
// http://google-appengine.googlegroups.com/web/efficient_paging_using_key_instead_of_a_dedicated_unique_property.txt?gda=SZwzXncAAACSStSWrftt07H4FK2RtvuruXxRXPydL8WzRjsY2Fv9EQQgER4RQV57mxjvIzAWBZmQ3TeCdbqm30Qz_AwgYlIpRbcWRj3jGGBm-fgbnPJIYc4-hXRRmo3Xgj6KgtSLBeZ45alvcyXc30EbEX-RNDZveV4duv6pDMGhhhZdjQlNAw
// http://snowball.tartarus.org/algorithms/english/stemmer.html
public class GSrchUtils {
	static Set<String> NOISE_WORDS = Collections.unmodifiableSet(new HashSet<String>(Arrays.asList("the", "a", "an", "it", "or", "and", "he", "she", "with", "often", "to", "do", "that", "this", "is", "are", "one", "two", "since", "just", "start", "beyond", "could", "not", "be", "from", "on", "could", "as", "say", "said", "will", "if", "by", "on", "often", "little", "big", "did", "do", "about", "any", "such", "up", "s", "already", "than", "now", "gave", "less", "more", "another", "for", "other", "goes", "would", "of", "her", "how", "told", "meet", "without", "few", "has", "ask", "run", "across", "rather", "me", "sometime", "want", "d", "look", "perhaps", "come", "o", "us", "m", "seem", "i", "u", "t", "what", "but", "last", "who", "toward", "when", "thing", "got", "can", "with", "at", "off", "in", "much", "under", "why", "also", "take", "am", "great", "in", "top")));
	static boolean MATCH_EXACT_WORDS = true;
	static Pattern WORD_BREAK_PATTERN = Pattern.compile("[\\p{Punct}\\s}]");
	static Pattern LINE_BREAK_PATTERN = Pattern.compile(".*$", Pattern.MULTILINE);


	public static Map<Class<? extends SearchCapable>, SearchCapableMetaData> getMap(String scanPath) {
		return getMap(new String[]{scanPath});
	}

	@SuppressWarnings("unchecked")
	public static Map<Class<? extends SearchCapable>, SearchCapableMetaData> getMap(String[] scanPath) {
		Map<Class<? extends SearchCapable>, SearchCapableMetaData> map = new HashMap<Class<? extends SearchCapable>, SearchCapableMetaData>();
		List<Class<? extends Annotation>> clazzList = AnnotationScanner.getClassAnnotations(scanPath, SearchCapable.class);
		Iterator<Class<? extends Annotation>> it = clazzList.iterator();
		while (it.hasNext()) {
			SearchCapableMetaData model = new SearchCapableMetaData();
			Class<? extends SearchCapable> clazz = (Class<? extends SearchCapable>) it.next();
			SearchCapable sc = clazz.getAnnotation(SearchCapable.class);
			String modelName = sc.name();
			model.setName(modelName.length() == 0 ? clazz.getSimpleName() : modelName);

			List<Field> fieldList = AnnotationScanner.getFieldAnnotations(clazz, SearchKey.class);
			if (fieldList.size() == 0) {
				// atleast 1 id;
			}
			if (fieldList.size() > 1) {
				// only one id;
			}
			model.setSearchKeyName(fieldList.get(0).getName());

			fieldList = AnnotationScanner.getFieldAnnotations(clazz, SearchField.class);
			if (fieldList.size() > 0) {
				// atleast one search field...
			}
			Map<String, String> fieldNames = new HashMap<String, String>();
			for (int i = 0; i < fieldList.size(); i++) {
				Field field = fieldList.get(i);
				String fieldName = field.getName();
				SearchField sf = field.getAnnotation(SearchField.class);
				String fVal = sf.value();
				fieldNames.put(fieldName, fVal.length() == 0 ? fieldName : fVal);
			}
			model.setSearchFieldNames(fieldNames);

			map.put(clazz, model);
		}

		return map;
	}

	public static Object getValue(Object obj, Field field) {
		field.setAccessible(true);
		Object value = null;
		try {
			value = field.get(obj);
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}
		return value;
	}

	public static Object getIdValue(Object obj, String idName) {
		Field field = null;
		try {
			field = obj.getClass().getDeclaredField(idName);
		} catch (SecurityException e1) {
			e1.printStackTrace();
		} catch (NoSuchFieldException e1) {
			e1.printStackTrace();
		}
		return getValue(obj, field);
	}


	public static List<String> getTokens(String text) {
		String str = text.toLowerCase();
		List<String> words = new ArrayList<String>();

		// Match line pattern to str
		Matcher lineMatcher = LINE_BREAK_PATTERN.matcher(str);

		// For each line
		while (lineMatcher.find()) {
			// Get line
			CharSequence line = lineMatcher.group();

			// .split("[\\ \\.\\,\\:\\;\\(\\)\\-\\[\\]!]");
			String[] tokens = WORD_BREAK_PATTERN.split(line);
			for (String token : tokens) {
				if (!NOISE_WORDS.contains(token)) {
					// exact words only
					if (MATCH_EXACT_WORDS) {
						words.add(token);
					} /*else {
						// TODO: (No Need??)
						// or, also match word prefixes
						int len = token.length();
						if (len > 2) {
							words.add(new Word(token));
							if (len > 3) {
								int start_index = 1 + (len / 2);
								for (int i = start_index; i < len; i++) {
									words.add(new Word(token.substring(0, i), (0.25f * i) / len));
								}
							}
						}
					}*/
				}
			}
		}
		return words;
	}
}
