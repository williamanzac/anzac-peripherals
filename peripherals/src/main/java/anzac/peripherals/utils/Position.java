package anzac.peripherals.utils;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ChunkCoordinates;
import net.minecraftforge.common.ForgeDirection;

public class Position {
	public int x, y, z;
	public ForgeDirection orientation;

	public Position(final int x, final int y, final int z) {
		this(x, y, z, ForgeDirection.UNKNOWN);
	}

	public Position(final int x, final int y, final int z, final ForgeDirection orientation) {
		this.x = x;
		this.y = y;
		this.z = z;
		this.orientation = orientation;
	}

	public Position(final ChunkCoordinates c) {
		this(c.posX, c.posY, c.posZ);
	}

	public Position(final ChunkCoordinates c, int d) {
		this(c.posX, c.posY, c.posZ, ForgeDirection.UNKNOWN);

	}

	public Position(final Position p) {
		this(p.x, p.y, p.z, p.orientation);
	}

	public Position(final TileEntity tile) {
		this(tile.xCoord, tile.yCoord, tile.zCoord);
	}

	public void moveRight(final int step) {
		switch (orientation) {
		case SOUTH:
			x = x - step;
			break;
		case NORTH:
			x = x + step;
			break;
		case EAST:
			z = z + step;
			break;
		case WEST:
			z = z - step;
			break;
		default:
		}
	}

	public void moveLeft(final int step) {
		moveRight(-step);
	}

	public void moveForwards(final int step) {
		switch (orientation) {
		case UP:
			y = y + step;
			break;
		case DOWN:
			y = y - step;
			break;
		case SOUTH:
			z = z + step;
			break;
		case NORTH:
			z = z - step;
			break;
		case EAST:
			x = x + step;
			break;
		case WEST:
			x = x - step;
			break;
		default:
		}
	}

	public void moveBackwards(final int step) {
		moveForwards(-step);
	}

	public void moveUp(final int step) {
		switch (orientation) {
		case SOUTH:
		case NORTH:
		case EAST:
		case WEST:
			y = y + step;
			break;
		default:
		}
	}

	public void moveDown(final int step) {
		moveUp(-step);
	}

	@Override
	public String toString() {
		return "{" + x + ", " + y + ", " + z + "}";
	}

	public Position min(final Position p) {
		return new Position(p.x > x ? x : p.x, p.y > y ? y : p.y, p.z > z ? z : p.z);
	}

	public Position max(final Position p) {
		return new Position(p.x < x ? x : p.x, p.y < y ? y : p.y, p.z < z ? z : p.z);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((orientation == null) ? 0 : orientation.hashCode());
		result = prime * result + x;
		result = prime * result + y;
		result = prime * result + z;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Position other = (Position) obj;
		// if (orientation != other.orientation)
		// return false;
		if (x != other.x)
			return false;
		if (y != other.y)
			return false;
		if (z != other.z)
			return false;
		return true;
	}
}
