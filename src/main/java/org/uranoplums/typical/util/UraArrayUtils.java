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
 * $Id: UraArrayUtils.java$
 */
package org.uranoplums.typical.util;

import java.util.Arrays;
import java.util.Comparator;

import org.apache.commons.collections4.comparators.ComparatorChain;


/**
 * UraArrayUtilsクラス。<br>
 *
 * @since 2015/11/28
 * @author syany
 */
public class UraArrayUtils extends UraUtils {
    public static final int[] ensureCapacityInt(int actSize, int... elementData) {
        int nowSize = elementData.length;
        if (actSize > nowSize) {
//            int j = nowSize * 3 / 2 + 1;
//            if (j < actSize) {
//                j = actSize;
//            }
//            elementData = Arrays.copyOf(elementData, j);
            elementData = Arrays.copyOf(elementData, actSize);
        }
        return elementData;
    }

    /**
     * 。<br>
     * @param array
     * @param comparators
     */
    public static final <E> void chainSort(E[] array, Comparator<Object>... comparators) {
        ComparatorChain<Object> cChain = new ComparatorChain<Object>();
        for (final Comparator<Object> c : comparators) {
            cChain.addComparator(c);
        }
        Arrays.sort(array, cChain);
    }
    /**
     * 。<br>
     * @param array
     * @param fromIndex
     * @param toIndex
     * @param comparators
     */
    public static final <E> void chainSort(E[] array, int fromIndex, int toIndex, Comparator<Object>... comparators) {
        ComparatorChain<Object> cChain = new ComparatorChain<Object>();
        for (final Comparator<Object> c : comparators) {
            cChain.addComparator(c);
        }
        Arrays.sort(array, fromIndex, toIndex, cChain);
    }

    /**
     * 。<br>
     * @param array
     * @param fromIndex
     * @param comparators
     */
    public static final <E> void chainSort(E[] array, int fromIndex, Comparator<Object>... comparators) {
        chainSort(array, fromIndex, array.length -1, comparators);
    }
}
