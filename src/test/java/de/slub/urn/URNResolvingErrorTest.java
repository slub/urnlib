/*
 * Copyright (C) 2017 Saxon State and University Library Dresden (SLUB)
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

public class URNResolvingErrorTest {

    @Test
    public void GetMessage_returns_message_passed_when_constructed() {
        String message = "The Message";

        URNResolvingError subject = new URNResolvingError(message);

        assertEquals(message, subject.getMessage());
    }

    @Test
    public void GetCause_returns_root_cause_exception() {
        Exception cause = new Exception("The Cause");

        URNResolvingError subject = new URNResolvingError("message-doesn't-matter", cause);

        assertEquals(cause, subject.getCause());
    }

    @Test
    public void Returns_failed_URN() throws URNSyntaxError {
        String anyMessage = "The Message";
        URN    urn        = URN.rfc8141().parse("urn:error:1234");

        URNResolvingError subject = new URNResolvingError(anyMessage, urn);

        assertEquals(urn, subject.failedUrn());
    }

}
