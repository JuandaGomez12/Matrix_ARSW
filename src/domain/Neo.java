import java.util.ArrayList;

public class Neo extends Thread {
    private int xPosition;
    private int yPosition;

    public Neo(int x, int y) {
        xPosition = x;
        yPosition = y;
    }

    @Override
    public void run() {
        while (!Matrix1.getInstance().getIsGameOver()) {
            move();
            Matrix1.getInstance().gameOver();
            Matrix1.getInstance().awaitBarrier();
        }
    }

    public void move() {
        ArrayList<int[]> available = new ArrayList<>();
        int[][] around = Matrix1.getInstance().getAround();
        int oldX = xPosition;
        int oldY = yPosition;
        for (int[] dir : around) {
            int nx = oldX + dir[0];
            int ny = oldY + dir[1];
            if (!Matrix1.getInstance().verifyValidPosition(nx, ny) && Matrix1.getInstance().verifyPosition(nx, ny)) {
                available.add(new int[]{nx, ny});
            }
        }
        if (available.isEmpty()) return;

        int phoneX = Matrix1.getInstance().getPhoneX();
        int phoneY = Matrix1.getInstance().getPhoneY();

        // Pick the cell closest to Phone while staying away from Agents
        int[] best = available.get(0);
        int bestScore = Integer.MAX_VALUE;
        for (int[] target : available) {
            int distToPhone = Math.abs(target[0] - phoneX) + Math.abs(target[1] - phoneY);
            int distToAgent = Math.min(Matrix1.getInstance().minDistToAgent(target[0], target[1]), 4);
            int score = distToPhone * 3 - distToAgent * 2;
            if (score < bestScore) {
                bestScore = score;
                best = target;
            }
        }

        if (Matrix1.getInstance().tryMove(oldX, oldY, best[0], best[1], "N")) {
            xPosition = best[0];
            yPosition = best[1];
        }
    }

    public int getXPosition() { return xPosition; }
    public int getYPosition() { return yPosition; }
    public void setXPosition(int xPosition) { this.xPosition = xPosition; }
    public void setYPosition(int yPosition) { this.yPosition = yPosition; }
}
