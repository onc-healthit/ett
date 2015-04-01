package gov.nist.healthcare.ttt.direct.sender;

import org.apache.log4j.Logger;
import org.xbill.DNS.Lookup;
import org.xbill.DNS.Record;
import org.xbill.DNS.SRVRecord;
import org.xbill.DNS.TextParseException;
import org.xbill.DNS.Type;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;

import javax.naming.Context;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.*;
import java.util.Hashtable;

public class LdapDnslookUp {

	private static Logger logger = Logger.getLogger(LdapDnslookUp.class.getName());
	
	public InputStream getLdapCert(String email) {
		ArrayList<String> domains = getLDAPServer(getTargetDomain(email));
        Iterator<String> it = domains.iterator();
        InputStream cert = null;
        while(cert==null && it.hasNext()) {
            cert = getCert(it.next(), email);
        }
        return cert;
	}
	
	
	public ArrayList<String> getLDAPServer(String domain) {

		ArrayList<String> res = new ArrayList<>();
		String query = "_ldap._tcp." + domain;

		try {
			Record[] records = new Lookup(query, Type.SRV).run();

			if (records != null) {
				for (Record record : records) {
					SRVRecord srv = (SRVRecord) record;

					String hostname = srv.getTarget().toString().replaceFirst("\\.$", "");
					int port = srv.getPort();

					logger.info("DNS SRV query found LDAP at " + hostname + ":" + port);
					res.add(hostname + ":" + port);
				}
			}
		} catch (TextParseException e) {
			logger.info("Error trying to get Ldap certificate " + e.getMessage());
			e.printStackTrace();
		} catch (Exception e) {
			logger.info("Error trying to get Ldap certificate " + e.getMessage());
			e.printStackTrace();
		}

		return res;
	}

	@SuppressWarnings("rawtypes")
	public InputStream getCert(String domain, String email) {
		InputStream cert = null;

        Hashtable<String, String> env = new Hashtable<String, String>();

        env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
        env.put(Context.PROVIDER_URL, "ldap://"+ domain);
        DirContext ctx = null;
        NamingEnumeration results = null;
        try {
            ctx = new InitialDirContext(env);
            SearchControls controls = new SearchControls();
            controls.setSearchScope(SearchControls.SUBTREE_SCOPE);
            results = ctx.search("", "(mail=" + email + ")", controls);
            while (results.hasMore()) {
                SearchResult searchResult = (SearchResult) results.next();
                Attributes attributes = searchResult.getAttributes();
//                Attribute attr = attributes.get("cn");
//                String cn = (String) attr.get();
//                System.out.println(" Person Common Name = " + cn);

                Attribute certAttribute = attributes.get("userCertificate");

                try {
                    cert = new ByteArrayInputStream((byte[]) certAttribute.get());
                    logger.info("Found certificate for " + email + " at " + domain);
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        } catch (NamingException e) {
            throw new RuntimeException(e);
        } finally {
            if (results != null) {
                try {
                    results.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            if (ctx != null) {
                try {
                    ctx.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        return cert;
    }
	
	public static String getTargetDomain(String targetedTo) {
		// Get the targeted domain
		String targetDomain = targetedTo;
		if(targetedTo.contains("@")) {
			targetDomain = targetedTo.split("@", 2)[1];
		}
		return targetDomain;
	}
}
