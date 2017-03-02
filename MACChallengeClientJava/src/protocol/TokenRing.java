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
		// Random ID for distinction between clients.
		ID = new Random().nextInt(1000000000);
		System.out.println("my ID: " + ID);
	}
	
	// Amount of Data being sent without changing to next client.
	// If the queue is longer, the amount gets higher.
	public int count(int localQueueLength) {
		return (int) (Math.sqrt(localQueueLength) * 1.4 + 3.1);
	}

	@Override
	public TransmissionInfo TimeslotAvailable(MediumState previousMediumState, int controlInformation,
			int localQueueLength) {
		slotnumber++;
		
		// Informations about current slot.
		System.out.println("-------------------------------");
		System.out.println("SLOT - " + slotnumber);
		System.out.println("last state: " + previousMediumState);
		System.out.println("last controlInfo: " + controlInformation);
		System.out.println("Queue length: " + localQueueLength);

		// No data to send, just be quiet.
		if (localQueueLength == 0) {
			System.out.println("SLOT - No data to send.");
			return new TransmissionInfo(TransmissionType.Silent, 0);
		}
		
		// Data to send!
		boolean sending = false;

		// No one has the token.
		if (controlInformation == 0) {
			// Last state was collision.
			if (previousMediumState == MediumState.Collision) {
				// Change of 25% for sending.
				sending = new Random().nextInt(100) < 25;
			// Last state was Idle or Succes.
			} else {
				sending = true;
			}
		// This client has the token
		} else if (controlInformation == ID) {
			sending = true;
			// The amount of data which still can be sent is reduced by one.
			counter--;
		}

		// Data will be sent.
		if (sending) {
			System.out.println("SLOT - Sending data & taking token.");
			// Last data being sent in this turn, setting token free and reseting counter.
			if (counter == 0 || localQueueLength == 1) {
				counter = count(localQueueLength);
				return new TransmissionInfo(TransmissionType.Data, 0);
			}
			return new TransmissionInfo(TransmissionType.Data, ID);		
		// No data will be sent
		} else {
			System.out.println("SLOT - idle");
			return new TransmissionInfo(TransmissionType.Silent, 0);
		}
	}
}
