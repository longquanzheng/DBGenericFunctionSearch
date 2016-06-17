package rstar;

import rstar.dto.PointDTO;
import rstar.dto.TreeDTO;
import rstar.interfaces.IDiskQuery;
import rstar.nodes.RStarLeaf;
import rstar.nodes.RStarNode;
import util.Constants;

import java.io.*;
import java.nio.channels.FileChannel;
import java.util.HashMap;

/**
 * provides all disk related functionality like
 * loading and saving of nodes, points and tree.
 */
public class StorageManagerMemory implements IDiskQuery {
    RandomAccessFile dataStore;
    FileChannel dataChannel;
    
    private static HashMap<Long,RStarNode> nodeMap = new HashMap<Long,RStarNode>();
    private static HashMap<Long,PointDTO> ptMap = new HashMap<Long,PointDTO>();
    private static long ptCounter = 0;

    public StorageManagerMemory() {
        try {
            dataStore = new RandomAccessFile(Constants.DATA_FILE, "rw");
            dataChannel = dataStore.getChannel();
        } catch (FileNotFoundException e) {
            System.err.println("Data File failed to be loaded/created. Exiting");
            System.exit(1);
        }
    }

    @Override
    public void saveNode(RStarNode node) {
    	if (node.isLeaf()) {
                RStarLeaf leaf = (RStarLeaf) node;

                if (leaf.hasUnsavedPoints()) {
                    //save unsaved points to disk first.
                    for (int i = leaf.loadedChildren.size() - 1; i >= 0; i--) {
                        leaf.childPointers.add(savePoint(leaf.loadedChildren.remove(i).toDTO()));
                    }
                }

                nodeMap.put(node.nodeId, node);
        } else {
            nodeMap.put(node.nodeId, node);
        }
    }

    @Override
    public RStarNode loadNode(long nodeId) throws FileNotFoundException {
        return nodeMap.get(nodeId);
    }

    /**
     * saves a Spatial Point to dataFile on disk and
     * returns the offset of the point in the file.
     *
     * @param pointDTO DTO of the point to be saved
     * @return the location where the point was saved in
     * datafile
     */
    @Override
    public long savePoint(PointDTO pointDTO) {
    	
        long pid = ptCounter++;
        float[] pt = new float[pointDTO.coords.length];
        for(int i=0;i<pointDTO.coords.length;i++){
        	pt[i] = pointDTO.coords[i];
        }
        PointDTO newPt = new PointDTO(pointDTO.oid,pt);
        ptMap.put(pid, newPt);
        return pid;
    }

    /**
     * loads a SpatialPoint from dataFile
     * @param pointer the offset of the point
     *                in dataFile
     * @return DTO of the point. Full SpatialPoint
     * can be easily constructed from the DTO
     */
    @Override
    public PointDTO loadPoint(long pointer) {
        return ptMap.get(pointer);
    }

    /**
     * saves the R* Tree to saveFile.
     * doesn't use RandomAccessFile
     * @param tree the DTO of the tree to be saved
     * @param saveFile saveNode file location
     * @return 1 is successful, else -1
     */
    @Override
    public int saveTree(TreeDTO tree, File saveFile) {
        int status = -1;
        try {
            if(saveFile.exists()) {
                saveFile.delete();
            }

            FileOutputStream fos = new FileOutputStream(saveFile);
            ObjectOutputStream oos = new ObjectOutputStream(fos);

            oos.writeObject(tree);
            oos.flush();
            oos.close();
            status = 1;             // successful saveNode
        } catch (IOException e) {
            System.err.println("Error while saving Tree to " + saveFile.toURI());
        }
        return status;
    }

    /**
     * loads a R* Tree from disk
     * @param saveFile the file to loadNode the tree from
     * @return DTO of the loaded R* Tree, null if none found
     * @throws FileNotFoundException
     */
    @Override
    public TreeDTO loadTree(File saveFile) {
        try {
            FileInputStream fis = new FileInputStream(saveFile);
            ObjectInputStream ois = new ObjectInputStream(fis);

            return (TreeDTO) ois.readObject();

        } catch (IOException e) {
            System.err.println("Exception while loading tree from " + saveFile);
        } catch (ClassNotFoundException e) {
            System.err.println("Exception while loading tree from " + saveFile);
        }
        return null;
    }

    public String constructFilename(long nodeId) {
        return Constants.TREE_DATA_DIRECTORY + "/" + Constants.NODE_FILE_PREFIX + nodeId + Constants.NODE_FILE_SUFFIX;
    }

    public long nodeIdFromFilename(String filename) {
        int i2 = filename.indexOf(Constants.NODE_FILE_SUFFIX);
        assert i2 != -1;
        return Long.parseLong(filename.substring((Constants.TREE_DATA_DIRECTORY+"/"+Constants.NODE_FILE_PREFIX).length(), i2));
    }

    

    public void createDataDir(File saveFile) {
        // check for the node-data directory. create one if doesn't exist
        File dataDir = new File(saveFile.getParentFile(), Constants.TREE_DATA_DIRECTORY);
        if (!dataDir.exists() || !dataDir.isDirectory()) {
            if (!dataDir.mkdir()) {
                System.err.println("Failed to create data directory of the tree. Exiting..");
                System.exit(1);
            }
            System.out.println("Data directory created");
        }
    }
}
