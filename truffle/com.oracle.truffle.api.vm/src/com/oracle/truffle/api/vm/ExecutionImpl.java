/*
 * Copyright (c) 2014, 2016, Oracle and/or its affiliates. All rights reserved.
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
import java.util.Objects;

final class ExecutionImpl {
    public static ContextStore createStore(PolyglotEngine vm) {
        return new ContextStore(vm, 4);
    }

    public static ContextStore executionStarted(ContextStoreProfile profile, ContextStore context) {
        Object vm = context.vm;
        Objects.requireNonNull(vm);
        profile.enter(context);
        return null;
    }

    @SuppressWarnings("unused")
    public static void executionEnded(ContextStore prev) {
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    static <C> C findContext(Object currentVM, Class<? extends TruffleLanguage> type) {
        PolyglotEngine vm = (PolyglotEngine) currentVM;
        TruffleLanguage.Env env = vm.findEnv(type);
        return (C) PolyglotEngine.Access.LANGS.findContext(env);
    }

    public static Object findVM() {
        return null;
    }
}
