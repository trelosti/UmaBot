package minesweeper;

public class Cell {
    boolean isOpened;
    boolean isFlagged;

    Cell() {
        isOpened = false;
        isFlagged = false;
    }

    String getEmoji(int value) {
        if (isFlagged) {
            return ":triangular_flag_on_post:";
        }

        if (isOpened) {
            switch (value) {
                case -1:
                    return ":bomb:";
                case 0:
                    return ":white_large_square:";
                case 1:
                    return ":one:";
                case 2:
                    return ":two:";
                case 3:
                    return ":three:";
                case 4:
                    return ":four:";
                case 5:
                    return ":five:";
                case 6:
                    return ":six:";
                case 7:
                    return ":seven:";
                case 8:
                    return ":eight:";
            }
        }

        return ":green_square:";
    }


    void setOpened(boolean isOpened) {
        this.isOpened = isOpened;
    }

    void setFlagged(boolean isFlagged) {
        this.isFlagged = isFlagged;
    }
}
