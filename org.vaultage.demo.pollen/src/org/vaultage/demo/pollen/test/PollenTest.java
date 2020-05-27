package org.vaultage.demo.pollen.test;

import static org.junit.Assert.*;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.vaultage.core.Vaultage;
import org.vaultage.core.VaultageMessage;
import org.vaultage.core.VaultageServer;
import org.vaultage.demo.pollen.app.SendNumberPollRequestHandler;
import org.vaultage.demo.pollen.app.SendNumberPollResponseHandler;
import org.vaultage.demo.pollen.app.User;
import org.vaultage.demo.pollen.data.PollRepository;
import org.vaultage.demo.pollen.gen.NumberPoll;
import org.vaultage.demo.pollen.gen.RemoteRequester;
import org.vaultage.demo.pollen.util.PollenUtil;

public class PollenTest {

	@Test
	public void testSalaryPoll() throws Exception {

		// setting the address of Vaultage server
		String address = "vm://localhost";
		final VaultageServer pollenBroker = new VaultageServer(address);

		// Alice
		double aliceFakeSalary = 25;
		double aliceRealSalary = 50;
		User alice = new User();
		alice.setName("Alice");
		PollenUtil.savePublicKey(alice);
		alice.register(pollenBroker);
		final RemoteRequester aliceRequester = new RemoteRequester(pollenBroker, alice);
		alice.setSendNumberPollRequestBaseHandler(new SendNumberPollRequestHandler() {
			@Override
			public double run(VaultageMessage senderMessage) throws Exception {
				User localVault = (User) this.vault;
				String json = senderMessage.getValue("poll");
				NumberPoll poll = Vaultage.Gson.fromJson(json, NumberPoll.class);

				if (localVault.getPublicKey().equals(poll.getOriginator())) {
					double myFakeSalary = aliceFakeSalary;
					result = myFakeSalary;
					return (double) result;
				} else {
					localVault.getPolls().put(poll.getId(), poll);
					double total = 0;
					for (String publicKey : poll.getParticipants()) {
						if (!publicKey.equals(localVault.getPublicKey())
								&& !publicKey.equals(senderMessage.getFrom())) {
							total = total + aliceRequester.sendNumberPoll(publicKey, poll);
						}
					}

					// this should be prompted to user to fill in his/her answer
					double mySalary = aliceFakeSalary;
					result = total + mySalary;
					return (double) result;
				}
			}
		});
		alice.setSendNumberPollResponseBaseHandler(new SendNumberPollResponseHandler());

		// Bob
		User bob = new User();
		bob.setName("Bob");
		PollenUtil.savePublicKey(bob);
		bob.register(pollenBroker);
		final RemoteRequester bobRequester = new RemoteRequester(pollenBroker, bob);
		bob.setSendNumberPollRequestBaseHandler(new SendNumberPollRequestHandler() {
			@Override
			public double run(VaultageMessage senderMessage) throws Exception {
				User localVault = (User) this.vault;
				String json = senderMessage.getValue("poll");
				NumberPoll poll = Vaultage.Gson.fromJson(json, NumberPoll.class);

				if (localVault.getPublicKey().equals(poll.getOriginator())) {
					double myFakeSalary = 25;
					result = myFakeSalary;
					return (double) result;
				} else {
					localVault.getPolls().put(poll.getId(), poll);
					double total = 0;
					for (String publicKey : poll.getParticipants()) {
						if (!publicKey.equals(localVault.getPublicKey())
								&& !publicKey.equals(senderMessage.getFrom())) {
							total = total + bobRequester.sendNumberPoll(publicKey, poll);
						}
					}

					// this should be prompted to user to fill in his/her answer
					double mySalary = 100.0;
					result = total + mySalary;
					return (double) result;
				}
			}
		});
		bob.setSendNumberPollResponseBaseHandler(new SendNumberPollResponseHandler());

		// Charlie
		User charlie = new User();
		charlie.setName("Charlie");
		PollenUtil.savePublicKey(charlie);
		charlie.register(pollenBroker);
		final RemoteRequester charlieRequester = new RemoteRequester(pollenBroker, charlie);
		charlie.setSendNumberPollRequestBaseHandler(new SendNumberPollRequestHandler() {
			@Override
			public double run(VaultageMessage senderMessage) throws Exception {
				User localVault = (User) this.vault;
				String json = senderMessage.getValue("poll");
				NumberPoll poll = Vaultage.Gson.fromJson(json, NumberPoll.class);

				if (localVault.getPublicKey().equals(poll.getOriginator())) {
					double myFakeSalary = 25;
					result = myFakeSalary;
					return (double) result;
				} else {
					localVault.getPolls().put(poll.getId(), poll);
					double total = 0;
					for (String publicKey : poll.getParticipants()) {
						if (!publicKey.equals(localVault.getPublicKey())
								&& !publicKey.equals(senderMessage.getFrom())) {
							total = total + charlieRequester.sendNumberPoll(publicKey, poll);
						}
					}

					// this should be prompted to user to fill in his/her answer
					double mySalary = 150.0;
					result = total + mySalary;
					return (double) result;
				}
			}
		});
		charlie.setSendNumberPollResponseBaseHandler(new SendNumberPollResponseHandler());

		// register participants
		List<String> participants = new ArrayList<>();
		participants.add(bob.getPublicKey());
		participants.add(charlie.getPublicKey());
		participants.add(alice.getPublicKey());

		// initialise salary poll
		NumberPoll salaryPoll = PollRepository.createSalaryPoll();
		salaryPoll.setOriginator(alice.getPublicKey());
		salaryPoll.setParticipants(participants);

		// send poll, initiated by Alice
		double fakeTotal = aliceRequester.sendNumberPoll(participants.get(0), salaryPoll);
		System.out.println("Total real salary = fakeTotal - aliceFakeSalary + aliceRealSalary");
		System.out.println("Total real salary = " + String.valueOf(fakeTotal) + " - " + String.valueOf(aliceFakeSalary) + " + " + String.valueOf(aliceRealSalary));
		double realTotal = fakeTotal - aliceFakeSalary + aliceRealSalary;
		System.out.println("Total real salary = " + realTotal);
		 
		assertEquals(300.0, realTotal, 0);

		alice.unregister();
		bob.unregister();
		charlie.unregister();
	}

}
