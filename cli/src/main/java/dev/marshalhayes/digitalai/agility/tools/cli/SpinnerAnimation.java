package dev.marshalhayes.digitalai.agility.tools.cli;

import com.github.ajalt.mordant.terminal.Terminal;

/**
 * Displays a Braille-dot spinner on the current line while a blocking operation runs.
 *
 * <p>No-ops silently when the terminal is non-interactive (piped, redirected) so machine
 * consumers see clean output. Must be closed (try-with-resources) to clear the spinner line.
 */
public class SpinnerAnimation implements AutoCloseable {

  private static final String[] FRAMES = {"⠋", "⠙", "⠹", "⠸", "⠼", "⠴", "⠦", "⠧", "⠇", "⠏"};

  private final Terminal terminal;
  private final String message;
  private final boolean interactive;
  private volatile boolean running = true;
  private final Thread thread;

  public static SpinnerAnimation start(Terminal terminal, String message) {
    return new SpinnerAnimation(terminal, message);
  }

  private SpinnerAnimation(Terminal terminal, String message) {
    this.terminal = terminal;
    this.message = message;
    this.interactive = terminal.getInfo().getInteractive();

    if (interactive) {
      terminal.getCursor().hide(true);
      this.thread = Thread.ofPlatform().daemon(true).start(this::spin);
    } else {
      this.thread = null;
    }
  }

  private void spin() {
    int i = 0;
    while (running) {
      terminal.rawPrint("\r" + FRAMES[i++ % FRAMES.length] + " " + message, false);
      try {
        Thread.sleep(80);
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
        break;
      }
    }
  }

  @Override
  public void close() {
    running = false;
    if (thread != null) {
      try {
        thread.join(500);
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
      }
      terminal.rawPrint("\r" + " ".repeat(message.length() + 4) + "\r", false);
      terminal.getCursor().show();
    }
  }
}
