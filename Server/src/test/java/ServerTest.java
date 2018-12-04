import org.junit.After;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class ServerTest {
    private volatile static TestListener listener;
    private final CountDownLatch latch = new CountDownLatch(3);

    @BeforeClass
    public static void setUp() {
        Thread serverThread = new Thread(Server::new);
        serverThread.start();
        listener = new TestListener();
    }

    @After
    public void tearDown() {
        listener.reconnect();
    }

    @Test
    public void AonConnection() throws InterruptedException {
        synchronized (latch) {
            new Thread(latch::countDown).start();
            latch.await(100, TimeUnit.MILLISECONDS);
            assertEquals("Connection: /192.168.0.101 установлено", listener.getBuffer());
        }
    }

    @Test
    public void BonMessage() throws InterruptedException {
        synchronized (latch) {
            new Thread(() -> {
                listener.send("Привет, мир");
                latch.countDown();
            }).start();
            latch.await(100, TimeUnit.MILLISECONDS);
            assertEquals("Привет, мир", listener.getBuffer());
        }
    }
}