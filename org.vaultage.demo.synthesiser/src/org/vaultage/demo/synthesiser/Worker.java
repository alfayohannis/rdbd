package org.vaultage.demo.synthesiser;

import org.vaultage.demo.synthesiser.message.SynchronisedGetTextSizeResponseHandler;

public class Worker extends WorkerBase {

	private int currentValue;
	private int completedValue;

	public Worker() throws Exception {
		super();
		currentValue = 0;
	}

	public void sendOperation(String workerPublicKey) throws Exception {
		RemoteWorker worker = new RemoteWorker(this, workerPublicKey);
		synchronized (this.getIncrementResponseHandler()) {
			worker.increment(currentValue);
			this.getIncrementResponseHandler().wait();
		}
	}

	@Override
	public void increment(String requesterPublicKey, String requestToken, Integer number) throws Exception {
		RemoteWorker remote = new RemoteWorker(this, requesterPublicKey);
		remote.respondToIncrement(number + 1, requestToken);
	}

	public boolean isWorkComplete() {
		return currentValue >= completedValue;
	}

	public void setCurrentValue(int currentValue) {
		this.currentValue = currentValue;
	}

	public void setCompletedValue(int completedValue) {
		this.completedValue = completedValue;
	}

	public int getTextSize(String workerPublicKey, String text) throws Exception {
		return this.getTextSize(workerPublicKey, text, true);
	}

	public int getTextSize(String workerPublicKey, String text, boolean isEncrypted) throws Exception {
		RemoteWorker worker = new RemoteWorker(this, workerPublicKey);
		synchronized (this.getGetTextSizeResponseHandler()) {
			worker.getTextSize(text, isEncrypted);
			this.getTextSizeResponseHandler.wait();
		}
		return ((SynchronisedGetTextSizeResponseHandler) this.getTextSizeResponseHandler).getTextSize();
	}

	@Override
	public void getTextSize(String requesterPublicKey, String requestToken, String text) throws Exception {
		this.getTextSize(requesterPublicKey, requestToken, text, true);
	}

	public void getTextSize(String requesterPublicKey, String requestToken, String text, boolean isEncrypted)
			throws Exception {
		RemoteWorker requester = new RemoteWorker(this, requesterPublicKey);
		requester.respondToGetTextSize(text.getBytes().length, requestToken, isEncrypted);
	}

}