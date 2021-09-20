package luecx.volume;

import luecx.util.Particle;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.libs.org.apache.commons.io.FileUtils;
import org.bukkit.entity.Player;

import java.io.*;
import java.util.ArrayList;

public class Volume {

    // name of the volume
    private final String name;

	// from to and which nodes are constrained
    private final Location from, to;
    private final boolean[][][] isFixed;

	// contains elements which are currently used, indexed by their id
	private ArrayList<Element> elementListByID;

    public Volume(String name, Location from, Location to, Player p) {
        this.name = name;
        this.from = min(from, to);
        this.to   = max(from, to).add(1, 1, 1);

		p.sendMessage("created volume: ["
				+ this.from.getX() + " "
				+ this.from.getY() + " "
				+ this.from.getZ() + "] - ["
				+ this.to.getX() + " "
				+ this.to.getY() + " "
				+ this.to.getZ() + "]");

        isFixed = new boolean
                [(int) (Math.abs(this.to.getX() - this.from.getX())) + 1]
                [(int) (Math.abs(this.to.getY() - this.from.getY())) + 1]
                [(int) (Math.abs(this.to.getZ() - this.from.getZ())) + 1];

    }

    public void render() {
        // render boundary box
        Particle.drawBox(this.from, this.to, this.from.getWorld());

        // render fixed nodes
        int xFrom = (int) from.getX(), xTo = (int) to.getX();
        int yFrom = (int) from.getY(), yTo = (int) to.getY();
        int zFrom = (int) from.getZ(), zTo = (int) to.getZ();

        for (int x = xFrom; x <= xTo; x++) {
            for (int y = yFrom; y <= yTo; y++) {
                for (int z = zFrom; z <= zTo; z++) {
                    int localX = x - xFrom;
                    int localY = y - yFrom;
                    int localZ = z - zFrom;

                    if (isFixed[localX][localY][localZ]) {
                        from.getWorld().spawnParticle(org.bukkit.Particle.BARRIER, new Location(from.getWorld(), x, y, z), 1);
                    }
                }
            }
        }
    }

