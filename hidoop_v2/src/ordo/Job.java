package ordo;

import formats.Format;
import formats.KVFormat;
import formats.LineFormat;

import hdfs.HdfsClient;
import hdfs.NameNode;
import hdfs.Node;
import map.MapReduce;
import map.Reducer;
import config.Settings;

import javax.naming.Name;
import java.io.File;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.UnknownHostException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Set;

public class Job extends Thread implements JobInterfaceX {

    private String inFilname;
    private String outFilename;
    private Format.Type inputFormat;
    private Format.Type outputFormat;
    private int mapNbr;
    private int reduceNbr;
    private SortComparator sc;
    private Worker worker;
    private MapReduce mr;
    private LineFormat reader;
    private KVFormat writer;
    private CallBack cb;
    private int CHUNKNB;
    private int chunknb = 0;

    public Job() {

    }

    public Job(Worker w, MapReduce mr, LineFormat reader, KVFormat writer, CallBack cb) {
        this.worker = w;
        this.mr = mr;
        this.reader = reader;
        this.writer = writer;
        this.cb = cb;
    }

    public void run() {
        try {
            // cb.decrement();
            // System.out.println("eeee");
            worker.runMap(mr, reader, writer, cb);
            chunknb++;
            // cb.increment();
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    public void runReduce(Reducer r, Format reader, Format writer) {
        // System.out.println();
        reader.open(Format.OpenMode.R);
        writer.open(Format.OpenMode.W);
        r.reduce(reader, writer);
        reader.close();
        writer.close();
    }

    @Override
    public void startJob(MapReduce mr) {
        int repFactor = 1;
        try {
            // HdfsClient.HdfsWrite(this.inputFormat, inFilname, repFactor);
        } catch (Exception e) {
            e.printStackTrace();
        }

        String[] pathTab = inFilname.split("/");
        String nom = pathTab[pathTab.length - 1];
        String pathFolder = inFilname.split(nom)[0];

        int chunksNbr = 0;

        ArrayList<Worker> workers = new ArrayList<>();

        // String[] hosts = {"queen.enseeiht.fr", "scorpion.enseeiht.fr"};

        try {
            NameNode nameNode = (NameNode) Naming.lookup("//" + Settings.NAMENODE +":"+Settings.NAMENODE_PORT+"/NameNode");
            int hostsNbr = nameNode.getDataNodeDispo().size();
            InetAddress adresse = InetAddress.getLocalHost();

            for (int i = 0; i < hostsNbr; i++) {
                try {
                    Worker d = (Worker) Naming
                            .lookup("//" + adresse.getHostName() + ":" + Settings.PORTS[i] + "/worker");
                    workers.add(d);
                } catch (MalformedURLException | RemoteException | NotBoundException e) {
                    e.printStackTrace();
                }
            }
            ArrayList<CallBack> cbs = new ArrayList<>();

            HashMap<Node, ArrayList<Integer>> mData = nameNode.demandeLecture(nom);
            CHUNKNB = 0;

            for (HashMap.Entry<Node, ArrayList<Integer>> entry : mData.entrySet()) {
                CHUNKNB += entry.getValue().size();
            }
            for (int i = 0; i < CHUNKNB; i++)
                cbs.add(new CallBackImpl());

            Thread[] th = new Thread[CHUNKNB];

            for (HashMap.Entry<Node, ArrayList<Integer>> entry : mData.entrySet()) {
                int port = entry.getKey().getPort();
                String host = entry.getKey().getHostName();
                chunksNbr += entry.getValue().size();

                for (int chunk : entry.getValue()) {
                    KVFormat writer = new KVFormat(
                            "DataNodeServers/DataNodeServer-" + host + "-" + port + "/" + nom + "-map-" + chunk);
                    KVFormat kvReader = new KVFormat(
                            "DataNodeServers/DataNodeServer-" + host + "-" + port + "/" + nom + "-" + chunk);
                    LineFormat lineReader = new LineFormat(
                            "DataNodeServers/DataNodeServer-" + host + "-" + port + "/" + nom + "-" + chunk);
                    if (getInputFormat() == Format.Type.KV) {
                    } else {

                        cb = cbs.get(0);
                        cbs.add(cbs.remove(0));
                        th[chunk] = new Job(workers.get(chunk % hostsNbr), mr, lineReader, writer, cb);

                        th[chunk].start();

                    }
                }
            }
            for (int i = 0; i < CHUNKNB; i++) {
                th[i].join();
            }

            setReduceNbr(1);
            setMapNbr(chunksNbr);

        } catch (MalformedURLException | RemoteException | NotBoundException | InterruptedException e) {
            e.printStackTrace();
        } catch (UnknownHostException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }

        try {
            HdfsClient.HdfsRead(nom + "-map", "reduce-" +nom );
            //HdfsClient.HdfsDelete(nom);

            KVFormat reduceReader = new KVFormat("reduce-" +nom);
            KVFormat reduceWriter = new KVFormat(  pathFolder + nom + "-res");

            /** Run the Reduce Operation **/
            reduceWriter.open(Format.OpenMode.W);
            reduceReader.open(Format.OpenMode.R);
            mr.reduce(reduceReader, reduceWriter);
            reduceReader.close();
            reduceWriter.close();

            /** Supprimer le fichier avec le suffixe reduce- (fichier temporaire) **/
            File f = new File("reduce-" + nom);
            f.delete();
            System.out.println("Reduce Done !!");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void setInputFormat(Format.Type ft) {
        this.inputFormat = ft;
    }

    @Override
    public Format.Type getInputFormat() {
        return this.inputFormat;
    }

    /////////////////////////////////////////////////////////////////////////

    @Override
    public void setInputFname(String fname) {
        this.inFilname = fname;
    }

    @Override
    public String getInputFname() {
        return this.inFilname;
    }

    /////////////////////////////////////////////////////////////////////////

    @Override
    public void setOutputFormat(Format.Type ft) {
        this.outputFormat = ft;
    }

    @Override
    public Format.Type getOutputFormat() {
        return this.outputFormat;
    }

    /////////////////////////////////////////////////////////////////////////

    @Override
    public void setOutputFname(String fname) {
        this.outFilename = fname;
    }

    @Override
    public String getOutputFname() {
        return this.outFilename;
    }

    /////////////////////////////////////////////////////////////////////////

    @Override
    public void setMapNbr(int mapNbr) {
        this.mapNbr = mapNbr;
    }

    @Override
    public int getMapNbr() {
        return this.mapNbr;
    }

    /////////////////////////////////////////////////////////////////////////

    @Override
    public void setReduceNbr(int reduceNbr) {
        this.reduceNbr = reduceNbr;
    }

    @Override
    public int getReduceNbr() {
        return this.reduceNbr;
    }

    /////////////////////////////////////////////////////////////////////////

    @Override
    public void setSortComparator(SortComparator sc) {
        this.sc = sc;
    }

    @Override
    public SortComparator getSortComparator() {
        return this.sc;
    }
}
