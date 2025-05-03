package fun.rockstarity.api.secure.logger;

import org.apache.logging.log4j.core.Logger;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.message.MessageFactory;
import org.apache.logging.log4j.message.SimpleMessageFactory;

import fun.rockstarity.api.IAccess;

/**
 * @author ConeTin
 * @since 5 РёСЋРЅ. 2024 Рі.
 */

public class SecureLogger extends Logger implements IAccess {

	public SecureLogger() {
		super(new LoggerContext("mc"), "mc", new SimpleMessageFactory());
	}
	
	@Override
	public void info(String message, Object p0) {
		//if (rock.isDebugging())
			super.info(message, p0);
	}
	
	@Override
	public void error(String message, Object p0) {
		//if (rock.isDebugging())
			super.error(message, p0);
	}
	
	@Override
	public void warn(String message, Object p0) {
		//if (rock.isDebugging())
			super.warn(message, p0);
	}
	
}
