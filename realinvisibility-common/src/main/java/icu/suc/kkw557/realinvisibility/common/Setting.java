package icu.suc.kkw557.realinvisibility.common;

public enum Setting {
    /**
     * Hide items in mainhand?
     */
    MAINHAND("mainhand", "Hide items in mainhand?"),
    /**
     * Hide items in offhand?
     */
    OFFHAND("offhand", "Hide items in offhand?"),
    /**
     * Hide boots?
     */
    FEET("feet", "Hide boots?"),
    /**
     * Hide leggings?
     */
    LEGS("legs", "Hide leggings?"),
    /**
     * Hide chestplates?
     */
    CHEST("chest", "Hide chestplates?"),
    /**
     * Hide helmets?
     */
    HEAD("head", "Hide helmets?"),
    /**
     * Hide animals' armors?
     */
    BODY("body", "Hide animals' armors?"),
    /**
     * Hide saddles?
     */
    SADDLE("saddle", "Hide saddles?"),
    /**
     * Hide fire in the body?
     */
    FIRE("fire", "Hide fire in the body?"),
    /**
     * Hide effect particles?
     */
    EFFECT_PARTICLES("effect_particles", "Hide effect particles?"),
    /**
     * Hide arrows in the body?
     */
    ARROWS("arrows", "Hide arrows in the body?"),
    /**
     * Hide stingers in the body?
     */
    STINGERS("stingers", "Hide stingers in the body?");

    private final String name;
    private final String comments;

    Setting(String name, String comments) {
        this.name = name;
        this.comments = comments;
    }

    public String getName() {
        return name;
    }

    public String getComments() {
        return comments;
    }
}
