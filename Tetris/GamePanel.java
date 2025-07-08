
import javax.crypto.spec.PSource;
import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.Random;

public class GamePanel extends JPanel implements Runnable{

    // Screen Settings
    final int originalTilesize = 16;
    final int scale = 2; 
    final int tileSize = originalTilesize * scale; // 32 X 32 
    public final int maxScreenCol = 19; // make sure col x row is at a 4x3 ratio
    public final int maxScreenRow = 20;
    final int screenWidth  = tileSize * maxScreenCol; // 320 pixel width
    final int screenHeight = tileSize * maxScreenRow; // 640 pixel height
    Color lightBlue = new Color(167,200,216);

    // Starting postion and bounds of tetromino
    final int START_GAME_X = screenWidth / 2 - 100;
    final int START_GAME_Y = 32;

    int FPS = 60;
    private long lastFallTime = System.currentTimeMillis();
    private int fallInterval = 1000; //miliseconds -> 1 second

    KeyHandler keyH = new KeyHandler(this);

    Thread gameThread;

    boolean gameover = false;
    JButton restartButton;

    TetrominoType[] types = TetrominoType.values();
    public Color[][] board = new Color[maxScreenRow][maxScreenCol];
    public Tetromino[] pieces = new Tetromino[3]; // array of pieces coming up next, 9 can fit in the screen
    Tetromino currentTetromino;

    // Score
    String scoreText = "Score: ";
    String score = "0";
    final int scoreXPos = 96;
    final int scoreYPos = 32;

    // High Score
    String highScoreText = "High Score: ";
    String highScore = "0";
    final int highScoreXPos = 224;
    final int highScoreYPos = 32;

    // Saved piece box
    String savedText = "Saved";
    String savedPieceText = "";
    final int savedPieceTextXPos = 10;
    final int savedPieceTextYPos = 32; 
    final int savedPieceXPos = 16;
    final int savedPieceYPos = 45;
    final int savedTypeXPos = 32;
    final int savedTypeYPos = 96;
    Tetromino savedTetromino = null;
    boolean savedThisTurn = false;

    // Next
    final int nextPieceXPos = screenWidth - 78;
    final int nextPieceYPos = 32;
    String nextPieceText = "Next";
    final int nextPieceOffsetX = 400;
    final int nextPieceOffsetY = 32;

    public GamePanel() {

        this.setPreferredSize(new Dimension(screenWidth, screenHeight));
        this.setBackground(Color.lightGray);
        this.setDoubleBuffered(true);
        this.addKeyListener(keyH);
        this.setFocusable(true);
        this.requestFocusInWindow();

        for(int r = 0; r < maxScreenRow; r++) {
            for(int c = 0; c < maxScreenCol; c++) {
                board[r][c] = Color.lightGray; // initialize the board
            }
        }

    }

    public void startGameThread() {

        if (gameThread == null || !gameThread.isAlive()) {
            gameThread = new Thread(this);
            gameThread.start();

            // Get Starting Piece
            TetrominoType randomType = types[new Random().nextInt(types.length)];
            currentTetromino = new Tetromino(randomType);
            System.out.println("Starting Piece is ");
            System.out.println(currentTetromino.getType());
            
            // Get Pieces coming up next
            for (int i = 0; i < pieces.length; i++) {
                pieces[i] = spawnNewTetromino();
                System.out.println("Next Piece " + (i) + " is " + pieces[i].getType());
            }
                
        }

    }

    @Override
    public void run() {

        double drawInterval = 1000000000/FPS;
        double delta = 0;
        long lastTime = System.nanoTime();
        long currentTime;
        long timer = 0;
        long drawCount = 0;

        while(gameThread != null){ // while gameThread exits, loop

            currentTime = System.nanoTime();

            delta += (currentTime - lastTime) / drawInterval;
            timer += (currentTime - lastTime);
            lastTime = currentTime;

            if(!gameover) {
                if (delta >= 1) {
                    // 1 Update: information in game
                    // 2 Draw: the screen with updated information
                    update();
                    repaint();
                    delta--;
                    drawCount++;
                }
            }

            if(timer >= 1000000000){
                drawCount = 0;
                timer = 0;
            }
        }
    }

