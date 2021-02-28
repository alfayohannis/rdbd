package org.vaultage.demo.synthesiser.twomachines;

import java.io.File;
import java.io.PrintStream;
import java.net.InetSocketAddress;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.text.MessageFormat;

import org.vaultage.core.Vaultage;
import org.vaultage.core.VaultageServer;
import org.vaultage.demo.synthesiser.Worker;
import org.vaultage.demo.synthesiser.message.SynchronisedGetTextSizeResponseHandler;

/**
 * Large message test. Run worker service first to prepare the workers before
 * running this class.
 *
 * @author Alfonso de la Vega, Alfa Yohannis
 */
public class LargeMessageTrafficRequester {

	private static final String SHARED_REQUESTER_DIRECTORY = "Z:\\requesters\\";
	private static final String SHARED_WORKER_DIRECTORY = "Z:\\workers\\";
	private static final String LOCAL_IP = "192.168.0.2";
	private static final String REMOTE_IP = "192.168.0.2";

	protected boolean isEncrypted;
	protected long latestRunTime;
	protected boolean brokered;
	protected boolean encrypted;
	protected int numWorkers;
	protected int numOfBytes;

	public static void main(String[] args) throws Exception {
		int numReps = 5;
		// Only one requester and worker are required for this test. The worker is
		// created by WorkerService. That's why I only put one worker here: the
		// requester.
		int numWorkers = 1;
		int[] numOfBytes = { 9000, 10000, 20000, 30000, 40000, 50000 };

		PrintStream profilingStream = new PrintStream(new File("largeMessageNetResults.csv"));
		profilingStream.println("Mode,Encryption,MessageBytes,TotalTimeMillis");

		// brokered and encrypted
		for (int n : numOfBytes) {
			LargeMessageTrafficRequester trafficSimulation = new LargeMessageTrafficRequester(numWorkers, n, true,
					true);
			for (int rep = 0; rep < numReps; rep++) {
				trafficSimulation.run();
				System.out.println(trafficSimulation.getLatestRunDetails());
				profilingStream.println(
						String.format("%s,%s,%s,%d", "brokered", "encrypted", n, trafficSimulation.getLatestRunTime()));
			}
		}

		// direct and encrypted
		for (int n : numOfBytes) {
			LargeMessageTrafficRequester trafficSimulation = new LargeMessageTrafficRequester(numWorkers, n, false,
					true);
			for (int rep = 0; rep < numReps; rep++) {
				trafficSimulation.run();
				System.out.println(trafficSimulation.getLatestRunDetails());
				profilingStream.println(
						String.format("%s,%s,%s,%d", "direct", "encrypted", n, trafficSimulation.getLatestRunTime()));
			}
		}

		// brokered and un-encrypted
		for (int n : numOfBytes) {
			LargeMessageTrafficRequester trafficSimulation = new LargeMessageTrafficRequester(numWorkers, n, true,
					false);
			for (int rep = 0; rep < numReps; rep++) {
				trafficSimulation.run();
				System.out.println(trafficSimulation.getLatestRunDetails());
				profilingStream.println(
						String.format("%s,%s,%s,%d", "brokered", "plain", n, trafficSimulation.getLatestRunTime()));
			}
		}

		// direct and un-encrypted
		for (int n : numOfBytes) {
			LargeMessageTrafficRequester trafficSimulation = new LargeMessageTrafficRequester(numWorkers, n, false,
					false);
			for (int rep = 0; rep < numReps; rep++) {
				trafficSimulation.run();
				System.out.println(trafficSimulation.getLatestRunDetails());
				profilingStream.println(
						String.format("%s,%s,%s,%d", "direct", "plain", n, trafficSimulation.getLatestRunTime()));
			}
		}
		profilingStream.close();
		System.out.println("Finished!");
		System.exit(0);
	}

	public LargeMessageTrafficRequester(int numWorkers, int numBytes, boolean brokered, boolean encrypted) {
		this.numWorkers = numWorkers;
		this.numOfBytes = numBytes;
		this.brokered = brokered;
		this.encrypted = encrypted;
	}

	public void run() throws Exception {
		Worker[] requesters = new Worker[numWorkers];

//		VaultageServer server = new VaultageServer("tcp://localhost:61616");
		VaultageServer server = new VaultageServer("tcp://139.162.228.32:61616");

		// loading workers public keys
		String[] workerPKs = new String[numWorkers];
		File directoryPath = new File(SHARED_WORKER_DIRECTORY);
		File[] files = directoryPath.listFiles();
//		for (int i = 0; i < files.length; i++) {
		String workerPK = new String(Files.readAllBytes(Paths.get(files[0].getAbsolutePath())));
		workerPKs[0] = workerPK;
//		}

		int port = Vaultage.DEFAULT_SERVER_PORT + 100;
		for (int i = 0; i < numWorkers; i++) {
			requesters[i] = new Worker();
			requesters[i].setId("Requester-" + i);
			requesters[i].setCompletedValue(numOfBytes);
			requesters[i].addOperationResponseHandler(new SynchronisedGetTextSizeResponseHandler());
			requesters[i].register(server);
			if (!brokered) {
				requesters[i].startServer(LOCAL_IP, port++);
				requesters[i].getVaultage().getPublicKeyToRemoteAddress().put(workerPKs[0],
						new InetSocketAddress(REMOTE_IP, Vaultage.DEFAULT_SERVER_PORT));
			} else {
				requesters[i].getVaultage().forceBrokeredMessaging(false);
			}
			Files.write(Paths.get(SHARED_REQUESTER_DIRECTORY + requesters[i].getId() + ".txt"),
					requesters[i].getPublicKey().getBytes(), StandardOpenOption.CREATE);
		}

		// create message
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < numOfBytes; i++) {
			sb.append("a");
		}

		// create the thread, only use one vault for this
		// (two: the requester and the worker)
		Thread thread[] = new Thread[numWorkers];
		String remoteWorkerKey = workerPKs[0];
		thread[0] = startWork(requesters[0], remoteWorkerKey, encrypted, sb.toString());

		long start = System.currentTimeMillis();

		thread[0].start();
		thread[0].join();

		long end = System.currentTimeMillis();

		latestRunTime = end - start;

		if (!brokered) {
			for (int i = 0; i < numWorkers; i++) {
				requesters[i].shutdownServer();
			}
		}
	}

	public int getNumOfBytes() {
		return numOfBytes;
	}

	public long getLatestRunTime() {
		return latestRunTime;
	}

	public String getLatestRunDetails() {
		return MessageFormat.format(
				"Brokered: {0}, Encrypted: {1}, Num workers: {2}, NumOfBytes: {3}, Total time: {4} ms", brokered,
				encrypted, numWorkers, numOfBytes, latestRunTime);
	}

	private static Thread startWork(Worker worker, String remoteWorkerKey, boolean encrypted, String text) {
		Thread t = new Thread() {
			public void run() {
				try {
					worker.getTextSize(remoteWorkerKey, text, encrypted);
				} catch (Exception e) {
					e.printStackTrace();

				}
			}
		};
		return t;
	}

}