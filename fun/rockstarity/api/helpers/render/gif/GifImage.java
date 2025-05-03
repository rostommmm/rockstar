package fun.rockstarity.api.helpers.render.gif;

import lombok.Getter;
import lombok.Setter;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


public class GifImage {


    @Getter
    private final GifDecoder decoder;
    private GifEncoder encoder;

    @Setter
    @Getter
    private File outputFile;

    @Getter
    private final List<BufferedImage> frames;

    public GifImage() {
        decoder = new GifDecoder();
        encoder = new GifEncoder();
        frames = new ArrayList<>();
    }


    public GifImage(File file) throws FileNotFoundException {
        this();
        loadFrom(file);
    }


    public int loadFrom(File file) throws FileNotFoundException {
        if (!file.exists()) throw new FileNotFoundException("File does not exist");
        return loadFrom(new FileInputStream(file));
    }


    public int loadFrom(InputStream stream) {
        frames.clear();
        encoder.setRepeat(0);
        int status = decoder.read(stream);
        for (int n = 0; n < decoder.getFrameCount(); n++) frames.add(decoder.getFrame(n));
        return status;
    }


    public void reverseFrames() {
        Collections.reverse(frames);
    }


    public void saveAllFrames(File outputDirectory) {
        saveAllFrames(outputDirectory, "png");
    }


    public void saveAllFrames(File outputDirectory, String format) {
        for (BufferedImage frame : getFrames()) {
            try {
                ImageIO.write(frame, format, new File(outputDirectory, "frame" + getFrames().indexOf(frame) + "." + format));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    public boolean save() {
        return finish();
    }


    public boolean finish() {
        if (outputFile == null) throw new NullPointerException("Output file is null");
        encoder = GifEncoder.cloneFrom(encoder);
        encoder.start(outputFile.getAbsolutePath());
        for (BufferedImage frame : frames) {
            encoder.addFrame(frame);
        }
        return encoder.finish();
    }


    public void setFrame(int index, BufferedImage frame) {
        frames.set(index, frame);
    }


    public void repeatInfinitely(boolean repeat) {
        if (encoder.isStarted()) throw new UnsupportedOperationException("Encoder has already started");
        encoder.setRepeat(repeat ? 0 : 1);
    }


    public void addFrame(BufferedImage frame) {
        frames.add(frame);
    }


    public void removeFrame(BufferedImage frame) {
        frames.remove(frame);
    }


    public void removeFrame(int frameIndex) {
        frames.remove(frameIndex);
    }


    public BufferedImage getFirstFrame() {
        return getFrame(0);
    }


    public BufferedImage getFrame(int index) {
        return getFrames().get(index);
    }


    public AnimatedGifEncoder getEncoder() {
        return encoder;
    }


    public void setRepeat(int iteration) {
        encoder.setRepeat(iteration);
    }


    public void setTransparent(Color color) {
        encoder.setTransparent(color);
    }


    public void setTransparent(Color color, boolean exactMatch) {
        setTransparent(color, exactMatch);
    }


    public void setQuality(int quality) {
        encoder.setQuality(quality);
    }


    public void setDelay(int ms) {
        encoder.setDelay(ms);
    }


    public void setBackground(Color background) {
        encoder.setBackground(background);
    }


    public void setFramerate(int fps) {
        encoder.setFrameRate(fps);
    }


    public void setSize(int width, int height) {
        encoder.setSize(width, height);
    }


    public void setDispose(int dispose) {
        encoder.setDispose(dispose);
    }
}