package de.slub.urn;

import java.util.regex.Pattern;

public class NID_RFC2141 extends NamespaceIdentifier {

    private static final Pattern allowedNID = Pattern.compile("^[0-9a-zA-Z]+[0-9a-zA-Z-]{0,31}$");

    public NID_RFC2141(String nid) throws URNSyntaxException {
        super(nid);
    }

    public NID_RFC2141(NamespaceIdentifier instanceForCloning) {
        super(instanceForCloning);
    }

    @Override
    public boolean isValidNamespaceIdentifier(String nid) {
        return allowedNID.matcher(nid).matches();
    }

}
