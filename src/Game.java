import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

@SuppressWarnings("ALL")
class Game {

    Game() {

    }

    public Player currentPlayer;

    class Player extends Thread {
        public final int[] board = new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0};

        private final BufferedReader bufferedReader;
        private final PrintWriter printWriter;
        private final Socket socket;
        private final String address;

        private Player opponent;
        private String color;
        private int symbol = -1;

        public String udid;
        private boolean ready = false;

        Player(Socket socket) throws Exception {
            this.socket = socket;
            this.address = this.socket.getLocalAddress().getHostAddress();
            System.out.println("client address: " + address);

            this.printWriter = new PrintWriter(socket.getOutputStream(), true);
            this.bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        }

        @Override
        public void run() {
            try {
                this.socket.setSoTimeout(10000);
                initGame();
                while (true) {
                    String message = readMessage();
                    JSONObject jsonObject = new JSONObject(message);
                    String type = jsonObject.getString(SocketConstants.TYPE);
                    switch (type) {
                        case SocketConstants.PING:
                            receivePing(jsonObject);
                            break;
                        case SocketConstants.SYMBOL:
                            receiveSymbol(jsonObject);
                            break;
                        case SocketConstants.LED_SETTINGS:
                            receiveLedSettings(jsonObject);
                            break;
                        case SocketConstants.GAME_INFO:
                            gameInfo();
                            break;
                        case SocketConstants.INDEX:
                            setIndex(jsonObject);
                            break;
                        case SocketConstants.WIN:
                            win();
                            break;
                        case SocketConstants.RESET_BOARD:
                            resetBoard();
                            break;
                        case SocketConstants.NEW_GAME:
                            newGame();
                            break;
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                close();
            }
        }

        private void newGame() throws Exception {
            symbol = -1;
            ready = false;
            opponent.symbol = -1;
            opponent.ready = false;
            clearBoard();
            startNewGame();
            opponent.startNewGame();
        }

        private void startNewGame() throws Exception {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put(SocketConstants.TYPE, SocketConstants.NEW_GAME);
            sendMessage(jsonObject.toString());
        }

        private void resetBoard() throws Exception {
            clearBoard();
            opponent.sendReset();
        }

        private void clearBoard() {
            for (int i = 0; i < 9; i++) {
                board[i] = 0;
            }
            for (int i = 0; i < 9; i++) {
                opponent.board[i] = 0;
            }
        }

        private void sendReset() throws Exception {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put(SocketConstants.TYPE, SocketConstants.RESET_BOARD);
            sendMessage(jsonObject.toString());
        }

        private void win() throws Exception {
            clearBoard();
            opponent.lost();
        }

        private void setIndex(JSONObject jsonObject) throws Exception {
            board[jsonObject.getInt(SocketConstants.INDEX)] = 1;
            opponent.board[jsonObject.getInt(SocketConstants.INDEX)] = 2;
            currentPlayer = opponent;
        }

        private void gameInfo() throws Exception {
            if (!opponent.ready || !ready) {
                return;
            }

            JSONObject jsonObject = new JSONObject();
            jsonObject.put(SocketConstants.TYPE, SocketConstants.GAME_INFO);
            jsonObject.put(SocketConstants.STATUS, SocketConstants.OK);

            JSONArray boardArray = new JSONArray();
            boardArray.put(board[0]);
            boardArray.put(board[1]);
            boardArray.put(board[2]);
            boardArray.put(board[3]);
            boardArray.put(board[4]);
            boardArray.put(board[5]);
            boardArray.put(board[6]);
            boardArray.put(board[7]);
            boardArray.put(board[8]);

            JSONArray playerArray = new JSONArray();

            JSONObject player2 = new JSONObject();
            player2.put(SocketConstants.UDID, opponent.udid);
            player2.put(SocketConstants.COLOR, opponent.color);
            player2.put(SocketConstants.SYMBOL, opponent.symbol);
            playerArray.put(player2);

            jsonObject.put(SocketConstants.INFO, playerArray);
            jsonObject.put(SocketConstants.GAME_BOARD, boardArray);
            jsonObject.put(SocketConstants.CURRENT_PLAYER, currentPlayer.udid);

            sendMessage(jsonObject.toString());
        }

        private void lost() throws Exception {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put(SocketConstants.TYPE, SocketConstants.WIN);
            sendMessage(jsonObject.toString());
        }

        private void receiveLedSettings(JSONObject jsonObject) throws Exception {
            color = jsonObject.getString(SocketConstants.COLOR);
            colorOk();
        }

        private void colorOk() throws Exception {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put(SocketConstants.TYPE, SocketConstants.LED_SETTINGS);
            jsonObject.put(SocketConstants.COLOR, SocketConstants.OK);
            sendMessage(jsonObject.toString());
            ready = true;
        }

        private void receiveSymbol(JSONObject jsonObject) throws Exception {
            symbol = jsonObject.getInt(SocketConstants.SYMBOL);
            udid = jsonObject.getString(SocketConstants.UDID);

            if (opponent.symbol == symbol) {
                symbolUnavailable();
            } else {
                symbolOk();
            }
        }

        private void symbolUnavailable() throws Exception {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put(SocketConstants.TYPE, SocketConstants.SYMBOL);
            jsonObject.put(SocketConstants.SYMBOL, SocketConstants.SYMBOL_ERROR);
            sendMessage(jsonObject.toString());
        }

        private void symbolOk() throws Exception {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put(SocketConstants.TYPE, SocketConstants.SYMBOL);
            jsonObject.put(SocketConstants.SYMBOL, SocketConstants.OK);
            sendMessage(jsonObject.toString());
        }

        private void receivePing(JSONObject jsonObject) throws Exception {

        }

        private void sendPing() throws Exception {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put(SocketConstants.TYPE, SocketConstants.PING);
            jsonObject.put(SocketConstants.TYPE, udid);
            sendMessage(jsonObject.toString());
        }

        private void initGame() throws Exception {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put(SocketConstants.TYPE, SocketConstants.INIT_GAME);
            sendMessage(jsonObject.toString());
        }

        String readMessage() throws Exception {
            String message = bufferedReader.readLine();
            if (message == null) {
                throw new Exception("null message");
            }
            System.out.println("readMessage: " + message);
            return message;
        }

        private void exetGame() throws Exception {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put(SocketConstants.TYPE, SocketConstants.EXIT_GAME);
            jsonObject.put(SocketConstants.UDID, udid);
            sendMessage(jsonObject.toString());
        }

        void sendMessage(String message) throws Exception {
            printWriter.println(message);
            System.out.println("sendMessage: " + message);
        }

        void close() {
            try {
                if (opponent != null) {
                    opponent.exetGame();
                }
                System.err.println("socket close");
                socket.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        public void setOpponent(Player opponent) {
            this.opponent = opponent;
        }
    }
}
