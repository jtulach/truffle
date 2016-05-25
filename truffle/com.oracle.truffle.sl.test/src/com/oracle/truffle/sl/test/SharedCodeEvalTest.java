/*
 * Copyright (c) 2012, 2013, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * The Universal Permissive License (UPL), Version 1.0
 *
 * Subject to the condition set forth below, permission is hereby granted to any
 * person obtaining a copy of this software, associated documentation and/or
 * data (collectively the "Software"), free of charge and under any and all
 * copyright rights in the Software, and any and all patent rights owned or
 * freely licensable by each licensor hereunder covering either (i) the
 * unmodified Software as contributed to or provided by such licensor, or (ii)
 * the Larger Works (as defined below), to deal in both
 *
 * (a) the Software, and
 *
 * (b) any piece of software and/or hardware listed in the lrgrwrks.txt file if
 * one is included with the Software each a "Larger Work" to which the Software
 * is contributed by such licensors),
 *
 * without restriction, including without limitation the rights to copy, create
 * derivative works of, display, perform, and distribute the Software and make,
 * use, sell, offer for sale, import, export, have made, and have sold the
 * Software and the Larger Work(s), and to sublicense the foregoing rights on
 * either these or other terms.
 *
 * This license is subject to the following condition:
 *
 * The above copyright notice and either this complete permission notice or at a
 * minimum a reference to the UPL must be included in all copies or substantial
 * portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package com.oracle.truffle.sl.test;

import com.oracle.truffle.api.source.Source;
import com.oracle.truffle.api.vm.PolyglotEngine;
import com.oracle.truffle.sl.runtime.SLFunction;
import static org.junit.Assert.assertEquals;
import org.junit.Test;

public class SharedCodeEvalTest {
    @Test
    public void twoEnginesShareTheirCode() throws Exception {
        PolyglotEngine.Builder builder = PolyglotEngine.newBuilder();

        PolyglotEngine engine1 = builder.build();
        PolyglotEngine engine2 = builder.build();

        Source sharedSource = Source.fromText("" + "function combine(a, b) {\n" + "  return a + b;\n" + "}",
                        "combine.sl").withMimeType("application/x-sl");

        Source redefine = Source.fromText("" + "function main() {\n" + "  defineFunction(\"function combine(a, b) { return a * b; }\");\n" + "}",
                        "redefine.sl").withMimeType("application/x-sl");

        engine1.eval(sharedSource);
        engine2.eval(sharedSource);

        final PolyglotEngine.Value fnValue1 = engine1.findGlobalSymbol("combine");
        final PolyglotEngine.Value fnValue2 = engine2.findGlobalSymbol("combine");

        SLFunction fn1 = fnValue1.as(SLFunction.class);
        SLFunction fn2 = fnValue2.as(SLFunction.class);

        // assertEquals("Functions are shared between two engines", fn1, fn2);

        assertEquals("Plus yields 8", 8L, fnValue1.execute(5, 3).get());
        assertEquals("Plus yields 7", 7L, fnValue2.execute(4, 3).get());

        engine1.eval(redefine);

        assertEquals("Mul yields 15", 15L, fnValue1.execute(5, 3).get());
        assertEquals("Other engine still uses plus", 7L, fnValue2.execute(4, 3).get());
    }
}
