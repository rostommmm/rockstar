package fun.rockstarity.client.commands;

import com.mojang.blaze3d.matrix.MatrixStack;
import fun.rockstarity.api.commands.CmdInfo;
import fun.rockstarity.api.commands.Command;
import fun.rockstarity.api.events.Event;
import fun.rockstarity.api.events.list.game.EventChatScreen;
import fun.rockstarity.api.events.list.game.packet.EventReceivePacket;
import fun.rockstarity.api.events.list.render.EventRender2D;
import fun.rockstarity.api.events.list.render.ui.shaders.EventBlur;
import fun.rockstarity.api.helpers.game.Chat;
import fun.rockstarity.api.helpers.game.Server;
import fun.rockstarity.api.helpers.math.TimerUtility;
import fun.rockstarity.api.helpers.render.Render;
import fun.rockstarity.api.render.color.FixColor;
import fun.rockstarity.api.render.shaders.list.Round;
import fun.rockstarity.api.render.ui.rect.Rect;
import fun.rockstarity.client.modules.render.Interface;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import net.minecraft.network.play.server.SChatPacket;
import ru.kotopushka.j2c.sdk.annotations.NativeInclude;

import java.util.Objects;

@NativeInclude
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@CmdInfo(names = { "claninvest", "ci" }, desc = "Автоматически переводит определенную сумму по достижению суммы в клан")
public class ClanInvestCommand extends Command {

    public static String balance = "";
    public static long need = 0;
    private TimerUtility timer = new TimerUtility();
    @NonFinal
    public boolean enamelware = false;
    @NonFinal
    public int money = 0;

    @Override
    public void execute(String[] args) {
        if (args.length == 0) {
            Chat.msg(".claninvest <sum> или .ci <sum>");
            this.error();
            return;
        }

        String input = String.join(" ", args);

        need = Long.parseLong(input);

        try {
            if ((Server.isFT() || Server.is("spooky"))) {
                enamelware = true;
            } else {
                Chat.msg("Работает только для SpookyTime и FunTime");
                enamelware = false;
            }
        } catch (NumberFormatException e) {
            e.printStackTrace();
            this.error();
        }
    }

    @Override
    public void onEvent(Event event) {
        if (event instanceof EventReceivePacket e && e.getPacket() instanceof SChatPacket packet) {
            String message = packet.getChatComponent().getString().toLowerCase();
            if (message.contains("не находитесь в клане") ||
                message.contains(mc.player.getScoreboardName() + " пополнил баланс казны ") & !enamelware) enamelware = false;
        }

        float x = sr.getScaledWidth() - 12;

        float leftWidth = bold.get(16).getWidth("Цель: " + formatNumber(money) + " / " + formatNumber(need)) + 12;

        float progressWidth = (leftWidth - 8) * Math.min((float) money / need, 1.0f);

        if (event instanceof EventBlur e) {
            if (money < need && enamelware) Round.draw(e.getMatrixStack(), new Rect(x - leftWidth, 12, leftWidth, 20), 4, rock.getThemes().getFirstColor());
        }

        if (event instanceof EventRender2D e) {
            MatrixStack ms = e.getMatrixStack();

            if (Objects.equals(balance, "")) return;

            if (Server.isFT() && !balance.isEmpty())
                money = Integer.parseInt(balance.substring(0, balance.length() - 1));
            else if (Server.is("spooky"))
                money = Integer.parseInt(balance);

            if (money < need && enamelware) {

                Render.outline(ms, new Rect(x - leftWidth, 12, leftWidth, 20), 1);

                Render.glow(ms, new Rect(x - leftWidth, 12, leftWidth, 20), 1);

                Round.draw(ms, new Rect(x - leftWidth, 12, leftWidth, 20), 4, rock.getThemes().getFirstColor().alpha(0.5));

                Round.draw(ms, new Rect(x - leftWidth + 4, 27, leftWidth - 8, 2), 1.5f, rock.getThemes().getFirstColor().alpha(0.5));

                Round.draw(ms, new Rect(x - leftWidth + 4, 27, progressWidth, 2), 1.5f, rock.getModules().get(Interface.class).getColor().get());

                bold.get(16).draw(ms, "Цель: " + formatNumber(money) + " / " + formatNumber(need), x - leftWidth + 5, 14, rock.getThemes().getTextFirstColor());

            } else if (money >= need && timer.passed(300) && enamelware && !Server.hasCT() && (Server.isFT() || Server.is("spooky"))) {

                mc.player.sendChatMessage("/clan invest " + need);

                enamelware = false;
            }
        }

        if (event instanceof EventChatScreen e) {
            String input = e.getField().getText();
            String results = "";
            if (input.startsWith(".claninvest ") || input.startsWith(".ci ")) {
                String numberStr = input.replace(".claninvest ", "").replace(".ci ", "").trim();
                if (!numberStr.isEmpty() && numberStr.matches("\\d+")) {
                    try {
                        results = "Цель: " + formatNumber(Long.parseLong(numberStr));
                    } catch (NumberFormatException ex) {
                        results = "Неверный формат числа";
                    }
                } else {
                    results = "Введите число";
                }
            }
            bold.get(16).draw(e.getMatrixStack(), results, 3, sr.getScaledHeight() - 27, FixColor.WHITE);
        }
    }

    private String formatNumber(long number) {
        String numStr = String.valueOf(number);
        StringBuilder formatted = new StringBuilder();
        int count = 0;
        for (int i = numStr.length() - 1; i >= 0; i--) {
            formatted.insert(0, numStr.charAt(i));
            count++;
            if (count % 3 == 0 && i != 0) {
                formatted.insert(0, ".");
            }
        }
        return formatted.toString();
    }
}
