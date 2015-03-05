/*
 * Copyright (C) 2015 HouKx <hkx.aidream@gmail.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package androidx.pluginmgr;

import java.lang.reflect.Field;

/**
 * 反射工具类
 * @author HouKangxi
 *
 */
public class ReflectionUtils {

	public static <T> T getFieldValue(Object obj, String fieldName)
			throws IllegalAccessException, IllegalArgumentException,
			NoSuchFieldException {
		return getFieldValue(obj, fieldName, true);
	}

	@SuppressWarnings("unchecked")
	public static <T> T getFieldValue(Object obj, String fieldName,
			boolean resolveParent) throws IllegalAccessException,
			IllegalArgumentException, NoSuchFieldException {
		Object[] rs = getField(obj, fieldName, resolveParent);
		if (rs == null) {
			throw new NoSuchFieldException("field:" + fieldName);
		}
		Field field = (Field) rs[0];
		Object targetObj = rs[1];
		return (T) field.get(targetObj);
	}

	public static void setFieldValue(Object obj, String fieldName, Object val)
			throws IllegalAccessException, IllegalArgumentException,
			NoSuchFieldException {
		setFieldValue(obj, fieldName, val, true);
	}

	public static void setFieldValue(Object obj, String fieldName, Object val,
			boolean resolveParent) throws IllegalAccessException,
			IllegalArgumentException, NoSuchFieldException {
		Object[] rs = getField(obj, fieldName, resolveParent);
		if (rs == null) {
			throw new NoSuchFieldException("field:" + fieldName);
		}
		Field field = (Field) rs[0];
		Object targetObj = rs[1];
		field.set(targetObj, val);
	}

	private static Object[] getField(Object obj, String elFieldName,
			boolean resolveParent) throws IllegalAccessException,
			IllegalArgumentException, NoSuchFieldException {
		if (obj == null) {
			return null;
		}
		String[] fieldNames = elFieldName.split("[.]");
		Object targetObj = obj;
		Class<?> targetClass = targetObj.getClass();
		Object val = null;
		int i = 0;
		Field field = null;
		Object[] rs = new Object[2];
		for (String fName : fieldNames) {
			i++;
			field = getField_(targetClass, fName, resolveParent);
			field.setAccessible(true);
			rs[0] = field;
			rs[1] = targetObj;
			val = field.get(targetObj);
			if (val == null) {
				if (i < fieldNames.length) {
					throw new IllegalAccessException(
							"can not getFieldValue as field '" + fName
									+ "' value is null in '"
									+ targetClass.getName() + "'");
				}
				break;
			}
			targetObj = val;
			targetClass = targetObj.getClass();
		}
		return rs;
	}

	public static Field getField_(Class<?> targetClass, String fieldName,
			boolean resolveParent) throws IllegalAccessException,
			IllegalArgumentException, NoSuchFieldException {
		NoSuchFieldException noSuchFieldExceptionOccor = null;
		Field rsField = null;
		try {
			Field field = targetClass.getDeclaredField(fieldName);
			rsField = field;
			if (!resolveParent) {
				field.setAccessible(true);
				return field;
			}
		} catch (NoSuchFieldException e) {
			noSuchFieldExceptionOccor = e;
		}
		if (noSuchFieldExceptionOccor != null) {
			if (resolveParent) {
				while (true) {
					targetClass = targetClass.getSuperclass();
					if (targetClass == null) {
						break;
					}
					try {
						Field field = targetClass.getDeclaredField(fieldName);
						field.setAccessible(true);
						return rsField = field;
					} catch (NoSuchFieldException e) {
						if (targetClass.getSuperclass() == null) {
							throw e;
						}
					}
				}
			} else {
				throw noSuchFieldExceptionOccor;
			}
		}
		return rsField;
	}
}
