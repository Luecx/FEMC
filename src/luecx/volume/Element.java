package luecx.volume;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;

import java.util.Arrays;
import java.util.Objects;

import static java.lang.Math.abs;

public class Element {

    private int[] indices;

    private Material mat;

    private Location location;
    private Location displacedLocation;

    private Result result;

    public Element(Block block) {
        this.indices           = new int[8];

        this.mat               = block.getType();

        this.location          = block.getLocation();
        this.displacedLocation = this.location.clone();

        this.result            = new Result();
    }

    public int[] getIndices() {
        return indices;
    }

    public Material getMat() {
        return mat;
    }

    public void hide(){
        location.getWorld().getBlockAt(location).setType(Material.AIR);
        location.getWorld().getBlockAt(this.displacedLocation).setType(Material.AIR);
    }

    public void showDisplaced(float factor){
        if(factor == 0){
            location.getWorld().getBlockAt(this.location).setType(mat);
        }else{
            this.displacedLocation = this.location.clone().add(
                    result.getDx() * factor + 0.5,
                    result.getDy() * factor + 0.5,
                    result.getDz() * factor + 0.5);
            if(location.getWorld().getBlockAt(this.displacedLocation).getType() == Material.AIR){
                location.getWorld().getBlockAt(this.displacedLocation).setType(mat);
            }
        }
    }

    public Result getResult() {
        return result;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Element node = (Element) o;
        return Arrays.equals(getIndices(), node.getIndices()) && getMat() == node.getMat();
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(getMat());
        result = 31 * result + Arrays.hashCode(getIndices());
        return result;
    }
}
