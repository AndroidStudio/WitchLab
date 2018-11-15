import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.*;

@SuppressWarnings("ALL")
public class Main {

    public static void main(String[] args) throws Exception {
        //ServerSocket serverSocket = new ServerSocket(9696,2, InetAddress.getByName("10.0.1.11"));
        //ServerSocket serverSocket = new ServerSocket(9696,2, InetAddress.getByName("176.119.44.252"));
        ServerSocket serverSocket = new ServerSocket(9696,2, InetAddress.getByName("176.119.40.186"));

        //ServerSocket serverSocket = new ServerSocket(9696,2, InetAddress.getByName("192.168.1.41"));

        InetSocketAddress localSocketAddress = (InetSocketAddress) serverSocket.getLocalSocketAddress();
        System.out.println("Ip address: " + localSocketAddress.getAddress().getHostAddress());
        System.out.println("Port: " + localSocketAddress.getPort());

        try {
            while (true) {
                Socket socketPlayer1 = serverSocket.accept();
                Socket socketPlayer2 = serverSocket.accept();
                try{
                    BufferedReader socketPlayer1Reader = new BufferedReader(
                            new InputStreamReader(socketPlayer1.getInputStream()));

                    BufferedReader socketPlayer2Reader = new BufferedReader(
                            new InputStreamReader(socketPlayer2.getInputStream()));

                    String message1 = socketPlayer1Reader.readLine();
                    String message2 = socketPlayer2Reader.readLine();

                    String udid1 =  new JSONObject(message1).getString("udid");
                    String udid2 =  new JSONObject(message2).getString("udid");

                    System.out.println("udid1: " + udid1);
                    System.out.println("udid2: " + udid2);

                    if(udid1.equals(udid2)){
                        socketPlayer1.close();
                        socketPlayer2.close();
                        continue;
                    }
                }catch (Exception e){
                    e.printStackTrace();
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
