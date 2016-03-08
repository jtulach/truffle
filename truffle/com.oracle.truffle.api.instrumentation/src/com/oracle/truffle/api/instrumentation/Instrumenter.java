/*
 * Copyright (c) 2016, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the LICENSE file that accompanied this code.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */
package com.oracle.truffle.api.instrumentation;

import com.oracle.truffle.api.TruffleLanguage;
import com.oracle.truffle.api.nodes.Node;
import com.oracle.truffle.api.nodes.RootNode;

/**
 * Provides the capabilities to attach {@link ExecutionEventNodeFactory} and
 * {@link ExecutionEventListener} instances for a set of source locations specified by a
 * {@link SourceSectionFilter}. The result of an attachment is a {@link EventBinding binding}.
 *
 * @see #attachFactory(SourceSectionFilter, ExecutionEventNodeFactory)
 * @see #attachListener(SourceSectionFilter, ExecutionEventListener)
 * @since 0.12
 */
public abstract class Instrumenter {

    Instrumenter() {
    }

    /**
     * Starts event notification for a given {@link ExecutionEventNodeFactory factory} and returns a
     * {@link EventBinding binding} which represents a handle to dispose the notification.
     *
     * @since 0.12
     * @throws IllegalArgumentException if the given filter is not compatible. For example if the
     *             filter refers to tags which were not declared using {@link RequiredTags}.
     */
    public abstract <T extends ExecutionEventNodeFactory> EventBinding<T> attachFactory(SourceSectionFilter filter, T factory);

    /**
     * Starts event notification for a given {@link ExecutionEventListener listener} and returns a
     * {@link EventBinding binding} which represents a handle to dispose the notification.
     *
     * @since 0.12
     * @throws IllegalArgumentException if the given filter is not compatible. For example if the
     *             filter refers to tags which were not declared using {@link RequiredTags}.
     */
    public abstract <T extends ExecutionEventListener> EventBinding<T> attachListener(SourceSectionFilter filter, T listener);

    /**
     * Returns <code>true</code> if the given node is tagged with a given tag. The given tag must be
     * interned or in other words equal to <code>tag.intern()</code>. If the instrumenter is used as
     * a {@link TruffleLanguage} then only nodes can be queried for tags that are associated with
     * the current language otherwise an {@link IllegalArgumentException} is thrown. The given node
     * and tag must not be <code>null</code>. If the given node is not instrumentable, the given
     * node is not yet adopted by a {@link RootNode} or the given tag was not {@link ProvidedTags
     * provided} by the language then always <code>false</code> is returned.
     *
     * @param node the node to check
     * @param tag the interned tag to check
     * @return <code>true</code> if the node is tagged with the given tag.
     * @since 0.12
     */
    public abstract boolean isNodeTaggedWith(Node node, String tag);

}
