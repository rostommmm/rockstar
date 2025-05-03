package fun.rockstarity.api.modules;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;

/**
 * @author ConeTin
 * @since 10 янв. 2025 г.
 */

@Getter
@Accessors(fluent = true)
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@AllArgsConstructor
public class ModuleInfo {

	String name;

    String desc;

    Category type;
	
    String[] module;

}
