package fun.rockstarity.api.commands;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author ConeTin
 * @since 24 РјР°СЂ. 2025вЂЇРі.
 */

@Getter
public class CommandParameter {
    private final Command command;
    private final String[] names;
    
    public CommandParameter(Command cmd, String... names) {
        this.command = cmd;
        this.names = names;
        
        cmd.getParameters().add(this);
        
        if (names.length == 0) {
            throw new IllegalArgumentException("Parameter must have at least one name");
        }
    }
    
    public String getPrimaryName() {
        return names[0];
    }
}