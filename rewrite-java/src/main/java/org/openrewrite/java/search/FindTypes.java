/*
 * Copyright 2020 the original author or authors.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * https://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.openrewrite.java.search;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.openrewrite.ExecutionContext;
import org.openrewrite.Recipe;
import org.openrewrite.TreeVisitor;
import org.openrewrite.java.JavaIsoVisitor;
import org.openrewrite.java.JavaVisitor;
import org.openrewrite.java.tree.J;
import org.openrewrite.java.tree.JavaType;
import org.openrewrite.java.tree.NameTree;
import org.openrewrite.java.tree.TypeUtils;
import org.openrewrite.marker.RecipeSearchResult;

import java.util.HashSet;
import java.util.Set;

/**
 * This recipe finds all explicit references to a type.
 */
@Data
@EqualsAndHashCode(callSuper = true)
public final class FindTypes extends Recipe {
    private final String fullyQualifiedTypeName;

    @Override
    protected TreeVisitor<?, ExecutionContext> getVisitor() {
        return new JavaVisitor<ExecutionContext>() {
            {
                setCursoringOn();
            }

            @Override
            public <N extends NameTree> N visitTypeName(N name, ExecutionContext ctx) {
                N n = super.visitTypeName(name, ctx);
                JavaType.Class asClass = TypeUtils.asClass(n.getType());
                if (asClass != null && asClass.getFullyQualifiedName().equals(fullyQualifiedTypeName) &&
                        getCursor().firstEnclosing(J.Import.class) == null) {
                    return n.withMarker(new RecipeSearchResult(FindTypes.this));
                }
                return n;
            }
        };
    }

    public static Set<NameTree> find(J j, String fullyQualifiedClassName) {
        JavaIsoVisitor<Set<NameTree>> findVisitor = new JavaIsoVisitor<Set<NameTree>>() {
            {
                setCursoringOn();
            }

            @Override
            public <N extends NameTree> N visitTypeName(N name, Set<NameTree> ns) {
                N n = super.visitTypeName(name, ns);
                JavaType.Class asClass = TypeUtils.asClass(n.getType());
                if (asClass != null && asClass.getFullyQualifiedName().equals(fullyQualifiedClassName) &&
                        getCursor().firstEnclosing(J.Import.class) == null) {
                    ns.add(name);
                }
                return n;
            }
        };

        Set<NameTree> ts = new HashSet<>();
        findVisitor.visit(j, ts);
        return ts;
    }
}