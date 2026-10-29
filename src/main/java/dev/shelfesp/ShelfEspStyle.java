package dev.shelfesp;

public enum ShelfEspStyle {
    BOXES(true, false),
    TRACERS(false, true),
    BOXES_AND_TRACERS(true, true);

    private final boolean boxes;
    private final boolean tracers;

    ShelfEspStyle(boolean boxes, boolean tracers) {
        this.boxes = boxes;
        this.tracers = tracers;
    }

    public boolean hasBoxes() {
        return boxes;
    }

    public boolean hasTracers() {
        return tracers;
    }
}
