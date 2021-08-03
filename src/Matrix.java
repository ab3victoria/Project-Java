
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.stream.Collectors;

public class Matrix {
    /**
     * Neighboring Indices are up,down, left,right
     * 1 0 0
     * 0 1 1
     * 0 0 0
     * 1 1 1
     * <p>
     * [[(0,0),
     * [(1,1) ,(1,2)],
     * [(3,0),(3,1),(3,2)]]
     * <p>
     * <p>
     * 1 0 0
     * 0 1 1
     * 0 1 0
     * 0 1 1
     */

    int[][] primitiveMatrix;

    public Matrix(int[][] oArray) {
        List<int[]> list = new ArrayList<>();
        for (int[] row : oArray) {
            int[] clone = row.clone();
            list.add(clone);
        }
        primitiveMatrix = list.toArray(new int[0][]);
    }


    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        for (int[] row : primitiveMatrix) {
            stringBuilder.append(Arrays.toString(row));
            stringBuilder.append("\n");
        }
        return stringBuilder.toString();
    }

    /**
     Gets a list of neighbours of an index - from left, right, up and down
     */
    public Collection<Index> getNeighbors(final Index index) {
        Collection<Index> list = new ArrayList<>();
        int extracted = -1;
        try {
            extracted = primitiveMatrix[index.row + 1][index.column];//down
            list.add(new Index(index.row + 1, index.column));
        } catch (ArrayIndexOutOfBoundsException ignored) {
        }
        try {
            extracted = primitiveMatrix[index.row][index.column + 1];//right
            list.add(new Index(index.row, index.column + 1));
        } catch (ArrayIndexOutOfBoundsException ignored) {
        }
        try {
            extracted = primitiveMatrix[index.row - 1][index.column];//up
            list.add(new Index(index.row - 1, index.column));
        } catch (ArrayIndexOutOfBoundsException ignored) {
        }
        try {
            extracted = primitiveMatrix[index.row][index.column - 1];//left
            list.add(new Index(index.row, index.column - 1));
        } catch (ArrayIndexOutOfBoundsException ignored) {
        }
        return list;
    }

    /**
     Gets a list of all neighbours of an index - including cross neighbors
     */
    public Collection<Index> getCrossNeighbors(final Index index){
        Collection<Index> list = new ArrayList<>();
        list = getNeighbors(index);
        int extracted = -1;
        try {
            extracted = primitiveMatrix[index.row - 1][index.column - 1];//left-up
            list.add(new Index(index.row - 1, index.column - 1));
        } catch (ArrayIndexOutOfBoundsException ignored) {
        }
        try {
            extracted = primitiveMatrix[index.row + 1][index.column + 1];//right-down
            list.add(new Index(index.row + 1, index.column + 1));
        } catch (ArrayIndexOutOfBoundsException ignored) {
        }
        try {
            extracted = primitiveMatrix[index.row + 1][index.column - 1];//left-down
            list.add(new Index(index.row + 1, index.column - 1));
        } catch (ArrayIndexOutOfBoundsException ignored) {
        }
        try {
            extracted = primitiveMatrix[index.row - 1][index.column + 1];//right-up
            list.add(new Index(index.row - 1, index.column + 1));
        } catch (ArrayIndexOutOfBoundsException ignored) {
        }
        return list;
    }

    public Collection<Index> getReachable(Index index) {
        ArrayList<Index> filteredIndices = new ArrayList<>();
        this.getCrossNeighbors(index).stream().filter(i -> getValue(i) == 1)
                .map(neighbor -> filteredIndices.add(neighbor)).collect(Collectors.toList());
        return filteredIndices;
    }

    public final int[][] getPrimitiveMatrix() {
        return primitiveMatrix;
    }

    public int getValue(final Index index) {
        return primitiveMatrix[index.row][index.column];
    }

    public void printMatrix() {
        for (int[] row : primitiveMatrix) {
            String s = Arrays.toString(row);
            System.out.println(s);
        }
    }


    public List<Index> matrixToList(int[][] matrix) {
        ArrayList<Index> list = new ArrayList<>();
        for (int i = 0; i < matrix.length; i++) {
            for (int j = 0; j < matrix[i].length; j++)
                list.add(new Index(i, j));
        }
        return list;
    }

    /********************************************/
    /********* Task #1 **************************/
    /********************************************/

    /** Returns a list of indices with value equals to "1" */
    public ArrayList<Index> getOnes() {
        ArrayList<Index> list = new ArrayList<>();
        this.matrixToList(this.primitiveMatrix).stream().filter(i -> getValue(i) == 1).map(list::add).collect((Collectors.toList()));
        return list;
    }


    /** Returns one strongly connected component starts from specific index, using DFS algorithm*/
    public Collection<Index> getSingleSCC(Matrix matrix, Index index) {
        TraversableMatrix myTraversableMat = new TraversableMatrix(matrix);
        myTraversableMat.setStartIndex(index);
        ThreadLocalDfsVisit<Index> singleSearch = new ThreadLocalDfsVisit<Index>();
        HashSet<Index> singleSCC = (HashSet<Index>) singleSearch.traverse(myTraversableMat);
        return singleSCC;
    }



    public Collection<? extends HashSet<Index>> getAllSCCs2() {
        List<Index> listOfOnes = this.getOnes();
        List<Index> firstHalfOnes = new ArrayList<Index>(listOfOnes.subList(0, listOfOnes.size() / 2));
        List<Index> secondHalfOnes = new ArrayList<Index>(listOfOnes.subList(listOfOnes.size() / 2, listOfOnes.size()));
        final List<Index> synFirstHalfOnes = Collections.synchronizedList(firstHalfOnes);
        final List<Index> synSecondHalfOnes = Collections.synchronizedList(secondHalfOnes);
        Set<HashSet<Index>> multiComponents = Collections.synchronizedSet(new HashSet<HashSet<Index>>());
        Thread part1 = new Thread(() -> {
            while (synFirstHalfOnes.size()!= 0) {
                HashSet<Index> singleSCC = (HashSet<Index>) getSingleSCC(this, synFirstHalfOnes.remove(0));
                synSecondHalfOnes.removeAll(singleSCC);
                synFirstHalfOnes.removeAll(singleSCC);
                multiComponents.add(singleSCC);
            }
        });
        Thread part2 = new Thread(() -> {
            while (synSecondHalfOnes.size() != 0) {
                HashSet<Index> singleSCC2 = (HashSet<Index>) getSingleSCC(this, synSecondHalfOnes.remove(0));
                synFirstHalfOnes.removeAll(singleSCC2);
                synSecondHalfOnes.removeAll(singleSCC2);
                multiComponents.add(singleSCC2);
            }
        });

        part1.start();
        part2.start();
        try {
            part1.join();
            part2.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        List<HashSet<Index>> result = new ArrayList<HashSet<Index>>();

        //Adding each SCC for the result array using iterator as it should be with an Collection.Synchronized matter.
        synchronized (multiComponents) {
            Iterator<HashSet<Index>> it = multiComponents.iterator();
            while (it.hasNext()) {
                result.add(it.next());
            }
        }
        return result;

    }




    /********************************************/
    /********* Task #3 another version***********/
    /********************************************/

    /**
     * This function checks the number of submarines that we have, it calls to isValidSubmarine function that checks if the submarines are valid.
     * @return result which is the number of submarines that we have.
     */
    public int submarinesAnotherVersion(){
        int result = 0;
        List<HashSet<Index>> scc = (List<HashSet<Index>>) getAllSCCs2();
        for(HashSet<Index> singleScc : scc){
            result += isValidSubmarine(singleScc);
        }
        return result;
    }

    private int isValidSubmarine(HashSet<Index> scc){
        if(scc.size()==1){
            return 0;
        }

        AtomicInteger rightBound = new AtomicInteger();
        AtomicInteger leftBound = new AtomicInteger();
        AtomicInteger topBound = new AtomicInteger();
        AtomicInteger bottomBound = new AtomicInteger();

        /**
         * In this section we check our submarine bounds, the Collections.max/min returns the max/min value
         * in our case the values are the columns/rows of indices, that belong to the collection which in this case is the scc
         */
        Thread part1 = new Thread(() -> {
            rightBound.set(Collections.max(scc, Comparator.comparingInt(Index::getColumn)).getColumn());
        });
        Thread part2 = new Thread(() -> {
            leftBound.set(Collections.min(scc, Comparator.comparingInt(Index::getColumn)).getColumn());
        });
        Thread part3 = new Thread(() -> {
            topBound.set(Collections.min(scc, Comparator.comparingInt(Index::getRow)).getRow());
        });
        Thread part4 = new Thread(() -> {
            bottomBound.set(Collections.max(scc, Comparator.comparingInt(Index::getRow)).getRow());
        });

        part1.start();
        part2.start();
        part3.start();
        part4.start();
        try {
            part1.join();
            part2.join();
            part3.join();
            part4.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        /**
         * Here we use the calculation of a rectangle's area and we check if the size of the rectangle is the same as the size of the scc
         * this way we ensure that the submarine is valid (like in a case when we have a square but the middle index's value is 0)
         */
        int sizeOfScc = (rightBound.get() - leftBound.get() +1 ) * (bottomBound.get() - topBound.get() + 1);
        if(scc.size()==sizeOfScc){
            return 1;
        }
        return 0;
    }



}


