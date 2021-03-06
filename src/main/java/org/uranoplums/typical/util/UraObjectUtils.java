/*
 * Copyright 2013-2015 the Uranoplums Foundation and the Others.
 * Copyright 2004-2011 the Seasar Foundation and the Others.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 *
 * $Id: UraObjectUtils.java$
 */
package org.uranoplums.typical.util;

import static org.uranoplums.typical.collection.factory.UraMapFactory.*;

import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.GenericDeclaration;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;
import java.util.ArrayDeque;
import java.util.Collection;
import java.util.Deque;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.ObjectUtils;
import org.uranoplums.typical.exception.UraSystemRuntimeException;
import org.uranoplums.typical.log.UraLogger;
import org.uranoplums.typical.log.UraLoggerFactory;

/**
 * UraObjectUtilsクラス。<br>
 *
 * @since 2015/04/10
 * @author syany
 */
public class UraObjectUtils extends ObjectUtils {

    protected static final UraLogger<String> LOGGER = UraLoggerFactory.getUraStringCodeLog();

    /**
     * デフォルトコンストラクタ。<br>
     */
    protected UraObjectUtils() {
        throw new AssertionError();
    }

    /**
     * 型パラメータの具象型取得の実装。再帰処理される。
     * @param clazz 現在の走査対象型
     * @param targetTypeName 現在の走査対象のジェネリクス型パラメータ名
     * @param deque 現在の走査対象型以下の継承階層が積まれたStack
     * @return 該当型パラメータの具現化された型
     */
    @SuppressWarnings ("unchecked")
    private static <E> Class<E> getGenericTypeImpl(Class<?> clazz,
            String targetTypeName, Deque<Class<?>> deque) {
        TypeVariable<? extends Class<?>>[] superGenTypeAray = clazz.getSuperclass().getTypeParameters();
        // 走査対象の型パラメータの名称(Tなど)から宣言のインデックスを取得
        int index = 0;
        boolean existFlag = false;
        for (final TypeVariable<? extends Class<?>> type : superGenTypeAray) {
            if (targetTypeName.equals(type.getName())) {
                existFlag = true;
                break;
            }
            index++;
        }
        if (!existFlag) {
            throw new IllegalArgumentException(
                    targetTypeName + "に合致するジェネリクス型パラメータがみつかりません");
        }
        // 走査対象の型パラメータが何型とされているのかを取得
        ParameterizedType type = (ParameterizedType) clazz.getGenericSuperclass();
        Type y = type.getActualTypeArguments()[index];
        // 具象型で継承されている場合
        if (y instanceof Class) {
            return (Class<E>) y;
        }
        // ジェネリックパラメータの場合
        if (y instanceof TypeVariable) {
            TypeVariable<Class<?>> tv = (TypeVariable<Class<?>>) y;
            // 再帰して同名の型パラメータを継承階層を下りながら解決を試みる
            Class<?> sub = deque.pop();
            return getGenericTypeImpl(sub, tv.getName(), deque);
        }
        // ジェネリック型パラメータを持つ型の場合
        if (y instanceof ParameterizedType) {
            ParameterizedType pt = (ParameterizedType) y;
            return (Class<E>) pt.getRawType();
        }
        throw new IllegalArgumentException("予期せぬ型 : "
                + y.toString() + " (" + y.getClass() + ")");
    }

    /**
     * 。<br>
     * @param source
     * @param klass
     * @return
     */
    public static final <E> E cast(Object source, Class<E> klass) {
        // @SuppressWarnings ("unchecked")
        // Class<E> klass = (Class<E>) type.getClass();
        if (source == null || !klass.isInstance(source)) {
            ClassLoader cl = UraClassUtils.getCurrentClassLoader(klass);
            try {
                @SuppressWarnings ("unchecked")
                Class<E> clz = (Class<E>) cl.loadClass(klass.getName());
                Constructor<E> co = clz.getDeclaredConstructor();
                co.setAccessible(true);
                return co.newInstance();
            } catch (Exception e) {
                UraSystemRuntimeException se = new UraSystemRuntimeException(e);
                LOGGER.log("ERROR: klass=[{}], source=[{}], exception=[{}]", klass, source, se.getLocalizedMessage(), se);
                throw se;
            }
        }
        return klass.cast(source);
    }

    /**
     * 。<br>
     * @param source
     * @param types
     * @return
     */
    public static final <E> E cast(Object source, E... types) {
        @SuppressWarnings ("unchecked")
        Class<E> klass = (Class<E>) types.getClass().getComponentType();
        return cast(source, klass);
    }

