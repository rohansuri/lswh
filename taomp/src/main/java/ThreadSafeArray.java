import org.checkerframework.checker.units.qual.A;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.atomic.AtomicReferenceArray;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/*

    Have an array interface i.e:
    1) put at an index
    2) get at an index

    BUT also being unbounded. So if the put is for an index out of bounds then we simply grow the array at will.

    The main task is of making this as non-blocking as possible.
    So what are the actors on our class?
    Readers, Writers.

    Is there anything operation based that is special for any of them?
    Well for writers if the position they want to write to exceeds the size of the array,
    then we need to grow our array.
    While we're growing, we cannot accept any new elements.
    Because growing means, creating a new backing array and replacing the current one.
    So while the copy is in progress we cannot allow writers to continue writing.

    So we could identify a writer who wants to resize as a resizer.

    A resizer needs to wait until all current writers have finished writing.
    Also any new writers that come in need to wait for any on going resizing to finish. Only then
    they get to insert at their index.

    On the read size, we could let readers continuing to read from the old array.

    So the main challenge is how to make resizer wait until current writers exit.
    And also how can resizer signal the waiting writers after it is done?

    The first fundamental construct that we need is "how to know if someone is inside the put method"
    i.e. is there an active writer? How can we know this?
    Counter? So the moment the resizer wants to resize it can take a note of the counter
    and spin loop on until it becomes zero? Once it does it makes counter -1 i.e some special value.
    that only resizer can do so that other concurrent writers reading the counter will see this and
    cooperatively decide to wait on a condition.

    So I'm sure we're getting the second bit by doing this i.e. to make writers wait and later get notified
    when a resize is happening.

    But I'm not sure if spinning until writers exit will work?
    I think it works since once someone sees a -1 they block cooperatively.
    And once the resizer sees a 0 it'll try to CAS a -1 and it is the only one who can do that.
    So we're protected by our CAS here to make sure either a writer goes in OR either a resizer goes in.

 */
public class ThreadSafeArray {
    private AtomicReference<AtomicReferenceArray<Integer>> a;
    private AtomicInteger writers;
    private Lock l;
    private Condition resizingDone;
    private static final int RESIZING = -1;

    ThreadSafeArray(int capacity) {
        a = new AtomicReference<>(new AtomicReferenceArray<>(capacity));
        writers = new AtomicInteger();
        l = new ReentrantLock();
        resizingDone = l.newCondition();
    }

    // Position is 0 based. Throws IndexOutOfBoundsException if supplied position is out of bounds.
    int get(int position) {
        return a.get().get(position);
    }

    int length() {
        return a.get().length();
    }

    private void resize(int newSize) {
        AtomicReferenceArray<Integer> old = a.get();
        AtomicReferenceArray<Integer> nw = new AtomicReferenceArray<>(newSize);
        for (int i = 0; i < old.length(); i++) {
            nw.set(i, old.get(i));
        }
        // System.out.printf("resizing from %d to %d in %s\n", old.length(), newSize, Thread.currentThread().getId());
        a.set(nw);
    }

    // Position is 0 based.
    // Array grows if position exceeds current capacity.
    // Writers block if there's a resizing in progress.
    void put(int position, int item) {
        boolean resizeNeeded;
        while (true) {
            // Do I need to resize?
            resizeNeeded = a.get().length() <= position;
            int w = writers.get();
            if (w == RESIZING) {
                // So this is kind of "queueing" up the writers meanwhile a resizing is happening.
                l.lock();
                // After obtaining the lock we must check again if resizing is done.
                // Since in between checking the resizing flag and "going to wait"
                // the resizing may have finished its work. In which case if we still go and wait
                // then we'll forever sleep! Since the resizer has already signalled the waiting writers.
                w = writers.get();
                if (w != RESIZING) {
                    l.unlock();
                    continue;
                }
                // Resizing still in progress, wait for it.
                try {
                    resizingDone.await();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                } finally {
                    l.unlock();
                }
            } else {
                // Try to get myself in either as a writer or resizer?
                if (resizeNeeded) {
                    // I'm a resizer.
                    if (w == 0) {
                        // When I last checked, no one was inside. Is still no one inside?
                        if (writers.compareAndSet(w, RESIZING)) {
                            // If writers are still zero, then I've got in!
                            // After this CAS is successful, no one else can come in.
                            // The reason no one else can come in once we've set to -1 is
                            // all other people who do their load i.e. writers.get()
                            // are already checking that the value loaded is not -1.
                            // And once they know that it is -1 already
                            // even if they themselves needed to resize, they can't and rather wait.
                            // This is the key, everyone reads ONCE makes their decisions on the loaded
                            // value and later verifies if they were the only one using CAS.
                            resize(position + 1);
                            break;
                        }
                        // Someone else got in, loop again.
                    } else {
                        // Someone is already inside, spin loop.
                    }
                } else {
                    // I'm a writer.
                    if (writers.compareAndSet(w, w + 1)) { // Try counting me in.
                        break;
                    }
                }

            }
        }

        // I'm in, I can write.
        a.get().set(position, item);

        if (resizeNeeded) {
            /*
                Is it ok to acquire this lock and then set writers to 0 and later leave the lock?
                At that point if there's a new writer who sees a 0 and doesn't want to resize,
                we're good, it may write.
                If a writer wants to resize, we're good again since we've completed our resizing and
                writing. Also after a new resizer comes in if after that any new writers come in
                and if we're still holding the lock, that is ok as well. They'll all get to wait one by one.
             */

            l.lock();
            try {
                // Since only we can be inside, we can safely set it to 0, without a CAS.
                // Since we've done our write, we set it to 0 counting us out.
                writers.set(0);
                // It is important to do signalling after setting the above counter.
                // If we signal before, then some writers may wake up, check the writers is still RESIZING
                // and then go back to sleep and there'll be no one to wake them up.
                resizingDone.signalAll();
            } finally {
                l.unlock();
            }
        } else {
            writers.decrementAndGet(); // count me out.
        }
    }
}
