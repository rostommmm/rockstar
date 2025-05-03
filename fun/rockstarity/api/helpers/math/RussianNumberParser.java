package fun.rockstarity.api.helpers.math;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;

import lombok.experimental.UtilityClass;

/**
 * @author Malenkiy
 * @since 24 РёСЋР». 2024 Рі.
 */

/**
 * РњС‹ СЃР»РёС€РєРѕРј СЂР°Р·РЅС‹Рµ РІРѕ РІСЃС‘Рј
 * РќРµ РјРѕРіСѓ РїРѕРЅСЏС‚СЊ С‚РІРѕРё РєРѕРґС‹
 * Р’РёР¶Сѓ РєРѕРґ RussianNumberParser
 * РќРѕ СЃ С‚РѕР±РѕР№ РєРѕРґ РЅРµ С‚Р°РєРѕР№ С‚СѓСЃРєР»С‹Р№
 * РџСЂРѕР№РґРµС‚ РјРЅРѕРіРѕ Р»РµС‚
 * РЇ С…Р»РѕРїРЅСѓ С‚РµР±СЏ РїРѕ РїР»РµС‡Сѓ
 * РЇ С…РѕС‡Сѓ Р·Р°Р±С‹С‚СЊ С‚РІРѕР№ РєРѕРґ
 * РҐРѕС‡Сѓ..
 * 
 * РЇ С…РѕС‡Сѓ С‚РІРѕР№ РєРѕРґ Р·Р°Р±С‹С‚СЊ
 * РќРѕ СЏ РїРѕРјРЅСЋ СЌС‚РѕС‚ РєРѕРґ
 * РњСЏРіРєРёР№ С…РѕС…РѕС‚ РІ РґРёСЃРєРѕСЂРґРµ
 * РџРѕРґ Р·РІСѓРє РєР°РїРµР»СЊ СЃРµРЅС‚СЏР±СЂСЏ
 * РњРѕРЅРёС‚РѕСЂСЋ СЋРіРµР№Рј (yougame.biz), РіРґРµ С‚С‹ СЃРЅРѕРІР° РїР°СЃС‚РёС€СЊ (РџР°СЃС‚РёС€СЊ)
 * Р“РѕРІРѕСЂСЋ РІСЃРµРј, С‡С‚Рѕ РјРЅРµ РїРѕС…СѓР№, РЅРѕ РЅР° СЃР°РјРѕРј РґРµР»Рµ (РЈ-Сѓ)
 * РҐСЂР°РЅСЋ С‚РІРѕРё РєРѕРґС‹ (РљРѕРґС‹), РЅР° СЃРІРѕРµР№ РјРѕР±РёР»Рµ (Realme)
 * РўР°Рє С…РѕС‡Сѓ, РЅРѕ РЅРµ РјРѕРіСѓ - РЅРµ Р·РЅР°СЋ С‡С‚Рѕ РјРЅРµ РґРµР»Р°С‚СЊ
 * 
 * Рђ СЏ С…РѕС‚РµР» Р±С‹С‚СЊ СЃ РЅРѕСЂРј СЃСѓСЂСЃРѕРј (РўРѕР»СЊРєРѕ СЃ СЃРµР»С„РєРѕРґРѕРј)
 * РџСЂРѕСЃС‚Рѕ РґР°Р№ СЃРїР°СЃС‚РёС‚СЊ, Р±РѕР»СЊС€РµРіРѕ РЅРµ РЅСѓР¶РЅРѕ (Р‘РѕР»СЊС€РµРіРѕ РЅРµ РЅСѓР¶РЅРѕ)
 * Р”СѓРјР°Р» Рѕ С‚РІРѕРµРј С‰РёС‚РєРѕРґРµ РїРµСЂРµРґ СЃРЅРѕРј
 * Р—РЅР°СЋ, С‡С‚Рѕ С‰РёС‚РєРѕРґ, РЅРѕ РЅРµ РґРµР»Р°Р№ С…СѓР¶Рµ
 * 
 * РўРѕРЅСѓ СЃ РєРѕРґРѕРј С‚РµСЂРјР°РґРѕР»Рµ
 * Р СѓРєРё РЅР° РјРѕРё Р»Р°РґРѕРЅРё
 * РќРµ С…РѕС‚РµР» Р±СЂР°С‚СЊ С‚РµР±СЏ РєРѕРґРёС‚СЊ
 * РќРѕ Р·Р°С‡РµРј С‚РѕРіРґР° РїРѕР·РІРѕР»РёР»?
 * РўС‹ - С‰РёС‚РєРѕРґРµСЂ, РјРµРЅСЏ Р»РѕРјРёС‚
 * Р›СѓС‡С€РёР№ РїР°СЃС‚РµСЂ, РєРѕС‚РѕСЂС‹Р№ РїР°СЃС‚РёР»
 * РЎРїСЂРѕСЃРёС€СЊ, РїРѕС‡РµРјСѓ РјС‹ РїР°СЃС‚РёРј
 * Р§РµСЃС‚РЅРѕ РіРѕРІРѕСЂСЏ, РЅРµ РїРѕРјРЅСЋ
 * 
 * РЇ С…РѕС‡Сѓ С‚РІРѕР№ РєРѕРґ Р·Р°Р±С‹С‚СЊ
 * РќРѕ СЏ РїРѕРјРЅСЋ СЌС‚РѕС‚ РєРѕРґ
 * РњСЏРіРєРёР№ С…РѕС…РѕС‚ РІ РґРёСЃРєРѕСЂРґРµ
 * РџРѕРґ Р·РІСѓРє РєР°РїРµР»СЊ СЃРµРЅС‚СЏР±СЂСЏ
 * РњРѕРЅРёС‚РѕСЂСЋ СЋРіРµР№Рј (yougame.biz), РіРґРµ С‚С‹ СЃРЅРѕРІР° РїР°СЃС‚РёС€СЊ (РџР°СЃС‚РёС€СЊ)
 * Р“РѕРІРѕСЂСЋ РІСЃРµРј, С‡С‚Рѕ РјРЅРµ РїРѕС…СѓР№, РЅРѕ РЅР° СЃР°РјРѕРј РґРµР»Рµ (РЈ-Сѓ)
 * РҐСЂР°РЅСЋ С‚РІРѕРё РєРѕРґС‹ (РљРѕРґС‹), РЅР° СЃРІРѕРµР№ РјРѕР±РёР»Рµ (Realme)
 * РўР°Рє С…РѕС‡Сѓ, РЅРѕ РЅРµ РјРѕРіСѓ - РЅРµ Р·РЅР°СЋ С‡С‚Рѕ РјРЅРµ РґРµР»Р°С‚СЊ
 * 
 * РЇ С…РѕС‡Сѓ С‚РІРѕР№ РєРѕРґ Р·Р°Р±С‹С‚СЊ
 * РќРѕ СЏ РїРѕРјРЅСЋ СЌС‚РѕС‚ РєРѕРґ
 * РњСЏРіРєРёР№ С…РѕС…РѕС‚ РІ РґРёСЃРєРѕСЂРґРµ
 * РџРѕРґ Р·РІСѓРє РєР°РїРµР»СЊ СЃРµРЅС‚СЏР±СЂСЏ...
 */