    /**
     * gg
     * 。<br>
     * @param source
     * @param targetArray
     * @return
     */
    public static final <E> E[] castToArray(Object source, E... targetArray) {
        try {
            @SuppressWarnings ("unchecked")
            Class<E[]> klass = (Class<E[]>) targetArray.getClass();
            if (source == null) {
                return targetArray;
            } else if (klass.isInstance(source)) {
                return klass.cast(source);
            } else if (source instanceof Collection) {
                @SuppressWarnings ("unchecked")
                Collection<E> c = Collection.class.cast(source);
                return c.toArray(targetArray);
            } else if (source instanceof Map) {
                @SuppressWarnings ("unchecked")
                Map<E, Object> m = Map.class.cast(source);
                return m.keySet().toArray(targetArray);
            }
        } catch (Exception e) {
            UraSystemRuntimeException se = new UraSystemRuntimeException(e);
            LOGGER.log("ERROR: arrayClass=[{}], source=[{}], exception=[{}]", targetArray, source, se.getLocalizedMessage(), se);
            throw se;
        }
        return targetArray;
    }

    /**
     * オブジェクト同士を確認します。<br>
     * 第一引数のequalsを利用します。
     * @param src
     * @param dst
     * @return
     */
    public static boolean equals(Object src, Object dst) {
        if (src == null) {
            return dst == null;
        }
        return src.equals(dst);
    }

    /**
     * 渡された型から継承階層を登って、
     * 指定の親の型の指定の名前のジェネリクス型パラメータが
     * 継承の過程で何型で具現化されているかを走査して返す。
     * @param clazz 走査開始する型
     * @param targetClass 走査する対象のジェネリクス型パラメータを持つ型。
     *            走査開始型の親である必要がある。
     * @param targetTypeName 何型で具現化されたを確認したい型パラメータのプレースホルダ名
     * @return 具現化された型
     */
    public static <E> Class<E> getGenericType(
            Class<?> clazz, Class<?> targetClass,
            String targetTypeName) {
        if (!targetClass.isAssignableFrom(clazz)) {
            throw new IllegalArgumentException(
                    "型" + clazz.getName() + "は、型"
                            + targetClass.getName() + "を継承していません");
        }
        Deque<Class<?>> deque = new ArrayDeque<Class<?>>();
        while (!targetClass.equals(clazz.getSuperclass())) {
            deque.push(clazz);
            clazz = clazz.getSuperclass();
        }
        return getGenericTypeImpl(clazz, targetTypeName, deque);
    }


    /**
     * <code>type</code>の原型が<code>clazz</code>に代入可能であれば<code>true</code>を、
     * それ以外の場合は<code>false</code>を返します。
     *
     * @param type
     *            タイプ
     * @param clazz
     *            クラス
     * @return <code>type</code>の原型が<code>clazz</code>に代入可能であれば<code>true</code>
     */
    public static boolean isTypeOf(final Type type, final Class<?> clazz) {
        if (Class.class.isInstance(type)) {
            return clazz.isAssignableFrom(Class.class.cast(type));
        }
        if (ParameterizedType.class.isInstance(type)) {
            final ParameterizedType parameterizedType = ParameterizedType.class
                    .cast(type);
            return isTypeOf(parameterizedType.getRawType(), clazz);
        }
        return false;
    }

    /**
     * <code>type</code>の原型を返します。
     * <ul>
     * <li><code>type</code>が<code>Class</code>の場合はそのまま返します。</li>
     * <li><code>type</code>がパラメータ化された型の場合はその原型を返します。</li>
     * <li><code>type</code>がワイルドカード型の場合は(最初の)上限境界を返します。</li>
     * <li><code>type</code>が配列の場合はその要素の実際の型の配列を返します。</li>
     * <li>その他の場合は<code>null</code>を返します。</li>
     * </ul>
     *
     * @param type
     *            タイプ
     * @return <code>type</code>の原型
     */
    public static Class<?> getRawClass(final Type type) {
        if (Class.class.isInstance(type)) {
            return Class.class.cast(type);
        }
        if (ParameterizedType.class.isInstance(type)) {
            final ParameterizedType parameterizedType = ParameterizedType.class
                    .cast(type);
            return getRawClass(parameterizedType.getRawType());
        }
        if (WildcardType.class.isInstance(type)) {
            final WildcardType wildcardType = WildcardType.class.cast(type);
            final Type[] types = wildcardType.getUpperBounds();
            return getRawClass(types[0]);
        }
        if (GenericArrayType.class.isInstance(type)) {
            final GenericArrayType genericArrayType = GenericArrayType.class
                    .cast(type);
            final Class<?> rawClass = getRawClass(genericArrayType
                    .getGenericComponentType());
            return Array.newInstance(rawClass, 0).getClass();
        }
        return null;
    }

