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
package com.oracle.truffle.object.dsl.processor;

import com.oracle.truffle.api.object.dsl.Layout;
import com.oracle.truffle.object.dsl.processor.model.LayoutModel;
import com.oracle.truffle.object.dsl.processor.model.PropertyModel;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeKind;
import javax.tools.JavaFileObject;
import javax.tools.Diagnostic.Kind;

import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
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
        final List<LayoutModel> layouts = new ArrayList<>();

        for (TypeElement element : elements) {
            final LayoutParser parser = new LayoutParser(this);
            parser.parse(element);
            layouts.add(parser.build());
        }

        try {
            String fqn = pe.getQualifiedName() + ".Layouts";
            final JavaFileObject output = processingEnv.getFiler().createSourceFile(fqn, elements);
            try (PrintStream stream = new PrintStream(output.openOutputStream(), false, "UTF8")) {
                stream.println("package " + pe.getQualifiedName() + ";");
                stream.println();

                boolean needsAtomicInteger = false;
                boolean needsAtomicBoolean = false;
                boolean needsAtomicReference = false;
                boolean needsIncompatibleLocationException = false;
                boolean needsFinalLocationException = false;
                boolean needsHiddenKey = false;
                boolean needsEnumSet = false;
                boolean needsBoundary = false;
                boolean needsLayout = false;
                boolean needsLocationModifier = false;
                boolean needsProperty = false;

                for (LayoutModel layout : layouts) {
                    for (PropertyModel property : layout.getProperties()) {
                        if (!property.hasIdentifier()) {
                            needsHiddenKey = true;
                        }

                        if (property.isVolatile()) {
                            if (property.getType().getKind() == TypeKind.INT) {
                                needsAtomicInteger = true;
                            } else if (property.getType().getKind() == TypeKind.BOOLEAN) {
                                needsAtomicBoolean = true;
                            } else {
                                needsAtomicReference = true;
                            }
                        } else {
                            if (property.hasSetter()) {
                                if (!property.isShapeProperty()) {
                                    needsIncompatibleLocationException = true;
                                    needsFinalLocationException = true;
                                }
                            }
                        }
                    }

                    needsEnumSet |= layout.hasFinalProperties() || layout.hasNonNullableProperties();
                    needsBoundary |= !layout.getShapeProperties().isEmpty();
                    needsLayout |= layout.getSuperLayout() == null;
                    needsLocationModifier |= layout.hasFinalProperties() || layout.hasNonNullableProperties();
                    needsProperty |= !layout.getInstanceProperties().isEmpty();
                }

                if (needsEnumSet) {
                    stream.println("import java.util.EnumSet;");
                }

                if (needsAtomicBoolean) {
                    stream.println("import java.util.concurrent.atomic.AtomicBoolean;");
                }

                if (needsAtomicInteger) {
                    stream.println("import java.util.concurrent.atomic.AtomicInteger;");
                }

                if (needsAtomicReference) {
                    stream.println("import java.util.concurrent.atomic.AtomicReference;");
                }

                stream.println("import com.oracle.truffle.api.CompilerAsserts;");

                if (needsBoundary) {
                    stream.println("import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;");
                }

                stream.println("import com.oracle.truffle.api.object.DynamicObject;");
                stream.println("import com.oracle.truffle.api.object.DynamicObjectFactory;");

                if (needsFinalLocationException) {
                    stream.println("import com.oracle.truffle.api.object.FinalLocationException;");
                }

                if (needsHiddenKey) {
                    stream.println("import com.oracle.truffle.api.object.HiddenKey;");
                }

                if (needsIncompatibleLocationException) {
                    stream.println("import com.oracle.truffle.api.object.IncompatibleLocationException;");
                }

                if (needsLayout) {
                    stream.println("import com.oracle.truffle.api.object.Layout;");
                }

                if (needsLocationModifier) {
                    stream.println("import com.oracle.truffle.api.object.LocationModifier;");
                }

                stream.println("import com.oracle.truffle.api.object.ObjectType;");

                if (needsProperty) {
                    stream.println("import com.oracle.truffle.api.object.Property;");
                }

                stream.println("import com.oracle.truffle.api.object.Shape;");

                stream.println();

                stream.println("public final class Layouts {");
                stream.println();
                stream.println("    private Layouts() {");
                stream.println("    }");
                stream.println();

                for (LayoutModel layout : layouts) {
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

    private static PackageElement findPkg(Element element) {
        Element elementN = element;
        for (;;) {
            if (elementN.getKind() == ElementKind.PACKAGE) {
                return (PackageElement) elementN;
            }
            elementN = elementN.getEnclosingElement();
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

    private static class TypeComparator implements Comparator<QualifiedNameable> {
        @Override
        public int compare(QualifiedNameable o1, QualifiedNameable o2) {
            return o1.getQualifiedName().toString().compareTo(o2.getQualifiedName().toString());
        }
    }
}
