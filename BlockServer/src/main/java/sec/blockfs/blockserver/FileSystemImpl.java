package sec.blockfs.blockserver;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.FileSystemException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.lang.Math;

import sec.blockfs.blockutility.BlockUtility;

public class FileSystemImpl implements FileSystem {

    public static final String BASE_PATH = "C:\\Temp";
	public static final int BLOCK_SIZE = 8000;

    public FileSystemImpl() {
        File file = new File(BASE_PATH);
        if (!file.exists()) {
            file.mkdir();
        }
    }

    @Override
    public void write(byte[] contents) throws FileSystemException  {
        try {
            byte[] dataDigest = BlockUtility.digest(contents);
            String fileName = BlockUtility.getKeyString(dataDigest);
            String filePath = BASE_PATH + File.separatorChar + fileName;

            System.out.println("Writing data block: " + filePath);
            FileOutputStream stream = new FileOutputStream(filePath);
            stream.write(contents);

            stream.close();
        } catch (IOException e) {
            System.out.println("Filesystem error - write operation failed" + e.getMessage());
            throw new FileSystemException("Write operation failed");
        }
    }
    
    /*
     * Write contents to blocks, receives client ID and data to write.
     * Data is written on the remaining size of the last block, if
     * not enough size then is written on new blocks.
     * */
    public void writeBlocks(int id, byte[] contents) throws FileSystemException {
		long remainingSize;
		int blockID = getLastBlockID(id);
		
		try {
			int contents_remain = contents.length;
			int current_offset = 0;
			
			String fileName = Integer.toString(id) + "_" + Integer.toString(blockID);
			String filePath = BASE_PATH + File.separatorChar + fileName;
			
			Long currentBlockSize = new File(filePath).length();
			remainingSize = BLOCK_SIZE - Integer.valueOf(currentBlockSize.intValue());
			
			//Check if incomplete block
			if(remainingSize > 0) {
				System.out.println("Writing on not full data block: " + filePath);
				FileOutputStream stream = new FileOutputStream(filePath, true);
				stream.write(contents, current_offset, remainingSize);
				stream.close();
				
				contents_remain -= remainingSize;
				current_offset += remainingSize;
				
				//Write on new blocks
				for(int block = 1; contents_remain > 0; block++) {
					fileName = Integer.toString(id) + "_" + Integer.toString(blockID + block);
					filePath = BASE_PATH + File.separatorChar + fileName;
					
					System.out.println("Writing on new data block: " + filePath);
					FileOutputStream stream2 = new FileOutputStream(filePath);
					stream2.write(contents, current_offset, BLOCK_SIZE);
					stream2.close();
					
					contents_remain -= BLOCK_SIZE;
					current_offset += BLOCK_SIZE;
				}
			} else {
				for(int blockI = 0; contents_remain > 0; blockI++) {
					fileName = Integer.toString(id) + "_" + Integer.toString(blockID + blockI);
					filePath = BASE_PATH + File.separatorChar + fileName;
					
					System.out.println("Writing on data block: " + filePath);
					FileOutputStream streamN = new FileOutputStream(filePath);
					streamN.write(contents, current_offset, BLOCK_SIZE);
					streamN.close();
					
					contents_remain -= BLOCK_SIZE;
					current_offset += BLOCK_SIZE;
				}
			}
		} catch (IOException e) {
			System.out.println("Filesystem error - write block operation failed" + e.getMessage());
			throw new FileSystemException("Write block operation failed");
		}
	}
	
	/*
	 * Method to search for last block part
	 */
	private int getLastBlockID(int id) {
		int lastID = 0;
		File f;
		
		while(true) {
			String fileName = Integer.toString(id) + "_" + Integer.toString(lastID);
			String filePath = BASE_PATH + File.separatorChar + fileName;
		
			f = new File(filePath);
			if(f.exists()) { lastID++; }
			else { break; }
		}
		
		return lastID;
	}
	

    @Override
    public byte[] read(String blockName) throws DataIntegrityFailureException, FileSystemException {
        byte[] dataBlock = new byte[0];
        byte[] nextBlock;
        byte[] previousBlocks;
        
        int blockID = 0;
        
        try {
			while(true) {
				String filePath = BASE_PATH + File.separatorChar + blockName + "_" + blockID++;
				
				if(!new File(filePath).exists()) break;
				else System.arraycopy(dataBlock, 0, previousBlock, 0, dataBlock.length);
				FileInputStream stream = new FileInputStream(filePath);
            
				Path path = Paths.get(filePath);
				nextBlock = Files.readAllBytes(path);
				
				//Combine previous blocks obtained with new one
				dataBlock = new byte[previousBlocks.length + nextBlock.length];
				System.arraycopy(previousBlocks, 0, dataBlock, 0, previousBlocks.length);
				System.arraycopy(nextBlock, 0, dataBlock, previousBlocks.length, nextBlock.length);
				
				stream.close();
			}
        } catch (Exception e) {
            throw new FileSystemException("Unable to read block "+blockName);
        }
        
        //Need to check how digest should be done
        byte[] dataDigest = BlockUtility.digest(dataBlock);
        String expectedFilename = BlockUtility.getKeyString(dataDigest);
        
        if (!blockName.equals(expectedFilename))
            throw new DataIntegrityFailureException("The stored data has been changed");
        
        return dataBlock;
    }

}