    /**
     * <code>type</code>の型引数の配列を返します。
     * <p>
     * <code>type</code>がパラメータ化された型でない場合は<code>null</code>を返します。
     * </p>
     *
     * @param type
     *            タイプ
     * @return <code>type</code>の型引数の配列
     */
    public static Type[] getGenericParameter(final Type type) {
        if (ParameterizedType.class.isInstance(type)) {
            return ParameterizedType.class.cast(type).getActualTypeArguments();
        }
        if (GenericArrayType.class.isInstance(type)) {
            return getGenericParameter(GenericArrayType.class.cast(type)
                    .getGenericComponentType());
        }
        return null;
    }

    /**
     * 指定された位置の<code>type</code>の型引数を返します。
     * <p>
     * <code>type</code>がパラメータ化された型でない場合は<code>null</code>を返します。
     * </p>
     *
     * @param type
     *            タイプ
     * @param index
     *            位置
     * @return 指定された位置の<code>type</code>の型引数
     */
    public static Type getGenericParameter(final Type type, final int index) {
        if (!ParameterizedType.class.isInstance(type)) {
            return null;
        }
        final Type[] genericParameter = getGenericParameter(type);
        if (genericParameter == null) {
            return null;
        }
        return genericParameter[index];
    }

    /**
     * パラメータ化された型を要素とする配列の要素型を返します。
     * <p>
     * <code>type</code>がパラメータ化された型の配列でない場合は<code>null</code>を返します。
     * </p>
     *
     * @param type
     *            パラメータ化された型を要素とする配列
     * @return パラメータ化された型を要素とする配列の要素型
     */
    public static Type getElementTypeOfArray(final Type type) {
        if (!GenericArrayType.class.isInstance(type)) {
            return null;
        }
        return GenericArrayType.class.cast(type).getGenericComponentType();
    }

    /**
     * パラメータ化された{@link Collection}の要素型を返します。
     * <p>
     * <code>type</code>がパラメータ化された{@link List}でない場合は<code>null</code>を返します。
     * </p>
     *
     * @param type
     *            パラメータ化された{@link List}
     * @return パラメータ化された{@link List}の要素型
     */
    public static Type getElementTypeOfCollection(final Type type) {
        if (!isTypeOf(type, Collection.class)) {
            return null;
        }
        return getGenericParameter(type, 0);
    }

    /**
     * パラメータ化された{@link List}の要素型を返します。
     * <p>
     * <code>type</code>がパラメータ化された{@link List}でない場合は<code>null</code>を返します。
     * </p>
     *
     * @param type
     *            パラメータ化された{@link List}
     * @return パラメータ化された{@link List}の要素型
     */
    public static Type getElementTypeOfList(final Type type) {
        if (!isTypeOf(type, List.class)) {
            return null;
        }
        return getGenericParameter(type, 0);
    }

    /**
     * パラメータ化された{@link Set}の要素型を返します。
     * <p>
     * <code>type</code>がパラメータ化された{@link Set}でない場合は<code>null</code>を返します。
     * </p>
     *
     * @param type
     *            パラメータ化された{@link Set}
     * @return パラメータ化された{@link Set}の要素型
     */
    public static Type getElementTypeOfSet(final Type type) {
        if (!isTypeOf(type, Set.class)) {
            return null;
        }
        return getGenericParameter(type, 0);
    }

    /**
     * パラメータ化された{@link Map}のキーの型を返します。
     * <p>
     * <code>type</code>がパラメータ化された{@link Map}でない場合は<code>null</code>を返します。
     * </p>
     *
     * @param type
     *            パラメータ化された{@link Map}
     * @return パラメータ化された{@link Map}のキーの型
     */
    public static Type getKeyTypeOfMap(final Type type) {
        if (!isTypeOf(type, Map.class)) {
            return null;
        }
        return getGenericParameter(type, 0);
    }

