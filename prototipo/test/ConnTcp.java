import unibo.actor22comm.interfaces.Interaction2021;
import unibo.actor22comm.tcp.TcpClientSupport;
import unibo.actor22comm.utils.ColorsOut;

public class ConnTcp implements Interaction2021 {
    private final Interaction2021 conn;

    public ConnTcp(String hostAddr, int port) throws Exception {
        conn = TcpClientSupport.connect(hostAddr, port, 10);
        ColorsOut.outappl("ConnTcp createConnection DONE:" + conn, ColorsOut.GREEN);
    }

    @Override
    public void forward(String msg) {
        try {
            // ColorsOut.outappl("ConnTcp forward:" + msg   , ColorsOut.GREEN);
            conn.forward(msg);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public String request(String msg) throws Exception {
        forward(msg);
        return receiveMsg();
    }

    @Override
    public void reply(String s) {
        forward(s);
    }

    @Override
    public String receiveMsg() throws Exception {
        return conn.receiveMsg();
    }

    @Override
    public void close() throws Exception {

    }


    @Override
    public void sendALine(String s) throws Exception {

    }

    @Override
    public void sendALine(String s, boolean b) throws Exception {

    }

    @Override
    public String receiveALine() throws Exception {
        return null;
    }

    @Override
    public void closeConnection() throws Exception {

    }
}
