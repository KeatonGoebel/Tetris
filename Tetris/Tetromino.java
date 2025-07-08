import java.awt.*;

public class Tetromino {
    private TetrominoType type;
    private int[][] shape;
    public int row, col;
    private Color color;

    public Tetromino(TetrominoType type) {
        this.type = type;
        this.row = 1;
        this.col = 8; // start in the center of the gamepanel
        initializeShapeAndColor();
    }

    private void initializeShapeAndColor() {
        switch (type) {
            case I:
                shape = new int[][] {
                    {0, 0, 0, 0},
                    {1, 1, 1, 1},
                    {0, 0, 0, 0},
                    {0, 0, 0, 0}
                };
                color = Color.decode("#094A7D");
                break;
            case O:
                shape = new int[][] {
                    {1, 1},
                    {1, 1}
                };
                color = Color.decode("#FDD055");
                break;
            case T:
                shape = new int[][] {
                    {0, 1, 0},
                    {1, 1, 1},
                    {0, 0, 0}
                };
                color = Color.decode("#006432");
                break;
            case L:
                shape = new int[][] {
                    {0, 0, 1},
                    {1, 1, 1},
                    {0, 0, 0}
                };
                color = Color.decode("#68AB4F");
                break;
            case J:
                shape = new int[][] {
                    {1, 0, 0},
                    {1, 1, 1},
                    {0, 0, 0}
                };
                color = Color.decode("#E23E57");
                break;
            case S:
                shape = new int[][] {
                    {0, 1, 1},
                    {1, 1, 0},
                    {0, 0, 0}
                };
                color = Color.decode("#E8772C");
                break;
            case Z:
                shape = new int[][] {
                    {1, 1, 0},
                    {0, 1, 1},
                    {0, 0, 0}
                };
                color = Color.decode("#E82CE8");
                break;
        }
    }

    public void rotateRight() {
        int n = shape.length;
        int[][] rotated = new int[n][n];
        for (int i = 0; i < n; i++)
            for (int j = 0; j < n; j++)
                rotated[j][n - 1 - i] = shape[i][j];
        shape = rotated;
    }

    public void rotateLeft() {
        int n = shape.length;
        int[][] rotated = new int[n][n];
        for (int i = 0; i < n; i++)
            for (int j = 0; j < n; j++)
                rotated[n - 1 - j][i] = shape[i][j];
        shape = rotated;
    }

    public void moveDown() {
        row++;
    }

    public void moveLeft() {
        col--;
    }

    public void moveRight() {
        col++;
    }

    public int[][] getShape() {
        return shape;
    }

    public Color getColor() {
        return color;
    }

    public int getRow() {
        return row;
    }

    public int getCol() {
        return col;
    }

    public TetrominoType getType() {
        return type;
    }
}

