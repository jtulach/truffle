/*
 * Copyright (c) 2012, 2015, Oracle and/or its affiliates. All rights reserved.
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

import static org.junit.Assert.assertNotNull;

import org.junit.Test;

import com.oracle.truffle.api.source.Source;
import com.oracle.truffle.api.source.SourceSection;
import com.oracle.truffle.api.test.vm.ExceptionLanguage.Exception1;
import com.oracle.truffle.api.vm.LanguageException;

import com.oracle.truffle.api.vm.PolyglotEngine;
import java.util.HashSet;
import java.util.Set;
import org.junit.After;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class ExceptionTest {
    private Set<PolyglotEngine> toDispose = new HashSet<>();

    protected PolyglotEngine.Builder createBuilder() {
        return PolyglotEngine.newBuilder();
    }

    private PolyglotEngine register(PolyglotEngine engine) {
        toDispose.add(engine);
        return engine;
    }

    @After
    public void dispose() {
        for (PolyglotEngine engine : toDispose) {
            engine.dispose();
        }
    }

    @Test
    public void throwsExceptionWithSourceSection() throws Exception {
        PolyglotEngine tvm = createBuilder().build();
        register(tvm);

        PolyglotEngine.Language exceptionLanguage = tvm.getLanguages().get(ExceptionLanguage.MIME);
        assertNotNull("Exception language found", exceptionLanguage);

        // @formatter:off
        final Source src = Source.newBuilder(
            "throw=Exception1\n" +
            "from=6\n" +
            "to=16\n"
        ).name("throw.exp").mimeType(ExceptionLanguage.MIME).build();
        // @formatter:on

        try {
            PolyglotEngine.Value value = exceptionLanguage.eval(src);
            fail("Should throw an exception, but returned " + value);
        } catch (LanguageException ex) {
            SourceSection section = ex.getSourceSection();
            Throwable cause = ex.getCause();

            assertTrue("Exception one thrown originally: " + cause, cause instanceof Exception1);
            assertNotNull("Source section is provided", section);
            assertEquals("Text is correct", "Exception1", section.getCode());
        }
    }
}
