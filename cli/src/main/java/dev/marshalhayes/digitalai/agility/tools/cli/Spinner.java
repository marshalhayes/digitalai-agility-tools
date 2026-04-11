package dev.marshalhayes.digitalai.agility.tools.cli;

import java.io.PrintWriter;
import java.util.concurrent.Callable;

public class Spinner implements AutoCloseable {
  private static final String[] FRAMES = { "⠋", "⠙", "⠹", "⠸", "⠼", "⠴", "⠦", "⠧", "⠇", "⠏" };

  private final PrintWriter out;
  private final Thread thread;

  private Spinner(PrintWriter out) {
    this.out = out;

    this.thread = Thread.ofVirtual()
        .unstarted(this::spin);
  }

  public static <T> T execute(PrintWriter out, Callable<T> callable) throws Exception {
    try (var spinner = new Spinner(out)) {
      spinner.start();

      return callable.call();
    }
  }

  private void start() {
    thread.start();
  }

  @Override
  public void close() {
    thread.interrupt();

    try {
      thread.join();
    } catch (InterruptedException ignored) {
      Thread.currentThread().interrupt();
    }

    out.print("\r \r");
    out.flush();
  }

  private void spin() {
    int frame = 0;

    try {
      while (!Thread.interrupted()) {
        out.print("\r" + FRAMES[frame++ % FRAMES.length]);
        out.flush();

        Thread.sleep(80);
      }
    } catch (InterruptedException ignored) {
      // sleep was interrupted — time to stop
    }
  }
}