    public void update() {
        if (gameover || currentTetromino == null) return;

        long currentTime = System.currentTimeMillis();
        if (currentTime - lastFallTime >= fallInterval) {

            // if it is not at the bottom or hitting another tetromino, move down every second
            // else save it to game board and spawn a new one
            if (!collidesAt(currentTetromino.getRow() + 1, currentTetromino.getCol())) {
                currentTetromino.moveDown();
            } else {
                lockTetrominoToBoard();
                checkBoard();

                // get the next piece from the array, shift the array, and spawn a new piece
                currentTetromino = pieces[0];
                for (int i = 0; i < pieces.length - 1; i++){
                    pieces[i] = pieces[i + 1]; 
                    System.out.println("Next Piece " + (i) + " is " + pieces[i].getType());
                }
                pieces[pieces.length - 1] = spawnNewTetromino();

                // Make the game get faster everytime it spawns a new piece
                fallInterval -= 10;
                //System.out.println("fall Interval: " + fallInterval);

            }
            lastFallTime = currentTime;
        }

        if (gameover) {
            showRestartButton();
            if (Integer.parseInt(score) > Integer.parseInt(highScore)) {
                highScore = score;
            }
            score = "0";
            savedTetromino = null;
            savedPieceText = "";
        }
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;

        // 1. Draw locked tiles from the board
        for (int row = 0; row < maxScreenRow; row++) {
            for (int col = 0; col < maxScreenCol; col++) {
                g2.setColor(board[row][col]);
                int x = col * tileSize;
                int y = row * tileSize;
                g2.fillRect(x, y, tileSize, tileSize);
                g2.setColor(Color.BLACK);
                g2.drawRect(x, y, tileSize, tileSize); // grid lines
            }
        }

        // 2. Draw the currently falling Tetromino
        if (currentTetromino != null) {
            drawTetromino(g2, currentTetromino, tileSize,0, 0);
        }

        // 3. Draw border
        g2.setColor(lightBlue);
        for (int i = 0; i < maxScreenRow; i++) {
            int y = i * tileSize;
            g2.fillRect(0, y, tileSize, tileSize); // left border
            g2.fillRect(32, y, tileSize, tileSize);
            g2.fillRect(64, y, tileSize, tileSize);

            g2.fillRect(screenWidth - tileSize, y, tileSize, tileSize); // right border
            g2.fillRect(screenWidth - tileSize * 2, y, tileSize, tileSize);
            g2.fillRect(screenWidth - tileSize * 3, y, tileSize, tileSize);
        }

        for (int i = 0; i < maxScreenCol; i++) {
            int x = i * tileSize;
            g2.fillRect(x, screenHeight - tileSize, tileSize, tileSize); // bottom border
        }

        // Score and High Scorewill be a Jlabel
        g2.setColor(Color.white);
        g2.setFont(new Font("Serif", Font.PLAIN, 30));
        g2.drawString(scoreText,scoreXPos,scoreYPos);
        g2.drawString(score, scoreXPos + 96, scoreYPos); // 96 equals tilesize * 3

        g2.setFont(new Font("Serif", Font.PLAIN, 30));
        g2.drawString(highScoreText,highScoreXPos,highScoreYPos);
        g2.drawString(highScore, highScoreXPos + 160, highScoreYPos); // 160 equals tilesize * 5

        // Saved Piece
        g2.setColor(Color.white);
        g2.setFont(new Font("Serif", Font.PLAIN, 30));
        g2.drawString(savedText,savedPieceTextXPos,savedPieceTextYPos);
        g2.drawRect(savedPieceXPos,savedPieceYPos,tileSize * 2,tileSize * 2);
        g2.setFont(new Font("Serif", Font.PLAIN, 60));
        g2.drawString(savedPieceText, savedTypeXPos, savedTypeYPos);

        // Draw the next pieces coming up next
        g2.setFont(new Font("Serif", Font.PLAIN, 30));
        g2.drawString(nextPieceText, nextPieceXPos, nextPieceYPos);
        for (int i = 0; i < pieces.length; i++) {
            if (pieces[i] != null) {
                Tetromino tetromino = pieces[i];
                drawTetromino(g2, tetromino, tileSize / 2, nextPieceOffsetX, nextPieceOffsetY);
                g2.translate(0, tileSize * 2); // Move down for the next piece
            }
        }

        g2.dispose();
    }

    public void showRestartButton() {
        JButton restartButton = new JButton("<html>You Lose <br> Restart?</html>");
        restartButton.setBounds(screenWidth / 2 - 100,screenHeight / 2 - 25,200,50);
        restartButton.setBackground(Color.red);
        restartButton.setForeground(Color.black);
        restartButton.setBorderPainted(false);
        restartButton.setOpaque(true);

        restartButton.addActionListener(e -> {
            resetGame();

            JLayeredPane layeredPane = (JLayeredPane) this.getParent();
            layeredPane.remove(restartButton);
            layeredPane.repaint();
        });

        JLayeredPane layeredPane = (JLayeredPane) this.getParent();
        layeredPane.add(restartButton, JLayeredPane.PALETTE_LAYER);
        layeredPane.repaint();
    }

