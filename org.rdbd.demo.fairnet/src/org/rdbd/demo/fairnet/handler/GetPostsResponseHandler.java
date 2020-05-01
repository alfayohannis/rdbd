package org.rdbd.demo.fairnet.handler;

import java.util.List;

import org.rdbd.core.server.RDBDHandler;
import org.rdbd.core.server.RDBDMessage;
import org.rdbd.demo.fairnet.User;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class GetPostsResponseHandler extends RDBDHandler {

	@Override
	public void run() {
		System.out.println("Get Posts Request");
		System.out.println("------------------");
		System.out.println("From: " + this.message.getFrom());
		System.out.println("Operation: " + this.message.getOperation());

		try {
			User me = (User) this.owner;
			User myFriend = new User(message.getFrom());
			List<String> posts = me.getPosts(myFriend);
			
			Gson gson = new GsonBuilder().setPrettyPrinting().create();
			String value = gson.toJson(posts);
			 
			RDBDMessage messageBack = new RDBDMessage();
			messageBack.setFrom(me.getPublicKey());
			messageBack.setTo(this.message.getFrom());
			messageBack.setOperation(GetPostsConfirmationHandler.class.getName());
			messageBack.setValue(value);
			
			me.getRdbd().sendMessage(myFriend.getPublicKey(), messageBack);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
