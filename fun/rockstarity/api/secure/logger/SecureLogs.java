package fun.rockstarity.api.secure.logger;

import org.apache.logging.log4j.core.Logger;

import lombok.experimental.UtilityClass;

/**
 * @author ConeTin
 * @since 5 РёСЋРЅ. 2024 Рі.
 */

@UtilityClass
public class SecureLogs {
	
	private SecureLogger logger = new SecureLogger();
	
	public SecureLogger getLogger() {
		return logger;
	}

}
