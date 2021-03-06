/*
 * Copyright 2013-2015 the Uranoplums Foundation and the Others.
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
 * $Id: UraCollectionUtilsTest.java$
 */
package org.uranoplums.typical.collection;

import static org.junit.Assert.*;
import static org.uranoplums.typical.collection.factory.UraListFactory.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.uranoplums.typical.collection.UraCollectionUtils.UraUnmodifiableChange;


/**
 * UraCollectionUtilsTestクラス。<br>
 *
 * @since 2015/11/02
 * @author syany
 */
public class UraCollectionUtilsTest {
    List<String> list;
    List<String> listDuplex;
    List<String> listUnique;

    /**
     * 。<br>
     * @throws java.lang.Exception
     */
    @BeforeClass
    public static void setUpBeforeClass() throws Exception {}

    /**
     * 。<br>
     * @throws java.lang.Exception
     */
    @AfterClass
    public static void tearDownAfterClass() throws Exception {}

    /**
     * 。<br>
     * @throws java.lang.Exception
     */
    @Before
    public void setUp() throws Exception {
        list = newArrayList(16);
        listDuplex = newArrayList(16);
        listUnique = newArrayList(16);
        list.add("acb");
        list.add("acb1");
        list.add("acb2");
        list.add("acb3");
        list.add("acb4");
        list.add("acb5");
        list.add("acb2");
        list.add("acb3");
        list.add("acb4");
        list.add("acb5");
        list.add("acb");
        listDuplex.add("acb2");
        listDuplex.add("acb3");
        listDuplex.add("acb4");
        listDuplex.add("acb5");
        listDuplex.add("acb");
        listUnique.add("acb");
        listUnique.add("acb1");
        listUnique.add("acb2");
        listUnique.add("acb3");
        listUnique.add("acb4");
        listUnique.add("acb5");
    }

    /**
     * 。<br>
     * @throws java.lang.Exception
     */
    @After
    public void tearDown() throws Exception {}

    /**
     * {@link org.uranoplums.typical.collection.UraCollectionUtils#isDuplicate(java.util.Collection)} のためのテスト・メソッド。
     */
    @Test
    public final void testIsDuplicate() {
        assertTrue(UraCollectionUtils.isDuplicate(list));
    }

    /**
     * {@link org.uranoplums.typical.collection.UraCollectionUtils#isUnique(java.util.Collection)} のためのテスト・メソッド。
     */
    @Test
    public final void testIsUnique() {
        assertTrue(UraCollectionUtils.isUnique(listUnique));
    }

    /**
     * {@link org.uranoplums.typical.collection.UraCollectionUtils#getDuplicateKeys(java.util.Collection)} のためのテスト・メソッド。
     */
    @Test
    public final void testGetDuplicateList() {
        assertEquals(UraListUtils.getDuplicateList(list), this.listDuplex);
    }
    @Test
    public final void testGetDuplicateSet() {
        Set<String> actualSet = new HashSet<String>(this.listDuplex);
        assertEquals(UraSetUtils.getDuplicateSet(list), actualSet);
    }

    /**
     * {@link org.uranoplums.typical.collection.UraCollectionUtils#getUnique(java.util.Collection)} のためのテスト・メソッド。
     */
    @Test
    public final void testGetUnique() {
        List<String> exList = newArrayList(UraCollectionUtils.getUnique(list));
        assertEquals(exList, this.listUnique);
    }

    /**
     * {@link org.uranoplums.typical.collection.UraCollectionUtils#getUniqueList(java.util.Collection)} のためのテスト・メソッド。
     */
    @Test
    public final void testGetUniqueList() {
        assertEquals(UraListUtils.getUniqueList(list), this.listUnique);
    }

    /**
     * {@link org.uranoplums.typical.collection.UraCollectionUtils#addUnique(java.util.Collection, java.util.Collection)} のためのテスト・メソッド。
     */
    @Test
    public final void testAddUnique() {
        List<String> exList = newArrayList();
        assertEquals(UraCollectionUtils.addUnique(list, exList), this.listUnique);

    }
    /**
     * {@link org.uranoplums.typical.collection.UraCollectionUtils#addUnique(java.util.Collection, java.util.Collection)} のためのテスト・メソッド。
     */
    @Test
    public final void testAddUnique02() {
        Set<String> exSet = new HashSet<String>();
        Set<String> actualSet = new HashSet<String>(this.listUnique);
        assertEquals(UraCollectionUtils.addUnique(list, exSet), actualSet);

    }

