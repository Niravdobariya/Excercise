import java.util.*;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class MyThreadPoolExecutor implements ExecutorService {

    //---------------States--------------//
    private final static int RUNNING = 0;
    private final static int SHUTDOWN = 1;
    private final static int STOP = 2;
    private final static int TERMINATED = 3;

    private Queue<Runnable> waitingTasks;
    private Set<MyThread> runningThreads;
    private final int threadPoolSize;
    private final Stat stat;
    private final Lock threadLock;
    private final Lock queueLock;
    private final Condition condition;

    private class MyThread implements Runnable {
        Thread t;
        Runnable task;

        MyThread(Runnable task) {
            this.task = task;
            t = new Thread(this);
        }

        @Override
        public void run() {
            doTask(this);
        }


    }

    private class Stat {
        int state;
        int poolSize;
        Lock statLock = new ReentrantLock();

        Stat() {
            state = RUNNING;
            poolSize = 0;
        }

        boolean checkAndSetPoolSize(int expected, int update) {
            statLock.lock();
            try {
                if (expected == poolSize) {
                    poolSize = update;
                    return true;
                } else {
                    return false;
                }
            } finally {
                statLock.unlock();
            }
        }

        boolean checkAndSetState(int newState) {
            statLock.lock();
            try {
                if (newState >= state) {
                    state = newState;
                    return true;
                } else {
                    return false;
                }
            } finally {
                statLock.unlock();
            }
        }

        public int getState() {
            return state;
        }

        public int getPoolSize() {
            return poolSize;
        }
    }

    MyThreadPoolExecutor(int poolSize) {
        threadPoolSize = poolSize;
        stat = new Stat();
        waitingTasks = new LinkedList<>();
        runningThreads = new HashSet<>();
        threadLock = new ReentrantLock();
        queueLock = new ReentrantLock();
        condition = threadLock.newCondition();
    }

    private void decrementThreadPoolSize() {
        int curSize;
        while (!stat.checkAndSetPoolSize(curSize = stat.getPoolSize(), curSize - 1)) {
        }
    }

    private void incrementThreadPoolSize() {
        int curSize;
        while (!stat.checkAndSetPoolSize(curSize = stat.getPoolSize(), curSize + 1)) {
        }
    }

    private void setThreadPoolState(int state) {
        stat.checkAndSetState(state);
    }

    private void doTask(MyThread thread) {
        Runnable task = thread.task;
        try {
            while (task != null || (task = getTask()) != null) {
                if (stat.getState() >= STOP && !Thread.interrupted()) {
                    Thread.currentThread().interrupt();
                }
                if (stat.getState() < STOP) {
                    task.run();
                    task = null;
                }
            }
        } finally {
            removeThread(thread);
        }
    }

    private Runnable getTask() {

        while (true) {
            if (stat.getState() >= STOP) {
                return null;
            }
            final Lock queueLock = this.queueLock;
            queueLock.lock();
            try {
                if (stat.getState() <= SHUTDOWN) {
                    if (!waitingTasks.isEmpty()) {
                        return waitingTasks.poll();
                    } else {
                        return null;
                    }
                }
            } finally {
                queueLock.unlock();
            }
        }
    }

    private boolean createNewThread(Runnable command) {

        if (command == null || stat.getState() >= STOP) {
            return false;
        }
        final MyThread myThread = new MyThread(command);
        final Thread thread = myThread.t;
        boolean added = false;

        final Lock threadLock = this.threadLock;
        threadLock.lock();
        try {
            if (thread.isAlive()) {
                throw new IllegalThreadStateException();
            }
            if (stat.getState() <= SHUTDOWN) {
                incrementThreadPoolSize();
                runningThreads.add(myThread);
                added = true;
            }
        } finally {
            threadLock.unlock();
        }
        if (added) {
            thread.start();
            return true;
        }
        return false;
    }

    private void removeThread(MyThread thread) {
        final Lock threadLock = this.threadLock;
        threadLock.lock();
        try {
            runningThreads.remove(thread);
            decrementThreadPoolSize();
        } finally {
            threadLock.unlock();
        }

        if (stat.getState() <= SHUTDOWN) {
            if (stat.getPoolSize() < threadPoolSize && !waitingTasks.isEmpty()) {
                createNewThread(getTask());
            }
        }

        final Lock queueLock = this.queueLock;
        queueLock.lock();
        try {
            if (stat.getState() >= SHUTDOWN && waitingTasks.isEmpty()) {
                setThreadPoolState(STOP);
                tryTerminate();
            }
        } finally {
            queueLock.unlock();
        }
    }

    private void tryTerminate() {
        if(stat.getPoolSize() > 0 || stat.getState() <= SHUTDOWN) return;
        final Lock threadLock = this.threadLock;
        threadLock.lock();
        try {
            condition.signalAll();
        } finally {
            setThreadPoolState(TERMINATED);
            threadLock.unlock();

        }
    }

    private void printState() {
        int curState = stat.getState();
        switch (curState) {
            case RUNNING:
                System.out.println("RUNNING");
                break;
            case SHUTDOWN:
                System.out.println("SHUTDOWN");
                break;
            case STOP:
                System.out.println("STOP");
                break;
            case TERMINATED:
                System.out.println("TERMINATED");
                break;
        }
    }

    //------------------------------Public Methods-------------------------------//

    @Override
    public void shutdown() {
        setThreadPoolState(SHUTDOWN);
    }

    @Override
    public List<Runnable> shutdownNow() {
        setThreadPoolState(STOP);
        tryTerminate();
        List<Runnable> remainingTask = new ArrayList<>();
        queueLock.lock();
        try {
            while (!waitingTasks.isEmpty()) {
                remainingTask.add(waitingTasks.poll());
            }
        } finally {
            queueLock.unlock();
        }
        return remainingTask;
    }

    @Override
    public boolean isShutdown() {
        return stat.getState() == SHUTDOWN;
    }

    @Override
    public boolean isTerminated() {
        return stat.getState() == TERMINATED;
    }

    @Override
    public boolean awaitTermination(long timeout, TimeUnit unit) throws InterruptedException {
        long time = unit.toNanos(timeout);
        threadLock.lock();
        try {
            while(true) {
                if(stat.getState() == TERMINATED) {
                    return true;
                }
                if(time <= 0) {
                    return false;
                }
                time = condition.awaitNanos(time);
            }
        } finally {
            threadLock.unlock();
        }

    }

    @Override
    public <T> Future<T> submit(Callable<T> task) {
        RunnableFuture<T> futureTask = new FutureTask<>(task);
        execute(futureTask);
        return futureTask;
    }

    @Override
    public <T> Future<T> submit(Runnable task, T result) {
        RunnableFuture<T> futureTask = new FutureTask<>(task, result);
        execute(futureTask);
        return futureTask;
    }

    @Override
    public Future<?> submit(Runnable task) {
        RunnableFuture<?> futureTask = new FutureTask<>(task, null);
        execute(futureTask);
        return futureTask;
    }

    @Override
    public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks) throws InterruptedException {
        List<Future<T>> futureTasks = new ArrayList<>(tasks.size());
        boolean doneAll = false;
        try {
            for (Callable<T> callable : tasks) {
                futureTasks.add(submit(callable));
            }
            for (Future<T> futureTask : futureTasks) {
                if(futureTask.isDone())continue;
                try {
                    futureTask.get();
                } catch (CancellationException | ExecutionException e) { }
            }
            doneAll = true;
            return futureTasks;
        } finally {
            if (!doneAll) {
                for (Future<T> futureTask : futureTasks) {
                    futureTask.cancel(true);
                }
            }
        }
    }

    @Override
    public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks, long timeout, TimeUnit unit) throws InterruptedException {
        if(unit == null) {
            throw new NullPointerException();
        }
        Long totalTime = unit.toNanos(timeout);
        Long finishTime = totalTime + System.nanoTime();
        List<Future<T>> futureTasks = new ArrayList<>(tasks.size());

        boolean doneAll = false;
        try {
            for (Callable<T> callable : tasks) {
                futureTasks.add(submit(callable));
                if(System.nanoTime() > finishTime){
                    return futureTasks;
                }
            }

            for (Future<T> futureTask : futureTasks) {
                if(futureTask.isDone()) continue;
                try {
                    long remainingTime = finishTime - System.nanoTime();
                    futureTask.get(remainingTime,TimeUnit.NANOSECONDS);
                } catch ( CancellationException | ExecutionException e) {

                } catch (TimeoutException e) {
                    return futureTasks;
                }
            }
            doneAll = true;
            return futureTasks;
        } finally {
            if (!doneAll) {
                for (Future<T> futureTask : futureTasks) {
                    futureTask.cancel(true);
                }
            }
        }
    }

    //TODO
    @Override
    public <T> T invokeAny(Collection<? extends Callable<T>> tasks) throws InterruptedException, ExecutionException {
        if(tasks == null) throw new IllegalArgumentException();
        T result = null;
        for(Callable<T> task : tasks) {
            try {
                Future<T> future = submit(task);
                result = future.get();
                return result;
            } catch ( CancellationException | ExecutionException e) {

            }
        }
        if(result == null) {
//            throw new ExecutionException();
        }
        return null;
    }

    //TODO
    @Override
    public <T> T invokeAny(Collection<? extends Callable<T>> tasks, long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
        return null;
    }

    //Not Shared Method
    @Override
    public void execute(Runnable command) {
        if (command == null) {
            throw new NullPointerException();
        }

        if (stat.getState() == RUNNING) {
            if (stat.getPoolSize() < threadPoolSize) {
                if (createNewThread(command)) {
                    return;
                }
            }

            queueLock.lock();
            try {
                waitingTasks.add(command);
                return;
            } catch (IllegalStateException e) {
                throw new RejectedExecutionException();
            } finally {
                queueLock.unlock();
            }

        }
        throw new RejectedExecutionException();

    }

}
