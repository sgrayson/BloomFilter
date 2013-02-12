import java.nio.charset.Charset;
import java.util.BitSet;

public class BloomFilter<E> {
	private BitSet bitset;
	private int expectedNumElems, bitSetSize, numAddedElems, k;
	private double bitsPerElem;
	
	static final Charset charset = Charset.forName("UTF-8");
	
	public BloomFilter(double falsePositiveProbability, int expectedNumElems) {
		this.bitsPerElem = Math.ceil(-(Math.log(falsePositiveProbability) / Math.log(2))) / Math.log(2);
		this.expectedNumElems = expectedNumElems;
		this.k = (int) Math.ceil(-(Math.log(falsePositiveProbability) / Math.log(2)));
		this.bitSetSize = (int) Math.ceil(this.bitsPerElem * this.expectedNumElems);
		this.bitset = new BitSet(this.bitSetSize);
		numAddedElems = 0;
	}
	
	public int[] createHashes(byte[] data, int hashes) {
		int[] result = new int[hashes];
		int hash1 = murmurHash32(data, data.length, 0);
		int hash2 = murmurHash32(data, data.length, hash1);
		for (int i = 0; i < hashes; i++) {
			result[i] = Math.abs((hash1 + i * hash2) % this.bitSetSize);
		} 
		return result;
	}
	
	private int murmurHash32(byte[] data, int length, int seed) {
		int m = 0x5bd1e995, r = 24, h = seed^length;
		for (int i = 0; i < length/4; i++) {
			int k = (data[i*4+0]&0xff) +((data[i*4+1]&0xff)<<8)
					+((data[i*4+2]&0xff)<<16) +((data[i*4+3]&0xff)<<24);
			k *= m;
			k ^= k >>> r;
			k *= m;
			h *= m;
			h ^= k;
		}
		switch (length % 4) {
			case 3: h ^= (data[(length&~3) +2]&0xff) << 16;
			case 2: h ^= (data[(length&~3) +1]&0xff) << 8;
			case 1: h ^= (data[length&~3]&0xff);
					h *= m;
		}
		h ^= h >>> 13;
		h *= m;
		h ^= h >>> 15;
		return h;
	}
	
	public double expectedFalsePositiveProbability() {
		return getFalsePositiveProbability(expectedNumElems);
	}
	
	public double getFalsePositiveProbability() {
		return getFalsePositiveProbability(numAddedElems);
	}
	
	public double getFalsePositiveProbability(double numElems) {
		return Math.pow((1 - Math.exp(-k * (double) numElems / (double) bitSetSize)), k);
	}
	
	public void add(E element) {
		byte[] bytes = element.toString().getBytes(charset);
		int[] hashes = createHashes(bytes, k);
		for (int hash : hashes) {
			bitset.set(hash, true);
		}
		numAddedElems++;
	}
	
	public boolean contains(E element) {
		byte[] bytes = element.toString().getBytes(charset);
		int[] hashes = createHashes(bytes, k);
		for (int hash : hashes) {
			if (!bitset.get(hash)) return false;
		}
		return true;
	}
	
	public void clear() {
		bitset.clear();
		numAddedElems = 0;
	}
	
	public double getExpectedBitsPerElement() {
		return this.bitsPerElem;
	}
	
	public double getBitsPerElement() {
		return this.bitSetSize / (double) numAddedElems;
	}
	
	public int getK() {
		return this.k;
	}
	
	public int size() {
		return this.bitSetSize;
	}
	
	public int count() {
		return this.numAddedElems;
	}

	public void testing() {
		System.out.println("jenkins test");
	}
}















