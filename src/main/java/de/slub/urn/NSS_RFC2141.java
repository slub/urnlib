package de.slub.urn;

import java.util.regex.Pattern;

public class NSS_RFC2141 extends NamespaceSpecificString {

    private static final Pattern allowedCharacters = Pattern.compile("^([0-9a-zA-Z()+,-.:=@;$_!*']|(%[0-9a-fA-F]{2}))+$");

    /**
     * Create a new {@code NamespaceSpecificString} instance that is an exact copy of the given instance.
     *
     * @param instanceForCopying Base instance for copying
     */
    public NSS_RFC2141(NamespaceSpecificString instanceForCopying) {
        super(instanceForCopying);
    }

    public NSS_RFC2141(String namespaceSpecificString, NssEncoding rawNss) throws URNSyntaxException {
        super(namespaceSpecificString, rawNss);
    }


    @Override
    protected boolean isValidURLEncodedNamespaceSpecificString(String encoded) {
         return allowedCharacters.matcher(encoded).matches();
    }

}