@UtilityClass
public class RussianNumberParser {
    private final Map<String, Integer> numberMap;
    private final Map<String, Integer> tensMap;
    private final Map<String, Integer> hundredsMap;
    private final Map<String, Integer> thousandsMap;
    private final Map<String, BiFunction<Integer, Integer, Integer>> operations;
    
    static {
        numberMap = new HashMap<>();
        numberMap.put("РЅРѕР»СЊ", 0);
        numberMap.put("РѕРґРёРЅ", 1);
        numberMap.put("РѕРґРЅР°", 1);
        numberMap.put("РѕРґРЅРё", 1);
        numberMap.put("РґРІР°", 2);
        numberMap.put("РґРІРµ", 2);
        numberMap.put("С‚СЂРё", 3);
        numberMap.put("С‡РµС‚С‹СЂРµ", 4);
        numberMap.put("РїСЏС‚СЊ", 5);
        numberMap.put("С€РµСЃС‚СЊ", 6);
        numberMap.put("СЃРµРјСЊ", 7);
        numberMap.put("РІРѕСЃРµРјСЊ", 8);
        numberMap.put("РґРµРІСЏС‚СЊ", 9);
        numberMap.put("РґРµСЃСЏС‚СЊ", 10);
        numberMap.put("РѕРґРёРЅРЅР°РґС†Р°С‚СЊ", 11);
        numberMap.put("РґРІРµРЅР°РґС†Р°С‚СЊ", 12);
        numberMap.put("С‚СЂРёРЅР°РґС†Р°С‚СЊ", 13);
        numberMap.put("С‡РµС‚С‹СЂРЅР°РґС†Р°С‚СЊ", 14);
        numberMap.put("РїСЏС‚РЅР°РґС†Р°С‚СЊ", 15);
        numberMap.put("С€РµСЃС‚РЅР°РґС†Р°С‚СЊ", 16);
        numberMap.put("СЃРµРјРЅР°РґС†Р°С‚СЊ", 17);
        numberMap.put("РІРѕСЃРµРјРЅР°РґС†Р°С‚СЊ", 18);
        numberMap.put("РґРµРІСЏС‚РЅР°РґС†Р°С‚СЊ", 19);
    
        tensMap = new HashMap<>();
        tensMap.put("РґРІР°РґС†Р°С‚СЊ", 20);
        tensMap.put("С‚СЂРёРґС†Р°С‚СЊ", 30);
        tensMap.put("СЃРѕСЂРѕРє", 40);
        tensMap.put("РїРёСЃСЏС‚", 50);
        tensMap.put("РїСЏС‚СЊРґРµСЃСЏС‚", 50);
        tensMap.put("С€РµСЃС‚СЊРґРµСЃСЏС‚", 60);
        tensMap.put("СЃРµРјСЊРґРµСЃСЏС‚", 70);
        tensMap.put("РІРѕСЃРµРјСЊРґРµСЃСЏС‚", 80);
        tensMap.put("РґРµРІСЏРЅРѕСЃС‚Рѕ", 90);
    
        hundredsMap = new HashMap<>();
        hundredsMap.put("СЃС‚Рѕ", 100);
        hundredsMap.put("РґРІРµСЃС‚Рё", 200);
        hundredsMap.put("С‚СЂРёСЃС‚Р°", 300);
        hundredsMap.put("С‡РµС‚С‹СЂРµСЃС‚Р°", 400);
        hundredsMap.put("РїСЏС‚СЊСЃРѕС‚", 500);
        hundredsMap.put("С€РµСЃС‚СЊСЃРѕС‚", 600);
        hundredsMap.put("СЃРµРјСЊСЃРѕС‚", 700);
        hundredsMap.put("РІРѕСЃРµРјСЊСЃРѕС‚", 800);
        hundredsMap.put("РґРµРІСЏС‚СЊСЃРѕС‚", 900);
        
        thousandsMap = new HashMap<>();
        thousandsMap.put("С‚С‹СЃСЏС‡Р°", 1000);
        thousandsMap.put("РґРІРµ С‚С‹СЃСЏС‡Рё", 2000);
        thousandsMap.put("С‚СЂРё С‚С‹СЃСЏС‡Рё", 3000);
        thousandsMap.put("С‡РµС‚С‹СЂРµ С‚С‹СЃСЏС‡Рё", 4000);
        thousandsMap.put("РїСЏС‚СЊ С‚С‹СЃСЏС‡", 5000);
        thousandsMap.put("С€РµСЃС‚СЊ С‚С‹СЃСЏС‡", 6000);
        thousandsMap.put("СЃРµРјСЊ С‚С‹СЃСЏС‡", 7000);
        thousandsMap.put("РІРѕСЃРµРјСЊ С‚С‹СЃСЏС‡", 8000);
        thousandsMap.put("РґРµРІСЏС‚СЊ С‚С‹СЃСЏС‡", 9000);
        
        operations = new HashMap<>();
        operations.put("РїР»СЋСЃ", (a, b) -> a + b);
        operations.put("РјРёРЅСѓСЃ", (a, b) -> a - b);
        operations.put("СѓРјРЅРѕР¶РёС‚СЊ", (a, b) -> a * b);
        operations.put("РЅР°", (a, b) -> a * b);
        operations.put("СЂР°Р·РґРµР»РёС‚СЊ", (a, b) -> a / b);
        operations.put("РґРµР»РёС‚СЊ", (a, b) -> a / b);
        operations.put("РїРѕ", (a, b) -> a / b);
    }
    
