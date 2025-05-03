package fun.rockstarity.api.helpers.system;

import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import java.util.Random;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

import fun.rockstarity.api.helpers.secure.Web;
import fun.rockstarity.api.render.ui.fonts.FontSize;
import lombok.experimental.UtilityClass;
import net.minecraft.util.IReorderingProcessor;

/**
 * @author Malecharik
 * @since 16 Mar 2024 13:23:04
 */


@UtilityClass
public class TextUtility {
	
	private final List<String> prefixes = Arrays.asList(
	        "The", "Super", "Mega", "Ultra", "Power", "Master", "Great",
	        "Hyper", "Quantum", "Atomic", "Cosmic", "Turbo", "Mighty",
	        "Fantastic", "Legendary", "Epic", "Glorious", "Incredible",
	        "Marvelous", "Supreme", "Stellar", "Dynamic", "Heroic",
	        "Valiant", "Brave", "Noble", "Radiant", "Brilliant", "Bold",
	        "Fearless", "Fierce", "Savage", "Infinit", "Storm", "Thunder",
	        "Lightning", "Solar", "Lunar", "Galactic", "Nebula", "Phoenix",
	        "Titan", "Colossal", "Majestic", "Regal", "Royal", "Sovereign",
	        "Auroral", "Divine", "Ethereal", "Fiery", "Flaming",
	        "Gigahertz", "Hypersonic", "Infernal", "Jovial", "Kaleidoscopic",
	        "Luminous", "Magnetic", "Nebulous", "Olympian", "Pulsar", "Quasar",
	        "Radiant", "Spectral", "Stellar", "Tachyon", "Umbra", "Vortex",
	        "Warp", "Xenon", "Yellowstone", "Zephyr", "Masha"
	    );

	    private final List<String> adjectives = Arrays.asList(
	        "Swift", "Fierce", "Sneaky", "Brave", "Savage", "Fearless", "Stealthy",
	        "Valiant", "Bold", "Cunning", "Mighty", "Noble", "Resolute", "Vigilant",
	        "Relentless", "Intrepid", "Daring", "Gallant", "Tenacious", "Ferocious",
	        "Unyielding", "Audacious", "Courageous", "Indomitable", "Dauntless",
	        "Unstoppable", "Determined", "Invincible", "Unbreakable", "Epic",
	        "Legendary", "Mythic", "Heroic", "Glorious", "Triumphant", "Fearsome",
	        "Imposing", "Stalwart", "Stout", "Steadfast", "Grim", "Resolute",
	        "Fateful", "Loyal", "Trusty", "Staunch", "Hardy", "Doughty",
	        "Unflinching", "Unfaltering", "Brisk", "Keen", "Alert", "Quick",
	        "Agile", "Nimble", "Lithe", "Spry", "Energetic", "Vibrant",
	        "Dynamic", "Lively", "Sprightly", "Active", "Forceful", "Vigorous",
	        "Spirited", "Animated", "Robust", "Brawny", "Muscular", "Husky",
	        "Strong", "Tough", "Solid", "Sturdy", "Hefty", "Powerful",
	        "Mighty", "Colossal", "Gigantic", "Mammoth", "Titanic", "Towering",
	        "Massive", "Monumental", "Heroic", "Bravehearted", "Gutsy", "Doughty",
	        "Unyielding", "Unwavering", "Iron-willed", "Strong-willed", "Unshakeable", "Xuesosina"
	    );

