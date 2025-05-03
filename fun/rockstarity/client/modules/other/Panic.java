package fun.rockstarity.client.modules.other;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import com.mojang.blaze3d.matrix.MatrixStack;

import fun.rockstarity.api.events.Event;
import fun.rockstarity.api.events.list.render.EventRender2D;
import fun.rockstarity.api.helpers.math.MathUtility;
import fun.rockstarity.api.helpers.render.Gifs;
import fun.rockstarity.api.helpers.render.Loading;
import fun.rockstarity.api.helpers.render.Render;
import fun.rockstarity.api.helpers.system.TextUtility;
import fun.rockstarity.api.helpers.system.ThreadManager;
import fun.rockstarity.api.modules.Category;
import fun.rockstarity.api.modules.Info;
import fun.rockstarity.api.modules.Module;
import fun.rockstarity.api.modules.settings.list.CheckBox;
import fun.rockstarity.api.render.animation.Animation;
import fun.rockstarity.api.render.animation.Easing;
import fun.rockstarity.api.render.color.themes.Style;
import fun.rockstarity.api.render.shaders.list.Round;
import fun.rockstarity.api.render.ui.rect.Rect;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import net.minecraft.client.AbstractOption;
import net.minecraft.client.gui.screen.ConfirmScreen;
import net.minecraft.resources.ResourcePackType;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TranslationTextComponent;
import net.optifine.shaders.Shaders;
import ru.kotopushka.j2c.sdk.annotations.NativeInclude;

/**
 * @author Malecharik
 * @since 13 Mar 2024 12:53:49
 */


@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Info(name="Panic", desc="Скрывает чит", type=Category.OTHER)
public class Panic extends Module {
	
	@Getter private final CheckBox launch = new CheckBox(this, "Открывать лаунчер").desc("Открывает legacy лаунчер после закрытия клиента");
	
	@NonFinal boolean showed;
	Animation cleaning = new Animation().setEasing(Easing.BOTH_SINE).setSpeed(300);
	Animation process = new Animation().setEasing(Easing.BOTH_SINE).setSpeed(5000);
	Animation closing = new Animation().setEasing(Easing.BOTH_SINE).setSpeed(300);

	@NonFinal @Getter private int code = 0000;
	
	@Override
	public void onAllEvent(Event event) {
		if (event instanceof EventRender2D e && rock.isPanic() && (!cleaning.finished(false) || !showed)) {
			MatrixStack ms = e.getMatrixStack();
			
			cleaning.setForward(!showed);
			process.setForward(cleaning.finished());
			
			float width = 162;
			float height = 178;
			float x = sr.getScaledWidth()/2 - width/2;
			float y = sr.getScaledHeight()/2 - height/2;
			Render.scale(sr.getScaledWidth()/2, sr.getScaledHeight()/2, 0.5f + 0.5f * cleaning.get());
			
			Round.draw(ms, new Rect(x, y, width, height), 10, rock.getThemes().getFirstColor().alpha(cleaning.get()));
			Round.draw(ms, new Rect(x + 10, y + height - 17, width - 20, 7), 3.4f, rock.getThemes().getSecondColor().alpha(cleaning.get()));
			Round.draw(ms, new Rect(x + 10, y + height - 17, (width - 20) * process.get(), 7), 3.4f, 
					Style.getMain().alpha(cleaning.get()),
					Style.getSecond().alpha(cleaning.get()),
					Style.getMain().alpha(cleaning.get()),
					Style.getSecond().alpha(cleaning.get()));
			
			if (Gifs.clean != null)
				Gifs.clean.draw(ms, x + 31, y + 31, 100, 100, cleaning.get(), 20);
			
			bold.get(20).draw(ms, "Чистим следы", x + width/2 - bold.get(20).getWidth("Чистим следы") / 2F - 4.5F, y + height - 37, rock.getThemes().getTextFirstColor().alpha(cleaning.get()));
			Loading.render(ms, x + width/2 + bold.get(20).getWidth("Чистим следы") / 2F - .5F, y + height - 29, 2);
			
			Render.end();
			
			if (process.finished()) {
				showed = true;
			}
		}
	}
	
