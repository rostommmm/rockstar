package fun.rockstarity.api.events;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import fun.rockstarity.api.events.Event;

@Retention(RetentionPolicy.RUNTIME)
public @interface EventType {
    Class<? extends Event>[] value();
}
