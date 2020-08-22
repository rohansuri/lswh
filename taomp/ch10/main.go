package main

import (
	"sync"
	"sync/atomic"
	"fmt"
	"time"
)

type blockingQueue struct {
	elements []int
	head, tail int
	capacity int32
	size int32

	deqLock sync.Mutex
	notEmpty sync.Cond
	enLock sync.Mutex
	notFull sync.Cond
}

func newBlockingQueue(capacity int) *blockingQueue {
	q := &blockingQueue {}
	q.elements = make([]int, capacity)
	q.capacity = int32(capacity)
	// never copy mutex.
	q.notEmpty.L = &q.deqLock
	q.notFull.L = &q.enLock
	return q
}

func (q *blockingQueue) enqueue(el int) {
	q.enLock.Lock()
	defer q.enLock.Unlock()
	for atomic.LoadInt32(&q.size) == q.capacity {
		// wait until a consumer dequeues.
		fmt.Println("waiting for notFull")
		q.notFull.Wait()
	}
	q.elements[q.head] = el
	q.head = (q.head+1)%len(q.elements)
	size := atomic.AddInt32(&q.size, 1)
	if size == 1 {
		// we were empty, we must signal waiting consumers.
		q.notEmpty.L.Lock()
		defer q.notEmpty.L.Unlock()
		q.notEmpty.Broadcast()
	}
}

func (q *blockingQueue) dequeue() int {
	q.deqLock.Lock()
	defer q.deqLock.Unlock()
	for atomic.LoadInt32(&q.size) == 0 {
		// wait until a consumer enqueues.
		fmt.Println("waiting for notEmpty")
		q.notEmpty.Wait()
	}
	el := q.elements[q.tail]
	q.tail = (q.tail+1)%len(q.elements)
	size := atomic.AddInt32(&q.size, -1)
	if size == q.capacity-1 {
		// we were full, we must signal waiting producers.
		q.notFull.L.Lock()
		defer q.notFull.L.Unlock()
		q.notFull.Broadcast()
	}
	return el
}

func main() {
	q := newBlockingQueue(10)
	wg := sync.WaitGroup{}
	wg.Add(100)
	for i := 0; i < 50; i++ {
		go func(x int){q.enqueue(x); fmt.Println("enqueued ", x); wg.Done()}(i)
		go func(){time.Sleep(time.Second); fmt.Println("dequeued ", q.dequeue()); wg.Done()}()
	}
	wg.Wait()

}
