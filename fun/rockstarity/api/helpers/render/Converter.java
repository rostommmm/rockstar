package fun.rockstarity.api.helpers.render;

import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;

import javax.imageio.ImageIO;

import fun.rockstarity.api.secure.Debugger;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.texture.NativeImage;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.util.ResourceLocation;

/**
 * @author ConeTin
 * @since 4 дек. 2023 г.
 */

// Native
public class Converter {
	
	private final static HashMap<String, ResourceLocation> images = new HashMap<>();

	private final static Map<String, byte[]> streamCache = new HashMap<>();

	public static InputStream getInputStream(String link) {
		link = link.replaceAll("https://rockstar.moscow/", "").replaceAll("http://rockstar.moscow/", "");
		if (streamCache.containsKey(link))
			return new ByteArrayInputStream(streamCache.get(link));

		String resourcePath = (link.startsWith("/assets/") ? "" : "/assets/minecraft/rockstar/") + link;

		try (InputStream inputStream = Converter.class.getResourceAsStream(resourcePath)) {
			if (inputStream == null) {
				System.out.println("Файл не найден: " + resourcePath);
				return null;
			}

			try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
				byte[] buffer = new byte[1024];
				int bytesRead;
				while ((bytesRead = inputStream.read(buffer)) != -1) {
					outputStream.write(buffer, 0, bytesRead);
				}
				byte[] data = outputStream.toByteArray();
				streamCache.put(link, data);
				return new ByteArrayInputStream(data);
			}
		} catch (IOException e) {
			Debugger.print(e);
			System.out.println("Ошибка при чтении ресурса: " + resourcePath);
			return null;
		}
	}

	public static ResourceLocation getResourceLocation(String link) {
		link = link.replaceAll("https://rockstar.moscow/", "").replaceAll("http://rockstar.moscow/", "");
		if (images.containsKey(link))
			return images.get(link);

		NativeImage nativeImage = null;
		String resourcePath = (link.startsWith("/assets/") ? "" : "/assets/minecraft/rockstar/") + link;

		try (InputStream inputStream = Converter.class.getResourceAsStream(resourcePath)) {
			if (inputStream == null) {
				System.out.println("Файл не найден: " + resourcePath);
				return null;
			}

			nativeImage = NativeImage.read(inputStream);

		} catch (IOException e) {
			Debugger.print(e);
			System.out.println("Ошибка при загрузке картинки: " + resourcePath);
			return null;
		}

		TextureManager textureManager = Minecraft.getInstance().getTextureManager();
		DynamicTexture dynamicTexture = new DynamicTexture(nativeImage);
		ResourceLocation res = textureManager.getDynamicTextureLocation("custom", dynamicTexture);
		images.put(link, res);
		return res;
	}

