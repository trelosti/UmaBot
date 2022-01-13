package minesweeper;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

import java.util.ArrayList;

public class Minefield {
    private static final int MINES = 10;

    private static final int FIELD_WIDTH = 8;
    private static final int FIELD_HEIGHT = 8;

    static final int MINE_VALUE = -1;
    static final int EMPTY_VALUE = 0;

    private GuildMessageReceivedEvent e;
    private boolean isActive;
    private final int mines;
    private int cellsToUncover;
    private final int width;
    private final int height;
    private int flags;
    private boolean isMined;
    private int shutdownCounter;
    private long channelId;

    private final boolean[][] uncoveredField;
    private final boolean[][] flaggedField;
    private final int[][] field;
    private final Cell[][] cells;

    int[][] getField() {
        return field;
    }

    boolean[][] getUncoveredField() {
        return uncoveredField;
    }

    boolean[][] getFlaggedField() {
        return flaggedField;
    }

    Cell[][] getCells() {
        return cells;
    }

    int getMines() {
        return mines;
    }

    int getCellsToUncover() {
        return cellsToUncover;
    }

    int getFlags() {
        return flags;
    }

    int getShutdownCounter() { return shutdownCounter; }

    long getChannelId() {
        return channelId;
    }

    boolean isActive() {
        return isActive;
    }

    boolean checkIfMined() {
        return isMined;
    }

    void markAsMined() {
        this.isMined = true;
    }

    void decrementCellsToUncover() {
        this.cellsToUncover--;
    }

    void incrementNumberOfFlags() {
        this.flags++;
    }

    void incrementShutdownCounter() { this.shutdownCounter++; }

    void restartShutdownCounter() {
        this.shutdownCounter = 0;
    }

    void decrementNumberOfFlags() {
        this.flags--;
    }

    void disableField() {
        this.isActive = false;
    }

    Minefield(GuildMessageReceivedEvent e, long channelId) {
        this.mines = MINES;
        this.width = FIELD_WIDTH;
        this.height = FIELD_HEIGHT;
        this.cellsToUncover = height * width - mines;
        this.flags = 0;
        uncoveredField = new boolean[height][width];
        flaggedField = new boolean[height][width];
        field = new int[height][width];
        cells = new Cell[height][width];
        for (int i = 0; i < cells.length; i++) {
            for (int j = 0; j < cells[0].length; j++) {
                cells[i][j] = new Cell();
            }
        }
        this.isMined = false;
        this.e = e;
        this.isActive = true;
        this.shutdownCounter = 0;
        this.channelId = channelId;
    }

    static final int[][] SHIFTS = { // shifts to find neighbouring cells
            {-1, -1, -1, 0, 0, 0, 1, 1, 1},
            {-1, 0, 1, -1, 0, 1, -1, 0, 1}
    };

    void present() {
        EmbedBuilder builder = new EmbedBuilder()
                .setTitle("Minesweeper (" + e.getAuthor().getName() + ")")
                .setFooter("Flags: " + this.flags);
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                builder.appendDescription(cells[y][x].getEmoji(field[y][x]));
            }
            builder.appendDescription("\n");
        }

        e.getChannel().sendMessage(builder.build()).queue();
    }

    void mineField(int selX, int selY) {
        ArrayList<Integer[]> potentialMinePlaces = new ArrayList<>();
        for (int y = 0; y < height; y++) {
            outer:
            for (int x = 0; x < width; x++) {
                for (int i = 0; i < Minefield.SHIFTS[0].length; i++) {
                    int ny = selY + Minefield.SHIFTS[0][i];
                    int nx = selX + Minefield.SHIFTS[1][i];
                    if (x == nx && y == ny) {
                        break outer;
                    }
                }

                potentialMinePlaces.add(new Integer[]{y, x});
            }
        }

        for (int i = 0; i < mines; i++) {
            int randomIndex = (int) (Math.random() * potentialMinePlaces.size());
            Integer[] mine = potentialMinePlaces.get(randomIndex);
            potentialMinePlaces.remove(randomIndex);

            int y = mine[0];
            int x = mine[1];
            field[y][x] = Minefield.MINE_VALUE;
            for (int j = 0; j < Minefield.SHIFTS[0].length; j++) {
                int ny = y + Minefield.SHIFTS[0][j];
                int nx = x + Minefield.SHIFTS[1][j];
                if (Minefield.areCoordsInside(field, nx, ny) && field[ny][nx] != Minefield.MINE_VALUE) {
                    ++field[ny][nx];
                }
            }
        }
    }

    private static boolean areCoordsInside(int[][] field, int x, int y) {
        int fieldWidth = field[0].length;
        int fieldHeight = field.length;

        return x >= 0 && x < fieldWidth && y >= 0 && y < fieldHeight;
    }

    void floodUncover(int selX, int selY) {
        for (int i = 0; i < SHIFTS[0].length; i++) {
            int ny = selY + SHIFTS[0][i];
            int nx = selX + SHIFTS[1][i];
            if (areCoordsInside(field, nx, ny) && !uncoveredField[ny][nx]) {
                if (flaggedField[ny][nx]) {
                    this.flaggedField[ny][nx] = !flaggedField[ny][nx];
                    this.cells[ny][nx].setFlagged(false);
                    this.decrementNumberOfFlags();
                }
                this.uncoveredField[ny][nx] = true;
                this.cells[ny][nx].setOpened(true);
                this.cellsToUncover--;
                if (field[ny][nx] == EMPTY_VALUE) {
                    floodUncover(nx, ny);
                }
            }
        }
    }

    void exposeField() {
        for (int y = 0; y < uncoveredField.length; y++) {
            for (int x = 0; x < uncoveredField[0].length; x++) {
                cells[y][x].setOpened(true);
                cells[y][x].setFlagged(false);
            }
        }

        this.present();

        for (int y = 0; y < uncoveredField.length; y++) {
            for (int x = 0; x < uncoveredField[0].length; x++) {
                if (!uncoveredField[y][x]) {
                    cells[y][x].setOpened(false);
                }
                if (flaggedField[y][x]) {
                    cells[y][x].setFlagged(true);
                }
            }
        }
    }

    void flagCell(int selY, int selX) {
        if (!this.flaggedField[selY][selX]) {
            this.incrementNumberOfFlags();
            this.flaggedField[selY][selX] = true;
            this.cells[selY][selX].setFlagged(true);
        }
    }

    void unflagCell(int selY, int selX) {
        if (this.flaggedField[selY][selX]) {
            this.decrementNumberOfFlags();
            this.flaggedField[selY][selX] = false;
            this.cells[selY][selX].setFlagged(false);
        }
    }
}
