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

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class URNSyntaxErrorTest {

    @Test
    public void GetMessage_returns_message_passed_when_constructed() {
        String message = "The Message";
        URNSyntaxError subject = new URNSyntaxError(message);

        assertEquals(message, subject.getMessage());
    }

    @Test
    public void GetCause_returns_root_cause_exception() {
        Exception cause = new Exception("The Cause");
        URNSyntaxError subject = new URNSyntaxError("message-doesn't-matter", cause);

        assertEquals(cause, subject.getCause());
    }

    @Test
    public void Stores_violated_RFC() {
        URNSyntaxError subject = URNSyntaxError.syntaxError(RFC.RFC_2141, "bla");
        assertEquals(RFC.RFC_2141, subject.getViolatedRfc());
    }

    @Test
    public void Stores_error_code() {
        URNSyntaxError subject = URNSyntaxError.syntaxError(RFC.RFC_2141, "bla");
        assertEquals(URNSyntaxError.ErrorTypes.SYNTAX_ERROR, subject.getError());
    }

    @Test
    public void ToString_works_for_message_only_constructor() {
        URNSyntaxError subject = new URNSyntaxError("The Message");
        assertEquals(URNSyntaxError.class.getName() + ": The Message", subject.toString());
    }

    @Test
    public void ToString_works_for_message_and_cause_constructor() {
        URNSyntaxError subject = new URNSyntaxError("The Message", new Exception("The Cause"));
        assertEquals(URNSyntaxError.class.getName() + ": The Message", subject.toString());
    }

    @Test
    public void ToString_includes_violated_RFC_for_typed_errors() {
        String str = URNSyntaxError.syntaxError(RFC.RFC_8141, "bla").toString();
        assertTrue(str.contains("RFC_8141"), str);
        assertTrue(str.contains(RFC.RFC_8141.url().toString()), str);
    }

}
