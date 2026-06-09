public class GameLogger implements GameObserver {

    @Override
    public void onEvent(String event) {
        System.out.println(">> " + event);
    }
}
