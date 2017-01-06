/*
 * Copyright (c) 2015, 2016, Oracle and/or its affiliates. All rights reserved.
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
package com.oracle.truffle.api.test.vm;

import com.oracle.truffle.api.CallTarget;
import com.oracle.truffle.api.Truffle;
import com.oracle.truffle.api.TruffleLanguage;
import com.oracle.truffle.api.TruffleLanguage.Env;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.nodes.RootNode;
import com.oracle.truffle.api.source.Source;
import com.oracle.truffle.api.source.SourceSection;
import java.io.IOException;
import java.io.StringReader;
import java.util.Properties;

@TruffleLanguage.Registration(name = "Exception", mimeType = ExceptionLanguage.MIME, version = "1.0")
public class ExceptionLanguage extends TruffleLanguage<Env> {
    public static final String MIME = "application/x-test-exception";
    public static final ExceptionLanguage INSTANCE = new ExceptionLanguage();

    @Override
    protected Env createContext(Env env) {
        return env;
    }

    @Override
    protected CallTarget parse(ParsingRequest env) {
        return Truffle.getRuntime().createCallTarget(new ExceptionNode(this, env.getSource()));
    }

    @Override
    protected Object findExportedSymbol(Env context, String globalName, boolean onlyExplicit) {
        return null;
    }

    @Override
    protected Object getLanguageGlobal(Env context) {
        return null;
    }

    @Override
    protected boolean isObjectOfLanguage(Object object) {
        if (object instanceof Exception1) {
            return true;
        }
        return false;
    }

    @Override
    protected SourceSection findSourceLocation(Env context, Object value) {
        if (value instanceof Exception1) {
            return ((Exception1) value).ss;
        }
        if (value instanceof Exception2) {
            return ((Exception2) value).ss;
        }
        return null;
    }

    private static class ExceptionNode extends RootNode {
        private static int counter;
        private final Source code;
        private final int id;

        ExceptionNode(ExceptionLanguage hash, Source code) {
            super(hash.getClass(), null, null);
            this.code = code;
            id = ++counter;
        }

        @Override
        public Object execute(VirtualFrame frame) {
            Properties p = new Properties();
            try {
                p.load(new StringReader(code.getCode()));
            } catch (IOException ex) {
                throw new IllegalStateException(ex);
            }
            final String msg = p.getProperty("message");
            final int from = Integer.parseInt(p.getProperty("from"));
            final int to = Integer.parseInt(p.getProperty("to"));
            final SourceSection ss = code.createSection(from, to - from);
            if ("Exception1".equals(p.getProperty("throw"))) {
                throw new Exception1(msg, ss);
            }
            return new UnsupportedOperationException(code.getCode());
        }
    }

    static final class Exception1 extends RuntimeException {
        static final long serialVersionUID = 1L;

        private final SourceSection ss;

        Exception1(String message, SourceSection ss) {
            super(message);
            this.ss = ss;
        }

    }

    static final class Exception2 extends RuntimeException {
        static final long serialVersionUID = 1L;

        private final SourceSection ss;

        Exception2(String message, SourceSection ss) {
            super(message);
            this.ss = ss;
        }

    }

    static final class Exception3 extends RuntimeException {
        static final long serialVersionUID = 1L;

        private final SourceSection ss;

        Exception3(String message, SourceSection ss) {
            super(message);
            this.ss = ss;
        }

    }

}
