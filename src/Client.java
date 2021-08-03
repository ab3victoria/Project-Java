import java.io.*;
import java.net.Socket;
import java.util.*;

public class Client {
    public static void main(String[] args) throws IOException, ClassNotFoundException {
        Socket socket =new Socket("127.0.0.1",8010);
        System.out.println("client: Created Socket");

        InputStream inputStream = socket.getInputStream();
        OutputStream outputStream = socket.getOutputStream();
        ObjectOutputStream toServer=new ObjectOutputStream(outputStream);
        ObjectInputStream fromServer=new ObjectInputStream(inputStream);

        // sending #1 matrix
        int[][] source = {
                {1, 0, 1, 1},
                {1, 0, 0, 0},
                {0, 1, 1, 0}
        };

        int[][] source2 = {
                {100, 200, -600},
                {-500, 1000,-50},
                {-50, 100, 100}
        };
        //send "matrix" command then write 2d array to socket
        toServer.writeObject("matrix");
        // sending #1 matrix
        toServer.writeObject(source);

        //send "neighbors" command then write an index to socket
        toServer.writeObject("neighbors");
        toServer.writeObject(new Index(1,1));

        // get neighboring indices as list
        List<Index> AdjacentIndices =
                new ArrayList<Index>((List<Index>) fromServer.readObject());
        System.out.println("from client - Neighboring Indices are: "+ AdjacentIndices);

        //send "reachables" command then write an index to socket
        toServer.writeObject("reachables");
        toServer.writeObject(new Index(1,1));

        // get reachable indices as list
        List<Index> reachables =
                new ArrayList<Index>((List<Index>) fromServer.readObject());
        System.out.println("from client - Reachable Indices are:  "+ reachables);

        toServer.writeObject("getAllSCC");
        List<HashSet<Index>> setsOfOnes = new ArrayList((Collection<Index>) fromServer.readObject());
        if(setsOfOnes.size() == 0){
            System.out.println("There are no indices with a value of 1\nPlease try again!");
        } else{
            System.out.println("List of connected components: ");
            setsOfOnes.forEach(t-> System.out.println(t));
        }

        toServer.writeObject("find shortest paths");
        toServer.writeObject(new Index(0,0));
        toServer.writeObject(new Index(2, 2));
        List<List<Index>> allPaths = (List<List<Index>>) fromServer.readObject();
        if(allPaths.size() == 0){
            System.out.println("There is no Path.");
        } else{
            System.out.println("the paths are:");
            allPaths.forEach(p-> System.out.println(p));
        }

        toServer.writeObject("submarines");
        int numOfSubs = (int)fromServer.readObject();
        //if(numOfSubs == -1) System.out.println("Submarines: ~invalid input");
        //else
        System.out.println("there are "+ numOfSubs +" submarines.");


        toServer.writeObject("shortestWeightedPath");
        toServer.writeObject(source2);
        toServer.writeObject(new Index(0,0));
        toServer.writeObject(new Index(2,2));
        List<List<Index>> lightestPaths = (List<List<Index>>) fromServer.readObject();
        System.out.println("the lightest paths are:");
        lightestPaths.forEach(lp-> System.out.println(lp));

        toServer.writeObject("stop");

        System.out.println("client: Close all streams");
        fromServer.close();
        toServer.close();
        socket.close();
        System.out.println("client: Closed operational socket");

    }
}