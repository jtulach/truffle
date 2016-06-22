/*
 * Copyright (c) 2012, 2014, Oracle and/or its affiliates. All rights reserved.
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
package com.oracle.truffle.sl.nodes.expression;

import com.oracle.truffle.api.CallTarget;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.nodes.NodeInfo;
import com.oracle.truffle.sl.nodes.SLExpressionNode;
import com.oracle.truffle.sl.runtime.SLContext;
import com.oracle.truffle.sl.runtime.SLFunction;
import com.oracle.truffle.sl.runtime.SLFunctionRegistry;
import java.lang.ref.Reference;

/**
 * Constant literal for a {@link SLFunction function} value, created when a function name occurs as
 * a literal in SL source code. Note that function redefinition can change the {@link CallTarget
 * call target} that is executed when calling the function, but the {@link SLFunction} for a name
 * never changes. This is guaranteed by the {@link SLFunctionRegistry}.
 */
@NodeInfo(shortName = "func")
public final class SLFunctionLiteralNode extends SLExpressionNode {
    /** The name of the function. */
    private final String functionName;

    /**
     * The resolved function. During parsing (in the constructor of this node), we do not have the
     * {@link SLContext} available yet, so the lookup can only be done at {@link #executeGeneric
     * first execution}. The {@link CompilationFinal} annotation ensures that the function can still
     * be constant folded during compilation.
     */
    private final Reference<SLContext> contextRef;
    private final FunctionCache functionCache;

    public SLFunctionLiteralNode(Reference<SLContext> ref, String functionName) {
        this.functionName = functionName;
        this.contextRef = ref;
        this.functionCache = new FunctionCache();
    }

    @Override
    public SLFunction executeGeneric(VirtualFrame frame) {
        return functionCache.get(contextRef.get());
    }

    private final class FunctionCache extends MappingCache<SLContext, SLFunction> {
        @Override
        protected SLFunction create(SLContext key) {
            final SLContext context = contextRef.get();
            if (context != null) {
                context.notifyTransferToInterpreter(functionName);
                return context.getFunctionRegistry().lookup(functionName, true);
            } else {
                throw new IllegalStateException();
            }
        }
    }
}