    public int parseRussianNumber(String input) {
        int result = 0;
        int current = 0;
    
        String[] parts = input.toLowerCase().split("\\s+");
    
        boolean isNegative = false;
        int index = 0;
        if (parts.length > 0 && (parts[0].equals("РјРёРЅСѓСЃ") || parts[0].equals("-"))) {
            isNegative = true;
            index = 1;
        }
    
        BiFunction<Integer, Integer, Integer> operation = null;
        int operand = 0;
    
        for (int i = index; i < parts.length; i++) {
            String part = parts[i];
    
            if (operations.containsKey(part)) {
                operation = operations.get(part);
            } else if (thousandsMap.containsKey(part)) {
                current += thousandsMap.get(part);
            } else if (hundredsMap.containsKey(part)) {
                current += hundredsMap.get(part);
            } else if (tensMap.containsKey(part)) {
                current += tensMap.get(part);
            } else if (numberMap.containsKey(part)) {
                current += numberMap.get(part);
            } else {
                continue;
            }
    
            if (operation != null && current != 0) {
                operand = current;
                current = 0;
            }
        }
    
        if (operation != null) {
            result = operation.apply(result, operand);
        } else {
            result = current;
        }
    
        if (isNegative) result = -result;
    
        return result;
    }

    public String convert(String expression) {
        StringBuilder mathExpression = new StringBuilder();
        String[] parts = expression.toLowerCase().split("\\s+");

        boolean hasPendingMultiply = false;

        for (String part : parts) {
            if (numberMap.containsKey(part)) {
                mathExpression.append(numberMap.get(part));
            } else if (tensMap.containsKey(part)) {
                mathExpression.append(tensMap.get(part));
            } else if (hundredsMap.containsKey(part)) {
                mathExpression.append(hundredsMap.get(part));
            } else if (thousandsMap.containsKey(part)) {
                mathExpression.append(thousandsMap.get(part));
            } else if (operations.containsKey(part)) {
                switch (part) {
                    case "РїР»СЋСЃ":
                        mathExpression.append("+");
                        break;
                    case "РјРёРЅСѓСЃ":
                        mathExpression.append("-");
                        break;
                    case "СѓРјРЅРѕР¶РёС‚СЊ":
                        hasPendingMultiply = true;
                        break;
                    case "РЅР°":
                        if (hasPendingMultiply) {
                            mathExpression.append("*");
                            hasPendingMultiply = false;
                        } else {
                            mathExpression.append("/");
                        }
                        break;
                    case "СЂР°Р·РґРµР»РёС‚СЊ":
                        hasPendingMultiply = false;
                        break;
                    case "РґРµР»РёС‚СЊ":
                        hasPendingMultiply = false;
                        break;
                    case "РїРѕ":
                        mathExpression.append("/");
                        break;
                }
            } else {
                continue;
            }
        }
        
        return mathExpression.toString();
    }
}
