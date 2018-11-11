import java.net.InetSocketAddress;
import java.net.ServerSocket;

@SuppressWarnings("ALL")
public class Main {

    public static void main(String[] args) throws Exception {
        ServerSocket serverSocket = new ServerSocket(9700,2);
        InetSocketAddress localSocketAddress = (InetSocketAddress) serverSocket.getLocalSocketAddress();
        System.out.println("Ip address: " + localSocketAddress.getAddress().getHostAddress());
        System.out.println("Port: " + localSocketAddress.getPort());

        try {
            while (true) {
                Game game = new Game();
                Game.Player player1 = game.new Player(serverSocket.accept());
                Game.Player player2 = game.new Player(serverSocket.accept());

                game.currentPlayer = player1;
                player1.setOpponent(player2);
                player2.setOpponent(player1);

                player1.start();
                player2.start();
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            serverSocket.close();
        }
    }
}
