package fun.rockstarity.api.binds;

import java.util.ArrayList;
import java.util.Optional;

import fun.rockstarity.api.IAccess;
import fun.rockstarity.api.events.list.game.inputs.EventKey;
import fun.rockstarity.api.helpers.game.Chat;
import lombok.Getter;

/**
 * @author ConeTin
 * @since 12 дек. 2023 г.
 */

public class Bindable implements IAccess {
	
	@Getter
	private final ArrayList<Bind> binds = new ArrayList<>();
	@Getter
	protected boolean toggled;
	
	public Optional<Bind> getBindByKey(EventKey event) {
	    if (event.getKey() == -1) {
	        return Optional.empty();
	    }
	    return getBinds().stream()
	                .filter(bind -> (bind.getKey() == -1 && bind.getScancode() == event.getScancode() && bind.getScancode() == event.getKey()) || (bind.getKey() == event.getKey() && event.getScancode() == event.getScancode()))
	                .findFirst();
	}
	
	public void toggleWithBind(Optional<Bind> opt) {
		Bind bind = opt.get();
		
		this.toggled = !this.toggled;
		
		if (bind.getType() != BindType.HOLD || bind.isHolding()) {
			if (this.toggled) {
			//	bind.setAlert(rock.getAlertHandler().alert(info.name() + " включен", bind.getType() == BindType.HOLD ? AlertType.WAIT : AlertType.SUCCESS));
			} else {
			//	bind.setAlert(rock.getAlertHandler().alert(info.name() + " выключен", bind.getType() == BindType.HOLD ? AlertType.WAIT : AlertType.ERROR));
			}
		} else {
			if (bind.getAlert() != null)
				bind.getAlert().hide();
		}
	}
	
}
