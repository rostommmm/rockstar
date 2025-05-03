package fun.rockstarity.api.modules;

import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * @author ConeTin
 * @since 2 дек. 2023 г.
 */
@Inherited
@Retention(RetentionPolicy.RUNTIME)
public @interface Info {

	String name();

    String desc();

    Category type();
	
    String[] module() default "";
}
