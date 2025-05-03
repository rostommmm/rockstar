package fun.rockstarity.api.helpers.math;

import java.util.Random;

public class FastRandom extends Random {
    private long seed;

    public FastRandom(long seed) {
        this.seed = seed;
    }

    public FastRandom() {
        this(System.nanoTime());
    }

    @Override
    protected int next(int bits) {
        seed ^= (seed << 13);
        seed ^= (seed >>> 17);
        seed ^= (seed << 5);
        return (int) (seed & ((1L << bits) - 1));
    }
}
