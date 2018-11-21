
import javax.naming.Context;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.*;
import java.util.Hashtable;

//import javax.naming.directory.Attributes;
//import javax.naming.directory.SearchResult;


/**
 * Ldap Client class is main class to create, modify, search and delete all the
 * LDAP functionality available.
 *
 * @author T.Elanjchezhiyan(chezhiyan @ siptech.co.in) - Sip Technologies and
 * Exports Ltd.
 * Created     Apr 29 2003 11:00 AM
 * @version $Revision: 1.7 $ Last updated: $Date: 2004/02/13 02:40:54 $
 */
public class LdapClient {
    private DirContext dirContext = null;

    /**
     * Constructor for the LdapClient object.
     */
    public static void main(String[] args) throws NamingException {
        LdapClient ldapClient = new LdapClient();
        ldapClient.connect("localhost", "389", "dc=example,dc=org", "cn=admin,dc=example,dc=org", "admin");
       boolean found = ldapClient.searchTest("dc=tudelft", "(&(uid=eddy)(objectClass=person))");
        if (found) {
            System.out.println("Gevonden");
        } else {
            System.out.println("NIET Gevonden");
        }
    }

    /**
     * Connect to server.
     */
    public void connect(
            String host,
            String port,
            String rootdn,
            String username,
            String password)
            throws NamingException {
        Hashtable env = new Hashtable();
        env.put(
                Context.INITIAL_CONTEXT_FACTORY,
                "com.sun.jndi.ldap.LdapCtxFactory");
        env.put(Context.PROVIDER_URL, "ldap://" + host + ":" + port + "/" + rootdn);
        env.put(Context.REFERRAL, "throw");
        env.put(Context.SECURITY_CREDENTIALS, password);
        env.put(Context.SECURITY_PRINCIPAL, username);
        dirContext = new InitialDirContext(env);
    }

    /**
     * Disconnect from the server.
     */
    public void disconnect() {
        try {
            if (dirContext != null) {
                dirContext.close();
                dirContext = null;
            }
        } catch (NamingException e) {
            System.out.println("Ldap client - " + e);
        }
    }

    /**
     * Filter  the data in the ldap directory for the given search base.
     *
     * @param searchBase   where the search should start
     * @param searchFilter filter this value from the base
     */
    public boolean searchTest(String searchBase, String searchFilter)
            throws NamingException {
        //System.out.println("Base="+searchBase+" Filter="+searchFilter);
        SearchControls searchcontrols =
                new SearchControls(SearchControls.SUBTREE_SCOPE,
                        1L, //count limit
                        0,  //time limit
                        null,//attributes (null = all)
                        true,// return object ?
                        false);// dereference links?
//        BasicAttributes naam = new BasicAttributes("uid", "jan");
//        BasicAttributes naam = new BasicAttributes("postalAddress", "Delft");
//        BasicAttributes naam = new BasicAttributes("postalAddress", "Abcoude");
        NamingEnumeration ne =
                     dirContext.search( searchBase, searchFilter , searchcontrols);
        boolean retVal = ne.hasMore();
		while (ne.hasMore()){
			Object tmp = ne.next();
			System.out.println(tmp.getClass().getName());
			SearchResult sr = (SearchResult) tmp;
			Attributes at = sr.getAttributes();
			System.out.println(at.get("cn"));
		}
        return retVal;
    }

    /**
     * Modify the attribute in the ldap directory for the given string.
     *
     * @param mods   add all the entry in to the ModificationItem
     * @param string the  string (dn) value
     */
    public void modifyTest(ModificationItem[] mods, String string)
            throws NamingException {
        dirContext.modifyAttributes(string, mods);
    }

    /**
     * Create the attribute in the ldap directory for the given string.
     *
     * @param basicattributes add all the entry in to the basicattribute
     * @param string          the  string (dn) value
     */
    public void createTest(BasicAttributes basicattributes, String string)
            throws NamingException {
        //DirContext dc = //TODO perhaps return this?
        dirContext.createSubcontext(string, basicattributes);
    }

    /**
     * Delete the attribute from the ldap directory.
     *
     * @param string the string (dn) value
     */
    public void deleteTest(String string)
            throws NamingException {
        dirContext.destroySubcontext(string);
    }
}