    public void resetGame() {

        for(int r = 0; r < maxScreenRow; r++) {
            for(int c = 0; c < maxScreenCol; c++) {
                board[r][c] = Color.lightGray; // clear the board
            }
        }

        fallInterval = 1000;
        gameover = false;
        startGameThread();
    }

    public void drawTetromino(Graphics2D g, Tetromino tetromino, int blockSize, int offsetX, int offsetY) {
        int[][] shape = tetromino.getShape();
        Color color = tetromino.getColor();
        int row = tetromino.getRow();
        int col = tetromino.getCol();

        g.setColor(color);
        for (int i = 0; i < shape.length; i++) {
            for (int j = 0; j < shape[i].length; j++) {
                if (shape[i][j] != 0) {
                    int x = (col + j) * blockSize + offsetX;
                    int y = (row + i) * blockSize + offsetY;
                    g.fillRect(x, y, blockSize, blockSize);

                    g.setColor(Color.BLACK);
                    g.drawRect(x, y, blockSize, blockSize);
                    g.setColor(color);
                }
            }
        }
    }

    // checking if the tetromino hits one of the screen boundaries or another tetromino
    public boolean collidesAt(int newRow, int newCol) {
        int[][] shape = currentTetromino.getShape();

        for (int i = 0; i < shape.length; i++) {
            for (int j = 0; j < shape[i].length; j++) {
                if (shape[i][j] == 0) continue;

                int r = newRow + i;
                int c = newCol + j;

                // Check screen boundaries
                if (c < 3 || c >= maxScreenCol || r >= maxScreenRow - 1) {
                    System.out.println("Hit wall of game");
                    return true;
                }

                // Check if board space is already filled
                if (r >= 0 && !board[r][c].equals(Color.lightGray)) {
                    System.out.println("hit another tetromino ");
                    return true;
                }
            }
        }
        return false;
    }

    // Saving the colors of the current tetromino to the gameboard
    private void lockTetrominoToBoard() {
        int[][] shape = currentTetromino.getShape();
        int row = currentTetromino.getRow();
        int col = currentTetromino.getCol();
        Color color = currentTetromino.getColor();

        for (int i = 0; i < shape.length; i++) {
            for (int j = 0; j < shape[i].length; j++) {
                if (shape[i][j] == 1) {
                    int r = row + i;
                    int c = col + j;
                    if (r >= 0 && r < maxScreenRow - 1 && c >= 0 && c < maxScreenCol - 3 ) {
                        board[r][c] = color;
                    }
                }
            }
        }
    }

    private Tetromino spawnNewTetromino() {

        TetrominoType randomType = types[new Random().nextInt(types.length)];
        Tetromino newTetromino = new Tetromino(randomType);

        // Game over if new piece immediately collides with a piece already on the board
        if (collidesAt(newTetromino.getRow(), newTetromino.getCol())) {
            gameover = true;
            return null;
        } else {
            savedThisTurn = false;
            return newTetromino;
        }
    }

    private void checkBoard() {

        for (int r = 0; r < board.length; r++) {
            boolean lineFull = true;

            for (int c = 3; c < board[r].length - 3; c++) {
                if (board[r][c].equals(Color.lightGray)) {
                    lineFull = false;
                    break;
                }
            }

            if (lineFull) {
                // Clear the full row
                for (int c = 2; c < board[r].length; c++) {
                    board[r][c] = Color.lightGray;
                }

                // Drop all rows above down by 1
                for (int i = r; i > 0; i--) {
                    for (int c = 2; c < board[i].length; c++) {
                        board[i][c] = board[i - 1][c];
                    }
                }

                // Clear the top row
                for (int c = 2; c < board[0].length; c++) {
                    board[0][c] = Color.lightGray;
                }

                // Recheck same row index after shifting and update score
                r--;
                int currentScore = Integer.parseInt(score);
                currentScore += 1;
                score = String.valueOf(currentScore);

            }
        }
    }

    // if saved piece doesnt exist, save it, if saved peice does exist, swap current and saved piece
    public void savePiece(){
        
        if(savedThisTurn) return;

        if(savedTetromino == null){
            savedTetromino = currentTetromino;
            savedPieceText = currentTetromino.getType().toString();

            // get the next piece from the array, shift the array, and spawn a new piece
            currentTetromino = pieces[0];
            for (int i = 0; i < pieces.length - 1; i++){
                pieces[i] = pieces[i + 1]; 
            }
            pieces[pieces.length - 1] = spawnNewTetromino();
            
        } else {
            Tetromino temp = savedTetromino;
            savedPieceText = currentTetromino.getType().toString();
            savedTetromino = currentTetromino;
            currentTetromino = temp;
        }
        savedThisTurn = true;
    }

}
