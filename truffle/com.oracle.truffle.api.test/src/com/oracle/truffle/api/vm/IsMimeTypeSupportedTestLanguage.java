/*
 * Copyright (c) 2016, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.
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

import java.io.IOException;

import com.oracle.truffle.api.CallTarget;
import com.oracle.truffle.api.TruffleLanguage;
import com.oracle.truffle.api.TruffleLanguage.Env;

@TruffleLanguage.Registration(name = "Hash", mimeType = "application/x-test-mime-type-supported", version = "1.0")
public class IsMimeTypeSupportedTestLanguage extends TruffleLanguage<Env> {

    public static final IsMimeTypeSupportedTestLanguage INSTANCE = new IsMimeTypeSupportedTestLanguage();

    @Override
    protected Env createContext(com.oracle.truffle.api.TruffleLanguage.Env env) {
        return env;
    }

    @Override
    protected CallTarget parse(ParsingRequest<Env> env) throws IOException {
        final String mimeType = env.getSource().getCode();
        final SharedEnv<Env> contextRef = env.getSharedEnv();
        return new CallTarget() {
            @Override
            public Object call(Object... arguments) {
                return contextRef.getContext().isMimeTypeSupported(mimeType);
            }
        };
    }

    @Override
    protected Object findExportedSymbol(Env context, String globalName, boolean onlyExplicit) {
        throw new UnsupportedOperationException();
    }

    @Override
    protected Object getLanguageGlobal(Env context) {
        throw new UnsupportedOperationException();
    }

    @Override
    protected boolean isObjectOfLanguage(Object object) {
        throw new UnsupportedOperationException();
    }
}
