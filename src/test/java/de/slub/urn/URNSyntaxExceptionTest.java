/*
 * Copyright (C) 2016 Saxon State and University Library Dresden (SLUB)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package de.slub.urn;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class URNSyntaxExceptionTest {

    @Test
    public void GetMessage_returns_message_passed_when_constructed() {
        String message = "The Message";
        URNSyntaxException subject = new URNSyntaxException(message);

        assertEquals(message, subject.getMessage());
    }

    @Test
    public void GetCause_returns_root_cause_exception() {
        Exception cause = new Exception("The Cause");
        URNSyntaxException subject = new URNSyntaxException("message-doesn't-matter", cause);

        assertEquals(cause, subject.getCause());
    }

}
