package sec.blockfs.blockserver;

public interface FileSystem {
	// initializes the file system
	int FS_init();
	// writes the 'contents' of length 'size' from the 'pos' position
	void FS_write(int position, int size, byte[] contents);
	// reads the data block identified by 'id'; returns the number of bytes read
	int FS_read(int id, int position, int size, byte[] buffer);
}