    public String writeMesh() throws IOException {

        // make sure the directories exist
        this.createFolder();

        // create the names
        String nodeFile    = "plugins/FEMC/" + this.name + "/nodes.txt";
        String elementFile = "plugins/FEMC/" + this.name + "/elements.txt";
        String matFile     = "plugins/FEMC/" + this.name + "/material.txt";
        String bcFile      = "plugins/FEMC/" + this.name + "/bc.txt";
        String loadFile    = "plugins/FEMC/" + this.name + "/loads.txt";
        String totalFile   = "plugins/FEMC/" + this.name + "/all.txt";

        // writes materials
        Materials.writeMaterials(matFile);

        BufferedWriter nodeWriter    = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(nodeFile)));
        BufferedWriter elementWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(elementFile)));
        BufferedWriter bcWriter      = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(bcFile)));
        BufferedWriter loadWriter    = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(loadFile)));
        BufferedWriter totalWriter   = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(totalFile)));

        nodeWriter.write("*NODE, NSET=NALL\n");
        bcWriter.write  ("*NSET, NSET=BC\n");
        loadWriter.write("*CLOAD\n");

        totalWriter.write("*INCLUDE, INPUT=" + nodeFile + "\n");
        totalWriter.write("*INCLUDE, INPUT=" + elementFile + "\n");
        totalWriter.write("*INCLUDE, INPUT=" + matFile + "\n");
        totalWriter.write("*INCLUDE, INPUT=" + bcFile + "\n");
        totalWriter.write("*INCLUDE, INPUT=" + loadFile + "\n");


		Node[][][] nodes = new Node
				[(int) (Math.abs(to.getX() - from.getX())) + 1]
				[(int) (Math.abs(to.getY() - from.getY())) + 1]
				[(int) (Math.abs(to.getZ() - from.getZ())) + 1];

		Element[][][] elements1 = new Element
				[(int) (Math.abs(to.getX() - from.getX()))]
				[(int) (Math.abs(to.getY() - from.getY()))]
				[(int) (Math.abs(to.getZ() - from.getZ()))];


        int xFrom = (int) from.getX(), xTo = (int) to.getX();
        int yFrom = (int) from.getY(), yTo = (int) to.getY();
        int zFrom = (int) from.getZ(), zTo = (int) to.getZ();

        int notNullCounter = 0;
        int blockID = 0;
        int[][] increments = new int[][]{
                {0, 0, 0},
                {1, 0, 0},
                {1, 1, 0},
                {0, 1, 0},
                {0, 0, 1},
                {1, 0, 1},
                {1, 1, 1},
                {0, 1, 1}
        };


        // store points for each element for each respective material
        ArrayList<ArrayList<Element>> elementLists = new ArrayList<>();
        for (int i = 0; i < Materials.values().length; i++) {
            elementLists.add(new ArrayList<>());
        }
        elementListByID = new ArrayList<>();

        // check all elements
        for (int x = xFrom; x < xTo; x++) {
            for (int y = yFrom; y < yTo; y++) {
                for (int z = zFrom; z < zTo; z++) {
                    Block b = from.getWorld().getBlockAt(x, y, z);

                    // check if material is valid for fem
                    int femIndex = Materials.getFEMMaterialID(b.getType().ordinal());

                    if (femIndex >= 0) {

                        // create the element
                        Element element = new Element(b);

                        // assign the 8 nodes
                        for (int i = 0; i < 8; i++) {
                            int localX = x - xFrom + increments[i][0];
                            int localY = y - yFrom + increments[i][1];
                            int localZ = z - zFrom + increments[i][2];

                            if (nodes[localX][localY][localZ] == null) {
                                nodes[localX][localY][localZ] = new Node(localX, localY, localZ, ++notNullCounter);
                                nodeWriter.write(notNullCounter + "," + localX + "," + localY + "," + localZ + "\n");
                                if (isFixed[localX][localY][localZ]) {
                                    bcWriter.write(notNullCounter + ",\n");
                                }
                            }
                            nodes[localX][localY][localZ].force_y -= 9.81 * Materials.values()[femIndex].density / 8.0;
                            element.getIndices()[i] = nodes[localX][localY][localZ].getId();
                        }

                        // store it locally
                        elements1[x - xFrom][y - yFrom][z - zFrom] = element;
                        elementLists.get(femIndex).add(element);
                    } else {
                        elements1[x - xFrom][y - yFrom][z - zFrom] = null;
                    }
                }
            }
        }

        // write boundary conditions
        bcWriter.write("*BOUNDARY\n");
        bcWriter.write("BC, 1, 0\n");
        bcWriter.write("BC, 2, 0\n");
        bcWriter.write("BC, 3, 0\n");

        // write loads
        for (int x = xFrom; x <= xTo; x++) {
            for (int y = yFrom; y <= yTo; y++) {
                for (int z = zFrom; z <= zTo; z++) {
                    int localX = x - xFrom;
                    int localY = y - yFrom;
                    int localZ = z - zFrom;

                    Node node = nodes[localX][localY][localZ];
                    if (node != null) {
                        loadWriter.write(node.id + ",2," + node.force_y + "\n");
                    }
                }
            }
        }

        // write element sets
        int id = 0;
        int elementID = 1;
        for (ArrayList<Element> elements : elementLists) {
            id++;
            if (elements.size() == 0) continue;
            elementWriter.write("*ELEMENT, ELSET=ELSET_" + id + ", TYPE=C3D8\n");
            for (Element element : elements) {
                elementListByID.add(element);
                elementWriter.write(elementID++ + ", ");
                for (int index : element.getIndices()) {
                    elementWriter.write(index + ", ");
                }
                elementWriter.write("\n");
            }
        }


        elementWriter.close();
        nodeWriter   .close();
        bcWriter     .close();
        loadWriter   .close();
        totalWriter  .close();

        return totalFile;
    }

    public void storeResult(int elementID, String line) {
        this.elementListByID.get(elementID).getResult().setFromString(line);
    }

    public void displayDisplaced(float factor) {
        for (Element el : elementListByID) {
            el.hide();
        }
        for (Element el : elementListByID) {
            el.showDisplaced(factor);
        }
    }

    public void processResultFile(String file) {
        BufferedReader br = null;
        try {
            br = new BufferedReader(new FileReader(file));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        String line = null;
        while (true) {
            try {
                line = br.readLine();
            } catch (IOException e) {
                e.printStackTrace();
                break;
            }
            if (line == null) break;

            String[] split = line.replace(" ", "").split(",");
            this.storeResult(Integer.parseInt(split[0])-1, line);
        }
    }

    public void constraint(Block block, boolean value) {
        int[][] increments = new int[][]{
                {0, 0, 0},
                {1, 0, 0},
                {1, 1, 0},
                {0, 1, 0},
                {0, 0, 1},
                {1, 0, 1},
                {1, 1, 1},
                {0, 1, 1}
        };

        for (int i = 0; i < 8; i++) {
            int localX = (int) block.getLocation().getX() + increments[i][0] - (int) from.getX();
            int localY = (int) block.getLocation().getY() + increments[i][1] - (int) from.getY();
            int localZ = (int) block.getLocation().getZ() + increments[i][2] - (int) from.getZ();
            if (localX < 0 || localX >= isFixed.length) continue;
            if (localY < 0 || localY >= isFixed[0].length) continue;
            if (localZ < 0 || localZ >= isFixed[0][0].length) continue;
            isFixed[localX][localY][localZ] = value;
        }
    }

    public void createFolder(){
        new File("plugins/FEMC/" + name + "/").mkdirs();
    }

    public void deleteFolder(){
        try {
            FileUtils.deleteDirectory(new File("plugins/FEMC/" + name + "/"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void compute(Player player){
        String file;
        try {
            file = writeMesh();
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }
        String inFile  = file;
        String outFile = file.replace(".txt", ".sol");
        player.sendMessage("§6§l[FEMC] Wrote files to: " + file);
        new Thread(() -> {
            try {
                // A Runtime object has methods for dealing with the OS
                Runtime r = Runtime.getRuntime();
                Process p;     // Process tracks one external native process
                BufferedReader is;  // reader for output of process
                String line;

                // Our argv[0] contains the program to run; remaining elements
                // of argv contain args for the target program. This is just
                // what is needed for the String[] form of exec.
                p = r.exec("./plugins/FEM.exe "
                        + inFile + " "
                        + outFile);
                player.sendMessage("§6§l[FEMC] Began computation...");

                // getInputStream gives an Input stream connected to
                // the process p's standard output. Just use it to make
                // a BufferedReader to readLine() what the program writes out.
                is = new BufferedReader(new InputStreamReader(p.getInputStream()));

                while ((line = is.readLine()) != null)
                    System.out.println(line);

                System.out.flush();
                try {
                    p.waitFor();  // wait for process to complete
                } catch (InterruptedException e) {
                    System.err.println(e);  // "Can'tHappen"
                    return;
                }
                player.sendMessage("§6§l[FEMC] Finished computation; exit code: " + p.exitValue());
                this.processResultFile(outFile);
                player.sendMessage("§6§l[FEMC] Postprocessed results");
                deleteFolder();

            } catch (IOException e) {
                System.out.println(e);
            }
        }).start();
    }


    public static Location min(Location a, Location b) {
        return new Location(
                a.getWorld(),
                Math.min(a.getX(), b.getX()),
                Math.min(a.getY(), b.getY()),
                Math.min(a.getZ(), b.getZ()));
    }

    public static Location max(Location a, Location b) {
        return new Location(
                a.getWorld(),
                Math.max(a.getX(), b.getX()),
                Math.max(a.getY(), b.getY()),
                Math.max(a.getZ(), b.getZ()));
    }

}
