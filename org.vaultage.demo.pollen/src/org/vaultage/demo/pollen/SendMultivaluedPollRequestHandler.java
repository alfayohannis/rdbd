/**** protected region SendMultivaluedPollRequestHandler on begin ****/
package org.vaultage.demo.pollen;

import java.util.List;
import org.vaultage.core.VaultageMessage;

public class SendMultivaluedPollRequestHandler extends SendMultivaluedPollRequestBaseHandler {

	@Override
	public List<Integer> run(VaultageMessage senderMessage) throws Exception {	
		return (List<Integer>) result;
		
	}
}
/**** protected region SendMultivaluedPollRequestHandler end ****/