    /**
     * パラメータ化された{@link Map}の値の型を返します。
     * <p>
     * <code>type</code>がパラメータ化された{@link Map}でない場合は<code>null</code>を返します。
     * </p>
     *
     * @param type
     *            パラメータ化された{@link Map}
     * @return パラメータ化された{@link Map}の値の型
     */
    public static Type getValueTypeOfMap(final Type type) {
        if (!isTypeOf(type, Map.class)) {
            return null;
        }
        return getGenericParameter(type, 1);
    }

    /**
     * パラメータ化された型(クラスまたはインタフェース)が持つ型変数をキー、型引数を値とする{@link Map}を返します。
     *
     * @param clazz
     *            パラメータ化された型(クラスまたはインタフェース)
     * @return パラメータ化された型が持つ型変数をキー、型引数を値とする{@link Map}
     */
    public static Map<TypeVariable<?>, Type> getTypeVariableMap(
            final Class<?> clazz) {
        final Map<TypeVariable<?>, Type> map = newLinkedHashMap();

        final TypeVariable<?>[] typeParameters = clazz.getTypeParameters();
        for (TypeVariable<?> typeParameter : typeParameters) {
            map.put(typeParameter, getActualClass(typeParameter.getBounds()[0],
                    map));
        }

        final Class<?> superClass = clazz.getSuperclass();
        final Type superClassType = clazz.getGenericSuperclass();
        if (superClass != null) {
            gatherTypeVariables(superClass, superClassType, map);
        }

        final Class<?>[] interfaces = clazz.getInterfaces();
        final Type[] interfaceTypes = clazz.getGenericInterfaces();
        for (int i = 0; i < interfaces.length; ++i) {
            gatherTypeVariables(interfaces[i], interfaceTypes[i], map);
        }

        return map;
    }

    /**
     * パラメータ化された型(クラスまたはインタフェース)が持つ型変数および型引数を集めて<code>map</code>に追加します。
     *
     * @param clazz
     *            クラス
     * @param type
     *            型
     * @param map
     *            パラメータ化された型が持つ型変数をキー、型引数を値とする{@link Map}
     */
    protected static void gatherTypeVariables(final Class<?> clazz,
            final Type type, final Map<TypeVariable<?>, Type> map) {
        if (clazz == null) {
            return;
        }
        gatherTypeVariables(type, map);

        final Class<?> superClass = clazz.getSuperclass();
        final Type superClassType = clazz.getGenericSuperclass();
        if (superClass != null) {
            gatherTypeVariables(superClass, superClassType, map);
        }

        final Class<?>[] interfaces = clazz.getInterfaces();
        final Type[] interfaceTypes = clazz.getGenericInterfaces();
        for (int i = 0; i < interfaces.length; ++i) {
            gatherTypeVariables(interfaces[i], interfaceTypes[i], map);
        }
    }

    /**
     * パラメータ化された型(クラスまたはインタフェース)が持つ型変数および型引数を集めて<code>map</code>に追加します。
     *
     * @param type
     *            型
     * @param map
     *            パラメータ化された型が持つ型変数をキー、型引数を値とする{@link Map}
     */
    protected static void gatherTypeVariables(final Type type,
            final Map<TypeVariable<?>, Type> map) {
        if (ParameterizedType.class.isInstance(type)) {
            final ParameterizedType parameterizedType = ParameterizedType.class
                    .cast(type);
            final TypeVariable<?>[] typeVariables = GenericDeclaration.class
                    .cast(parameterizedType.getRawType()).getTypeParameters();
            final Type[] actualTypes = parameterizedType
                    .getActualTypeArguments();
            for (int i = 0; i < actualTypes.length; ++i) {
                map.put(typeVariables[i], actualTypes[i]);
            }
        }
    }

