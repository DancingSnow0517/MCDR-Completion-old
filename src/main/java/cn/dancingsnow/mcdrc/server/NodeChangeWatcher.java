package cn.dancingsnow.mcdrc.server;

import com.google.common.util.concurrent.ThreadFactoryBuilder;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import static java.nio.file.StandardWatchEventKinds.*;

public class NodeChangeWatcher extends Thread {

    public static final NodeChangeWatcher INSTANCE = new NodeChangeWatcher();

    static {
        INSTANCE.start();
    }

    private static final ScheduledExecutorService EXECUTOR = Executors.newSingleThreadScheduledExecutor(
            new ThreadFactoryBuilder().setPriority(Thread.MIN_PRIORITY).setDaemon(true).setNameFormat("MCDRC Scheduler").build()
    );

    public static void init() {
        // intentionally empty
    }

    private NodeChangeWatcher() {
        super("MCDRC Watcher Thread");
        this.setDaemon(true);
        this.setPriority(Thread.MIN_PRIORITY);
    }

    private final Path nodePath = Path.of(MCDRCommandServer.modConfig.getNodePath());
    private WatchService watchService;

    @Override
    public synchronized void start() {
        if (this.getState() != State.NEW) {
            throw new IllegalStateException("Thread already started");
        }

        try {
            watchService = FileSystems.getDefault().newWatchService();
            nodePath.getParent().register(watchService, ENTRY_CREATE, ENTRY_MODIFY, ENTRY_DELETE);
        } catch (IOException e) {
            MCDRCommandServer.LOGGER.error("Failed to start watch service, you may need to manually invoke reload command to reload command tree.", e);
        }

        super.start();
    }

    @Override
    public void run() {
        WatchKey key;
        try {
            AtomicBoolean reloading = new AtomicBoolean(false);
            while ((key = watchService.take()) != null) {
                if (key.pollEvents().stream()
                        .filter(Objects::nonNull)
                        .filter(event -> event.context() instanceof Path)
                        .map(event -> (Path) event.context())
                        .anyMatch(path -> path.getFileName().toString().equals(nodePath.getFileName().toString()))) {
                    if (reloading.compareAndSet(false, true)) {
                        EXECUTOR.schedule(() -> {
                            reloading.set(false);
                            MCDRCommandServer.LOGGER.info("MCDR command tree updated, reloading...");
                            MCDRCommandServer.loadNodeData();
                        }, 100, TimeUnit.MILLISECONDS);
                    }
                }
                key.reset();
            }
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        MCDRCommandServer.LOGGER.warn("Watch service exited, you may need to manually invoke reload command to reload command tree.");
    }
}
