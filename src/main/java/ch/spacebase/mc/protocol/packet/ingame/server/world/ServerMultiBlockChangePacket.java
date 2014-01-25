package ch.spacebase.mc.protocol.packet.ingame.server.world;

import java.io.IOException;

import ch.spacebase.mc.protocol.data.game.BlockChangeRecord;
import ch.spacebase.mc.protocol.data.game.Position;
import ch.spacebase.packetlib.io.NetInput;
import ch.spacebase.packetlib.io.NetOutput;
import ch.spacebase.packetlib.packet.Packet;

public class ServerMultiBlockChangePacket implements Packet {
	
	private BlockChangeRecord records[];
	
	@SuppressWarnings("unused")
	private ServerMultiBlockChangePacket() {
	}
	
	public ServerMultiBlockChangePacket(BlockChangeRecord... records) {
		if(records == null || records.length == 0) {
			throw new IllegalArgumentException("Records must contain at least 1 value.");
		}
		
		this.records = records;
	}
	
	public BlockChangeRecord[] getRecords() {
		return this.records;
	}

	@Override
	public void read(NetInput in) throws IOException {
		int chunkX = in.readInt();
		int chunkZ = in.readInt();
		this.records = new BlockChangeRecord[in.readShort()];
		in.readInt(); // Unneeded size variable
		for(int index = 0; index < this.records.length; index++) {
			short pos = in.readShort();
			short block = in.readShort();
			int x = (chunkX << 4) + (pos >> 12 & 15);
			int y = pos & 255;
			int z = (chunkZ << 4) + (pos >> 8 & 15);
			int id = block >> 4 & 4095;
			int metadata = block & 15;
			this.records[index] = new BlockChangeRecord(new Position(x, y, z), id, metadata);
		}
	}

	@Override
	public void write(NetOutput out) throws IOException {
		int chunkX = this.records[0].getPosition().getX() >> 4;
		int chunkZ = this.records[0].getPosition().getZ() >> 4;
		out.writeInt(chunkX);
		out.writeInt(chunkZ);
		out.writeShort(this.records.length);
		out.writeInt(this.records.length * 4);
		for(BlockChangeRecord record : this.records) {
			out.writeShort((record.getPosition().getX() - (chunkX << 4)) << 12 | (record.getPosition().getZ() - (chunkZ << 4)) << 8 | record.getPosition().getY());
			out.writeShort((record.getId() & 4095) << 4 | record.getMetadata() & 15);
		}
	}
	
	@Override
	public boolean isPriority() {
		return false;
	}

}