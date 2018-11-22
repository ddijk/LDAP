
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.naming.AuthenticationException;
import javax.naming.Context;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;
import java.util.Hashtable;

public class LdapClientAuth implements Authenticator {

    private final String INITIAL_CONTEXT_FACTORY = "com.sun.jndi.ldap.LdapCtxFactory";

    // Configuration properties:
    private final String LDAP_SERVER_URL;
    private final String SEARCH_BASE_DN;
    private final String USER_DN;
    private final String USER_DN_PASSWORD;
    private final String LDAP_USER_FILTER;

    private static final Logger LOGGER = LoggerFactory.getLogger(LdapClientAuth.class);


    public LdapClientAuth(String url, String searchBase, String username, String password, String ldapUserFilter) {
        this.LDAP_SERVER_URL = url;
        this.SEARCH_BASE_DN = searchBase;
        this.USER_DN = username;
        this.USER_DN_PASSWORD = password;
        this.LDAP_USER_FILTER = ldapUserFilter;
    }

    @Override
    public void authenticate(String username, String password) throws AuthenticationException {
        Hashtable env = new Hashtable();
        env.put(Context.INITIAL_CONTEXT_FACTORY, INITIAL_CONTEXT_FACTORY);
        env.put(Context.PROVIDER_URL, LDAP_SERVER_URL);
        env.put(Context.SECURITY_PRINCIPAL, USER_DN);
        env.put(Context.SECURITY_CREDENTIALS, USER_DN_PASSWORD);

        DirContext dirContext = null;
        try {
            dirContext = new InitialDirContext(env);

            SearchControls searchControls = new SearchControls();
            searchControls.setSearchScope(SearchControls.SUBTREE_SCOPE);
            searchControls.setCountLimit(2);
            searchControls.setReturningObjFlag(true);

            String searchFilter = String.format(LDAP_USER_FILTER, username);
            NamingEnumeration ne = dirContext.search(SEARCH_BASE_DN, searchFilter, searchControls);
            while (ne.hasMore()) {
                SearchResult sr = (SearchResult) ne.next();
                final String studentDn = sr.getNameInNamespace();
                LOGGER.info("Trying to authenticate user with DN=" + studentDn);
                checkPasswordWithReconnect(studentDn, password);
            }

            if (ne.hasMore()) {
                // More than one entry with the same username. Maybe this should throw an exception...
                // But if authentication succeeds, then it's probably OK
                LOGGER.info("More than one user with the same username:" + username);
            }
        } catch (NamingException e) {
            throw new AuthenticationException(e.getExplanation());
        } finally {
            if (dirContext != null) {
                try {
                    dirContext.close();
                } catch (NamingException e) {
                    LOGGER.error("Failed to close LDAP connection.", e);
                }
            }
            dirContext = null;
        }

    }

    /**
     * Authenticate the user. When an InitialDirContext can be created then authentication was successful, otherwise
     * an AuthenticationException is thrown.
     *
     * @param studentsDn DN of the user
     * @param password
     * @throws NamingException
     */
    private void checkPasswordWithReconnect(String studentsDn, String password) throws NamingException {
        Hashtable env = new Hashtable();
        env.put(Context.INITIAL_CONTEXT_FACTORY, INITIAL_CONTEXT_FACTORY);
        env.put(Context.PROVIDER_URL, LDAP_SERVER_URL);
        env.put(Context.SECURITY_PRINCIPAL, studentsDn);
        env.put(Context.SECURITY_CREDENTIALS, password);

        new InitialDirContext(env);

    }

}
