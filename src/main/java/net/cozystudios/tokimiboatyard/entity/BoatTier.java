package net.cozystudios.tokimiboatyard.entity;

import java.util.Locale;

public enum BoatTier {
    COPPER(   "Copper",    4, 1.25f, "copper_ingot",    false, 0xCC8D67),
    IRON(     "Iron",      5, 1.50f, "iron_ingot",      false, 0xEBEBEB),
    GOLD(     "Gold",      6, 1.75f, "gold_ingot",      false, 0xEFB244),
    EMERALD(  "Emerald",   7, 2.00f, "emerald",         false, 0x00D062),
    DIAMOND(  "Diamond",   8, 2.50f, "diamond",         false, 0x34EBC9),
    NETHERITE("Netherite", 9, 3.00f, "netherite_ingot", true,  0x31292A);

    private final String displayName;
    private final int rows;
    private final float speedMultiplier;
    private final String ingredient;
    private final boolean fireproof;
    private final int tooltipColor;

    BoatTier(String displayName, int rows, float speedMultiplier,
             String ingredient, boolean fireproof, int tooltipColor) {
        this.displayName = displayName;
        this.rows = rows;
        this.speedMultiplier = speedMultiplier;
        this.ingredient = ingredient;
        this.fireproof = fireproof;
        this.tooltipColor = tooltipColor;
    }

    public String displayName() { return displayName; }
    public int rows() { return rows; }
    public int slots() { return rows * 9; }
    public float speedMultiplier() { return speedMultiplier; }
    public String ingredient() { return ingredient; }
    public boolean fireproof() { return fireproof; }
    public int tooltipColor() { return tooltipColor; }

    public String id() { return name().toLowerCase(Locale.ROOT); }

    public BoatTier previous() {
        return ordinal() == 0 ? null : values()[ordinal() - 1];
    }

    public static BoatTier fromId(int id) {
        BoatTier[] values = values();
        if (id < 0 || id >= values.length) return COPPER;
        return values[id];
    }

    public String blockTexturePath() {
        return switch (this) {
            case COPPER    -> "textures/block/copper_block.png";
            case IRON      -> "textures/block/iron_block.png";
            case GOLD      -> "textures/block/gold_block.png";
            case EMERALD   -> "textures/block/emerald_block.png";
            case DIAMOND   -> "textures/block/diamond_block.png";
            case NETHERITE -> "textures/block/netherite_block.png";
        };
    }
}
