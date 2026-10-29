package dev.shelfesp;

/**
 * User-facing settings, persisted as JSON in {@code config/shelfesp.json}.
 *
 * <p>The fields are public and non-final on purpose: Gson reads and writes them
 * directly, which keeps the config file readable and easy to edit by hand.</p>
 */
public final class ShelfEspConfig {
    public boolean enabled = true;

    /** Which visual elements to draw for a shelf that contains items. */
    public ShelfEspStyle style = ShelfEspStyle.BOXES;

    /** Highlight colour as {@code #RRGGBB}. */
    public String color = "#FFB300";

    /** Alpha (0-255) of the translucent box fill. */
    public int fillAlpha = 40;

    /** Alpha (0-255) of the box outline. */
    public int outlineAlpha = 220;

    /** Alpha (0-255) of the optional tracer lines. */
    public int tracerAlpha = 170;

    /** Only shelves within this many blocks of the player are highlighted. */
    public double maxDistance = 64.0D;

    /** How often (in client ticks) the loaded shelves are rescanned. */
    public int updateIntervalTicks = 10;

    public boolean drawFill = true;
    public boolean drawOutline = true;

    public void validate() {
        if (style == null) {
            style = ShelfEspStyle.BOXES;
        }
        if (color == null || !color.matches("#[0-9a-fA-F]{6}")) {
            color = "#FFB300";
        } else {
            color = color.toUpperCase();
        }

        fillAlpha = clamp(fillAlpha, 0, 255);
        outlineAlpha = clamp(outlineAlpha, 0, 255);
        tracerAlpha = clamp(tracerAlpha, 0, 255);
        updateIntervalTicks = clamp(updateIntervalTicks, 1, 200);
        if (!Double.isFinite(maxDistance)) {
            maxDistance = 64.0D;
        }
        maxDistance = Math.max(8.0D, Math.min(256.0D, maxDistance));
    }

    public int rgb() {
        return Integer.parseInt(color.substring(1), 16);
    }

    public int argb(int alpha) {
        return (clamp(alpha, 0, 255) << 24) | rgb();
    }

    private static int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }
}
