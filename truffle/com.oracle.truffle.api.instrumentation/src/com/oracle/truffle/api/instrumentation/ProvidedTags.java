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

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.oracle.truffle.api.TruffleLanguage;
import com.oracle.truffle.api.nodes.Node;

/**
 * Specifies a set of tags that are provided by a {@link TruffleLanguage language} implementation.
 * If a {@link TruffleLanguage language} does not provide all tags an {@link TruffleInstrument
 * instrument} {@link RequiredTags requires} then language and instrument are considered
 * incompatible. Instrumentations applied to languages by instruments which are incompatible to the
 * language have no effect.
 * <p>
 * Tags are used by languages to indicate that a {@link Node node} is a member of a certain category
 * of nodes. For example a debugger {@link TruffleInstrument instrument} might require a guest
 * language to tag all nodes as halt locations that should be considered as such. The full set of
 * tags {@link RequiredTags required} by an {@link TruffleInstrument instrument} to be functional is
 * defined by the instrument implementation. The set of {@link RequiredTags required} tags might
 * overlap between instrument implementations.
 *
 * @since 0.12
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
public @interface ProvidedTags {

    /**
     * @return the set of tags that are provided by the {@link TruffleLanguage language}
     *         implementation.
     * @see ProvidedTags class documentation for further details
     * @since 0.12
     */
    String[] value();

}
