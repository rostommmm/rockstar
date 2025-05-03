package fun.rockstarity.api.constuctor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import fun.rockstarity.api.IAccess;
import fun.rockstarity.api.constuctor.blocks.Block;
import fun.rockstarity.api.constuctor.blocks.types.BlockAction;
import fun.rockstarity.api.constuctor.blocks.types.BlockEvent;
import fun.rockstarity.api.constuctor.blocks.types.actions.BlockChatMsg;
import fun.rockstarity.api.constuctor.blocks.types.actions.BlockCondition;
import fun.rockstarity.api.constuctor.interfaces.ICondition;
import fun.rockstarity.api.constuctor.interfaces.IPlayerAction;
import fun.rockstarity.api.events.Event;
import fun.rockstarity.api.events.list.game.EventKill;
import fun.rockstarity.api.events.list.game.EventWorldChange;
import fun.rockstarity.api.events.list.game.packet.EventReceivePacket;
import fun.rockstarity.api.events.list.game.packet.EventSendPacket;
import fun.rockstarity.api.events.list.player.EventAttack;
import fun.rockstarity.api.events.list.player.EventJump;
import fun.rockstarity.api.events.list.player.EventPlace;
import fun.rockstarity.api.events.list.player.EventUpdate;
import fun.rockstarity.api.events.list.render.EventRender2D;
import fun.rockstarity.api.events.list.render.EventRender3D;
import fun.rockstarity.api.helpers.player.Move;
import lombok.Getter;
import ru.kotopushka.antiautistleak.obfuscator.includes.annotations.auth.ReleaseNativeAuth;
import ru.kotopushka.antiautistleak.obfuscator.includes.annotations.compile.ReleaseCompileToNativeCalls;

/**
 * @author ConeTin
 * @since 7 дек. 2023 г.
 */

@Getter
public class ScriptConstructor implements IAccess {
	
	private final ArrayList<Block> blocks = new ArrayList<>();
	private final HashMap<String, Class> events = new HashMap<>();
	private final HashMap<String, HashMap> actionTypes = new HashMap<>();
	private final HashMap<String, IPlayerAction> playerActions = new HashMap<>();
	private final HashMap<String, Consumer<String>> worldActions = new HashMap<>();
	private final HashMap<String, ICondition> conditions = new HashMap<>();
	private final ConstructorScreen screen;
	
	@ReleaseNativeAuth
	public ScriptConstructor() {
		screen = new ConstructorScreen();
		
		playerActions.put("Прыжок", args -> mc.player.jump());
		playerActions.put("Вывод в чат", args -> mc.player.sendChatMessage(args[0].toString()));
		playerActions.put("Движение по y", args -> mc.player.getMotion().y = Double.parseDouble(args[0].toString()));
		playerActions.put("Делать взмах", args -> mc.clickMouse());
		playerActions.put("Скорость", args -> Move.setSpeed(Double.parseDouble(args[0].toString())));
		
		worldActions.put("Устанавливать время", (args) -> mc.world.setDayTime(Long.parseLong(args)));
		worldActions.put("Устанавливать гамму", (args) -> mc.getGameSettings().setGamma(Double.parseDouble(args)));
		
		events.put("Каждом тике", EventUpdate.class);
		events.put("Получении пакетов", EventReceivePacket.class);
		events.put("Отправлении пакетов", EventSendPacket.class);
		events.put("2D отрисовке", EventRender2D.class);
		events.put("3D отрисовке", EventRender3D.class);
		events.put("Убийстве", EventKill.class);
		events.put("Прыжке", EventJump.class);
		events.put("Атаке", EventAttack.class);
		events.put("Поставке блока", EventPlace.class);
		events.put("Смене мира", EventWorldChange.class);
		
		actionTypes.put("Игрок", playerActions);
		actionTypes.put("Мир", worldActions);
		
        conditions.put("Игрок на земле", new ICondition() {
            @Override
            public boolean check() {
                return mc.player.isOnGround();
            }
            
            @Override
            public String getName() {
                return "Игрок на земле";
            }
        });
        
        conditions.put("Игрок в воде", new ICondition() {
        	@Override
        	public boolean check() {
        		return mc.player.isInWater();
        	}
        	
        	@Override
        	public String getName() {
        		return "Игрок в воде";
        	}
        });
        
        conditions.put("Игрок плавает", new ICondition() {
			
			@Override
			public String getName() {
				return "Игрок плавает";
			}
			
			@Override
			public boolean check() {
				return mc.player.isSwimming();
			}
		});
        
        conditions.put("Нажат шифт", new ICondition() {
			
			@Override
			public String getName() {
				return "Нажат шифт";
			}
			
			@Override
			public boolean check() {
				return mc.getGameSettings().keyBindSneak.isKeyDown();
			}
		});
        
        conditions.put("Нажата кнопка прыжка", new ICondition() {
			
			@Override
			public String getName() {
				return "Нажата кнопка прыжка";
			}
			
			@Override
			public boolean check() {
				return mc.getGameSettings().keyBindJump.isKeyDown();
			}
		});
	}
	
	public void execute(Event event) {
	    for (Block b : blocks) {
	        if (b instanceof BlockEvent block) {
	            if (block.getEvent().isAssignableFrom(event.getClass())) {
	                if (checkConditions(block)) {
	                    processChildBlock(block.getChild());
	                }
	            }
	        }
	    }
	}
	
	private boolean checkConditions(BlockEvent blockEvent) {
	    Block current = blockEvent.getChild();
	    while (current != null) {
	        if (current instanceof BlockCondition blockCondition) {
	            if (!blockCondition.getCondition().check()) {
	                return false;
	            }
	        }
	        current = current.getChild();
	    }
	    return true;
	}
    
    private void processChildBlock(Block child) {
        if (child instanceof BlockAction actionBlock) {
            onEvent(actionBlock);
        } else if (child instanceof BlockCondition conditionBlock) {
            if (conditionBlock.getCondition().check()) {
                if (conditionBlock.getChild() != null) {
                    processChildBlock(conditionBlock.getChild());
                }
            }
        }
    }
	
	private void onEvent(BlockAction block) {
		if (block instanceof BlockChatMsg msg) {
			String inputText = msg.getInput().getText();
			
			if (inputText.contains("random[")) {
				block.getAction().performAction(processRandom(inputText));
			} else if (!inputText.isBlank()) {
				block.getAction().performAction(inputText);
			} else {
	            block.getAction().performAction("1");
	        }
		}
	}
	
	private String processRandom(String input) {
		Pattern pattern = Pattern.compile("random\\[([^\\]]+)]");
		Matcher matcher = pattern.matcher(input);
		Random random = new Random();
		
		while (matcher.find()) {
			String[] options = matcher.group(1).split(",\\s*");
			String randomOption = options[random.nextInt(options.length)];
			
			input = input.replace(matcher.group(0), randomOption);
		}
		
		return input;
	}
}
