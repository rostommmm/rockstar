package fun.rockstarity.api.scripts.wrappers;

import org.luaj.vm2.LuaFunction;

import fun.rockstarity.api.events.list.game.EventChat;
import fun.rockstarity.api.events.list.game.EventKill;
import fun.rockstarity.api.events.list.game.EventShutdown;
import fun.rockstarity.api.events.list.game.EventTick;
import fun.rockstarity.api.events.list.game.EventTotemBreak;
import fun.rockstarity.api.events.list.game.client.EventAlert;
import fun.rockstarity.api.events.list.game.inputs.EventInput;
import fun.rockstarity.api.events.list.game.inputs.EventKey;
import fun.rockstarity.api.events.list.game.packet.EventReceivePacket;
import fun.rockstarity.api.events.list.game.packet.EventSendPacket;
import fun.rockstarity.api.events.list.player.EventAttack;
import fun.rockstarity.api.events.list.player.EventDeath;
import fun.rockstarity.api.events.list.player.EventJump;
import fun.rockstarity.api.events.list.player.EventMotion;
import fun.rockstarity.api.events.list.player.EventMotionMove;
import fun.rockstarity.api.events.list.player.EventMove;
import fun.rockstarity.api.events.list.player.EventUpdate;
import fun.rockstarity.api.events.list.render.EventRender2D;
import fun.rockstarity.api.events.list.render.EventRender3D;
import fun.rockstarity.api.events.list.render.entity.EventRenderItem;
import lombok.Getter;
import lombok.Setter;

public class Events {
	public static Event 
			render_2d = new Event(EventRender2D.class),
			motion_move = new Event(EventMotionMove.class),
			render_3d = new Event(EventRender3D.class),
			update = new Event(EventUpdate.class),
			send_packet = new Event(EventSendPacket.class),
			receive_packet = new Event(EventReceivePacket.class),
			motion = new Event(EventMotion.class),
			key = new Event(EventKey.class),
			jump = new Event(EventJump.class),
			chat = new Event(EventChat.class),
			attack = new Event(EventAttack.class),
			shutdown = new Event(EventShutdown.class),
			input = new Event(EventInput.class),
			movefix = new Event(EventMove.class),
			kill = new Event(EventKill.class),
			death = new Event(EventDeath.class),
			swing = new Event(EventRenderItem.class),
			notification = new Event(EventAlert.class),
			tick = new Event(EventTick.class),
			totem_break = new Event(EventTotemBreak.class);
	
	public static class Event {
		@Getter
		private Class clazz;
		@Getter @Setter
		private LuaFunction fun;
		
		public Event(Class clazz) {
			this.clazz = clazz;
		}
		
		public void set(LuaFunction fun) {
			this.fun = fun;
		}
	}
}