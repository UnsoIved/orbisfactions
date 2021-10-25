package io.github.definitlyevil.orbisfactions;

import org.bukkit.Chunk;
import org.bukkit.World;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class ChunkCache {

    private Map<ChunkKey, CachedChunk> chunks = Collections.synchronizedMap(new HashMap<>());

    private CacheModifier modifier = new CacheModifier();

    public CachedChunk getChunk(ChunkKey ck) {
        if(ck == null) throw new IllegalArgumentException("ChunkKey can not be null! ");
        return chunks.get(ck);
    }

    public CachedChunk getChunk(World w, int cx, int cz) {
        ChunkKey ck = new ChunkKey(w, cx, cz);
        return chunks.get(ck);
    }

    public CachedChunk getChunk(String w, int cx, int cz) {
        ChunkKey ck = new ChunkKey(w, cx, cz);
        return chunks.get(ck);
    }

    public CacheModifier getModifier() {
        return modifier;
    }

    public class CacheModifier {
        protected CacheModifier() { }

        public void setCache(ChunkKey ck, CachedChunk status) {
            chunks.put(ck, status);
        }

        public void markWilderness(String w, int cx, int cz) {
            chunks.put(new ChunkKey(
                w, cx, cz
            ), CachedWildernessChunk.instance);
        }

        public void markFaction(String w, int cx, int cz, int factionId, String factionName) {
            chunks.put(new ChunkKey(w, cx, cz), new CachedFactionChunk(factionId, factionName));
        }

        public void remove(ChunkKey ck) { chunks.remove(ck); }
    }


    public interface CachedChunk { }

    public static class CachedWildernessChunk implements CachedChunk {
        public static final CachedWildernessChunk instance = new CachedWildernessChunk();
        private CachedWildernessChunk() { }

        @Override
        public String toString() {
            return "Wilderness{}";
        }
    }

    public static class CachedFactionChunk implements CachedChunk {
        public final int id;
        public final String name;

        public CachedFactionChunk(int id, String name) {
            this.id = id;
            this.name = name;
        }

        @Override
        public String toString() {
            return String.format("Claimed{id=#%d, name=%s}", id, name);
        }
    }

    public static class ChunkKey {
        public final String world;
        public final int cx;
        public final int cz;
        public final int hash;
        private final String str;

        public ChunkKey(Chunk chunk) {
            this(chunk.getWorld(), chunk.getX(), chunk.getZ());
        }

        public ChunkKey(World world, int cx, int cz) {
            this(world.getName(), cx, cz);
        }

        public ChunkKey(String world, int cx, int cz) {
            this.world = world;
            this.cx = cx;
            this.cz = cz;
            str = String.format("%s:%d:%d", world, cx, cz);
            hash = str.hashCode();
        }

        @Override
        public boolean equals(Object obj) {
            if(obj == null || !ChunkKey.class.isAssignableFrom(obj.getClass())) return false;
            ChunkKey ck = (ChunkKey) obj;
            return world.equalsIgnoreCase(ck.world) && cx == ck.cx && cz == ck.cz;
        }

        @Override
        public int hashCode() {
            return hash;
        }

        @Override
        public String toString() {
            return str;
        }
    }

}