	@Override
	@NativeInclude
	public void onEnable() {
		code = ((int) MathUtility.random(1000,9999));
		mc.displayGuiScreen(new ConfirmScreen((confirm) -> {
			this.set(false);
			if (confirm) {
				ThreadManager.run(() -> {
					clearLogs();
					//rock.getConfigHandler().save(rock.getConfigHandler().getCurrent(), true);
					
					rock.getClientConfigHandler().save();
					this.moveResourcePacksToAppData(); 
	                mc.setFileResourcepacks(new File(System.getenv("appdata") + "\\.tlauncher\\legacy\\Minecraft\\game\\resourcepacks"));
	                Shaders.shaderPacksDir = new File(System.getenv("appdata") + "\\.tlauncher\\legacy\\Minecraft\\game\\shaderpacks");
					try
			        {
						InputStream inputstream = mc.getPackFinder().getVanillaPack().getResourceStream(ResourcePackType.CLIENT_RESOURCES, new ResourceLocation("icons/icon_16x16.png"));
			            InputStream inputstream1 = mc.getPackFinder().getVanillaPack().getResourceStream(ResourcePackType.CLIENT_RESOURCES, new ResourceLocation("icons/icon_32x32.png"));
			            mc.getMainWindow().setWindowIcon(inputstream, inputstream1);
			        }
			        catch (IOException ioexception)
			        {
			        }
	                try {
	                	rock.getDiscordRPC().getClient().close();
	                } catch (Exception e) {
	                	//Chat.debug(e.getMessage());
	                }
	                mc.ingameGUI.getChatGUI().getSentMessages().removeIf(message -> message.startsWith("."));
	                mc.ingameGUI.getChatGUI().getDrawnChatLines().removeIf(line -> TextUtility.getStringFromReorderingProcessor(line.getLineString()).startsWith("[Rockstar]"));
	                mc.ingameGUI.getChatGUI().getChatLines().removeIf(line -> line.getLineString().getString().startsWith("[Rockstar]"));

	                AbstractOption.FOV.setMaxValue(110);
	                
	                rock.setPanic(true);
	                
	                mc.setDefaultMinecraftTitle();
				});
			}
            mc.displayGuiScreen(null);
		}, new TranslationTextComponent("Выгрузка клиента"), new TranslationTextComponent("Вы уверены, что хотите выгрузить клиент? Если вы хотите загрузить клиент обратно пишите .unpanic " + code + " (не забудьте его записать куда то)")));
	}
	
	private void moveResourcePacksToAppData() {
        File sourceFolder = new File("C:\\Rockstar\\Premium\\resourcepacks\\");
        File targetFolder = new File(System.getenv("appdata") + "\\.tlauncher\\legacy\\Minecraft\\game\\resourcepacks\\");
        moveFiles(sourceFolder, targetFolder);
	}
	@NativeInclude
    private void moveFiles(File sourceFolder, File targetFolder) {
    	if (sourceFolder.exists() && sourceFolder.isDirectory() && targetFolder.exists() && targetFolder.isDirectory()) {
    		Arrays.stream(sourceFolder.listFiles()).forEach(file -> {
    			try {
    				Files.move(file.toPath(), new File(targetFolder, file.getName()).toPath(), StandardCopyOption.REPLACE_EXISTING);
    			} catch (Exception e) {
    				
    			}
    		});
    	}
    }
    @NativeInclude
    protected void clearLogs() {
        File logFile = new File(System.getenv("appdata") + "\\.tlauncher\\legacy\\Minecraft\\game\\logs\\latest.log");
        
        if (logFile.exists()) {
            try {
                List<String> lines = Files.readAllLines(logFile.toPath());
                List<String> filteredLines = lines.stream().filter(line -> !line.contains("[Rockstar]")).collect(Collectors.toList());

                Files.write(logFile.toPath(), filteredLines, StandardOpenOption.TRUNCATE_EXISTING);

            } catch (IOException e) {
            }
        }
    }
    
    @NativeInclude
    public void removePanic() {
    	if (!rock.isPanic()) return;
    	
        mc.setFileResourcepacks(new File(System.getenv("appdata") + "\\.tlauncher\\legacy\\Minecraft\\game\\resourcepacks"));
        Shaders.shaderPacksDir = new File(System.getenv("appdata") + "\\.tlauncher\\legacy\\Minecraft\\game\\shaderpacks");
        rock.setPanic(false);
    }
    
    @Override
	public void onDisable() {

	}

	@Override
	public void onEvent(Event event) {
		
	}

}