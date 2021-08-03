import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/********************************************/
/********* Task #4 in Bellman Ford  , TODO: multithreaded way **************************/
/********************************************/

public class LightestPath<T> {

    /**
     * This function finds all paths from source to destination according to the neighbours (up down left and right) of the indices.
     * @param matrix type of Matrix
     * @param source Index
     * @param dest Index
     * @return all paths from source to destination
     */
    public List<List<Index>> allPathsToDestination(Matrix matrix, Index source, Index dest){
        List<List<Index>> result = new ArrayList<>();
        //Set<Index> finished = new HashSet<>();
        Queue<List<Index>> queue = new LinkedList<>();
        queue.add(Arrays.asList(source));
        while(!queue.isEmpty()){
            List<Index> path = queue.poll();
            Index lastIndex = path.get(path.size()-1);

            if(lastIndex.equals(dest)){
                result.add(new ArrayList<>(path));
            } else{
                // finished.add(lastIndex);
                List<Index> neighborIndices = (List<Index>) matrix.getNeighbors(lastIndex);
                for(Index neighbor : neighborIndices){
                    if(!path.contains(neighbor)){
                        List<Index> list = new ArrayList<>(path);
                        list.add(neighbor);
                        queue.add(list);
                    }
                }
            }

        }
        result = filterPathsThreads(result,matrix);
        return result;
    }


    /**
     * This function uses multi-threads to achieve maximum effect of calculating values of the lists.
     * In sumLogic we calculate the minimum sum of each path, then we filter and returns the collection of paths with the minimum weight.
     * @param result the lists of all paths to destination
     * @param matrix type of Matrix
     * @return all light-weight-paths from source to destination
     */
    public List<List<Index>> filterPathsThreads(List<List<Index>> result, Matrix matrix) {
        List<List<Index>> filteredResults;
        Map<List<Index>,Integer> pathSum = new HashMap<>();
        final Map<List<Index>,Integer> synPathSum = Collections.synchronizedMap(pathSum);
        final List<List<Index>> synResult = Collections.synchronizedList(result);
        AtomicInteger minPathSum = new AtomicInteger(Integer.MAX_VALUE);

        ThreadPoolExecutor threadPool =
                new ThreadPoolExecutor(2,3,30, TimeUnit.MILLISECONDS,new LinkedBlockingQueue<>());

        Runnable sumLogic = ()->{
            List<Index> specificPath = synResult.remove(0);
            AtomicInteger sum= new AtomicInteger(0);
            for(Index i : specificPath){
                sum.addAndGet(matrix.getValue(i));
            }

            if(sum.get() <= minPathSum.get()) {
                minPathSum.set(sum.get());
                synPathSum.put(specificPath, sum.get());
            }

        };

        for(int i=0;i<result.size();i++){
            threadPool.execute(sumLogic);
        }
        threadPool.shutdown();
        try {
            threadPool.awaitTermination(Long.MAX_VALUE,TimeUnit.NANOSECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        filteredResults = pathSum.entrySet().stream().filter(i -> i.getValue() == minPathSum.get()).map(Map.Entry::getKey)
                .collect(Collectors.toList());
        return filteredResults;
    }






}


