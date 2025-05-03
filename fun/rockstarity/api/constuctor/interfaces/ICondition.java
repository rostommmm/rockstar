package fun.rockstarity.api.constuctor.interfaces;

public interface ICondition {
    boolean check();
    String getName();
    default ICondition invert() {
        return new ICondition() {
            @Override
            public boolean check() {
                return !ICondition.this.check();
            }

            @Override
            public String getName() {
                return "РќРµ " + ICondition.this.getName();
            }
        };
    }
}