package icu.suc.realinvisibility;

import java.util.Set;

public abstract class Settings<T> {
    public final boolean mainhand;
    public final boolean offhand;
    public final boolean boots;
    public final boolean leggings;
    public final boolean chestplate;
    public final boolean helmet;
    public final boolean body;
    public final boolean particles;
    public final boolean arrows;
    public final boolean equipment;
    public final boolean metadata;
    public final Set<T> slots;

    public Settings(
            boolean mainhand,
            boolean offhand,
            boolean boots,
            boolean leggings,
            boolean chestplate,
            boolean helmet,
            boolean body,
            boolean particles,
            boolean arrows
    ) {
        this.mainhand = mainhand;
        this.offhand = offhand;
        this.boots = boots;
        this.leggings = leggings;
        this.chestplate = chestplate;
        this.helmet = helmet;
        this.body = body;
        this.particles = particles;
        this.arrows = arrows;
        this.equipment = mainhand || offhand || boots || leggings || chestplate || helmet || body;
        this.metadata = particles || arrows;
        this.slots = Set.copyOf(slots(mainhand, offhand, boots, leggings, chestplate, helmet, body));
    }

    protected abstract Set<T> slots(
            boolean mainhand,
            boolean offhand,
            boolean boots,
            boolean leggings,
            boolean chestplate,
            boolean helmet,
            boolean body
    );

    @FunctionalInterface
    public interface Slots<T> {
        Set<T> slots(
                boolean mainhand,
                boolean offhand,
                boolean boots,
                boolean leggings,
                boolean chestplate,
                boolean helmet,
                boolean body
        );
    }
}