    @Test(expected=UnsupportedOperationException.class)
    public final void testUnmodified01() {
        Map<String, List<Map<String, String>>> ext = new HashMap<String, List<Map<String, String>>>();

        List<Map<String, String>> entryList01 = new ArrayList<Map<String, String>>();
        Map<String, String> entryMap01 = new HashMap<String, String>();
        entryMap01.put("k01::01", "val01::01");
        entryMap01.put("k01::02", "val01::02");
        entryList01.add(entryMap01);
        Map<String, String> entryMap02 = new HashMap<String, String>();
        entryMap02.put("k02::01", "val02::01");
        List<Map<String, String>> entryList02 = new ArrayList<Map<String, String>>();
        entryList02.add(entryMap02);
        ext.put("e01", entryList01);
        ext.put("e02", entryList02);
        ext = UraCollectionUtils.deepUnmodifiableMap(ext);
        for (final List<Map<String, String>> list : ext.values()) {
                System.out.println("xx class : " + list.getClass().getName());
            for (final Map<String, String> map : list) {
                    System.out.println("xx class : " + map.getClass().getName());
                    map.put("oo", "pp"); //force exception
            }
        }

        fail("トウタツシマセン");
    }
    @Test(expected=UnsupportedOperationException.class)
    public void testUnmodified02() throws Exception {
//        Integer i = 0;
        Map<String, UraTuple3<String,String,String>> map = new HashMap<String, UraTuple3<String,String,String>>();
        UraTuple3<String,String,String> t1 = UraTuple3.tuple3("tt", "oo", "yy");
        UraTuple3<String,String,String> t2 = UraTuple3.tuple3("tt", "oo", "yy");
        map.put("k1", t1);
        map.put("k2", t2);
        System.out.println(t1.getClass().getName() + "(before) : "+ t1.getValue1());
//        t1.setValue1("zzzz");
        t1.value1 = "zzzz";
        System.out.println(t1.getClass().getName() + "(after) : "+ t1.getValue1());
        map = UraCollectionUtils.deepUnmodifiableMap(map, new UneditAna());
        for (final UraTuple3<String,String,String> v : map.values()) {
            System.out.println(v.getClass().getName() + " : "+ v.getValue1());
            ((UneditableTaple3) v).setValue1("hh");
        }
        fail("トウタツシマセン");
    }
}

class UneditAna implements UraUnmodifiableChange {
    /* (非 Javadoc)
     * @see org.uranoplums.typical.collection.UraCollectionUtils.UraUnmodifiableChange#transfer(java.lang.Object)
     */
    @SuppressWarnings ("unchecked")
    @Override
    public <E> E transfer(E source) {
        if (source instanceof UraTuple3) {
            UraTuple3<?, ?, ?> t = UraTuple3.class.cast(source);
            source = (E) new UneditableTaple3(t.getValue1(), t.getValue2(), t.getValue3());
        }
        return source;
    }

}

class UneditableTaple3<T1, T2, T3> extends UraTuple3<T1, T2, T3> {

    /**  */
    private static final long serialVersionUID = 1L;


    /**
     * デフォルトコンストラクタ。<br>
     * @param value1
     * @param value2
     * @param value3
     */
    public UneditableTaple3(T1 value1, T2 value2, T3 value3) {
        super(value1, value2, value3);
    }

    /* (非 Javadoc)
     * @see org.uranoplums.typical.collection.UraTuple3#setValue1(java.lang.Object)
     */
//    @Override
    public void setValue1(T1 value1) {
        throw new UnsupportedOperationException();
    }

    /* (非 Javadoc)
     * @see org.uranoplums.typical.collection.UraTuple3#setValue2(java.lang.Object)
     */
//    @Override
    public void setValue2(T2 value2) {
        throw new UnsupportedOperationException();
    }

    /* (非 Javadoc)
     * @see org.uranoplums.typical.collection.UraTuple3#setValue3(java.lang.Object)
     */
//    @Override
    public void setValue3(T3 value3) {
        throw new UnsupportedOperationException();
    }

}
