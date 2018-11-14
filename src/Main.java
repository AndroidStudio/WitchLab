import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;

@SuppressWarnings("ALL")
public class Main {

    public static void main(String[] args) throws Exception {
        //ServerSocket serverSocket = new ServerSocket(9696,2, InetAddress.getByName("176.119.44.252"));
        //ServerSocket serverSocket = new ServerSocket(9696,2, InetAddress.getByName("192.168.1.41"));
        ServerSocket serverSocket = new ServerSocket(9696, 2, InetAddress.getByName("192.168.0.101"));

        InetSocketAddress localSocketAddress = (InetSocketAddress) serverSocket.getLocalSocketAddress();
        System.out.println("Ip address: " + localSocketAddress.getAddress().getHostAddress());
        System.out.println("Port: " + localSocketAddress.getPort());

        try {
            while (true) {
                Socket socketPlayer1 = serverSocket.accept();
                Socket socketPlayer2 = serverSocket.accept();

                String hostAddress1 = socketPlayer1.getInetAddress().getHostAddress();
                String hostAddress2 = socketPlayer2.getInetAddress().getHostAddress();

                if (hostAddress1.equals(hostAddress2)) {
                    socketPlayer1.close();
                    socketPlayer2.close();
                    continue;
                }

                Game game = new Game();
                Game.Player player1 = game.new Player(socketPlayer1);
                Game.Player player2 = game.new Player(socketPlayer2);

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
