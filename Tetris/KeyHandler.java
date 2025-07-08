
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

public class KeyHandler implements KeyListener{
    
    GamePanel gp;
    Tetromino currentTetromino; 

    public KeyHandler(GamePanel gp) {
        this.gp = gp;
    }

    @Override
    public void keyTyped(KeyEvent e){
    }

    @Override
    public void keyPressed(KeyEvent e){
        if (gp.currentTetromino == null) return;

        int code = e.getKeyCode();
        Tetromino t = gp.currentTetromino;

        if (code == KeyEvent.VK_DOWN) {
            if (!onBottom(t)) {
                if(!gp.collidesAt(t.getRow() + 1, t.getCol())){
                    t.moveDown();
                }
                
            }
        }

        if (code == KeyEvent.VK_LEFT) {
            if (!onLeftWall(t)) {
                if(!gp.collidesAt(t.getRow(), t.getCol() - 1)){
                    t.moveLeft();
                }
            }
        }

        if (code == KeyEvent.VK_RIGHT) {
            if (!onRightWall(t)) {
                if(!gp.collidesAt(t.getRow(), t.getCol() + 1)){
                    t.moveRight();
                }
            }
        }

        if (code == KeyEvent.VK_A) {
            t.rotateLeft();
        }

        if (code == KeyEvent.VK_D) {
            t.rotateRight();  
        }

        // Hard drop
        if (code == KeyEvent.VK_SPACE) {
            int row = t.getRow();
            while(!gp.collidesAt(row + 1, t.getCol()) && !onBottom(t)){
                row ++;
            }
            t.row = row;
        }

        // save Piece
        if (code == KeyEvent.VK_TAB){
            gp.savePiece();
        }
    }


    @Override
    public void keyReleased(KeyEvent e){
    }

    public boolean onLeftWall(Tetromino t) {
        int[][] shape = t.getShape();
        int col = t.getCol();

        for (int i = 0; i < shape.length; i++) {
            for (int j = 0; j < shape[i].length; j++) {
                if (shape[i][j] == 1) {
                    if (col + j - 1 == 2) {
                        return true;
                    }
                }
            }
        }
        return false;
}

    public boolean onRightWall(Tetromino t) {
        int[][] shape = t.getShape();
        int col = t.getCol();

        for (int i = 0; i < shape.length; i++) {
            for (int j = 0; j < shape[i].length; j++) {
                if (shape[i][j] == 1) {
                    if (col + j + 1 == gp.maxScreenCol - 3) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public boolean onBottom(Tetromino t) {
        int[][] shape = t.getShape();
        int row = t.getRow();

        for (int i = 0; i < shape.length; i++) {
            for (int j = 0; j < shape[i].length; j++) {
                if (shape[i][j] == 1) {
                    if (row + i + 1 == gp.maxScreenRow - 1) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

}
