package rstar;

import java.io.File;

import rstar.dto.TreeDTO;
import util.Constants;

public class LoadRstar {

	public static void main(String[] args) {
		StorageManagerMemory storage = new StorageManagerMemory();
		File f = new File(Constants.TREE_FILE);
		TreeDTO rt = storage.loadTree(f);
		System.out.println(rt);
	}

}