    /**
     * <code>type</code>の実際の型を返します。
     * <ul>
     * <li><code>type</code>が<code>Class</code>の場合はそのまま返します。</li>
     * <li><code>type</code>がパラメータ化された型の場合はその原型を返します。</li>
     * <li><code>type</code>がワイルドカード型の場合は(最初の)上限境界を返します。</li>
     * <li><code>type</code>が型変数で引数{@code map}のキーとして含まれている場合はその変数の実際の型引数を返します。</li>
     * <li><code>type</code>が型変数で引数{@code map}のキーとして含まれていない場合は(最初の)上限境界を返します。</li>
     * <li><code>type</code>が配列の場合はその要素の実際の型の配列を返します。</li>
     * <li>その他の場合は<code>null</code>を返します。</li>
     * </ul>
     *
     * @param type
     *            タイプ
     * @param map
     *            パラメータ化された型が持つ型変数をキー、型引数を値とする{@link Map}
     * @return <code>type</code>の実際の型
     */
    public static Class<?> getActualClass(final Type type,
            final Map<TypeVariable<?>, Type> map) {
        if (Class.class.isInstance(type)) {
            return Class.class.cast(type);
        }
        if (ParameterizedType.class.isInstance(type)) {
            return getActualClass(ParameterizedType.class.cast(type)
                    .getRawType(), map);
        }
        if (WildcardType.class.isInstance(type)) {
            return getActualClass(WildcardType.class.cast(type)
                    .getUpperBounds()[0], map);
        }
        if (TypeVariable.class.isInstance(type)) {
            final TypeVariable<?> typeVariable = TypeVariable.class.cast(type);
            if (map.containsKey(typeVariable)) {
                return getActualClass(map.get(typeVariable), map);
            }
            return getActualClass(typeVariable.getBounds()[0], map);
        }
        if (GenericArrayType.class.isInstance(type)) {
            final GenericArrayType genericArrayType = GenericArrayType.class
                    .cast(type);
            final Class<?> componentClass = getActualClass(genericArrayType
                    .getGenericComponentType(), map);
            return Array.newInstance(componentClass, 0).getClass();
        }
        return null;
    }

    /**
     * パラメータ化された型を要素とする配列の実際の要素型を返します。
     * <ul>
     * <li><code>type</code>がパラメータ化された型の配列でない場合は<code>null</code>を返します。</li>
     * <li><code>type</code>が<code>Class</code>の場合はそのまま返します。</li>
     * <li><code>type</code>がパラメータ化された型の場合はその原型を返します。</li>
     * <li><code>type</code>がワイルドカード型の場合は(最初の)上限境界を返します。</li>
     * <li><code>type</code>が型変数の場合はその変数の実際の型引数を返します。</li>
     * <li><code>type</code>が配列の場合はその要素の実際の型の配列を返します。</li>
     * <li>その他の場合は<code>null</code>を返します。</li>
     * </ul>
     *
     * @param type
     *            パラメータ化された型を要素とする配列
     * @param map
     *            パラメータ化された型が持つ型変数をキー、型引数を値とする{@link Map}
     * @return パラメータ化された型を要素とする配列の実際の要素型
     */
    public static Class<?> getActualElementClassOfArray(final Type type,
            final Map<TypeVariable<?>, Type> map) {
        if (!GenericArrayType.class.isInstance(type)) {
            return null;
        }
        return getActualClass(GenericArrayType.class.cast(type)
                .getGenericComponentType(), map);
    }

    /**
     * パラメータ化された{@link Collection}の実際の要素型を返します。
     * <ul>
     * <li><code>type</code>がパラメータ化された{@link Collection}でない場合は<code>null</code>
     * を返します。</li>
     * <li><code>type</code>が<code>Class</code>の場合はそのまま返します。</li>
     * <li><code>type</code>がパラメータ化された型の場合はその原型を返します。</li>
     * <li><code>type</code>がワイルドカード型の場合は(最初の)上限境界を返します。</li>
     * <li><code>type</code>が型変数の場合はその変数の実際の型引数を返します。</li>
     * <li><code>type</code>が配列の場合はその要素の実際の型の配列を返します。</li>
     * <li>その他の場合は<code>null</code>を返します。</li>
     * </ul>
     *
     * @param type
     *            パラメータ化された{@link Collection}
     * @param map
     *            パラメータ化された型が持つ型変数をキー、型引数を値とする{@link Map}
     * @return パラメータ化された{@link Collection}の実際の要素型
     */
    public static Class<?> getActualElementClassOfCollection(final Type type,
            final Map<TypeVariable<?>, Type> map) {
        if (!isTypeOf(type, Collection.class)) {
            return null;
        }
        return getActualClass(getGenericParameter(type, 0), map);
    }

