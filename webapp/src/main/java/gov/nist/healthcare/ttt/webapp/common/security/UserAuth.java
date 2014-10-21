package gov.nist.healthcare.ttt.webapp.common.security;

import gov.nist.healthcare.ttt.database.jdbc.DatabaseException;
import gov.nist.healthcare.ttt.webapp.common.db.DatabaseInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.ArrayList;
import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;

public class UserAuth implements UserDetailsService {
	
	private static Logger logger = Logger.getLogger(UserAuth.class.getName());
	
	@Autowired
	private DatabaseInstance db;

	@Override
	public UserDetails loadUserByUsername(String username)
			throws UsernameNotFoundException {
		
		Collection<GrantedAuthority> authorities = new ArrayList<GrantedAuthority>();
		authorities.add(new SimpleGrantedAuthority("user"));
		
		try {
			if(db.getDf().doesUsernameExist(username)) {
				return new User(username, db.getDf().getPasswordForUsername(username), authorities);			
			}
		} catch (DatabaseException e) {
			e.printStackTrace();
		}
		
		logger.log(Level.WARNING, "User " + username + " not found.");
		throw new UsernameNotFoundException("User " + username + " not found.");
	}

}
