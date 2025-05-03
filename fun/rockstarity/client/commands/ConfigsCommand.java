package fun.rockstarity.client.commands;

// import org.checkerframework.checker.units.qual.m; // This import seems unused, removing it.

import fun.rockstarity.api.commands.CmdInfo;
import fun.rockstarity.api.commands.Command;
import fun.rockstarity.api.commands.CommandParameter;
 import fun.rockstarity.api.configs.ConfigsHandler; // No longer using the handler directly for file operations
import fun.rockstarity.api.helpers.secure.Web; // Keep for the original 'dir' behavior if needed, but changing it
import ru.kotopushka.j2c.sdk.annotations.NativeInclude;

import java.awt.Desktop; // Needed for opening the directory
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.Comparator;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author ConeTin
 * @since 9 дек. 2023 г.
 *        Modified to use a local directory for config management.
 */
@NativeInclude
@CmdInfo(names = { "cfg", "cfgs", "config", "configs", "кфг", "конфиг", "сап" }, desc = "Позволяет управлять конфигами в локальной папке")
public class ConfigsCommand extends Command {

	// Define the target local directory - USE DOUBLE BACKSLASHES IN JAVA STRINGS
	private static final String TARGET_CONFIG_DIR_PATH = "C:\\Users\\VIP\\Desktop\\expensive-1.16.5-master\\amedir\\config";
	private static final Path TARGET_CONFIG_DIR = Paths.get(TARGET_CONFIG_DIR_PATH);
	// Define a common config extension (adjust if needed, e.g., ".json")
	private static final String CONFIG_EXTENSION = ".cfg";

	CommandParameter list = new CommandParameter(this, "list", "дшые");
	CommandParameter save = new CommandParameter(this, "save", "create", "add", "сохранить", "ыфму");
	CommandParameter load = new CommandParameter(this, "load", "use", "использовать", "дщфв");
	CommandParameter del = new CommandParameter(this, "delete", "remove", "del", "удалить", "вудуеу");
	CommandParameter dir = new CommandParameter(this, "dir", "folder", "папка"); // Changed 'direction' alias
	CommandParameter reset = new CommandParameter(this, "reset", "default", "сброс", "дефолт");

	@Override
	public void execute(String[] args) {
		// ConfigsHandler handler = rock.getConfigHandler(); // We are bypassing the original handler

		if (args == null || args.length == 0) {
			printUsage();
			return;
		}

		// Ensure the target directory exists
		if (!ensureConfigDirectoryExists()) {
			// Error message already printed by ensureConfigDirectoryExists()
			return;
		}

		String command = args[0].toLowerCase();
		String configName = args.length > 1 ? args[1] : null;
		// String potentialArg3 = args.length > 2 ? args[2] : null; // Original load had a 3rd arg

		try {
			if (contains(command, save)) {
				if (configName == null) {
					printFeedback("Ошибка: Необходимо указать имя конфига для сохранения. Пример: cfg save <имя>");
					return;
				}
				handleSave(configName);
			} else if (contains(command, load)) {
				if (configName == null) {
					printFeedback("Ошибка: Необходимо указать имя конфига для загрузки. Пример: cfg load <имя>");
					return;
				}
				// Original load had args[2] - ignoring it for now as its purpose isn't clear
				// if (potentialArg3 != null) {
				//     handleLoad(configName, potentialArg3); // Need to define what this would do
				// } else {
				handleLoad(configName);
				// }
			} else if (contains(command, del)) {
				if (configName == null) {
					printFeedback("Ошибка: Необходимо указать имя конфига для удаления. Пример: cfg delete <имя>");
					return;
				}
				handleDelete(configName);
			} else if (contains(command, list)) {
				handleList();
			} else if (contains(command, dir)) {
				handleDir();
			} else if (contains(command, reset)) {
				handleReset();
			} else {
				printUsage();
			}
		} catch (IOException e) {
			printFeedback("Ошибка файловой операции: " + e.getMessage());
			// Consider logging the stack trace e.printStackTrace();
		}
	}

