import java.util.*;

public class BFSvisit {
    /********************************************/
    /********* Task #2 **************************/
    /********************************************/

    /**
     * This function implements BFS and returns all the paths from source to destination, use finding neighbours/reachables
     * with getCrossNeighbors() method
     * @param matrix type of Matrix
     * @param source Index
     * @param dest Index
     * @return all the paths from source to destination
     */
    public List<List<Index>> allPathsToDestination(Matrix matrix, Index source, Index dest){
        List<List<Index>> result = new ArrayList<>();
        Set<Index> finished = new HashSet<>();
        Queue<List<Index>> queue = new LinkedList<>();
        queue.add(Arrays.asList(source));
        if(!matrix.getSingleSCC(matrix,source).contains(dest)){
            System.out.println("there is no path!");
            return result;
        }
        while(!queue.isEmpty()){
            List<Index> path = queue.poll();
            Index lastIndex = path.get(path.size()-1);

            if(lastIndex.equals(dest)){
                result.add(new ArrayList<>(path));
            } else{
                finished.add(lastIndex);
                List<Index> reachableIndices = (List<Index>) matrix.getReachable(lastIndex);
                for(Index neighbor : reachableIndices){
                    if(!finished.contains(neighbor)){
                        List<Index> list = new ArrayList<>(path);
                        list.add(neighbor);
                        queue.add(list);
                    }
                }
            }

        }
        filterPaths(result);
        return result;
    }

    /**
     * This function returns the shortest paths in a graph, when we already know that the first path in the list is (one of) the shortest path.
     * @param result - contains all the paths
     * @return result - after filtering it contains the shortest paths.
     */
    public List<List<Index>> filterPaths(List<List<Index>> result){
        List<Index> singleFiltered = result.get(0);
        int minSize = singleFiltered.size();
        result.removeIf(singleArray -> singleArray.size() > minSize);
        return result;
    }


}



