package fun.rockstarity.api.waveycapes.config;

import fun.rockstarity.api.waveycapes.CapeMovement;
import fun.rockstarity.api.waveycapes.CapeStyle;
import fun.rockstarity.api.waveycapes.WindMode;

public class Config {
    public int configVersion = 2;
    public WindMode windMode = WindMode.NONE;
    public CapeStyle capeStyle = CapeStyle.SMOOTH;
    public CapeMovement capeMovement = CapeMovement.BASIC_SIMULATION;
    public int gravity = 25;
    public int heightMultiplier = 6;
    public int straveMultiplier = 2;
}