    /**
     * パラメータ化された{@link List}の実際の要素型を返します。
     * <ul>
     * <li><code>type</code>がパラメータ化された{@link List}でない場合は<code>null</code>を返します。</li>
     * <li><code>type</code>が<code>Class</code>の場合はそのまま返します。</li>
     * <li><code>type</code>がパラメータ化された型の場合はその原型を返します。</li>
     * <li><code>type</code>がワイルドカード型の場合は(最初の)上限境界を返します。</li>
     * <li><code>type</code>が型変数の場合はその変数の実際の型引数を返します。</li>
     * <li><code>type</code>が配列の場合はその要素の実際の型の配列を返します。</li>
     * <li>その他の場合は<code>null</code>を返します。</li>
     * </ul>
     *
     * @param type
     *            パラメータ化された{@link List}
     * @param map
     *            パラメータ化された型が持つ型変数をキー、型引数を値とする{@link Map}
     * @return パラメータ化された{@link List}の実際の要素型
     */
    public static Class<?> getActualElementClassOfList(final Type type,
            final Map<TypeVariable<?>, Type> map) {
        if (!isTypeOf(type, List.class)) {
            return null;
        }
        return getActualClass(getGenericParameter(type, 0), map);
    }

    /**
     * パラメータ化された{@link Set}の実際の要素型を返します。
     * <ul>
     * <li><code>type</code>がパラメータ化された{@link Set}でない場合は<code>null</code>を返します。</li>
     * <li><code>type</code>が<code>Class</code>の場合はそのまま返します。</li>
     * <li><code>type</code>がパラメータ化された型の場合はその原型を返します。</li>
     * <li><code>type</code>がワイルドカード型の場合は(最初の)上限境界を返します。</li>
     * <li><code>type</code>が型変数の場合はその変数の実際の型引数を返します。</li>
     * <li><code>type</code>が配列の場合はその要素の実際の型の配列を返します。</li>
     * <li>その他の場合は<code>null</code>を返します。</li>
     * </ul>
     *
     * @param type
     *            パラメータ化された{@link Set}
     * @param map
     *            パラメータ化された型が持つ型変数をキー、型引数を値とする{@link Map}
     * @return パラメータ化された{@link Set}の実際の要素型
     */
    public static Class<?> getActualElementClassOfSet(final Type type,
            final Map<TypeVariable<?>, Type> map) {
        if (!isTypeOf(type, Set.class)) {
            return null;
        }
        return getActualClass(getGenericParameter(type, 0), map);
    }

    /**
     * パラメータ化された{@link Map}のキーの実際の型を返します。
     * <ul>
     * <li>キー型がパラメータ化された{@link Map}でない場合は<code>null</code>を返します。</li>
     * <li><code>type</code>が<code>Class</code>の場合はそのまま返します。</li>
     * <li><code>type</code>がパラメータ化された型の場合はその原型を返します。</li>
     * <li><code>type</code>がワイルドカード型の場合は(最初の)上限境界を返します。</li>
     * <li><code>type</code>が型変数の場合はその変数の実際の型引数を返します。</li>
     * <li><code>type</code>が配列の場合はその要素の実際の型の配列を返します。</li>
     * <li>その他の場合は<code>null</code>を返します。</li>
     * </ul>
     *
     * @param type
     *            パラメータ化された{@link Map}
     * @param map
     *            パラメータ化された型が持つ型変数をキー、型引数を値とする{@link Map}
     * @return パラメータ化された{@link Map}のキーの実際の型
     */
    public static Class<?> getActualKeyClassOfMap(final Type type,
            final Map<TypeVariable<?>, Type> map) {
        if (!isTypeOf(type, Map.class)) {
            return null;
        }
        return getActualClass(getGenericParameter(type, 0), map);
    }

    /**
     * パラメータ化された{@link Map}の値の実際の型を返します。
     * <ul>
     * <li><code>type</code>がパラメータ化された{@link Map}でない場合は<code>null</code>を返します。</li>
     * <li><code>type</code>が<code>Class</code>の場合はそのまま返します。</li>
     * <li><code>type</code>がパラメータ化された型の場合はその原型を返します。</li>
     * <li><code>type</code>がワイルドカード型の場合は(最初の)上限境界を返します。</li>
     * <li><code>type</code>が型変数の場合はその変数の実際の型引数を返します。</li>
     * <li><code>type</code>が配列の場合はその要素の実際の型の配列を返します。</li>
     * <li>その他の場合は<code>null</code>を返します。</li>
     * </ul>
     *
     * @param type
     *            パラメータ化された{@link Map}
     * @param map
     *            パラメータ化された型が持つ型変数をキー、型引数を値とする{@link Map}
     * @return パラメータ化された{@link Map}の値の実際の型
     */
    public static Class<?> getActualValueClassOfMap(final Type type,
            final Map<TypeVariable<?>, Type> map) {
        if (!isTypeOf(type, Map.class)) {
            return null;
        }
        return getActualClass(getGenericParameter(type, 1), map);
    }
}
