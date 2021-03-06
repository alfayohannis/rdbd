package org.vaultage.core;

public abstract class VaultageHandler {

	private static long COUNTER = 0;
	protected String queueId;
	protected String senderPublicKey;
	protected VaultageMessage message;
	protected Object vault;
	protected String name;
	protected Thread thread;
	protected Thread callerThread;
	protected Object result;

	public VaultageHandler() {
		
	}

	public void execute(String queueId, VaultageMessage message) throws InterruptedException {
		this.name = message.getOperation() + "-" + COUNTER;
		COUNTER++;
		this.message = message;
		this.start();
	}

	public void start() throws InterruptedException {

		thread = new Thread(VaultageHandler.this.getName()) {
			@Override
			public void run() {
				VaultageHandler.this.run();
			}
		};
		thread.start();
//		this.run();
		if (callerThread != null) {
			thread.join();
			synchronized (callerThread) {
				callerThread.notify();
			}
		}
	}

	public abstract void run();

	public String getSenderPublicKey() {
		return senderPublicKey;
	}

	public void setSenderPublicKey(String senderPublicKey) {
		this.senderPublicKey = senderPublicKey;
	}

	public String getName() {
		return name;

	}

	public void setName(String name) {
		this.name = name;
	}

	public Object getOwner() {
		return vault;
	}

	public void setOwner(Object owner) {
		this.vault = owner;
	}

	public VaultageMessage getMessage() {
		return message;
	}

	public Thread getThread() {
		return this.thread;
	}

	protected boolean isAlive() {
		if (this.thread != null) {
			return this.thread.isAlive();
		} else {
			return false;
		}
	}

	protected void interrupt() {
		if (this.thread != null) {
			this.thread.interrupt();
		}
	}

	public Thread getCallerThread() {
		return callerThread;
	}

	public void setCallerThread(Thread callerThread) {
		this.callerThread = callerThread;
	}

	public Object getResult() {
		return result;
	}

	public void setResult(Object result) {
		this.result = result;
	}
}
