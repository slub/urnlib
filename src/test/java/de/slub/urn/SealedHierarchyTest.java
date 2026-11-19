/*
 * Copyright (C) 2025 Saxon State and University Library Dresden (SLUB)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package de.slub.urn;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Checks that the roots of the type hierarchy cannot be extended from outside of the library's package. Otherwise a
 * third party could create URN types that do not conform to an RFC.
 */
public class SealedHierarchyTest {

    private static final int ACCESSIBLE_FROM_OUTSIDE = Modifier.PUBLIC | Modifier.PROTECTED;

    @ParameterizedTest
    @ValueSource(classes = {URN.class, NamespaceIdentifier.class, NamespaceSpecificString.class})
    public void Root_has_no_constructor_accessible_from_outside_the_package(Class<?> root) {
        for (Constructor<?> constructor : root.getDeclaredConstructors()) {
            assertFalse((constructor.getModifiers() & ACCESSIBLE_FROM_OUTSIDE) != 0,
                    constructor + " makes the hierarchy extensible");
        }
    }

    @ParameterizedTest
    @ValueSource(classes = {URN.class, NamespaceIdentifier.class, NamespaceSpecificString.class})
    public void Root_has_no_protected_methods(Class<?> root) {
        for (Method method : root.getDeclaredMethods()) {
            assertFalse(Modifier.isProtected(method.getModifiers()),
                    method + " suggests an extension point that does not exist");
        }
    }

    @ParameterizedTest
    @ValueSource(classes = {
            URN_2141.class, URN_8141.class, NamespaceURN.class,
            NID_RFC2141.class, NID_RFC8141.class,
            NSS_RFC2141.class, NSS_RFC8141.class})
    public void Leaf_is_final(Class<?> leaf) {
        assertTrue(Modifier.isFinal(leaf.getModifiers()), leaf + " can be extended");
    }

}
