package dev.marshalhayes.digitalai.agility.tools.cli.stories;

import com.github.ajalt.mordant.input.InputEvent;
import com.github.ajalt.mordant.input.MouseTracking;
import com.github.ajalt.mordant.rendering.AnsiLevel;
import com.github.ajalt.mordant.rendering.Size;
import com.github.ajalt.mordant.rendering.Theme;
import com.github.ajalt.mordant.terminal.PrintRequest;
import com.github.ajalt.mordant.terminal.Terminal;
import com.github.ajalt.mordant.terminal.TerminalInfo;
import com.github.ajalt.mordant.terminal.TerminalInterface;
import java.util.stream.Collectors;
import kotlin.time.TimeMark;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class StoryRendererTest {

    // ── Capturing terminal helper ────────────────────────────────────────────

    private static final class CapturingTerminalInterface implements TerminalInterface {

        private final TerminalInfo terminalInfo;
        private final StringBuilder captured = new StringBuilder();

        CapturingTerminalInterface(TerminalInfo terminalInfo) {
            this.terminalInfo = terminalInfo;
        }

        @Override
        public TerminalInfo info(AnsiLevel ansiLevel, Boolean hyperlinks, Boolean outputInteractive,
                Boolean inputInteractive) {
            return terminalInfo;
        }

        @Override
        public void completePrintRequest(PrintRequest request) {
            captured.append(request.getText());
            if (request.getTrailingLinebreak()) {
                captured.append('\n');
            }
        }

        @Override
        public String readLineOrNull(boolean echo) {
            return null;
        }

        @Override
        public Size getTerminalSize() {
            return TerminalInterface.DefaultImpls.getTerminalSize(this);
        }

        @Override
        public InputEvent readInputEvent(TimeMark timeMark, MouseTracking mouseTracking) {
            return TerminalInterface.DefaultImpls.readInputEvent(this, timeMark, mouseTracking);
        }

        @Override
        public AutoCloseable enterRawMode(MouseTracking mouseTracking) {
            return TerminalInterface.DefaultImpls.enterRawMode(this, mouseTracking);
        }

        @Override
        public boolean shouldAutoUpdateSize() {
            return TerminalInterface.DefaultImpls.shouldAutoUpdateSize(this);
        }

        String captured() {
            return captured.toString();
        }
    }

    // ── Factory helpers ──────────────────────────────────────────────────────

    private static CapturingTerminalInterface nonTtyInterface() {
        return new CapturingTerminalInterface(
                new TerminalInfo(AnsiLevel.NONE, false, false, false, false));
    }

    private static Terminal nonTtyTerminal(CapturingTerminalInterface iface) {
        return new Terminal(
                AnsiLevel.NONE,
                Theme.Companion.getDefault(),
                null, null, 120, null,
                false, 4, false,
                iface);
    }

    /** Strips trailing whitespace from every line so layout padding doesn't affect assertions. */
    private static String normalize(String output) {
        return output.lines()
                .map(String::stripTrailing)
                .collect(Collectors.joining("\n"));
    }

    private static StoryView fullStory() {
        return new StoryView(
                "S-12345",
                "My Story Name",
                "<p>Story <strong>description</strong> text.</p>",
                "In Progress",
                "High",
                "3",
                "Sprint 5",
                "My Project",
                "Alice, Bob",
                "https://example.com/story/12345");
    }

    // ── Tests ────────────────────────────────────────────────────────────────

    @Test
    void renderFullStory_nonTty_producesExactOutput() {
        var iface = nonTtyInterface();
        new StoryRenderer(nonTtyTerminal(iface)).render(fullStory());

        assertThat(normalize(iface.captured())).isEqualTo(
                "╭─ S-12345 ────────────────────────────────────────────────────────────────────────────────────────────────────────────╮\n" +
                "│ My Story Name                                                                                                        │\n" +
                "╰──────────────────────────────────────────────────────────────────────────────────────────────────────────────────────╯\n" +
                "Status  In Progress    Priority  ● High       Estimate  3\n" +
                "Sprint  Sprint 5       Project  My Project    Owners  Alice, Bob\n" +
                "─ Description ──────────────────────────────────────────────────────────────────────────────────────────────────────────\n" +
                "Story description text.\n" +
                "────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────\n" +
                "Open in browser https://example.com/story/12345");
    }

    @Test
    void renderMinimalStory_nonTty_omitsAbsentSections() {
        var iface = nonTtyInterface();
        new StoryRenderer(nonTtyTerminal(iface))
                .render(new StoryView("S-001", "Minimal Story", null, null, null, null, null, null, null, null));

        assertThat(normalize(iface.captured())).isEqualTo(
                "╭─ S-001 ──────────────────────────────────────────────────────────────────────────────────────────────────────────────╮\n" +
                "│ Minimal Story                                                                                                        │\n" +
                "╰──────────────────────────────────────────────────────────────────────────────────────────────────────────────────────╯\n" +
                "─ Description ──────────────────────────────────────────────────────────────────────────────────────────────────────────");
    }

    @Test
    void renderCriticalPriority_nonTty_showsBulletDot() {
        var iface = nonTtyInterface();
        new StoryRenderer(nonTtyTerminal(iface))
                .render(new StoryView("S-002", "Critical Story", null, null, "Critical", null, null, null, null, null));

        assertThat(normalize(iface.captured())).isEqualTo(
                "╭─ S-002 ──────────────────────────────────────────────────────────────────────────────────────────────────────────────╮\n" +
                "│ Critical Story                                                                                                       │\n" +
                "╰──────────────────────────────────────────────────────────────────────────────────────────────────────────────────────╯\n" +
                "Priority  ● Critical\n" +
                "─ Description ──────────────────────────────────────────────────────────────────────────────────────────────────────────");
    }

    @Test
    void renderHtmlTableDescription_nonTty_stripsEmptyRows() {
        var iface = nonTtyInterface();
        new StoryRenderer(nonTtyTerminal(iface)).render(new StoryView(
                "S-003", "Table Story",
                "<table><tr><th>Header</th></tr><tr><td></td></tr><tr><td>Cell</td></tr></table>",
                null, null, null, null, null, null, null));

        assertThat(normalize(iface.captured())).isEqualTo(
                "╭─ S-003 ──────────────────────────────────────────────────────────────────────────────────────────────────────────────╮\n" +
                "│ Table Story                                                                                                          │\n" +
                "╰──────────────────────────────────────────────────────────────────────────────────────────────────────────────────────╯\n" +
                "─ Description ──────────────────────────────────────────────────────────────────────────────────────────────────────────\n" +
                "┌────────┐\n" +
                "│ Header │\n" +
                "╞════════╡\n" +
                "│ Cell   │\n" +
                "└────────┘");
    }
}
