import java.util.ArrayList;

public class Agent extends Thread {
    private int xPosition;
    private int yPosition;

    public Agent(int xPosition, int yPosition) {
        this.xPosition = xPosition;
        this.yPosition = yPosition;
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

        int neoX = Matrix1.getInstance().getNeoX();
        int neoY = Matrix1.getInstance().getNeoY();

        // Pick the cell closest to Neo
        int[] best = available.get(0);
        int bestDist = Math.abs(best[0] - neoX) + Math.abs(best[1] - neoY);
        for (int[] target : available) {
            int dist = Math.abs(target[0] - neoX) + Math.abs(target[1] - neoY);
            if (dist < bestDist) {
                bestDist = dist;
                best = target;
            }
        }

        if (Matrix1.getInstance().tryMove(oldX, oldY, best[0], best[1], "A")) {
            xPosition = best[0];
            yPosition = best[1];
        }
    }

    public int getXPosition() { return xPosition; }
    public int getYPosition() { return yPosition; }
    public void setXPosition(int xPosition) { this.xPosition = xPosition; }
    public void setYPosition(int yPosition) { this.yPosition = yPosition; }
}
