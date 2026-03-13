package base.client.helpers.utils;

import base.client.Client;
import base.client.feature.impl.player.Scaffold;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.Vec3;

public class BlockPosUtil {






    public static boolean placecheck(Direction dir, Vec3 locat, double diff) {
        Direction newdir=dir.getOpposite();     if(newdir.equals(Direction.UP)) {      return true;   }
        else  if(newdir.equals(Direction.DOWN)) {     return true; }
        else if(newdir.equals(Direction.WEST) ) {
            if((Client.instance.packet.LastPosX-locat.x)>diff) {
                return true;
            }
        }
        else if(newdir.equals(Direction.EAST) ) {
            if((locat.x-Client.instance.packet.LastPosX)>diff) {
                return true;
            }
        }
        else if(newdir.equals(Direction.NORTH) ) {
            if((Client.instance.packet.LastPosZ-locat.z)>diff) {
                return true;
            }
        }
        else if(newdir.equals(Direction.SOUTH) ) {
            if((locat.z-Client.instance.packet.LastPosZ)>diff) {
                return true;
            }
        }


        return false;
    }


    public static BlockPos checkforplaceableblock(BlockPos p, boolean up) {
        if(!placeableblock(p)) {  return null;  }
        BlockPos beast=p.east();	BlockPos bdown=p.below();		BlockPos bnorth=p.north();	BlockPos bwest=p.west();BlockPos bsouth=p.south();	 BlockPos bup=p.above();
        if(!placeableblock(beast)) {	return beast;	}
        if(!placeableblock(bwest)) {		return bwest;	}
        if(!placeableblock(bnorth)) {	return bnorth;	}
        if(!placeableblock(bsouth)) {return bsouth;	}
        if(!placeableblock(bdown)) {	return bdown;}
        if(up && !placeableblock(bup)) {		return bup;	}
        return null;
    }

    public static Vec3 fixvec(Vec3 vc3,Direction npd){
        npd=npd.getOpposite();
        if (npd == Direction.UP) {
            vc3 = new Vec3(vc3.x, Math.round(vc3.y), vc3.z);
        } else if (npd == Direction.DOWN) {
            vc3 = new Vec3(vc3.x, Math.round(vc3.y), vc3.z);
        } else if (npd == Direction.EAST) {
            vc3 = new Vec3(Math.round(vc3.x), vc3.y, vc3.z);
        } else if (npd == Direction.WEST) {
            vc3 = new Vec3(Math.round(vc3.x), vc3.y, vc3.z);
        } else if (npd == Direction.NORTH) {
            vc3 = new Vec3(vc3.x, vc3.y, Math.round(vc3.z));
        } else if (npd == Direction.SOUTH) {
            vc3 = new Vec3(vc3.x, vc3.y, Math.round(vc3.z));
        }
        return vc3;
    }


    public static Direction getDirection(BlockPos oldp, BlockPos newp) {
        if(newp.getY()>oldp.getY()) {
            return Direction.DOWN;
        }
        if(newp.getY()<oldp.getY()) {
            return Direction.UP;
        }

        if(newp.getX()>oldp.getX()) {
            return Direction.WEST;
        }
        if(newp.getX()<oldp.getX()) {
            return Direction.EAST;
        }

        if(newp.getZ()>oldp.getZ()) {
            return Direction.NORTH;
        }
        if(newp.getZ()<oldp.getZ()) {
            return Direction.SOUTH;
        }




        return null;
    }







    public static boolean placeableblock(BlockPos p) {
        if(Minecraft.getInstance().level.getBlockState(p).isAir() || !Minecraft.getInstance().level.getBlockState(p).getFluidState().isEmpty() 	) {
            return true;
        }
        return false;
    }

    public static BlockPos generateBlock(BlockPos p,Direction dir) {
        if(dir==Direction.DOWN) { return p.below(); }
        if(dir==Direction.UP) { return p.above(); }
        if(dir==Direction.EAST) { return p.east(); }
        if(dir==Direction.WEST) { return p.west(); }
        if(dir==Direction.NORTH) { return p.north(); }
        if(dir==Direction.SOUTH) { return p.south(); }
        return p;
    }





}
