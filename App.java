import java.util.ArrayList;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.IntStream;

public class App {

    private static class Square {
        public int row, col;

        public Square(int r, int c) {
            row = r;
            col = c;
        }

        public String toString() {
            return "Square " + row + ", " + col;
        }
    }

    private static class Board {
        public boolean[][] boardState;
        public int openSquares = 0;
        public int rows, cols;

        public Board(boolean[][] b) {
            boardState = b;
            openSquares = b.length * b[0].length;
            rows = b.length;
            cols = b[0].length;
        }

        public void fillSquare(int r, int c) {
            boardState[r][c] = false;
            openSquares -= 1;
        }

        public void fillSquare(Square s) {
            fillSquare(s.row, s.col);
        }

        public void clearSquare(int r, int c) {
            boardState[r][c] = true;
            openSquares += 1;
        }

        public void clearSquare(Square s) {
            fillSquare(s.row, s.col);
        }

        public boolean checkSquare(int r, int c) {
            return boardState[r][c];
        }

        public boolean checkSquare(Square s) {
            return checkSquare(s.row, s.col);
        }
    }

    public static Board initBoard(int r, int c) {
        r = Math.max(r, 0);
        c = Math.max(c, 0);

        boolean[][] board = new boolean[r][c];
        for (int i = 0; i < r; i++) {
            for (int j = 0; j < c; j++) {
                board[i][j] = true;
            }
        }
        return new Board(board);
    }

    public static Board initBoard(int r, int c, int startX, int startY) {
        Board board = initBoard(r, c);

        if (r > startX && c > startY)
            board.fillSquare(r, c);
        return board;
    }

    public static ArrayList<Square> validMoves(Board b, Square s) {
        ArrayList<Square> squares = new ArrayList<>();

        int[] c_offsets = { -1, 1, 2, 2, 1, -1, -2, -2 };
        int[] r_offsets = { -2, -2, -1, 1, 2, 2, 1, -1 };
        for (int i = 0; i < c_offsets.length; i++) {
            int r = s.row + r_offsets[i];
            int c = s.col + c_offsets[i];
            if (0 <= r && r < b.rows && 0 <= c && c < b.cols && b.checkSquare(r, c)) {
                Square square = new Square(r, c);
                squares.add(square);
            }
        }
        return squares;
    }

    public static Square getRandomMove(ArrayList<Square> moves) {
        if (moves.size() == 0)
            return null;
        return moves.get(ThreadLocalRandom.current().nextInt(moves.size()));
    }

    public static Square getBestMove(ArrayList<Square> moves, Board b) {
        int leastMoves = Integer.MAX_VALUE;
        Square bestMove = null;
        Board newBoard = new Board(new boolean[b.rows][b.cols]);
        for (int i = 0; i < b.rows; i++) {
            for (int j = 0; j < b.cols; j++) {
                newBoard.boardState[i][j] = b.boardState[i][j];
            }
        }
        newBoard.openSquares = b.openSquares;
        for (Square m : moves) {
            newBoard.fillSquare(m);
            int movesLeft = validMoves(newBoard, m).size();
            if (movesLeft < leastMoves) {
                leastMoves = movesLeft;
                bestMove = m;
            }
            newBoard.clearSquare(m);
        }
        return bestMove;
    }

    public void test() {
        Board b = initBoard(8, 8, 4, 4);
        Square s = new Square(0, 0);
        ArrayList<Square> moves = validMoves(b, s);
        for (Square square : moves) {
            System.out.println(square);
        }
    }

    public static int randomWalk(int size, int row, int col) {
        Board b = initBoard(size, size);
        Square s = new Square(row, col);
        b.fillSquare(s);

        boolean done = false;
        int count = 0;

        while (!done) {
            count++;
            // Square move = getRandomMove(validMoves(b, s));
            Square move = getBestMove(validMoves(b, s), b);
            if (move == null) {
                done = true;
            } else {
                b.fillSquare(move);
                s = move;
                // System.out.println("Move " + count + ": " + move);
            }
        }
        return b.openSquares;
    }

    public void exhaustiveSearch(int size) {
        Optional<int[]> result = IntStream.range(0, size)
                .boxed()
                .flatMap(r -> IntStream.range(0, size).mapToObj(c -> new int[] { r, c }))
                .parallel()
                .filter(pos -> randomWalk(size, pos[0], pos[1]) == 0)
                .findAny();

        if (result.isPresent()) {
            int[] pos = result.get();
            System.out.println("Size: %d, Remaining: %d, Position: (%d,%d)".formatted(size, 0, pos[0], pos[1]));
        } else {
            System.out.println("Size: %d Failed!".formatted(size));
        }
    }

    public void walks() {
        int sum = 0;
        int sizes = 200;
        for (int i = 8; i < sizes; i++) {
            int remaining = randomWalk(i, i / 2, i / 2);
            sum += remaining;
            if (remaining != 0)
                System.out.println(" " + i + " " + remaining);
        }
    }

    public static void main(String[] args) throws Exception {
        System.out.println("Searching");
        App app = new App();
        IntStream.range(8, 20).parallel().forEach(app::exhaustiveSearch);
    }

}
