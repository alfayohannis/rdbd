package org.vaultage.demo.fairnet.handler;

import org.vaultage.core.VaultageHandler;
import org.vaultage.demo.fairnet.FairnetVault;
import org.vaultage.demo.fairnet.Post;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class GetPostResponseHandler extends VaultageHandler {
	private Post post;

	@Override
	public void run() {
		System.out.println("Get Post Confirmation");
		System.out.println("------------------");
		System.out.println("From: " + this.message.getFrom());
		System.out.println("Operation: " + this.message.getOperation());
		System.out.println("Post:");
		try {
			System.out.println(this.message.getValue("value"));
			Gson gson = new GsonBuilder().setPrettyPrinting().create();
			post = gson.fromJson(message.getValue("value"), Post.class);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public Post getPost() {
		return post;
	}

}