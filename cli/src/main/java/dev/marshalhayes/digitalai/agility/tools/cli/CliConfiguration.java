package dev.marshalhayes.digitalai.agility.tools.cli;

import com.github.ajalt.mordant.rendering.Theme;
import com.github.ajalt.mordant.terminal.Terminal;
import com.github.ajalt.mordant.terminal.TerminalInterface;
import com.github.ajalt.mordant.terminal.TerminalInterfaceProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.ServiceLoader;

@Configuration
public class CliConfiguration {

  /**
   * Singleton Mordant terminal. Auto-detects ANSI capability: emits full ANSI on TTY,
   * plain text when piped. Shared across all commands so cursor state is consistent.
   *
   * <p>Mordant's Terminal has no no-arg Java constructor (Kotlin default params without
   * {@code @JvmOverloads}). We use ServiceLoader to discover the platform-specific
   * {@link TerminalInterface} — exactly what Kotlin does when {@code terminalInterface = null}.
   */
  @Bean
  Terminal terminal() {
    var iface = ServiceLoader.load(TerminalInterfaceProvider.class)
        .findFirst()
        .map(TerminalInterfaceProvider::load)
        .orElseThrow(() -> new IllegalStateException(
            "No TerminalInterfaceProvider found on classpath. " +
            "Ensure mordant-jvm-jna or mordant-jvm-graal-ffi is a dependency."));
    return new Terminal(null, Theme.Companion.getDefault(), null, null, null, null, null, 8, null, iface);
  }
}