/*    public static InputStream getInputStream(String link) {
        if (streamCache.containsKey(link))
            return new ByteArrayInputStream(streamCache.get(link));
        else {
            byte[] data = null;
            try {
                URL url = new URL(link);
                try (InputStream inputStream = url.openStream();
                     ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
                    byte[] buffer = new byte[1024];
                    int bytesRead;
                    while ((bytesRead = inputStream.read(buffer)) != -1) {
                        outputStream.write(buffer, 0, bytesRead);
                    }
                    data = outputStream.toByteArray();
                } catch (IOException e) {
                    Debugger.print(e);
    				System.out.println("Ошибка при загрузке картинки: " + link);
                    if (e.getMessage() != null && e.getMessage().contains("400"))
                        return getInputStream(link);
                }
            } catch (IOException e) {
                Debugger.print(e);
				System.out.println("Ошибка при загрузке картинки: " + link);
            }
            if (data != null) {
                streamCache.put(link, data);
                return new ByteArrayInputStream(data);
            }
            return null;
        }
    }*/
    
	/*public static ResourceLocation getResourceLocationFromUrl(String link) {
	    if (images.containsKey(link)) 
	        return images.get(link);
	    else {
	        NativeImage nativeImage = null;

	        try {
	            URL url = new URL(link);

	            try (InputStream inputStream = url.openStream();
	                 ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {

	                byte[] buffer = new byte[1024];
	                int bytesRead;

	                while ((bytesRead = inputStream.read(buffer)) != -1) {
	                    outputStream.write(buffer, 0, bytesRead);
	                }

	                byte[] imageData = outputStream.toByteArray();
	                nativeImage = NativeImage.read(new ByteArrayInputStream(imageData));
	            } catch (IOException e) {
	            	Debugger.print(e);
	            	
	            	if (e.getMessage().contains("400")) {
	            		return getResourceLocation(link);
	            	}
	            }
	        } catch (IOException e) {
	        	Debugger.print(e); 
				System.out.println("Ошибка при загрузке картинки: " + link);
	        }

	        TextureManager textureManager = Minecraft.getInstance().getTextureManager();
	        DynamicTexture dynamicTexture = new DynamicTexture(nativeImage);
	        ResourceLocation res = textureManager.getDynamicTextureLocation("custom", dynamicTexture);
	        images.put(link, res);
	        return res;
	    }
	}*/

	public static ResourceLocation getResourceLocationFromUrl(String link) {
		if (images.containsKey(link)) {
			return images.get(link);
		}

		NativeImage nativeImage = null;
		try {
			URL url = new URL(link);
			HttpURLConnection connection = (HttpURLConnection) url.openConnection();
			connection.setRequestMethod("GET");
			connection.setConnectTimeout(5000);
			connection.setReadTimeout(5000);

			if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
				System.err.println("HTTP Error " + connection.getResponseCode() + " for URL: " + link);
				return getDefaultResourceLocation();
			}

			String contentType = connection.getContentType();
			if (!contentType.startsWith("image/")) {
				System.err.println("Invalid content type: " + contentType + " for URL: " + link);
				return getDefaultResourceLocation();
			}

			try (InputStream inputStream = connection.getInputStream();
				 ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
				byte[] buffer = new byte[1024];
				int bytesRead;
				while ((bytesRead = inputStream.read(buffer)) != -1) {
					outputStream.write(buffer, 0, bytesRead);
				}
				byte[] imageData = outputStream.toByteArray();
				nativeImage = NativeImage.read(new ByteArrayInputStream(imageData));
			}
		} catch (IOException e) {
			System.err.println("Failed to load image from " + link + ": " + e.getMessage());
			return getDefaultResourceLocation();
		}

		if (nativeImage == null) {
			System.err.println("Failed to create NativeImage for URL: " + link);
			return getDefaultResourceLocation();
		}

		TextureManager textureManager = Minecraft.getInstance().getTextureManager();
		DynamicTexture dynamicTexture = new DynamicTexture(nativeImage);
		ResourceLocation res = textureManager.getDynamicTextureLocation("custom_" + link.hashCode(), dynamicTexture);
		textureManager.loadTexture(res, dynamicTexture);
		images.put(link, res);
		return res;
	}

	private static ResourceLocation getDefaultResourceLocation() {
		return new ResourceLocation("minecraft", "textures/misc/unknown.png");
	}
	
	public static ResourceLocation getFixed(String path) {
		if (images.containsKey(path)) 
	        return images.get(path);
	    else {
	    	ResourceLocation rs = new ResourceLocation(path);
	    	images.put(path, rs);
	        return rs;
	    }
	}

	public static BufferedImage loadImage(ResourceLocation resourceLocation) {
        try (InputStream inputStream = Minecraft.getInstance().getResourceManager().getResource(resourceLocation).getInputStream()) {
            return ImageIO.read(inputStream);
        } catch (IOException e) {
        	Debugger.print(e);
        	System.out.println("Ошибка при загрузке картинки: " + resourceLocation.getNamespace());
            return null;
        }
    }
	
	public static InputStream loadInputStream(String filePath) {
	    try (FileInputStream fileInputStream = new FileInputStream(new File(filePath));
	         ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
	        byte[] buffer = new byte[1024];
	        int bytesRead;
	        while ((bytesRead = fileInputStream.read(buffer)) != -1) {
	            outputStream.write(buffer, 0, bytesRead);
	        }
	        return new ByteArrayInputStream(outputStream.toByteArray());
	    } catch (IOException e) {
	        e.printStackTrace();
			System.out.println("Ошибка при загрузке: " + filePath);

	    }
	    return null;
	}


	public static NativeImage loadNativeImage(String filePath) {
		NativeImage nativeImage = null;

		try (FileInputStream fileInputStream = new FileInputStream(new File(filePath))) {
			ByteBuffer byteBuffer = ByteBuffer.allocateDirect(fileInputStream.available());
			byte[] buffer = new byte[1024];
			int bytesRead;

			while ((bytesRead = fileInputStream.read(buffer)) != -1) {
				byteBuffer.put(buffer, 0, bytesRead);
			}

			byteBuffer.flip();

			nativeImage = NativeImage.read(byteBuffer);
		} catch (IOException e) {
			e.printStackTrace();
			System.out.println("Ошибка при загрузке картинки: " + filePath);
		}

		return nativeImage;
	}

	public static final Font getFont(File file, int style, int size) {
		Font font = null;
		try {
			font = Font.createFont(Font.TRUETYPE_FONT, file)
				.deriveFont(style, size);
		} catch (FontFormatException | IOException e) {e.printStackTrace();}
		
		return font;
	}
	
	
}