	    private final List<String> animals = Arrays.asList(
	        "Wolf", "Tiger", "Lion", "Eagle", "Panther", "Dragon", "Phoenix",
	        "Bear", "Leopard", "Hawk", "Falcon", "Cheetah", "Jaguar", "Griffin",
	        "Raven", "Fox", "Shark", "Viper", "Cobra", "Falcon", "Crocodile",
	        "Raptor", "Condor", "Lynx", "Ocelot", "Cougar", "Puma", "Hound",
	        "Bison", "Mammoth", "Rhino", "Buffalo", "Stallion", "Mustang",
	        "Pegasus", "Wyvern", "Cerberus", "Minotaur", "Chimera", "Hydra",
	        "Kraken", "Basilisk", "Manticore", "Unicorn", "Sphinx", "Grizzly",
	        "Kodiak", "Polar Bear", "Sabertooth", "Direwolf", "Orca",
	        "Narwhal", "Walrus", "Beluga", "Elephant", "Hippo", "Gorilla",
	        "Orangutan", "Chimpanzee", "Baboon", "Mongoose", "Ferret",
	        "Weasel", "Otter", "Badger", "Wolverine", "Honey Badger", "Lizard",
	        "Iguana", "Gecko", "Komodo Dragon", "Monitor Lizard", "Tortoise",
	        "Turtle", "Alligator", "Caiman", "Anaconda", "Python", "Boa",
	        "Eel", "Swordfish", "Marlin", "Barracuda", "Piranha", "Penguin",
	        "Albatross", "Seagull", "Pelican", "Stork", "Heron", "Flamingo", "MasTyp6ek"
	    );

	    private final List<String> suffixes = Arrays.asList(
	        "Gamer", "Player", "Ninja", "Warrior", "Champion", "Legend", "Hero",
	        "Master", "Conqueror", "Slayer", "Guardian", "Knight", "Paladin",
	        "Crusader", "Ranger", "Assassin", "Mage", "Sorcerer", "Wizard",
	        "Enchanter", "Necromancer", "Berserker", "Gladiator", "Samurai",
	        "Viking", "Pirate", "Outlaw", "Mercenary", "Hunter", "Scout",
	        "Rogue", "Thief", "Sentinel", "Protector", "Savior", "Defender",
	        "Avenger", "Warlord", "Commander", "Captain", "General", "Marshal",
	        "Overlord", "Monarch", "Emperor", "King", "Queen", "Prince",
	        "Princess", "Duke", "Duchess", "Baron", "Baroness", "Lord", "Lady",
	        "Warden", "Sentinel", "Crusader", "Champion", "Virtuoso", "Adept",
	        "Prodigy", "Savant", "Genius", "Maven", "Whiz", "Ace",
	        "Virtuoso", "Expert", "Specialist", "Technician", "Strategist",
	        "Tactician", "Operative", "Agent", "Spy", "Infiltrator", "Saboteur",
	        "Shadow", "Phantom", "Specter", "Shade", "Mystic", "Seer",
	        "Oracle", "Prophet", "Visionary", "Dreamer", "Illusionist",
	        "Conjurer", "Invoker", "Diviner", "Alchemist", "Shaman", "Druid",
	        "Elementalist", "Geomancer", "Pyromancer", "Hydromancer", "Aeromancer",
	        "Archon", "Brawler", "Catalyst", "Dynamo", "Energizer", "Flux",
	        "Fusion", "Gizmo", "Hacker", "Innovator", "Juggernaut", "Kinetix",
	        "Luminary", "Marauder", "Nomad", "Operator", "Pioneer", "Quickshot",
	        "Rascal", "Slasher", "Titan", "Umbra", "Vanguard", "Warden", "Pro",
	        "Xenon", "Yokai", "Zealot", "Zorro", "Zoltar"
	    );
	    
	private static final Random random = new Random();
    
	public static String getRandomNick() {
	    String prefix = getRandomElement(prefixes);
	    String adjective = getRandomElement(adjectives);
	    String animal = getRandomElement(animals);
	    String suffix = getRandomElement(suffixes);
	    String year = (random.nextInt(100) < 30) ? String.valueOf(2000 + random.nextInt(26)) : "";

	    List<String> parts = new ArrayList<>();
	    if (random.nextBoolean()) parts.add(prefix);
	    if (random.nextBoolean()) parts.add(adjective);
	    if (random.nextBoolean()) parts.add(animal);
	    if (random.nextBoolean()) parts.add(suffix);
	    
	    if (parts.isEmpty()) parts.add(prefix);
	    if (parts.size() < 2) parts.add(random.nextBoolean() ? adjective : animal);

	    String nickname = String.join("", parts) + year;

	    if (random.nextInt(100) < 20) {
	        nickname += random.nextBoolean() ? "52" : "69";
	    } else {
	        nickname += generateNumbers(2 + random.nextInt(3));
	    }

	    // Обрезаем ник, если он длиннее 16 символов
	    if (nickname.length() > 16) {
	        nickname = nickname.substring(nickname.length() - 16);
	    }

	    return nickname;
	}
    
