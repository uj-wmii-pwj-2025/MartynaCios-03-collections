package uj.wmii.pwj.collections;

import java.util.*;

public class BattleshipGeneratorImpl implements BattleshipGenerator {
    private static final int BOARD_SIZE = 10;
    private static final int TOTAL_CELLS = BOARD_SIZE * BOARD_SIZE;
    private static final char WATER = '.';
    private static final char SHIP = '#';
    private static final int[] SHIP_SIZES = {4, 3, 3, 2, 2, 2, 1, 1, 1, 1};

    private static final int[][] DIRECTIONS = {{-1, 0}, {1, 0}, {0, -1}, {0, 1}};
    private static final int[][] NEIGHBORS = {
            {-1, -1}, {-1, 0}, {-1, 1},
            {0, -1},           {0, 1},
            {1, -1},  {1, 0},  {1, 1}
    };

    private final Random random;

    public BattleshipGeneratorImpl() {
        this.random = new Random();
    }

    @Override
    public String generateMap() {
        char[][] board = new char[BOARD_SIZE][BOARD_SIZE];
        fillBoard(board);

        if (placeAllShips(board)) {
            return boardToString(board);
        }

        throw new RuntimeException("Failed to generate board");
    }

    private void fillBoard( char[][] board ) {
        for ( int i = 0; i < BOARD_SIZE; i++ ) {
            Arrays.fill( board[i], WATER );
        }
    }

    private boolean placeAllShips( char[][] board ) {
        Set<Cell> available = new HashSet<>();
        for ( int i = 0; i < BOARD_SIZE; i++ ) {
            for ( int j = 0; j < BOARD_SIZE; j++ ) {
                available.add( new Cell(i, j) );
            }
        }

        for ( int size : SHIP_SIZES ) {
            List<Cell> ship = findShipPosition( board, available, size );
            if ( ship == null ) return false;

            setShip( board, ship );
            removeUsedAndBlocked( available, ship );
        }

        return true;
    }

    private List<Cell> findShipPosition( char[][] board, Set<Cell> available, int size ) {
        List<Cell> positions = new ArrayList<>(available);
        Collections.shuffle(positions, random);

        for ( Cell start : positions ) {
            List<List<Cell>> shapes = createShapes(board, start, size);
            Collections.shuffle(shapes, random);

            for ( List<Cell> shape : shapes ) {
                if ( canPlace(board, shape) ) {
                    return shape;
                }
            }
        }

        return null;
    }

    private List<List<Cell>> createShapes( char[][] board, Cell start, int size ) {
        if ( size == 1 ) {
            return Collections.singletonList(Collections.singletonList(start));
        }

        List<List<Cell>> results = new ArrayList<>();
        boolean[][] visited = new boolean[BOARD_SIZE][BOARD_SIZE];
        List<Cell> currentShape = new ArrayList<>();
        currentShape.add(start);
        visited[start.row][start.col] = true;

        exploreShape( board, visited, currentShape, start, size, results );

        return results;
    }

    private void exploreShape( char[][] board, boolean[][] visited, List<Cell> currentShape,
                              Cell current, int targetSize, List<List<Cell>> results ) {
        if ( currentShape.size() == targetSize ) {
            results.add(new ArrayList<>(currentShape));
            return;
        }

        int[] dirOrder = {0, 1, 2, 3};
        shuffleArray(dirOrder);

        for ( int dirIdx : dirOrder ) {
            int[] dir = DIRECTIONS[dirIdx];
            int nr = current.row + dir[0];
            int nc = current.col + dir[1];

            if ( nr >= 0 && nr < BOARD_SIZE && nc >= 0 && nc < BOARD_SIZE ) {
                if ( board[nr][nc] == WATER && ! visited[nr][nc] ) {

                    Cell next = new Cell(nr, nc);
                    visited[nr][nc] = true;
                    currentShape.add(next);

                    exploreShape(board, visited, currentShape, next, targetSize, results);

                    currentShape.remove(currentShape.size() - 1);
                    visited[nr][nc] = false;
                }
            }
        }
    }

    private void shuffleArray( int[] array ) {
        for ( int i = array.length - 1; i > 0; i-- ) {
            int j = random.nextInt(i + 1);
            int temp = array[i];
            array[i] = array[j];
            array[j] = temp;
        }
    }

    private boolean canPlace( char[][] board, List<Cell> shape ) {
        for (Cell cell : shape) {
            for (int[] d : NEIGHBORS) {
                int nr = cell.row + d[0];
                int nc = cell.col + d[1];
                if (nr >= 0 && nr < BOARD_SIZE && nc >= 0 && nc < BOARD_SIZE) {
                    if (board[nr][nc] == SHIP) return false;
                }
            }
        }
        return true;
    }

    private void setShip( char[][] board, List<Cell> shape ) {
        for ( Cell cell : shape ) {
            board[cell.row][cell.col] = SHIP;
        }
    }

    private void removeUsedAndBlocked(Set<Cell> available, List<Cell> shape) {
        for ( Cell cell : shape ) {
            available.remove(cell);
        }

        for ( Cell cell : shape ) {
            for ( int[] d : NEIGHBORS ) {
                int nr = cell.row + d[0];
                int nc = cell.col + d[1];
                if ( nr >= 0 && nr < BOARD_SIZE && nc >= 0 && nc < BOARD_SIZE ) {
                    available.remove(new Cell(nr, nc));
                }
            }
        }
    }

    private String boardToString( char[][] board ) {
        StringBuilder sb = new StringBuilder(TOTAL_CELLS);
        for ( int i = 0; i < BOARD_SIZE; i++ ) {
            for ( int j = 0; j < BOARD_SIZE; j++ ) {
                sb.append(board[i][j]);
            }
        }
        return sb.toString();
    }

    private static class Cell {
        final int row;
        final int col;

        Cell(int row, int col) {
            this.row = row;
            this.col = col;
        }
    }
}
