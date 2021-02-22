package ordo;

import map.MapReduce;
import formats.Format;

public interface JobInterface {

	public void setInputFormat(Format.Type ft);
    public void setInputFname(String fname);
    public void startJob (MapReduce mr);
}