    private String getRandomElement(List<String> list) {
        return list.get(random.nextInt(list.size()));
    }
    
    private String generateNumbers(int length) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < length; i++) {
            sb.append(random.nextInt(10));
        }
        return sb.toString();
    }
	
	public String makeGender(String parent) {
		
		if (parent.endsWith("а")) return "а"; // ru
		if (parent.endsWith("a")) return "а"; // en
		if (parent.endsWith("y")) return "о";
		if (parent.endsWith("я")) return "а";
		if (parent.endsWith("ы")) return "ы";
		if (parent.endsWith("и")) return "ы";
		
		return "";
	}
	
	public String formatNumber(double number) {
	    if (number == (int) number) {
	        // Целое число: возвращаем без точки и нуля
	        return String.valueOf((int) number);
	    } else {
	        // Форматируем с двумя знаками после точки и удаляем лишние нули
	        String formatted = String.format("%.2f", number)
	            .replace(",", ".")                        // Замена запятой на точку
	            .replaceAll("\\.?0+$", "");               // Удаление хвостовых нулей и точки
	        
	        // Обработка случаев вроде "123." (оставшихся после удаления нулей)
	        return formatted.endsWith(".") 
	            ? formatted.replace(".", "") 
	            : formatted;
	    }
	}
	
	public String formatNumberOld(double number) {
		String formatted = String.format("%.1f", number);
        return formatted;
	}
	
    public void copyText(String text) {
        Clipboard clip = Toolkit.getDefaultToolkit().getSystemClipboard();
        StringSelection strse1 = new StringSelection(text);
        clip.setContents(strse1, strse1);
    }
    
    public int levenshteinDistance(String input, String output) {
        int lenS1 = input.length();
        int lenS2 = output.length();
        int[][] dp = new int[lenS1 + 1][lenS2 + 1];

        for (int i = 0; i <= lenS1; i++) {
            dp[i][0] = i;
        }
        for (int j = 0; j <= lenS2; j++) {
            dp[0][j] = j;
        }

        for (int i = 1; i <= lenS1; i++) {
            for (int j = 1; j <= lenS2; j++) {
                int cost = (input.charAt(i - 1) == output.charAt(j - 1)) ? 0 : 1;
                dp[i][j] = Math.min(Math.min(dp[i - 1][j] + 1, dp[i][j - 1] + 1), dp[i - 1][j - 1] + cost);
            }
        }

        return dp[lenS1][lenS2];
    }
    

    public String getStringFromReorderingProcessor(IReorderingProcessor reorderingProcessor) {
        StringBuilder stringBuilder = new StringBuilder();
        reorderingProcessor.accept((index, style, codePoint) -> {
            stringBuilder.append(Character.toChars(codePoint));
            return true;
        });
        return stringBuilder.toString();
    }
    
    public boolean isNumeric(String str) {
    	return str.matches("-?\\d+(\\.\\d+)?"); 
    }
    
    private static final String secret = "PassWorld";
	private static byte[] key;
	private static SecretKeySpec secretKey;

	public String encrypt(String strToEncrypt) 
    {
        try
        {
            setKey(secret);
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
            cipher.init(Cipher.ENCRYPT_MODE, secretKey);
            return Base64.getEncoder().encodeToString(cipher.doFinal(strToEncrypt.getBytes("UTF-8")));
        } 
        catch (Exception e) 
        {
            System.out.println("Error while encrypting: " + e.toString());
        }
        return null;
    }
 
    public String decrypt(String strToDecrypt) 
    {
        try
        {
            setKey(secret);
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5PADDING");
            cipher.init(Cipher.DECRYPT_MODE, secretKey);
            return new String(cipher.doFinal(Base64.getDecoder().decode(strToDecrypt)));
        } 
        catch (Exception e) 
        {
        	//e.printStackTrace();
           // System.out.println("Error while decrypting: " + e.toString());
        }
        return null;
    }
    
    public static void setKey(String myKey) 
    {
        MessageDigest sha = null;
        try {
            key = myKey.getBytes("UTF-8");
            sha = MessageDigest.getInstance("SHA-1");
            key = sha.digest(key);
            key = Arrays.copyOf(key, 16); 
            secretKey = new SecretKeySpec(key, "AES");
        } 
        catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } 
        catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }
    
    public String fixWidth(String text, FontSize font, float maxWidth) {
        if (font.getWidth(text) <= maxWidth) {
            return text;
        }
        String ellipsis = "..";
        float ellipsisWidth = font.getWidth(ellipsis);
        if (ellipsisWidth > maxWidth) {
            return "";
        }
        int lo = 0;
        int hi = text.length();
        int best = 0;
        while (lo <= hi) {
            int mid = (lo + hi) / 2;
            int prefixCount = mid / 2;
            int suffixCount = mid - prefixCount;
            String candidate = text.substring(0, prefixCount) + ellipsis + text.substring(text.length() - suffixCount);
            if (font.getWidth(candidate) <= maxWidth) {
                best = mid;
                lo = mid + 1;
            } else {
                hi = mid - 1;
            }
        }
        int prefixCount = best / 2;
        int suffixCount = best - prefixCount;
        return text.substring(0, prefixCount) + ellipsis + text.substring(text.length() - suffixCount);
    }
    
    public String formatTime(long milliseconds) {
        long totalSeconds = milliseconds / 1000;
        long days = totalSeconds / 86400;
        totalSeconds %= 86400;
        long hours = totalSeconds / 3600;
        totalSeconds %= 3600;
        long minutes = totalSeconds / 60;
        long seconds = totalSeconds % 60;
        StringBuilder sb = new StringBuilder();
        if (days > 0) {
            sb.append(days).append(" ").append(getWord(days, "день", "дня", "дней")).append(" ");
        }
        if (hours > 0) {
            sb.append(hours).append(" ").append(getWord(hours, "час", "часа", "часов")).append(" ");
        }
        if (minutes > 0) {
            sb.append(minutes).append(" ").append(getWord(minutes, "минуту", "минуты", "минут")).append(" ");
        }
        if (seconds > 0 || sb.length() == 0) {
            sb.append(seconds).append(" ").append(getWord(seconds, "секунду", "секунды", "секунд")).append(" ");
        }
        return sb.toString().trim();
    }

    private String getWord(long number, String form1, String form2, String form5) {
        long n = number % 100;
        if (n >= 11 && n <= 19) {
            return form5;
        }
        n = number % 10;
        if (n == 1) {
            return form1;
        }
        if (n >= 2 && n <= 4) {
            return form2;
        }
        return form5;
    }
    
    public static List<String> wrapText(String text, FontSize font, float maxWidth) {
        List<String> lines = new ArrayList<>();
        while (font.getWidth(text) > maxWidth) {
            int lastSpace = -1;
            for (int i = 0; i < text.length(); i++) {
                if (font.getWidth(text.substring(0, i)) > maxWidth) {
                    break;
                }
                if (text.charAt(i) == ' ') {
                    lastSpace = i;
                }
            }
            if (lastSpace == -1) lastSpace = text.length();
            lines.add(text.substring(0, lastSpace));
            text = text.substring(lastSpace).trim();
        }
        lines.add(text);
        return lines;
    }
    
}
