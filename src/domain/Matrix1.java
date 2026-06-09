import java.util.ArrayList;
import java.util.Collections;
import java.util.Scanner;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;

public final class Matrix1 {

    private static Matrix1 matrix1;

    private final String[][] matrix = new String[8][8];

    private final int[][] around = {{-1,0},{0,1},{1,0},{0,-1}};

    private Neo neo;

    private Phone phone;

    private ArrayList<Agent> agents = new ArrayList<>();

    private ArrayList<Wall> walls = new ArrayList<>();

    private ArrayList<GameObserver> observers = new ArrayList<>();

    private final Scanner scanner = new Scanner(System.in);

    private CyclicBarrier barrier;

    private boolean isGameOver = false;

    public Matrix1() {}

    public void addObserver(GameObserver observer) {
        observers.add(observer);
    }

    public void removeObserver(GameObserver observer) {
        observers.remove(observer);
    }

    private void notifyObservers(String event) {
        for (GameObserver o : observers) {
            o.onEvent(event);
        }
    }

    public void startGame() {
        // All threads meet here after each round
        int total = 1 + agents.size();
        barrier = new CyclicBarrier(total, () -> {
            // Show board and wait for Enter when all threads finish
            showMatrix();
            if (!isGameOver) {
                System.out.println("Press Enter for next round...");
                scanner.nextLine();
            }
        });

        showMatrix();
        System.out.println("Press Enter to start...");
        scanner.nextLine();

        neo.start();
        for (Agent a : agents) {
            a.start();
        }
    }

    // First thread to acquire the lock claims the cell
    public synchronized boolean tryMove(int fromX, int fromY, int toX, int toY, String symbol) {
        if (isGameOver) return false;
        if (!verifyPosition(toX, toY)) return false;
        matrix[fromX][fromY] = " ";
        matrix[toX][toY] = symbol;
        notifyObservers(("N".equals(symbol) ? "Neo" : "Agent") + " moved to (" + toX + "," + toY + ")");
        return true;
    }

    public synchronized boolean gameOver() {
        if (isGameOver) return true;
        int neoX = neo.getXPosition();
        int neoY = neo.getYPosition();
        for (int i = 0; i < around.length; i++) {
            int aX = neoX + around[i][0];
            int aY = neoY + around[i][1];
            if (verifyValidPosition(aX, aY)) continue;
            if ("T".equals(matrix[aX][aY])) {
                isGameOver = true;
                notifyObservers("Neo reached the Phone - Neo WINS!");
                return true;
            }
            if ("A".equals(matrix[aX][aY])) {
                isGameOver = true;
                notifyObservers("An Agent caught Neo - GAME OVER!");
                return true;
            }
        }
        return false;
    }

    public void awaitBarrier() {
        try {
            barrier.await();
        } catch (InterruptedException | BrokenBarrierException e) {
            Thread.currentThread().interrupt();
        }
    }

    public synchronized void showMatrix() {
        for (int i = 0; i < 8; i++) {
            System.out.print("|");
            for (int j = 0; j < 8; j++) {
                System.out.print(matrix[i][j]);
                if (j < 7) System.out.print("|");
            }
            System.out.println("|");
        }
    }

    public boolean verifyValidPosition(int x, int y) {
        return (x < 0 || x > 7 || y < 0 || y > 7);
    }

    public boolean verifyPosition(int x, int y) {
        return " ".equals(matrix[x][y]);
    }

    public synchronized Boolean getIsGameOver() {
        return isGameOver;
    }

    public synchronized int getNeoX() { return neo.getXPosition(); }
    public synchronized int getNeoY() { return neo.getYPosition(); }

    public synchronized int getPhoneX() { return phone.getXPosition(); }
    public synchronized int getPhoneY() { return phone.getYPosition(); }

    public synchronized int minDistToAgent(int x, int y) {
        int min = Integer.MAX_VALUE;
        for (Agent a : agents) {
            int dist = Math.abs(x - a.getXPosition()) + Math.abs(y - a.getYPosition());
            if (dist < min) min = dist;
        }
        return min;
    }

    public int[][] getAround() {
        return around;
    }

    public synchronized String[][] getMatrix() {
        return matrix;
    }

    public synchronized void initialBoard(int n) {
        for (int i = 0; i < 8; i++)
            for (int j = 0; j < 8; j++)
                matrix[i][j] = " ";

        agents.clear();
        walls.clear();

        // Shuffle all positions so every entity spawns randomly
        ArrayList<int[]> available = new ArrayList<>();
        for (int i = 0; i < 8; i++)
            for (int j = 0; j < 8; j++)
                available.add(new int[]{i, j});

        Collections.shuffle(available);

        int safeN = Math.min(n, (available.size() - 2) / 2);
        int idx = 0;

        for (int i = 0; i < safeN; i++, idx++) {
            int[] pos = available.get(idx);
            agents.add(new Agent(pos[0], pos[1]));
            matrix[pos[0]][pos[1]] = "A";
        }

        for (int i = 0; i < safeN; i++, idx++) {
            int[] pos = available.get(idx);
            walls.add(new Wall(pos[0], pos[1]));
            matrix[pos[0]][pos[1]] = "W";
        }

        // Phone at the next random free position
        int[] phonePos = available.get(idx++);
        phone = new Phone(phonePos[0], phonePos[1]);
        matrix[phonePos[0]][phonePos[1]] = "T";

        // Neo starts at the next random free position
        int[] neoStart = available.get(idx);
        neo = new Neo(neoStart[0], neoStart[1]);
        matrix[neoStart[0]][neoStart[1]] = "N";

        notifyObservers("Board initialized - " + safeN + " agents, " + safeN + " walls");
    }

    public static Matrix1 getInstance() {
        if (matrix1 == null) {
            matrix1 = new Matrix1();
        }
        return matrix1;
    }

    public static void main(String args[]) {
        int n = (args.length > 0) ? Integer.parseInt(args[0]) : 2;
        Matrix1 matrixInitial = getInstance();
        matrixInitial.addObserver(new GameLogger());
        matrixInitial.initialBoard(n);
        matrixInitial.startGame();
    }
}
