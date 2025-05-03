package fun.rockstarity.api.scripts.wrappers.base;

import java.io.File;
import java.nio.file.FileSystem;

import fun.rockstarity.Rockstar;
import fun.rockstarity.api.IAccess;
import fun.rockstarity.api.helpers.secure.Web;
import fun.rockstarity.api.helpers.system.FileUtility;

public class FilesBase implements IAccess {
	public String read_web(String url) {
		return Web.read(url);
	}
	
	public String read_file(String path) {
		return FileUtility.readFile(new File(getPath(path)));
	}
	
	public void write_file(String url, String content) {
		FileUtility.writeFile(new File(getPath(url)), content);
	}
	
	public void download_file(String url, String path) {
		Web.downloadFile(url, getPath(path));
	}
	
	private String getPath(String path) {
		return path.contains(":/") ? path : rock.getPath() + "scripts/" + path;
	}
}