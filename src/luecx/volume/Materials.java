package luecx.volume;

import org.bukkit.Material;

import java.io.*;

public enum Materials {
    STONE (Material.STONE.ordinal()     , Material.CALCITE.ordinal()               ,  60000e6, 0.1,  2800),
    PLANKS(Material.OAK_PLANKS.ordinal(), Material.WARPED_PLANKS.ordinal()         ,   8500e6, 0.1,   500),
    LOG   (Material.OAK_LOG   .ordinal(), Material.WARPED_HYPHAE.ordinal()         ,   8500e6, 0.1,   500),
    IRON  (Material.IRON_BLOCK.ordinal(), Material.IRON_BLOCK.ordinal()            , 210000e6, 0.3,  7874),
    GOLD  (Material.GOLD_BLOCK.ordinal(), Material.GOLD_BLOCK.ordinal()            ,  78000e6, 0.3, 19300),
    WOOL  (Material.WHITE_WOOL.ordinal(), Material.BLACK_WOOL.ordinal()            ,      1e6, 0.3,   120),;


    Materials(int minBlockID, int maxBlockID, double youngsModule, double poisson, double density) {
        this.minBlockID   = minBlockID;
        this.maxBlockID   = maxBlockID;
        this.youngsModule = youngsModule;
        this.poisson      = poisson;
        this.density      = density;
    }

    public int minBlockID;
    public int maxBlockID;

    public double youngsModule;
    public double poisson;
    public double density;

    public static int getFEMMaterialID(int ID){
        int counter = 0;
        for(Materials v:Materials.values()){
            if(v.minBlockID <= ID && v.maxBlockID >= ID){
                return counter;
            }
            counter ++;
        }
        return -1;
    }

    public static void writeMaterials(String txt) throws IOException {

        BufferedWriter matWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(txt)));

        int counter = 1;
        for(Materials v:Materials.values()){
            matWriter.write("**\n");
            matWriter.write("*MATERIAL, NAME=MAT_" + counter + "\n");
            matWriter.write("*ELASTIC, TYPE=ISOTROPIC" + "\n");
            matWriter.write(v.youngsModule + ", " + v.poisson + "\n");
            matWriter.write("*DENSITY" + "\n");
            matWriter.write(v.density + "\n");
            matWriter.write("**\n");
            matWriter.write("*SOLID SECTION \n");
            matWriter.write("ELSET_" + counter + ", MAT_" + counter + "\n");
            counter ++;
        }
        matWriter.close();
    }
}
