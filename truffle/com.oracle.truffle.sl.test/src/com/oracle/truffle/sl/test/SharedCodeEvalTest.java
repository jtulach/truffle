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
import com.oracle.truffle.sl.SLLanguage;
import com.oracle.truffle.sl.runtime.SLFunction;
import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.fail;
import org.junit.Test;

public class SharedCodeEvalTest {
    @Test
    public void twoEnginesShareTheirCode() throws Exception {
        ByteArrayOutputStream os = new ByteArrayOutputStream();

        PolyglotEngine.Builder builder = PolyglotEngine.newBuilder();
        builder.config(SLLanguage.MIME_TYPE, "notifyTransferToInterpreter", true);
        builder.setErr(os);

        PolyglotEngine engine1 = builder.build();
        PolyglotEngine engine2 = builder.build();

        // @formatter:off
        Source sharedSource = Source.newBuilder(""
            + "function combine(a, b) {\n"
            + "  return a + b;\n"
            + "}"
            + "function invoke(a, b) {\n"
            + "  return combine(a, b);\n"
            + "}"
            ).
            name("combine.sl").
            mimeType("application/x-sl").
            build();

        Source redefine = Source.newBuilder(""
            + "function main() {\n"
            + "  defineFunction(\"function combine(a, b) { return a * b; }\");\n"
            + "}"
            ).
            name("redefine.sl").
            mimeType("application/x-sl").
            build();
        // @formatter:

        engine1.eval(sharedSource);
        engine2.eval(sharedSource);

        final PolyglotEngine.Value fnCombine1 = engine1.findGlobalSymbol("combine");
        final PolyglotEngine.Value fnCombine2 = engine2.findGlobalSymbol("combine");

        SLFunction fn1 = fnCombine1.as(SLFunction.class);
        SLFunction fn2 = fnCombine2.as(SLFunction.class);

        assertNotEquals("Functions are different", fn1, fn2);
        assertEquals("Code is shared between two engines", fn1.getCallTarget(), fn2.getCallTarget());
        assertEquals("AST is shared between two engines", fn1.getCallTarget().getRootNode(), fn2.getCallTarget().getRootNode());

        assertTransfer("No transfer yet", os);

        final PolyglotEngine.Value fnInvoke1 = engine1.findGlobalSymbol("invoke");
        final PolyglotEngine.Value fnInvoke2 = engine2.findGlobalSymbol("invoke");

        assertEquals("Plus yields 8", 8L, fnInvoke1.execute(5, 3).get());
        assertEquals("Plus yields 7", 7L, fnInvoke2.execute(4, 3).get());

        assertTransfer("Two transfers", os, "combine", "combine");

        assertEquals("Plus yields 3", 3L, fnInvoke1.execute(1, 2).get());
        assertEquals("Plus yields 4", 4L, fnInvoke2.execute(2, 2).get());

        assertTransfer("No transfers anymore everything is optimized and stable", os);

        engine1.eval(redefine);

        assertTransfer("Redefine causes one transfer", os, "defineFunction");

        assertEquals("Mul yields 15", 15L, fnInvoke1.execute(5, 3).get());
        assertEquals("2nd engine still uses plus", 7L, fnInvoke2.execute(4, 3).get());

        assertTransfer("No transfers for literal node either - no new context", os);
    }

    private static void assertTransfer(String msg, ByteArrayOutputStream os, String... expectedTransfers) throws UnsupportedEncodingException {
        String text = os.toString("UTF-8");
        Pattern pattern = Pattern.compile("^notifyTransferToInterpreter: (\\w*)$", Pattern.MULTILINE);
        Matcher matcher = pattern.matcher(text);
        for (int i = 0; i < expectedTransfers.length; i++) {
            if (!matcher.find()) {
                fail(msg + ". Cannot find " + i + "th transferToInterpreter in:\n" + text);
            }
            assertEquals(msg + " :" + i + "th group is correct", expectedTransfers[i], matcher.group(1));
        }
        assertFalse(msg + ". No more transfers in:\n" + text, matcher.find());
        os.reset();
    }
}
