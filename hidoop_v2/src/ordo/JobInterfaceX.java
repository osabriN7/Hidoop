
package ordo;

import map.MapReduce;
import formats.Format;

public interface JobInterfaceX  extends JobInterface {
    public void setReduceNbr(int reduceNbr);
    public void setMapNbr(int mapNbr);
    public void setOutputFormat(Format.Type ft);
    public void setOutputFname(String fname);
    public void setSortComparator(SortComparator sc);
    
    public int getReduceNbr();
    public int getMapNbr();
    public Format.Type getInputFormat();
    public Format.Type getOutputFormat();
    public String getInputFname();
    public String getOutputFname();
    public SortComparator getSortComparator();
}