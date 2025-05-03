package fun.rockstarity.api.secure;

import fun.rockstarity.api.IAccess;
import lombok.experimental.UtilityClass;
import net.minecraft.util.text.TranslationTextComponent;
// Добавим импорты для вывода стектрейса, если понадобится альтернативный вывод
import java.io.PrintWriter;
import java.io.StringWriter;


/**
 * Утилитарный класс для вывода отладочной информации.
 * @author ConeTin
 * @since 5 июн. 2024 г.
 */
@UtilityClass // Эта аннотация Lombok делает все методы статическими
public class Debugger implements IAccess {

	/**
	 * Выводит информацию об исключении (включая стектрейс) в поток ошибок,
	 * если включен режим отладки.
	 * @param e Исключение для вывода.
	 */
	public void print(Exception e) {
		// Проверяем, включен ли режим отладки через IAccess `rock`
		if (rock != null && rock.isDebugging()) {
			// Выводим сообщение в стандартный поток ошибок (System.err),
			// т.к. это ошибка. Это часто выделяется красным в консолях IDE.
			System.err.println("[DEBUG] Exception occurred: ");
			// Печатаем полный стектрейс в поток ошибок.
			if (e != null) {
				e.printStackTrace(System.err);
			} else {
				System.err.println("[DEBUG] Attempted to print a null Exception.");
			}
		}
		// Если отладка выключена, ничего не делаем.
	}

	// --- ДОБАВЛЕННЫЙ МЕТОД ---
	/**
	 * Выводит отладочное сообщение в стандартный поток вывода,
	 * если включен режим отладки.
	 * @param message Сообщение для вывода.
	 */
	public void print(String message) {
		// Проверяем, включен ли режим отладки
		if (rock != null && rock.isDebugging()) {
			// Выводим сообщение в стандартный поток вывода (System.out)
			// Добавляем префикс [DEBUG] для ясности.
			System.out.println("[DEBUG] " + message);
		}
		// Если отладка выключена, ничего не делаем.
	}

	/**
	 * Отображает сообщение в оверлее игры, если включен режим отладки.
	 * @param msg Объект, который будет преобразован в строку и отображен.
	 */
	public void overlay(Object msg) {
		// Проверяем режим отладки
		if (rock != null && rock.isDebugging()) {
			// Добавляем проверку, что mc и ingameGUI инициализированы,
			// чтобы избежать NullPointerException при запуске игры.
			if (mc != null && mc.ingameGUI != null && msg != null) {
				try {
					// Создаем текстовый компонент (можно использовать и простой TextComponent)
					mc.ingameGUI.setOverlayMessage(new TranslationTextComponent(msg.toString()), false);
				} catch (Exception e) {
					// Если что-то пошло не так с созданием компонента, выведем ошибку
					print("Error displaying overlay message: " + msg.toString()); // Используем наш же print(String)
					print(e); // Используем print(Exception) для деталей
				}
			} else if (mc == null || mc.ingameGUI == null) {
				// Выведем сообщение, если игра еще не готова
				print("Cannot show overlay: Minecraft GUI not ready.");
			}
		}
	}

	// --- Опционально: Метод для печати сообщения вместе с исключением ---
	/**
	 * Выводит префиксное сообщение и информацию об исключении, если включена отладка.
	 * @param message Сообщение для префикса.
	 * @param e Исключение для вывода.
	 */
	public void print(String message, Exception e) {
		if (rock != null && rock.isDebugging()) {
			print(message); // Сначала выводим сообщение
			print(e);       // Затем выводим исключение
		}
	}

}