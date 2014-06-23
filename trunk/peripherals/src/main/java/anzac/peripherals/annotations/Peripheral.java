package anzac.peripherals.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import anzac.peripherals.tiles.PeripheralEvent;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Inherited
public @interface Peripheral {
	String type();

	PeripheralEvent[] events() default {};
}
