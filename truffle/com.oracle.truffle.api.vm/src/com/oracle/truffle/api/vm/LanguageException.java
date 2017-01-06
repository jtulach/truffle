/*
 * Copyright (c) 2015, 2016, Oracle and/or its affiliates. All rights reserved.
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
package com.oracle.truffle.api.vm;

import com.oracle.truffle.api.TruffleLanguage;
import com.oracle.truffle.api.source.Source;
import com.oracle.truffle.api.source.SourceSection;
import java.util.Objects;

/**
 * Universal exception when dealing with
 * <q>user script errors</q> when calling {@link PolyglotEngine} and related methods. Any
 * <q>user error</q> - e.g. error caused by mistakes in {@link Source code written} by end user and
 * {@link PolyglotEngine#eval(com.oracle.truffle.api.source.Source) evaluated} by the
 * {@link PolyglotEngine engine} is signaled by this exception. When writing robust code, catch
 * {@link LanguageException} when calling for example
 * {@link PolyglotEngine.Value#execute(java.lang.Object...)} and preform correct error recovery.
 *
 *
 * <h3>User Error Handling</h3>
 *
 * The {@link TruffleLanguage languages} in Truffle system are supposed to notify errors in user
 * scripts by throwing their own messages that are associated with {@link SourceSection source
 * section} identifying the error location. This is done by making sure that such language properly
 * responds to {@link TruffleLanguage#findSourceLocation(java.lang.Object, java.lang.Object)} call
 * for its own user exception instances by returning appropriate {@link SourceSection} object. If
 * such exception is detected by the {@link PolyglotEngine engine infrastructure}, it is wrapped
 * into an instance of {@link LanguageException} - as such the end user code, can only catch
 * {@link LanguageException} - the language specific exceptions will be {@link Throwable#getCause()
 * wrapped} for the caller's convinience.
 *
 * 
 * @since 0.22
 */
public final class LanguageException extends RuntimeException {
    static final long serialVersionUID = 1L;
    private final SourceSection section;

    LanguageException(String msg, SourceSection section, Throwable cause) {
        super(msg, cause);
        Objects.nonNull(section);
        this.section = section;
    }

    /**
     * Location where the error occured. The source section identifies the end user code - in a
     * {@link Source script} - that is responsible for the error in the script execution.
     *
     * @return the section where the error occured, never <code>null</code>
     * @since 0.22
     */
    public SourceSection getSourceSection() {
        return section;
    }

}
