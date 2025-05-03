package fun.rockstarity.api.via.platform;

import java.io.File;
import java.util.logging.Logger;

import com.viaversion.viabackwards.api.ViaBackwardsPlatform;

import fun.rockstarity.api.via.ViaLoadingBase;

public class ViaBackwardsPlatformImpl implements ViaBackwardsPlatform {
    private final File directory;

    public ViaBackwardsPlatformImpl(File directory) {
        this.directory = directory;
        this.init(this.directory);
    }

    @Override
    public Logger getLogger() {
        return ViaLoadingBase.LOGGER;
    }

    @Override
    public boolean isOutdated() {
        return false;
    }

    @Override
    public void disable() {
    }

    @Override
    public File getDataFolder() {
        return this.directory;
    }
}
