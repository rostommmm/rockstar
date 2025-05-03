package fun.rockstarity.api.via;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import com.viaversion.viaversion.api.protocol.version.ProtocolVersion;

import consts.NativeConsts;
import fun.rockstarity.api.IAccess;
import fun.rockstarity.api.via.ui.VersionSelectorElement;
import net.minecraft.util.text.ITextComponent;
import ru.kotopushka.j2c.sdk.annotations.NativeInclude;
import ru.kotopushka.j2c.sdk.annotations.VMProtect;
import ru.kotopushka.j2c.sdk.enums.VMProtectType;

public class ViaMCP implements IAccess {
	public int NATIVE_VERSION = 754;
    public List<ProtocolVersion> PROTOCOLS;
    public VersionSelectorElement viaScreen;
    
    @VMProtect(type=VMProtectType.ULTRA)
    @NativeInclude
    private void nativka56() {
    	List<ProtocolVersion> protocolList = ProtocolVersion.getProtocols().stream().filter(pv -> pv.getVersion() == 47 || pv.getVersion() >= 107).sorted((f, s) -> Integer.compare(s.getVersion(), f.getVersion())).toList();
        this.PROTOCOLS = new ArrayList<ProtocolVersion>(protocolList.size() + 1);
        this.PROTOCOLS.addAll(protocolList);
        ViaLoadingBase.ViaLoadingBaseBuilder.create().runDirectory(new File(NativeConsts.PATH)).nativeVersion(754).build();
        this.viaScreen = new VersionSelectorElement(mc.fontRenderer, 5,5,100,20, ITextComponent.getTextComponentOrEmpty("1.16.5"));
    }
    
    public ViaMCP() {
    	nativka56();
    }
    
    public int getNATIVE_VERSION() {
        return this.NATIVE_VERSION;
    }

    public List<ProtocolVersion> getPROTOCOLS() {
        return this.PROTOCOLS;
    }

    public VersionSelectorElement getViaScreen() {
        return this.viaScreen;
    }
}
