package protocol;

import java.util.Random;

/**
 * A fairly trivial Medium Access Control scheme.
 * 
 * @author Jaco ter Braak, Twente University
 * @version 05-12-2013
 */
public class TokenRing implements IMACProtocol {

	public final int ID;
	public int slotnumber = 0;
	public int counter = 4;

	public TokenRing() {
		ID = new Random().nextInt(1000);
		System.out.println("my ID: " + ID);
	}
	
	public int count(int localQueueLength) {
		return (int) (Math.sqrt(localQueueLength) * 1.4 + 3.4);
	}

	@Override
	public TransmissionInfo TimeslotAvailable(MediumState previousMediumState, int controlInformation,
			int localQueueLength) {
		slotnumber++;

		System.out.println("-------------------------------");
		System.out.println("SLOT - " + slotnumber);
		System.out.println("last state: " + previousMediumState);
		System.out.println("last controlInfo: " + controlInformation);
		System.out.println("Queue length: " + localQueueLength);

		// No data to send, just be quiet
		if (localQueueLength == 0) {
			if (controlInformation == ID) {
				System.out.println("SLOT - giving away Token");
				counter = count(localQueueLength);
				return new TransmissionInfo(TransmissionType.NoData, 0);
			}
			System.out.println("SLOT - No data to send.");
			return new TransmissionInfo(TransmissionType.Silent, 0);
		}
		// Data to send!
		boolean sending = false;

		if (controlInformation == 0) {
			boolean permission = new Random().nextInt(100) < 100;
			if (previousMediumState == MediumState.Collision) {
				sending = new Random().nextInt(100) < 25;
			} else if (permission) {
				sending = true;
			}
		} else if (controlInformation == ID) {
			if (counter > 0) {
				sending = true;
				counter--;
			} else {
				sending = false;
				counter = count(localQueueLength);
			}
		}

		if (sending) {
			System.out.println("SLOT - Sending data & taking token.");
			if (counter == 0 || localQueueLength == 1) {
				counter = count(localQueueLength);
				return new TransmissionInfo(TransmissionType.Data, 0);
			}
			return new TransmissionInfo(TransmissionType.Data, ID);		
		} else {
			System.out.println("SLOT - idle");
			return new TransmissionInfo(TransmissionType.Silent, 0);
		}
	}
}
