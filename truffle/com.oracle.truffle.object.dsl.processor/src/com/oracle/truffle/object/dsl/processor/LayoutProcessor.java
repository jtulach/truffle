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
package com.oracle.truffle.object.dsl.processor;

import com.oracle.truffle.api.object.dsl.Layout;
import com.oracle.truffle.object.dsl.processor.layout.LayoutGenerator;
import com.oracle.truffle.object.dsl.processor.layout.LayoutParser;
import com.oracle.truffle.object.dsl.processor.layout.model.LayoutModel;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.tools.JavaFileObject;
import javax.tools.Diagnostic.Kind;

import java.io.IOException;
import java.io.PrintStream;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.QualifiedNameable;

@SupportedAnnotationTypes("com.oracle.truffle.api.object.dsl.Layout")
@SupportedSourceVersion(SourceVersion.RELEASE_7)
public class LayoutProcessor extends AbstractProcessor {

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnvironment) {
        Set<PackageElement> packages = new HashSet<>();
        
        for (Element element : roundEnvironment.getElementsAnnotatedWith(Layout.class)) {
            packages.add(findPkg(element));
        }
        
        for (PackageElement pe : packages) {
            TypeElement[] arr = findLayoutElements(pe, new TreeSet<TypeElement>(new TypeComparator())).toArray(new TypeElement[0]);
                processLayouts(pe, arr);
        }
        

        return true;
    }

    private void processLayouts(PackageElement pe, TypeElement... elements) {
        try {
            String fqn = pe.getQualifiedName() + ".Layouts";
            final JavaFileObject output = processingEnv.getFiler().createSourceFile(fqn, elements);
            try (PrintStream stream = new PrintStream(output.openOutputStream(), false, "UTF8")) {
                stream.println("package " + pe + ";\n");
                stream.println("import java.util.EnumSet;");
                stream.println("import com.oracle.truffle.api.object.*;");
                stream.println("import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;");
                stream.println("import com.oracle.truffle.api.CompilerAsserts;");
                stream.println("import java.util.concurrent.atomic.*;");
                stream.println("final class Layouts {\n");
                stream.println("  private Layouts() {}\n");
                for (TypeElement element : elements) {
                    final LayoutParser parser = new LayoutParser(this);
                    parser.parse(element);
                    final LayoutModel layout = parser.build();
                    final LayoutGenerator generator = new LayoutGenerator(layout);
                    generator.generate(stream);
                }
                stream.println("}");
            }
        } catch (IOException e) {
            reportError(elements[0], "IO error %s while writing code generated from @Layout", e.getMessage());
        }
    }

    public void reportError(Element element, String messageFormat, Object... formatArgs) {
        final String message = String.format(messageFormat, formatArgs);
        processingEnv.getMessager().printMessage(Kind.ERROR, message, element);
    }

    private PackageElement findPkg(Element element) {
        for (;;) {
            if (element.getKind() == ElementKind.PACKAGE) {
                return (PackageElement) element;
            }
            element = element.getEnclosingElement();
        }
    }

    private Set<TypeElement> findLayoutElements(Element element, Set<TypeElement> collectTo) {
        if (element.getAnnotation(Layout.class) != null) {
            collectTo.add((TypeElement) element);
        }
        for (Element enclosedElement : element.getEnclosedElements()) {
            findLayoutElements(enclosedElement, collectTo);
        }
        return collectTo;
    }

    private static class TypeComparator implements Comparator<QualifiedNameable>{
        @Override
        public int compare(QualifiedNameable o1, QualifiedNameable o2) {
            return o1.getQualifiedName().toString().compareTo(o2.getQualifiedName().toString());
        }
    }
}
