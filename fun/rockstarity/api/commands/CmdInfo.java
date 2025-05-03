package fun.rockstarity.api.commands;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * @author ConeTin
 * @since 9 дек. 2023 г.
 */

@Retention(RetentionPolicy.RUNTIME)
public @interface CmdInfo {

	String[] names();

    String desc();
	
}
