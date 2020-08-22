import java.util.concurrent.*;
import java.util.concurrent.locks.*;
import java.util.concurrent.atomic.*;
public class BoundedBlockingQueue { // FIFO.
    
    
    int[] elements; // circular array.
    
    AtomicInteger size; // since both producers/consumers touch this.
    
    Lock e;
    Condition notFull;
    int nextE; // index at which next enqueue will happen.
    
    Lock d;
    Condition notEmpty;
    int nextD;// index at which next dequeue will happen.

    public BoundedBlockingQueue(int capacity) {
        elements = new int[capacity];
        size = new AtomicInteger();
        e = new ReentrantLock();
        notFull = e.newCondition();
        
        d = new ReentrantLock();
        notEmpty = d.newCondition();
    }
    
    private boolean isFull() {
        return size.get() == elements.length;
    }
    
    private boolean isEmpty() {
        return size.get() == 0;
    }
    
    public void enqueue(int element) throws InterruptedException {
        e.lock();
        while(isFull()){
            notFull.await();
        }
        elements[nextE] = element;
        nextE++;
        if (nextE == elements.length) {
            nextE = 0;
        }
        boolean wake = false;
        if (isEmpty()) {
            wake = true;
        }
        size.incrementAndGet();
        e.unlock();
        
        if (wake) {
            d.lock();
            notEmpty.signal();
            d.unlock();
        }
    }
    
    public int dequeue() throws InterruptedException {
        d.lock();
        while(isEmpty()) {
            notEmpty.await();
        }
        int element = elements[nextD];
        nextD++;
        if (nextD == elements.length) {
            nextD = 0;
        }
        boolean wake = false;
        if (isFull()) {
            wake = true;
        }
        size.decrementAndGet();
        d.unlock();
        
        if (wake) {
            e.lock();
            notFull.signal();
            e.unlock();
        }
        
        return element;
    }
    
    public int size() {
        return size.get();
    }

    static class P implements Runnable {

	 BoundedBlockingQueue q;
	 AtomicInteger i;
	 P(BoundedBlockingQueue q) { this.q = q; i = new AtomicInteger();}
         public void run() {
                try {
			int count = i.incrementAndGet();
			q.enqueue(1);
			System.out.println("enqueued " + count);
		} catch(InterruptedException e) {e.printStackTrace();}
         }
    }



    static class C implements Runnable {

	 BoundedBlockingQueue q;
	 AtomicInteger i;
	 C(BoundedBlockingQueue q) { this.q = q; i = new AtomicInteger();}
         public void run() {
                try {
			int count = i.incrementAndGet();
			q.dequeue();
			System.out.println("dequeued " + count);
		} catch(InterruptedException e) {e.printStackTrace();}
         }
    }

   
   static class SizePrinter implements Runnable {

   	BoundedBlockingQueue q;
        SizePrinter(BoundedBlockingQueue q) { this.q = q; }
        public void run() { 
		while(true)
			{
				try { 
					System.out.println("size = " + q.size()); 
					Thread.sleep(2000);  
				} catch(InterruptedException e) { e.printStackTrace(); }
			}
		}
   }


    public static void main(String args[])  throws Exception {
	   int n = 1000;
           BoundedBlockingQueue q = new BoundedBlockingQueue(1000);
	   P p = new P(q);
	   C c = new C(q);

		Thread ts[] = new Thread[1000];
	    for (int i = 0; i < 1000; i++) {
		// Runnable r;
		// if(ThreadLocalRandom.current().nextInt()%2 == 0){r = p;} else {r = c;}
		
		Thread t = new Thread(p);
		t.start();
		ts[i] = t;
		t = new Thread(c); t.start();
           }

//	   for(int i = 0; i < 1000; i++ ){
//		ts[i].join();
//	  }
	   

//	   for (int i = 0; i < 1000; i++) {
  //             Thread t = new Thread(c); t.start();
  //         }
		
//           Thread t = new Thread(new SizePrinter(q)); t.setDaemon(true); t.start(); 
	   
    }
}
