import org.junit.Assert;
import org.junit.Test;
import minesweeper.*;

public class MinesweeperTest {
    @Test
    public void newCellShouldBeHiddenAndUnflagged()
    {
        Cell cell = new Cell();
        Assert.assertFalse(cell.isFlagged());
        Assert.assertFalse(cell.isOpened());
    }
}
