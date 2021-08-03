import java.io.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

public class MatrixIHandler implements IHandler {
    private Matrix matrix;
    private Index start,end;

    /*
    to clear data members between clients (if same instance is shared among clients/tasks)
     */
    private void resetParams(){
        this.matrix = null;
        this.start = null;
        this.end = null;
    }

    @Override
    public void handle(InputStream fromClient, OutputStream toClient)
            throws IOException, ClassNotFoundException {

        // In order to read either objects or primitive types we can use ObjectInputStream
        ObjectInputStream objectInputStream = new ObjectInputStream(fromClient);
        // In order to write either objects or primitive types we can use ObjectOutputStream
        ObjectOutputStream objectOutputStream = new ObjectOutputStream(toClient);
        this.resetParams(); // in order to use same handler between tasks/clients

        boolean doWork = true;
        while(doWork){
            /*
             Use switch-case in order to get commands from client
             - client sends a 2D array
             - client send start index
             - client send end index
             - client sends an index and wished to get neighbors
             - client sends an index and wished to get reachable indices
             */

            // client send a verbal command
            switch(objectInputStream.readObject().toString()){
                case "matrix":{
                    // client will send a 2d array. handler will create a new Matrix object
                    int[][] primitiveMatrix = (int[][])objectInputStream.readObject();
                    System.out.println("Server: Got 2d array from client");
                    this.matrix = new Matrix(primitiveMatrix);
                    this.matrix.printMatrix();
                    break;
                }

                case "neighbors":{
                    Index findNeighborsIndex = (Index)objectInputStream.readObject();
                    List<Index> neighbors = new ArrayList<>();
                    if(this.matrix!=null){
                        neighbors.addAll(this.matrix.getNeighbors(findNeighborsIndex));
                        // print result in server
                        System.out.println("neighbors of " + findNeighborsIndex + ": " + neighbors);
                        // send to socket's OutputStream
                        objectOutputStream.writeObject(neighbors);
                    }
                    break;
                }

                case "reachables":{
                    Index findNeighborsIndex = (Index)objectInputStream.readObject();
                    List<Index> reachables = new ArrayList<>();
                    if(this.matrix!=null){
                        reachables.addAll(this.matrix.getReachable(findNeighborsIndex));
                        // print result in server
                        System.out.println("reachables from " + findNeighborsIndex + ": " + reachables);
                        // send to socket's OutputStream
                        objectOutputStream.writeObject(reachables);
                    }
                    break;
                }

                case "start index":{
                    this.start = (Index)objectInputStream.readObject();
                    break;
                }

                case "end index":{
                    this.end = (Index)objectInputStream.readObject();
                    break;
                }

                case "getAllSCC":{
                    List<HashSet<Index>> setOfSCCs = new ArrayList<>();
                    if(this.matrix != null){
                        setOfSCCs.addAll(this.matrix.getAllSCCs2());
                    }
                    System.out.println("List of SCCs:\n" + setOfSCCs);
                    objectOutputStream.writeObject(setOfSCCs);
                    break;
                }

                case "find shortest paths":{
                    List<List<Index>> allPaths = new ArrayList<>();
                    //int[][] primitiveMatrix = (int[][])objectInputStream.readObject(); INPUT OF 2dArray
                    Index source = (Index)objectInputStream.readObject();
                    Index dest = (Index)objectInputStream.readObject();
                    if(this.matrix != null){
                        allPaths.addAll(new BFSvisit().
                                allPathsToDestination(this.matrix, source, dest));
                    }
                    if(allPaths.size() != 0) System.out.println(allPaths);
                    objectOutputStream.writeObject((allPaths));
                    break;
                }

              /*  case "submarines":{
                    int num = -2;
                    try{
                        num = matrix.submarines();
                    }catch (InterruptedException e){}
                    if(num == -1) System.out.println("Submarines: inValid input!");
                    else System.out.println("there are "+num + " submarines");
                    objectOutputStream.writeObject(num);
                    break;
                }*/

                case "submarines":{
                    int num = matrix.submarinesAnotherVersion();
                    System.out.println("there are "+num + " submarines");
                    objectOutputStream.writeObject(num);
                    break;
                }

                case "shortestWeightedPath":{
                    Matrix matrix = new Matrix((int[][])objectInputStream.readObject());
                    List<List<Index>> paths = new ArrayList<>();
                    Index source = (Index)objectInputStream.readObject();
                    Index dest = (Index)objectInputStream.readObject();
                    paths.addAll(new LightestPath().
                            allPathsToDestination(matrix, source, dest));
                    System.out.println("Lightest paths are: " + paths);
                    objectOutputStream.writeObject((paths));
                    break;
                }

                case "stop":{
                    doWork = false;
                    break;
                }
            }
        }
    }
}
