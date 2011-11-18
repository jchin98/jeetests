package perf.test.ehcache246;

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;
import net.sf.ehcache.constructs.blocking.BlockingCache;
import net.sf.ehcache.store.MemoryStoreEvictionPolicy;

public class GetTest {
	private static final String cacheName = "testCache";
	private static final String key1 = "systemparams";
	private static final String key2 = "sr_contents";
	private static final Element nullElementForKey1 = new Element(key1, null);
	private static final Element nullElementForKey2 = new Element(key2, null);
	private static final int maxNumOfThreads = 100;
	private static final int numOfLoops = 10
	0000;
	private static final long delayBetweenReadsInMillis = 100;

	private static BlockingCache testCache;

	private static class ReadValue implements Runnable {

		public void run() {
			final String name = Thread.currentThread().getName();
			for (int i = 0; i < numOfLoops; i++) {
				readCache(name, key1, nullElementForKey1);
				// sleep(delayBetweenReadsInMillis, name);
				readCache(name, key2, nullElementForKey2);
				// sleep(delayBetweenReadsInMillis, name);
			}
		}

		private void readCache(String threadName, String key,
				Element nullElement) {
			Element element1 = testCache.get(key);
			if (element1 == null) {
//				System.out.println(System.currentTimeMillis() + ": "
//						+ threadName + ": cache miss for " + key);
				testCache.put(nullElement);
			} else {
//				System.err.println(System.currentTimeMillis() + ": "
//						+ threadName
//						+ ": it is expected that there is cache miss for "
//						+ key);
			}
		}

		private void sleep(long ms, String name) {
			try {
				System.out.println(name + ": sleeping for " + ms + " ms");
				Thread.sleep(ms);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

	}

	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception {
			// TODO Auto-generated method stub
			long before = System.currentTimeMillis();
			System.out.println(System.currentTimeMillis()
					+ ": Main Thread: ===> Creating a Blocking memory cache ...");
			createBlockingCache();
			prePopulateCache();
			Thread[] threads = createThreads(maxNumOfThreads);
			for (Thread thread : threads) {
				thread.start();
			}
			for (Thread thread : threads) {
				thread.join();
				System.out.println(System.currentTimeMillis()
						+ ": Main Thread: ===> " + thread.getName()
						+ " is done ...");
			}
			System.out.println();
			System.out.println("Total threads = " + maxNumOfThreads);
			System.out.println("Each thread performing number of reads = " + numOfLoops * 2);
			long elapseTime = System.currentTimeMillis() - before;
			System.out.println("Total elapse time = " + elapseTime + " ms");
			long totalReads = maxNumOfThreads * numOfLoops * 2;
			System.out.println("Total number of gets = " + totalReads);
			long elapseTimeInSec = elapseTime / 1000;
			System.out.println("Average reads/s = " + (totalReads/elapseTimeInSec) );
		}

	private static void createBlockingCache() {
		CacheManager cacheManager = CacheManager.getInstance();
		Cache cache = new Cache(cacheName, 100, false, true, 60, 30);
		cache.getCacheConfiguration().setMemoryStoreEvictionPolicyFromObject(
				MemoryStoreEvictionPolicy.LFU);
		testCache = new BlockingCache(cache);
		cacheManager.addCache(testCache);
	}

	private static Thread[] createThreads(int numOfThreads) {
		Thread[] threads = new Thread[numOfThreads];
		for (int i = 0; i < threads.length; i++) {
			threads[i] = new Thread(new ReadValue(), "Thread[" + i + "]");
		}
		return threads;
	}
	
	private static void prePopulateCache() {
		Element val1 = new Element(key1, key1);
		Element val2 = new Element(key2, key2);
		testCache.put(val1);
		testCache.put(val2);
	}
}
