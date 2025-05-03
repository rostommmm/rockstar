package fun.rockstarity.api.helpers.render.gif;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.OutputStream;

public class GifEncoder extends AnimatedGifEncoder {


    public static GifEncoder cloneFrom(GifEncoder encoder) {
        GifEncoder gifEncoder = new GifEncoder();
        gifEncoder.setDelay(encoder.getDelay());
        gifEncoder.setRepeat(encoder.getRepeat());
        gifEncoder.setBackground(encoder.getBackground());
        gifEncoder.setDispose(encoder.getDispose());
        gifEncoder.setQuality(encoder.getSample());
        if (encoder.getWidth() >= 1 && encoder.getHeight() >= 1)
            gifEncoder.setSize(encoder.getWidth(), encoder.getHeight());
        gifEncoder.setTransparent(encoder.getTransparent(), encoder.isTransparentExactMatch());
        return gifEncoder;
    }


    public int getWidth() {
        return width;
    }


    public int getHeight() {
        return height;
    }


    public Color getTransparent() {
        return transparent;
    }


    public boolean isTransparentExactMatch() {
        return transparentExactMatch;
    }


    public Color getBackground() {
        return background;
    }


    public int getTransIndex() {
        return transIndex;
    }


    public int getRepeat() {
        return repeat;
    }


    public int getDelay() {
        return delay;
    }


    public OutputStream getOut() {
        return out;
    }


    public BufferedImage getImage() {
        return image;
    }


    public byte[] getPixels() {
        return pixels;
    }


    public byte[] getIndexedPixels() {
        return indexedPixels;
    }


    public int getColorDepth() {
        return colorDepth;
    }


    public byte[] getColorTab() {
        return colorTab;
    }


    public boolean[] getUsedEntry() {
        return usedEntry;
    }


    public int getPalSize() {
        return palSize;
    }


    public int getDispose() {
        return dispose;
    }


    public boolean isCloseStream() {
        return closeStream;
    }


    public boolean isFirstFrame() {
        return firstFrame;
    }


    private boolean isSizeSet() {
        return sizeSet;
    }


    private int getSample() {
        return sample;
    }
}