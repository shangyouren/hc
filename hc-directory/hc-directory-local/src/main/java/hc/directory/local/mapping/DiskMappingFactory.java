package hc.directory.local.mapping;

import java.io.File;
import java.io.IOException;

public interface DiskMappingFactory
{

    DiskMapping create(File file, long blockIndex, int diskBlockSize) throws IOException;
}
