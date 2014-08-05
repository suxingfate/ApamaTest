package pers.danisan00.apama;

import com.apama.engine.beans.interfaces.EngineClientInterface;
import com.apama.util.CompoundException;

/**
 * Mix of SendAndReceiveUPGCEPEvents and SendAndReceiveUPGCEPEvents2.
 * 
 * Sends three Tweet events plus a RequestStateSummary event, and receives an
 * OutputStateSummary event.
 * 
 * The target correlator is assumed to contain the related event definitions and
 * monitors (i.e. it is running the UPG-CEP UC1 project).
 */
public class SendAndReceiveUPGCEPUC1Events3
		extends SendAndReceiveUPGCEPUC1Events2 {

	public SendAndReceiveUPGCEPUC1Events3() {
	}
	
	public static void main(String[] args) {
		SendAndReceiveUPGCEPUC1Events3 self = new SendAndReceiveUPGCEPUC1Events3();
		self.runTest();
	}
	
	private void runTest() {
//		String host = "10.42.109.236"; // Apama Studio 5.0 @ SRVUDC01
//		String host = "localhost"; // local Apama Studio 5.0
		String host = "10.42.102.190"; // UPG-integrated Apama 4.3 @ upg-virtual
		int port = 15903;
		
		try {
			EngineClientInterface engineClient = connect(host, port,
					"Apama43Tests_SendAndReceiveUPGCEPUC1Events3");

//			dumpEngineInfo(engineClient);
//			dumpEngineStatus(engineClient);
			
			startListeningForOutputStateSummaryEvents(engineClient);
			
			// First event: positive tweet
			sendTweetEvent(engineClient, "love", "whenever", "whoever",
					"40.00000", "-3.00000", "133");
			
			sleep(3);
			
			// Second event: negative tweet (within 10 seconds time window)
			sendTweetEvent(engineClient, "hate", "whenever", "whoever",
					"40.00000", "-3.00000", "134");
			
			sleep(3);
			
			// Third event: neutral tweet (within 10 seconds time window)
			sendTweetEvent(engineClient, "dunno", "whenever", "whoever",
					"40.00000", "-3.00000", "135");
			
			sleep(5);
			
			// Fourth event: negative tweet (out of 10 seconds time window)
			sendTweetEvent(engineClient, "more hate", "whenever", "whoever",
					"40.00000", "-3.00000", "136");

			// Since more than 10 seconds have passed, the first event has
			// already fallen out of the 10 seconds window, and only the latest
			// 3 events remain: 2 negative tweets and 1 neutral tweet.
			sendRequestStateSummaryEvent(engineClient, true, true, true, true,
					true);
			
			sleep(3);
			
			stopListeningForOutputStateSummaryEvents(engineClient);
			
//			dumpEngineInfo(engineClient);
//			dumpEngineStatus(engineClient);
			
			disconnect(engineClient);
			
			System.out.println("Done.");
			
		} catch (CompoundException ce) {
			System.out.println("Error: " + ce.getMessage());
			ce.printStackTrace();
		}
	}
	
}