	private boolean ensureConfigDirectoryExists() {
		if (Files.notExists(TARGET_CONFIG_DIR)) {
			try {
				Files.createDirectories(TARGET_CONFIG_DIR);
				printFeedback("Создана папка для конфигов: " + TARGET_CONFIG_DIR_PATH);
				return true;
			} catch (IOException e) {
				printFeedback("Ошибка: Не удалось создать папку для конфигов: " + TARGET_CONFIG_DIR_PATH);
				printFeedback("Детали ошибки: " + e.getMessage());
				return false;
			}
		} else if (!Files.isDirectory(TARGET_CONFIG_DIR)) {
			printFeedback("Ошибка: Путь для конфигов существует, но не является папкой: " + TARGET_CONFIG_DIR_PATH);
			return false;
		}
		return true;
	}

	private Path getConfigPath(String name) {
		// Ensure the name ends with the standard extension
		String filename = name.endsWith(CONFIG_EXTENSION) ? name : name + CONFIG_EXTENSION;
		// Sanitize name to prevent directory traversal (basic example)
		filename = filename.replaceAll("[^a-zA-Z0-9_.-]", "_");
		if (filename.isEmpty() || filename.equals(CONFIG_EXTENSION)) {
			throw new IllegalArgumentException("Недопустимое имя файла конфигурации.");
		}
		return TARGET_CONFIG_DIR.resolve(filename);
	}

	private void handleList() throws IOException {
		printFeedback("Конфиги в папке: " + TARGET_CONFIG_DIR_PATH);
		try (Stream<Path> stream = Files.list(TARGET_CONFIG_DIR)) {
			String fileList = stream
					.filter(Files::isRegularFile)
					// Optionally filter by extension:
					.filter(path -> path.toString().toLowerCase().endsWith(CONFIG_EXTENSION))
					.map(path -> path.getFileName().toString())
					.collect(Collectors.joining(", "));

			if (fileList.isEmpty()) {
				printFeedback("  (пусто)");
			} else {
				printFeedback("  " + fileList);
			}
		}
	}

	private void handleSave(String configName) throws IOException {
		Path configFile = getConfigPath(configName);

		// --- CRITICAL LIMITATION ---
		// This section CANNOT save the actual application state like ConfigsHandler likely did.
		// It only creates/overwrites a file. You would need to integrate with
		// your application's actual config serialization logic here.
		// For demonstration, we'll just write a placeholder.
		// --- END LIMITATION ---

		String placeholderContent = "// Config saved on: " + java.time.LocalDateTime.now() + "\n"
				+ "// This is placeholder content. Actual saving requires application integration.\n";

		Files.write(configFile, placeholderContent.getBytes(StandardCharsets.UTF_8),
				StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING); // Create or overwrite

		printFeedback("Конфиг '" + configFile.getFileName() + "' сохранен (как плейсхолдер) в " + TARGET_CONFIG_DIR_PATH);
	}

	private void handleLoad(String configName) throws IOException {
		Path configFile = getConfigPath(configName);

		if (Files.notExists(configFile) || !Files.isRegularFile(configFile)) {
			printFeedback("Ошибка: Конфиг '" + configName + "' не найден в " + TARGET_CONFIG_DIR_PATH);
			return;
		}

		// --- CRITICAL LIMITATION ---
		// This section CANNOT apply the loaded settings to the application state.
		// It only reads the file. You would need to integrate with your application's
		// actual config deserialization and application logic here.
		// --- END LIMITATION ---

		// Example: Read content (but don't apply it)
		// String content = new String(Files.readAllBytes(configFile), StandardCharsets.UTF_8);
		// printFeedback("Содержимое " + configFile.getFileName() + ":\n" + content); // Optional: show content

		printFeedback("Конфиг '" + configFile.getFileName() + "' найден. (Загрузка состояния приложения НЕ реализована)");
		// In a real scenario, you'd parse 'content' and update application settings here.
	}

	private void handleDelete(String configName) throws IOException {
		Path configFile = getConfigPath(configName);

		if (Files.notExists(configFile)) {
			printFeedback("Конфиг '" + configName + "' не найден для удаления.");
			return;
		}

		try {
			Files.delete(configFile);
			printFeedback("Конфиг '" + configFile.getFileName() + "' удален из " + TARGET_CONFIG_DIR_PATH);
		} catch (NoSuchFileException e) {
			printFeedback("Конфиг '" + configName + "' не найден для удаления (возможно, удален одновременно).");
		} catch (IOException e) {
			printFeedback("Ошибка при удалении конфига '" + configFile.getFileName() + "': " + e.getMessage());
			throw e; // Re-throw for main catch block
		}
	}

