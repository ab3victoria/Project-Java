import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public interface IHandler {
    void handle(InputStream fromClient,
                OutputStream toClient) throws IOException, ClassNotFoundException;
}