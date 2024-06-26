/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.commons.numbers.examples.jmh.arrays;

/**
 * A cache of pivot indices used for partitioning an array into multiple regions.
 *
 * <p>A pivot is an index position that contains a value equal to the value in a fully
 * sorted array.
 *
 * <p>For a pivot {@code p}:
 *
 * <pre>{@code
 * i < p < j
 * data[i] <= data[p] <= data[j]
 * }</pre>
 *
 * <p>Partitioning moves data in a range {@code [lower, upper]} so that the pivot
 * {@code p} partitions the data. During this process many pivots may be found
 * before the search ends. A pivot cache supports storing these pivots so that
 * they can be used to bracket further searches in the array.
 *
 * <p>A pivot cache supports finding a search bracket to partition an index {@code k}
 * within an array of length {@code n}. The bracket should be an enclosing bound
 * of known pivots. All data can be rearranged within this bracket without destroying
 * other regions of the partitioned array. The support for {@code k} is provided within
 * an inclusive range {@code [left, right]} where {@code 0 <= left <= right < n}.
 * Thus {@code [left, right]} denotes the region containing all target indices {@code k}
 * for multi-region partitioning.
 *
 * <p>The cache provides the following functionality:
 *
 * <ul>
 * <li>Test if an index {@code [left <= k <= right]} is a known pivot.
 * <li>Return a {@code lower} bounding pivot for a partition of an index {@code [left <= k <= right]}.
 * <li>Return an {@code upper} bounding pivot for a partition of an index {@code [left <= k <= right]}.
 * </ul>
 *
 * <p>Note that searching with the bound {@code [lower, upper]} will reorder data
 * and pivots within this range may be invalidated by moving of data. To prevent
 * error the bound provided by a cache must use the closest bracketing pivots.
 *
 * <p>At least two strategies can be used:
 *
 * <ol>
 * <li>Process {@code k} indices in any order. Store all pivots during the partitioning.
 * Each subsequent search after the first can use adjacent pivots to bracket the search.
 * <li>Process {@code k} indices in sorted order. The {@code lower} bound for {@code k+1}
 * will be {@code k <= lower}. This does not require a cache as {@code upper} can be set
 * using the end of the data {@code n}. For this case a cache can store pivots which can
 * be used to bracket the search for {@code k+1}.
 * </ol>
 *
 * <p>Implementations may assume indices are positive.
 *
 * @since 1.2
 */
interface PivotCache extends PivotStore {
    /**
     * The start (inclusive) of the range of indices supported.
     *
     * @return start of the supported range
     */
    int left();

    /**
     * The end (inclusive) of the range of indices supported.
     *
     * @return end of the supported range
     */
    int right();

    /**
     * Returns {@code true} if the cache supports storing some of the pivots in the supported
     * range. A sparse cache can provide approximate bounds for partitioning. These bounds may be
     * smaller than using the bounds of the entire array. Note that partitioning may destroy
     * previous pivots within a range. Thus a sparse cache should be used to partition indices
     * in sorted order so that bounds generated by each iteration do not overlap the bounds
     * of a previous partition. This can be done by using the previous {@code k} as the left
     * bound.
     *
     * <p>A sparse cache can be created to store 1 pivot between all {@code k} of interest
     * after the first {@code k}, and optionally two pivots that bracket the entire supported
     * range. In the following example the partition of {@code k1} stores pivots {@code p}.
     * These can be used to bracket {@code k2, k3}. An alternative scheme
     * where no pivots are stored is shown for comparison:
     *
     * <pre>
     * Partition:
     * 0------k1----------k2------k3---------N
     *
     * Iteration 1:
     * 0------k1------p--------p---------p---N
     *
     * Iteration 2:
     *                l---k2---r
     * or:    l-----------k2-----------------N
     *
     * Iteration 3:
     *                         l--k3-----r
     * or:                l-------k3---------N
     * </pre>
     *
     * <p>If false then the cache will store all pivots within the supported range and
     * ideally provide the closest bounding pivot around the supported range.
     *
     * @return true if sparse
     */
    boolean sparse();

    /**
     * Test if the index {@code k} is a pivot.
     *
     * <p><em>If {@code index < left} or {@code index > right} the behavior is not
     * defined.</em></p>
     *
     * @param k Index.
     * @return true if the index is a pivot within the supported range
     */
    boolean contains(int k);

    /**
     * Returns the nearest pivot index that occurs on or before the specified starting
     * index. If none exist then {@code -1} is returned.
     *
     * @param k Index to start checking from (inclusive).
     * @return the index of the previous pivot, or {@code -1} if there is no index
     */
    int previousPivot(int k);

    /**
     * Returns the nearest pivot index that occurs on or after the specified starting
     * index. If none exist then {@code -1} is returned.
     *
     * @param k Index to start checking from (inclusive).
     * @return the index of the next pivot, or {@code -1} if there is no index
     */
    default int nextPivot(int k) {
        return nextPivotOrElse(k, -1);
    }

    /**
     * Returns the nearest pivot index that occurs on or after the specified starting
     * index. If none exist then {@code other} is returned.
     *
     * @param k Index to start checking from (inclusive).
     * @param other Other value.
     * @return the index of the next pivot, or {@code other} if there is no index
     */
    int nextPivotOrElse(int k, int other);
}
