/**** protected region User on begin ****/
package org.vaultage.demo.pollen.app;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;

import org.vaultage.demo.pollen.gen.MultivaluedPoll;
import org.vaultage.demo.pollen.gen.NumberPoll;
import org.vaultage.demo.pollen.gen.UserBase;

public class User extends UserBase {
	private String name = new String();
	
	public User() throws FileNotFoundException, NoSuchAlgorithmException, IOException {
		super();
	}
	
	// getter
	public String getName() {
		return this.name;
	}
	
	// setter
	public void setName(String name) {
		this.name = name;
	}
	
	// operations
	
	public void sendNumberPoll(String requesterPublicKey, NumberPoll poll) throws Exception {
		throw new Exception();
	}
	
	
	public void sendMultivaluedPoll(String requesterPublicKey, MultivaluedPoll poll) throws Exception {
		throw new Exception();
	}
	
	
}
/**** protected region User end ****/