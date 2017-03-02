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
	public static final int TOKEN = 0;
	public int slotnumber = 0;
	public int counter = 4;

	public TokenRing() {
		ID = new Random().nextInt(1000);
		System.out.println("my ID: " + ID);
	}
	
	public int count(int localQueueLength) {
		if (localQueueLength < 5) {
			return 4;
		} else if (localQueueLength > 50) {
			return (int) (localQueueLength * 0.25);
		}
		return (int) (localQueueLength * 0.4);
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
			if (controlInformation == TOKEN + ID) {
				System.out.println("SLOT - giving away Token");
				counter = count(localQueueLength);
				return new TransmissionInfo(TransmissionType.NoData, TOKEN);
			}
			System.out.println("SLOT - No data to send.");
			return new TransmissionInfo(TransmissionType.Silent, 0);
		}
		// Data to send!
		boolean sending = false;

		if (controlInformation == 0 || controlInformation == TOKEN || previousMediumState == MediumState.Idle) {
			boolean permission = new Random().nextInt(100) < 90;
			if (previousMediumState == MediumState.Collision) {
				sending = new Random().nextInt(100) < 25;
			} else if (permission) {
				sending = true;
			}
		} else if (controlInformation == TOKEN + ID) {
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
			return new TransmissionInfo(TransmissionType.Data, TOKEN + ID);
		} else {
			System.out.println("SLOT - idle");
			return new TransmissionInfo(TransmissionType.Silent, 0);
		}
	}
}
