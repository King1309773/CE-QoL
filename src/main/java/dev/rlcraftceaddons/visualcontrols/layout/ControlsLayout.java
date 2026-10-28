package dev.rlcraftceaddons.visualcontrols.layout;

/** Shared geometry for drawing and hitting rows in the reference layout. */
public final class ControlsLayout {
    public static final int ROW_HEIGHT = 24;
    public final boolean compact;
    public final int boardX, boardY, boardWidth, boardHeight;
    public final int searchX, searchY, searchWidth;
    public final int listX, listY, listWidth, listBottom;
    public final int bindingX, bindingWidth, clearX, clearWidth, resetX, resetWidth;
    public final int footerY, filterWidth, resetAllX, resetAllWidth, doneX, doneWidth;
    private ControlsLayout(int width, int height) {
        if (width < 1 || height < 1) throw new IllegalArgumentException("Window dimensions must be positive");
        compact = width < 320 || height < 240;
        if (compact) {
            boardX = boardY = boardWidth = boardHeight = 0;
            searchX = searchY = searchWidth = 0;
            listX = listY = listWidth = listBottom = 0;
            bindingX = bindingWidth = clearX = clearWidth = resetX = resetWidth = 0;
            footerY = Math.max(0, height - 24);
            filterWidth = resetAllX = resetAllWidth = 0;
            doneWidth = Math.min(100, width);
            doneX = (width - doneWidth) / 2;
            return;
        }
        searchWidth = Math.min(width - 24, Math.max(160, Math.min(340, width * 40 / 100)));
        searchX = (width - searchWidth) / 2;
        searchY = 23;
        boardY = 41;
        boardWidth = Math.min(width * 80 / 100, (height - 171) * 90 / 25);
        boardHeight = boardWidth * 25 / 90;
        boardX = (width - boardWidth) / 2;
        listWidth = Math.min(width - 24, Math.max(270, width * 68 / 100));
        listX = (width - listWidth) / 2;
        listY = boardY + boardHeight + 15;
        listBottom = height - 32;
        bindingWidth = Math.max(60, listWidth * 28 / 100);
        clearWidth = 14;
        resetWidth = width < 480 ? 36 : 54;
        resetX = listX + listWidth - 8 - resetWidth;
        clearX = resetX - 3 - clearWidth;
        bindingX = clearX - 3 - bindingWidth;
        footerY = height - 25;
        filterWidth = width < 400 ? 38 : 54;
        doneWidth = width < 400 ? 64 : 80;
        doneX = width - 8 - doneWidth;
        resetAllWidth = width < 400 ? 74 : 94;
        resetAllX = doneX - 4 - resetAllWidth;
    }
    public static ControlsLayout create(int width, int height) { return new ControlsLayout(width, height); }
    public int rowTop(int visibleRow) { return listY + visibleRow * ROW_HEIGHT; }
    public int pageSize() { return compact ? 0 : Math.max(1, (listBottom - listY) / ROW_HEIGHT); }
}