	private void handleDir() {
		printFeedback("Открытие папки: " + TARGET_CONFIG_DIR_PATH);
		try {
			if (Desktop.isDesktopSupported()) {
				Desktop desktop = Desktop.getDesktop();
				if (Files.exists(TARGET_CONFIG_DIR)) {
					desktop.open(TARGET_CONFIG_DIR.toFile());
				} else {
					printFeedback("Папка еще не создана. Попробуйте сохранить конфиг сначала.");
					// Optionally try opening the parent directory
					if (Files.exists(TARGET_CONFIG_DIR.getParent())) {
						desktop.open(TARGET_CONFIG_DIR.getParent().toFile());
					}
				}
			} else {
				printFeedback("Ошибка: Функция открытия папки не поддерживается на этой системе.");
				// Fallback to original web behavior?
				// Web.openWebpage("https://rockstar.moscow/profile");
			}
		} catch (IOException e) {
			printFeedback("Ошибка при открытии папки: " + e.getMessage());
		} catch (UnsupportedOperationException e) {
			printFeedback("Ошибка: Функция открытия папки не поддерживается на этой системе.");
		}
	}

	private void handleReset() throws IOException {
		printFeedback("Сброс конфигов в папке " + TARGET_CONFIG_DIR_PATH + " (удаление файлов *" + CONFIG_EXTENSION + ")");
		int deleteCount = 0;
		try (Stream<Path> stream = Files.list(TARGET_CONFIG_DIR)) {
			// Create a list first to avoid issues with modifying the directory while iterating
			var filesToDelete = stream
					.filter(Files::isRegularFile)
					.filter(path -> path.toString().toLowerCase().endsWith(CONFIG_EXTENSION))
					.collect(Collectors.toList());

			if (filesToDelete.isEmpty()) {
				printFeedback("Нет файлов (" + CONFIG_EXTENSION + ") для удаления.");
				return;
			}

			for (Path file : filesToDelete) {
				try {
					Files.delete(file);
					printFeedback("  Удален: " + file.getFileName());
					deleteCount++;
				} catch (IOException e) {
					printFeedback("  Ошибка при удалении " + file.getFileName() + ": " + e.getMessage());
					// Continue attempting to delete others
				}
			}
		}
		printFeedback("Сброс завершен. Удалено файлов: " + deleteCount);
		// --- NOTE ---
		// This does NOT reset the application's in-memory state to default.
		// That would require separate logic calling the application's reset mechanisms.
	}

	// Helper to print feedback (replace with actual game chat/log mechanism if available)
	private void printFeedback(String message) {
		System.out.println("[ConfigCmd] " + message); // Use System.out as a placeholder
		// If 'rock' object has a chat or log method, use it instead:
		// rock.getChat().print(message);
		// rock.getLogger().info(message);
	}

	private void printUsage() {
		printFeedback("Использование команды Config:");
		printFeedback("  cfg list                - Показать список конфигов в " + TARGET_CONFIG_DIR_PATH);
		printFeedback("  cfg save <имя>          - Сохранить текущий конфиг (плейсхолдер) как <имя>" + CONFIG_EXTENSION);
		printFeedback("  cfg load <имя>          - Загрузить конфиг <имя>" + CONFIG_EXTENSION + " (не применяет настройки)");
		printFeedback("  cfg delete <имя>        - Удалить конфиг <имя>" + CONFIG_EXTENSION);
		printFeedback("  cfg dir                 - Открыть папку с конфигами (" + TARGET_CONFIG_DIR_PATH + ")");
		printFeedback("  cfg reset               - Удалить все конфиги (" + CONFIG_EXTENSION + ") из папки");
	}

	// Keep the contains helper method from the original class (or ensure it's accessible)
	public boolean contains(String arg, CommandParameter param) {
		if (arg == null || param == null) return false;
		String lowerArg = arg.toLowerCase();
		for (String name : param.getNames()) {
			if (lowerArg.equals(name.toLowerCase())) {
				return true;
			}
		}
		return false;
	}

	// Assuming CommandParameter has a getNames() method like this:
	// public class CommandParameter {
	//     private String[] names;
	//     // ... constructor and other methods
	//     public String[] getNames() { return names; }
	// }
}